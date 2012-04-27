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

import org.openremote.modeler.client.rpc.SensorRPCService;
import org.openremote.modeler.domain.Sensor;
import org.openremote.modeler.service.SensorService;
import org.openremote.modeler.service.UserService;

public class SensorController extends BaseGWTSpringControllerWithHibernateSupport implements SensorRPCService {

   private static final long serialVersionUID = 7122839354773238989L;

   private SensorService sensorService;
   
   private UserService userService;
   
   public Boolean deleteSensor(long id) {
      return sensorService.deleteSensor(id);
   }

   public List<Sensor> loadAll() {
      return sensorService.loadAll(userService.getAccount());
   }

   public Sensor saveSensor(Sensor sensor) {
      sensor.setAccount(userService.getAccount());
      return sensorService.saveSensor(sensor);
   }

   public Sensor updateSensor(Sensor sensor) {
      return sensorService.updateSensor(sensor);
   }

   public void setSensorService(SensorService sensorService) {
      this.sensorService = sensorService;
   }
   
   public void setUserService(UserService userService) {
      this.userService = userService;
   }

   public Sensor getById(long id) {
      return sensorService.loadById(id);
   }

   /*@Override
   public List<Sensor> loadByDevice(Device device) {
      return sensorService.loadByDevice(device);
   }*/
}
