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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.mail.internet.MimeMessage;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.velocity.app.VelocityEngine;
import org.hibernate.Hibernate;
import org.openremote.modeler.client.Configuration;
import org.openremote.modeler.client.Constants;
import org.openremote.modeler.domain.Account;
import org.openremote.modeler.domain.ConfigCategory;
import org.openremote.modeler.domain.ControllerConfig;
import org.openremote.modeler.domain.Role;
import org.openremote.modeler.domain.User;
import org.openremote.modeler.exception.UserInvitationException;
import org.openremote.modeler.service.BaseAbstractService;
import org.openremote.modeler.service.UserService;
import org.openremote.modeler.utils.XmlParser;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.providers.encoding.Md5PasswordEncoder;
import org.springframework.ui.velocity.VelocityEngineUtils;

/**
 * The service implementation for UserService.
 * 
 * @author Dan 2009-7-14
 */
public class UserServiceImpl extends BaseAbstractService<User> implements UserService {
   
   private static Logger log = Logger.getLogger(UserServiceImpl.class);
   
   private JavaMailSenderImpl mailSender;
   
   private VelocityEngine velocityEngine;
   
   private Configuration configuration;
   
   /**
    * {@inheritDoc}
    */
   public void initRoles() {
      boolean hasDesignerRole = false;
      boolean hasModelerRole = false;
      boolean hasAdminRole = false;
      List<Role> allRoles = genericDAO.loadAll(Role.class);
      for (Role r : allRoles) {
         if (r.getName().equals(Role.ROLE_DESIGNER)) {
            hasDesignerRole = true;
         } else if (r.getName().equals(Role.ROLE_MODELER)) {
            hasModelerRole = true;
         } else if (r.getName().equals(Role.ROLE_ADMIN)) {
            hasAdminRole = true;
         }
      }
      if (!hasDesignerRole) {
         Role r = new Role();
         r.setName(Role.ROLE_DESIGNER);
         genericDAO.save(r);
      }
      if (!hasModelerRole) {
         Role r = new Role();
         r.setName(Role.ROLE_MODELER);
         genericDAO.save(r);
      }
      if (!hasAdminRole) {
         Role r = new Role();
         r.setName(Role.ROLE_ADMIN);
         genericDAO.save(r);
      }
      
   }
   
   /**
    * {@inheritDoc}
    */
   public User getUserById(long id) {
      return genericDAO.getById(User.class, id);
   }
   
   /**
    * {@inheritDoc}
    */
    public Account getAccount() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return genericDAO.getByNonIdField(User.class, "username", username).getAccount();
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean createUserAccount(String username, String password, String email) {
      if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password) || StringUtils.isEmpty(email)) {
         return false;
      }
      User user = new User();
      user.setUsername(username);
      user.setRawPassword(password);
      user.setPassword(new Md5PasswordEncoder().encodePassword(password, username));
      user.setEmail(email);
      if (isUsernameAvailable(username)) {
         user.addRole(genericDAO.getByNonIdField(Role.class, "name", Role.ROLE_ADMIN));
         saveUser(user);
         return sendRegisterActivationEmail(user);
      } else {
         return false;
      }
   }

    /**
    * {@inheritDoc}
    */
    public void saveUser(User user) {
        genericDAO.save(user.getAccount());
        genericDAO.save(user);
    }
    /**
     * {@inheritDoc}
     */
    public void updateUser(User user) {
       genericDAO.update(user);
    }
    
    private void setDefaultConfigsForAccount(Account account){
       Set<ConfigCategory> categories = new HashSet<ConfigCategory>();
       Set<ControllerConfig> allDefaultConfigs = new HashSet<ControllerConfig>();
       XmlParser.initControllerConfig(categories, allDefaultConfigs);
       for(ControllerConfig cfg : allDefaultConfigs){
          cfg.setAccount(account);
       }
       genericDAO.getHibernateTemplate().saveOrUpdateAll(allDefaultConfigs);
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean sendRegisterActivationEmail(final User user) {
       if (user == null || user.getOid() == 0 || StringUtils.isEmpty(user.getEmail())
            || StringUtils.isEmpty(user.getUsername()) || StringUtils.isEmpty(user.getPassword())) {
         return false;
       }
       
       MimeMessagePreparator preparator = new MimeMessagePreparator() {
          @SuppressWarnings("unchecked")
          public void prepare(MimeMessage mimeMessage) throws Exception {
             MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true, "UTF-8");
             message.setSubject("OpenRemote Boss 2.0 Account Registration Confirmation");
             message.setTo(user.getEmail());
             message.setFrom(mailSender.getUsername());
             Map model = new HashMap();
             model.put("user", user);
             String rpwd = user.getRawPassword();
             StringBuffer maskPwd = new StringBuffer();
             maskPwd.append(rpwd.substring(0, 1));
             for (int i = 0; i < rpwd.length() - 2; i++) {
               maskPwd.append("*");
             }
             maskPwd.append(rpwd.substring(rpwd.length() - 1));
             model.put("maskPassword", maskPwd.toString());
             model.put("webapp", configuration.getWebappServerRoot());
             model.put("aid", new Md5PasswordEncoder().encodePassword(user.getUsername(), user.getPassword()));
             String text = VelocityEngineUtils.mergeTemplateIntoString(velocityEngine,
                  Constants.REGISTRATION_ACTIVATION_EMAIL_VM_NAME, "UTF-8", model);
             message.setText(text, true);
          }
       };
       try {
          this.mailSender.send(preparator);
          log.info("Sent 'Modeler Account Registration Confirmation' email to " + user.getEmail());
          return true;
       } catch (MailException e) {
          log.error("Can't send 'Modeler Account Registration Confirmation' email", e);
          return false;
       }
   }

    /**
    * {@inheritDoc}
    */
   @Override
   public boolean activateUser(String userOid, String aid) {
      long id = 0;
      try {
         id = Long.valueOf(userOid);
      } catch (NumberFormatException e) {
         return false;
      }
      User user = getUserById(id);
      if (user != null && aid != null) {
         if (new Md5PasswordEncoder().encodePassword(user.getUsername(), user.getPassword()).equals(aid)) {
            user.setValid(true);
            updateUser(user);
            setDefaultConfigsForAccount(user.getAccount());
            return true;
         }
      }
      return false;
   }
   
   public boolean isUsernameAvailable(String username) {
      return genericDAO.getByNonIdField(User.class, "username", username) == null;
   }

   public void setMailSender(JavaMailSenderImpl mailSender) {
      this.mailSender = mailSender;
   }

   public void setVelocityEngine(VelocityEngine velocityEngine) {
      this.velocityEngine = velocityEngine;
   }

   public void setConfiguration(Configuration configuration) {
      this.configuration = configuration;
   }

   public User getCurrentUser() {
      String username = SecurityContextHolder.getContext().getAuthentication().getName();
      return genericDAO.getByNonIdField(User.class, "username", username);
   }

   public User inviteUser(String email, String role, User currentUser) {
      User invitee = null;
      if (isUsernameAvailable(email)) {
         invitee = new User(currentUser.getAccount());
         invitee.setEmail(email);
         invitee.setUsername(email);
         invitee.setPassword("pending password");
         convertRoleStringToRole(role, invitee, genericDAO.loadAll(Role.class));
         genericDAO.save(invitee);
         if (!sendInvitation(invitee, currentUser)) {
            throw new UserInvitationException("Failed to send invitation.");
         }
         return invitee;
      } else {
         invitee = genericDAO.getByNonIdField(User.class, "username", email);
         if (!sendInvitation(invitee, currentUser)) {
            throw new UserInvitationException("Failed to send invitation.");
         }
         return null;
      }
   }

   public boolean sendInvitation(final User invitee, final User currentUser) {
       if (invitee == null || invitee.getOid() == 0 || StringUtils.isEmpty(invitee.getEmail())) {
         return false;
       }
       
       MimeMessagePreparator preparator = new MimeMessagePreparator() {
          @SuppressWarnings("unchecked")
          public void prepare(MimeMessage mimeMessage) throws Exception {
             MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true, "UTF-8");
             message.setSubject("Invitation to Share an OpenRemote Boss 2.0 Account");
             message.setTo(invitee.getEmail());
             message.setFrom(mailSender.getUsername());
             Map model = new HashMap();
             model.put("uid", invitee.getOid());
             model.put("role", invitee.getRole());
             model.put("cid", currentUser.getOid());
             model.put("host", currentUser.getEmail());
             model.put("webapp", configuration.getWebappServerRoot());
             model.put("aid", new Md5PasswordEncoder().encodePassword(invitee.getEmail(), currentUser.getPassword()));
             String text = VelocityEngineUtils.mergeTemplateIntoString(velocityEngine,
                  Constants.REGISTRATION_INVITATION_EMAIL_VM_NAME, "UTF-8", model);
             message.setText(text, true);
          }
       };
       try {
          this.mailSender.send(preparator);
          log.info("Sent 'Modeler Account Invitation' email to " + invitee.getEmail());
          return true;
       } catch (MailException e) {
          log.error("Can't send 'Modeler Account Invitation' email", e);
          return false;
       }
   }

   public boolean checkInvitation(String userOid, String hostOid, String aid) {
      long uid = 0;
      long hid = 0;
      try {
         uid = Long.valueOf(userOid);
      } catch (NumberFormatException e) {
         return false;
      }
      try {
         hid = Long.valueOf(hostOid);
      } catch (NumberFormatException e) {
         return false;
      }
      User user = getUserById(uid);
      User hostUser = getUserById(hid);
      if (user != null && hostUser != null && aid != null) {
         if (new Md5PasswordEncoder().encodePassword(user.getEmail(), hostUser.getPassword()).equals(aid)) {
            return true;
         }
      }
      return false;
   }

   public boolean createInviteeAccount(String userOid, String username, String password, String email) {
      if (StringUtils.isEmpty(userOid) || StringUtils.isEmpty(username) || StringUtils.isEmpty(password) || StringUtils.isEmpty(email)) {
         return false;
      }
      long id = 0;
      try {
         id = Long.valueOf(userOid);
      } catch (NumberFormatException e) {
         return false;
      }
      if (isUsernameAvailable(username)) {
         User user = getUserById(id);
         user.setValid(true);
         user.setUsername(username);
         user.setRawPassword(password);
         user.setPassword(new Md5PasswordEncoder().encodePassword(password, username));
         user.setEmail(email);
         updateUser(user);
         return true;
      } else {
         return false;
      }
   }

   public List<User> getPendingInviteesByAccount(User currentUser) {
      List<User> invitees = new ArrayList<User>();
      List<User> sameAccountUsers = currentUser.getAccount().getUsers();
      sameAccountUsers.remove(currentUser);
      for (User invitee : sameAccountUsers) {
         if(!invitee.isValid()) {
            Hibernate.initialize(invitee.getRoles());
            invitees.add(invitee);
         }
      }
      return invitees;
   }

   public User updateUserRoles(long uid, String roles) {
      User user = getUserById(uid);
      user.getRoles().clear();
      convertRoleStringToRole(roles, user, genericDAO.loadAll(Role.class));
      Hibernate.initialize(user.getRoles());
      return user;
   }

   private void convertRoleStringToRole(String roles, User user, List<Role> allRoles) {
      for (Role role : allRoles) {
         if(role.getName().equals(Role.ROLE_ADMIN) && roles.indexOf(Constants.ROLE_ADMIN_DISPLAYNAME) != -1) {
            user.addRole(role);
         } else if (role.getName().equals(Role.ROLE_MODELER) && roles.indexOf(Constants.ROLE_MODELER_DISPLAYNAME) != -1) {
            user.addRole(role);
         } else if (role.getName().equals(Role.ROLE_DESIGNER) && roles.indexOf(Constants.ROLE_DESIGNER_DISPLAYNAME) != -1) {
            user.addRole(role);
         }
      }
   }

   public void deleteUser(long uid) {
      User user = getUserById(uid);
      genericDAO.delete(user);
   }

   public List<User> getAccountAccessUsers(User currentUser) {
      List<User> accessUsers = new ArrayList<User>();
      List<User> sameAccountUsers = currentUser.getAccount().getUsers();
      sameAccountUsers.remove(currentUser);
      accessUsers.add(currentUser);
      Hibernate.initialize(currentUser.getRoles());
      for (User accessUser : sameAccountUsers) {
         if(accessUser.isValid()) {
            Hibernate.initialize(accessUser.getRoles());
            accessUsers.add(accessUser);
         }
      }
      return accessUsers;
   }

   public User forgetPassword(String username) {
      final User user = genericDAO.getByNonIdField(User.class, "username", username);
      final String passwordToken = UUID.randomUUID().toString();
      
      MimeMessagePreparator preparator = new MimeMessagePreparator() {
         @SuppressWarnings("unchecked")
         public void prepare(MimeMessage mimeMessage) throws Exception {
            MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            message.setSubject("OpenRemote Password Assistance");
            message.setTo(user.getEmail());
            message.setFrom(mailSender.getUsername());
            Map model = new HashMap();
            model.put("webapp", configuration.getWebappServerRoot());
            model.put("username", user.getUsername());
            model.put("uid", user.getOid());
            model.put("aid", passwordToken);
            String text = VelocityEngineUtils.mergeTemplateIntoString(velocityEngine,
                 Constants.FORGET_PASSWORD_EMAIL_VM_NAME, "UTF-8", model);
            message.setText(text, true);
         }
      };
      try {
         this.mailSender.send(preparator);
         log.info("Sent 'Reset password' email to " + user.getEmail());
         user.setToken(passwordToken);
         updateUser(user);
         return user;
      } catch (MailException e) {
         log.error("Can't send 'Reset password' email", e);
         return null;
      }
   }

   public User checkPasswordToken(long uid, String passwordToken) {
      User user = getUserById(uid);
      if (user != null && passwordToken.equals(user.getToken())) {
         return user;
      }
      return null;
   }

   public boolean resetPassword(long uid, String password, String passwordToken) {
      User user = getUserById(uid);
      if (user != null && passwordToken.equals(user.getToken())) {
         user.setPassword(new Md5PasswordEncoder().encodePassword(password, user.getUsername()));
         user.setToken(null);
         updateUser(user);
         return true;
      }
      return false;
   }
   
}
