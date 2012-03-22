package org.openremote.controller.service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Hex;
import org.apache.log4j.Logger;
import org.openremote.controller.Constants;
import org.openremote.controller.ControllerConfiguration;
import org.openremote.controller.model.Client;
import org.openremote.controller.model.Group;
import org.openremote.controller.service.ClientService;
import org.openremote.controller.service.DatabaseService;
import org.openremote.controller.spring.SpringContext;
import org.springframework.orm.jpa.vendor.Database;

/**
 * Get client information out the (request) certificates
 * 
 * @author <a href="mailto:melroy.van.den.berg@tass.nl">Melroy van den Berg</a> 2012
 */

public class ClientServiceImpl implements ClientService 
{  
   private final static Logger logger = Logger.getLogger(Constants.REST_ALL_PANELS_LOG_CATEGORY);
   
   private DatabaseService database;
   private static String selectAllClientsQuery = "SELECT * FROM CLIENT"; // ORDER BY date
   
   //private static final String rootCADir = ControllerConfiguration.readXML().getCaPath();
   private static final String rootCADir = "/usr/share/tomcat6/cert/ca";
   
   private final static Group noGroup = new Group("admin", 0);

   private static final String openssl = "openssl"; 
   private static final String CRTDir = "/certs";
   private static final String CSRDir = "/csr";
   
   private int uniqueClientID = 0;
   private Map<String,String> trustedClients = new HashMap<String,String>();  
   
   private List<Client> getClientsAuthorized(String path) throws NullPointerException
   {
      String fileName, userName;
      List<Client> output = new ArrayList<Client>();
      File[] listOfFiles = this.getListFromPath(path);
      
      for (int i = 0; i < listOfFiles.length; i++) 
      {
         if (listOfFiles[i].isFile()) 
         {
            fileName = listOfFiles[i].getName();

            if (fileName.endsWith(".crt") && !fileName.equals("myca.crt"))
            {
               userName = fileName.substring(0, fileName.lastIndexOf('.'));
               
               trustedClients.put(userName, fileName);              
               String message = executeOpenSSLCommand(path, fileName, true);
               
               int serial = 0;
               String username = "<i>Undefined</i>", serialString = "";
               try
               {
                  username = message.substring(message.indexOf("CN=") + 3);
                  username = username.substring(0, username.indexOf("\n"));
               }
               catch(IndexOutOfBoundsException e)
               {
                  logger.error(e.getMessage());
               }
               
               try
               {
                  serialString = message.substring(message.indexOf("serial=") + 7);
                  serialString = serialString.substring(0, serialString.indexOf("\n"));
                  serial = Integer.parseInt(serialString);
               }   
               catch(IndexOutOfBoundsException e)
               {
                  logger.error(e.getMessage());
               }
               catch(NumberFormatException e)
               {
                  logger.error(e.getMessage() + " Serial: " + serialString);
               }
               output.add(new Client(uniqueClientID, serial, username, "email", "-", fileName, true, noGroup));
               uniqueClientID++;
            }
         }
      }
      return output;
   }

   private List<Client> getClientsNotAuthorized(String path) throws NullPointerException
   {
      String fileName, userName;
      String username = "<i>Undefined</i>";
      String pinCode = "<i>Undefined</i>";
      String publicKey = "";
      
      List<Client> output = new ArrayList<Client>();
      File[] listOfFiles = this.getListFromPath(path);
     
      for (int i = 0; i < listOfFiles.length; i++) 
      {
         if (listOfFiles[i].isFile()) 
         {
            fileName = listOfFiles[i].getName();
            if (fileName.endsWith(".csr"))
            {
               userName = fileName.substring(0, fileName.lastIndexOf('.'));
               
               // Check if the file is already trusted, than don't display the client (again)
               if(!trustedClients.containsKey(userName))
               {
                  String message = executeOpenSSLCommand(path, fileName, false);
                                    
                  
                  try
                  {
                     username = message.substring(message.indexOf("CN=") + 3);
                     username = username.substring(0, username.indexOf("/"));
                  }
                  catch(IndexOutOfBoundsException e)
                  {
                     logger.error(e.getMessage());
                  }

                  try
                  {
                     publicKey = message.substring(message.indexOf("KEY-----") + 9, message.lastIndexOf("-----END") - 1);
                     if(!publicKey.isEmpty())
                     {
                        pinCode = generateMD5Sum(publicKey);
                        pinCode = pinCode.substring(pinCode.length() - 4, pinCode.length());
                     }
                     else
                     {
                        pinCode = "<i>No public key</i>";
                     }
                  }
                  catch(IndexOutOfBoundsException e)
                  {
                     logger.error(e.getMessage());
                  }
                  catch (NoSuchAlgorithmException e)
                  {
                     logger.error(e.getMessage());
                  }                  
                  
                  output.add(new Client(uniqueClientID, 0, username, "email", pinCode, fileName, false, noGroup));
                  uniqueClientID++;
               }
            }
         }
      }
      return output;
   }
   
   private String generateMD5Sum(String message) throws NoSuchAlgorithmException
   {
      final MessageDigest messageDigest = MessageDigest.getInstance("MD5");
      messageDigest.reset();
      messageDigest.update(message.getBytes(Charset.forName("UTF8")));
      final byte[] resultByte = messageDigest.digest();
      return new String(Hex.encodeHex(resultByte));
   }
   
   private File[] getListFromPath(String path)
   {
      File folder = new File(path);
      File[] listOfFiles = folder.listFiles();
      return listOfFiles;
   }
   
   private String executeOpenSSLCommand(String path, String fileName, boolean isCert)
   {
      List<String> command = new ArrayList<String>();
      command.add(openssl); // command
      if(isCert) // CRT
      {
         command.add("x509");
         command.add("-subject");
         command.add("-enddate");
         command.add("-serial");
         command.add("-noout");
      }
      else // CSR
      {
         command.add("req");
         command.add("-subject");
         command.add("-noout");
         command.add("-pubkey");
      }
      command.add("-in"); // input file
      command.add(path + "/" + fileName); // file path
      
      ProcessBuilder pb = new ProcessBuilder(command);
      pb.directory(new File(rootCADir));

      Process p = null;
      StringBuffer buffer = new StringBuffer();
      try {
         p = pb.start();
         p.waitFor();
         
         BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
          
         String line = null;
         while ( (line = br.readLine()) != null) {
             buffer.append(line).append("\n");
         }
         
      } catch (IOException e) {
         logger.error(e.getMessage());
      } catch (InterruptedException e) {
         logger.error(e.getMessage());
      }
      
      return buffer.toString();
   }
   
   @Deprecated
   public List<Client> getClientList() throws NullPointerException
   {
      trustedClients.clear();
      
      List<Client> clients =  new ArrayList<Client>();
      List<Client> authorizedClients = this.getClientsAuthorized(rootCADir + CRTDir);
      List<Client> notAuthorizedClients = (this.getClientsNotAuthorized(rootCADir + CSRDir));
      
      clients.addAll(notAuthorizedClients);
      clients.addAll(authorizedClients);
      return clients;
   }
   
   @Override
   public ResultSet getClients()
   {
      ResultSet returnValue = null;
      if(database != null)
      {
         returnValue = database.doSQL(selectAllClientsQuery);
      }
      else
      {
         logger.error("Database is not yet set (null)");
      }
      return returnValue;
   }
   
   @Override
   public int addClient(String pinCode, String deviceName, String email, String fileName)
   {
      int resultValue = -1;
      if(database != null)
      {
         resultValue = database.doUpdateSQL("INSERT INTO PUBLIC.CLIENT (CLIENT_ID, CLIENT_SERIAL, CLIENT_PINCODE, CLIENT_DEVICE_NAME, CLIENT_EMAIL, CLIENT_FILE_NAME, CLIENT_ACTIVE, CLIENT_CREATION_TIMESTAMP, CLIENT_MODIFICATION_TIMESTAMP, CLIENT_GROUP_ID) " +
         "VALUES " +
         "(null, '', '" + pinCode + "', '" + deviceName + "', '" + email + "', '" + fileName + "', FALSE, NOW, NOW, null);");
      }
      else
      {
         logger.error("Database is not yet set (null)");
      }
            
      return resultValue;      
   }     
   
   @Override
   public int getNumClients()
   {
      int newNum = -1;
      if(database != null)
      {
         newNum = database.getNumRows();
      }
      return newNum;
   }
   
   
   public void setDatabase(DatabaseService database)
   {
      this.database = database;
   } 
}
