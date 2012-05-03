package org.openremote.controller.service.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.openremote.controller.Constants;
import org.openremote.controller.service.DatabaseService;
import org.openremote.controller.service.GroupService;

/**
 * Get group information
 * 
 * @author <a href="mailto:melroy.van.den.berg@tass.nl">Melroy van den Berg</a> 2012
 */

public class GroupServiceImpl implements GroupService
{
   private final static Logger logger = Logger.getLogger(Constants.SERVICE_LOG_CATEGORY);

   private static String selectGroupQuery = "SELECT * FROM client_group WHERE group_id = ? ";
   private static String selectAllGroupsQuery = "SELECT * FROM client_group ORDER BY group_name ASC";
   private static String insertGroupQuery = "INSERT INTO client_group (group_name) VALUES ";
   private static String limitByOne = " LIMIT 1";

   private DatabaseService database;

   /**
    * Get all groups.
    * 
    * @return The result set from the database with all the information from every group
    */
   @Override
   public ResultSet getGroups() {
      ResultSet returnValue = null;
      if (database != null) {
         returnValue = database.doSQL(selectAllGroupsQuery);
      } else {
         logger.error("Database is not yet set (null)");
      }
      return returnValue;
   }

   /**
    * Add new group to the database.
    * 
    * @param groupName
    *           the name of the group
    * 
    * @return int 0 = error with select or insert, 1 insert query went successfully, 2 group already exists
    */
   @Override
   public int addGroup(String groupName) 
   {
      int returnValue = 0;
      int resultValue = -1;
      int numRows = -1;

      if (database != null) 
      {
         PreparedStatement preparedStatement = null;

         try
         {            
            preparedStatement = database.createPrepareStatement("SELECT group_name FROM PUBLIC.client_group WHERE group_name = ? LIMIT 1");
            preparedStatement.setString(1, groupName);
            database.doSQL(preparedStatement);
            numRows = database.getNumRows();
         } catch (SQLException e) {
            logger.error("SQL Exception: " + e.getMessage());
         }

         // Check if client doesn't exist in the database
         if (numRows == 0) 
         {
            try
            {  
               preparedStatement = database.createPrepareStatement(insertGroupQuery + "(?)");
               preparedStatement.setString(1, groupName);
               resultValue = database.doUpdateSQL(preparedStatement);
            } catch (SQLException e) {
               logger.error("SQL Exception: " + e.getMessage());
            }
            
            if (resultValue >= 1) {
               returnValue = 1;
            }
         } else {
            // ignore a second user with the same device name and pin
            returnValue = 2;
         }
      } else {
         logger.error("Database is not yet set (null)");
      }
      return returnValue;
   }

   /**
    * Get one group result set from the database.
    * 
    * @param groupID
    *           id from the group
    * @return ResultSet the result from the database with group information
    */
   @Override
   public ResultSet getGroup(int groupID) 
   {
      ResultSet returnValue = null;
      PreparedStatement preparedStatement = null;
      
      if (database != null) 
      {
         try {
            preparedStatement = database.createPrepareStatement(selectGroupQuery);
            preparedStatement.setInt(1, groupID);
            returnValue = database.doSQL(preparedStatement);
         } catch (SQLException e) {
            logger.error("SQL Exception: " + e.getMessage());
         }
      } else {
         logger.error("Database is not yet set (null)");
      }
      return returnValue;
   }
   
   /**
    * Deletes a group from the database
    * @param groupID The id of the group you want to remove
    * @return int value -1 or 0 is incorrect, 1 is action succeed
    */
   public int removeGroup(int groupID) {
      int resultValue = -1;
      PreparedStatement preparedStatement = null;
      if(database != null) 
      {
         try
         {
            preparedStatement = database.createPrepareStatement("DELETE FROM client_group WHERE group_id = ?");
            preparedStatement.setInt(1, groupID);
            resultValue = database.doUpdateSQL(preparedStatement);
         } catch (SQLException e) {
            logger.error("SQL Exception: " + e.getMessage());
         }
      }
      
      return resultValue;
   }
   
   /**
    * Drop/remove all groups from the database
    * 
    * @return value -1 or 0 is incorrect, 1 is correct
    */
   @Override
   public int dropGroups()
   {
      int resultValue = -1;

      if (database != null) {
         resultValue = database.doUpdateSQL("TRUNCATE TABLE client_group");
      }
      return resultValue;
   }
   
   /**
    * Returns the number of groups. Note: You should use getGroups() first and directly a getNumGroups()
    * 
    * @see #getGroups()
    * 
    * @return int of the number of groups
    */
   @Override
   public int getNumGroups() {
      int newNum = -1;
      if (database != null) {
         newNum = database.getNumRows();
      }
      return newNum;
   }
      
   /**
    * Close the result set.
    */
   @Override
   public void free() {
      database.free();
   }

   /**
    * Sets the database.
    * 
    * @param database
    *           service
    */

   public void setDatabase(DatabaseService database) {
      this.database = database;
   }
}
