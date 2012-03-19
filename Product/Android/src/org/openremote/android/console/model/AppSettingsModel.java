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
package org.openremote.android.console.model;

import java.io.Serializable;
import java.net.URL;
import java.net.MalformedURLException;

import org.openremote.android.console.Constants;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;



/**
 * Utility methods to access application's settings.
 *
 * @author <a href="mailto:juha@openremote.org">Juha Lindfors</a>
 * @author Tomsky Wang
 * @author Dan Cong
 *
 */
public class AppSettingsModel implements Serializable
{

  // Serialization --------------------------------------------------------------------------------

  private static final long serialVersionUID = Constants.MODEL_VERSION;


  // Constants ------------------------------------------------------------------------------------

  /**
   * Constant that can be used to set configuration back to a default SSL port. This is interpreted
   * based on configured controller URL value -- if there's no explicit port in the controller
   * URL, default SSL port will be 443, if an explicit port has been configured in controller
   * URL, we guess it is for OpenRemote/Runtime and default to SSL port 8443.
   */
  public final static int DEFAULT_SSL_PORT  = -1;


  /**
   * Default SSL port for Tomcat runtime. We default to this port if SSL is enabled and an
   * explicit port has been set in the controller URL (we are assuming explicit port exists to
   * connect to OpenRemote/Tomcat runtime so we default to Tomcat's default SSL port).
   */
  private static final int DEFAULT_TOMCAT_SSL_PORT = 8443;

  /**
   * Default SSL port for HTTPD. We default to this port if the user configured controller URL
   * has no explicit port (therefore using default httpd port 80) and therefore we default to
   * port 443 for SSL (this usually only applies when controller is hosted behind an httpd server).
   */
  private static final int DEFAULT_HTTPD_SSL_PORT = 443;


  /**
   * Common log category for this class
   */
  private final static String LOG_CATEGORY = Constants.LOG_CATEGORY + "Settings";


  private static final String APP_SETTINGS = "appSettings";
  private static final String CUSTOM_SERVERS = "customServers";
  private static final String CURRENT_SERVER = "currentServer";
  private static final String AUTO_MODE = "autoMode";

  /**
   * Stores the current panel ID this application is rendering (controller may store several
   * panel designs)
   */
  private static final String CURRENT_PANEL_IDENTITY = "currentPanelIdentity";

  /**
   * Indicates whether encrypted HTTP communication is used between this application
   * and controller.
   */
  private static final String USE_SSL = "useSSL";

  /**
   * The port number to connect to if encrypted SSL communication is used.
   */
  private static final String SSL_PORT = "sslPort";


  // Class Methods --------------------------------------------------------------------------------

  /**
   * Returns controller URL from appSettings.xml. Note that this method always returns the
   * (user) configured URL regardless of whether SSL has been configured or not.
   *
   * @param   context  global Android application context
   *
   * @return  Returns the (user) configured URL to controller, as-is, or empty string if nothing
   *          has been stored
   */
  public static String getCurrentServer(Context context)
  {
    // TODO: return URL instead of String

    return context.getSharedPreferences(APP_SETTINGS, Context.MODE_PRIVATE)
                  .getString(CURRENT_SERVER, "");
  }


  /**
   * Returns controller URL. <p>
   *
   * This method will return the user configured URL if SSL has not been configured (assuming
   * a valid URL has been entered), otherwise the returned controller URL is modified to use
   * HTTPS instead. <p>
   *
   * If an explicit SSL port has been configured, it is added to the returned URL.  <p>
   *
   * For example, if user-configured controller URL is
   * 'http://controller.openremote.org/test/controller' : <p>
   *
   *  - Returns the same URL string if SSL has not been turned on <br>
   *  - Returns 'https://controller.openremote.org:443/test/controller' if SSL has been turned on
   *
   * <p>
   *
   * If user-configured controller URL has an explicit port number (case of default OpenRemote
   * runtime/Tomcat installation) then URL 'http://controller.openremote.org:8080/test/controller'
   * is translated as follows: <p>
   *
   * - Same URL if SSL has not been turned on <br>
   * - Returns 'https://controller.openremote.org:8443/test/controller' if SSL has been turned on
   *   but no specific SSL port has been configured (same applies regardless what explicit port
   *   number user has configured in the original URL)
   *
   * <p>
   *
   * If both SSL has been enabled and explicit SSL port has been configured then the URL is
   * transformed as expected with HTTPS protocol schema and SSL port number, regardless whether
   * the original URL included explicit port or not.
   *
   *
   * @param   context  global Android application context
   *
   * @return  modified controller URL according to SSL settings, or null if the user-configured
   *          controller URL is malformed.
   */
  public static String getSecuredServer(Context context)
  {
    // TODO : return URL instead of string

    try
    {
      URL configuredControllerURL = new URL(getCurrentServer(context));

      int port = configuredControllerURL.getPort();
      String protocol = configuredControllerURL.getProtocol();
      String host = configuredControllerURL.getHost();
      String file = configuredControllerURL.getFile();

      if (isSSLEnabled(context))
      {
        protocol = "https";

        if (getSSLPort(context) == -1 && port == -1)
        {
          port = DEFAULT_HTTPD_SSL_PORT;
        }

        else if (getSSLPort(context) == -1)
        {
          port = DEFAULT_TOMCAT_SSL_PORT;
        }

        else
        {
          port = getSSLPort(context);
        }
      }

      String realURL = new URL(protocol, host, port, file).toExternalForm();

      Log.d(LOG_CATEGORY, realURL);

      return realURL;
    }
    catch (MalformedURLException e)
    {
      // TODO : should propagate malformed URL up to user, so they know they messed up, requires API modification

      return null;
    }
  }


  /**
   * Saves controller URL into appSettings.xml.
   *
   * @param context        global Android application context
   * @param controllerURL  controller URL to save in the application settings
   */
  public static void setCurrentServer(Context context, String controllerURL)
  {
    // TODO : use URL instead of String in the API

    SharedPreferences.Editor editor = context.getSharedPreferences(
        APP_SETTINGS,
        Context.MODE_PRIVATE
    ).edit();

    editor.putString(CURRENT_SERVER, controllerURL);
    editor.commit();
  }



   /**
    * Sets the auto discovery mode.
    * 
    * @param context the context
    * @param isAuto the is auto
    */
   public static void setAutoMode(Context context, boolean isAuto) {
      SharedPreferences.Editor editor = context.getSharedPreferences(APP_SETTINGS, 0).edit();
      editor.putBoolean(AUTO_MODE, isAuto);
      editor.commit();
   }
   
   public static boolean isAutoMode(Context context) {
      return context.getSharedPreferences(APP_SETTINGS, 0).getBoolean(AUTO_MODE, true);
   }


  /**
   * Returns the current, in-use panel identity from application's settings.
   *
   * @param context   global Android application context
   *
   * @return  panel identity (name)
   */
  public static String getCurrentPanelIdentity(Context context)
  {
     return context.getSharedPreferences(APP_SETTINGS, Context.MODE_PRIVATE)
            .getString(CURRENT_PANEL_IDENTITY, "");
  }
   
  /**
   * Saves a current, in-use panel identity to application's settings.
   *
   * @param context               global Android application context
   * @param currentPanelIdentity  new panel identity (name)
   */
  public static void setCurrentPanelIdentity(Context context, String currentPanelIdentity)
  {
    SharedPreferences.Editor editor = context.getSharedPreferences(
       APP_SETTINGS,
       Context.MODE_PRIVATE
    ).edit();

    editor.putString(CURRENT_PANEL_IDENTITY, currentPanelIdentity);
    editor.commit();
  }




   public static void setCustomServers(Context context, String customServers) {
      SharedPreferences.Editor editor = context.getSharedPreferences(CUSTOM_SERVERS, 0).edit();
      editor.putString(CUSTOM_SERVERS, customServers);
      editor.commit();
   }
   
   public static String getCustomServers(Context context) {
      return context.getSharedPreferences(CUSTOM_SERVERS, 0).getString(CUSTOM_SERVERS, "http://controller.openremote.org/android/controller");
   }


  /**
   * Return current SSL status.
   *
   * @param   context   global Android application context
   *
   * @return  true if SSL over HTTP has been enabled, false otherwise
   */
  public static boolean isSSLEnabled(Context context)
  {
    return context.getSharedPreferences(
        APP_SETTINGS,
        Context.MODE_PRIVATE
    ).getBoolean(USE_SSL, false);
  }

  /**
   * Enable/disable SSL over HTTP communication.
   *
   * @param context     global Android application context
   * @param enableSSL   true to enable SSL, false to disable
   */
  public static void enableSSL(Context context, boolean enableSSL)
  {
    SharedPreferences.Editor editor = context.getSharedPreferences(
        APP_SETTINGS,
        Context.MODE_PRIVATE
    ).edit();

    editor.putBoolean(USE_SSL, enableSSL);
    editor.commit();
  }


  /**
   * Returns a configured SSL port number. <p>
   *
   * If SSL port has not been configured, attempts to inspect the user-configured controller URL
   * and makes a best guess for the appropriate port number:  <p>
   *
   * If user-configured URL does not contain an explicit port (so defaulting to httpd port 80)
   * then returns httpd default SSL port 443.  <p>
   *
   * If user-configured URL contains an explicit port number then returns the default SSL port
   * of OpenRemote/Tomcat runtime, 8443 (making a guess here that the explicit URL port is
   * configured normally to connect direct to OR/Tomcat runtime).
   *
   *
   * @param   context   global Android application context
   *
   * @return  configured SSL port value or 443 or 8443 default ports depending how controller
   *          URL has been configured
   */
  public static int getSSLPort(Context context)
  {
    int port = context.getSharedPreferences(APP_SETTINGS, Context.MODE_PRIVATE).getInt(SSL_PORT, -1);

    if (port != -1)
      return port;

    String configuredControllerURL = getCurrentServer(context);

    try
    {
      int configuredPort = new URL(configuredControllerURL).getPort();

      if (configuredPort == -1)
      {
        return DEFAULT_HTTPD_SSL_PORT;
      }
      else
      {
        return DEFAULT_TOMCAT_SSL_PORT;
      }

    }
    catch (MalformedURLException e)
    {
      //   if we enforce proper URL on controller URL set, we can assume this only occurs
      //   as programming error, no need to propagate back up to user

      Log.e(LOG_CATEGORY, "Controller URL is invalid", e);

      // Best guess return value...

      return DEFAULT_TOMCAT_SSL_PORT;
    }
  }
   
  /**
   * Sets the SSL port for controller URL.
   *
   * @param context  global Android application context
   * @param sslPort  SSL port number (0 to 65535) or {@link #DEFAULT_SSL_PORT}.
   *
   * @throws  IllegalArgumentException if the port number is not within the required range
   */
  public static void setSSLPort(Context context, int sslPort)
  {
    if (sslPort < 0 && sslPort != DEFAULT_SSL_PORT)
      throw new IllegalArgumentException("negative port number");

    if (sslPort > 65535)
      throw new IllegalArgumentException("port number too large");

    SharedPreferences.Editor editor = context.getSharedPreferences(
        APP_SETTINGS,
        Context.MODE_PRIVATE
    ).edit();

    editor.putInt(SSL_PORT, sslPort);
    editor.commit();
  }



  // Constructors ---------------------------------------------------------------------------------


  private AppSettingsModel() {}

}
