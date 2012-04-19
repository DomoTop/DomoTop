/* OpenRemote, the Home of the Digital Home.
* Copyright 2008-2012, OpenRemote Inc.
*
* See the contributors.txt file in the distribution for a
* full listing of individual contributors.
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Affero General Public License as
* published by the Free Software Foundation, either version 3 of the
* License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU Affero General Public License for more details.
*
* You should have received a copy of the GNU Affero General Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package org.openremote.controller.service.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.openremote.controller.Constants;
import org.openremote.controller.service.DatabaseService;
import org.openremote.controller.service.ConfigurationService;

/**
 * The implementation for dynamic configuration settings service;
 * 
 * @author <a href="mailto:vincent.kriek@tass.nl">Vincent Kriek</a> 2012-03-29
 * @author <a href="mailto:melroy.van.den.berg@tass.nl">Melroy van den Berg</a> 2012
 */
public class ConfigurationServiceImpl implements ConfigurationService 
{
   private DatabaseService database;
   private boolean reboot = false;
   
   private final static Logger logger = Logger.getLogger(Constants.SERVICE_LOG_CATEGORY);
   
   /**
    * Update a configuration item
    *
    * @param name The key of the configuration item
    * @param value The value of the configuration item
    * @return int value -1 or 0 is incorrect, 1 is action succeed
    */
   @Override
   public int updateItem(String name, String value)
   {
      int resultValue = -1;
      PreparedStatement preparedStatement = null;
      
      if (database != null) 
      {        
         try
         {            
            preparedStatement = database.createPrepareStatement("UPDATE configuration SET configuration_value = ? WHERE configuration_name = ?");
            preparedStatement.setString(1, value);
            preparedStatement.setString(2, name);
            resultValue = database.doUpdateSQL(preparedStatement);
         } catch (SQLException e) {
            logger.error("SQL Exception: " + e.getMessage());
         }
      }
      return resultValue;
   }
   
   /**
    * Add or update a configuration item
    *
    * @param name The key of the configuration item
    * @param value The value of the configuration item
    */
   @Override
   public int updateItem(String name, boolean value) 
   {
      int resultValue = -1;
      String stringValue = "not_defiend";
      PreparedStatement preparedStatement = null;
      
      if(value)
      {
         stringValue = "true";
      }
      else
      {
         stringValue = "false";
      }
      
      if (database != null) 
      {
         try
         {            
            preparedStatement = database.createPrepareStatement("UPDATE configuration SET configuration_value = ? WHERE configuration_name = ?");
            preparedStatement.setString(1, stringValue);
            preparedStatement.setString(2, name);
            resultValue = database.doUpdateSQL(preparedStatement);
         } catch (SQLException e) {
            logger.error("SQL Exception: " + e.getMessage());
         }
      }
      return resultValue;
   }
   
   /**
    * Get all items (configurations values and names) from the database
    * 
    * @return resultSet with the result
    */
   @Override
   public ResultSet getAllItems()
   {
      ResultSet result = null;
      if(database != null)
      {
         // Get all configuration except the password
         result = database.doSQL("SELECT * FROM configuration WHERE configuration_name != 'composer_password'");
      }      
      return result;
   }
   

   /**
    * Retrieve a configuration item
    *
    * @param name The key of the configuration item
    * @return The  of the configuration item
    */
   @Override
   public String getItem(String name)
   {
      String returnValue = "";
      PreparedStatement preparedStatement = null;
      ResultSet resultSet = null;
      
       try {
          if(database != null)
          {
             try
             {            
                preparedStatement = database.createPrepareStatement("SELECT configuration_value FROM configuration WHERE configuration_type = 'string' AND  configuration_name = ?");
                preparedStatement.setString(1, name);
                resultSet = database.doSQL(preparedStatement);
             } catch (SQLException e) {
                logger.error("SQL Exception: " + e.getMessage());
             }
             
             if(resultSet != null) 
             {
                resultSet.next();
                returnValue = resultSet.getString("configuration_value");
             }
             database.free();
          }
       } catch (SQLException e) {
           return e.getMessage();
       }       
       return returnValue;
   }

   /**
    * Ask if the value of the item is a boolean or not
    *
    * @param name The key of the configuration item
    * @return true if the value is a boolean and otherwise false
    */
   @Override
   public boolean isItemValueBoolean(String name) {
      String configurationType = "";
      PreparedStatement preparedStatement = null;
      ResultSet resultSet = null;
      
      boolean returnValue = false;

      if(database != null) 
      {
         try
         {            
            preparedStatement = database.createPrepareStatement("SELECT configuration_type FROM configuration WHERE configuration_name = ?");
            preparedStatement.setString(1, name);
            resultSet = database.doSQL(preparedStatement);
            
            if(resultSet != null) {
               resultSet.next();
               configurationType = resultSet.getString("configuration_type");
            }
            database.free();
         } catch (SQLException e) {
            logger.error("SQL Exception: " + e.getMessage());
         }
      }
      
      if(configurationType.equals("boolean"))
      {
         returnValue = true; 
      }
      
      return returnValue;
   }
   
   /**
    * Check if the pin check is enabled or disabled
    * 
    * @return true if the pin check is enabled otherwise false
    */
   @Override
   public boolean isPinCheckActive()
   {
      return this.getBooleanItem("pin_check");
   }

   /**
    * Retrieve a configuration item
    *
    * @param name The key of the configuration item
    * @return The value of the configuration item
    */
   @Override
   public boolean getBooleanItem(String name) 
   {
      boolean returnValue = false;
      PreparedStatement preparedStatement = null;
      ResultSet resultSet = null;
      
      if(database != null)
      {         
         try
         {            
            preparedStatement = database.createPrepareStatement("SELECT configuration_value FROM configuration WHERE configuration_type = 'boolean' AND configuration_name = ?");
            preparedStatement.setString(1, name);
            resultSet = database.doSQL(preparedStatement);
            
            if(resultSet != null) 
            {
               resultSet.next();
               returnValue = resultSet.getString("configuration_value").equals("true") ? true : false;
            }
         } catch (SQLException e) {
            logger.error("SQL Exception: " + e.getMessage());
         }
         database.free();
      }
  
      return returnValue;
   } 

   /**
    * Empty a configuration value of the configuration name specified
    *
    * @param name The key of the configuration item
    * @return int value -1 or 0 is incorrect, 1 is action succeed
    */
   @Override
   public int emptyItem(String name)
   {
      int resultValue = -1;
      PreparedStatement preparedStatement = null;
      
      if (database != null) 
      {
         try
         {            
            preparedStatement = database.createPrepareStatement("UPDATE configuration SET configuration_value = '' WHERE configuration_name = ? LIMIT 1");
            preparedStatement.setString(1, name);
            resultValue = database.doUpdateSQL(preparedStatement); 
         } catch (SQLException e) {
            logger.error("SQL Exception: " + e.getMessage());
         }
      }
      return resultValue;
   }
   
   /**
    * Save the boolean of the reboot flag
    * 
    * @param reboot boolean
    */
   @Override
   public void setReboot()
   {
      this.reboot = true;
   }

   /**
    * Get the reboot flag
    * 
    * @return reboot boolean
    */
   @Override
   public boolean shouldReboot()
   {
      return reboot;
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
    * Free the result set
    */
   @Override
   public void free() {
      this.database.free();      
   }
   
   /**
    * Enable the authentication in web.xml. Requires OpenRemote to be restarted
    * @param enable True if you want to enable
    * @throws IOException
    */
   public void setAuthentication(boolean enable) throws IOException
   {
      String curDir = System.getProperty("user.dir");

      File security = new File(curDir + "/webapps/controller/WEB-INF/security.xml");
      BufferedReader in = new BufferedReader(new FileReader(security));
      StringBuilder security_contents = new StringBuilder();
      String line = "";
      
      while((line = in.readLine()) != null) {
         security_contents.append(line + "\n");
      }
      
      in.close();

      File file = new File(curDir + "/webapps/controller/WEB-INF/web.xml");
      in = new BufferedReader(new FileReader(file));
      StringBuilder contents = new StringBuilder();
      line = "";
      
      while((line = in.readLine()) != null) {
         contents.append(line + "\n");
      }
      in.close();

      BufferedWriter out = new BufferedWriter(new FileWriter(file, false));

      if(enable) {
         out.write(contents.toString().replace("</web-app>", security_contents.toString()));
      } else {
         out.write(contents.toString().replace(security_contents.toString(), "</web-app>"));
      }
      
      out.close();

   }
}
