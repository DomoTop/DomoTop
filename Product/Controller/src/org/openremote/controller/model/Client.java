package org.openremote.controller.model;

public class Client {
   
   private String name, email, pinCode;
   private boolean active;
   private Group group;
   
   public Client(String name, String email, String pinCode, boolean active, Group group)
   {
      this.name = name;
      this.email = email;
      this.pinCode = pinCode;
      this.active = active;
      this.group = group;
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
