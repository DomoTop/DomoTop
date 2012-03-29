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
package org.openremote.controller.service.impl;

import org.apache.log4j.Logger;

import org.openremote.controller.Constants;
import org.openremote.controller.service.DatabaseService;
import org.openremote.controller.service.ConfigurationService;

/**
 * The implementation for dynamic configuration settings service;
 * 
 * @author <a href="mailto:vincent.kriek@tass.nl">Vincent Kriek</a> 2012-03-29
 */
public class ConfigurationServiceImpl implements ConfigurationService {
   private final static Logger logger = Logger.getLogger(Constants.SERVICE_LOG_CATEGORY);

   private DatabaseService database;
   
   /**
    * Add a configuration item
    *
    * @param name The key of the configuration item
    * @param value The value of the configuration item
    * @return true if it did not exist and was added, false if it already exists and it will not be added
    */
   public boolean addItem(String name, String value)
   {
        return false;
   }

   /**
    * Add or update a configuration item
    *
    * @param name The key of the configuration item
    * @param value The value of the configuration item
    */
   public boolean updateItem(String name, String value)
   {
        return false;
   }

   /**
    * Delete a configuration item
    *
    * @param name The key of the configuration item
    * @return true if the deletion was succesful
    */
   public boolean deleteItem(String name)
   {
       return false;
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
}
