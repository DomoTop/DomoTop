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
package org.openremote.controller.protocol.http;


import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.Assert;

import org.jdom.Element;
import org.junit.Before;
import org.junit.Test;
import org.junit.After;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.AbstractHandler;
import org.openremote.controller.command.Command;
import org.openremote.controller.command.ExecutableCommand;
import org.openremote.controller.command.StatusCommand;
import org.openremote.controller.component.EnumSensorType;
import org.openremote.controller.component.Sensor;
import org.openremote.controller.exception.NoSuchCommandException;


/**
 * Tests the implementation of {@link org.openremote.controller.protocol.http.HttpGetCommand} class.
 *
 * TODO:
 *   http authentication tests
 *
 *
 * @author <a href="mailto:juha@openremote.org">Juha Lindfors</a>
 * @author Javen
 * @author Dan Cong
 *
 */
public class HttpGetCommandBuilderTest
{

  // Constants ------------------------------------------------------------------------------------

  /**
   * Port we are using for the HTTP server during tests.
   */
  private final static int HTTP_SERVER_PORT = 9999;

  /**
   * Localhost IP address.
   */
  private final static String LOCALHOST = "127.0.0.1";

  /**
   * URL to access the local HTTP server for these tests.
   */
  private final static String HTTP_SERVER_URL = "http://" + LOCALHOST + ":" + HTTP_SERVER_PORT;



  // Instance Fields ------------------------------------------------------------------------------


  /**
   * Reference to the builder implementation for HTTP commands.
   */
  private HttpGetCommandBuilder builder = new HttpGetCommandBuilder();

  /**
   * HTTP server that can be used to provide responses to HTTP commands.
   */
  private Server httpServer;



  // Test Setup and Tear Down ---------------------------------------------------------------------


  /**
   * Start the HTTP server for each test.
   *
   * @throws Exception  if HTTP server start failed.
   */
  @Before public void setUp() throws Exception
  {
    httpServer = new Server(HTTP_SERVER_PORT);
    httpServer.setHandler(new HttpServerResponse());
    httpServer.start();
  }

  /**
   * Stop the HTTP server after each test (regardless of success or failure)
   *
   * @throws Exception    if server stop failed
   */
  @After public void tearDown() throws Exception
  {
    httpServer.stop();
  }




  // Constructor Tests ----------------------------------------------------------------------------


  /**
   * Tests basic constructor access and property setting is done correctly.
   */
  @Test public void testBasicConstruction()
  {
    final String URL = "http://www.openremote.org";

    Command cmd = getHttpCommand(URL);

    Assert.assertTrue(cmd instanceof HttpGetCommand);

    HttpGetCommand httpCommand = (HttpGetCommand)cmd;

    Assert.assertTrue(URL.equals(httpCommand.getUrl().toExternalForm()));
  }



  // Variable ${param} Tests ----------------------------------------------------------------------

  /**
   * Tests for parameter replacement -- if ${param} is used somewhere within the configured URL,
   * it should be replaced with a value given in the command's XML element attribute (denoted
   * with {@link Command#DYNAMIC_VALUE_ATTR_NAME} attribute) in the HTTP command's builder
   * implementation.
   */
  @Test public void testParameterPlacement()
  {
    final String parameterizedURL = "http://www.openremote.org/command?param=";

    Command cmd = getParameterizedHttpCommand(parameterizedURL, 100);

    Assert.assertTrue(cmd instanceof HttpGetCommand);

    HttpGetCommand httpCommand = (HttpGetCommand)cmd;

    Assert.assertTrue((parameterizedURL + "100").equals(httpCommand.getUrl().toExternalForm()));
  }

  /**
   * Tests for parameter replacement when multiple ${param} variables are present -- if ${param}
   * is used somewhere within the configured URL, it should be replaced with a value given in the
   * command's XML element attribute (denoted with {@link Command#DYNAMIC_VALUE_ATTR_NAME}
   * attribute) in the HTTP command's builder implementation.
   */
  @Test public void testParameterPlacementMultiple()
  {
    final String parameterizedURL = "http://www.openremote.org/command?param=${param}&another=${param}";
    final String finalURL = "http://www.openremote.org/command?param=10000&another=10000";

    Command cmd = getHttpCommand(parameterizedURL, "10000");

    Assert.assertTrue(cmd instanceof HttpGetCommand);

    HttpGetCommand httpCommand = (HttpGetCommand)cmd;

    Assert.assertTrue(
        "Expected '" + finalURL + "', got '" + httpCommand.getUrl() + "'.",
        "http://www.openremote.org/command?param=10000&another=10000".equals(httpCommand.getUrl().toExternalForm())
    );
  }


  /**
   * Tests parameter replacement is done correctly regardless where it is used in the configured
   * URL -- if ${param} is used somewhere within the configured URL, it should be replaced with a
   * value given in the command's XML element attribute
   * (denoted with {@link Command#DYNAMIC_VALUE_ATTR_NAME} attribute) in the HTTP command's builder
   * implementation.
   */
  @Test public void testParameterPlacementMiddle()
  {
    final String parameterizedURL = "http://www.openremote.org/command?param=${param}&another=foo";
    final String finalURL = "http://www.openremote.org/command?param=XXX&another=foo";

    Command cmd = getHttpCommand(parameterizedURL, "XXX");

    Assert.assertTrue(cmd instanceof HttpGetCommand);

    HttpGetCommand httpCommand = (HttpGetCommand)cmd;


    Assert.assertTrue(
        "Expected '" + finalURL + "', got '" + httpCommand.getUrl() + "'.",
        "http://www.openremote.org/command?param=XXX&another=foo".equals(httpCommand.getUrl().toExternalForm())
    );
  }



  // API Execution Tests --------------------------------------------------------------------------


  /**
   * Test implementation behavior when null args are given to HTTP read command.
   */
  @Test public void testReadValueWithNullSensorType()
  {
    String URL = HTTP_SERVER_URL + "/response/abc";

    StatusCommand command = (StatusCommand) getHttpCommand(URL);

    Assert.assertTrue("abc".equals(command.read(null, null)));
  }


  /**
   * Test send() implementation against an arbitrary URL.
   */
  @Test public void testSendCommand()
  {
    ExecutableCommand command = (ExecutableCommand) getHttpCommand(HTTP_SERVER_URL);

    command.send();
  }

  /**
   * Test send() implementation when an empty URL has been set.
   */
  @Test public void testSendCommandEmptyURL()
  {
    try
    {
      ExecutableCommand command = (ExecutableCommand) getHttpCommand("");

      command.send();

      Assert.fail("should not get here, was expecting a NoSuchCommandException");
    }
    catch (NoSuchCommandException e)
    {
      // expected, do nothing...
    }
  }


  /**
   * Test send() implementation behavior when a malfored URL has been configured.
   */
  @Test public void testSendCommandMalformedURL()
  {
    try
    {
      ExecutableCommand command = (ExecutableCommand) getHttpCommand("foo://bar");

      command.send();

      Assert.fail("should not get here, was expecting a NoSuchCommandException...");
    }

    catch (NoSuchCommandException e)
    {
      // expected, do nothing...
    }
  }


  /**
   * Test send() implementation when URL not found (404) is returned.
   */
  @Test public void testSendCommandNonExistentURL()
  {
    ExecutableCommand command = (ExecutableCommand) getHttpCommand(HTTP_SERVER_URL + "/doesnotexist");

    command.send();
  }


  /**
   * Test send() implementation behavior when HTTP server responds with an error code.
   */
  @Test public void testSendCommandToErrorURL()
  {
    ExecutableCommand command = (ExecutableCommand) getHttpCommand(HTTP_SERVER_URL + "/error/500");

    command.send();
  }



  // Read SWITCH Sensor Related Tests -------------------------------------------------------------


  /**
   * Test read() implementation against 'SWITCH' sensor type.
   */
  @Test public void testReadSwitchStatus()
  {
    StatusCommand cmd = (StatusCommand) getHttpCommand(HTTP_SERVER_URL + "/response/on");

    String response = cmd.read(EnumSensorType.SWITCH, null);

    Assert.assertTrue("Expected response 'on', got '" + response + "'.", "on".equals(response));
  }


  /**
   * Even if distinct state mapping is used for a 'SWITCH' sensor, ignore it for now. <p>
   *
   * Since distinct state mapping behavior is only defined for 'CUSTOM' type of sensors,
   * enforcing that it is ignored for 'SWITCH' types here. A boolean switch state that maps
   * return values to distinct 'on/off' state could be implemented as a CUSTOM sensor instead.
   *
   * If it is convenient to support distinct state mapping for all types of sensors, this test
   * can be modified / removed. The mapping behavior should then be documented and implemented
   * uniformly against all protocol implementations.
   */
  @Test public void testReadSwitchStatusIgnoreMapping()
  {
    StatusCommand cmd = (StatusCommand) getHttpCommand(HTTP_SERVER_URL + "/response/on");

    HashMap<String, String> map = new HashMap<String, String>(3);
    map.put("foo", "on");

    String response = cmd.read(EnumSensorType.SWITCH, map);

    Assert.assertTrue("Expected response 'on', got '" + response + "'.", "on".equals(response));
  }


  /**
   * Even if distinct state mapping is used for a 'SWITCH' sensor, ignore it for now. <p>
   *
   * Since distinct state mapping behavior is only defined for 'CUSTOM' type of sensors,
   * enforcing that it is ignored for 'SWITCH' types here. A boolean switch state that maps
   * return values to distinct 'on/off' state could be implemented as a CUSTOM sensor instead.
   *
   * If it is convenient to support distinct state mapping for all types of sensors, this test
   * can be modified / removed. The mapping behavior should then be documented and implemented
   * uniformly against all protocol implementations.
   */
  @Test public void testReadSwitchStatusDontMap()
  {
    StatusCommand cmd = (StatusCommand) getHttpCommand(HTTP_SERVER_URL + "/response/gaga");

    HashMap<String, String> map = new HashMap<String, String>(3);
    map.put("on", "gaga");

    String response = cmd.read(EnumSensorType.SWITCH, map);

    Assert.assertTrue(
        "Expected response " + HttpGetCommand.UNKNOWN_STATUS + ", got '" + response + "'.",
        HttpGetCommand.UNKNOWN_STATUS.equals(response)
    );
  }



  // Read RANGE Sensor Related Tests --------------------------------------------------------------


  /**
   * Basic read() test on 'RANGE' type of sensor.
   */
  @Test public void testReadRangeStatus()
  {
    StatusCommand cmd = (StatusCommand) getHttpCommand(HTTP_SERVER_URL + "/response/1");

    String response = cmd.read(EnumSensorType.RANGE, null);

    Assert.assertTrue("1".equals(response));
  }


  /**
   * If no explicit range boundaries have been set (see {@link Sensor#RANGE_MAX_STATE} and
   * {@link Sensor#RANGE_MIN_STATE}), assume and enforce range boundaries at Java Integer min
   * and max values.
   */
  @Test public void testReadRangeStatusMaxLimit()
  {
    StatusCommand command = (StatusCommand) getHttpCommand(HTTP_SERVER_URL + "/response/" + Integer.MAX_VALUE);

    String response = command.read(EnumSensorType.RANGE, null);

    Assert.assertTrue(Integer.toString(Integer.MAX_VALUE).equals(response));
  }



  /**
   * If no explicit range boundaries have been set (see {@link Sensor#RANGE_MAX_STATE} and
   * {@link Sensor#RANGE_MIN_STATE}), assume and enforce range boundaries at Java Integer min
   * and max values.
   */
  @Test public void testReadRangeStatusMinLimit()
  {
    StatusCommand command = (StatusCommand) getHttpCommand(HTTP_SERVER_URL + "/response/" + Integer.MIN_VALUE);

    String response = command.read(EnumSensorType.RANGE, null);

    Assert.assertTrue(Integer.toString(Integer.MIN_VALUE).equals(response));
  }


  /**
   * If no explicit range boundaries have been set (see {@link Sensor#RANGE_MAX_STATE} and
   * {@link Sensor#RANGE_MIN_STATE}), assume and enforce range boundaries at Java Integer min
   * and max values. <p>
   *
   * Values crossing the upper bounds should be reduced to fit within the Java integer value bounds.
   */
  @Test public void testReadRangeStatusOutOfUpperBounds()
  {
    StatusCommand command = (StatusCommand) getHttpCommand(HTTP_SERVER_URL + "/response/2147483648");

    String response = command.read(EnumSensorType.RANGE, null);

    Assert.assertTrue(
        "Was expecting range to be bound to max value " + Integer.MAX_VALUE +
        ", got '" + response + "' instead.",
        Integer.toString(Integer.MAX_VALUE).equals(response));
  }


  /**
   * If no explicit range boundaries have been set (see {@link Sensor#RANGE_MAX_STATE} and
   * {@link Sensor#RANGE_MIN_STATE}), assume and enforce range boundaries at Java Integer min
   * and max values. <p>
   *
   * Values crossing the lower bounds should be reduced to fit within the Java integer value bounds.
   */
  @Test public void testReadRangeStatusOutOfLowerBounds()
  {
    StatusCommand command = (StatusCommand) getHttpCommand(HTTP_SERVER_URL + "/response/-2147483649");

    String response = command.read(EnumSensorType.RANGE, null);

    Assert.assertTrue(
        "Was expecting range to be bound to min value " + Integer.MIN_VALUE +
        ", got '" + response + "' instead.",
        Integer.toString(Integer.MIN_VALUE).equals(response));
  }


  /**
   * Even if distinct state mapping is used for a 'RANGE' sensor, ignore it for now. <p>
   *
   * Since distinct state mapping behavior is only defined for 'CUSTOM' type of sensors,
   * enforcing that it is ignored for 'RANGE' types here. While a small range sensor could map
   * each of its distinct states to other return values, this could be implemented as a CUSTOM
   * sensor instead. <p>
   *
   * If it is convenient to support distinct state mapping for RANGE sensors where particular
   * values in the range should have distinct mappings, this test can be modified / removed.
   * The mapping behavior should then be documented and implemented uniformly against all protocol
   * implementations.
   */
  @Test public void testReadRangeStatusIgnoreMapping()
  {
    StatusCommand command = (StatusCommand) getHttpCommand(HTTP_SERVER_URL + "/response/1");

    HashMap<String, String> map = new HashMap<String, String>(3);
    map.put("foo", "1");

    String response = command.read(EnumSensorType.RANGE, map);

    Assert.assertTrue("1".equals(response));
  }


  /**
   * Even if distinct state mapping is used for a 'RANGE' sensor, ignore it for now. <p>
   *
   * Since distinct state mapping behavior is only defined for 'CUSTOM' type of sensors,
   * enforcing that it is ignored for 'RANGE' types here. While a small range sensor could map
   * each of its distinct states to other return values, this could be implemented as a CUSTOM
   * sensor instead. <p>
   *
   * If it is convenient to support distinct state mapping for RANGE sensors where particular
   * values in the range should have distinct mappings, this test can be modified / removed.
   * The mapping behavior should then be documented and implemented uniformly against all protocol
   * implementations.
   */
  @Test public void testReadRangeStatusDoNotMap()
  {
    StatusCommand command = (StatusCommand) getHttpCommand(HTTP_SERVER_URL + "/response/bar");

    HashMap<String, String> map = new HashMap<String, String>(3);
    map.put("1", "bar");

    String response = command.read(EnumSensorType.RANGE, map);

    Assert.assertTrue(
        "Expecting " + HttpGetCommand.UNKNOWN_STATUS + " response, got '" + response + " instead.",
        HttpGetCommand.UNKNOWN_STATUS.equals(response)
    );
  }


  /**
   * When an upper bound limit is set for a 'RANGE' sensor, the return values should be limited
   * to those boundaries.
   */
  @Test public void testRangeSensorExplicitUpperBound()
  {
    StatusCommand command = (StatusCommand) getHttpCommand(HTTP_SERVER_URL + "/response/10");

    HashMap<String, String> map = new HashMap<String, String>();
    map.put(Sensor.RANGE_MAX_STATE, "5");

    map.put("4", "10");                     // this mapping should be ignored by implementation

    String response =  command.read(EnumSensorType.RANGE, map);

    Assert.assertTrue(
        "Expected range bound to return max value of 5, got '" + response + "' instead.",
        "5".equals(response)
    );
  }



  /**
   * When a lower bound limit is set for a 'RANGE' sensor, the return values should be limited
   * to those boundaries.
   */
  @Test public void testRangeSensorExplicitLowerBound()
  {
    StatusCommand command = (StatusCommand) getHttpCommand(HTTP_SERVER_URL + "/response/-10");

    HashMap<String, String> map = new HashMap<String, String>();
    map.put(Sensor.RANGE_MIN_STATE, "-5");
    map.put("-4", "-10");

    String response =  command.read(EnumSensorType.RANGE, map);

    Assert.assertTrue(
        "Expected range bound to return min value of -5, got '" + response + "' instead.",
        "-5".equals(response)
    );
  }


  /**
   * Test the behavior of a misconfigured RANGE sensor where boundaries have been configured
   * to exclude all possible values. <p>
   *
   * The behavior enforced here for such a misconfiguration is that the minimum boundary value
   * should be returned.
   */
  @Test public void testRangeSensorCrossBoundaries()
  {
    StatusCommand command = (StatusCommand) getHttpCommand(HTTP_SERVER_URL + "/response/1");

    HashMap<String, String> map = new HashMap<String, String>();

    map.put(Sensor.RANGE_MIN_STATE, "10");    // minimum is larger than maximum
    map.put(Sensor.RANGE_MAX_STATE, "0");

    String response =  command.read(EnumSensorType.RANGE, map);

    Assert.assertTrue(
        "Expected range bound to return min value of 10, got '" + response + "' instead.",
        "10".equals(response)
    );
  }


  /**
   * Test RANGE sensor behavior when boundaries are configured as equal, making only one value
   * a valid return value.
   */
  @Test public void testRangeSensorEqualBoundaries()
  {
    StatusCommand command = (StatusCommand) getHttpCommand(HTTP_SERVER_URL + "/response/1");

    HashMap<String, String> map = new HashMap<String, String>();
    map.put(Sensor.RANGE_MIN_STATE, "1");
    map.put(Sensor.RANGE_MAX_STATE, "1");

    String response =  command.read(EnumSensorType.RANGE, map);

    Assert.assertTrue(
        "Expected range bound to return min value of 1, got '" + response + "' instead.",
        "1".equals(response)
    );

    command = (StatusCommand)getHttpCommand(HTTP_SERVER_URL + "/response/10");

    response = command.read(EnumSensorType.RANGE, map);

    Assert.assertTrue(
        "Expected range bound to return min value of 1, got '" + response + "' instead.",
        "1".equals(response)
    );

  }


  /**
   * Test a range sensor misconfiguration behavior when ranges have been set above the Java integer
   * max value. Enforcing here for the implementation to defensively default to Integer MAX value.
   */
  @Test public void testRangeSensorOverflowMaxBoundary()
  {
    StatusCommand command = (StatusCommand) getHttpCommand(HTTP_SERVER_URL + "/response/2147483648");

    HashMap<String, String> map = new HashMap<String, String>();
    map.put(Sensor.RANGE_MIN_STATE, "1");
    map.put(Sensor.RANGE_MAX_STATE, "2147483648");

    String response =  command.read(EnumSensorType.RANGE, map);

    Assert.assertTrue(
        "Expected range bound to return max value of 2147483647, got '" + response + "' instead.",
        "2147483647".equals(response)
    );
  }


  /**
   * Test a range sensor misconfiguration behavior when range has been set below the Java integer
   * minimum value. Enforcing the implementation to defensively default to Integer MIN value.
   */
  @Test public void testRangeSensorOverflowMinBoundary()
  {
    StatusCommand command = (StatusCommand) getHttpCommand(HTTP_SERVER_URL + "/response/-2147483649");

    HashMap<String, String> map = new HashMap<String, String>();
    map.put(Sensor.RANGE_MIN_STATE, "-2147483649");
    map.put(Sensor.RANGE_MAX_STATE, "1");

    String response =  command.read(EnumSensorType.RANGE, map);

    Assert.assertTrue(
        "Expected range bound to return min value of -2147483648, got '" + response + "' instead.",
        "-2147483648".equals(response)
    );
  }


  /**
   * Test behavior against incorrect API use (internal implementation), if range minimum value
   * has been set to unparseable number.
   */
  @Test public void testRangeSensorInvalidMinBoundary()
  {
    StatusCommand command = (StatusCommand) getHttpCommand(HTTP_SERVER_URL + "/response/-2147483649");

    HashMap<String, String> map = new HashMap<String, String>();
    map.put(Sensor.RANGE_MIN_STATE, "foo");
    map.put(Sensor.RANGE_MAX_STATE, "1");

    String response =  command.read(EnumSensorType.RANGE, map);

    Assert.assertTrue(
        "Expected range bound to return min value of -2147483648, got '" + response + "' instead.",
        "-2147483648".equals(response)
    );
  }


  /**
   * Test behavior against incorrect API use (internal implementation), if range max value is
   * set to unpaseable number.
   */
  @Test public void testRangeSensorInvalidMaxBoundary()
  {
    StatusCommand command = (StatusCommand) getHttpCommand(HTTP_SERVER_URL + "/response/2147483648");

    HashMap<String, String> map = new HashMap<String, String>();
    map.put(Sensor.RANGE_MIN_STATE, "0");
    map.put(Sensor.RANGE_MAX_STATE, "bar");

    String response =  command.read(EnumSensorType.RANGE, map);

    Assert.assertTrue(
        "Expected range bound to return max value of 2147483647, got '" + response + "' instead.",
        "2147483647".equals(response)
    );
  }


  // Read LEVEL Sensor Tests ----------------------------------------------------------------------


  /**
   * Basic read() test on 'LEVEL' sensor type.
   */
  @Test public void testReadLevelStatus()
  {
    StatusCommand command = (StatusCommand) getHttpCommand(HTTP_SERVER_URL + "/response/1");

    String response = command.read(EnumSensorType.LEVEL, null);

    Assert.assertTrue("1".equals(response));
  }


  /**
   * Test LEVEL sensor behavior when return value is above LEVEL implicit range of [0-100]. <p>
   *
   * Value is expected to be truncated within the LEVEL range.
   */
  @Test public void testLevelOverMaxBoundary()
  {
    StatusCommand command = (StatusCommand) getHttpCommand(HTTP_SERVER_URL + "/response/101");

    String response = command.read(EnumSensorType.LEVEL, null);

    Assert.assertTrue(
        "Expected level over max boundary to return 100, got '" + response + "' instead.",
        "100".equals(response)
    );
  }


  /**
   * Test LEVEL sensor behavior when return value is below LEVEL implicit range of [0-100]. <p>
   *
   * Value is expected to be truncated within the LEVEL range.
   */
  @Test public void testLevelBelowMinBoundary()
  {
    StatusCommand command = (StatusCommand) getHttpCommand(HTTP_SERVER_URL + "/response/-1");

    String response = command.read(EnumSensorType.LEVEL, null);

    Assert.assertTrue(
        "Expected level below min boundary to return 0, got '" + response + "' instead.",
        "0".equals(response)
    );
  }


  /**
   * Test LEVEL sensor behavior when explicit ranges have been configured. These ought to be
   * ignored since LEVEL sensor has always a range of [0-100].
   */
  @Test public void testReadLevelStatusIgnoreRange()
  {
    StatusCommand command = (StatusCommand) getHttpCommand(HTTP_SERVER_URL + "/response/100");

    HashMap<String, String> map = new HashMap<String, String>();
    map.put(Sensor.RANGE_MAX_STATE, "35");
    map.put(Sensor.RANGE_MIN_STATE, "-10");

    String response = command.read(EnumSensorType.LEVEL, map);

    Assert.assertTrue(
        "Expected response '100', got '" + response + "' instead.",
        "100".equals(response)
    );

    command = (StatusCommand)getHttpCommand(HTTP_SERVER_URL + "/response/0");

    response = command.read(EnumSensorType.LEVEL, map);

    Assert.assertTrue(
        "Expected response '0', got '" + response + "' instead.",
        "0".equals(response)
    );


    command = (StatusCommand)getHttpCommand(HTTP_SERVER_URL + "/response/101");

    response = command.read(EnumSensorType.LEVEL, map);

    Assert.assertTrue(
        "Expected response '100' to over boundary value, got '" + response + "' instead.",
        "100".equals(response)
    );



    command = (StatusCommand)getHttpCommand(HTTP_SERVER_URL + "/response/-1");

    response = command.read(EnumSensorType.LEVEL, map);

    Assert.assertTrue(
        "Expected response '0' to below boundary value, got '" + response + "' instead.",
        "0".equals(response)
    );
  }


  /**
   * Even if distinct state mapping is used for a 'LEVEL' sensor, ignore it for now. <p>
   *
   * Since distinct state mapping behavior is only defined for 'CUSTOM' type of sensors,
   * enforcing that it is ignored for 'LEVEL' types here. While a level sensor could map
   * each of its distinct states to other return values, this could be implemented as a CUSTOM
   * sensor instead. <p>
   *
   * If it is convenient to support distinct state mapping for LEVEL sensors where particular
   * values in the range should have distinct mappings, this test can be modified / removed.
   * The mapping behavior should then be documented and implemented uniformly against all protocol
   * implementations.
   */
  @Test public void testReadLevelStatusIgnoreMapping()
  {
    StatusCommand command = (StatusCommand) getHttpCommand(HTTP_SERVER_URL + "/response/1");

    HashMap<String, String> map = new HashMap<String, String>(3);
    map.put("foo", "1");

    String response = command.read(EnumSensorType.LEVEL, map);

    Assert.assertTrue("1".equals(response));
  }


  /**
   * Even if distinct state mapping is used for a 'LEVEL' sensor, ignore it for now. <p>
   *
   * Since distinct state mapping behavior is only defined for 'CUSTOM' type of sensors,
   * enforcing that it is ignored for 'LEVEL' types here. While a level sensor could map
   * each of its distinct states to other return values, this could be implemented as a CUSTOM
   * sensor instead. <p>
   *
   * If it is convenient to support distinct state mapping for LEVEL sensors where particular
   * values in the range should have distinct mappings, this test can be modified / removed.
   * The mapping behavior should then be documented and implemented uniformly against all protocol
   * implementations.
   */
  @Test public void testReadLevelStatusDoNotMap()
  {
    StatusCommand command = (StatusCommand) getHttpCommand(HTTP_SERVER_URL + "/response/bar");

    HashMap<String, String> map = new HashMap<String, String>(3);
    map.put("1", "bar");

    String response = command.read(EnumSensorType.RANGE, map);

    Assert.assertTrue(
        "Expected " + HttpGetCommand.UNKNOWN_STATUS + ", got '" + response + "' instead.",
        HttpGetCommand.UNKNOWN_STATUS.equals(response)
    );
  }





//  @Test public void testReadColorStatus() throws Exception
//  {
//    //TODO not clear what color really is.
//    httpServer.setHandler(new Handler("#000000"));
//    httpServer.start();
//
//    StatusCommand cmd = (StatusCommand) getHttpCommand(HTTP_SERVER_URL);
//
//    Assert.assertEquals("#000000", cmd.read(EnumSensorType.COLOR, null));
//  //      Assert.assertEquals("black", cmd.read(EnumSensorType.COLOR, null));
//
//  }



  // Distinct Sensor State Mapping ----------------------------------------------------------------

  /**
   * Test basic 'CUSTOM' sensor type where return values are mapped to specific distinct state
   * values.
   */
  @Test public void testDistinctStateMapping()
  {
    StatusCommand command = (StatusCommand) getHttpCommand(HTTP_SERVER_URL + "/response/return_value_on");

    HashMap<String, String> map = new HashMap<String, String>();
    map.put("dim0", "light1_dim0");
    map.put("dim30", "light1_dim30");
    map.put("dim50", "light1_dim50");
    map.put("dim70", "light1_dim70");
    map.put("dim100", "light1_dim100");
    map.put("on", "return_value_on");
    map.put("off", "light1_off");

    String response = command.read(EnumSensorType.CUSTOM, map);

    Assert.assertTrue("on".equals(response));

    command = (StatusCommand)getHttpCommand(HTTP_SERVER_URL + "/response/light1_off");

    response = command.read(EnumSensorType.CUSTOM, map);

    Assert.assertTrue("off".equals(response));

    command = (StatusCommand)getHttpCommand(HTTP_SERVER_URL + "/response/light1_dim0");

    response = command.read(EnumSensorType.CUSTOM, map);

    Assert.assertTrue("dim0".equals(response));


    command = (StatusCommand)getHttpCommand(HTTP_SERVER_URL + "/response/no_mapping");

    response = command.read(EnumSensorType.CUSTOM, map);

    Assert.assertTrue("no_mapping".equals(response));

  }



  // Helpers --------------------------------------------------------------------------------------


  private Command getParameterizedHttpCommand(String url, int paramValue)
  {
    return getHttpCommand(url + "${param}", "" + paramValue);
  }

  private Command getHttpCommand(String url)
  {
    return getHttpCommand(url, null);
  }

  private Command getHttpCommand(String url, String paramValue)
  {
    Element ele = new Element("command");
    ele.setAttribute("id", "test");
    ele.setAttribute("protocol", "httpGet");

    if (paramValue != null)
    {
      ele.setAttribute(Command.DYNAMIC_VALUE_ATTR_NAME, paramValue);
    }
    
    Element propName = new Element("property");
    propName.setAttribute("name", "name");
    propName.setAttribute("value", "commandname");

    Element propUrl = new Element("property");
    propUrl.setAttribute("name", "url");
    propUrl.setAttribute("value", url);

    ele.addContent(propName);
    ele.addContent(propUrl);

    return builder.build(ele);
  }



  // Nested Classes -------------------------------------------------------------------------------

  private static class HttpServerResponse extends AbstractHandler
  {


    public void handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch)
         throws IOException, ServletException
    {
      if (target.startsWith("/doesnotexist"))
      {
        response.sendError(404);
      }

      else if (target.startsWith("/error/"))
      {
        target = target.substring(7);

        response.sendError(Integer.parseInt(target));
      }
      
      else
      {
        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);

        if (target.startsWith("/response/"))
        {
          target = target.substring(10, target.length());
          response.getWriter().print(target);
          response.getWriter().flush();
        }
      }
      
      ((Request) request).setHandled(true);
    }

  }

}


