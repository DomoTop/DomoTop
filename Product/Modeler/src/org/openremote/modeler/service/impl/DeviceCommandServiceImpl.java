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
package org.openremote.modeler.service.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.hibernate.Hibernate;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.openremote.modeler.domain.CommandRefItem;
import org.openremote.modeler.domain.Device;
import org.openremote.modeler.domain.DeviceCommand;
import org.openremote.modeler.service.BaseAbstractService;
import org.openremote.modeler.service.DeviceCommandService;
import org.openremote.modeler.service.DeviceMacroItemService;

/**
 * The implementation for DeviceCommandService interface.
 * 
 * @author Allen
 */
public class DeviceCommandServiceImpl extends BaseAbstractService<DeviceCommand> implements DeviceCommandService {

 
   /** The device macro item service. */
   private DeviceMacroItemService deviceMacroItemService;

   /**
    * Sets the device macro item service.
    * 
    * @param deviceMacroItemService the new device macro item service
    */
   public void setDeviceMacroItemService(DeviceMacroItemService deviceMacroItemService) {
      this.deviceMacroItemService = deviceMacroItemService;
   }

   /**
    * {@inheritDoc}
    * @see org.openremote.modeler.service.DeviceCommandService#saveAll(java.util.List)
    */
   public List<DeviceCommand> saveAll(List<DeviceCommand> deviceCommands) {
      for (DeviceCommand command : deviceCommands) {
         genericDAO.save(command);
      }
      return deviceCommands;
   }

   /**
    * {@inheritDoc}
    * @see org.openremote.modeler.service.DeviceCommandService#save(org.openremote.modeler.domain.DeviceCommand)
    */
   public DeviceCommand save(DeviceCommand deviceCommand) {
      genericDAO.save(deviceCommand);
      Hibernate.initialize(deviceCommand.getProtocol().getAttributes());
      return deviceCommand;
   }

   /**
    * {@inheritDoc}
    * @see org.openremote.modeler.service.DeviceCommandService#deleteCommand(long)
    */
   public Boolean deleteCommand(long id) {
      DeviceCommand deviceCommand = loadById(id);
      DetachedCriteria criteria = DetachedCriteria.forClass(CommandRefItem.class);
      List<CommandRefItem> commandRefItems = genericDAO.findByDetachedCriteria(criteria.add(Restrictions.eq("deviceCommand", deviceCommand)));
      if (commandRefItems.size() > 0) {
         return false;
      } else {
         deviceMacroItemService.deleteByDeviceCommand(deviceCommand);
         genericDAO.delete(deviceCommand);
      }
      return true;
   }

   /**
    * {@inheritDoc}
    * @see org.openremote.modeler.service.DeviceCommandService#update(org.openremote.modeler.domain.DeviceCommand)
    */
   public DeviceCommand update(DeviceCommand deviceCommand) {
      DeviceCommand old = loadById(deviceCommand.getOid());
      genericDAO.delete(old.getProtocol());
      old.setName(deviceCommand.getName());
      old.setProtocol(deviceCommand.getProtocol());
      return old;

   }

   /**
    * {@inheritDoc}
    * @see org.openremote.modeler.service.BaseAbstractService#loadById(long)
    */
   public DeviceCommand loadById(long id) {
      DeviceCommand deviceCommand = super.loadById(id);
      Hibernate.initialize(deviceCommand.getProtocol().getAttributes());
      return deviceCommand;
   }

   /**
    * {@inheritDoc}
    * @see org.openremote.modeler.service.DeviceCommandService#loadByDevice(long)
    */
   public List<DeviceCommand> loadByDevice(long id) {
      Device device = genericDAO.loadById(Device.class, id);
      List<DeviceCommand> deviceCommandList = device.getDeviceCommands();
      for (DeviceCommand deviceCommand : deviceCommandList) {
         Hibernate.initialize(deviceCommand.getProtocol().getAttributes());
      }
      return deviceCommandList;
   }

   public List<DeviceCommand> loadSameCommands(DeviceCommand deviceCommand) {
      List<DeviceCommand> tmpResult = new ArrayList<DeviceCommand>();
      DetachedCriteria critera = DetachedCriteria.forClass(DeviceCommand.class);
      critera.add(Restrictions.eq("device.oid", deviceCommand.getDevice().getOid()));
      critera.add(Restrictions.eq("name", deviceCommand.getName()));
      if (deviceCommand.getSectionId() != null) {
         critera.add(Restrictions.eq("sectionId", deviceCommand.getSectionId()));
      }
      tmpResult = genericDAO.findByDetachedCriteria(critera);
      if (tmpResult != null) {
         for(Iterator<DeviceCommand> iterator= tmpResult.iterator();iterator.hasNext();) {
            DeviceCommand cmd = iterator.next();
            if (! cmd.equalsWithoutCompareOid(deviceCommand)) {
               iterator.remove();
            }
         }
      }
      return tmpResult;
   }
}
