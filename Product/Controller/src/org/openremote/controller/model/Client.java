package org.openremote.controller.model;

public class Client {
   
   private int id;
   private String name, email, pinCode, fileName;
   private boolean active;
   private Group group;
   
   public Client(int id, String name, String email, String pinCode, String fileName, boolean active, Group group)
   {
      this.id = id;
      this.name = name;
      this.email = email;
      this.pinCode = pinCode;
      this.fileName = fileName;
      this.active = active;
      this.group = group;
   }

   public int getId()
   {
      return id;
   }
   
   public String getName()
   {
      return name;
   }

   public String getEmail()
   {
      return email;
   }
   
   public String getPinCode()
   {
      return pinCode;
   } 
   
   public String getFileName()
   {
      return fileName;
   } 
   
   public boolean getActive()
   {
      return active;
   }  
   
   public String getGroupName()
   {
      return group.getName();
   }  
   
   public int getGroupLevel()
   {
      return group.getLevel();
   }  
}
