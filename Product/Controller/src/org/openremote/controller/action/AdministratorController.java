/* OpenRemote, the Home of the Digital Home.
* Copyright 2008-2012, OpenRemote Inc.
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
package org.openremote.controller.action;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.SignatureException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import org.bouncycastle.asn1.pkcs.CertificationRequest;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.Certificate;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.asn1.x509.X509CertificateStructure;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
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
import org.openremote.controller.Constants;
import org.openremote.controller.service.ClientService;
import org.openremote.controller.spring.SpringContext;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.util.encoders.Base64;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/**
 * The controller for Administrator management.
 * 
 * @author <a href="mailto:melroy.van.den.berg@tass.nl">Melroy van den Berg</a> 2012
 */
public class AdministratorController extends MultiActionController {
      
   private static final ClientService clientService = (ClientService) SpringContext.getInstance().getBean("clientService");

   //private static final String rootCADir = ControllerConfiguration.readXML().getCaPath();
   private static final String rootCADir = "/usr/share/tomcat6/cert/ca";
   
   private static final String openssl = "openssl";
   private static final String mycaKey = "myca.key";
   private static final String CRTDir = "certs";
   private static final String CSRDir = "csr";
   private static final String PRIVATEDir = "private";
   
   private static final String privateKeyPassword = "password";

   /**
    * Request handler for accepting or denying an user
    * @param request HTTP servlet request
    * @param response HTTP response to the servlet
    */  
   public ModelAndView changeUserStatus(HttpServletRequest request, HttpServletResponse response) throws IOException,
         ServletRequestBindingException {
      String action = request.getParameter("action");      
      int clientID = Integer.parseInt(request.getParameter("client_id"));
      String pin = "";
      String clientUsername = ""; 

      try 
      {
         ResultSet resultSet = clientService.getClient(clientID);         
         while(resultSet.next())
         {
            String clientFileName = resultSet.getString("client_file_name");
            clientUsername = clientFileName.substring(0, clientFileName.lastIndexOf('.'));

            pin = resultSet.getString("client_pincode");
         }
         clientService.free();
      }
      catch (SQLException e) {
         logger.error(e.getMessage());
      }
            
      try 
      {   
         int result = -1;
         if(action.equals("accept")) // trust
         {
            //result = executeOpenSSLCommand(clientUsername, true);
            
            logger.error("Trying to accept certificate:");
            try {
               PKCS10CertificationRequest certificateRequest = this.getCertificationRequest(clientUsername);
               logger.error("Private Key:");
               PrivateKey caPrivateKey = getPrivateKey(rootCADir + "/" + PRIVATEDir + "/" + mycaKey);
               logger.error("CA: " + caPrivateKey.getFormat());
               logger.error("CA: " + caPrivateKey.getAlgorithm());
               logger.error("CA: " + caPrivateKey.getEncoded().toString());
               
               X509Certificate certificate = this.sign(certificateRequest, caPrivateKey);
               if(certificate != null && this.saveCertificate(certificate, rootCADir + "/" + CRTDir + "/" + clientUsername + ".crt"))
               {
                  result = 0;
               }       
               else
               {
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
         }
         else if(action.equals("deny")) // revoke
         {
            result = executeOpenSSLCommand(clientUsername, false);            
         }

         // OpenSSL Command successful
         if(result == 0)
         {
            // @TODO: Add the serial ID to the database and use index.txt
            // to get the right list in the administrator Panel.
            
            // If successfully revoked, than remove the certificate
            if(action.equals("deny"))
            {
               if(deleteCertificate(clientUsername))
               {
                  int statusReturn = clientService.updateClientStatus(clientID, false);
                  int serialReturn = clientService.clearClientSerial(clientID);
                  if(statusReturn == 1 && serialReturn == 1)
                  {
                     response.getWriter().print(Constants.OK + "-" + clientID + "-" + action + "-" + pin);                     
                  }
                  else
                  {
                     response.getWriter().print("Client is not successfully updated in the database");
                  }
               }
               else
               {
                  response.getWriter().print("Certificate is successfully revoked, but couldn't be removed.");
               }
            }
            else if(action.equals("accept"))
            {
               int statusReturn = clientService.updateClientStatus(clientID, true);
               int serialReturn = clientService.updateClientSerial(clientID);
               if(statusReturn == 1 & serialReturn == 1)
               {
                  response.getWriter().print(Constants.OK + "-" + clientID + "-" + action + "-" + clientService.getSerial());
               }
               else
               {
                  response.getWriter().print("Client is not successfully updated in the database");                  
               }               
            }
         }
         else
         {
            if(action.equals("deny"))
            {
               if(deleteCertificate(clientUsername))
               {
                  response.getWriter().print("OpenSSL command failed, exit with exit code: " + result + "" +
                  		"\n\rCertificate deleted.");
               }
               else
               {
                  response.getWriter().print("OpenSSL command failed, exit with exit code: " + result + ". " +
                  		"\n\rPlus the certificate couldn't be removed.");
               }
            }
            else
            {
               response.getWriter().print("OpenSSL command failed, exit with exit code: " + result + ". " +
               		"\n\rProbably database index.txt file problem from the CA, please check this file which is located in the CA path.");
            }
         }         
      } catch (NullPointerException e) {
         response.getWriter().print("nullpointer: " + e.getMessage());
      } catch (InterruptedException e) {
         response.getWriter().print("interrupt: " + e.getMessage());
      }
      return null;
   }
   
   private PKCS10CertificationRequest getCertificationRequest(String username) throws IOException
   {
      File file = new File(rootCADir + "/" + CSRDir + "/" + username + ".csr");
      String data = "";
      
      FileInputStream fis = new FileInputStream(file);
      data = convertStreamToString(fis);
      
      BASE64Decoder decoder = new BASE64Decoder();
      byte[] decodedBytes = decoder.decodeBuffer(data);

      return new PKCS10CertificationRequest(decodedBytes);
   }
   
   private String convertStreamToString(InputStream is) throws IOException
   {
      if (is != null)
      {
         Writer writer = new StringWriter();
   
         char[] buffer = new char[1024];
         try {
            Reader reader = new BufferedReader(
            new InputStreamReader(is, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) 
            {
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
   
   private PrivateKey getPrivateKey(String keyPath) throws IOException
   {
      BufferedReader br = new BufferedReader(new FileReader(keyPath));
      Security.addProvider(new BouncyCastleProvider());
      
      PasswordFinder passwordFinder = new PasswordFinder() {
         @Override
         public char[] getPassword() {
             return privateKeyPassword.toCharArray();
         }
     };      
      
      KeyPair kp = (KeyPair) new PEMReader(br, passwordFinder).readObject();
      
      return kp.getPrivate();
   }
   
   private boolean saveCertificate(X509Certificate certificate, String fileName)
   {
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
   
   private X509Certificate sign(PKCS10CertificationRequest inputCSR, PrivateKey caPrivate) //, KeyPair pair
         throws InvalidKeyException, NoSuchAlgorithmException,
         NoSuchProviderException, SignatureException, IOException,
         OperatorCreationException, CertificateException
         {   

     AlgorithmIdentifier sigAlgId = new DefaultSignatureAlgorithmIdentifierFinder()
             .find("SHA1withRSA");
     AlgorithmIdentifier digAlgId = new DefaultDigestAlgorithmIdentifierFinder()
             .find(sigAlgId);

     AsymmetricKeyParameter foo = PrivateKeyFactory.createKey(caPrivate
             .getEncoded());
     //SubjectPublicKeyInfo keyInfo = SubjectPublicKeyInfo.getInstance(pair
     //        .getPublic().getEncoded());

     X509v3CertificateBuilder myCertificateGenerator = new X509v3CertificateBuilder(
           new X500Name("CN=issuer"), 
           new BigInteger("1"), 
           new Date(System.currentTimeMillis()), 
           new Date(System.currentTimeMillis() + 30 * 365 * 24 * 60 * 60 * 1000), 
           inputCSR.getSubject(), 
           inputCSR.getSubjectPublicKeyInfo());
     ContentSigner sigGen = new BcRSAContentSignerBuilder(sigAlgId, digAlgId)
             .build(foo);        

     X509CertificateHolder holder = myCertificateGenerator.build(sigGen);
     /*
     Very old code:
     Certificate eeX509CertificateStructure =  holder.toASN1Structure();
     CertificateFactory cf = CertificateFactory.getInstance("X.509", "BC");

     // Read Certificate
     InputStream is1 = new ByteArrayInputStream(eeX509CertificateStructure.getEncoded());
     X509Certificate theCert = (X509Certificate) cf.generateCertificate(is1);
     is1.close();
     */
     
     X509Certificate cert = new
           JcaX509CertificateConverter().setProvider("BC").getCertificate(holder);
     return cert;
 }
   
   
   /**
    * Execute the OpenSSL command on command-line
    * @param username
    * @param accept true is for signing, false is to revoke a certificate
    * @return the exit code of the command executed
    * @throws NullPointerException
    * @throws IOException
    * @throws InterruptedException
    */
   private int executeOpenSSLCommand(String username, boolean accept) throws NullPointerException, IOException, InterruptedException
   {
      List<String> command = new ArrayList<String>();
      command.add(openssl); // command
      if(accept) // Signing
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
      }
      else // revoke
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
   

   private boolean deleteCertificate(String username) throws NullPointerException, IOException, InterruptedException
   {
      boolean retunvalue = true;
      File file = new File(rootCADir + "/" + CRTDir + "/" + username + ".crt");
      
      if (!file.exists())
      {
         retunvalue = false;
      }
       
      if (!file.canWrite())
      {
         retunvalue = false;
      }

      if (file.isDirectory()) 
      {
         retunvalue = false;
      }

      // Attempt to delete it
      if(retunvalue)
      {
         retunvalue = file.delete();
      }
      
      return retunvalue;
   }  
}
