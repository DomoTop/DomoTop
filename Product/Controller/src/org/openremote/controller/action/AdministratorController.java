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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.asn1.x509.X509Extension;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.bc.BcRSAContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.openremote.controller.Constants;
import org.openremote.controller.service.CertificateService;
import org.openremote.controller.service.ClientService;
import org.openremote.controller.service.ConfigurationService;
import org.openremote.controller.spring.SpringContext;
import org.openremote.controller.utils.AuthenticationUtil;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

import sun.misc.BASE64Decoder;

/**
 * The controller for Administrator management.
 * 
 * @author <a href="mailto:melroy.van.den.berg@tass.nl">Melroy van den Berg</a> 2012
 */
public class AdministratorController extends MultiActionController 
{
   private static final ConfigurationService configurationService = (ConfigurationService) SpringContext.getInstance().getBean(
         "configurationService"); 
   private static final ClientService clientService = (ClientService) SpringContext.getInstance().getBean(
         "clientService");  
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
      if(!AuthenticationUtil.isAuth(request)){
         return null;
      }
      boolean success = false;
      
      if(clientService.dropClients() == 1)
      { 
         success = true;
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
      if(!AuthenticationUtil.isAuth(request)){
         return null;
      }
      
      Enumeration names = request.getParameterNames(); 
      boolean success = false;
      while(names.hasMoreElements())
      {
         String name = (String) names.nextElement();
         
         // Ignore the method and composer_password name
         if(!name.equals("method") && !name.equals("composer_password"))
         {
            String value = request.getParameter(name);
            // if type boolean
            if(value.equals("true") || value.equals("false"))
            {
               success = (configurationService.updateItem(name, Boolean.parseBoolean(value)) == 1) ? true : false;
            }
            else // type is String
            {
               success = (configurationService.updateItem(name, value) == 1) ? true : false;
            }
         }  
         
         if(!success)
         {
            break;
         }
      }
      
      if(success)
      {      
         response.getWriter().print(Constants.OK);
      }
      else
      {
         response.getWriter().print("Failed to save the configuration into the database");
      }
      return null;
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
      if(!AuthenticationUtil.isAuth(request)){
         return null;
      }  
      int clientID = Integer.parseInt(request.getParameter("client_id"));      
      
      if(clientService.isClientIDValid(clientID))
      {
         int returnResult = clientService.removeClient(clientID);
         if (returnResult == 1)
         {
            response.getWriter().print(Constants.OK);
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
      if(!AuthenticationUtil.isAuth(request)){
         return null;
      }      
      boolean result = false;
      String pin = "";
      String alias = "";
      String action = request.getParameter("action");
      int clientID = Integer.parseInt(request.getParameter("client_id"));      
      String errorString = "";
      
      try {
         ResultSet resultSet = clientService.getClient(clientID);
         while (resultSet.next()) 
         {
            alias = resultSet.getString("client_alias");
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
         String requestPin = request.getParameter("pin");
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
               result = this.acceptClient(alias, clientID);
            } 
            else if (action.equals("deny")) // deny device
            {
               result = this.denyClient(clientID);
            }
            else if (action.equals("remove")) // remove device
            {
               result = this.removeClient(alias, clientID);
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
    * @param alias client alias, which should be unique
    * @param clientID client ID
    * @return true if succeed otherwise false
    */
   private boolean acceptClient(String alias, int clientID)
   {
      boolean result = false;
      if(privateKey == null)
      {
         privateKey = certificateService.getCaPrivateKey();
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
    * @param alias client alias, which should be unique
    * @param clientID client ID
    * @return
    */
   private boolean removeClient(String alias, int clientID)
   {
      boolean returnValue = false;
      
      if(certificateService.deleteClientFromKeyStore(alias))
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
