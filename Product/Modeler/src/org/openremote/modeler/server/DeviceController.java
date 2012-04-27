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

import org.openremote.modeler.client.rpc.DeviceRPCService;
import org.openremote.modeler.domain.Account;
import org.openremote.modeler.domain.Device;
import org.openremote.modeler.service.DeviceService;
import org.openremote.modeler.service.impl.UserServiceImpl;

/**
 * The server side implementation of the RPC service <code>DeviceRPCService</code>.
 */
public class DeviceController extends BaseGWTSpringControllerWithHibernateSupport implements DeviceRPCService {

   /** The Constant serialVersionUID. */
   private static final long serialVersionUID = -6698924847005128888L;
   
   /** The device service. */
   private DeviceService deviceService;
   
   /** The user service. */
   private UserServiceImpl userService;

    /**
     * Sets the device service .
     * 
     * @param deviceService the device service
     */
   public void setDeviceService(DeviceService deviceService) {
      this.deviceService = deviceService;
   }

   /**
    * {@inheritDoc}
    * 
    * @see org.openremote.modeler.client.rpc.DeviceRPCService#saveDevice(java.util.Map)
    */
   public Device saveDevice(Device device) {
      device.setAccount(userService.getAccount());
      return deviceService.saveDevice(device);
   }

   /**
    * {@inheritDoc}
    * 
    * @see org.openremote.modeler.client.rpc.DeviceRPCService#removeDevice(org.openremote.modeler.domain.Device)
    */
   public void deleteDevice(long id) {
      deviceService.deleteDevice(id);
   }

   /**
    * {@inheritDoc}
    * 
    * @see org.openremote.modeler.client.rpc.DeviceRPCService#loadById(long)
    */
   public Device loadById(long id) {
      return deviceService.loadById(id);
   }

   /**
    * {@inheritDoc}
    * 
    * @see org.openremote.modeler.client.rpc.DeviceRPCService#loadAll()
    */
   public List<Device> loadAll() {
      return deviceService.loadAll(userService.getAccount());
   }

   /**
    * Sets the user service.
    * 
    * @param userService the new user service
    */
   public void setUserService(UserServiceImpl userService) {
      this.userService = userService;
   }

   /**
    * {@inheritDoc}
    * 
    * @see org.openremote.modeler.client.rpc.DeviceRPCService#loadAll(org.openremote.modeler.domain.Account)
    */
   public List<Device> loadAll(Account account) {
      return null;
   }

   
   /**
    * {@inheritDoc}
    * 
    * @see org.openremote.modeler.client.rpc.DeviceRPCService#updateDevice(org.openremote.modeler.domain.Device)
    */
   public void updateDevice(Device device) {
      deviceService.updateDevice(device);
      
   }

   public Account getAccount() {
      return userService.getAccount();
   }
   
}
