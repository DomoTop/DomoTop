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
package org.openremote.modeler.service;

import java.util.List;

import org.openremote.modeler.domain.Account;
import org.openremote.modeler.domain.User;

/**
 * The service for User.
 * 
 * @author Dan 2009-7-14
 */
public interface UserService {
   
   void initRoles();

   void saveUser(User user);
   
   void updateUser(User user);
   
   User getUserById(long id);
   
   Account getAccount();
   
   /**
    * Creates the user and its account, set disabled by default.
    * 
    * @param username the username
    * @param password the password
    * 
    * @return true, if successful
    */
   boolean createUserAccount(String username, String password, String email);
   
   /**
    * Activate user, then user can login.
    * 
    * @param userOid
    *           user oid
    * @param aid
    *           activation id
    * @return true if success
    */
   boolean activateUser(String userOid, String aid);
   
   /**
    * Send register activation email.
    * 
    * @param user
    *           the user
    * 
    * @return true, if success
    */
   boolean sendRegisterActivationEmail(User user);
   
   boolean isUsernameAvailable(String username);
   
   User getCurrentUser();
   
   User inviteUser(String email, String role, User currentUser);

   boolean sendInvitation(User invitee, User currentUser);
   
   boolean checkInvitation(String userOid, String hostOid, String aid);
   
   boolean createInviteeAccount(String userOid, String username, String password, String email);
   
   List<User> getPendingInviteesByAccount(User currentUser);
   
   User updateUserRoles(long uid, String roles);
   
   void deleteUser(long uid);
   
   List<User> getAccountAccessUsers(User currentUser);
   
   User forgetPassword(String username);
   
   User checkPasswordToken(long uid, String passwordToken);
   
   boolean resetPassword(long uid, String password, String passwordToken);
}