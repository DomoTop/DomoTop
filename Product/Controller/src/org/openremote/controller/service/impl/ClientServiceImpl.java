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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Hex;
import org.apache.log4j.Logger;
import org.openremote.controller.Constants;
import org.openremote.controller.ControllerConfiguration;
import org.openremote.controller.model.Group;
import org.openremote.controller.service.ClientService;
import org.openremote.controller.service.DatabaseService;

/**
 * Get client information out the (request) certificates
 * 
 * @author <a href="mailto:melroy.van.den.berg@tass.nl">Melroy van den Berg</a> 2012
 */

public class ClientServiceImpl implements ClientService 
{  
   private final static Logger logger = Logger.getLogger(Constants.REST_ALL_PANELS_LOG_CATEGORY);
   
   private static final String openssl = "openssl"; 
   private static final String CRTDir = "certs";
   private static final String CSRDir = "csr";
   
   private static String selectClientQuery = "SELECT * FROM client WHERE client_id = ";
   private static String selectAllClientsQuery = "SELECT * FROM client ORDER BY client_creation_timestamp ASC"; 
   private static String insertClientQuery = "INSERT INTO client (client_serial, client_pincode, client_device_name, client_email, client_file_name, client_active, client_creation_timestamp, client_modification_timestamp) VALUES ";
   private static String limitByOne = " LIMIT 1";

   private DatabaseService database;
   private ControllerConfiguration configuration;   
   private String rootCADir = "";
   private String serial = "";
   private String pin;
   private String email;
   private String deviceName;  
           
   /**
    * Get all clients.
    * 
    * @return The result set from the database with all the information from every client
    */
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
   /**
    * Add new client to the database.
    * 
    * @param csrFileName it the file name including the .csr as extension
    * @return int 0 = error with select or insert, 1 insert query went successfully, 2 user already exists
    */
   @Override
   public int addClient(String csrFileName)
   {
      this.parseCSRFile(csrFileName);
      
      logger.error(" Add client pin: "+ this.getPin());
      logger.error(" Add client device name: "+ this.getDeviceName());
      logger.error(" Add client email: "+ this.getEmail());
      
      return this.addClient(this.getPin(), this.getDeviceName(), this.getEmail(), csrFileName);
   }
   
   /**
    * Add new client to the database.
    * 
    * @param pinCode the client pin
    * @param deviceName the client device name
    * @param email the client e-mail address
    * @param fileName the client file name (certificate request file)
    * @return int 0 = error with select or insert, 1 insert query went successfully, 2 user already exists
    */
   @Override
   public int addClient(String pin, String deviceName, String email, String fileName)
   {
      int returnValue = 0;
      int resultValue = -1;
      int numRows = -1;
      
      if(database != null)
      {
         database.doSQL("SELECT client_pincode, client_device_name FROM PUBLIC.client WHERE client_pincode = '" + pin + "' AND client_device_name = '" + deviceName + "' LIMIT 1");
         numRows = database.getNumRows();
      }
      
      // Check if client doesn't exist in the database
      if(numRows == 0)
      {      
         if(database != null)
         {
            resultValue = database.doUpdateSQL(insertClientQuery +
            "('', '" + pin + "', '" + deviceName + "', '" + email + "', '" + fileName + "', FALSE, NOW, NOW);");
         }
         else
         {
            logger.error("Database is not yet set (null)");
         }
         
         if(resultValue >= 1)
         {
            returnValue = 1;
         }
      }
      else
      {
         // ignore a second user with the same device name and pin
         returnValue = 2;
      }
      return returnValue;
   }     
   
   /**
    * Get one client result set from the database.
    * 
    * @param clientID id from the client
    * @return ResultSet the result from the database with client information
    */
   @Override
   public ResultSet getClient(int clientID)
   {
      ResultSet returnValue = null;
      if(database != null)
      {
         returnValue = database.doSQL(selectClientQuery + clientID + limitByOne);
      }
      else
      {
         logger.error("Database is not yet set (null)");
      }
      return returnValue;
   }
   
   /**
    * Updates the client active boolean flag in the database.
    * 
    * @param clientID is the client id 
    * @param active boolean true is active false is non-active
    * @return int value -1 or 0 is incorrect, 1 is action succeed 
    */
   @Override
   public int updateClientStatus(int clientID, boolean active)
   {
      int resultValue = -1;
      
      if(database != null)
      {
         resultValue = database.doUpdateSQL("UPDATE client SET client_active = " + active + " WHERE client_id = " + clientID);
      }
      return resultValue;
   }
   
   /**
    * Update client serial number.
    * @param clientID id client
    * @return value -1 or 0 is , 1 is correct 
    */
   @Override
   public int updateClientSerial(int clientID)
   {
      int resultValue = -1;
      String clientCRTFileName = "";
      
      try 
      {
         ResultSet resultSet = this.getClient(clientID);
         while(resultSet.next())
         {
            String clientFileName = resultSet.getString("client_file_name");
            clientCRTFileName = clientFileName.substring(0, clientFileName.lastIndexOf('.'));
            clientCRTFileName += ".crt";
         }
         this.free();
      } 
      catch (SQLException e) 
      {
         logger.error("SQL exception: " + e.getMessage());
      }
      
      if(!clientCRTFileName.isEmpty())
      {
         serial = this.getSerial(clientCRTFileName);
      }
      else
      {
         logger.error("File name is empty");
      }
            
      if(database != null && !serial.isEmpty())
      {
         resultValue = database.doUpdateSQL("UPDATE client SET client_serial = '" + serial + "' WHERE client_id = " + clientID);
      }      
      return resultValue;
   }
   
   @Override
   public int clearClientSerial(int clientID)
   {
      int resultValue = -1;
      
      if(database != null)
      {
         resultValue = database.doUpdateSQL("UPDATE client SET client_serial = '' WHERE client_id = " + clientID);
      }      
      return resultValue;
   }
   
   @Override
   public String getSerial()
   {
      return serial;
   }
   
   /**
    * Close the result set.
    */
   @Override
   public void free()
   {
      database.free();
   }
   
   /**
    * Returns the number of clients.
    * Note: You should use getClients() first and directly a getNumClients()
    * 
    * @see #getClients()
    * 
    * @return int of the number of clients
    */
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
   
   /**
    * Sets the database.
    * 
    * @param database service
    */
   
   public void setDatabase(DatabaseService database)
   {
      this.database = database;
   } 
   
   /**
    * Sets the configuration.
    * 
    * @param configuration the new configuration
    */   
   public void setConfiguration(ControllerConfiguration configuration)
   {
      this.configuration = configuration;
   } 
   
   private String getSerial(String crtFileName)
   {
      String returnValue = "";
      String message = this.executeOpenSSLCommand(CRTDir, crtFileName, true);
      
      returnValue = message.substring(message.indexOf("serial=") + 7);
      returnValue = returnValue.substring(0, returnValue.indexOf("\n"));      
      return returnValue;
   }
   
   private void parseCSRFile(String csrFileName)
   {
      // init
      pin = "";
      email = "";
      deviceName = "";
      
      String message = this.executeOpenSSLCommand(CSRDir, csrFileName, false);
      
      // Get pin
      try
      {
         String publicKey = message.substring(message.indexOf("KEY-----") + 9, message.lastIndexOf("-----END") - 1);
         if(!publicKey.isEmpty())
         {
            pin = generateMD5Sum(publicKey);
            pin = pin.substring(pin.length() - 4, pin.length());
         }
         else
         {
            pin = "<i>No public key</i>";
         }
      }
      catch(IndexOutOfBoundsException e)
      {
         logger.error("Parsing error: " + e.getMessage());
      }
      // Get email
      try
      {
         email = message.substring(message.indexOf("email:") + 6);
         email = email.substring(0, email.indexOf("\n"));
      }
      catch(IndexOutOfBoundsException e)
      {
         logger.error("Parsing error: " + e.getMessage());
      }
      
      // Get device name
      try
      {
         deviceName = message.substring(message.indexOf("CN=") + 3);
         deviceName = deviceName.substring(0, deviceName.indexOf("\n"));
      }
      catch(IndexOutOfBoundsException e)
      {
         logger.error("Parsing error: " + e.getMessage());
      }      
   }
   
   private String getPin()
   {
      return pin;
   }
   
   private String getEmail()
   {
      return email;
   }

   private String getDeviceName() 
   {
      return deviceName;
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
         command.add("-text");
      }
      command.add("-in"); // input file
      command.add(path + "/" + fileName); // file path
            
      if(rootCADir.isEmpty())
      {
         this.rootCADir = configuration.getCaPath();
      }
      
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
   
   private String generateMD5Sum(String message)
   {
      byte[] resultByte = null;
      
      try
      {
         final MessageDigest messageDigest = MessageDigest.getInstance("MD5");
         messageDigest.reset();
         messageDigest.update(message.getBytes(Charset.forName("UTF8")));
         resultByte = messageDigest.digest();
      }
      catch (NoSuchAlgorithmException e)
      {
         logger.error(e.getMessage());
      }      
      return new String(Hex.encodeHex(resultByte));
   }   
}
