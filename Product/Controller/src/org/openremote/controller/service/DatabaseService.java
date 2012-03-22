package org.openremote.controller.service;

import java.sql.ResultSet;

/**
 * The interface DatabaseService
 * @author <a href="mailto:melroy.van.den.berg@tass.nl">Melroy van den Berg</a> 2012
 */

public interface DatabaseService {
   
   ResultSet doSQL(String sql);
   int getNumRows();   
   int getInsertID();
   void free();
   void close();
   int doUpdateSQL(String sql);
}
