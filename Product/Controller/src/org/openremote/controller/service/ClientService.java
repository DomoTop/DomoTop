package org.openremote.controller.service;

import java.security.cert.X509Certificate;
import java.sql.ResultSet;

/**
 * @author <a href="mailto:melroy.van.den.berg@tass.nl">Melroy van den Berg</a> 2012
 */

public interface ClientService 
{   
   ResultSet getClients();
   int getNumClients();
   int addClient(String pinCode, String deviceName, String email, String fileName, String cn);
   ResultSet getClient(int clientID);
   int updateClientStatus(int clientID, boolean active);
   int updateClientSerial(int clientID, String serial);
   String getSerial();
   X509Certificate getClientCertificate(String alias);
   void free();
   int clearClientSerial(int clientID);
   int addClient(String csrFileName);
   void initCaPath();
   int removeClient(int clientID);
}
