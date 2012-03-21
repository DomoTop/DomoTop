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
   Statement statement;
   ResultSet resultSet;

   public void initDatabase()
   {
      // init database path
      configuration = ControllerConfiguration.readXML();
      
      if(configuration != null)
      {
         databasePath = configuration.getResourcePath() + "/database/openremote";
      }
            
      try {
         Class.forName("org.hsqldb.jdbcDriver");
         connection = DriverManager.getConnection("jdbc:hsqldb:" + databasePath, "sa", "");
         
         statement = connection.createStatement();
      } catch (SQLException e) {
         logger.error(e.getMessage());
      } catch (ClassNotFoundException e) {
         logger.error(e.getMessage());
      }
      
      resultSet = null;   
   }

   @Override
   public ResultSet doSQL(String sql) {    
      try {
         resultSet = statement.executeQuery(sql);
      } catch (SQLException e) {
         logger.error(e.getMessage());
      }
      return null;
   }

   @Override
   public int getNumRows() {   
      int numRows = 0;
      try {
         numRows = resultSet.getRow();
      } catch (SQLException e) {
         logger.error(e.getMessage());
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
         logger.error(e.getMessage());
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
         logger.error(e.getMessage());
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
         logger.error(e.getMessage());
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
