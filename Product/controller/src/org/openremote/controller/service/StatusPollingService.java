/* OpenRemote, the Home of the Digital Home.
* Copyright 2008-2011, OpenRemote Inc.
*
* See the contributors.txt file in the distribution for a
* full listing of individual contributors.
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Affero General Public License as
* published by the Free Software Foundation, either version 3 of the
* License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU Affero General Public License for more details.
*
* You should have received a copy of the GNU Affero General Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package org.openremote.controller.service;


/**
 * It's responsiable for polling changed statuses from changed status table.
 * 
 * @author Handy.Wang 2009-10-21
 */
public interface StatusPollingService {
   
   /** The max length of time of current servlet response. */
    static final int MAX_TIME_OUT_SECONDS = 50;
   
   /** A second equals how much mili seconds. */
    static final int MILLI_SECONDS_A_SECOND = 1000;

   /** This value will be responsed when current servlet couldn't get the changed statuses in the <b>MAX_TIME_OUT_SECONDS</b>. */
   static final String SERVER_RESPONSE_TIME_OUT_STATUS_CODE = "TIMEOUT";

   /**
    * Query changed states from ChangedStatus table. 
    */
   public String queryChangedState(String deviceID, String unParsedSensorIDs);
   
}
