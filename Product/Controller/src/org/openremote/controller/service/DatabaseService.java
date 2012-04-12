package org.openremote.controller.service;

import java.sql.ResultSet;

/**
 * The interface DatabaseService
 * @author <a href="mailto:melroy.van.den.berg@tass.nl">Melroy van den Berg</a> 2012
 */

public interface DatabaseService 
{
   /**
    * Database initialization
    * 
    * @return true is success else false
    */
   boolean databaseInit();
   /**
    * Execute SQL query into the database
    * 
    * @return ResultSet with the result of the query
    */
   ResultSet doSQL(String sql);

   /**
    * Do a insert, update or delete SQL query into the database
    * 
    * @return 0 or -1 is unsuccessfully, 1 (or higher) is successfully 
    */
   int doUpdateSQL(String sql);
   /**
    * Get the number of rows of the result set (after a doSQL)
    * 
    * @see doSQL(String sql)
    * @return the number of the rows
    */
   int getNumRows(); 
   /**
    * Get the last inserted ID (IDENTITY) of the last database query
    * 
    * @return the last inserted id
    */
   int getInsertID();
   /**
    * Free the database result set, so it closes the result set and frees the resources
    */
   void free();
   /**
    * Close the database and shutdown the HSQLDB server, only necessary at stopping the application
    */
   void close();
}
