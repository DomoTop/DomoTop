package org.openremote.controller.service;

import java.sql.ResultSet;

/**
 * @author <a href="mailto:melroy.van.den.berg@tass.nl">Melroy van den Berg</a> 2012
 */

public interface GroupService 
{   
   /**
    * Get all groups.
    * 
    * @return The result set from the database with all the information from every groups
    */
   ResultSet getGroups();
   /**
    * Add new group to the database.
    * 
    * @param groupName
    *           the name of the group
    * 
    * @return int 0 = error with select or insert, 1 insert query went successfully, 2 group already exists
    */
   int addGroup(String groupName);
   /**
    * Get one group result set from the database.
    * 
    * @param groupID
    *           id from the group
    * @return ResultSet the result from the database with group information
    */
   ResultSet getGroup(int groupID);
   /**
    * Deletes a group from the database
    * @param groupID The id of the group you want to remove
    * @return int value -1 or 0 is incorrect, 1 is action succeed
    */
   int removeGroup(int groupID);
   /**
    * Drop/remove all groups from the database
    * 
    * @return value -1 or 0 is incorrect, 1 is correct
    */
   int dropGroups();
   /**
    * Returns the number of groups. Note: You should use getGroups() first and directly a getNumGroups()
    * 
    * @see #getGroups()
    * 
    * @return int of the number of groups
    */
   int getNumGroups();   
   /**
    * Close the result set.
    */
   void free();
}
