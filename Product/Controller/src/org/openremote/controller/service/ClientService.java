package org.openremote.controller.service;

import java.sql.ResultSet;

/**
 * @author <a href="mailto:melroy.van.den.berg@tass.nl">Melroy van den Berg</a> 2012
 */

public interface ClientService 
{   
   ResultSet getClients();
   int getNumClients();
   int addClient(String pinCode, String deviceName, String email, String fileName);
}
