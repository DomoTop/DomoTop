/* OpenRemote, the Home of the Digital Home.
* Copyright 2008-2009, OpenRemote Inc.
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
package org.openremote.modeler.client.rpc;

import java.util.List;

import org.openremote.modeler.domain.DeviceCommand;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The async counterpart of <code>DeviceCommandRPCService</code>.
 */
public interface DeviceCommandRPCServiceAsync {
   
   /**
    * Save all.
    * 
    * @param deviceCommands the device commands
    * @param callback the callback
    */
   void saveAll(List<DeviceCommand> deviceCommands, AsyncCallback<List<DeviceCommand>> callback);
   
   /**
    * Save.
    * 
    * @param deviceCommand the device command
    * @param callback the callback
    */
   void save(DeviceCommand deviceCommand, AsyncCallback<DeviceCommand> callback);
   
   /**
    * Update.
    * 
    * @param deviceCommand the device command
    * @param callback the callback
    */
   void update(DeviceCommand deviceCommand, AsyncCallback<DeviceCommand> callback);
   
   /**
    * Load by id.
    * 
    * @param id the id
    * @param callback the callback
    */
   void loadById(long id, AsyncCallback<DeviceCommand> callback);

   /**
    * Delete command.
    * 
    * @param id
    *           the id
    * @param callback
    *           the callback
    */
   void deleteCommand(long id, AsyncCallback<Boolean> callback);

   /**
    * Load by device.
    * 
    * @param id the id
    * @param asyncCallback the async callback
    */
   void loadByDevice(long id, AsyncCallback<List<DeviceCommand>> asyncCallback);
   
   
}
