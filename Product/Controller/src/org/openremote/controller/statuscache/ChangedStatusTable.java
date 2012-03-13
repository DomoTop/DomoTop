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
package org.openremote.controller.statuscache;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Use wait, notify, synchronize mechanism to do polling.<br /><br /> 
 * This table is used to record the skipped changed statuses and waited changed statuses .<br />
 * <b>Use Case1:</b>&nbsp;During the process of iPhone refreshing the polling connection or
 * dealing with the response of changed state, a later (very soon, before iPhone reestablishes the next polling,
 * although it's kind of probability stuff) change won't be detected by iPhone , in other word, this polling request has left
 * the changed status. the result is when iPhone comes back to continue polling, it
 * knows nothing about what has happened just now, and iPhone will keep the old view and waiting for the next change
 * which is not synchronous any more.<br />
 * 
 * <b>Use Case2:</b>If no statuses changed, polling request will <b>WAIT</b><br /> the Corresponded ChangedStatusRecord until<br />
 * the waited polling sensor ids' statuses changed. So, the polling request will be notified and get the change statuses.<br />
 * 
 * @author Handy.Wang 2009-10-23
 */
public class ChangedStatusTable {
   
   private List<ChangedStatusRecord> recordList;

   public ChangedStatusTable() {
      super();
      recordList = new ArrayList<ChangedStatusRecord>();
   }

   /**
    * Insert a changed status record.
    */
   public synchronized void insert(ChangedStatusRecord record) {
      if (this.query(record.getDeviceID(), record.getPollingSensorIDs()) == null) { 
         recordList.add(record);
      }
   }
   
   /**
    * Query changed status record by deviceID and pollingSensorIDs(pollingSensorIDs is order-insensitive).
    */
   public synchronized ChangedStatusRecord query(String deviceID, List<Integer> pollingSensorIDs) {
      if (recordList.size() == 0 || pollingSensorIDs == null || pollingSensorIDs.size() == 0) {
         return null;
      }
      ChangedStatusRecord record = new ChangedStatusRecord(deviceID, pollingSensorIDs);
      
      for (ChangedStatusRecord tempRecord : recordList) {
         if (tempRecord.equals(record)) {
            return tempRecord;
         }
      }
      return null;
   }
   
   /**
    * Query all changed status records whose pollingSensorID column contains statusChangedSensorID.
    */
   public synchronized List<ChangedStatusRecord> query(Integer statusChangedSensorID) {
      List<ChangedStatusRecord> statusChangedRecord = new ArrayList<ChangedStatusRecord>();
      if(recordList==null||recordList.size()==0){
         return null;
      }
      for (ChangedStatusRecord record : recordList) {
         if (record.getPollingSensorIDs().contains(statusChangedSensorID)) {
            statusChangedRecord.add(record);
         }
      }
      return statusChangedRecord;
   }
   
   /**
    * Update status_changed_id column.
    */
   public void updateStatusChangedIDs(Integer statusChangedSensorID) {
      for(ChangedStatusRecord record : recordList){
         synchronized (record) {
            if (record.getPollingSensorIDs() != null && record.getPollingSensorIDs().size() != 0) {
               for (Integer tmpSensorId : record.getPollingSensorIDs()) {
                  if (statusChangedSensorID.equals(tmpSensorId)) {
                     record.getStatusChangedSensorIDs().add(statusChangedSensorID);
                     record.notifyAll();
                     break;
                  }
               }
            }
         }
      }
   }
   
   /**
    * Reset changed status of panel in {@link ChangedStatusTable}. 
    */
   public synchronized void resetChangedStatusIDs(String deviceID, List<Integer> pollingSensorIDs) {
      ChangedStatusRecord skippedStatusRecord = this.query(deviceID, pollingSensorIDs);
      skippedStatusRecord.setStatusChangedSensorIDs(new HashSet<Integer>());
   }

   /**
    * Clear all records
    */
   public void clearAllRecords() {
      this.recordList.clear();
   }

   public List<ChangedStatusRecord> getRecordList() {
      return recordList;
   }
   
}
