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
package org.openremote.controller.statuscache;

import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.junit.Before;
import org.junit.Test;
import org.openremote.controller.suite.AllTests;
import org.openremote.controller.command.RemoteActionXMLParser;
import org.openremote.controller.exception.NoSuchComponentException;
import org.openremote.controller.service.PollingMachinesService;
import org.openremote.controller.service.StatusCacheService;
import org.openremote.controller.utils.SpringTestContext;
import org.openremote.controller.Constants;

/**
 *  
 * @author Handy.Wang 2010-03-17
 */
public class InitCachedStatusDBListenerTest
{
  private Logger logger = Logger.getLogger(this.getClass().getName());

  private long pollingMachineInterval = 5000;// 2 seconds

  private RemoteActionXMLParser remoteActionXMLParser = (RemoteActionXMLParser)SpringTestContext.getInstance().getBean("remoteActionXMLParser");
  private StatusCacheService statusCacheService = (StatusCacheService) SpringTestContext.getInstance().getBean("statusCacheService");

  private Document doc = null;
  private List<Element> sensorElements;
  private PollingMachinesService pollingMachinesService = (PollingMachinesService)SpringTestContext.getInstance().getBean("pollingMachinesService");

  @Before public void setUp() throws Exception
  {
    String controllerXMLPath = AllTests.getFixtureFile(Constants.CONTROLLER_XML);

    SAXBuilder builder = new SAXBuilder();
    doc = builder.build(controllerXMLPath);

    sensorElements = remoteActionXMLParser.queryElementsFromXMLByName(doc, "sensor");
    pollingMachinesService.initStatusCacheWithControllerXML(doc);
    pollingMachinesService.startPollingMachineMultiThread();
  }


  /**
   * Test method initStatusCacheWithControllerXML
   */
  @Test public void testInitStatusCacheWithControllerXML() throws Exception
  {
    List<Element> sensorElements = remoteActionXMLParser.queryElementsFromXMLByName(doc, "sensor");

    Assert.assertTrue("Expected true but false.", sensorElements.size() == 7);

    for (Element sensorElement : sensorElements)
    {
       try
       {
          int sensorElementID = Integer.parseInt(sensorElement.getAttributeValue("id"));
          logger.info("Sensor id: " + sensorElementID + ", read status:" + statusCacheService.getStatusBySensorId(sensorElementID));
       }

       catch (NoSuchComponentException e)
       {
          fail(e.getMessage());
       }
    }

    Thread.sleep(6000);
  }


  /**
  * Test method startPollingMachineMultiThread
  *
  * if status of one sensor in status cache changed, it can explain that the pollingMachine threads work.
  */
  @Test public void testStartPollingMachineMultiThread() throws Exception
  {

    //Begin: Load the initialized statuses of sensorIDs into a sensorID-Status pair map for comparing later.

    Map<String, String> sensorIDStatusMap = new HashMap<String, String>();

    for (Element sensorElement : sensorElements)
    {
      String sensorID = sensorElement.getAttributeValue("id");
      sensorIDStatusMap.put(sensorID, statusCacheService.getStatusBySensorId(Integer.parseInt(sensorID)));
    }
    //End

    //Begin: Nap for a while longer than pollingMachine interval.
    Thread.sleep(pollingMachineInterval + 1000);
    //End

    //Begin: Compare the old statuses of sensorIDs after napping. If certain status of corresponding sensorID had changed, it indicates the pollingMachine threads work, else fail.
    Set<String> sensorIDSet = sensorIDStatusMap.keySet();
    for (String sensorID : sensorIDSet)
    {
      logger.info("Old status of sensor " + sensorID + " is " + sensorIDStatusMap.get(sensorID));
      logger.info("Latest status of sensor " + sensorID + " is " + statusCacheService.getStatusBySensorId(Integer.parseInt(sensorID)));

      if (!statusCacheService.getStatusBySensorId(Integer.parseInt(sensorID)).equals(sensorIDStatusMap.get(sensorID))) {
        logger.info("*********************************************************");
        logger.info("*Finished test method testStartPollingMachineMultiThread*");
        logger.info("*********************************************************");
        return;
      }
    }
    fail();

    //End
    logger.info("*********************************************************");
    logger.info("*Finished test method testStartPollingMachineMultiThread*");
    logger.info("*********************************************************");
  }
}
