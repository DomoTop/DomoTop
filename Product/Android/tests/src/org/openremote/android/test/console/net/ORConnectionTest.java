/*
 * OpenRemote, the Home of the Digital Home.
 * Copyright 2008-2010, OpenRemote Inc.
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
package org.openremote.android.test.console.net;

import java.net.HttpURLConnection;
import java.net.UnknownHostException;
import java.net.MalformedURLException;
import java.io.IOException;
import java.util.Random;
import java.util.HashSet;
import java.util.Set;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.openremote.android.console.net.ORConnection;
import org.openremote.android.console.net.ORHttpMethod;
import org.openremote.android.console.AppSettingsActivity;
import org.apache.http.HttpResponse;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.NamedNodeMap;
import org.xml.sax.SAXException;
import android.test.ActivityInstrumentationTestCase2;
import android.app.Activity;

/**
 * Tests for {@link ORConnection} class.
 *
 * TODO:
 *   Strictly speaking these are integration tests, rather than unit tests, operating against
 *   public controller instances. Haven't separated out unit vs. integration tests as separate
 *   targets yet. These tests can take little time to finish, although I've tried to keep time
 *   spent minimal.
 *                                                                                      [JPL]
 *
 *
 * @author <a href="mailto:juha@openremote.org">Juha Lindfors</a>
 */
public class ORConnectionTest extends ActivityInstrumentationTestCase2<AppSettingsActivity>
{

  // Constants ------------------------------------------------------------------------------------

  /**
   * Controller 2.0 REST/XML URI for listing all available panel ids in the controller
   */
  public final static String RESTAPI_PANEL_URI = "/rest/panels";

  /**
   * Controller 2.0 REST/XML URI for retrieving panel by id from the controller.
   */
  public final static String RESTAPI_FULLPANEL_URI = "/rest/panel/";

  /**
   * Controller 2.0 REST/XML URI for write commands to the controller.
   */
  public final static String RESTAPI_CONTROL_URI = "/rest/control/";

  /**
   * Controller 2.0 REST/XML URI for read commands.
   */
  public final static String RESTAPI_STATUS_URI = "/rest/status/";

  /**
   * Don't authenticate against controllers.
   */
  public final static boolean NO_HTTP_AUTH = false;

  /**
   * MIME type for HTTP content-type header
   */
  public final static String TEXTHTML_MIME_TYPE = "text/html";

  /**
   * MIME type for HTTP content-type header
   */
  public final static String APPLICATIONXML_MIME_TYPE = "application/xml";



  /**
   *
   */
  public static final String TEST_CONTROLLER_URL = "http://controller.openremote.org/test/controller";
  public static final String TEST_CMDERRORS_CONTROLLER_URL = "http://controller.openremote.org/cmderrors/controller";
  public static final String TEST_EMPTY_CONTROLLER_URL = "http://controller.openremote.org/empty/controller";
  public static final String TEST_BROKEN_CONTROLLER_URL = "http://controller.openremote.org/broken/controller";


  /**
   * Command param for HTTP assertions to skip charset check due to existing bugs in Controller
   * REST/XML implementation.
   */
  public final static boolean SKIP_CHARSET_CHECK = true;

  /**
   * Command param for HTTP assertions to explicitly ensure UTF-8 character encoding in responses.
   */
  public final static boolean ASSERT_UTF8_ENCODING = !SKIP_CHARSET_CHECK;


  public final static boolean ASSERT_NON_ZERO_BODY_LENGTH = true;
  public final static boolean DO_NOT_CHECK_BODY_LENGTH = !ASSERT_NON_ZERO_BODY_LENGTH;


  public final static int NO_HTTP_BODY = 0;




  // Instance Fields ------------------------------------------------------------------------------

  /**
   * Reference to the activity used to back the tests.
   */
  private Activity activity;


  // Constructors ---------------------------------------------------------------------------------

  public ORConnectionTest()
  {
    super("org.openremote.android.console", AppSettingsActivity.class);
  }




  // Test Setup -----------------------------------------------------------------------------------

  @Override public void setUp()
  {
    try
    {
      super.setUp();
    }
    catch (Throwable t)
    {
      fail ("Test setup failed in ActivityInstrumentationTestCase2: " + t.getMessage());
    }

    this.activity = getActivity();
  }



  // Tests ----------------------------------------------------------------------------------------



  /**
   * Tests a basic GET through {@link ORConnection#checkURLWithHTTPProtocol} method. Attempts to
   * reach the welcome page of a publicly deployed controller.
   *
   * @throws IOException see checkURLWithHTTPProtocol javadoc for details
   */
  public void testURLConnectionBasicGET() throws IOException
  {
    HttpResponse response = ORConnection.checkURLWithHTTPProtocol(
        activity, ORHttpMethod.GET, TEST_CONTROLLER_URL, NO_HTTP_AUTH
    );

    assertNotNullResponse(response, TEST_CONTROLLER_URL);
    assertHttpReturnCode(response, HttpURLConnection.HTTP_OK);
    assertNotZeroResponseBody(response);
    assertHttpContentType(response, TEXTHTML_MIME_TYPE);
  }


  /**
   * Tests a GET behavior on an unknown (no DNS resolution) host.
   *
   * @throws IOException if connection fails for any reason, see checkURLWithHTTPProtocol javadoc
   */
  public void testURLConnectionUnknownHost() throws IOException
  {
    final String NOHOST_TESTURL = "http://controller.openremotetest.org/test/controller";

    try
    {
      ORConnection.checkURLWithHTTPProtocol(
          activity, ORHttpMethod.GET, NOHOST_TESTURL, NO_HTTP_AUTH
      );

      fail ("Should not get here...");
    }
    catch (UnknownHostException e)
    {
      // expected unknown host exception...
    }
  }


  /**
   * Test null arg behavior...
   *
   * @throws IOException if connection fails for any reason, see checkURLWithHTTPProtocol javadoc
   */
  public void testURLConnectionWithNullArg() throws IOException
  {
    final String TESTURL = null;

    try
    {
      ORConnection.checkURLWithHTTPProtocol(
          activity, ORHttpMethod.GET, TESTURL, NO_HTTP_AUTH
      );

      fail ("Should not get here...");
    }
    catch (MalformedURLException e)
    {
      // expected malformed URL exception...
    }
  }


  /**
   * Test empty string arg behavior...
   *
   * @throws IOException if connection fails for any reason, see checkURLWithHTTPProtocol javadoc
   */
  public void testURLConnectionWithEmptyArg() throws IOException
  {
    final String TESTURL = "";

    try
    {
      ORConnection.checkURLWithHTTPProtocol(
          activity, ORHttpMethod.GET, TESTURL, NO_HTTP_AUTH
      );

      fail ("Should not get here...");
    }
    catch (MalformedURLException e)
    {
      // expected malformed URL exception...
    }
  }

  /**
   * Test invalid URL arg behavior...
   *
   * @throws IOException if connection fails for any reason, see checkURLWithHTTPProtocol javadoc
   */
  public void testURLConnectionWithRandomString() throws IOException
  {
    final String TESTURL = "random";

    try
    {
      ORConnection.checkURLWithHTTPProtocol(
          activity, ORHttpMethod.GET, TESTURL, NO_HTTP_AUTH
      );

      fail ("Should not get here...");
    }
    catch (MalformedURLException e)
    {
      // expected malformed URL exception...
    }
  }


  /**
   * Test malformed URL arg behavior...
   *
   * @throws IOException if connection fails for any reason, see checkURLWithHTTPProtocol javadoc
   */
  public void testURLConnectionWithMissingProtocol() throws IOException
  {
    final String NO_PROTOCOL_SCHEME_TESTURL = "controller.openremote.org/test/controller";

    try
    {
      ORConnection.checkURLWithHTTPProtocol(
          activity, ORHttpMethod.GET, NO_PROTOCOL_SCHEME_TESTURL, NO_HTTP_AUTH
      );

      fail ("Should not get here...");
    }
    catch (MalformedURLException e)
    {
      // expected malformed URL exception...
    }
  }


  /**
   * Test malformed URL arg behavior...
   *
   * @throws IOException if connection fails for any reason, see checkURLWithHTTPProtocol javadoc
   */
  public void testURLConnectionWithUnknownProtocol() throws IOException
  {
    final String INVALID_PROTOCOL_TESTURL = "foo://controller.openremote.org/test/controller";

    try
    {
      ORConnection.checkURLWithHTTPProtocol(
          activity, ORHttpMethod.GET, INVALID_PROTOCOL_TESTURL, NO_HTTP_AUTH
      );

      fail ("Should not get here...");
    }
    catch (MalformedURLException e)
    {
      // expected malformed URL exception...
    }
  }


  /**
   * Test non-existent URL path behavior...
   *
   * @throws IOException if connection fails for any reason, see checkURLWithHTTPProtocol javadoc
   */
  public void testURLConnectionWithWrongURLPath() throws IOException
  {
    final String NOT_EXIST_TESTURL = "http://controller.openremote.org/test/controller/does/not/exist";

    HttpResponse response = ORConnection.checkURLWithHTTPProtocol(
        activity, ORHttpMethod.GET, NOT_EXIST_TESTURL, NO_HTTP_AUTH
    );

    assertNotNullResponse(response, NOT_EXIST_TESTURL);

    assertHttpReturnCode(response, HttpURLConnection.HTTP_NOT_FOUND);
  }



  // Tests against Controller 2.0 REST/XML API ----------------------------------------------------



  //
  // TODO:
  //
  //  The following tests validate the REST/XML API assumptions from the point of view of the
  //  Android client.
  //
  //  These tests are somewhat redundant to unit tests in the Controller that should (ideally)
  //  ensure that the REST API is implemented correctly. However, testing the API from the
  //  client side is useful for documenting the expected version of API the client assumes,
  //  and they can therefore be used to test client compatibility against different versions of
  //  Controller, as the API evolves (therefore, these are also integration tests, rather than
  //  unit tests).
  //
  //  The current version of Controller available at the time of writing of these tests did not
  //  implement the REST/XML API completely and correctly. Some of the following tests point out
  //  these issues. These particular tests that document incorrect REST/XML API implementation
  //  should phase out once the controller becomes release-quality.





  /**
   * Test basic get of list of panels from public controller instance -- expects two pre-configured
   * panels to be returned.
   *
   * @throws IOException if connection fails for any reason, see checkURLWithHTTPProtocol javadoc
   * @throws ParserConfigurationException if DOM parsing fails on return data
   * @throws SAXException if parsing fails on return data
   */
  public void testControllerGETRestPanelXML() throws IOException, ParserConfigurationException, SAXException
  {
    final String TESTURL = TEST_CONTROLLER_URL + RESTAPI_PANEL_URI;


    HttpResponse response = ORConnection.checkURLWithHTTPProtocol(
        activity, ORHttpMethod.GET, TESTURL, NO_HTTP_AUTH
    );

    assertHttpResponse(response, TESTURL, HttpURLConnection.HTTP_OK);


    // TODO :
    //   This should be fixed in Controller 2.0 Alpha 12 -- uncomment when online test controller
    //   has been upgraded (currently returns 'text/plain')
    //
    try
    {
      assertMimeType(response, APPLICATIONXML_MIME_TYPE);

      fail("\n\nIncorrect content-type issue has been fixed, please update this test.\n\n");
    }
    catch (Throwable t)
    {
      // TODO: Ignore for now, remove the check once content-type issue has been fixed.
    }

    Document doc = getDOMDocument(response);

    assertOpenRemoteRootElement(doc);

    NodeList list = doc.getElementsByTagName("panel");

    assertTrue(list.getLength() >= 2);

    Map<String, String> panels = new HashMap<String, String>(3);

    for (int panelIndex = 0; panelIndex < list.getLength(); ++panelIndex)
    {
      Node panel = list.item(panelIndex);
      NamedNodeMap attrs = panel.getAttributes();

      Node id = attrs.getNamedItem("id");
      String idVal = id.getNodeValue();

      Node name = attrs.getNamedItem("name");
      String nameVal = name.getNodeValue();

      panels.put(idVal, nameVal);
    }

    assertNotNull("Expected panel id '1' but that was not found.", panels.get("1"));
    assertNotNull("Expected panel id '5' but that was not found.", panels.get("5"));

    assertTrue(
        "Expected panel id=1 with name 'SimpleName', got " + panels.get("1"),
        panels.get("1").equalsIgnoreCase("SimpleName")
    );

    assertTrue(
        "Expected panel id=5 with name 'Name With Space', got " + panels.get("5"),
        panels.get("5").equalsIgnoreCase("Name With Spaces")
    );
  }

  /**
   * TODO:
   *
   *  This test points out incorrectly implemented Controller REST/XML API -- returned content
   *  type should be application/xml. Once the issue has been fixed, this test can be removed.
   *
   * @throws IOException if connection fails for any reason, see checkURLWithHTTPProtocol javadoc
   */
  public void testKnownBugAgainstController_2_0_Alpha11_RESTAPI() throws IOException
  {
    final String TESTURL = TEST_CONTROLLER_URL + RESTAPI_PANEL_URI;

    HttpResponse response = ORConnection.checkURLWithHTTPProtocol(
        activity, ORHttpMethod.GET, TESTURL, NO_HTTP_AUTH
    );

    // TODO :
    //   once the issue with incorrect content-type header is fixed, this test can be removed
    //   and the correct assertions in other tests can be made
    //                                                                                [JPL]
    //
    assertHttpResponse(response, TESTURL, HttpURLConnection.HTTP_OK, APPLICATIONXML_MIME_TYPE);
  }



  /**
   * Asserts the panel XML definition contains all the necessary elements
   * for the integration tests.
   *
   * @throws IOException if connection fails for any reason, see checkURLWithHTTPProtocol javadoc
   * @throws ParserConfigurationException if DOM parsing fails on return data
   * @throws SAXException if parsing fails on return data
   */
  public void testControllerGETRestFullPanelXML() throws IOException, ParserConfigurationException, SAXException
  {
    final String TESTURL = TEST_CONTROLLER_URL + RESTAPI_FULLPANEL_URI + "SimpleName";

    HttpResponse response = ORConnection.checkURLWithHTTPProtocol(
        activity, ORHttpMethod.GET, TESTURL, NO_HTTP_AUTH
    );

    assertHttpResponse(response, TESTURL, HttpURLConnection.HTTP_OK);

    // TODO :
    //   This should be fixed in Controller 2.0 Alpha 12 -- uncomment when online test controller
    //   has been upgraded (currently returns 'text/plain')
    //
    try
    {
      assertMimeType(response, APPLICATIONXML_MIME_TYPE);

      fail("\n\nIncorrect content-type issue has been fixed, please update this test.\n\n");
    }
    catch (Throwable t)
    {
      // TODO: Ignore for now, remove the check once content-type issue has been fixed.
    }


    Document doc = getDOMDocument(response);

    assertOpenRemoteRootElement(doc);

    NodeList list = doc.getElementsByTagName("screens");

    assertTrue("Expected one <screens> element in panel definition.", list.getLength() == 1);

    list = doc.getElementsByTagName("groups");

    assertTrue("Expected one <groups> element in panel definition.", list.getLength() == 1);

    list = doc.getElementsByTagName("group");

    assertTrue("Expected at least one group in the panel definition.", list.getLength() >= 1);

    Set<String> screenReferences = new HashSet<String>(10);

    for (int groupIndex = 0; groupIndex < list.getLength(); ++groupIndex)
    {
      Node group = list.item(groupIndex);

      if (group.getNodeType() != Node.ELEMENT_NODE)
        continue;

      assertTrue("Expected <group> to have child elements, found none.", group.hasChildNodes());

      NodeList includes = group.getChildNodes();

      assertTrue("Expected at least one included screen in the group.", includes.getLength() >= 1);

      for (int includeIndex = 0; includeIndex < includes.getLength(); ++includeIndex)
      {
        Node include = includes.item(includeIndex);

        if (include.getNodeType() != Node.ELEMENT_NODE)
          continue;
        
        NamedNodeMap attrs = include.getAttributes();

        assertNotNull("Expected to find attributes in include", attrs);
        assertNotNull("Expected to find 'ref' attribute in <include>, got null", attrs.getNamedItem("ref"));
        
        Node ref = attrs.getNamedItem("ref");

        String screenRef = ref.getNodeValue();

        screenReferences.add(screenRef);
      }
    }

    list = doc.getElementsByTagName("screen");

    Set<String> screenIDs = new HashSet<String>(10);

    assertTrue("Expected at least one <screen> in panel definition.", list.getLength() >= 1);

    for (int screenIndex = 0; screenIndex < list.getLength(); ++screenIndex)
    {
      Node screen = list.item(screenIndex);

      NamedNodeMap attrs = screen.getAttributes();

      assertNotNull("Expected to find 'id' attribute in screen, not null.", attrs.getNamedItem("id"));

      Node id = attrs.getNamedItem("id");

      screenIDs.add(id.getNodeValue());
    }


    for (String ref : screenReferences)
    {
      assertTrue("Expected to find screen with id: " + ref, screenIDs.contains(ref));
    }


    list = doc.getElementsByTagName("button");

    assertTrue(
        "Expected at least two <button> elements, got " + list.getLength(),
        list.getLength() >= 2
    );

    Set<String> buttonIDs = new HashSet<String>(10);
    
    for (int btnIndex = 0; btnIndex < list.getLength(); ++btnIndex)
    {
      Node button = list.item(btnIndex);

      NamedNodeMap attrs = button.getAttributes();

      assertNotNull("Expected to find attributes in <button>, got null.", attrs);

      assertNotNull("Expected to find id attribute in <button>.", attrs.getNamedItem("id"));

      Node id = attrs.getNamedItem("id");

      buttonIDs.add(id.getNodeValue());
    }

    assertTrue("Expected to find a button with ID=22", buttonIDs.contains("22"));
    assertTrue("Expected to find a button with ID=24", buttonIDs.contains("24"));


    list = doc.getElementsByTagName("switch");

    assertTrue(
        "Expected at least one <switch> element, got " + list.getLength(),
        list.getLength() >= 1
    );

    Set<String> switchIDs = new HashSet<String>(10);
    Set<String> sensorReferences = new HashSet<String>(10);

    for (int switchIndex = 0; switchIndex < list.getLength(); ++switchIndex)
    {
      Node switchComp = list.item(switchIndex);

      NamedNodeMap attrs = switchComp.getAttributes();

      assertNotNull("Expected to find attributes in <switch>, got null.", attrs);

      assertNotNull("Expected to find id attribute in <switch>.", attrs.getNamedItem("id"));

      Node id = attrs.getNamedItem("id");

      switchIDs.add(id.getNodeValue());

      NodeList links = switchComp.getChildNodes();


      for (int linksIndex = 0; linksIndex < links.getLength(); ++linksIndex)
      {
        Node link = links.item(linksIndex);

        if (link.getNodeType() != Node.ELEMENT_NODE)
          continue;

        if (!link.getNodeName().equalsIgnoreCase("link"))
          continue;

        NamedNodeMap linkAttrs = link.getAttributes();

        assertNotNull("Expected attributes on <link>, got null.", linkAttrs);

        Node typeAttr = linkAttrs.getNamedItem("type");

        if (typeAttr.getNodeValue().equalsIgnoreCase("sensor"))
        {
          Node refAttr = linkAttrs.getNamedItem("ref");

          assertNotNull("Expected a 'ref' attribute in 'sensor' link, didn't find it.", refAttr);

          sensorReferences.add(refAttr.getNodeValue());
        }
      }
    }

    assertTrue("Expected to find a switch with ID=28", switchIDs.contains("28"));
    assertTrue("Expected to find sensor link with ID=29", sensorReferences.contains("29"));
  }


  /**
   * Tests the HTTP response code on request for panel list against controller that has no
   * configuration deployed.
   *
   * @throws IOException if connection fails for any reason, see checkURLWithHTTPProtocol javadoc
   */
  public void testControllerGETRestPanelXML_EmptyController() throws IOException
  {
    final String TESTURL = TEST_EMPTY_CONTROLLER_URL + RESTAPI_PANEL_URI;

    HttpResponse response = ORConnection.checkURLWithHTTPProtocol(
        activity, ORHttpMethod.GET, TESTURL, NO_HTTP_AUTH
    );

    // TODO :
    //   Returned HTTP headers specify 'utf8' instead of 'utf-8' as the charset encoding -- this
    //   test skips the assertion. The buggy charset header value is triggered by another test.
    //   Once the issue has been fixed (should be in Controller 2.0 Alpha 12), remove the
    //   SKIP_CHARSET_CHECK parameter.
    //                                                                                [JPL]
    //
    assertHttpResponse(response, TESTURL, 426, SKIP_CHARSET_CHECK);

    try
    {
      assertHttpResponse(response, TESTURL, 426);

      fail("\n\nIt appears the charset bug has been addressed, please update this test.\n\n");
    }
    catch (Throwable t)
    {
      // TODO: Ignore this until the charset issue has been fixed...
    }
  }

  /**
   * TODO:
   *
   *   This test points out incorrect character encoding against Controller 2.0 Alpha 11.
   *   Test can be removed once the controller REST/XML API has been fixed (fix should
   *   be in Alpha 12).
   *
   * @throws IOException if connection fails for any reason, see checkURLWithHTTPProtocol javadoc
   */
  public void testControllerGETRestPanelXML_EmptyController_BROKEN_CHARSET_ENCODING() throws IOException
  {
    final String TESTURL = TEST_EMPTY_CONTROLLER_URL + RESTAPI_PANEL_URI;

    HttpResponse response = ORConnection.checkURLWithHTTPProtocol(
        activity, ORHttpMethod.GET, TESTURL, NO_HTTP_AUTH
    );

    assertHttpResponse(response, TESTURL, 426);

    fail("\n\nAppears the character encoding issue has been fixed. This test can be removed.\n\n");
  }


  /**
   * Tests the HTTP response code on request for panel list against controller that has a malformed
   * configuration deployed
   *
   * @throws IOException if connection fails for any reason, see checkURLWithHTTPProtocol javadoc
   */
  public void testControllerGETRestPanelXML_BrokenPanelXML() throws IOException
  {
    final String TESTURL = TEST_BROKEN_CONTROLLER_URL + RESTAPI_PANEL_URI;

    HttpResponse response = ORConnection.checkURLWithHTTPProtocol(
        activity, ORHttpMethod.GET, TESTURL, NO_HTTP_AUTH
    );

    // TODO :
    //   Returned HTTP headers specify 'utf8' instead of 'utf-8' as the charset encoding -- this
    //   test skips the assertion. The buggy charset header value is triggered by another test.
    //   Once the issue has been fixed (should be in Controller 2.0 Alpha 12), remove the
    //   SKIP_CHARSET_CHECK parameter.
    //                                                                                [JPL]
    //
    assertHttpResponse(response, TESTURL, 424, SKIP_CHARSET_CHECK);

    try
    {
      assertHttpResponse(response, TESTURL, 424);

      fail("\n\nIt appears the charset bug has been addressed, please update this test.\n\n");
    }
    catch (Throwable t)
    {
      // TODO: Ignore this until the charset issue has been fixed...
    }
  }

  /**
   * TODO:
   *
   *   This test points out incorrect character encoding against Controller 2.0 Alpha 11.
   *   Test can be removed once the controller REST/XML API has been fixed (fix should
   *   be in Alpha 12).
   *
   * @throws IOException if connection fails for any reason, see checkURLWithHTTPProtocol javadoc
   */
  public void testControllerGETRestPanelXML_BrokenPanelXML_BROKEN_CHARSET_ENCODING() throws IOException
  {
    final String TESTURL = TEST_BROKEN_CONTROLLER_URL + RESTAPI_PANEL_URI;

    HttpResponse response = ORConnection.checkURLWithHTTPProtocol(
        activity, ORHttpMethod.GET, TESTURL, NO_HTTP_AUTH
    );

    assertHttpResponse(response, TESTURL, 424);

    fail("\n\nAppears the character encoding issue has been fixed. This test can be removed.\n\n");
  }


  /**
   * Tests the HTTP response code when requesting panel definition by name that does not exist.
   *
   * @throws IOException if connection fails for any reason, see checkURLWithHTTPProtocol javadoc
   */
  public void testControllerGETRestFullPanelXML_NotExist() throws IOException
  {
    final String TESTURL = TEST_CONTROLLER_URL + RESTAPI_FULLPANEL_URI + "DoesNotExist";


    HttpResponse response = ORConnection.checkURLWithHTTPProtocol(
        activity, ORHttpMethod.GET, TESTURL, NO_HTTP_AUTH
    );

    // TODO :
    //   Returned HTTP headers specify 'utf8' instead of 'utf-8' as the charset encoding -- this
    //   test skips the assertion. The buggy charset header value is triggered by another test.
    //   Once the issue has been fixed (should be in Controller 2.0 Alpha 12), remove the
    //   SKIP_CHARSET_CHECK parameter.
    //                                                                                [JPL]
    //
    assertHttpResponse(response, TESTURL, 428, SKIP_CHARSET_CHECK);

    try
    {
      assertHttpResponse(response, TESTURL, 428);

      fail("\n\nIt appears the charset bug has been addressed, please update this test.\n\n");
    }
    catch (Throwable t)
    {
      // TODO: Ignore this until the charset issue has been fixed...
    }
  }


  /**
   * TODO:
   *
   *   This test points out incorrect character encoding against Controller 2.0 Alpha 11.
   *   Test can be removed once the controller REST/XML API has been fixed (fix should
   *   be in Alpha 12).
   *
   * @throws IOException if connection fails for any reason, see checkURLWithHTTPProtocol javadoc
   */
  public void testControllerGETRestFullPanelXML_NotExist_BROKEN_CHARSET_ENCODING() throws IOException
  {
    final String TESTURL = TEST_CONTROLLER_URL + RESTAPI_FULLPANEL_URI + "DoesNotExist";

    HttpResponse response = ORConnection.checkURLWithHTTPProtocol(
        activity, ORHttpMethod.GET, TESTURL, NO_HTTP_AUTH
    );

    assertHttpResponse(response, TESTURL, 428);

    fail("\n\nAppears the character encoding issue has been fixed. This test can be removed.\n\n");
  }

  
  /**
   * TODO:
   *
   *   This test points out incorrect return documents against Controller 2.0 Alpha 11 REST/XML API.
   *   As per the documentation, an XML document with error data should be returned.
   *
   *
   * @throws IOException if connection fails for any reason, see checkURLWithHTTPProtocol javadoc
   * @throws ParserConfigurationException if DOM parsing fails on return data
   */
  public void testControllerGETRestPanelXML_EmptyController_BROKEN_API_IMPL()
      throws ParserConfigurationException, IOException
  {
    final String TESTURL = TEST_EMPTY_CONTROLLER_URL + RESTAPI_PANEL_URI;


    HttpResponse response = ORConnection.checkURLWithHTTPProtocol(
        activity, ORHttpMethod.GET, TESTURL, NO_HTTP_AUTH
    );

    // TODO : remove skip once Controller REST/XML API has been fixed...

    assertHttpResponse(response, TESTURL, 426, SKIP_CHARSET_CHECK);


    try
    {
      getDOMDocument(response);
    }
    catch (SAXException e)
    {
      fail (
          "\n\nError codes should return an XML body content as per the API documentation \n" +
          "This is currently not implemented as per Controller 2.0 Alpha 11 -- \n" +
          "it is supposedly fixed in /branches/feature/Controller_REST_JSON_API branch.\n\n"
      );
    }


    // TODO -- once the controller is fixed, this test can be completed...

    fail (
        "\n\nError type return values issue appears to be fixed (or changed), " +
        "update this test accordingly.\n\n"
    );
  }


  /**
   * TODO:
   *
   *   This test points out incorrect return documents against Controller 2.0 Alpha 11 REST/XML API.
   *   As per the documentation, an XML document with error data should be returned.
   *
   * @throws IOException if connection fails for any reason, see checkURLWithHTTPProtocol javadoc
   * @throws ParserConfigurationException if DOM parsing fails on return data
   */
  public void testControllerGETRestPanelXML_BrokenPanelXML_BROKEN_API_IMPL()
      throws IOException, ParserConfigurationException
  {
    final String TESTURL = TEST_BROKEN_CONTROLLER_URL + RESTAPI_PANEL_URI;



    HttpResponse response = ORConnection.checkURLWithHTTPProtocol(
        activity, ORHttpMethod.GET, TESTURL, NO_HTTP_AUTH
    );

    // TODO : remove skip once Controller REST/XML API has been fixed...

    assertHttpResponse(response, TESTURL, 424, SKIP_CHARSET_CHECK);

    try
    {
      getDOMDocument(response);
    }
    catch (SAXException e)
    {
      fail (
          "\n\nError codes should return an XML body content as per the API documentation \n" +
          "This is currently not implemented as per Controller 2.0 Alpha 11 -- \n" +
          "it is supposedly fixed in /branches/feature/Controller_REST_JSON_API branch.\n\n"
      );
    }


    // TODO -- once the controller is fixed, this test can be completed...

    fail (
        "\n\nError type return values issue appears to be fixed (or changed), " +
        "update this test accordingly.\n\n"
    );
  }


  /**
   * TODO:
   *
   *   This test points out incorrect return documents against Controller 2.0 Alpha 11 REST/XML API.
   *   As per the documentation, an XML document with error data should be returned.
   *
   * @throws IOException if connection fails for any reason, see checkURLWithHTTPProtocol javadoc
   * @throws ParserConfigurationException if DOM parsing fails on return data
   */
  public void testControllerGETRestFullPanelXML_NotExist_BROKEN_API_IMPL()
      throws IOException, ParserConfigurationException
  {
    final String TESTURL = TEST_CONTROLLER_URL + RESTAPI_FULLPANEL_URI + "DoesNotExist";

    final boolean NO_HTTP_AUTH = false;


    HttpResponse response = ORConnection.checkURLWithHTTPProtocol(
        activity, ORHttpMethod.GET, TESTURL, NO_HTTP_AUTH
    );

    // TODO : remove skip once Controller REST/XML API has been fixed...

    assertHttpResponse(response, TESTURL, 428, SKIP_CHARSET_CHECK);

    try
    {
      getDOMDocument(response);
    }
    catch (SAXException e)
    {
      fail (
          "\n\nError codes should return an XML body content as per the API documentation \n" +
          "This is currently not implemented as per Controller 2.0 Alpha 11 -- \n" +
          "it is supposedly fixed in /branches/feature/Controller_REST_JSON_API branch.\n\n"
      );
    }

    // TODO -- once the controller is fixed, this test can be completed...

    fail (
        "\n\nError type return values issue appears to be fixed (or changed), " +
        "update this test accordingly.\n\n"
    );

  }

  
  /**
   * Test a 'click' write command on a pre-configured button ID.
   *
   * @throws IOException if connection fails for any reason, see checkURLWithHTTPProtocol javadoc
   */
  public void testControllerSimpleButtonCommand() throws IOException
  {
    final String BUTTON_ID = "22";
    final String COMMAND_PARAM = "/click";

    final String TESTURL = TEST_CONTROLLER_URL + RESTAPI_CONTROL_URI + BUTTON_ID + COMMAND_PARAM;


    HttpResponse response = ORConnection.checkURLWithHTTPProtocol(
        activity, ORHttpMethod.POST, TESTURL, NO_HTTP_AUTH
    );

    assertHttpResponse(response, TESTURL, HttpURLConnection.HTTP_OK, NO_HTTP_BODY);
  }



  /**
   * TODO:
   *
   *   This test points out incorrect return documents against Controller 2.0 Alpha 11 REST/XML API.
   *   As per the documentation, an XML document with error data should be returned.
   *
   * @throws IOException if connection fails for any reason, see checkURLWithHTTPProtocol javadoc
   * @throws ParserConfigurationException if DOM parsing fails on return data
   */
  public void testControllerCmdBuildErrorOnButton_BROKEN_API_IMPL() throws IOException, ParserConfigurationException
  {
    final String BUTTON_ID = "999";
    final String COMMAND_PARAM = "/click";

    final String TESTURL = TEST_CMDERRORS_CONTROLLER_URL + RESTAPI_CONTROL_URI + BUTTON_ID + COMMAND_PARAM;


    HttpResponse response = ORConnection.checkURLWithHTTPProtocol(
        activity, ORHttpMethod.POST, TESTURL, NO_HTTP_AUTH
    );

    assertHttpResponse(response, TESTURL, 418);

    try
    {
      getDOMDocument(response);
    }
    catch (SAXException e)
    {
      fail (
          "\n\nError codes should return an XML body content as per the API documentation \n" +
          "This is currently not implemented as per Controller 2.0 Alpha 11 -- \n" +
          "it is supposedly fixed in /branches/feature/Controller_REST_JSON_API branch.\n\n"
      );
    }


    // TODO -- once the controller is fixed, this test can be completed...

    fail (
        "\n\nError type return values issue appears to be fixed (or changed), " +
        "update this test accordingly.\n\n"
    );
  }

  /**
   * Test the HTTP error response on a command that has invalid XML configuration.
   *
   * @throws IOException if connection fails for any reason, see checkURLWithHTTPProtocol javadoc
   */
  public void testControllerCmdBuildErrorOnButton() throws IOException
  {
    final String BROKEN_BUTTON_ID = "999";
    final String COMMAND_PARAM = "/click";

    final String TESTURL = TEST_CMDERRORS_CONTROLLER_URL + RESTAPI_CONTROL_URI + BROKEN_BUTTON_ID + COMMAND_PARAM;

    HttpResponse response = ORConnection.checkURLWithHTTPProtocol(
        activity, ORHttpMethod.POST, TESTURL, NO_HTTP_AUTH
    );

    assertHttpResponse(response, TESTURL, 418);
  }


  /**
   * Test the HTTP error response on a write command URI that has an invalid/malformed component ID.
   *
   * @throws IOException if connection fails for any reason, see checkURLWithHTTPProtocol javadoc
   */
  public void testControllerRESTControlInvalidURI() throws IOException
  {
    final String INVALID_BUTTON_ID = "must-be-integer";
    final String COMMAND_PARAM = "/click";

    final String TESTURL = TEST_CMDERRORS_CONTROLLER_URL + RESTAPI_CONTROL_URI + INVALID_BUTTON_ID + COMMAND_PARAM;

    HttpResponse response = ORConnection.checkURLWithHTTPProtocol(
        activity, ORHttpMethod.POST, TESTURL, NO_HTTP_AUTH
    );

    assertHttpResponse(response, TESTURL, 400);
  }
  

  /**
   * TODO:
   *
   *   This test points out incorrect return documents against Controller 2.0 Alpha 11 REST/XML API.
   *   As per the documentation, an XML document with error data should be returned.
   *
   * @throws IOException if connection fails for any reason, see checkURLWithHTTPProtocol javadoc
   * @throws ParserConfigurationException if DOM parsing fails on return data
   */
  public void testControllerRESTControlInvalidURI_BROKEN_API_IMPL() throws IOException, ParserConfigurationException
  {
    final String INVALID_BUTTON_ID = "must-be-integer";
    final String COMMAND_PARAM = "/click";

    final String TESTURL = TEST_CMDERRORS_CONTROLLER_URL + RESTAPI_CONTROL_URI + INVALID_BUTTON_ID + COMMAND_PARAM;


    HttpResponse response = ORConnection.checkURLWithHTTPProtocol(
        activity, ORHttpMethod.POST, TESTURL, NO_HTTP_AUTH
    );

    assertHttpResponse(response, TESTURL, 400);

    try
    {
      getDOMDocument(response);
    }
    catch (SAXException e)
    {
      fail (
          "\n\nError codes should return an XML body content as per the API documentation \n" +
          "This is currently not implemented as per Controller 2.0 Alpha 11 -- \n" +
          "it is supposedly fixed in /branches/feature/Controller_REST_JSON_API branch.\n\n"
      );
    }


    // TODO -- once the controller is fixed, this test can be completed...

    fail (
        "\n\nError type return values issue appears to be fixed (or changed), " +
        "update this test accordingly.\n\n"
    );

  }

  /**
   * A very light and simple stress test on multiple consequtive button clicks to ensure the
   * behavior and response remains constant.
   *
   * @throws IOException if connection fails for any reason, see checkURLWithHTTPProtocol javadoc
   */
  public void testControllerButtonClicks() throws IOException
  {
    final String LIGHTON_BUTTON_ID = "22";
    final String LIGHTOFF_BUTTON_ID = "24";

    final String BUTTON_COMMAND_PARAM = "/click";

    final String LIGHTON_BUTTON_URL =
        TEST_CONTROLLER_URL + RESTAPI_CONTROL_URI + LIGHTON_BUTTON_ID + BUTTON_COMMAND_PARAM;

    final String LIGHTOFF_BUTTON_URL =
        TEST_CONTROLLER_URL + RESTAPI_CONTROL_URI + LIGHTOFF_BUTTON_ID + BUTTON_COMMAND_PARAM;


    Random rand = new Random(System.currentTimeMillis());

    for (int index = 0; index < 20; ++index)
    {
      int option = rand.nextInt(2);

      String url = "<undefined>";

      switch (option)
      {
        case 0:
          url = LIGHTON_BUTTON_URL;
          break;

        case 1:
          url = LIGHTOFF_BUTTON_URL;
          break;
      }

      HttpResponse response = ORConnection.checkURLWithHTTPProtocol(
          activity, ORHttpMethod.POST, url, NO_HTTP_AUTH
      );

      assertHttpResponse(response, url, HttpURLConnection.HTTP_OK, NO_HTTP_BODY);
    }

  }



  /**
   * Test HTTP response on a 'click' write command where the component ID is not found.
   *
   * @throws IOException if connection fails for any reason, see checkURLWithHTTPProtocol javadoc
   */
  public void testControllerButtonCommandUnknownID() throws IOException
  {
    final String UNKNOWN_BUTTON_ID = "22222";
    final String COMMAND_PARAM = "/click";

    final String TESTURL = TEST_CONTROLLER_URL + RESTAPI_CONTROL_URI + UNKNOWN_BUTTON_ID + COMMAND_PARAM;

    HttpResponse response = ORConnection.checkURLWithHTTPProtocol(
        activity, ORHttpMethod.POST, TESTURL, NO_HTTP_AUTH
    );

    assertHttpResponse(response, TESTURL, 419);
  }



  /**
   * TODO:
   *
   *   This test points out incorrect return documents against Controller 2.0 Alpha 11 REST/XML API.
   *   As per the documentation, an XML document with error data should be returned.
   *
   * @throws IOException if connection fails for any reason, see checkURLWithHTTPProtocol javadoc
   * @throws ParserConfigurationException if DOM parsing fails on return data
   */
  public void testControllerButtonCommandUnknownID_BROKEN_API_IMPL() throws IOException, ParserConfigurationException
  {
    final String UNKNOWN_BUTTON_ID = "22222";
    final String COMMAND_PARAM = "/click";

    final String TESTURL = TEST_CONTROLLER_URL + RESTAPI_CONTROL_URI + UNKNOWN_BUTTON_ID + COMMAND_PARAM;

    HttpResponse response = ORConnection.checkURLWithHTTPProtocol(
        activity, ORHttpMethod.POST, TESTURL, NO_HTTP_AUTH
    );

    assertHttpResponse(response, TESTURL, 419);

    try
    {
      getDOMDocument(response);
    }
    catch (SAXException e)
    {
      fail (
          "\n\nError codes should return an XML body content as per the API documentation \n" +
          "This is currently not implemented as per Controller 2.0 Alpha 11 -- \n" +
          "it is supposedly fixed in /branches/feature/Controller_REST_JSON_API branch.\n\n"
      );
    }


    // TODO -- once the controller is fixed, this test can be completed...

    fail (
        "\n\nError type return values issue appears to be fixed (or changed), " +
        "update this test accordingly.\n\n"
    );

  }


  /**
   * Test HTTP response on a 'click' command that has been configured with an unknown protocol.
   *
   * @throws IOException if connection fails for any reason, see checkURLWithHTTPProtocol javadoc
   */
  public void testControllerButtonCommandUnknownProtocol() throws IOException
  {
    final String UNKNOWN_PROTOCOL_BUTTON_ID = "444";
    final String COMMAND_PARAM = "/click";

    final String TESTURL = TEST_CMDERRORS_CONTROLLER_URL + RESTAPI_CONTROL_URI + UNKNOWN_PROTOCOL_BUTTON_ID + COMMAND_PARAM;

    HttpResponse response = ORConnection.checkURLWithHTTPProtocol(
        activity, ORHttpMethod.POST, TESTURL, NO_HTTP_AUTH
    );

    assertHttpResponse(response, TESTURL, 420);
  }


  /**
   * TODO:
   *
   *   This test points out incorrect return documents against Controller 2.0 Alpha 11 REST/XML API.
   *   As per the documentation, an XML document with error data should be returned.
   *
   * @throws IOException if connection fails for any reason, see checkURLWithHTTPProtocol javadoc
   * @throws ParserConfigurationException if DOM parsing fails on return data
   */
  public void testControllerButtonCommandUnknownProtocol_BROKEN_API_IMPL() throws IOException, ParserConfigurationException
  {
    final String UNKNOWN_PROTOCOL_BUTTON_ID = "444";
    final String COMMAND_PARAM = "/click";

    final String TESTURL = TEST_CMDERRORS_CONTROLLER_URL + RESTAPI_CONTROL_URI + UNKNOWN_PROTOCOL_BUTTON_ID + COMMAND_PARAM;


    HttpResponse response = ORConnection.checkURLWithHTTPProtocol(
        activity, ORHttpMethod.POST, TESTURL, NO_HTTP_AUTH
    );

    assertHttpResponse(response, TESTURL, 420);

    try
    {
      getDOMDocument(response);
    }
    catch (SAXException e)
    {
      fail (
          "\n\nError codes should return an XML body content as per the API documentation \n" +
          "This is currently not implemented as per Controller 2.0 Alpha 11 -- \n" +
          "it is supposedly fixed in /branches/feature/Controller_REST_JSON_API branch.\n\n"
      );
    }


    // TODO -- once the controller is fixed, this test can be completed...

    fail (
        "\n\nError type return values issue appears to be fixed (or changed), " +
        "update this test accordingly.\n\n"
    );
  }




  /**
   * Test HTTP Response on button 'click' command when controller XML has not been deployed.
   *
   * @throws IOException if connection fails for any reason, see checkURLWithHTTPProtocol javadoc
   */
  public void testControllerButtonCommandOnEmptyController() throws IOException
  {
    final String BUTTON_ID = "444";
    final String COMMAND_PARAM = "/click";

    final String TESTURL = TEST_EMPTY_CONTROLLER_URL + RESTAPI_CONTROL_URI + BUTTON_ID + COMMAND_PARAM;

    HttpResponse response = ORConnection.checkURLWithHTTPProtocol(
        activity, ORHttpMethod.POST, TESTURL, NO_HTTP_AUTH
    );

    assertHttpResponse(response, TESTURL, 422);
  }



  /**
   * TODO:
   *
   *   This test points out incorrect return documents against Controller 2.0 Alpha 11 REST/XML API.
   *   As per the documentation, an XML document with error data should be returned.
   *
   * @throws IOException if connection fails for any reason, see checkURLWithHTTPProtocol javadoc
   * @throws ParserConfigurationException if DOM parsing fails on return data
   */
  public void testControllerButtonCommandOnEmptyController_BROKEN_API_IMPL() throws IOException, ParserConfigurationException
  {
    final String NONEXISTENT_BUTTON_ID = "444";
    final String COMMAND_PARAM = "/cLiCk";

    final String TESTURL = TEST_EMPTY_CONTROLLER_URL + RESTAPI_CONTROL_URI + NONEXISTENT_BUTTON_ID + COMMAND_PARAM;

    HttpResponse response = ORConnection.checkURLWithHTTPProtocol(
        activity, ORHttpMethod.POST, TESTURL, NO_HTTP_AUTH
    );

    assertHttpResponse(response, TESTURL, 422);

    try
    {
      getDOMDocument(response);
    }
    catch (SAXException e)
    {
      fail (
          "\n\nError codes should return an XML body content as per the API documentation \n" +
          "This is currently not implemented as per Controller 2.0 Alpha 11 -- \n" +
          "it is supposedly fixed in /branches/feature/Controller_REST_JSON_API branch.\n\n"
      );
    }


    // TODO -- once the controller is fixed, this test can be completed...

    fail (
        "\n\nError type return values issue appears to be fixed (or changed), " +
        "update this test accordingly.\n\n"
    );

  }



  /**
   * Test HTTP error response on 'click' command when the command cannot be parsed (invalid XML).
   *
   * @throws IOException if connection fails for any reason, see checkURLWithHTTPProtocol javadoc
   */
  public void testControllerButtonCommandOnBrokenControllerXML() throws IOException
  {
    final String UNPARSEABLE_BUTTON_ID = "444";
    final String COMMAND_PARAM = "/CLICK";

    final String TESTURL = TEST_BROKEN_CONTROLLER_URL + RESTAPI_CONTROL_URI + UNPARSEABLE_BUTTON_ID + COMMAND_PARAM;


    HttpResponse response = ORConnection.checkURLWithHTTPProtocol(
        activity, ORHttpMethod.POST, TESTURL, NO_HTTP_AUTH
    );

    assertHttpResponse(response, TESTURL, 424);
  }


  /**
   * Quick test that URL's get trimmed properly.
   *
   * @throws IOException if connection fails for any reason, see checkURLWithHTTPProtocol javadoc
   */
  public void testControllerButtonCommandURITrimming() throws IOException
  {
    final String BUTTON_ID = "24";
    final String COMMAND_PARAM = "/CLICK        ";

    final String TESTURL = TEST_CONTROLLER_URL + RESTAPI_CONTROL_URI + BUTTON_ID + COMMAND_PARAM;

    HttpResponse response = ORConnection.checkURLWithHTTPProtocol(
        activity, ORHttpMethod.POST, TESTURL, NO_HTTP_AUTH
    );

    assertHttpResponse(response, TESTURL, HttpURLConnection.HTTP_OK, NO_HTTP_BODY);
  }



  /**
   * TODO:
   *
   *   This test points out incorrect return documents against Controller 2.0 Alpha 11 REST/XML API.
   *   As per the documentation, an XML document with error data should be returned.
   *
   * @throws IOException if connection fails for any reason, see checkURLWithHTTPProtocol javadoc
   * @throws ParserConfigurationException if DOM parsing fails on return data
   */
  public void testControllerButtonCommandOnBrokenControllerXML_BROKEN_API_IMPL() throws IOException, ParserConfigurationException
  {
    final String UNPARSEABLE_BUTTON_ID = "444";
    final String COMMAND_PARAM = "/Click";

    final String TESTURL = TEST_BROKEN_CONTROLLER_URL + RESTAPI_CONTROL_URI + UNPARSEABLE_BUTTON_ID + COMMAND_PARAM;


    HttpResponse response = ORConnection.checkURLWithHTTPProtocol(
        activity, ORHttpMethod.POST, TESTURL, NO_HTTP_AUTH
    );

    assertHttpResponse(response, TESTURL, 424);

    try
    {
      getDOMDocument(response);
    }
    catch (SAXException e)
    {
      fail (
          "\n\nError codes should return an XML body content as per the API documentation \n" +
          "This is currently not implemented as per Controller 2.0 Alpha 11 -- \n" +
          "it is supposedly fixed in /branches/feature/Controller_REST_JSON_API branch.\n\n"
      );
    }


    // TODO -- once the controller is fixed, this test can be completed...

    fail (
        "\n\nError type return values issue appears to be fixed (or changed), " +
        "update this test accordingly.\n\n"
    );

  }



  /**
   * Basic switch on/off test with a preconfigured switch component
   *
   * @throws IOException if connection fails for any reason, see checkURLWithHTTPProtocol javadoc
   */
  public void testControllerSimpleSwitch() throws IOException
  {
    final String COMPONENT_ID = "28";
    final String COMMAND_PARAM_ON = "/on";
    final String COMMAND_PARAM_OFF = "/off";


    final String TESTURL_ON = TEST_CONTROLLER_URL + RESTAPI_CONTROL_URI + COMPONENT_ID + COMMAND_PARAM_ON;
    final String TESTURL_OFF = TEST_CONTROLLER_URL + RESTAPI_CONTROL_URI + COMPONENT_ID + COMMAND_PARAM_OFF;


    HttpResponse response = ORConnection.checkURLWithHTTPProtocol(
        activity, ORHttpMethod.POST, TESTURL_ON, NO_HTTP_AUTH
    );

    assertHttpResponse(response, TESTURL_ON, HttpURLConnection.HTTP_OK, NO_HTTP_BODY);



    response = ORConnection.checkURLWithHTTPProtocol(
        activity, ORHttpMethod.POST, TESTURL_OFF, NO_HTTP_AUTH
    );

    assertHttpResponse(response, TESTURL_OFF, HttpURLConnection.HTTP_OK, NO_HTTP_BODY);
  }


  /**
   *  TODO:
   *
   *    improvement for the current Controller REST/XML API implementation -- when passing an
   *    unknown command paratemer to component, the HTTP return response is OK (200), should
   *    indicate incorrect API use instead.
   *
   * @throws IOException if connection fails for any reason, see checkURLWithHTTPProtocol javadoc
   */
  public void testControllerWrongSwitchCommand() throws IOException
  {
    final String COMPONENT_ID = "28";
    final String COMMAND_PARAM = "/click";

    final String TESTURL = TEST_CONTROLLER_URL + RESTAPI_CONTROL_URI + COMPONENT_ID + COMMAND_PARAM;


    HttpResponse response = ORConnection.checkURLWithHTTPProtocol(
        activity, ORHttpMethod.POST, TESTURL, NO_HTTP_AUTH
    );

    assertTrue("Expected response to " + TESTURL + ", got <null>", response != null);

    int  httpResponseCode = response.getStatusLine().getStatusCode();

    assertTrue(
        "Expected HTTP Response other than " + HttpURLConnection.HTTP_OK +
        " on incorrect command parameter.",
        httpResponseCode != HttpURLConnection.HTTP_OK
    );

  }


  /**
   *  TODO:
   *
   *    improvement for the current Controller REST/XML API implementation -- when passing an
   *    unknown command paratemer to component, the HTTP return response is OK (200), should
   *    indicate incorrect API use instead.
   *
   * @throws IOException if connection fails for any reason, see checkURLWithHTTPProtocol javadoc
   */
  public void testControllerWrongButtonCommand() throws IOException
  {
    final String COMPONENT_ID = "22";
    final String COMMAND_PARAM = "/on";

    final String TESTURL = TEST_CONTROLLER_URL + RESTAPI_CONTROL_URI + COMPONENT_ID + COMMAND_PARAM;


    HttpResponse response = ORConnection.checkURLWithHTTPProtocol(
        activity, ORHttpMethod.POST, TESTURL, NO_HTTP_AUTH
    );

    assertTrue("Expected response to " + TESTURL + ", got <null>", response != null);

    int  httpResponseCode = response.getStatusLine().getStatusCode();

    assertTrue(
        "Expected HTTP Response other than " + HttpURLConnection.HTTP_OK +
        " on incorrect command parameter.",
        httpResponseCode != HttpURLConnection.HTTP_OK
    );

  }


  /**
   *  TODO:
   *
   *    improvement for the current Controller REST/XML API implementation -- when passing an
   *    unknown command paratemer to component, the HTTP return response is OK (200), should
   *    indicate incorrect API use instead.
   *
   * @throws IOException if connection fails for any reason, see checkURLWithHTTPProtocol javadoc
   */
  public void testControllerWrongButtonCommand2() throws IOException
  {
    final String COMPONENT_ID = "24";
    final String COMMAND_PARAM = "/off";

    final String TESTURL = TEST_CONTROLLER_URL + RESTAPI_CONTROL_URI + COMPONENT_ID + COMMAND_PARAM;


    HttpResponse response = ORConnection.checkURLWithHTTPProtocol(
        activity, ORHttpMethod.POST, TESTURL, NO_HTTP_AUTH
    );

    assertTrue("Expected response to " + TESTURL + ", got <null>", response != null);

    int  httpResponseCode = response.getStatusLine().getStatusCode();

    assertTrue(
        "Expected HTTP Response other than " + HttpURLConnection.HTTP_OK +
        " on incorrect command parameter.",
        httpResponseCode != HttpURLConnection.HTTP_OK
    );

  }



  /**
   * Small stress test on a switch to ensure the switch state is returned correctly after
   * consequtive calls with random order of 'on' and 'off' commands.  <p>
   *
   * Note that the system is asynchronous due to polling implementation on devices -- this
   * means that the correct state might not be immediately available in state cache after
   * the write command, so we iterate a couple of times to ensure the state is changed.
   *
   * @throws IOException if connection fails for any reason, see checkURLWithHTTPProtocol javadoc
   * @throws ParserConfigurationException if DOM parsing fails on return data
   * @throws SAXException if parsing fails on return data
   * @throws InterruptedException if tests were interrupted prematurely
   */
  public void testControllerSwitches()
      throws IOException, ParserConfigurationException, SAXException, InterruptedException
  {
    final String SWITCH_ID = "28";
    final String SENSOR_ID = "29";
    
    final String COMMAND_ON_PARAM = "/on";
    final String COMMAND_OFF_PARAM = "/off";


    final String SWITCH_ON_URL =
        TEST_CONTROLLER_URL + RESTAPI_CONTROL_URI + SWITCH_ID + COMMAND_ON_PARAM;

    final String SWITCH_OFF_URL =
        TEST_CONTROLLER_URL + RESTAPI_CONTROL_URI + SWITCH_ID + COMMAND_OFF_PARAM;

    final String READ_SENSOR_URL =
        TEST_CONTROLLER_URL + RESTAPI_STATUS_URI + SENSOR_ID;


    Random rand = new Random(System.currentTimeMillis());

    for (int index = 0; index < 20; ++index)
    {
      int option = rand.nextInt(2);

      String url = "<undefined>";

      switch (option)
      {
        case 0:
          url = SWITCH_ON_URL;
          break;

        case 1:
          url = SWITCH_OFF_URL;
          break;
      }

      HttpResponse response = ORConnection.checkURLWithHTTPProtocol(
          activity, ORHttpMethod.POST, url, NO_HTTP_AUTH
      );

      assertHttpResponse(response, url, HttpURLConnection.HTTP_OK, NO_HTTP_BODY);


      final int MAX_ATTEMPTS = 5;

      for (int attempts = 1; attempts <= MAX_ATTEMPTS; ++attempts)
      {
        response = ORConnection.checkURLWithHTTPProtocol(
            activity, ORHttpMethod.GET, READ_SENSOR_URL, NO_HTTP_AUTH
        );

        assertHttpResponse(response, READ_SENSOR_URL, HttpURLConnection.HTTP_OK);



        Document doc = getDOMDocument(response);

        NodeList nodes = doc.getElementsByTagName("status");

        assertTrue(
            "Expected exactly one status node, got " + nodes.getLength(),
            nodes.getLength() == 1
        );

        Node node = nodes.item(0);
        String content = node.getFirstChild().getNodeValue().trim();

        boolean foundCorrectState = false;

        switch (option)
        {
          case 0:
            if (content.equalsIgnoreCase("on"))
            {
              foundCorrectState = true;
            }
            break;

          case 1:
            if (content.equalsIgnoreCase("off"))
            {
              foundCorrectState = true;
            }
            break;
        }

        if (foundCorrectState)
          break;

        else
        {
          if (attempts == MAX_ATTEMPTS)
          {
            fail("Sent " + url + " on round " + index + "\nGot wrong response '" + content + "'.");
          }
          else
          {
            Thread.sleep(500);
          }
        }
      }
    }
  }


  /**
   * TODO:
   *
   *   Demonstrates error in current Controller implementation that returns an incorrect
   *   HTTP content-type header.
   *
   * @throws IOException if connection fails for any reason, see checkURLWithHTTPProtocol javadoc
   * @throws ParserConfigurationException if DOM parsing fails on return data
   * @throws SAXException if parsing fails on return data
   */
  public void testControllerSwitch_BROKEN_API_IMPL() throws IOException, ParserConfigurationException, SAXException
  {
    final String SENSOR_ID = "29";

    final String READ_SENSOR_URL =
        TEST_CONTROLLER_URL + RESTAPI_STATUS_URI + SENSOR_ID;


    HttpResponse response = ORConnection.checkURLWithHTTPProtocol(
        activity, ORHttpMethod.GET, READ_SENSOR_URL, NO_HTTP_AUTH
    );

    assertHttpResponse(response, READ_SENSOR_URL, HttpURLConnection.HTTP_OK, APPLICATIONXML_MIME_TYPE);
  }



  /**
   * Test a simple 'switch' sensor read (disregarding returned state)
   *
   * @throws IOException if connection fails for any reason, see checkURLWithHTTPProtocol javadoc
   * @throws ParserConfigurationException if DOM parsing fails on return data
   * @throws SAXException if parsing fails on return data
   */
  public void testControllerSwitchSensor() throws IOException, ParserConfigurationException, SAXException
  {
    final String SENSOR_ID = "29";

    final String READ_SENSOR_URL =
        TEST_CONTROLLER_URL + RESTAPI_STATUS_URI + SENSOR_ID;


    HttpResponse response = ORConnection.checkURLWithHTTPProtocol(
        activity, ORHttpMethod.GET, READ_SENSOR_URL, NO_HTTP_AUTH
    );

    assertHttpResponse(response, READ_SENSOR_URL, HttpURLConnection.HTTP_OK);


    // TODO :
    //   This should be fixed in Controller 2.0 Alpha 12 -- uncomment when online test controller
    //   has been upgraded (currently returns 'text/plain')
    //
    try
    {
      assertMimeType(response, APPLICATIONXML_MIME_TYPE);

      fail("\n\nIncorrect content-type issue has been fixed, please update this test.\n\n");
    }
    catch (Throwable t)
    {
      // TODO: Ignore for now, remove the check once content-type issue has been fixed.
    }

    Document doc = getDOMDocument(response);

    NodeList nodes = doc.getElementsByTagName("status");

    assertTrue(
        "Expected exactly one status node, got " + nodes.getLength(),
        nodes.getLength() == 1
    );

    Node node = nodes.item(0);
    String content = node.getFirstChild().getNodeValue().trim();

    assertTrue(content.equalsIgnoreCase("on") || content.equalsIgnoreCase("off"));
  }

  


  // Helpers --------------------------------------------------------------------------------------


  /**
   * TODO:
   *
   *    The skipCharsetCheck boolean parameter can be removed once the Controller has been fixed
   *    to return correct UTF-8 encoded responses.
   *
   * @param response            HTTP response
   * @param url                 Original URL
   * @param returnCode          HTTP response code
   * @param skipCharsetCheck    Whether to assert UTF-8 character encoding or not
   */
  private void assertHttpResponse(HttpResponse response, String url, int returnCode, boolean skipCharsetCheck)
  {
    assertHttpResponse(response, url, returnCode, skipCharsetCheck, ASSERT_NON_ZERO_BODY_LENGTH);
  }


  /**
   * TODO:
   *
   *    The skipCharsetCheck boolean parameter can be removed once the Controller has been fixed
   *    to return correct UTF-8 encoded responses.
   *
   * @param response            HTTP response
   * @param url                 Original URL
   * @param returnCode          HTTP response code
   * @param skipCharsetCheck    Whether to assert UTF-8 character encoding or not
   * @param bodyLength          Assert precise body length in HTTP response
   */
  private void assertHttpResponse(HttpResponse response, String url, int returnCode, boolean skipCharsetCheck, int bodyLength)
  {
    assertHttpResponse(response, url, returnCode, skipCharsetCheck, DO_NOT_CHECK_BODY_LENGTH);

    assertHttpBodyLength(response, bodyLength);
  }


  /**
   * TODO:
   *
   *    The skipCharsetCheck boolean parameter can be removed once the Controller has been fixed
   *    to return correct UTF-8 encoded responses.
   *
   * @param response            HTTP response
   * @param url                 Original URL
   * @param returnCode          HTTP response code
   * @param skipCharsetCheck    Whether to assert UTF-8 character encoding or not
   * @param hasContentBodyCheck Indicates if any assertions should be made wrt HTTP body content
   */
  private void assertHttpResponse(HttpResponse response, String url, int returnCode, boolean skipCharsetCheck, boolean hasContentBodyCheck)
  {
    assertNotNullResponse(response, url);
    assertHttpReturnCode(response, returnCode);

    if (hasContentBodyCheck)
      assertNotZeroResponseBody(response);

    assertHasContentTypeHeader(response);

    if (!skipCharsetCheck)
      assertUTF8Encoding(response);
  }


  /**
   *
   * @param response
   * @param url
   * @param returnCode
   * @param mimeType
   * @param skipCharsetCheck
   */
  private void assertHttpResponse(HttpResponse response, String url, int returnCode, String mimeType, boolean skipCharsetCheck)
  {
    assertHttpResponse(response, url, returnCode, skipCharsetCheck);

    assertMimeType(response, mimeType);
  }




  private void assertHttpResponse(HttpResponse response, String url, int returnCode, int bodyLength)
  {
    assertHttpResponse(response, url, returnCode, ASSERT_UTF8_ENCODING, bodyLength);
  }

  private void assertHttpResponse(HttpResponse response, String url, int returnCode)
  {
    assertHttpResponse(response, url, returnCode, ASSERT_UTF8_ENCODING);
  }


  private void assertHttpResponse(HttpResponse response, String url, int returnCode, String mimeType)
  {
    assertHttpResponse(response, url, returnCode, mimeType, ASSERT_UTF8_ENCODING);

  }





  private void assertMimeType(HttpResponse response, String mimeType)
  {
    String httpMimeContentType = response.getEntity().getContentType().getValue();

    assertTrue(
        "Expected HTTP Mime type '" + mimeType + "', got '" + httpMimeContentType + "'.",
        httpMimeContentType.startsWith(mimeType)
    );
  }


  private void assertNotNullResponse(HttpResponse response, String URL)
  {
    assertNotNull("Expected response to " + URL + ", got <null>", response);
  }


  private void assertHttpReturnCode(HttpResponse response, int code)
  {
    int httpResponseCode = response.getStatusLine().getStatusCode();

    assertTrue(
        "Expected HTTP Response '" + code + "', got '" + httpResponseCode + "'.",
        httpResponseCode == code
    );
  }

  private void assertHttpBodyLength(HttpResponse response, int bodyLen)
  {
    long httpResponseContentLen = response.getEntity().getContentLength();

    assertTrue(
        "Expected content length of '" + bodyLen + " bytes, got " + httpResponseContentLen,
        response.getEntity().getContentLength() == bodyLen
    );
  }

  private void assertNotZeroResponseBody(HttpResponse response)
  {
    long httpResponseContentLen = response.getEntity().getContentLength();

    assertTrue(
        "Expected content length > 0, got " + httpResponseContentLen,
        response.getEntity().getContentLength() > 0
    );
  }

  private void assertHttpContentType(HttpResponse response, String mimeType)
  {
    final String HTTP_CONTENT_TYPE_HEADER = "content-type";

    String httpContentTypeHeader = response.getEntity().getContentType().getName();
    String httpMimeContentType = response.getEntity().getContentType().getValue();

    assertTrue(
        "Expected HTTP Header '" + HTTP_CONTENT_TYPE_HEADER + "', got '" + httpContentTypeHeader + "'.",
        httpContentTypeHeader.equalsIgnoreCase(HTTP_CONTENT_TYPE_HEADER)
    );

    assertTrue(
        "Expected HTTP Mime type '" + mimeType + "', got '" + httpMimeContentType + "'.",
        httpMimeContentType.startsWith(mimeType)
    );

  }


  private void assertUTF8Encoding(HttpResponse response)
  {
    final String HTTP_CHAR_ENCODING = "charset=UTF-8".toUpperCase();

    String httpMimeContentType = response.getEntity().getContentType().getValue();

    assertTrue(
        "Expected character encoding '" + HTTP_CHAR_ENCODING + "', got '" + httpMimeContentType + "'.",
        httpMimeContentType.toUpperCase().contains(HTTP_CHAR_ENCODING)
    );

  }

  private void assertHasContentTypeHeader(HttpResponse response)
  {
    final String HTTP_CONTENT_TYPE_HEADER = "content-type";

    String httpContentTypeHeader = response.getEntity().getContentType().getName();

    assertTrue(
        "Expected HTTP Header '" + HTTP_CONTENT_TYPE_HEADER + "', got '" + httpContentTypeHeader + "'.",
        httpContentTypeHeader.equalsIgnoreCase(HTTP_CONTENT_TYPE_HEADER)
    );
  }


  private Document getDOMDocument(HttpResponse response)
      throws ParserConfigurationException, SAXException, IOException
  {
    DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder parser = domFactory.newDocumentBuilder();

    return parser.parse(response.getEntity().getContent());
  }



  private void assertOpenRemoteRootElement(Document doc)
  {
    NodeList list = doc.getElementsByTagName("openremote");

    assertTrue(
        "Expected to find exactly one <openremote> element, got " + list.getLength(),
        list.getLength() == 1
    );

    list = doc.getChildNodes();
    int foundRootElement = 0;

    for (int cindex = 0; cindex < list.getLength(); cindex++)
    {
      if (list.item(cindex).getNodeType() == Node.ELEMENT_NODE)
        ++foundRootElement;
    }


    assertTrue(
        "Expected exactly one child element on document, got " + foundRootElement,
        foundRootElement == 1
    );
  }

}

