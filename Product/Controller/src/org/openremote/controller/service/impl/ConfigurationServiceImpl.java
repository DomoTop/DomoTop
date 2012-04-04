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

import java.sql.ResultSet;
import java.sql.SQLException;

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

      if (database != null) {
         resultValue = database.doUpdateSQL("UPDATE configuration SET configuration_value = '" + value + "' WHERE " +
         		"configuration_name = '" + name + "'");
      }
      return resultValue;
   }
   
   /**
    * Get all items (configurations values and names) from the database
    * @return resultSet with the result
    */
   @Override
   public ResultSet getAllItems()
   {
      ResultSet result = null;
      if(database != null)
      {
         result = database.doSQL("SELECT * FROM configuration");
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
       try {
           ResultSet result = database.doSQL("SELECT configuration_value FROM configuration WHERE configuration_name = '" + name + "'");
           result.next();
           returnValue = result.getString("configuration_value");
           database.free();
       } catch (SQLException e) {
           return e.getMessage();
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

      if (database != null) {
         resultValue = database.doUpdateSQL("UPDATE configuration SET configuration_value = '' WHERE configuration_name = '" + name + "' LIMIT 1");
      }
      return resultValue;
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
}
