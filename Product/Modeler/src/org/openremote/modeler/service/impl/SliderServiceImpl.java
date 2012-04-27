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

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.openremote.modeler.domain.Slider;
import org.openremote.modeler.service.BaseAbstractService;
import org.openremote.modeler.service.SliderService;
import org.openremote.modeler.service.UserService;

public class SliderServiceImpl extends BaseAbstractService<Slider> implements SliderService {

   private UserService userService = null;
   
   @Override
   public void delete(long id) {
      Slider slider = super.loadById(id);
      genericDAO.delete(slider);
   }

   @Override
   public List<Slider> loadAll() {
      List<Slider> result = userService.getAccount().getSliders();
      if (result == null || result.size() == 0) {
         return new ArrayList<Slider> ();
      }
      return result;
   }

   @Override
   public Slider save(Slider slider) {
      genericDAO.save(slider);
      return slider;
   }

   @Override
   public Slider update(Slider slider) {
      Slider oldSlider = genericDAO.loadById(Slider.class, slider.getOid());
      if (oldSlider.getSliderSensorRef() != null) {
         genericDAO.delete(oldSlider.getSliderSensorRef());
         if (slider.getSliderSensorRef().getOid() != oldSlider.getSliderSensorRef().getOid()) {
            slider.getSliderSensorRef().setSlider(oldSlider);
            oldSlider.setSliderSensorRef(slider.getSliderSensorRef());
         }
      }
      oldSlider.setName(slider.getName());
      if (slider.getSetValueCmd().getOid() != oldSlider.getSetValueCmd().getOid()) {
         genericDAO.delete(oldSlider.getSetValueCmd());
         slider.getSetValueCmd().setSlider(oldSlider);
         oldSlider.setSetValueCmd(slider.getSetValueCmd());
      }
      return oldSlider;
   }
   
   public List<Slider> loadSameSliders(Slider slider) {
      List<Slider> result = null;
      DetachedCriteria critera = DetachedCriteria.forClass(Slider.class);
      critera.add(Restrictions.eq("device.oid", slider.getDevice().getOid()));
      critera.add(Restrictions.eq("name", slider.getName()));
      result = genericDAO.findByDetachedCriteria(critera);
      if (result != null) {
         for(Iterator<Slider> iterator = result.iterator();iterator.hasNext();) {
            Slider sld = iterator.next();
            if (!sld.equalsWithoutCompareOid(slider)) {
               iterator.remove();
            }
         }
      }
      return result;
   }

   public void setUserService(UserService userService) {
      this.userService = userService;
   }
}
