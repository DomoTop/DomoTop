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

package org.openremote.modeler.server;

import java.util.ArrayList;
import java.util.List;

import org.openremote.modeler.client.rpc.DeviceMacroItemRPCService;
import org.openremote.modeler.domain.DeviceCommandRef;
import org.openremote.modeler.domain.DeviceMacroRef;
import org.openremote.modeler.service.DeviceMacroItemService;

/**
 * The Class DeviceMacroItemController.
 * 
 * @author allen.wei
 */
public class DeviceMacroItemController extends BaseGWTSpringController implements DeviceMacroItemRPCService {


   /** The Constant serialVersionUID. */
   private static final long serialVersionUID = 7610942536309345872L;
   
   /** The device macro item service. */
   private DeviceMacroItemService deviceMacroItemService;

   /**
    * Gets the device macro item ids by device command id.
    * 
    * @param id the id
    * 
    * @return the device macro item ids by device command id
    */
   public List<Long> getDeviceMacroItemIdsByDeviceCommandId(long id) {
      List<Long> ids = new ArrayList<Long>();
      for (DeviceCommandRef deviceCommandRef : deviceMacroItemService.loadByDeviceCommandId(id)) {
         ids.add(deviceCommandRef.getDeviceCommand().getOid());
      }
      return ids;
   }

   /**
    * Gets the device macro item ids by device macro id.
    * 
    * @param id the id
    * 
    * @return the device macro item ids by device macro id
    */
   public List<Long> getDeviceMacroItemIdsByDeviceMacroId(long id) {
      List<Long> ids = new ArrayList<Long>();
      for (DeviceMacroRef deviceMacroRef : deviceMacroItemService.loadByDeviceMacroId(id)) {
         ids.add(deviceMacroRef.getTargetDeviceMacro().getOid());
      }
      return ids;
   }
}
