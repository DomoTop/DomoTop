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
package org.openremote.controller.component;


import junit.framework.Assert;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.junit.Test;
import org.openremote.controller.Constants;
import org.openremote.controller.suite.AllTests;
import org.openremote.controller.command.RemoteActionXMLParser;
import org.openremote.controller.utils.SpringTestContext;

/**
 * Sensor Builder Test.
 *
 * fixture: org/openremote/controller/fixture/controller.xml
 * 
 * @author Dan Cong 
 */
public class SensorBuilderTest
{

  private static SensorBuilder sensorBuilder = (SensorBuilder) SpringTestContext.getInstance()
       .getBean("sensorBuilder");
  private static RemoteActionXMLParser remoteActionXMLParser = (RemoteActionXMLParser) SpringTestContext.getInstance()
       .getBean("remoteActionXMLParser");



  /**
   * <pre>{@code
   * <sensor id="1008" name="range sensor" type="range">
   *   <include type="command" ref="96" />
   *   <min value="-20" />
   *   <max value="100" />
   * </sensor>
   *
   * }</pre>
   */
  @Test public void testCreateRangeSensor() throws Exception
  {
    Sensor s = getSensor(EnumSensorType.RANGE);
    Assert.assertEquals(EnumSensorType.RANGE, s.getSensorType());
    Assert.assertEquals("100", s.getRangeMaxSatateValue());
    Assert.assertEquals("-20", s.getRangeMinSatateValue());
    Assert.assertNotNull(s.getStatusCommand());
  }


  /**
   * <pre>{@code
   * <sensor id="1001" name="lampA power sensor" type="switch">
   *   <include type="command" ref="98" />
   *   <state name="on" value="on" />
   *   <state name="off" value="off" />
   * </sensor>
   *
   * }</pre>
   */
  @Test public void testCreateSwitchSensor() throws Exception
  {
    Sensor s = getSensor(EnumSensorType.SWITCH);
    Assert.assertEquals(EnumSensorType.SWITCH, s.getSensorType());
    Assert.assertEquals("on", s.getStateMap().get("on"));
    Assert.assertEquals("off", s.getStateMap().get("off"));
    Assert.assertNull(s.getRangeMaxSatateValue());
    Assert.assertNull(s.getRangeMinSatateValue());
    Assert.assertNotNull(s.getStatusCommand());
  }


  /**
   * <pre>{@code
   * <sensor id="1010" name="range sensor" type="level">
   *   <include type="command" ref="96" />
   *   <min value="0" />
   *   <max value="100" />
   * </sensor>
   *
   * }</pre>
   */
  @Test public void testCreateLevelSensor() throws Exception
  {
    Sensor s = getSensor(EnumSensorType.LEVEL);
    Assert.assertEquals(EnumSensorType.LEVEL, s.getSensorType());
    Assert.assertEquals("100", s.getRangeMaxSatateValue());
    Assert.assertEquals("0", s.getRangeMinSatateValue());
    Assert.assertNotNull(s.getStatusCommand());
  }


  /**
   * <pre>{@code
   * <sensor id="1009" name="Door power sensor" type="custom">
   *   <include type="command" ref="98" />
   *   <state name="open" value="on" />
   *   <state name="close" value="off" />
   * </sensor>
   *
   * }</pre>
   */
  @Test public void testCreateCustomSensor() throws Exception
  {
    Sensor s = getSensor(EnumSensorType.CUSTOM);
    Assert.assertEquals(EnumSensorType.CUSTOM, s.getSensorType());
    Assert.assertEquals("on", s.getStateMap().get("open"));
    Assert.assertEquals("off", s.getStateMap().get("close"));
    Assert.assertNull(s.getStateMap().get("on"));
    Assert.assertNull(s.getRangeMaxSatateValue());
    Assert.assertNull(s.getRangeMinSatateValue());
    Assert.assertNotNull(s.getStatusCommand());
  }



  // Helpers --------------------------------------------------------------------------------------

  private Sensor getSensor(EnumSensorType type) throws Exception
  {

    String controllerXmlFixturePath = AllTests.getFixtureFile(Constants.CONTROLLER_XML);

    Document doc = null;
    SAXBuilder builder = new SAXBuilder();

    doc = builder.build(controllerXmlFixturePath);

    Element ele = null;

    switch (type)
    {
      case RANGE:
        ele = remoteActionXMLParser.queryElementFromXMLById(doc, "1008");
        break;

      case LEVEL:
        ele = remoteActionXMLParser.queryElementFromXMLById(doc, "1010");
        break;

      case SWITCH:
        ele = remoteActionXMLParser.queryElementFromXMLById(doc, "1001");
        break;

      case CUSTOM:
        ele = remoteActionXMLParser.queryElementFromXMLById(doc, "1009");
        break;

      case COLOR:
        //TODO
        break;

      default:
        break;
    }
    
    return sensorBuilder.build(ele);
  }

}
