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
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigInteger;
import java.nio.charset.Charset;
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
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.openssl.PasswordFinder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.bc.BcRSAContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.openremote.controller.Constants;
import org.openremote.controller.service.ClientService;
import org.openremote.controller.spring.SpringContext;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/**
 * The controller for Administrator management.
 * 
 * @author <a href="mailto:melroy.van.den.berg@tass.nl">Melroy van den Berg</a> 2012
 */
public class AdministratorController extends MultiActionController 
{
   // private static final String rootCADir = ControllerConfiguration.readXML().getCaPath();
   private static final String rootCADir = "/usr/share/tomcat6/cert/ca";
   private static final String KEY_STORE = "/usr/share/tomcat6/cert/server.jks";
   private static final String openssl = "openssl";
   private static final String CRTDir = "certs";
   private static final String CSRDir = "csr";

   private static final String KEYSTORE_PASSWORD = "password";
   private static final int NUM_ALLOWED_INTERMEDIATE_CAS = 0;

   private static final X500Name CA_NAME = new X500Name("C=NL,O=TASS,OU=Software Developer,CN=CA_Melroy");
   
   private static final ClientService clientService = (ClientService) SpringContext.getInstance().getBean(
         "clientService");    
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
      KeyPair KPair = null;
      X509Certificate cert = null;
            
      boolean success = false;
      
      KPair = this.createKeyPair();      
      cert = this.buildCertificate(KPair, CA_NAME);
            
      if(cert != null && KPair != null)
      {
         success = this.saveToKeyStore(KPair, cert);
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
      return null;
   }
   
   private KeyPair createKeyPair() 
   {
      KeyPair KPair = null;
      
      try {
         KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
         keyPairGenerator.initialize(2048); // 2048
         KPair = keyPairGenerator.generateKeyPair();
      } catch (NoSuchAlgorithmException e) {
         logger.error("Ca: " + e.getMessage());
      }
      return KPair;
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
      String action = request.getParameter("action");
      int clientID = Integer.parseInt(request.getParameter("client_id"));
      String pin = "";
      String clientUsername = "";

      try {
         ResultSet resultSet = clientService.getClient(clientID);
         while (resultSet.next()) {
            String clientFileName = resultSet.getString("client_file_name");
            clientUsername = clientFileName.substring(0, clientFileName.lastIndexOf('.'));

            pin = resultSet.getString("client_pincode");
         }
         clientService.free();
      } catch (SQLException e) {
         logger.error(e.getMessage());
      }

      try {
         int result = -1;
         if (action.equals("accept")) // trust
         {
            // result = executeOpenSSLCommand(clientUsername, true);

            if(privateKey == null)
            {
               privateKey = this.getPrivateKey();
            }
            
            logger.error("Trying to accept certificate:");
            try {
               PKCS10CertificationRequest certificateRequest = this.getCertificationRequest(clientUsername);
               logger.error("test: " + privateKey.getFormat() +  " - " + privateKey.getEncoded().toString());
               
               X509Certificate certificate = this.signCertificate(certificateRequest, privateKey);
               if (certificate != null
                     && this.saveCertificate(certificate, rootCADir + "/" + CRTDir + "/" + clientUsername + ".crt")) {
                  result = 0;
               } else {
                  logger.error("Certificate is null");
               }
            } catch (InvalidKeyException e) {
               result = -1;
               logger.error("Signing error - Invalid Key: " + e.getMessage());
            } catch (NoSuchAlgorithmException e) {
               result = -1;
               logger.error("Signing error - No Such Algorithem: " + e.getMessage());
            } catch (NoSuchProviderException e) {
               result = -1;
               logger.error("Signing error - No Such Provider: " + e.getMessage());
            } catch (SignatureException e) {
               result = -1;
               logger.error("Signing error - Signature: " + e.getMessage());
            } catch (OperatorCreationException e) {
               result = -1;
               logger.error("Signing error - Operator Creation: " + e.getMessage());
            } catch (CertificateException e) {
               result = -1;
               logger.error("Signing error - Certificate: " + e.getMessage());
            } catch (IOException e) {
               result = -1;
               logger.error("Signing error - IO Exception: " + e.getMessage());
            }
         } else if (action.equals("deny")) // revoke
         {
            result = executeOpenSSLCommand(clientUsername, false);
         }

         // OpenSSL Command successful
         if (result == 0) {
            // @TODO: Add the serial ID to the database and use index.txt
            // to get the right list in the administrator Panel.

            // If successfully revoked, than remove the certificate
            if (action.equals("deny")) {
               if (deleteCertificate(clientUsername)) {
                  int statusReturn = clientService.updateClientStatus(clientID, false);
                  int serialReturn = clientService.clearClientSerial(clientID);
                  if (statusReturn == 1 && serialReturn == 1) {
                     response.getWriter().print(Constants.OK + "-" + clientID + "-" + action + "-" + pin);
                  } else {
                     response.getWriter().print("Client is not successfully updated in the database");
                  }
               } else {
                  response.getWriter().print("Certificate is successfully revoked, but couldn't be removed.");
               }
            } else if (action.equals("accept")) {
               int statusReturn = clientService.updateClientStatus(clientID, true);
               int serialReturn = clientService.updateClientSerial(clientID);
               if (statusReturn == 1 & serialReturn == 1) {
                  response.getWriter().print(
                        Constants.OK + "-" + clientID + "-" + action + "-" + clientService.getSerial());
               } else {
                  response.getWriter().print("Client is not successfully updated in the database");
               }
            }
         } else {
            if (action.equals("deny")) {
               if (deleteCertificate(clientUsername)) {
                  response.getWriter().print(
                        "OpenSSL command failed, exit with exit code: " + result + "" + "\n\rCertificate deleted.");
               } else {
                  response.getWriter().print(
                        "OpenSSL command failed, exit with exit code: " + result + ". "
                              + "\n\rPlus the certificate couldn't be removed.");
               }
            } else {
               response
                     .getWriter()
                     .print(
                           "OpenSSL command failed, exit with exit code: "
                                 + result
                                 + ". "
                                 + "\n\rProbably database index.txt file problem from the CA, please check this file which is located in the CA path.");
            }
         }
      } catch (NullPointerException e) {
         response.getWriter().print("nullpointer: " + e.getMessage());
      } catch (InterruptedException e) {
         response.getWriter().print("interrupt: " + e.getMessage());
      }
      return null;
   }
   
   /**
    * Get the certificate file from the csr directory and create and returns a certificationRequest
    * @param username
    * @return
    * @throws IOException
    */
   private PKCS10CertificationRequest getCertificationRequest(String username) throws IOException {
      File file = new File(rootCADir + "/" + CSRDir + "/" + username + ".csr");
      String data = "";

      FileInputStream fis = new FileInputStream(file);
      data = convertStreamToString(fis);

      BASE64Decoder decoder = new BASE64Decoder();
      byte[] decodedBytes = decoder.decodeBuffer(data);

      return new PKCS10CertificationRequest(decodedBytes);
   }

   /**
    * Convert a Inputstream to a String
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
   
   private X509Certificate buildCertificate(KeyPair KPair, X500Name name)
   {
      @SuppressWarnings("unused")
      boolean success = false;
      JcaX509ExtensionUtils extUtils = null;
      
      try {
         extUtils = new JcaX509ExtensionUtils();
      } catch (NoSuchAlgorithmException e) {
         logger.error("Generate CA certificate: " + e.getMessage());
      }
      SubjectPublicKeyInfo keyInfo = SubjectPublicKeyInfo.getInstance(KPair.getPublic().getEncoded());

      X509v3CertificateBuilder myCertificateGenerator = new X509v3CertificateBuilder(name,
            new BigInteger("41"), 
            new Date(System.currentTimeMillis()), 
            new Date(System.currentTimeMillis() + 40 * 365 * 24 * 60 * 60 * 1000), 
            name,
            keyInfo);
      
      ContentSigner sigGen;
      X509Certificate cert = null;
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

   private boolean saveToKeyStore(KeyPair KPair, X509Certificate cert) 
   {
      boolean success = false;
      KeyStore privateKS;
      try
      {
         privateKS = KeyStore.getInstance("JKS");

         // Load the key store to memory.
         FileInputStream fis = new FileInputStream(KEY_STORE);  
         privateKS.load(fis, KEYSTORE_PASSWORD.toCharArray());  
       
         // Import the private key to the key store
         privateKS.setKeyEntry("ca.alias", KPair.getPrivate(),  
               KEYSTORE_PASSWORD.toCharArray(),  
               new java.security.cert.Certificate[]{cert});
         
         // Write the key store back to disk                 
         privateKS.store(new FileOutputStream(KEY_STORE), KEYSTORE_PASSWORD.toCharArray());      
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
   
   private PrivateKey getPrivateKey()
   {
      PrivateKey privateKey = null;
      KeyStore privateKS;
      try
      {
         privateKS = KeyStore.getInstance("JKS");
         
         FileInputStream fis = new FileInputStream(KEY_STORE);  
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
      
      /*
      PrivateKey returnValue = null;
      try {
         // Deserialize from a file
         File file = new File(rootCADir + "/" + PRIVATECA_KEY);
         ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
         // Deserialize the object
         returnValue = (PrivateKey) in.readObject();
     
         in.close();
      } catch (IOException e) {
         logger.error("Deserialize private key: " + e.getMessage());
      } catch (ClassNotFoundException e) {
         logger.error("Deserialize private key: " + e.getMessage());
      }*/
      return privateKey;
   }

   private boolean saveCertificate(X509Certificate certificate, String fileName) {
      boolean returnValue = false;
      try {
         // Get the encoded form which is suitable for exporting
         byte[] buf = certificate.getEncoded();

         FileOutputStream os = new FileOutputStream(new File(fileName));

         // Write in text form
         Writer wr = new OutputStreamWriter(os, Charset.forName("UTF-8"));
         wr.write("-----BEGIN CERTIFICATE-----\n");
         wr.write(new BASE64Encoder().encode(buf));
         wr.write("\n-----END CERTIFICATE-----\n");
         wr.flush();

         os.close();
         returnValue = true;
      } catch (CertificateEncodingException e) {
         returnValue = false;
      } catch (IOException e) {
         returnValue = false;
      }

      return returnValue;
   }

   private X509Certificate signCertificate(PKCS10CertificationRequest inputCSR, PrivateKey caPrivate)
         // , KeyPair pair
         throws InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException, SignatureException,
         IOException, OperatorCreationException, CertificateException
   {
      AlgorithmIdentifier sigAlgId = new DefaultSignatureAlgorithmIdentifierFinder().find("SHA1withRSA");
      AlgorithmIdentifier digAlgId = new DefaultDigestAlgorithmIdentifierFinder().find(sigAlgId);

      AsymmetricKeyParameter asymKey = PrivateKeyFactory.createKey(caPrivate.getEncoded());
      // SubjectPublicKeyInfo keyInfo = SubjectPublicKeyInfo.getInstance(pair
      // .getPublic().getEncoded());

      X509v3CertificateBuilder myCertificateGenerator = new X509v3CertificateBuilder(CA_NAME,
            new BigInteger("1"), 
            new Date(System.currentTimeMillis()),
            new Date(System.currentTimeMillis() + 30 * 365 * 24 * 60 * 60 * 1000), 
            inputCSR.getSubject(), 
            inputCSR.getSubjectPublicKeyInfo());
      ContentSigner sigGen = new BcRSAContentSignerBuilder(sigAlgId, digAlgId).build(asymKey);

      X509CertificateHolder holder = myCertificateGenerator.build(sigGen);
      /*
       * Very old code: Certificate eeX509CertificateStructure = holder.toASN1Structure(); CertificateFactory cf =
       * CertificateFactory.getInstance("X.509", "BC");
       * 
       * // Read Certificate InputStream is1 = new ByteArrayInputStream(eeX509CertificateStructure.getEncoded());
       * X509Certificate theCert = (X509Certificate) cf.generateCertificate(is1); is1.close();
       */

      X509Certificate cert = new JcaX509CertificateConverter().getCertificate(holder);
      return cert;
   }

   /**
    * Execute the OpenSSL command on command-line
    * 
    * @param username
    * @param accept
    *           true is for signing, false is to revoke a certificate
    * @return the exit code of the command executed
    * @throws NullPointerException
    * @throws IOException
    * @throws InterruptedException
    */
   private int executeOpenSSLCommand(String username, boolean accept) throws NullPointerException, IOException,
         InterruptedException {
      List<String> command = new ArrayList<String>();
      command.add(openssl); // command
      if (accept) // Signing
      {
         command.add("ca");
         command.add("-batch");
         command.add("-passin");
         command.add("pass:password");
         command.add("-config");
         command.add("openssl.my.cnf");
         command.add("-policy");
         command.add("policy_anything");
         command.add("-out");
         command.add(CRTDir + "/" + username + ".crt");
         command.add("-in");
         command.add(CSRDir + "/" + username + ".csr");
      } else // revoke
      {
         command.add("ca");
         command.add("-passin");
         command.add("pass:password");
         command.add("-config");
         command.add("openssl.my.cnf");
         command.add("-revoke");
         command.add(CRTDir + "/" + username + ".crt");
      }

      ProcessBuilder pb = new ProcessBuilder(command);
      pb.directory(new File(rootCADir));

      Process p = null;

      p = pb.start();
      p.waitFor();
      return p.exitValue();
   }

   private boolean deleteCertificate(String username) throws NullPointerException, IOException, InterruptedException {
      boolean retunvalue = true;
      File file = new File(rootCADir + "/" + CRTDir + "/" + username + ".crt");

      if (!file.exists()) {
         retunvalue = false;
      }

      if (!file.canWrite()) {
         retunvalue = false;
      }

      if (file.isDirectory()) {
         retunvalue = false;
      }

      // Attempt to delete it
      if (retunvalue) {
         retunvalue = file.delete();
      }

      return retunvalue;
   }
}
