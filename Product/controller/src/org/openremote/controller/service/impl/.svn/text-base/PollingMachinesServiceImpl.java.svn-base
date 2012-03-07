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
package org.openremote.controller.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.openremote.controller.Constants;
import org.openremote.controller.ControllerConfiguration;
import org.openremote.controller.command.RemoteActionXMLParser;
import org.openremote.controller.component.Sensor;
import org.openremote.controller.component.SensorBuilder;
import org.openremote.controller.config.ControllerXMLListenSharingData;
import org.openremote.controller.exception.ControllerXMLNotFoundException;
import org.openremote.controller.service.PollingMachinesService;
import org.openremote.controller.service.StatusCacheService;
import org.openremote.controller.statuscache.PollingMachineThread;
import org.openremote.controller.utils.PathUtil;

/**
 * Polling status from devices by sensor status command. 
 * 
 * @author Handy.Wang 2010-03-17
 *
 */
public class PollingMachinesServiceImpl implements PollingMachinesService {
   
   private static Logger log = Logger.getLogger(PollingMachinesServiceImpl.class);
   private StatusCacheService statusCacheService;
   private RemoteActionXMLParser remoteActionXMLParser;
   private ControllerXMLListenSharingData controllerXMLListenSharingData;

   private Logger logger = Logger.getLogger("INIT");
   private SensorBuilder sensorBuilder;
   
   /**
    * {@inheritDoc}
    */
   @Override
   public void initStatusCacheWithControllerXML(Document document) {
      List<Element> sensorElements = null;
      try {
         if (document == null) {
            sensorElements = remoteActionXMLParser.queryElementsFromXMLByName("sensor");
         } else {
            sensorElements = remoteActionXMLParser.queryElementsFromXMLByName(document, "sensor");
         }
      } catch (ControllerXMLNotFoundException e) {
         log.warn("No sensor to init, controller.xml not found.");
         return;
      }
      
      if (sensorElements != null)
      {
         for (Element sensorElement : sensorElements)
         {
            Sensor sensor = sensorBuilder.build(document, sensorElement);

            controllerXMLListenSharingData.addSensor(sensor);

            statusCacheService.saveOrUpdateStatus(sensor.getSensorID(), "N/A");
         }
      }
      
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void startPollingMachineMultiThread() {
      for (Sensor sensor : controllerXMLListenSharingData.getSensors()) {
         PollingMachineThread pollingMachineThread = new PollingMachineThread(sensor, statusCacheService);
         pollingMachineThread.start();
         nap(3);
         controllerXMLListenSharingData.addPollingMachineThread(pollingMachineThread);
      }
      
      storeXMLContent(Constants.CONTROLLER_XML);
      storeXMLContent(Constants.PANEL_XML);
   }
   
   private void storeXMLContent(String xmlFileName) {
      String xmlFilePath = PathUtil.addSlashSuffix(ControllerConfiguration.readXML().getResourcePath()) + xmlFileName;
      File xmlFile = new File(xmlFilePath);
      try {
         StringBuffer fileContent = new StringBuffer(FileUtils.readFileToString(xmlFile, "utf-8"));
         if (Constants.CONTROLLER_XML.equals(xmlFileName)) {
            controllerXMLListenSharingData.setControllerXMLFileContent(fileContent);
         } else if (Constants.PANEL_XML.equals(xmlFileName)) {
            controllerXMLListenSharingData.setPanelXMLFileContent(fileContent);
         }
      } catch (IOException ioe) {
         logger.warn("Skipped " + xmlFileName + " change check, Failed to read " + xmlFile.getAbsolutePath());
      }
   }
   
   /** Sleep for several seconds */
   private void nap(long sec) {
      try {
         Thread.sleep(sec);
      } catch (InterruptedException e) {
         logger.error("Failed to sleep");
      }
   }

   public void setStatusCacheService(StatusCacheService statusCacheService) {
      this.statusCacheService = statusCacheService;
   }

   public void setRemoteActionXMLParser(RemoteActionXMLParser remoteActionXMLParser) {
      this.remoteActionXMLParser = remoteActionXMLParser;
   }

   public void setControllerXMLListenSharingData(ControllerXMLListenSharingData controllerXMLListenSharingData) {
      this.controllerXMLListenSharingData = controllerXMLListenSharingData;
   }

   public void setSensorBuilder(SensorBuilder sensorBuilder) {
      this.sensorBuilder = sensorBuilder;
   }
   
}
