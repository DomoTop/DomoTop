package org.openremote.controller.service;

import java.security.Principal;
import java.security.cert.X509Certificate;
import java.sql.ResultSet;
import java.util.Date;

/**
 * @author <a href="mailto:melroy.van.den.berg@tass.nl">Melroy van den Berg</a> 2012
 */

public interface ClientService 
{   
   /**
    * Get all clients.
    * 
    * @return The result set from the database with all the information from every client
    */
   ResultSet getClients();
   /**
    * Add new client to the database.
    * 
    * @param alias clients alias
    * @param timestamp
    *           timestamp of the csr
    * @return int 0 = error with select or insert, 1 insert query went successfully, 2 user already exists
    */
   int addClient(String alias, long timestamp);
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
    * @param timestamp
    *           timestamp of the csr
    * @return int 0 = error with select or insert, 1 insert query went successfully, 2 user already exists
    */
   int addClient(String pinCode, String deviceName, String email, String fileName, String cn, long timestamp);
   /**
    * Get one client result set from the database.
    * 
    * @param clientID
    *           id from the client
    * @return ResultSet the result from the database with client information
    */
   ResultSet getClient(int clientID);
   /**
    * Updates the client active boolean flag in the database.
    * 
    * @param clientID
    *           is the client id
    * @param active
    *           boolean true is active false is non-active
    * @return int value -1 or 0 is incorrect, 1 is action succeed
    */
   int updateClientStatus(int clientID, boolean active);
   /**
    * Update client serial number.
    * 
    * @param clientID
    *           id client
    * @return value -1 or 0 is , 1 is correct
    */
   int updateClientSerial(int clientID, String serial);
   /**
    * Update the client's group
    * 
    * @param clientID id of the client
    * @param groupID id of the group
    * @return int value -1 or 0 is incorrect, 1 is action succeed
    */
   int updateClientGroup(int clientID, int groupID);
   /**
    * Reset all the groups of all the clients
    * 
    * @return int value -1 or 0 is incorrect, 1 or higher is correct
    */
   int resetAllGroupClients();
   /**
    * Write a empty string to the client serial in the database
    * 
    * @param clientID client ID
    * @return value -1 or 0 is , 1 is correct
    */
   int clearClientSerial(int clientID); 
   /**
    * Drop/remove all clients from the database
    * 
    * @return value -1 or 0 is incorrect, 1 is correct
    */
   int dropClients();
   /**
    * Deletes a client from the database
    * @param clientID The id of the client you want to remove
    * @return int value -1 or 0 is incorrect, 1 is action succeed
    */
   int removeClient(int clientID);      
   /**
    * Get the X509 Certificate from the client key store file via username alias
    * @param alias is the certificate alias name
    */
   X509Certificate getClientCertificate(String alias);
   /**
    * Returns the number of clients. Note: You should use getClients() first and directly a getNumClients()
    * 
    * @see #getClients()
    * 
    * @return int of the number of clients
    */
   int getNumClients();
   /**
    * Check if the client ID which is provided is valid
    * @param clientID
    * @return true if the client id is valid else false
    */
   boolean isClientIDValid(int clientID);
   /**
    * Check if the client is valid based on a dname
    * @param dname the dynamic name
    * @return true if the client is valid
    */
   boolean isClientValid(String dname);
   /**
    * Check if the client's date is valid
    * @param date the date (not after date)
    * @return true if the client data is valid
    */  
   boolean isClientDateValid(Date date);   
   /**
    * Get the client group name via DN
    * @return string group name
    */   
   String getGroupName(String DN);   
   /**
    * Get timestamp from client via alias and pin
    * @param alias
    * @param pin
    * @return long timestamp
    */
   long getTimestamp(String pin, String deviceName);
   /**
    * Close the result set.
    */
   void free();   
}
