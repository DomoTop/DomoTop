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
package org.openremote.controller.config;

import java.util.ArrayList;
import java.util.List;

import org.openremote.controller.component.Sensor;
import org.openremote.controller.statuscache.PollingMachineThread;

/**
 * 
 * @author handy.wang 2010-03-19
 *
 */
public class ControllerXMLListenSharingData {

   private Boolean isControllerXMLChanged = false;
   private List<PollingMachineThread> pollingMachineThreads = new ArrayList<PollingMachineThread>();
   private List<Sensor> sensors = new ArrayList<Sensor>();
   private StringBuffer controllerXMLFileContent = new StringBuffer();
   private StringBuffer panelXMLFileContent = new StringBuffer();
   
   public Sensor findSensorById(String id) {
      for (Sensor sensor : sensors) {
         if (sensor.getSensorID() == Integer.valueOf(id)) {
            return sensor;
         }
      }
      return null;
   }
   
   public void addPollingMachineThread(PollingMachineThread pollingMachineThread) {
      this.pollingMachineThreads.add(pollingMachineThread);
   }
   
   public List<PollingMachineThread> getPollingMachineThreads() {
      return pollingMachineThreads;
   }

   public void addSensor(Sensor sensor) {
      this.sensors.add(sensor);
   }
   
   public List<Sensor> getSensors() {
      return sensors;
   }

   public void setControllerXMLFileContent(StringBuffer controllerXMLFileContent) {
      this.controllerXMLFileContent = controllerXMLFileContent;
   }

   public String getControllerXMLFileContent() {
      return controllerXMLFileContent.toString();
   }

   public String getPanelXMLFileContent() {
      return panelXMLFileContent.toString();
   }

   public void setPanelXMLFileContent(StringBuffer panelXMLFileContent) {
      this.panelXMLFileContent = panelXMLFileContent;
   }

   public Boolean getIsControllerXMLChanged() {
      return isControllerXMLChanged;
   }

   public void setIsControllerXMLChanged(Boolean isControllerXMLChanged) {
      this.isControllerXMLChanged = isControllerXMLChanged;
   }
   
   
}
