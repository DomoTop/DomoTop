package org.openremote.controller.service.impl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
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

   private boolean initDatabasePath()
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
      
      return returnValue;
   }
   
   private boolean setupConnection()
   {
      boolean returnValue = true;
      try {
         Class.forName("org.hsqldb.jdbcDriver");
         connection = DriverManager.getConnection("jdbc:hsqldb:" + databasePath, "SA", "");         
      } catch (SQLException e) {
         returnValue = false;
         logger.error("SQL Exception: " + e.getMessage());
      } catch (ClassNotFoundException e) {
         returnValue = false;
         logger.error("SQL Exception Class not Found: " + e.getMessage());
      }
      return returnValue;
   }
   
   private void createTables()
   {
      try
      {
         connection.prepareStatement("SET SCHEMA PUBLIC").execute();
         connection.prepareStatement("SET DATABASE DEFAULT INITIAL SCHEMA PUBLIC").execute();
         
         connection.prepareStatement("CREATE TABLE PUBLIC.client "+
                     "(client_id INTEGER GENERATED BY DEFAULT AS IDENTITY (START WITH 0, INCREMENT BY 1) NOT NULL, "+
                     "client_serial VARCHAR(25) NOT NULL, "+
                     "client_pincode VARCHAR(4) NOT NULL, "+
                     "client_device_name VARCHAR(150), "+
                     "client_email VARCHAR(250), "+
                     "client_alias VARCHAR(200), "+
                     "client_active BOOLEAN DEFAULT FALSE NOT NULL, "+
                     "client_creation_timestamp TIMESTAMP DEFAULT NOW,  "+
                     "client_modification_timestamp TIMESTAMP DEFAULT NOW,  "+
                     "client_group_id BIGINT DEFAULT '0' NOT NULL, "+
                     "PRIMARY KEY (client_id), "+
                     "UNIQUE (client_alias))")
                     .execute();

         connection.prepareStatement("CREATE TABLE PUBLIC.configuration "+
                     "(configuration_id INTEGER GENERATED BY DEFAULT AS IDENTITY (START WITH 0, INCREMENT BY 1) NOT NULL, "+
                     "configuration_name VARCHAR(100) NOT NULL, "+
                     "configuration_value VARCHAR(250) NOT NULL, "+
                     "PRIMARY KEY (configuration_id), "+
                     "UNIQUE (configuration_name))")
                     .execute();

      } catch (SQLException e) {
         // ignore exceptions, because table creations can be done multiple times
         logger.error("SQL exception table creation: " + e.getMessage());
      }
   }
   
   private boolean createStatement()
   {
      boolean returnValue = true;
      try {
         statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, 
                                          ResultSet.CONCUR_UPDATABLE);
      } catch (SQLException e) {
         returnValue = false;
         logger.error("SQL Exception: " + e.getMessage());
      }
      return returnValue;
   }

   
   public boolean databaseInit()
   { 
      boolean returnValue = true;
      
      returnValue = this.initDatabasePath();
      returnValue = this.setupConnection();
      
      this.createTables();

      returnValue = this.createStatement();

      return returnValue;
   }
   
   private void checkFirstTime()
   {
      if(statement == null) // first time?
      {
         // Initialize the database by creating the tables
         if(this.databaseInit())
         {
            logger.info("Database tables successfully created.");
         }
         else
         {
            logger.error("Database connection was unsuccessfully.");
         }
      }
   }   

   @Override
   public ResultSet doSQL(String sql)
   {      
      this.checkFirstTime();
      
      try 
      {
         if(statement != null)
         {
            resultSet = statement.executeQuery(sql);
         }
      } catch (SQLException e) {
         logger.error("SQL Exception: " + e.getMessage());
      }
      return resultSet;
   }

   @Override
   public int doUpdateSQL(String sql)
   {
      int result = -1;
      try 
      {
         if(statement != null)
         {
            result = statement.executeUpdate(sql);
         }
      } catch (SQLException e) {
         logger.error("SQL Exception: " + e.getMessage());
      }
      return result;
   }   
   
   @Override
   public int getNumRows()
   {
      int numRows = 0;
      try 
      {
         resultSet.last();
         numRows = resultSet.getRow();
         resultSet.beforeFirst();
      } 
      catch (NullPointerException e) 
      {
         logger.error(e.getMessage());
      }
      catch (SQLException e) {
         logger.error("SQLException: "  + e.getMessage());
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
      } catch (SQLException e) {
         logger.error("SQL Exception: " + e.getMessage());
      }
   }

   @Override
   public void close() {
      try {         
         if(statement != null)
         {
            statement.close();
         }
         
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
