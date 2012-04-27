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

import org.openremote.modeler.domain.DeviceMacro;
import org.openremote.modeler.domain.DeviceMacroItem;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;


/**
 * The Interface DeviceMacroService.
 */
@RemoteServiceRelativePath("deviceMacro.smvc")
public interface DeviceMacroRPCService extends RemoteService {

   /**
    * Load all.
    * 
    * @return the list< device macro>
    */
   List<DeviceMacro> loadAll();


   /**
    * Save device macro.
    * 
    * @param deviceMacro the device macro
    * 
    * @return the device macro
    */
   DeviceMacro saveDeviceMacro(DeviceMacro deviceMacro);


   /**
    * Update device macro.
    * 
    * @param deviceMacro the device macro
    * 
    * @return the device macro
    */
   DeviceMacro updateDeviceMacro(DeviceMacro deviceMacro);


   /**
    * Delete device macro.
    * 
    * @param id the id
    */
   void deleteDeviceMacro(long id);


   /**
    * Load device macro items.
    * 
    * @param deviceMacro the device macro
    * 
    * @return the list< device macro item>
    */
   List<DeviceMacroItem> loadDeviceMacroItems(DeviceMacro deviceMacro);


}
