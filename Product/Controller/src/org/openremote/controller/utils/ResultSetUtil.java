/*
* Copyright 2001,2004 The Apache Software Foundation.
* 
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* 
*      http://www.apache.org/licenses/LICENSE-2.0
* 
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.openremote.controller.utils;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * General purpose utility methods related to ResultSets
 *
 * @author Ted Husted, Melroy van den Berg
 */
public class ResultSetUtil {

    /**
     * Returns next record of result set as a Map.
     * The keys of the map are the column names,
     * as returned by the metadata.
     * The values are the columns as Objects.
     *
     * @param resultSet The ResultSet to process.
     * @exception SQLException if an error occurs.
     */
    public static Map getMap(ResultSet resultSet)
       throws SQLException {

           // Acquire resultSet MetaData
       ResultSetMetaData metaData = resultSet.getMetaData();
       int cols = metaData.getColumnCount();

           // Create hashmap, sized to number of columns
       HashMap row = new HashMap(cols,1);

           // Transfer record into hashmap
       if (resultSet.next()) {
           for (int i=1; i<=cols ; i++) {
               row.put(metaData.getColumnName(i),
                   resultSet.getObject(i));
           }
       } // end while

       return ((Map) row);

    } // end getMap


    /**
     * Return a Collection of Maps, each representing
     * a row from the ResultSet.
     * The keys of the map are the column names,
     * as returned by the metadata.
     * The values are the columns as Objects.
     *
     * @param resultSet The ResultSet to process.
     * @exception SQLException if an error occurs.
     */
    public static Collection getMaps(ResultSet resultSet)
       throws SQLException {

           // Acquire resultSet MetaData
       ResultSetMetaData metaData = resultSet.getMetaData();
       int cols = metaData.getColumnCount();

           // Use ArrayList to maintain ResultSet sequence
       ArrayList list = new ArrayList();

           // Scroll to each record, make map of row, add to list
       while (resultSet.next()) {
           HashMap row = new HashMap(cols,1);
           for (int i=1; i<=cols ; i++) {              
               row.put(metaData.getColumnName(i).toLowerCase(),
                   resultSet.getObject(i)); //toString(i)
           }
           list.add(row);
       } // end while

       return ((Collection) list);

    } // end getMaps
} // end ResultSetUtils
