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
package org.openremote.controller;

/**
 * Application wide constants for OpenRemote Controller.
 * 
 * @author <a href="mailto:juha@openremote.org">Juha Lindfors</a>
 * @author Dan 2009-6-1
 */
public class Constants
{

  /**
   * Top level category for all controller related logging.
   */
  public final static String CONTROLLER_ROOT_LOG_CATEGORY = "OpenRemote.Controller";

  /**
   * Logging subcategory for protocol implementations. Each protocol implementation should
   * add its unique subcategory to this parent log category.
   */
  public final static String CONTROLLER_PROTOCOL_LOG_CATEGORY = CONTROLLER_ROOT_LOG_CATEGORY + ".protocol.";

  /**
   * Logging subcategory for XML parsing.
   */
  public final static String XML_PARSER_LOG_CATEGORY = CONTROLLER_ROOT_LOG_CATEGORY + ".xml.parser";

  /**
   * Logging subcategory for incoming requests on the Controller HTTP/REST API.
   */
  public final static String HTTP_REST_LOG_CATEGORY = CONTROLLER_ROOT_LOG_CATEGORY + ".rest";

  /**
   * Specific logging subcategory for /rest/panels related incoming HTTP requests on REST API.
   */
  public final static String REST_ALL_PANELS_LOG_CATEGORY = HTTP_REST_LOG_CATEGORY + ".panels";

  /**
   * Specific logging subcategory for /rest/panel/[id] related incoming HTTP requests on REST API.
   */
  public final static String REST_GET_PANEL_DEF_LOG_CATEGORY = HTTP_REST_LOG_CATEGORY + ".panel.id";

  /**
   * Specific log category for reporting runtime system properties.
   */
  public final static String RUNTIME_CONFIGURATION_LOG_CATEGORY = CONTROLLER_ROOT_LOG_CATEGORY + ".system.configuration";

  /**
   * Specific log category for deployers.
   */
  public final static String DEPLOYER_LOG_CATEGORY = CONTROLLER_ROOT_LOG_CATEGORY + ".deployer";

  

  public final static String LIRCD_CONF = "lircd.conf";

  public final static String CONTROLLER_XSD_PATH = "/controller-2.0-M7.xsd";

  public final static String PANEL_XSD_PATH = "/panel-2.0-M7.xsd";

  public final static String CONTROLLER_XML = "controller.xml";

  public final static String OPENREMOTE_WEBSITE= "http://www.openremote.org";

  public final static String OPENREMOTE_NAMESPACE= "or";

  public final static String SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";

  public final static String XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";

  public final static String SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";

  public final static String OK = "OK";

  public static final String SERVER_RESPONSE_TIME_OUT = "TIMEOUT";

  /**
   * File name of the panel UI definition file in the controller. The file is located in the
   * 'resource path' directory that can be found from the controller configuration object.
   *
   * @see ControllerConfiguration#getResourcePath()
   */
  public static final String PANEL_XML = "panel.xml";


  public static final String STATUS_POLLING_SENSOR_IDS_SEPARATOR = ",";

  public static final String STATUS_XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<openremote xmlns=\"http://www.openremote.org\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.openremote.org http://www.openremote.org/schemas/controller.xsd\">\n";

  public static final String STATUS_XML_STATUS_RESULT_ELEMENT_NAME = "status";

  public static final String SENSORS_ELEMENT_NAME = "sensors";

  public static final String INCLUDE_ELEMENT_NAME = "include";

  public static final String SENSOR_TYPE_ATTRIBUTE_NAME = "type";

  public static final String ID_ATTRIBUTE_NAME = "id";

  public static final String REF_ATTRIBUTE_NAME = "ref";

  /** The Constant XML_STATUS_RESULT_ELEMENT_SENSOR_IDENTITY composed xml-formatted status results. */
  public static final String STATUS_XML_STATUS_RESULT_ELEMENT_SENSOR_IDENTITY = "id";

  /** The Constant XML_TAIL of composed xml-formatted status results. */
  public static final String STATUS_XML_TAIL = "</openremote>";



  // HTTP Request Headers -------------------------------------------------------------------------

  /**
   * HTTP 'Authorization' header for requests that require HTTP server-side authentication.
   */
  public final static String HTTP_AUTHORIZATION_HEADER = "Authorization";

  /**
   * Prefix for 'Authorization' header <b>value</b> to indicate a HTTP 'Basic' authentication.
   * The prefix should be followed with encoded user name and password.
   */
  public final static String HTTP_BASIC_AUTHORIZATION = "Basic ";

  /**
   * HTTP 'Accept' header used to indicate what types of documents the sender of request is
   * capable of accepting.
   */
  public final static String HTTP_ACCEPT_HEADER = "accept";



  // HTTP Response Codes --------------------------------------------------------------------------


  /**
   * HTTP respose code if panel.xml cannot be parsed for any reason: {@value}
   *
   * @see #HTTP_RESPONSE_INVALID_CONTROLLER_XML
   */
  public final static int HTTP_RESPONSE_INVALID_PANEL_XML = 424;

  /**
   * HTTP response code if controller.xml cannot be parsed for any reason: {@value}
   *
   * @see #HTTP_RESPONSE_INVALID_PANEL_XML
   */
  public final static int HTTP_RESPONSE_INVALID_CONTROLLER_XML = 424;


  /**
   * HTTP response code if panel.xml has not been correctly deployed: {@value}
   */
  public final static int HTTP_RESPONSE_PANEL_XML_NOT_DEPLOYED = 426;


  /**
   * HTTP response code to request asking for a specific panel ID that cannot be found: {@value}
   */
  public final static int HTTP_RESPONSE_PANEL_ID_NOT_FOUND = 428;



  // Character Encodings --------------------------------------------------------------------------

  /**
   * IANA code for UTF-8 character encoding.
   */
  public final static String CHARACTER_ENCODING_UTF8 = "UTF-8";



  // MIME Types -----------------------------------------------------------------------------------

  /**
   * MIME type string for 'application/xml' content.
   */
  public final static String MIME_APPLICATION_XML = "application/xml";

  /**
   * MIME type string for 'application/json' content.
   */
  public final static String MIME_APPLICATION_JSON = "application/json";



  // JSON API -------------------------------------------------------------------------------------

  /**
   * HTTP parameter name used for JSON-P function callback name.
   */
  public final static String CALLBACK_PARAM_NAME = "callback";

}
