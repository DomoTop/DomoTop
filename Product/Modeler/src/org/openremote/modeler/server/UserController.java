/* OpenRemote, the Home of the Digital Home.
* Copyright 2008-2010, OpenRemote Inc.
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

import org.apache.commons.lang.StringUtils;
import org.openremote.modeler.client.rpc.UserRPCService;
import org.openremote.modeler.domain.User;
import org.openremote.modeler.exception.UserInvitationException;
import org.openremote.modeler.service.UserService;

public class UserController extends BaseGWTSpringControllerWithHibernateSupport implements UserRPCService {

   private static final long serialVersionUID = -3486307399647834562L;

   private UserService userService;
   public User inviteUser(String email, String role) throws UserInvitationException{
      if (StringUtils.isEmpty(email) || StringUtils.isEmpty(role)) {
         throw new UserInvitationException("Failed to send invitation");
      }
      return userService.inviteUser(email, role, userService.getCurrentUser());
   }

   public void setUserService(UserService userService) {
      this.userService = userService;
   }

   public List<User> getPendingInviteesByAccount() {
      return userService.getPendingInviteesByAccount(userService.getCurrentUser());
   }

   public User updateUserRoles(long uid, String roles) {
      return userService.updateUserRoles(uid, roles);
   }

   public void deleteUser(long uid) {
      userService.deleteUser(uid);
   }

   public List<User> getAccountAccessUsers() {
      return userService.getAccountAccessUsers(userService.getCurrentUser());
   }

   public Long getUserId() {
      return userService.getCurrentUser().getOid();
   }
}
