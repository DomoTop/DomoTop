package org.openremote.controller.service.impl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.apache.log4j.Logger;
import org.openremote.controller.Constants;
import org.openremote.controller.ControllerConfiguration;
import org.openremote.controller.service.DatabaseService;
import org.openremote.controller.spring.SpringContext;

import freemarker.template.Configuration;

/**
 * Database service for creating connection, do sql statements, 
 *  get number of rows, closing the connection(s) and more...
 * 
 * @author <a href="mailto:melroy.van.den.berg@tass.nl">Melroy van den Berg</a> 2012
 */

public class DatabaseServiceImpl implements DatabaseService 
{  
   private final static Logger logger = Logger.getLogger(Constants.REST_ALL_PANELS_LOG_CATEGORY);

   private ControllerConfiguration configuration;
   private String databasePath = "";
   private static Connection connection;
   Statement statement = null;
   ResultSet resultSet = null;

   private void createTables() throws SQLException
   {
      connection.prepareStatement("drop table client;")
                  .execute();
      connection.prepareStatement(
                  "create table client ( client_id BIGINT NOT NULL, "+
                  "client_serial INTEGER(5) NOT NULL, "+
                  "client_pincode INTEGER(4,0), "+
                  "client_device_name VARCHAR, "+
                  "client_email VARCHAR, "+
                  "client_file_name VARCHAR, "+ 
                  "client_active BOOLEAN, "+ //NOT NULL
                  "client_creation_date DATE, "+ //NOT NULL
                  "client_group_id BIGINT, "+
                  "name VARCHAR);")
                  .execute();      
      connection.prepareStatement(
          "insert into client(client_id, client_pincode) "+
          "values (1, 234);")
          .execute();
   }
   
   public boolean databaseInit()
   { 
      boolean returnValue = true;
      
      // init database path
      configuration = ControllerConfiguration.readXML();
      
      if(configuration != null)
      {
         databasePath = configuration.getResourcePath() + "/database/openremote";
      }
      else
      {
         returnValue = false;
         logger.error("Controller configuration is null");
      }
            
      try {
         Class.forName("org.hsqldb.jdbcDriver");
         connection = DriverManager.getConnection("jdbc:hsqldb:" + databasePath, "sa", "");
         
         this.createTables();
         
         statement = connection.createStatement();
      } catch (SQLException e) {
         returnValue = false;
         logger.error("SQL Exception:" + e.getMessage());
      } catch (ClassNotFoundException e) {
         returnValue = false;
         logger.error("SQL Exception Class not Found: " + e.getMessage());
      }
      return returnValue;
   }

   @Override
   public ResultSet doSQL(String sql) {  
      if(statement == null)
      {
         if(!this.databaseInit())
         {
            logger.error("Database connection was unsuccessfully.");
         }
      }
      
      try 
      {
         if(statement != null)
         {
            resultSet = statement.executeQuery(sql);
         }
      } catch (SQLException e) {
         logger.error("SQL Exception: " + e.getMessage());
      }
      return null;
   }

   @Override
   public int getNumRows() {   
      int numRows = 0;
      try {
         numRows = resultSet.getRow();
      } catch (SQLException e) {
         logger.error("SQL Exception: " + e.getMessage());
      }
      return numRows;
   }

   @Override
   public int getInsertID()
   {
      int newID = -1;

      try 
      {
         if (resultSet != null && resultSet.next()) 
         {
            newID = resultSet.getInt(1);   
         }
      } catch (SQLException e) {
         logger.error("SQL Exception: " + e.getMessage());
      } 
      return newID;
   }

   @Override
   public void free() {
      try {
         if(resultSet != null)
         {
            resultSet.close();
         }
         
         if(statement != null)
         {
            statement.close();
         }
      } catch (SQLException e) {
         logger.error("SQL Exception: " + e.getMessage());
      }
   }

   @Override
   public void close() {
      try {
         if(connection != null)
         {
            connection.close();
         }
      } catch (SQLException e) {
         logger.error("SQL Exception: " + e.getMessage());
      }
   }
   
   /**
    * Sets the configuration.
    * 
    * @param configuration the new configuration
    */   
   public void setConfiguration(ControllerConfiguration configuration)
   {
      this.configuration = configuration;
   }   
}
