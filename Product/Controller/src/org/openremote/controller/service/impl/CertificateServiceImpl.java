package org.openremote.controller.service.impl;

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
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;
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
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.bc.BcRSAContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.openremote.controller.Constants;
import org.openremote.controller.ControllerConfiguration;
import org.openremote.controller.service.CertificateService;
import org.openremote.controller.service.ClientService;
import org.openremote.controller.service.ConfigurationService;

import sun.misc.BASE64Decoder;

/**
 * Certificate Service implementation, provide CA and other key store functions
 * 
 * @author <a href="mailto:melroy.van.den.berg@tass.nl">Melroy van den Berg</a> 2012
 */

public class CertificateServiceImpl implements CertificateService
{
   private static final String CA_PATH = "ca_path";
   private static final String CA_ALIAS = "ca.alias";
   private final static Logger logger = Logger.getLogger(Constants.SERVICE_LOG_CATEGORY);
   private static final X500Name CA_NAME = new X500Name("C=NL,O=TASS,OU=Software Developer,CN=CA_MelroyvdBerg");
   private static final String KEYSTORE_PASSWORD = "password";
   private static final int NUM_ALLOWED_INTERMEDIATE_CAS = 0;
   
   private ClientService clientService;
   private ConfigurationService configurationService;
   private ControllerConfiguration controllerConfiguration;
   
   /**
    * Initialize CA Path, only if the CA Path is empty 
    * then get the CA Path from the XML configuration and save it in the database
    */
   @Override
   public void initCaPath() 
   {   
      if(configurationService.getItem(CA_PATH).isEmpty())
      {
         configurationService.updateItem(CA_PATH, controllerConfiguration.getCaPath());
      }      
   }
   
   /**
    * Check if the CA certificate already exists in the server key store
    * @return true if exists else false
    */
   @Override
   public boolean ifCaExists()
   {      
      String rootCaPath = configurationService.getItem(CA_PATH);
      String keyStorePath = rootCaPath + "/server.jks";
      
      return existsAliasInKeyStore(keyStorePath, CA_ALIAS);
   }
   
   /**
    * Create a new CA, first dropping all clients from the database, create a new key pair
    * build a new certificate and finally store it in the key store
    * (if the server key store does not exists yet it will be created)
    * 
    * @return true when successfully created else false
    */
   @Override
   public boolean createCa()
   {      
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
              
         String rootCaPath = configurationService.getItem(CA_PATH);
         String keyStorePath = rootCaPath + "/server.jks";
         
         if(cert != null && KPair != null)
         {
            if(!this.keyStoreExists(keyStorePath))
            {
               if(!this.createKeyStore(keyStorePath))
               {
                  logger.error("Failed to create CA keystore.");
                  success = false;
               }
            }
            if(success)
            {
               success = this.saveToKeyStore(KPair, cert, keyStorePath, CA_ALIAS);
            }
         }
         else
         {
            logger.error("No CA certificate generated or no key pair generated.");
         }
      }
      
      if(success)
      {
         logger.error("CA Successfully created.");
      }
      
      return success;         
   }
   
   /**
    * Create a new client key store only when there is not yet a client key store
    * @return true when key store is successfully created
    * or true when key store already exists else false
    */
   @Override
   public boolean createClientKeyStore()
   {
      boolean success = false;
      String rootCaPath = configurationService.getItem(CA_PATH);
      String clientKeyStorePath =  rootCaPath + "/client_certificates.jks";
      
      if(!this.keyStoreExists(clientKeyStorePath))
      {
         if(createKeyStore(clientKeyStorePath))
         {
            success = true;
         }
      }
      else
      {
         success = true;
      }
      return success;
   }
   
   /**
    * Create a key pair (public & private key) using the RSA algorithm 
    * 
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
    * Check if the file exists
    * 
    * @param keyStoreFile path to the key store
    * @return true if exists else false
    */
   private boolean keyStoreExists(String keyStoreFile)
   {
      File f = new File(keyStoreFile);
      return (f.exists() ? true : false);
   }
   
   /**
    * Create a key store from scratch
    * 
    * @param keyStoreFile path to the key store
    * @return true if keystore was successfully created
    */
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
    * Add the certificate to the server's key store file
    * 
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
    * 
    * @param cert the X509 certificate
    * @param alias the alias of the certificate
    * @return true if success, false if unsuccessfully
    */
   @Override
   public boolean saveCertificateToClientKeyStore(X509Certificate cert, String alias) 
   {
      boolean success = false;
      KeyStore clientKS;
      
      String rootCaPath = configurationService.getItem(CA_PATH);
      String clientKeyStorePath =  rootCaPath + "/client_certificates.jks";
      
      try
      {
         clientKS = KeyStore.getInstance("JKS");

         // Load the key store to memory.
         FileInputStream fis = new FileInputStream(clientKeyStorePath);  
         clientKS.load(fis, KEYSTORE_PASSWORD.toCharArray());  
       
         // Import the certificate to the key store
         clientKS.setCertificateEntry(alias, cert);
         
         // Write the key store back to disk                 
         clientKS.store(new FileOutputStream(clientKeyStorePath), KEYSTORE_PASSWORD.toCharArray());      
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
    * Delete the client certificate from the client key store file
    * 
    * @param alias of the client certificate that will be removed
    * @return true if successfully removed from the key store else false
    */
   @Override
   public boolean deleteClientFromClientKeyStore(String alias)
   {
      boolean success = false;
      KeyStore clientKS;
      
      String rootCaPath = configurationService.getItem(CA_PATH);
      String clientKeyStorePath =  rootCaPath + "/client_certificates.jks";
      
      try
      {
         clientKS = KeyStore.getInstance("JKS");

         // Load the key store to memory.
         FileInputStream fis = new FileInputStream(clientKeyStorePath);  
         clientKS.load(fis, KEYSTORE_PASSWORD.toCharArray());  
       
         // Import the certificate to the key store
         clientKS.deleteEntry(alias);
         
         // Write the key store back to disk                 
         clientKS.store(new FileOutputStream(clientKeyStorePath), KEYSTORE_PASSWORD.toCharArray());      
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
    * Get the certificate file from the csr directory and create and returns a PKCS10CertificationRequest object
    * 
    * @param alias client alias
    * @return PKCS10CertificationRequest object
    * @throws IOException
    */
   @Override
   public PKCS10CertificationRequest getCertificationRequest(String alias) throws IOException
   {
      String rootCaPath = configurationService.getItem(CA_PATH);
      String csrPath = rootCaPath + "/ca/csr/";
      
      File file = new File(csrPath + alias + ".csr");
      String data = "";

      FileInputStream fis = new FileInputStream(file);
      data = convertStreamToString(fis);

      BASE64Decoder decoder = new BASE64Decoder();
      byte[] decodedBytes = decoder.decodeBuffer(data);

      return new PKCS10CertificationRequest(decodedBytes);
   }

   
   /**
    * Get the private key from the CA, using the server key store file   
    * 
    * @return PrivateKey Object
    */
   @Override
   public PrivateKey getCaPrivateKey()
   {
      PrivateKey privateKey = null;
      KeyStore privateKS;
      try
      {
         privateKS = KeyStore.getInstance("JKS");
         
         String rootCaPath = configurationService.getItem(CA_PATH);
         String keyStorePath = rootCaPath + "/server.jks";
         
         FileInputStream fis = new FileInputStream(keyStorePath);  
         privateKS.load(fis, KEYSTORE_PASSWORD.toCharArray());  
         Key key = privateKS.getKey(CA_ALIAS, KEYSTORE_PASSWORD.toCharArray());
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
   @Override
   public X509Certificate signCertificate(PKCS10CertificationRequest inputCSR, PrivateKey caPrivate, String serial)
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
   
   /**
    * Build a new X509 certificate, using the key pair earlier created and X500Name
    * 
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
    * Check if the alias exists in the key store
    * 
    * @param the alias of the certificate
    * @return true if exists else false
    */
   private boolean existsAliasInKeyStore(String keyStoreFile, String alias) 
   {
      boolean success = false;
      KeyStore privateKS;
      Certificate certificate = null;
      try
      {
         privateKS = KeyStore.getInstance("JKS");

         // Load the key store to memory.
         FileInputStream fis = new FileInputStream(keyStoreFile);  
         privateKS.load(fis, KEYSTORE_PASSWORD.toCharArray());  
       
         // Get certificate from key store         
         certificate = privateKS.getCertificate(alias);    
         if(certificate != null)
         {
            success = true;
         }
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
    * Convert a input stream to a String
    * 
    * @param is InputStream
    * @return String
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
    * Sets the Controller configuration.
    * 
    * @param Controller configuration
    *           the new configuration
    */
   public void setControllerConfiguration(ControllerConfiguration controllerConfiguration) {
      this.controllerConfiguration = controllerConfiguration;
   }
   
   /**
    * Sets client service.
    * 
    * @param Client
    *           service
    */
   public void setClientService(ClientService clientService) {
      this.clientService = clientService;
   }

   /**
    * Sets the configuration service.
    * 
    * @param configuration
    *           service
    */
   public void setConfigurationService(ConfigurationService configurationService) {
      this.configurationService = configurationService;
   }
}
