package org.openremote.controller.model;

public class Group {
   
   private String name;
   private int level;
   
   public Group(String name, int level)
   {
      this.name = name;
      this.level = level;
   }
   
   public Group(String name)
   {
      this.name = name;
      this.level = 0;
   }
   
   public String getName()
   {
      return name;
   }
   
   public int getLevel()
   {
      return level;
   }

}
