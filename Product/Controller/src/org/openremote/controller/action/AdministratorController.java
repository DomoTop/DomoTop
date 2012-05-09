/*
 * OpenRemote, the Home of the Digital Home. Copyright 2008-2012, OpenRemote Inc.
 * 
 * See the contributors.txt file in the distribution for a full listing of individual contributors.
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package org.openremote.controller.action;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.openremote.controller.Constants;
import org.openremote.controller.service.CertificateService;
import org.openremote.controller.service.ClientService;
import org.openremote.controller.service.ConfigurationService;
import org.openremote.controller.service.GroupService;
import org.openremote.controller.spring.SpringContext;
import org.openremote.controller.utils.AuthenticationUtil;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

/**
 * The controller for Administrator management.
 * 
 * @author <a href="mailto:melroy.van.den.berg@tass.nl">Melroy van den Berg</a> 2012
 * @author <a href="mailto:vincent.kriek@tass.nl">Vincent Kriek</a>
 */
public class AdministratorController extends MultiActionController 
{
   private static final ConfigurationService configurationService = (ConfigurationService) SpringContext.getInstance().getBean(
         "configurationService"); 
   private static final ClientService clientService = (ClientService) SpringContext.getInstance().getBean(
         "clientService");
   private static final GroupService groupService = (GroupService) SpringContext.getInstance().getBean(
         "groupService");   
   private static final CertificateService certificateService = (CertificateService) SpringContext.getInstance().getBean(
         "certificateService");  
   static {
      Security.addProvider(new BouncyCastleProvider());
   }
   
   private PrivateKey privateKey = null;
   
   /**
    * Create a new CA, imports the certificate into the server's key store and saves the private key
    * 
    * @param request
    *           HTTP servlet request
    * @param response
    *           HTTP response to the servlet
    */
   public ModelAndView setupCA(HttpServletRequest request, HttpServletResponse response) throws IOException,
         ServletRequestBindingException {    
      if(!AuthenticationUtil.isAuth(request, configurationService)){
         response.sendRedirect("/controller/login");
         return null;
      }
      boolean success = false;
      success = certificateService.deleteClientKeyStore();
      if(success)
      {
         if(clientService.dropClients() == 1)
         { 
            success = true;
         }
         else
         {
            success = false;
         }
      }
      else
      {
         response.getWriter().print("Couldn't delete client keystore.");
      }
      
      if(success)
      {         
         success = certificateService.createCa();
   
         if(!success)
         {
            response.getWriter().print("Failed to create and/or save a CA certificate into the server's keystore.");
         }
      }
      else
      {
         response.getWriter().print("Failed to empty the database table.");
      }
      
      if(success)
      {   
         // Set the reboot flag
         configurationService.setReboot();
         
         response.getWriter().print(Constants.OK);
      }
      
      return null;
   }
   
   /**
    * Save settings from the administrator panel
    * 
    * @param request
    *           HTTP servlet request
    * @param response
    *           HTTP response to the servlet
    * @return
    * @throws IOException
    * @throws ServletRequestBindingException
    */
   @SuppressWarnings("rawtypes")
   public ModelAndView saveSettings(HttpServletRequest request, HttpServletResponse response) throws IOException,
   ServletRequestBindingException 
   {      
      if(!AuthenticationUtil.isAuth(request, configurationService)){
         response.sendRedirect("/controller/login");
         return null;
      }
      
      Enumeration names = request.getParameterNames();
      String errorMessage = "";
      boolean success = false;
      boolean reboot = false;
      boolean changeAuthentication = false, authenticationValue = false;
      
      while(names.hasMoreElements())
      {
         String name = (String) names.nextElement();
         
         // Ignore the method and composer_password name
         if(!name.equals("method") && !name.equals("composer_password"))
         {
            if(!configurationService.isDisabled(name))
            {
               success = true;
            }
            else
            {
               errorMessage = "The configuration item " + name + " is disabled, so can't be updated, please don't cheat.";
               logger.error("The configuration item is disabled, so can't be updated.");
               success = false;
               reboot = false;
            }
            
            if(success)
            {
               String value = request.getParameter(name);
               // if type boolean
               if(value.equals("true") || value.equals("false"))
               {
                  boolean newValue = Boolean.parseBoolean(value);
                  boolean oldValue = configurationService.getBooleanItem(name);
                  
                  success = configurationService.updateItem(name, newValue) == 1;
                  
                  if(name.equals("authentication")) 
                  {                     
                     if(newValue != oldValue)
                     {
                        // authentication change should be applied
                        changeAuthentication = true;
                        authenticationValue = newValue;
                     } 
                  }
               }
               else // type is String
               {
                  success = configurationService.updateItem(name, value) == 1;
               }
            }
         }  

         if(!success)
         {
            break;
         }
      }
      
      if(changeAuthentication)
      {
         if(success = this.setAuthentication(authenticationValue))
         {
            reboot = true;
         }
         else
         {
            errorMessage = "Could not update disable flag of the configuration item(s).";
         }
      }
      
      if(success)
      {         
         if(reboot) {
            response.getWriter().print(Constants.OK_REBOOT);
         } else {
            response.getWriter().print(Constants.OK);
         }
      }
      else
      {
         response.getWriter().print("Failed to save the configuration into the database: " + errorMessage);
      }
      return null;
   }
   
   /**
    * Do all changes when authentication checkbox value is changed
    * @param newValue true if authentication is enabled, false when authentication should be disabled
    * @return true if everything went successfully otherwise false
    */
   private boolean setAuthentication(boolean newValue) 
   {
      boolean success = true;
      
      // Check for "authentication" to set that in the web.xml
      try 
      {
         // Change the disable flag of the following configuration items: 
         // Note: If newValue is true updateConfiguration accept !newValue (false) and visa versa
         if(configurationService.updateConfiguration("composer_username", !newValue) != 1)
         {
            success = false;
         }
         if(configurationService.updateConfiguration("ca_path", !newValue) != 1)
         {
            success = false;
         }
         if(configurationService.updateConfiguration("pin_check", !newValue) != 1)
         {
            success = false;
         }
         if(configurationService.updateConfiguration("group_required", !newValue) != 1)
         {
            success = false;
         }
         
         if(success)
         {
            // Enable/disable the authentication in web.xml
            configurationService.setAuthentication(newValue);
         }
      } catch (IOException e) {
         success = false;
         logger.error("Authentication could not be enabled or disabled");
         logger.error(e.getMessage());
      }  
      return success;
   }

   /**
    * Request handler of deleting an user from the client key store and database
    * 
    * @param request
    *           HTTP servlet request
    * @param response
    *           HTTP response to the servlet
    */
   public ModelAndView deleteUser(HttpServletRequest request, HttpServletResponse response) throws IOException,
   ServletRequestBindingException {
      if(!AuthenticationUtil.isAuth(request, configurationService)){
         response.sendRedirect("/controller/login");
         return null;
      }  
      int clientID = -1;
      
      // Client ID number check
      try
      {
         clientID = Integer.parseInt(request.getParameter("client_id"));
      } catch ( NumberFormatException e) {
         logger.error("Client ID is not a number");
      }
      
      if(clientService.isClientIDValid(clientID))
      {
         
         String alias = "";         
         try {
            ResultSet resultSet = clientService.getClient(clientID);
            while (resultSet.next()) 
            {
               alias = resultSet.getString("client_alias");
            }
            clientService.free();
         } catch (SQLException e) {
            logger.error(e.getMessage());
         }
         
         int returnResult = clientService.removeClient(clientID);
         if (returnResult == 1)
         {
            if(certificateService.deleteClientFromClientKeyStore(alias))
            {
               response.getWriter().print(Constants.OK);
            }
         }
         else
         {
            response.getWriter().print("Device could not be removed from the database.");
         }
      }
      else
      {
         response.getWriter().print("Device is invalid.");
      }
      return null;
   }
   
   /**
    * Request handler for accepting or denying an user
    * 
    * @param request
    *           HTTP servlet request
    * @param response
    *           HTTP response to the servlet
    */
   public ModelAndView changeUserStatus(HttpServletRequest request, HttpServletResponse response) throws IOException,
         ServletRequestBindingException {
      if(!AuthenticationUtil.isAuth(request, configurationService)){
         response.sendRedirect("/controller/login");
         return null;
      }      
      boolean result = false;
      String pin = "";
      String action = request.getParameter("action");
      int clientID = -1;
      
      // Client ID number check
      try
      {
         clientID = Integer.parseInt(request.getParameter("client_id"));
      } catch ( NumberFormatException e) {
         logger.error("Client ID is not a number");
      }   
      String errorString = "";
      
      // Get client's pin
      try {
         ResultSet resultSet = clientService.getClient(clientID);
         while (resultSet.next()) 
         {
            pin = resultSet.getString("client_pincode");
         }
         clientService.free();
      } catch (SQLException e) {
         logger.error(e.getMessage());
      }

      // If action equals accept and the pin check is activated, checking for the pin
      // if there is not pin check set result true
      boolean pinCheck = configurationService.isPinCheckActive();
      
      if(action.equals("accept") && pinCheck)
      {
         String requestPin = request.getParameter("pin").toLowerCase();
         if(requestPin.isEmpty())
         {
            result = false;
            errorString = "The pin you entered is empty. <br/>Please enter the pin shown on the device.";
         }
         else
         {
            if(requestPin.equals(pin))
            {
               result = true; // passed
            }
            else
            {
               errorString = "The pin you entered doesn't match, please try again.";
            }            
         }
      }
      else
      {
         result = true;
      }
      
      // Check if the client ID is valid in the database before we continue
      if(!clientService.isClientIDValid(clientID))
      {
         result = false;
      }
      
      try {
         
         if(result)
         {
            if (action.equals("accept")) // accept device
            {
               result = this.acceptClient(clientID);
            } 
            else if (action.equals("deny")) // deny device
            {
               result = this.denyClient(clientID);
            }
            else if (action.equals("remove")) // remove device
            {
               result = this.removeClient(clientID);
            }
         }
         
         // if the user is successfully accepted, denied or removed
         if (result) 
         {
            // If the action was deny, result the pin in the response
            if (action.equals("deny"))
            {
               response.getWriter().print(Constants.OK + "-" + clientID + "-" + action + "-" + pin + "-" + pinCheck);
            }
            else
            {
               response.getWriter().print(Constants.OK + "-" + clientID + "-" + action);
            }
         } 
         else // user was not successfully accepted, denied or removed
         {
            if (action.equals("deny"))
            {
               response.getWriter().print("Client certificate could not be denied, this device still has access.");
            } 
            else if (action.equals("accept"))
            {
               if(!errorString.isEmpty())
               {
                  response.getWriter().print(errorString);
               }
               else
               {
                  response.getWriter().print("Certificate has not been created and/or added to the client key store.");
               }
            }
            else if (action.equals("remove"))
            {
               response.getWriter().print("Device is not successfully removed from the database and/or client key store.");
            }
         }
      } catch (NullPointerException e) {
         response.getWriter().print("nullpointer: " + e.getMessage());
      }
      return null;
   }
  
   /**
    * Request handler for chaning the group of an user
    * 
    * @param request
    *           HTTP servlet request
    * @param response
    *           HTTP response to the servlet
    */
   public ModelAndView updateGroup(HttpServletRequest request, HttpServletResponse response) throws IOException,
         ServletRequestBindingException {
      if(!AuthenticationUtil.isAuth(request, configurationService)){
         response.sendRedirect("/controller/login");
         return null;
      }
      boolean result = false;
      int groupID = -1, clientID = -1;
      
      // Group ID number check & Client ID number check
      try
      {
         groupID = Integer.parseInt(request.getParameter("group_id"));
         clientID = Integer.parseInt(request.getParameter("client_id"));
         result = true;
      } catch ( NumberFormatException e) {
         result = false;
         logger.error("Group ID and/or Client ID is not a number");
      } 

      String errorString = "";
      if(result)
      {
         // Result is true when groupID is -1, meaning reset the group ID to NULL
         if(groupID == -1)
         {
            result = true;
         }
         else
         {
            // Group ID validation
            groupService.getGroup(groupID);
            if(groupService.getNumGroups() == 1)
            {
               result = true;
            }
            else
            {
               result = false;
               errorString = "Group ID is not valid: " + groupID;
            }
            groupService.free();
         }
         
         // Check if the client ID is valid in the database before we continue
         if(!clientService.isClientIDValid(clientID))
         {
            result = false;
            errorString = "Clien ID is not valid: " + clientID;
         }
      }      
      
      if(result)
      {
         int returnResult = clientService.updateClientGroup(clientID, groupID);
         if(returnResult == 1)
         {
            response.getWriter().print(Constants.OK);
         }
         else
         {
            response.getWriter().print("The group of the device is not successfully updated.");
         }
      }
      else
      {
         response.getWriter().print(errorString);
      }
      return null;
   }   

   /**
    * Request handler logging out the administrator user
    * 
    * @param request
    *           HTTP servlet request
    * @param response
    *           HTTP response to the servlet
    */
   public ModelAndView logOut(HttpServletRequest request, HttpServletResponse response) throws IOException,
   ServletRequestBindingException 
   {
      HttpSession session = request.getSession(true);
      session.removeAttribute(AuthenticationUtil.AUTH_SESSION);
      
      response.getWriter().print(Constants.OK);
      return null;
   }
   
   /**
    * Accept the client, adding it to the client key store
    * 
    * @param clientID client ID
    * @return true if succeed otherwise false
    */
   private boolean acceptClient(int clientID)
   {
      boolean result = false;
      if(privateKey == null)
      {
         privateKey = certificateService.getCaPrivateKey();
      }
      String alias = "";      
      try {
         ResultSet resultSet = clientService.getClient(clientID);
         while (resultSet.next()) 
         {
            alias = resultSet.getString("client_alias");
         }
         clientService.free();
      } catch (SQLException e) {
         logger.error(e.getMessage());
      }
      
      try {
         PKCS10CertificationRequest certificateRequest = certificateService.getCertificationRequest(alias);
         
         X509Certificate certificate = certificateService.signCertificate(certificateRequest, privateKey, Integer.toString(clientID + 1));
                        
         if (certificate != null)
         {
            // Create client key store if necessary
            if(!certificateService.createClientKeyStore())
            {
               logger.error("Failed to create client keystore.");
            }
            
            if(certificateService.saveCertificateToClientKeyStore(certificate, alias))
            {
               // Update the client database
               int statusReturn = clientService.updateClientStatus(clientID, true);
               if(statusReturn == 1)
               {
                  result = true;
               }
               else
               {
                  logger.error("Accept client: Datebase couldn't be updated.");
               }
            }
            else
            {
               logger.error("Couldn't save the certificate into the key store.");
            }
         }
         else
         {
            logger.error("Certificate is null.");
         }
      } catch (InvalidKeyException e) {
         result = false;
         logger.error("Signing error - Invalid Key: " + e.getMessage());
      } catch (NoSuchAlgorithmException e) {
         result = false;
         logger.error("Signing error - No Such Algorithem: " + e.getMessage());
      } catch (NoSuchProviderException e) {
         result = false;
         logger.error("Signing error - No Such Provider: " + e.getMessage());
      } catch (SignatureException e) {
         result = false;
         logger.error("Signing error - Signature: " + e.getMessage());
      } catch (OperatorCreationException e) {
         result = false;
         logger.error("Signing error - Operator Creation: " + e.getMessage());
      } catch (CertificateException e) {
         result = false;
         logger.error("Signing error - Certificate: " + e.getMessage());
      } catch (IOException e) {
         result = false;
         logger.error("Signing error - IO Exception: " + e.getMessage());
      }      
      return result;
   }
   
   /**
    * Deny the client
    * 
    * @param alias client alias, which should be unique
    * @param clientID client ID
    * @return
    */
   private boolean denyClient(int clientID) 
   {
      boolean returnValue = false;

      int statusReturn = clientService.updateClientStatus(clientID, false);
      if (statusReturn == 1)
      {
         returnValue = true;
      }
      else
      {
         logger.error("Deny client: Datebase couldn't be updated.");
      }
      return returnValue;
   }
   
   /**
    * Remove the client from the client key store
    * 
    * @param clientKeyStorePath Client key store path
    * @param clientID client ID
    * @return
    */
   private boolean removeClient(int clientID)
   {
      boolean returnValue = false;
      
      String alias = "";      
      try {
         ResultSet resultSet = clientService.getClient(clientID);
         while (resultSet.next()) 
         {
            alias = resultSet.getString("client_alias");
         }
         clientService.free();
      } catch (SQLException e) {
         logger.error(e.getMessage());
      }
      
      if(certificateService.deleteClientFromClientKeyStore(alias))
      {
         // Update the client database
         int statusReturn = clientService.removeClient(clientID);
         if(statusReturn == 1)
         {
            returnValue = true;
         }
         else
         {
            logger.error("Remove client: Datebase couldn't be updated.");
         }
      }
      return returnValue;
   }
}
