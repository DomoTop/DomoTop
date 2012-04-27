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

import java.util.List;

import org.openremote.modeler.client.rpc.DeviceMacroRPCService;
import org.openremote.modeler.domain.DeviceMacro;
import org.openremote.modeler.domain.DeviceMacroItem;
import org.openremote.modeler.service.DeviceMacroService;
import org.openremote.modeler.service.impl.UserServiceImpl;

/**
 * The Class DeviceMacroController.
 */
@SuppressWarnings("serial")
public class DeviceMacroController extends BaseGWTSpringControllerWithHibernateSupport implements DeviceMacroRPCService {
   
   /** The device macro service. */
   private DeviceMacroService deviceMacroService;
   
   /** The user service. */
   private UserServiceImpl userService;


    /**
     * Sets the device macro service.
     * 
     * @param deviceMacroService the new device macro service
     */
   public void setDeviceMacroService(DeviceMacroService deviceMacroService) {
      this.deviceMacroService = deviceMacroService;
   }


   /**
    * {@inheritDoc}
    * @see org.openremote.modeler.client.rpc.DeviceMacroRPCService#loadAll()
    */
   public List<DeviceMacro> loadAll() {
      return deviceMacroService.loadAll(userService.getAccount());
   }


   /**
    * {@inheritDoc}
    * @see org.openremote.modeler.client.rpc.DeviceMacroRPCService#save(org.openremote.modeler.domain.DeviceMacro)
    */
   public DeviceMacro saveDeviceMacro(DeviceMacro deviceMacro) {
      return deviceMacroService.saveDeviceMacro(deviceMacro);
   }


   /**
    * {@inheritDoc}
    * @see org.openremote.modeler.client.rpc.DeviceMacroRPCService#edit(org.openremote.modeler.domain.DeviceMacro)
    */
   public DeviceMacro updateDeviceMacro(DeviceMacro deviceMacro) {
      return deviceMacroService.updateDeviceMacro(deviceMacro);
   }


   /**
    * {@inheritDoc}
    * @see org.openremote.modeler.client.rpc.DeviceMacroRPCService#deleteDeviceMacro(long)
    */
   public void deleteDeviceMacro(long id) {
      deviceMacroService.deleteDeviceMacro(id);
   }

    /**
     * {@inheritDoc}
     * @see org.openremote.modeler.client.rpc.DeviceMacroRPCService#loadDeviceMacroItems(org.openremote.modeler.domain.DeviceMacro)
     */
    public List<DeviceMacroItem> loadDeviceMacroItems(DeviceMacro deviceMacro) {
        return deviceMacroService.loadByDeviceMacro(deviceMacro.getOid());
    }


   /**
    * Sets the user service.
    * 
    * @param userService the new user service
    */
   public void setUserService(UserServiceImpl userService) {
      this.userService = userService;
   }
    
    

}
