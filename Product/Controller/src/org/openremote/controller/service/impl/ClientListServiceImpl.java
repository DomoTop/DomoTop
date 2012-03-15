package org.openremote.controller.service.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.openremote.controller.model.Client;
import org.openremote.controller.model.Group;
import org.openremote.controller.service.ClientListService;

public class ClientListServiceImpl implements ClientListService 
{   
   private  final static Group noGroup = new Group("No Group", 0);

   private List<Client> getClientsAuthorized(String path) throws NullPointerException
   {
      String fileName;
      List<Client> output = new ArrayList<Client>();
      File folder = new File(path);
      File[] listOfFiles = folder.listFiles(); 
  
      for (int i = 0; i < listOfFiles.length; i++) 
      {
         if (listOfFiles[i].isFile()) 
         {
            fileName = listOfFiles[i].getName();
            if (fileName.endsWith(".crt") && !fileName.equals("myca.crt"))
            {
               //openssl x509 -subject -enddate -serial -noout -in ./certs/vincent.crt                
               output.add(new Client(fileName, "email", "ioed823", true, noGroup));
            }
         }
      }
      return output;
   }

   private List<Client> getClientsNotAuthorized(String path) throws NullPointerException
   {
      String fileName;
      List<Client> output = new ArrayList<Client>();
      File folder = new File(path);
      File[] listOfFiles = folder.listFiles(); 
     
      for (int i = 0; i < listOfFiles.length; i++) 
      {
         if (listOfFiles[i].isFile()) 
         {
            fileName = listOfFiles[i].getName();
            if (fileName.endsWith(".csr"))
            {
               //openssl x509 -subject -enddate -serial -noout -in ./certs/vincent.crt
               output.add(new Client(fileName, "email", "ioed823", false, noGroup));
            }
         }
      }
      return output;
   }
   
   public List<Client> getClientList() throws NullPointerException
   {
      List<Client> clients =  new ArrayList<Client>();
      clients.add(new Client("Vincent Kriek", "vincent@vincentkriek.nl", "2AE9", false, noGroup));
      clients.addAll(this.getClientsNotAuthorized("/usr/share/tomcat6/cert/ca/csr"));
      clients.addAll(this.getClientsAuthorized("/usr/share/tomcat6/cert/ca/certs"));
      return clients;
   }
}
