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

import org.openremote.modeler.domain.Account;
import org.openremote.modeler.domain.Device;

import com.google.gwt.user.client.rpc.AsyncCallback;


/**
 * The async counterpart of <code>DeviceRPCService</code>.
 */
public interface DeviceRPCServiceAsync {
   
   /**
    * Save device.
    * 
    * @param device the device
    * @param callback the callback
    */
   void saveDevice(Device device, AsyncCallback<Device> callback);
   
   /**
    * Update device.
    * 
    * @param device the device
    * @param callback the callback
    */
   void updateDevice(Device device, AsyncCallback<Void> callback);
   
   /**
    * Delete device.
    * 
    * @param id the id
    * @param callback the callback
    */
   void deleteDevice(long id, AsyncCallback<Void> callback);
   
   /**
    * Load by id.
    * 
    * @param id the id
    * @param callback the callback
    */
   void loadById(long id, AsyncCallback<Device> callback);
   
   /**
    * Load all.
    * 
    * @param callback the callback
    */
   void loadAll(AsyncCallback<List<Device>> callback);
   
   /**
    * Load all.
    * 
    * @param account the account
    * @param callback the callback
    */
   void loadAll(Account account, AsyncCallback<List<Device>> callback);
   
   void getAccount(AsyncCallback<Account> callback);
}
