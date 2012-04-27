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

import org.openremote.modeler.client.rpc.SwitchRPCService;
import org.openremote.modeler.domain.Switch;
import org.openremote.modeler.service.SwitchService;
import org.openremote.modeler.service.UserService;
import org.openremote.modeler.service.impl.UserServiceImpl;

/**
 * The Class SwitchController.
 */
@SuppressWarnings("serial")
public class SwitchController extends BaseGWTSpringControllerWithHibernateSupport implements SwitchRPCService {

   private SwitchService switchService;
   
   private UserService userService;
   
   @Override
   public void delete(long id) {
      switchService.delete(id);
   }

   @Override
   public List<Switch> loadAll() {
      return switchService.loadAll();
   }

   @Override
   public Switch save(Switch switchToggle) {
      switchToggle.setAccount(userService.getAccount());
      return switchService.save(switchToggle);
   }

   
   @Override
   public Switch update(Switch switchToggle) {
      switchToggle.setAccount(userService.getAccount());
      return switchService.update(switchToggle);
   }

   public void setSwitchService(SwitchService switchService) {
      this.switchService = switchService;
   }

   public void setUserService(UserServiceImpl userService) {
      this.userService = userService;
   }

   
}
