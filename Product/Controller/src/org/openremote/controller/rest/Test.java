package org.openremote.controller.rest;

public class Test {
   
   private String string1, string2;
   
   Test(String string1, String string2)
   {
      this.string1 = string1;
      this.string2 = string2;
   }
   
   public String getNote()
   {
      return string1;
   }

   public String getAmount()
   {
      return string2;
   }   
}
