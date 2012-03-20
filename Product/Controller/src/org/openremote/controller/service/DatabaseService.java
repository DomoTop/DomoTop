package org.openremote.controller.service;

import java.sql.ResultSet;
import java.util.List;

import org.openremote.controller.model.Client;

/**
 * The interface DatabaseService
 * @author <a href="mailto:melroy.van.den.berg@tass.nl">Melroy van den Berg</a> 2012
 */

public interface DatabaseService {
   
   void initDatabase();
   ResultSet doSQL(String sql);
   int getNumRows();
   int getInsertID();
   void free();
   void close();   
}
