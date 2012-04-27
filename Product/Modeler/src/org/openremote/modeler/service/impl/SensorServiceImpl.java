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

import java.util.Iterator;
import java.util.List;

import org.hibernate.Hibernate;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.openremote.modeler.domain.Account;
import org.openremote.modeler.domain.CustomSensor;
import org.openremote.modeler.domain.RangeSensor;
import org.openremote.modeler.domain.Sensor;
import org.openremote.modeler.domain.SensorRefItem;
import org.openremote.modeler.domain.SensorType;
import org.openremote.modeler.service.BaseAbstractService;
import org.openremote.modeler.service.SensorService;

public class SensorServiceImpl extends BaseAbstractService<Sensor> implements SensorService {

   public Boolean deleteSensor(long id) {
      Sensor sensor = super.loadById(id);
      DetachedCriteria criteria = DetachedCriteria.forClass(SensorRefItem.class);
      List<SensorRefItem> sensorRefItems = genericDAO.findByDetachedCriteria(criteria.add(Restrictions.eq("sensor",
            sensor)));
      if (sensorRefItems.size() > 0) {
         return false;
      } else {
         genericDAO.delete(sensor);
      }
      return true;
   }

   public List<Sensor> loadAll(Account account) {
      List<Sensor> sensors = account.getSensors();
      for (Sensor sensor : sensors) {
         if (sensor.getType() == SensorType.CUSTOM) {
            Hibernate.initialize(((CustomSensor) sensor).getStates());
         }
      }
      return sensors;
   }

   public Sensor saveSensor(Sensor sensor) {
      genericDAO.save(sensor);
      return sensor;
   }

   public Sensor updateSensor(Sensor sensor) {
      Sensor old = null;
      
      if (SensorType.RANGE == sensor.getType()) {
         old = genericDAO.loadById(RangeSensor.class, sensor.getOid());
         RangeSensor rangeSensor = (RangeSensor)sensor;
         ((RangeSensor) old).setMax(rangeSensor.getMax());
         ((RangeSensor) old).setMin(rangeSensor.getMin());
      } else if (SensorType.CUSTOM == sensor.getType()) {
         old = genericDAO.loadById(CustomSensor.class, sensor.getOid());
         genericDAO.deleteAll(((CustomSensor)old).getStates());
         ((CustomSensor)old).setStates(((CustomSensor) sensor).getStates());
      } else {
         old = genericDAO.loadById(Sensor.class, sensor.getOid());
      }
      
      old.setName(sensor.getName());
      genericDAO.delete(old.getSensorCommandRef());
      old.setSensorCommandRef(sensor.getSensorCommandRef());
      return old;
   }

   public Sensor loadById(long id) {
      Sensor sensor = genericDAO.getById(Sensor.class, id);
      if (sensor instanceof CustomSensor) {
         Hibernate.initialize(((CustomSensor) sensor).getStates());
      }
      return sensor;
   }

  /* @Override
   public List<Sensor> loadByDevice(Device device) {
      Device dvic = genericDAO.loadById(Device.class, device.getOid());
      return dvic.getSensors();
   }
*/
   public List<Sensor> loadSameSensors(Sensor sensor) {
      List<Sensor> result = null;
      DetachedCriteria critera = DetachedCriteria.forClass(Sensor.class);
      critera.add(Restrictions.eq("name", sensor.getName()));
      critera.add(Restrictions.eq("type", sensor.getType()));
      critera.add(Restrictions.eq("device.oid", sensor.getDevice().getOid()));
      result = genericDAO.findByDetachedCriteria(critera);
      
      if (result != null) {
         for(Iterator<Sensor> iterator=result.iterator();iterator.hasNext();) {
            Sensor s = iterator.next();
            if(!s.equalsWithoutCompareOid(sensor)){
               iterator.remove();
            }
         }
      }
      return result;
   }
}
