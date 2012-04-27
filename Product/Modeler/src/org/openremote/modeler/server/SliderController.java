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

import org.openremote.modeler.client.rpc.SliderRPCService;
import org.openremote.modeler.domain.Slider;
import org.openremote.modeler.service.SliderService;
import org.openremote.modeler.service.UserService;
import org.openremote.modeler.service.impl.UserServiceImpl;

@SuppressWarnings("serial")
public class SliderController extends BaseGWTSpringControllerWithHibernateSupport implements SliderRPCService {

   private SliderService sliderService;
   
   private UserService userService;
   
   @Override
   public void delete(long id) {
      sliderService.delete(id);
   }

   @Override
   public List<Slider> loadAll() {
      return sliderService.loadAll();
   }

   @Override
   public Slider save(Slider slider) {
      slider.setAccount(userService.getAccount());
      return sliderService.save(slider);
   }

   
   @Override
   public Slider update(Slider slider) {
      slider.setAccount(userService.getAccount());
      return sliderService.update(slider);
   }

   public void setSliderService(SliderService switchService) {
      this.sliderService = switchService;
   }

   public void setUserService(UserServiceImpl userService) {
      this.userService = userService;
   }

   
}
