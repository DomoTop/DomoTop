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

import org.openremote.modeler.client.rpc.DeviceCommandRPCService;
import org.openremote.modeler.domain.DeviceCommand;
import org.openremote.modeler.service.DeviceCommandService;

/**
 * The server side implementation of the RPC service <code>DeviceCommandRPCService</code>.
 */
public class DeviceCommandController extends BaseGWTSpringControllerWithHibernateSupport implements
        DeviceCommandRPCService {

   /** The Constant serialVersionUID. */
   private static final long serialVersionUID = -8417889117208060088L;
   
   /** The device command service. */
   private DeviceCommandService deviceCommandService;

    /**
     * Sets the device command service.
     * 
     * @param deviceCommandRPCService the new device command service
     */
    public void setDeviceCommandService(DeviceCommandService deviceCommandRPCService) {
      this.deviceCommandService = deviceCommandRPCService;
   }

   /**
    * {@inheritDoc}
    * 
    * @see org.openremote.modeler.client.rpc.DeviceCommandRPCService#saveAll(java.util.List)
    */
   public List<DeviceCommand> saveAll(List<DeviceCommand> deviceCommands) {
      return deviceCommandService.saveAll(deviceCommands);
   }

   /**
    * {@inheritDoc}
    * 
    * @see org.openremote.modeler.client.rpc.DeviceCommandRPCService#save(org.openremote.modeler.domain.DeviceCommand)
    */
   public DeviceCommand save(DeviceCommand deviceCommand) {
      return deviceCommandService.save(deviceCommand);
   }

   /**
    * {@inheritDoc}
    * 
    * @see org.openremote.modeler.client.rpc.DeviceCommandRPCService#deleteCommand(long)
    */
   public Boolean deleteCommand(long id) {
      return deviceCommandService.deleteCommand(id);
   }

   /**
    * {@inheritDoc}
    * 
    * @see org.openremote.modeler.client.rpc.DeviceCommandRPCService#update(org.openremote.modeler.domain.DeviceCommand)
    */
   public DeviceCommand update(DeviceCommand deviceCommand) {
      return deviceCommandService.update(deviceCommand);
   }

   /**
    * {@inheritDoc}
    * 
    * @see org.openremote.modeler.client.rpc.DeviceCommandRPCService#loadById(long)
    */
   public DeviceCommand loadById(long id) {
      return deviceCommandService.loadById(id);
   }

   /**
    * {@inheritDoc}
    * 
    * @see org.openremote.modeler.client.rpc.DeviceCommandRPCService#loadByDevice(long)
    */
   public List<DeviceCommand> loadByDevice(long id) {
      return deviceCommandService.loadByDevice(id);
   }

}
