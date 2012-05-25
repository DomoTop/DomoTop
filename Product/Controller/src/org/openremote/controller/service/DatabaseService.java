package org.openremote.controller.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

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
    * Execute SQL query via prepare statement to prevent SQL Injection
    * 
    * @param prepareStatement Prepare statement
    * @return ResultSet with the result of the query
    */
   ResultSet doSQL(PreparedStatement preparedStatement);
   /**
    * Create a PreparedStatement from a query
    * @return PreparedStatement
    */
   PreparedStatement createPrepareStatement(String query) throws SQLException;
   /**
    * Do a insert, update or delete SQL query into the database
    * 
    * @return 0 or -1 is unsuccessfully, 1 (or higher) is successfully 
    */
   int doUpdateSQL(String sql);
   /**
    * Do a insert, update or delete SQL query via prepare statement to prevent SQL Injection
    * 
    * @return 0 or -1 is unsuccessfully, 1 (or higher) is successfully 
    */
   int doUpdateSQL(PreparedStatement preparedStatement);
   /**
    * Reset the database configuration table to default settings
    * 
    * @return boolean true if success else false
    */
   boolean resetConfigurationTables();
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
