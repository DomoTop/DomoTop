/*
 * OpenRemote, the Home of the Digital Home.
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
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.jdom.Element;
import org.openremote.controller.Constants;
import org.openremote.controller.ControllerConfiguration;
import org.openremote.controller.command.RemoteActionXMLParser;
import org.openremote.controller.component.Sensor;
import org.openremote.controller.component.SensorBuilder;
import org.openremote.controller.config.ControllerXMLListenSharingData;
import org.openremote.controller.exception.ControllerException;
import org.openremote.controller.exception.NoSuchComponentException;
import org.openremote.controller.service.ControllerXMLChangeService;
import org.openremote.controller.service.StatusCacheService;
import org.openremote.controller.statuscache.ChangedStatusTable;
import org.openremote.controller.statuscache.PollingMachineThread;
import org.openremote.controller.utils.PathUtil;

/**
 * Controller.xml monitoring service.
 * 
 * @author handy.wang 2010-03-19
 *
 */
public class ControllerXMLChangeServiceImpl implements ControllerXMLChangeService
{

  private static Logger logger = Logger.getLogger(Constants.DEPLOYER_LOG_CATEGORY);

  
  private ControllerXMLListenSharingData controllerXMLListenSharingData;
  private RemoteActionXMLParser remoteActionXMLParser;
  private StatusCacheService statusCacheService;
  private ChangedStatusTable changedStatusTable;
  private SensorBuilder sensorBuilder;

   
  @SuppressWarnings("finally")
  @Override public synchronized boolean refreshController()
  {
    if (!isObservedXMLContentChanged(Constants.CONTROLLER_XML) && !isObservedXMLContentChanged(Constants.PANEL_XML))
    {
       return true;
    }

    logger.info("Controller.xml of Controller changed, refreshing controller.xml");
    boolean success = false;
    tagControllerXMLChanged(true);

    try
    {
       killAndClearDevicePollingThreads();
       clearChangedStatusTable();
       clearStatusCache();
       clearAndReloadSensors();
       restartDevicePollingThreads();
       success = true;
    }

    catch (ControllerException e)
    {
      logger.error("Error occured while refreshing controller.", e);
      success = false;
    }

    finally
    {
      tagControllerXMLChanged(false);
      String isSuccessInfo = success ? " success " : " failed ";
      logger.info("Finished refreshing controller.xml" + isSuccessInfo);
      return success;
    }
  }
   
  private boolean isObservedXMLContentChanged(String observedXMLFileName)
  {
    //if changed, save the latest controller.xml.
    String observedXMLFilePath = PathUtil.addSlashSuffix(ControllerConfiguration.readXML().getResourcePath()) + observedXMLFileName;
    File observedXMLFile = new File(observedXMLFilePath);
    StringBuffer fileContent = new StringBuffer();
    String oldXMLFileContent = new String();

    if (Constants.CONTROLLER_XML.equals(observedXMLFileName))
    {
      oldXMLFileContent = controllerXMLListenSharingData.getControllerXMLFileContent();
    }

    else if (Constants.PANEL_XML.equals(observedXMLFileName))
    {
      oldXMLFileContent = controllerXMLListenSharingData.getPanelXMLFileContent();
    }

    try
    {
      fileContent.append(FileUtils.readFileToString(observedXMLFile, Constants.CHARACTER_ENCODING_UTF8));
    }

    catch (IOException ioe)
    {
      logger.warn("Skipped " + observedXMLFileName + " change check, Failed to read " + observedXMLFile.getAbsolutePath());
      return false;
    }

    if (oldXMLFileContent.equals(fileContent.toString()))
    {
      return false;
    }

    if (Constants.CONTROLLER_XML.equals(observedXMLFileName))
    {
      controllerXMLListenSharingData.setControllerXMLFileContent(fileContent);
    }

    else if (Constants.PANEL_XML.equals(observedXMLFileName))
    {
      controllerXMLListenSharingData.setPanelXMLFileContent(fileContent);
    }

    return true;
  }
   
  private void tagControllerXMLChanged(boolean isChanged)
  {
    controllerXMLListenSharingData.setIsControllerXMLChanged(isChanged);
  }
   
  private void killAndClearDevicePollingThreads()
  {
    List<PollingMachineThread> pollingThreads = controllerXMLListenSharingData.getPollingMachineThreads();

    for (PollingMachineThread pollingThread : pollingThreads)
    {
       pollingThread.kill();
    }

    nap(10); // Just give the theads some times to get the timeslice and really stop.

    pollingThreads.clear();
  }
   
  private void clearChangedStatusTable()
  {
    List<Sensor> sensors= controllerXMLListenSharingData.getSensors();

    for (Sensor sensor : sensors)
    {
       // Just wake up all the records, acturelly, the status didn't change.
       changedStatusTable.updateStatusChangedIDs(sensor.getSensorID());
    }

    changedStatusTable.clearAllRecords();
  }
   
  private void clearStatusCache()
  {
    statusCacheService.clearAllStatusCache();
  }
   
  @SuppressWarnings("unchecked")
  private void clearAndReloadSensors()
  {
    controllerXMLListenSharingData.getSensors().clear();

    // followings are re-parse sensors and their included statuscommand.
    Element sensorsElement = remoteActionXMLParser.queryElementFromXMLByName(Constants.SENSORS_ELEMENT_NAME);

    if (sensorsElement == null)
    {
      throw new NoSuchComponentException("DOM element " + Constants.SENSORS_ELEMENT_NAME + " doesn't exist in " + Constants.CONTROLLER_XML);
    }

    List<Element> sensorElements = sensorsElement.getChildren();

    if (sensorElements == null)
    {
      throw new ControllerException("There is no sub DOM elements in " + Constants.SENSORS_ELEMENT_NAME + " in " + Constants.CONTROLLER_XML);
    }

    Iterator<Element> sensorElementIterator = sensorElements.iterator();

    while (sensorElementIterator.hasNext())
    {
      Element sensorElement = sensorElementIterator.next();
      Sensor sensor = sensorBuilder.build(sensorElement);
      controllerXMLListenSharingData.addSensor(sensor);
    }
  }
   
  private void restartDevicePollingThreads()
  {
    controllerXMLListenSharingData.getPollingMachineThreads().clear();
    Iterator<Sensor> sensorIterator = controllerXMLListenSharingData.getSensors().iterator();

    while (sensorIterator.hasNext())
    {
      Sensor sensor = sensorIterator.next();
      PollingMachineThread pollingThread = new PollingMachineThread(sensor, statusCacheService);
      pollingThread.start();
      nap(3);
      controllerXMLListenSharingData.getPollingMachineThreads().add(pollingThread);
    }
  }

  public void setControllerXMLListenSharingData(ControllerXMLListenSharingData controllerXMLListenSharingData)
  {
    this.controllerXMLListenSharingData = controllerXMLListenSharingData;
  }
   
  public void setRemoteActionXMLParser(RemoteActionXMLParser remoteActionXMLParser)
  {
    this.remoteActionXMLParser = remoteActionXMLParser;
  }

  public void setStatusCacheService(StatusCacheService statusCacheService)
  {
    this.statusCacheService = statusCacheService;
  }
   
  public void setChangedStatusTable(ChangedStatusTable changedStatusTable)
  {
    this.changedStatusTable = changedStatusTable;
  }
   
  public void setSensorBuilder(SensorBuilder sensorBuilder)
  {
    this.sensorBuilder = sensorBuilder;
  }

  private void nap(long milliseconds)
  {
    try
    {
       Thread.sleep(milliseconds);
    }

    catch (InterruptedException e)
    {
       logger.error("InterruptedException", e);
    }
  }

}

