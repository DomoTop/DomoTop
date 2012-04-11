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

import java.io.BufferedReader;
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
import org.openremote.controller.ControllerConfiguration;
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
   private static final String rootCADir = ControllerConfiguration.readXML().getCaPath();
   private static final String KEYSTORE_PATH = rootCADir + "/server.jks";
   private static final String CLIENT_KEYSTORE_PATH =  rootCADir + "/client_certificates.jks";
   private static final String CRTDir = "/ca/certs";
   private static final String CSRDir = "/ca/csr";

   private static final String KEYSTORE_PASSWORD = "password";
   private static final int NUM_ALLOWED_INTERMEDIATE_CAS = 0;

   private static final X500Name CA_NAME = new X500Name("C=NL,O=TASS,OU=Software Developer,CN=CA_MelroyvdBerg");
   
      
   private static final ClientService clientService = (ClientService) SpringContext.getInstance().getBean(
         "clientService");  
   private static final ConfigurationService configurationService = (ConfigurationService) SpringContext.getInstance().getBean(
         "configurationService");   
   static {
      Security.addProvider(new BouncyCastleProvider());
   }
   
   private PrivateKey privateKey = null;
   
   /**
    * Create a new CA, imports the certificate into the server's key store and saves the private key
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
      
      KeyPair KPair = null;
      X509Certificate cert = null;
      boolean success = false;
      
      if(clientService.dropClients() == 1)
      { 
         success = true;
      }
      
      if(success)
      {         
         KPair = this.createKeyPair();      
         cert = this.buildCertificate(KPair, CA_NAME);            
     
         if(cert != null && KPair != null)
         {
            if(!this.keyStoreExists(KEYSTORE_PATH))
            {
               if(!createKeyStore(KEYSTORE_PATH))
               {
                  logger.error("Failed to create CA keystore.");
               }
            }
            success = this.saveToKeyStore(KPair, cert, KEYSTORE_PATH, "ca.alias");
         }
         else
         {
            logger.error("No CA certificate generated or no key pair generated.");
         }
   
         if(success)
         {
            response.getWriter().print(Constants.OK);
         }
         else
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
         Process child = Runtime.getRuntime().exec("service tomcat6 restart");
      }
      
      return null;
   }
   
   @SuppressWarnings("rawtypes")
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
         if(!name.equals("method"))
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
            errorString = "The pin you entered is empty. Please enter the pin shown on the device.";
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
               result = this.acceptClient(CLIENT_KEYSTORE_PATH, alias, clientID);
            } 
            else if (action.equals("deny")) // deny device
            {
               result = this.denyClient(clientID);
            }
            else if (action.equals("remove")) // remove device
            {
               result = this.removeClient(CLIENT_KEYSTORE_PATH, alias, clientID);
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
   

   public ModelAndView logOut(HttpServletRequest request, HttpServletResponse response) throws IOException,
   ServletRequestBindingException 
   {
      HttpSession session = request.getSession(true);
      session.removeAttribute(AuthenticationUtil.AUTH_SESSION);
      
      response.getWriter().print(Constants.OK);
      return null;
   }
   
   private boolean acceptClient(String clientKeyStorePath, String alias, int clientID)
   {
      boolean result = false;
      if(privateKey == null)
      {
         privateKey = this.getPrivateKey();
      }
      
      try {
         PKCS10CertificationRequest certificateRequest = this.getCertificationRequest(alias);
         
         X509Certificate certificate = this.signCertificate(certificateRequest, privateKey, Integer.toString(clientID + 1));
                        
         if (certificate != null)
         {
            if(!this.keyStoreExists(clientKeyStorePath))
            {
               if(!createKeyStore(clientKeyStorePath))
               {
                  logger.error("Failed to create client keystore.");
               }
            }
            
            if(this.saveToClientKeyStore(certificate, clientKeyStorePath, alias))
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
    * @param alias
    * @param clientID
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
    * @param clientKeystorePath
    * @param alias
    * @return
    */
   private boolean removeClient(String clientKeystorePath, String alias, int clientID)
   {
      boolean returnValue = false;
      
      if(this.deleteClientKeyStore(clientKeystorePath, alias))
      {
         // Update the client database
         // TODO:
         /*
         int statusReturn = clientService.removeClient(clientID);
         if(statusReturn == 1)
         {
            returnValue = true;
         }
         else
         {
            logger.error("Remove client: Datebase couldn't be updated.");
         }
         */
      }
      return returnValue;
   }

   /**
    * Get the certificate file from the csr directory and create and returns a certificationRequest
    * @param username
    * @return
    * @throws IOException
    */
   private PKCS10CertificationRequest getCertificationRequest(String alias) throws IOException {
      File file = new File(rootCADir + "/" + CSRDir + "/" + alias + ".csr");
      String data = "";

      FileInputStream fis = new FileInputStream(file);
      data = convertStreamToString(fis);

      BASE64Decoder decoder = new BASE64Decoder();
      byte[] decodedBytes = decoder.decodeBuffer(data);

      return new PKCS10CertificationRequest(decodedBytes);
   }

   /**
    * Convert a input stream to a String
    * @param is
    * @return
    * @throws IOException
    */
   private String convertStreamToString(InputStream is) throws IOException {
      if (is != null) {
         Writer writer = new StringWriter();

         char[] buffer = new char[1024];
         try {
            Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
               writer.write(buffer, 0, n);
            }
         } finally {
            is.close();
         }
         return writer.toString();
      } else {
         return "";
      }
   }
   
   /**
    * Create a key pair (public & private key) using the RSA algorithm 
    * @return Generated KeyPair
    */
   private KeyPair createKeyPair() 
   {
      KeyPair KPair = null;
      
      try {
         KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
         keyPairGenerator.initialize(2048);
         KPair = keyPairGenerator.generateKeyPair();
      } catch (NoSuchAlgorithmException e) {
         logger.error("Ca: " + e.getMessage());
      }
      return KPair;
   }   
   
   /**
    * Build a new X509 certificate, using the key pair earlier created and X500Name
    * @param KPair is the KeyPair object
    * @param name X500Name with information about the issuer
    * @return a new X509Certificate, returns null if something went wrong
    */
   private X509Certificate buildCertificate(KeyPair KPair, X500Name name)
   {
      @SuppressWarnings("unused")
      boolean success = false;
      ContentSigner sigGen;
      X509Certificate cert = null;
      JcaX509ExtensionUtils extUtils = null;
      
      try {
         extUtils = new JcaX509ExtensionUtils();
      } catch (NoSuchAlgorithmException e) {
         logger.error("Generate CA certificate: " + e.getMessage());
      }
      SubjectPublicKeyInfo keyInfo = SubjectPublicKeyInfo.getInstance(KPair.getPublic().getEncoded());
      
      Calendar cal = Calendar.getInstance();
      cal.set(cal.get(Calendar.YEAR) + 100, 04, 18, 13, 30);
      
      X509v3CertificateBuilder myCertificateGenerator = new X509v3CertificateBuilder(name,
            new BigInteger("41"), 
            new Date(System.currentTimeMillis()), 
            cal.getTime(), 
            name,
            keyInfo);
      try
      {
         myCertificateGenerator.addExtension(X509Extension.subjectKeyIdentifier, false,
               extUtils.createSubjectKeyIdentifier(KPair.getPublic()));
   
         myCertificateGenerator.addExtension(X509Extension.authorityKeyIdentifier, false,
               extUtils.createAuthorityKeyIdentifier(KPair.getPublic()));
         
         myCertificateGenerator.addExtension(X509Extension.basicConstraints, false,
               new BasicConstraints(NUM_ALLOWED_INTERMEDIATE_CAS ));
         
         // prepare the signer with the private Key
         AlgorithmIdentifier sigAlgId = new DefaultSignatureAlgorithmIdentifierFinder().find("SHA1withRSA");
         AlgorithmIdentifier digAlgId = new DefaultDigestAlgorithmIdentifierFinder().find(sigAlgId);
   
         // hopefully format is PKCS#8
         AsymmetricKeyParameter asymKey = PrivateKeyFactory.createKey(KPair.getPrivate().getEncoded());


         sigGen = new BcRSAContentSignerBuilder(sigAlgId, digAlgId).build(asymKey);

         // Build
         X509CertificateHolder holder = myCertificateGenerator.build(sigGen);
         cert = new JcaX509CertificateConverter().getCertificate(holder);
         success = true;
      } catch (OperatorCreationException e) {
         success = false;
         logger.error("Generate CA certificate: " + e.getMessage());
      } catch (CertificateException e) {
         success = false;
         logger.error("Generate CA certificate: " + e.getMessage());
      } catch (CertIOException e) {
         success = false;
         logger.error("Generate CA certificate: " + e.getMessage());
      } catch (IOException e) {
         success = false;
         logger.error("Generate CA certificate: " + e.getMessage());
      }
      return cert;
   }

   /**
    * And the certificate to the server's key store file
    * @param KPair the KeyPair object
    * @param cert X509Certificate
    * @param keyStoreFile the path of the file where the key store is located
    * @param the alias of the certificate
    * @return true if success or false if unsuccessfully
    */
   private boolean saveToKeyStore(KeyPair KPair, X509Certificate cert, String keyStoreFile, String alias) 
   {
      boolean success = false;
      KeyStore privateKS;
      try
      {
         privateKS = KeyStore.getInstance("JKS");

         // Load the key store to memory.
         FileInputStream fis = new FileInputStream(keyStoreFile);  
         privateKS.load(fis, KEYSTORE_PASSWORD.toCharArray());  
       
         // Import the private key to the key store
         privateKS.setKeyEntry(alias, KPair.getPrivate(),  
               KEYSTORE_PASSWORD.toCharArray(),  
               new java.security.cert.Certificate[]{cert});                  
         // Write the key store back to disk                 
         privateKS.store(new FileOutputStream(keyStoreFile), KEYSTORE_PASSWORD.toCharArray());      
         success = true;
      } catch (KeyStoreException e) {
         success = false;
         logger.error("Key store: " + e.getMessage());
      } catch (NoSuchAlgorithmException e) {
         success = false;
         logger.error("Key store: " + e.getMessage());
      } catch (CertificateException e) {
         success = false;
         logger.error("Key store: " + e.getMessage());
      } catch (IOException e) {
         logger.error("Key store: " + e.getMessage());
      }
      return success;
   }
   
   /**
    * Saves the client certificate into a client key store file
    * @param cert the X509 certificate
    * @param keyStoreFile the file path to the key store
    * @param alias the alias of the certificate
    * @return true if success, false if unsuccessfully
    */
   private boolean saveToClientKeyStore(X509Certificate cert, String keyStoreFile, String alias) 
   {
      boolean success = false;
      KeyStore clientKS;
      try
      {
         clientKS = KeyStore.getInstance("JKS");

         // Load the key store to memory.
         FileInputStream fis = new FileInputStream(keyStoreFile);  
         clientKS.load(fis, KEYSTORE_PASSWORD.toCharArray());  
       
         // Import the certificate to the key store
         clientKS.setCertificateEntry(alias, cert);
         
         // Write the key store back to disk                 
         clientKS.store(new FileOutputStream(keyStoreFile), KEYSTORE_PASSWORD.toCharArray());      
         success = true;
      } catch (KeyStoreException e) {
         success = false;
         logger.error("Key store: " + e.getMessage());
      } catch (NoSuchAlgorithmException e) {
         success = false;
         logger.error("Key store: " + e.getMessage());
      } catch (CertificateException e) {
         success = false;
         logger.error("Key store: " + e.getMessage());
      } catch (IOException e) {
         logger.error("Key store: " + e.getMessage());
      }
      return success;
   }
   
   /**
    * Delete the client certificate from the servers keystore file
    * 
    * @param keyStoreFile the file path to the key store
    * @param alias of the client certificate that will be removed
    * @return true if successfully removed from the keystore else false
    */
   private boolean deleteClientKeyStore(String keyStoreFile, String alias) {
      boolean success = false;
      KeyStore clientKS;
      try
      {
         clientKS = KeyStore.getInstance("JKS");

         // Load the key store to memory.
         FileInputStream fis = new FileInputStream(keyStoreFile);  
         clientKS.load(fis, KEYSTORE_PASSWORD.toCharArray());  
       
         // Import the certificate to the key store
         clientKS.deleteEntry(alias);
         
         // Write the key store back to disk                 
         clientKS.store(new FileOutputStream(keyStoreFile), KEYSTORE_PASSWORD.toCharArray());      
         success = true;
      } catch (KeyStoreException e) {
         success = false;
         logger.error("Key store: " + e.getMessage());
      } catch (NoSuchAlgorithmException e) {
         success = false;
         logger.error("Key store: " + e.getMessage());
      } catch (CertificateException e) {
         success = false;
         logger.error("Key store: " + e.getMessage());
      } catch (IOException e) {
         logger.error("Key store: " + e.getMessage());
      }
      return success;
   }
   
   /**
    * Get the private key from the CA, using the server key store file   
    * @return PrivateKey Object
    */
   private PrivateKey getPrivateKey()
   {
      PrivateKey privateKey = null;
      KeyStore privateKS;
      try
      {
         privateKS = KeyStore.getInstance("JKS");
         
         FileInputStream fis = new FileInputStream(KEYSTORE_PATH);  
         privateKS.load(fis, KEYSTORE_PASSWORD.toCharArray());  
         Key key = privateKS.getKey("ca.alias", KEYSTORE_PASSWORD.toCharArray());
         if(key instanceof PrivateKey) 
         {
            privateKey = (PrivateKey)key;            
         }
      } catch (UnrecoverableKeyException e) {
         logger.error("Get private key: " + e.getMessage());
      } catch (KeyStoreException e) {
         logger.error("Get private key: " + e.getMessage());
      } catch (NoSuchAlgorithmException e) {
         logger.error("Get private key: " + e.getMessage());
      } catch (CertificateException e) {
         logger.error("Get private key: " + e.getMessage());
      } catch (IOException e) {
         logger.error("Get private key: " + e.getMessage());
      }
      return privateKey;
   }

   private boolean keyStoreExists(String keyStoreFile)
   {
      File f = new File(keyStoreFile);
      return (f.exists() ? true : false);
   }
   
   private boolean createKeyStore(String keyStoreFile)
   {
      boolean returnValue = false;
      // CREATE A KEYSTORE OF TYPE "Java Key Store"  
      try
      {
         KeyStore ks = KeyStore.getInstance("JKS");  
         /* 
          * LOAD THE STORE 
          * The first time you're doing this (i.e. the keystore does not 
          * yet exist - you're creating it), you HAVE to load the keystore 
          * from a null source with null password. Before any methods can 
          * be called on your keystore you HAVE to load it first. Loading 
          * it from a null source and null password simply creates an empty 
          * keystore.
          */  
         ks.load( null, null ); 
         //SAVE THE KEYSTORE TO A FILE  
         ks.store( new FileOutputStream(keyStoreFile), KEYSTORE_PASSWORD.toCharArray() );  
         returnValue = true;
      } catch (NoSuchAlgorithmException e) {
         returnValue = false;
         logger.error("KeyStore: " + e.getMessage());
      } catch (CertificateException e) {
         returnValue = false;
         logger.error("KeyStore: " + e.getMessage());
      } catch (IOException e) {
         returnValue = false;
         logger.error("KeyStore: " + e.getMessage());
      } catch (KeyStoreException e) {
         returnValue = false;
         logger.error("KeyStore: " + e.getMessage());
      } 
      
      return returnValue;
   }
   
   /**
    * Sign a certificate using a PCKS10 Certification request file and the PrivateKey from the CA
    * 
    * @param inputCSR PCKS10 Certification Request file
    * @param caPrivate PrivateKey from CA
    * @return a new signed certificate
    * @throws InvalidKeyException
    * @throws NoSuchAlgorithmException
    * @throws NoSuchProviderException
    * @throws SignatureException
    * @throws IOException
    * @throws OperatorCreationException
    * @throws CertificateException
    */
   private X509Certificate signCertificate(PKCS10CertificationRequest inputCSR, PrivateKey caPrivate, String serial)
         throws InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException, SignatureException,
         IOException, OperatorCreationException, CertificateException
   {
      AlgorithmIdentifier sigAlgId = new DefaultSignatureAlgorithmIdentifierFinder().find("SHA1withRSA");
      AlgorithmIdentifier digAlgId = new DefaultDigestAlgorithmIdentifierFinder().find(sigAlgId);
      AsymmetricKeyParameter asymKey = PrivateKeyFactory.createKey(caPrivate.getEncoded());

      Calendar cal = Calendar.getInstance();
      cal.set(cal.get(Calendar.YEAR) + 100, 04, 18, 13, 30);
      
      X509v3CertificateBuilder myCertificateGenerator = new X509v3CertificateBuilder(CA_NAME,
            new BigInteger(serial), 
            new Date(System.currentTimeMillis()),
            cal.getTime(), 
            inputCSR.getSubject(), 
            inputCSR.getSubjectPublicKeyInfo());
      ContentSigner sigGen = new BcRSAContentSignerBuilder(sigAlgId, digAlgId).build(asymKey);

      X509CertificateHolder holder = myCertificateGenerator.build(sigGen);
      X509Certificate cert = new JcaX509CertificateConverter().getCertificate(holder);
      return cert;
   }
}
