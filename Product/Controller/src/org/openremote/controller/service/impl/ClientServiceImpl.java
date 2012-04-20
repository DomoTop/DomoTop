package org.openremote.controller.service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.security.auth.x500.X500Principal;

import org.apache.commons.codec.binary.Hex;
import org.apache.log4j.Logger;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.pkcs.Attribute;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.RSAPublicKeyStructure;
import org.bouncycastle.asn1.x509.X509Extension;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.x509.X509V2AttributeCertificate;
import org.openremote.controller.Constants;
import org.openremote.controller.service.ClientService;
import org.openremote.controller.service.ConfigurationService;
import org.openremote.controller.service.DatabaseService;
import org.openremote.controller.utils.AlgorithmUtil;

import sun.misc.BASE64Decoder;

/**
 * Get client information out the (request) certificates
 * 
 * @author <a href="mailto:melroy.van.den.berg@tass.nl">Melroy van den Berg</a> 2012
 */

public class ClientServiceImpl implements ClientService {

   static {
      Security.addProvider(new BouncyCastleProvider());
   }
   private final static Logger logger = Logger.getLogger(Constants.SERVICE_LOG_CATEGORY);
   private static final String CSRDir = "/ca/csr";
   private static final String KEYSTORE_PASSWORD = "password";

   private static final String CA_PATH = "ca_path";
   private static String selectClientQuery = "SELECT * FROM client WHERE client_id = ? ";
   private static String selectAllClientsQuery = "SELECT * FROM client ORDER BY client_creation_timestamp ASC";
   private static String insertClientQuery = "INSERT INTO client (client_serial, client_pincode, client_device_name, client_email, client_alias, client_active, client_creation_timestamp, client_modification_timestamp, client_role, client_dn) VALUES ";
   private static String limitByOne = " LIMIT 1";

   private DatabaseService database;
   private ConfigurationService databaseConfiguration;
   private String serial = "";
   private String pin;
   private String email;
   private String deviceName;
   private String cn;

   /**
    * Get all clients.
    * 
    * @return The result set from the database with all the information from every client
    */
   @Override
   public ResultSet getClients() {
      ResultSet returnValue = null;
      if (database != null) {
         returnValue = database.doSQL(selectAllClientsQuery);
      } else {
         logger.error("Database is not yet set (null)");
      }
      return returnValue;
   }

   /**
    * Add new client to the database.
    * 
    * @param alias clients alias
    * @return int 0 = error with select or insert, 1 insert query went successfully, 2 user already exists
    */
   @Override
   public int addClient(String alias) {
      this.parseCSRFile(alias);

      return this.addClient(this.getPin(), this.getDeviceName(), this.getEmail(), alias, this.getCN());
   }

   /**
    * Add new client to the database.
    * 
    * @param pinCode
    *           the client pin
    * @param deviceName
    *           the client device name
    * @param email
    *           the client e-mail address
    * @param alias
    *           the client alias
    * @return int 0 = error with select or insert, 1 insert query went successfully, 2 user already exists
    */
   @Override
   public int addClient(String pin, String deviceName, String email, String alias, String cn) 
   {
      int returnValue = 0;
      int resultValue = -1;
      int numRows = -1;

      if (database != null) 
      {
         PreparedStatement preparedStatement = null;

         try
         {            
            preparedStatement = database.createPrepareStatement("SELECT client_pincode, client_device_name FROM PUBLIC.client WHERE client_pincode = ? AND client_device_name = ? LIMIT 1");
            preparedStatement.setString(1, pin);
            preparedStatement.setString(2, deviceName);
            database.doSQL(preparedStatement);
            numRows = database.getNumRows();
         } catch (SQLException e) {
            logger.error("SQL Exception: " + e.getMessage());
         }

         // Check if client doesn't exist in the database
         if (numRows == 0) 
         {
            try
            {  
               preparedStatement = database.createPrepareStatement(insertClientQuery + "('', ?, ?, ?, ?, FALSE, NOW, NOW, '', ?)");
               preparedStatement.setString(1, pin);
               preparedStatement.setString(2, deviceName);
               preparedStatement.setString(3, email);
               preparedStatement.setString(4, alias);
               preparedStatement.setString(5, cn);
               resultValue = database.doUpdateSQL(preparedStatement);
            } catch (SQLException e) {
               logger.error("SQL Exception: " + e.getMessage());
            }
            
            if (resultValue >= 1) {
               returnValue = 1;
            }
         } else {
            // ignore a second user with the same device name and pin
            returnValue = 2;
         }
      } else {
         logger.error("Database is not yet set (null)");
      }
      return returnValue;
   }

   /**
    * Get one client result set from the database.
    * 
    * @param clientID
    *           id from the client
    * @return ResultSet the result from the database with client information
    */
   @Override
   public ResultSet getClient(int clientID) 
   {
      ResultSet returnValue = null;
      PreparedStatement preparedStatement = null;
      
      if (database != null) 
      {
         try {
            preparedStatement = database.createPrepareStatement(selectClientQuery + limitByOne);
            preparedStatement.setInt(1, clientID);
            returnValue = database.doSQL(preparedStatement);
         } catch (SQLException e) {
            logger.error("SQL Exception: " + e.getMessage());
         }
      } else {
         logger.error("Database is not yet set (null)");
      }
      return returnValue;
   }

   /**
    * Updates the client active boolean flag in the database.
    * 
    * @param clientID
    *           is the client id
    * @param active
    *           boolean true is active false is non-active
    * @return int value -1 or 0 is incorrect, 1 is action succeed
    */
   @Override
   public int updateClientStatus(int clientID, boolean active) {
      int resultValue = -1;
      PreparedStatement preparedStatement = null;
      String role = "";
      if (database != null) 
      {         
         try
         {
            if(active) {
               role = "openremote";
            }
            
            preparedStatement = database.createPrepareStatement("UPDATE client SET client_active = ?, client_role = ? WHERE client_id = ?");
            preparedStatement.setBoolean(1, active);
            preparedStatement.setString(2, role);
            preparedStatement.setInt(3, clientID);
            resultValue = database.doUpdateSQL(preparedStatement);
         } catch (SQLException e) {
            logger.error("SQL Exception: " + e.getMessage());
         }
      }
      return resultValue;
   }
   
   /**
    * Deletes a client from the database
    * @param clientID The id of the client you want to remove
    * @return int value -1 or 0 is incorrect, 1 is action succeed
    */
   public int removeClient(int clientID) {
      int resultValue = -1;
      PreparedStatement preparedStatement = null;
      if(database != null) 
      {
         try
         {
            preparedStatement = database.createPrepareStatement("DELETE FROM client WHERE client_id = ?");
            preparedStatement.setInt(1, clientID);
            resultValue = database.doUpdateSQL(preparedStatement);
         } catch (SQLException e) {
            logger.error("SQL Exception: " + e.getMessage());
         }
      }
      
      return resultValue;
   }

   /**
    * Update client serial number.
    * 
    * @param clientID
    *           id client
    * @return value -1 or 0 is , 1 is correct
    */
   @Override
   public int updateClientSerial(int clientID, String serial)
   {
      int resultValue = -1;
      PreparedStatement preparedStatement = null;
      
      if (database != null && !serial.isEmpty()) 
      {
         try
         {
            preparedStatement = database.createPrepareStatement("UPDATE client SET client_serial = ? WHERE client_id = ?");
            preparedStatement.setString(1,  serial);
            preparedStatement.setInt(2, clientID);
            resultValue = database.doUpdateSQL(preparedStatement);
         } catch (SQLException e) {
            logger.error("SQL Exception: " + e.getMessage());
         }
      }
      return resultValue;
   }  
   
   /**
    * Write a empty string to the client serial in the database
    * 
    * @param clientID client ID
    * @return value -1 or 0 is , 1 is correct
    */
   @Override
   public int clearClientSerial(int clientID)
   {
      int resultValue = -1;
      PreparedStatement preparedStatement = null;
      
      if (database != null) 
      {
         try
         {
            preparedStatement = database.createPrepareStatement("UPDATE client SET client_serial = '' WHERE client_id =  ?");
            preparedStatement.setInt(1, clientID);
            resultValue = database.doUpdateSQL(preparedStatement);
         } catch (SQLException e) {
            logger.error("SQL Exception: " + e.getMessage());
         }
      }
      return resultValue;
   }
   
   /**
    * Drop/remove all clients from the database
    * 
    * @return value -1 or 0 is incorrect, 1 is correct
    */
   @Override
   public int dropClients()
   {
      int resultValue = -1;

      if (database != null) {
         resultValue = database.doUpdateSQL("TRUNCATE TABLE client");
      }
      return resultValue;
   }
   
   /**
    * Get Client serial
    * @return String serial
    */
   @Override
   public String getSerial() {
      return serial;
   }
   
   /**
    * Get the X509 Certificate from the client key store file via username alias
    * @param alias is the certificate alias name
    */
   @Override
   public X509Certificate getClientCertificate(String alias)
   {
      KeyStore clientKS;
      X509Certificate certificate = null;
      String rootCADir = databaseConfiguration.getItem(CA_PATH);
      String client_key_store = rootCADir + "/client_certificates.jks";
      
      try
      {
         clientKS = KeyStore.getInstance("JKS");

         // Load the key store to memory.
         FileInputStream fis = new FileInputStream(client_key_store);  
         clientKS.load(fis, KEYSTORE_PASSWORD.toCharArray()); 
         
         certificate = (X509Certificate) clientKS.getCertificate(alias);
      } catch (NoSuchAlgorithmException e) {
         logger.error("Client certificate: " + e.getMessage());
      } catch (CertificateException e) {
         logger.error("Client certificate: " + e.getMessage());
      } catch (IOException e) {
         logger.error("Client certificate: " + e.getMessage());
      } catch (KeyStoreException e) {
         logger.error("Client certificate: " + e.getMessage());
      }
      return certificate;
   }   
   
   /**
    * Returns the number of clients. Note: You should use getClients() first and directly a getNumClients()
    * 
    * @see #getClients()
    * 
    * @return int of the number of clients
    */
   @Override
   public int getNumClients() {
      int newNum = -1;
      if (database != null) {
         newNum = database.getNumRows();
      }
      return newNum;
   }
   
   /**
    * Check if the client ID which is provided is valid
    * @param clientID
    * @return true if the client id is valid else false
    */
   @Override
   public boolean isClientIDValid(int clientID)
   {
      boolean returnValue = false;
      PreparedStatement preparedStatement = null;
      if (database != null) 
      {
         try {
            preparedStatement = database.createPrepareStatement(selectClientQuery);
            preparedStatement.setInt(1, clientID);
            database.doSQL(preparedStatement);
         } catch (SQLException e) {
            logger.error("SQL Exception: " + e.getMessage());
         }
         
         if(this.getNumClients() == 1)
         {
            returnValue = true;
         }
      } else {
         logger.error("Database is not yet set (null)");
      }
      return returnValue;
   }
   
   /**
    * Close the result set.
    */
   @Override
   public void free() {
      database.free();
   }

   @SuppressWarnings("deprecation")
   private void parseCSRFile(String alias)
   {
      // init
      pin = "";
      email = "";
      deviceName = "";
      cn = "";
      PKCS10CertificationRequest certificationRequest = null;
      try {
         certificationRequest = this.getCertificationRequest(alias);
      } catch (IOException e) {
         logger.error("Parse CSR error: " + e.getMessage());
      }

      if(certificationRequest != null)
      {      
         // Get pin
         try {
            ASN1Sequence seqkey = ASN1Sequence.getInstance(certificationRequest.getSubjectPublicKeyInfo().getPublicKey());
            RSAPublicKeyStructure publicKey = new RSAPublicKeyStructure(seqkey);
            
            pin = AlgorithmUtil.generateMD5Sum(publicKey.getModulus().toByteArray());
            pin = pin.substring(pin.length() - 4);
         } catch (IOException e) {
            logger.error("Can't get public key.");
         }
         
         
         Attribute[] attributes = certificationRequest.getAttributes(PKCSObjectIdentifiers.pkcs_9_at_extensionRequest);
         if(attributes != null && attributes.length >= 1)
         {
            
            ASN1Set attributeSet = attributes[0].getAttrValues();
            for (int i = 0; i != attributeSet.size(); i++)
            {
               ASN1Encodable object = attributeSet.getObjectAt(i);
               X509Extensions extensions = X509Extensions.getInstance(object);
               
               X509Extension ext = extensions.getExtension(X509Extension.subjectAlternativeName);
              
               email = new String(ext.getValue().getOctets()).substring(4);               
            }
            
            //get cn
            X500Name cna = certificationRequest.getSubject();
            cn = cna.toString();
            X500Principal prin = null;
            try {
               prin = new X500Principal(cna.getEncoded());
               cn = prin.getName(X500Principal.RFC1779);
            } catch (IOException e) {
               logger.error(e.getMessage());
               logger.debug(e.getStackTrace());
            }
                        
            // Get device name
            deviceName = certificationRequest.getSubject().toString();
            deviceName = deviceName.substring(deviceName.indexOf("CN=") + 3);
          
         }
         else
         {
            logger.error("Certification request couldn't be decoded.");
         }
      }

   }

   private PKCS10CertificationRequest getCertificationRequest(String alias) throws IOException {
      String rootCADir = databaseConfiguration.getItem(CA_PATH);
      File file = new File(rootCADir + CSRDir + "/" + alias + ".csr");
      String data = "";

      FileInputStream fis = new FileInputStream(file);
      data = convertStreamToString(fis);

      BASE64Decoder decoder = new BASE64Decoder();
      byte[] decodedBytes = decoder.decodeBuffer(data);

      return new PKCS10CertificationRequest(decodedBytes);
   }

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

   private String getPin() {
      return pin;
   }

   private String getEmail() {
      return email;
   }

   private String getDeviceName() {
      return deviceName;
   }

   private String getCN() {
      return cn;
   }
   
   /**
    * Sets the database.
    * 
    * @param database
    *           service
    */

   public void setDatabase(DatabaseService database) {
      this.database = database;
   }

   /**
    * Sets the database configuration.
    * 
    * @param configuration
    *           service
    */

   public void setDatabaseConfiguration(ConfigurationService databaseConfiguration) {
      this.databaseConfiguration = databaseConfiguration;
   }
}
