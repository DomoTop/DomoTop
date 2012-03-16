package org.openremote.controller.service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openremote.controller.Constants;
import org.openremote.controller.ControllerConfiguration;
import org.openremote.controller.model.Client;
import org.openremote.controller.model.Group;
import org.openremote.controller.service.ClientListService;

/**
 * @author <a href="mailto:melroy.van.den.berg@tass.nl">Melroy van den Berg</a> 2012
 */

public class ClientListServiceImpl implements ClientListService 
{  
   private final static Logger logger = Logger.getLogger(Constants.REST_ALL_PANELS_LOG_CATEGORY);
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
               
               String username = "<i>Undefined</i>";
               try
               {
                  username = message.substring(message.indexOf("CN=") + 3);
                  username = username.substring(0, username.indexOf("\n"));
               }
               catch(IndexOutOfBoundsException e)
               {
                  logger.error(e.getMessage());
               }
               output.add(new Client(uniqueClientID, username, "email", "-", fileName, true, noGroup));
               uniqueClientID++;
  
            }
         }
      }
      return output;
   }

   private List<Client> getClientsNotAuthorized(String path) throws NullPointerException
   {
      String fileName, userName;
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
                  
                  String username = "<i>Undefined</i>";
                  try
                  {
                     username = message.substring(message.indexOf("CN=") + 3);
                     username = username.substring(0, username.indexOf("/"));
                  }
                  catch(IndexOutOfBoundsException e)
                  {
                     logger.error(e.getMessage());
                  }
                  
                  String pinCode = "<i>Undefined</i>";
                  try
                  {
                     int index = message.lastIndexOf("-----END") - 1;
                     pinCode = message.substring(index - 4, index);
                  }
                  catch(IndexOutOfBoundsException e)
                  {
                     logger.error(e.getMessage());
                  }                  
                  
                  output.add(new Client(uniqueClientID, username, "email", pinCode, fileName, false, noGroup));
                  uniqueClientID++;
               }
            }
         }
      }
      return output;
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
}
