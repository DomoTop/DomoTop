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
package org.openremote.controller.component;

import java.util.HashMap;
import java.util.Map;

import org.openremote.controller.command.NoStatusCommand;
import org.openremote.controller.command.StatusCommand;

/**
 * Sensor will listen status change by using {@link StatusCommand}.
 *
 * @author Handy.Wang 2010-01-04
 *
 */
public class Sensor {

   /**
    * key name used in state map to store range min state.
    */
   public final static String RANGE_MIN_STATE = "_range_min_state_";
   /**
    * key name used in state map to store range max state.
    */
   public final static String RANGE_MAX_STATE = "_range_max_state_";

   private int sensorID;

   private EnumSensorType sensorType;

   private StatusCommand statusCommand;

   /**
    * all states in a map.
    * key is state name, value is actual state value.
    */
   private Map<String, String> stateMap;

   public Sensor() {
      super();
      this.statusCommand = new NoStatusCommand();
      stateMap = new HashMap<String, String>();
   }

   public Sensor(int sensorID, String sensorType, StatusCommand statusCommand, Map<String, String> stateMap) {
      super();
      this.sensorID = sensorID;
      this.sensorType = EnumSensorType.enumValueOf(sensorType);
      this.statusCommand = statusCommand;
      this.stateMap = stateMap;
   }

   public Sensor(StatusCommand statusCommand) {
      super();
      this.statusCommand = statusCommand;
   }

   public int getSensorID() {
      return sensorID;
   }

   public void setSensorID(int sensorID) {
      this.sensorID = sensorID;
   }

   public StatusCommand getStatusCommand() {
      return statusCommand;
   }

   public void setStatusCommand(StatusCommand statusCommand) {
      this.statusCommand = statusCommand;
   }

   public EnumSensorType getSensorType() {
      return sensorType;
   }

   public void setSensorType(EnumSensorType sensorType) {
      this.sensorType = sensorType;
   }

   public void setSensorType(String sensorTypeStr) {
      this.sensorType = EnumSensorType.enumValueOf(sensorTypeStr);
   }

   /**
    * State map contains all supported states.
    * key is state name, value is actual state value.
    * e.g. for a switch may be:
    * <ul>
    * <li>on:light1_on</li>
    * <li>off:light1_off</li>
    * </ul>
    *
    * This map is used to find state name according to returned state value.
    *
    * @return state map
    */
   public Map<String, String> getStateMap() {
      return stateMap;
   }

   public void setStateMap(Map<String, String> stateMap) {
      this.stateMap = stateMap;
   }

   /**
    * Read status using status command.
    *
    * @return status
    */
   public String readStatus() {
      return statusCommand.read(sensorType, stateMap);
   }
   /**
    * Only available when sensor type is RANGE or LEVEL.
    *
    * @return range max state value
    */
   public String getRangeMaxSatateValue() {
      return stateMap == null ? null : stateMap.get(RANGE_MAX_STATE);
   }

   /**
    * Only available when sensor type is RANGE or LEVEL.
    *
    * @return range min state value
    */
   public String getRangeMinSatateValue() {
      return stateMap == null ? null : stateMap.get(RANGE_MIN_STATE);
   }


  @Override public String toString()
  {
    return sensorID + " " + sensorType + " " + statusCommand;
  }

}
