/* OpenRemote, the Home of the Digital Home.
* Copyright 2008-2011, OpenRemote Inc.
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
package org.openremote.controller.service;

import java.io.IOException;
import java.sql.ResultSet;

/**
 * The service for dynamic configuration settings.
 * 
 * @author <a href="mailto:vincent.kriek@tass.nl">Vincent Kriek</a> 2012-03-29
 * @author <a href="mailto:melroy.van.den.berg@tass.nl">Melroy van den Berg</a> 2012
 */
public interface ConfigurationService 
{
   /**
    * Add or update a configuration item
    *
    * @param name The key of the configuration item
    * @param value The value of the configuration item
    */
   public int updateItem(String name, String value);

   /**
    * Add or update a configuration item
    *
    * @param name The key of the configuration item
    * @param value The value of the configuration item
    */
   public int updateItem(String name, boolean value);
   
   /**
    * Ask if the value of the item is a boolean or not
    *
    * @param name The key of the configuration item
    * @return true if the value is a boolean and otherwise false
    */
   public boolean isItemValueBoolean(String name);
   
   /**
    * Check if the pin check is enabled or disabled
    * 
    * @return true if the pin check is enabled otherwise false
    */
   boolean isPinCheckActive();
   /**
    * Check if the configuration item is disabled
    * @return true if disabled otherwise false
    */
   boolean isDisabled(String name);
   /**
    * Retrieve a configuration item
    *
    * @param name The key of the configuration item
    * @return The value of the configuration item
    */
   public String getItem(String name);
   
   /**
    * Retrieve a configuration item
    *
    * @param name The key of the configuration item
    * @return The value of the configuration item
    */
   public boolean getBooleanItem(String name);
   
   /**
    * Empty a configuration value of the configuration name specified
    *
    * @param name The key of the configuration item
    * @return int value -1 or 0 is incorrect, 1 is action succeed
    */
   int emptyItem(String name);
   /**
    * Reset the configuration table to default settings
    * 
    * @return true is success else false
    */
   boolean resetConfigurations();   
   /**
    * Get all items (configurations values and names) from the database
    * @return resultSet with the result
    */
   public ResultSet getConfigurationItems();
   /**
    * Get all advanced items (configurations values and names) from the database
    * @return resultSet with the result
    */
   public ResultSet getAdvancedConfigurationItems();
   /**
    * Enable or disable the configuration item
    * @param name of the configuration item
    * @return int value -1 or 0 is incorrect, 1 is action succeed
    */
   int updateConfiguration(String name, boolean newValue);
   /**
    * Save the boolean of the reboot flag
    * 
    * @param reboot boolean
    */
   void setReboot();
   /**
    * Get the reboot flag
    * 
    * @return reboot boolean
    */
   boolean shouldReboot();

   /**
    * Free the result set
    */
   public void free();
   
   /**
    * Enable the authentication in web.xml. Requires OpenRemote to be restarted
    * @param enable True if you want to enable
    * @throws IOException
    */
   public void setAuthentication(boolean enable) throws IOException;
}
