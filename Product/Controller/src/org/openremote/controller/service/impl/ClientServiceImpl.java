package org.openremote.controller.service.impl;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.apache.log4j.Logger;
import org.openremote.controller.Constants;
import org.openremote.controller.service.CertificateService;
import org.openremote.controller.service.ClientService;
import org.openremote.controller.service.ConfigurationService;
import org.openremote.controller.service.DatabaseService;

/**
 * Get client information out the (request) certificates
 * 
 * @author <a href="mailto:melroy.van.den.berg@tass.nl">Melroy van den Berg</a> 2012
 */

public class ClientServiceImpl implements ClientService 
{
   private final static Logger logger = Logger.getLogger(Constants.SERVICE_LOG_CATEGORY);
   private static final String KEYSTORE_PASSWORD = "password";

   private static final String CA_PATH = "ca_path";
   private static String checkClientQuery = "SELECT client_id FROM client WHERE client_dn = ? ";
   private static String selectClientQuery = "SELECT * FROM client WHERE client_id = ? ";
   private static String selectAllClientsQuery = "SELECT * FROM client ORDER BY client_creation_timestamp ASC";
   private static String insertClientQuery = "INSERT INTO client (client_serial, client_pincode, client_device_name, client_email, client_alias, client_active, client_creation_timestamp, client_modification_timestamp, client_role, client_dn, client_timestamp) VALUES ";
   private static String limitByOne = " LIMIT 1";

   private DatabaseService database;
   private ConfigurationService databaseConfiguration;
   private CertificateService certificateService;
   
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
   public int addClient(String alias, long timestamp)
   {      
      certificateService.parseCSRFile(alias);

      return this.addClient(certificateService.getPin(), certificateService.getDeviceName(), certificateService.getEmail(), alias, certificateService.getCN(), timestamp);
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
   public int addClient(String pin, String deviceName, String email, String alias, String cn, long timestamp) 
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
               preparedStatement = database.createPrepareStatement(insertClientQuery + "('', ?, ?, ?, ?, FALSE, NOW, NOW, '', ?, ?)");
               preparedStatement.setString(1, pin);
               preparedStatement.setString(2, deviceName);
               preparedStatement.setString(3, email);
               preparedStatement.setString(4, alias);
               preparedStatement.setString(5, cn);
               preparedStatement.setLong(6, timestamp);
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
    * Update the client's group
    * 
    * @param clientID id of the client
    * @param groupID id of the group
    * @return int value -1 or 0 is incorrect, 1 is action succeed
    */
   @Override
   public int updateClientGroup(int clientID, int groupID) {
      int resultValue = -1;
      PreparedStatement preparedStatement = null;     
      if (database != null) 
      {
         try
         {
            if(groupID == -1)
            {
               preparedStatement = database.createPrepareStatement("UPDATE client SET client_group_id = NULL WHERE client_id = ?");
               preparedStatement.setInt(1, clientID);               
            }
            else
            {
               preparedStatement = database.createPrepareStatement("UPDATE client SET client_group_id = ? WHERE client_id = ?");
               preparedStatement.setInt(1, groupID);
               preparedStatement.setInt(2, clientID);
            }
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
    * Reset all the groups of all the clients
    * 
    * @return int value -1 or 0 is incorrect, 1 or higher is correct
    */
   @Override   
   public int resetAllGroupClients()
   {
      int resultValue = -1; 
      if (database != null) 
      {
         resultValue = database.doUpdateSQL("UPDATE client SET client_group_id = NULL");
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
    * Get the client group name via DN
    * @return string group name
    */
   @Override
   public String getGroupName(String DN) {
      
      String returnValue = "";
      PreparedStatement preparedStatement = null;
      ResultSet resultSet = null;

      if (database != null)
      {
         try
         {            
            preparedStatement = database.createPrepareStatement("SELECT group_name FROM client JOIN client_group ON (client.client_group_id = client_group.group_id) WHERE client_dn = ?");
            preparedStatement.setString(1, DN);
            resultSet = database.doSQL(preparedStatement);
            
            if(resultSet != null) 
            {
               resultSet.next();
               returnValue = resultSet.getString("group_name");
            }
         } catch (SQLException e) {
            logger.error("SQL Exception: " + e.getMessage() + " (this error can be ignored if you didn't grant access)");
         }
      }
      return returnValue;
   }

   /**
    * Get timestamp from client via alias and pin
    * @param alias
    * @param pin
    * @return long timestamp
    */
   @Override
   public long getTimestamp(String pin, String deviceName)
   {      
      long returnValue = 0;
      PreparedStatement preparedStatement = null;
      ResultSet resultSet = null;

      if (database != null)
      {
         try
         {
            preparedStatement = database.createPrepareStatement("SELECT client_timestamp FROM PUBLIC.client WHERE client_pincode = ? AND client_device_name = ? LIMIT 1");
            preparedStatement.setString(1, pin);
            preparedStatement.setString(2, deviceName);
            resultSet = database.doSQL(preparedStatement);
            
            if(resultSet != null) 
            {
               resultSet.next();
               returnValue = resultSet.getLong("client_timestamp");
            }
         } catch (SQLException e) {
            logger.error("SQL Exception: " + e.getMessage() + " (this error can be ignored if you didn't grant access)");
         }
      }
      return returnValue;
   }   
   
   /**
    * Check if the client is valid based on a dname
    * @param dname the dynamic name
    * @return true if the client is valid
    */
   public boolean isClientValid(String dname) {
         boolean returnValue = false;
         PreparedStatement preparedStatement = null;
         
         if (database != null) 
         {
            try {
               preparedStatement = database.createPrepareStatement(checkClientQuery + limitByOne);
               preparedStatement.setString(1, dname);
               database.doSQL(preparedStatement);
               returnValue = database.getNumRows() == 1;
            } catch (SQLException e) {
               logger.error("SQL Exception: " + e.getMessage());
            }
         } else {
            logger.error("Database is not yet set (null)");
         }
         return returnValue;
   }

   /**
    * Check if the client's date is valid
    * @param date the date (not after date)
    * @return true if the client data is valid
    */  
   @Override
   public boolean isClientDateValid(Date date) {
      boolean returnValue = false;
      Date currentDate = new Date();
      
      if(currentDate.before(date)){
         returnValue = true;
      } else if(currentDate.equals(date)) {
         returnValue = true;
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
   
   /**
    * Sets the certificate configuration.
    * 
    * @param certificate
    *           service
    */
   public void setCertificateService(CertificateService certificateService) {
      this.certificateService = certificateService;
   }   
}
