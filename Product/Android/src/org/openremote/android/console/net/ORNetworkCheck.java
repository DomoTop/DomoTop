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
package org.openremote.android.console.net;

import java.net.HttpURLConnection;
import java.io.IOException;

import org.apache.http.HttpResponse;
import org.openremote.android.console.Constants;
import org.openremote.android.console.model.AppSettingsModel;
import org.openremote.android.console.util.HTTPUtil;

import android.content.Context;
import android.util.Log;
import android.net.wifi.WifiManager;
import android.net.NetworkInfo;
import android.net.ConnectivityManager;


/**
 * TODO: Checks access to OpenRemote controller.
 * 
 * @author <a href="mailto:juha@openremote.org">Juha Lindfors</a>
 * @author handy 2010-04-28
 */
public class ORNetworkCheck
{

  // Constants ------------------------------------------------------------------------------------

  /**
   * Log category name used by this class.
   */
  private final static String LOG_CATEGORY = Constants.LOG_CATEGORY + "WiFi";



  // Public Class Methods -------------------------------------------------------------------------
  
  /**
   * Verifies the network access to the currently configured controller URL by checking if
   * REST API {controllerServerURL}/rest/panel/{panel identity} is available.
   *
   * @param context               global Android application context
   * @param url                   an URL to a controller instance
   *
   * @return TODO: returns null or HttpResponse
   *
   * @throws IOException TODO
   */
  public static HttpResponse verifyControllerURL(Context context, String url) throws IOException
  {
    // TODO : Use URL class instead of string in param


    // TODO : modifying the settings probably doesn't belong here, as it is an undocumented side-effect
    AppSettingsModel.setCurrentServer(context, url);


    HttpResponse response = isControllerAvailable(context);

    Log.d(LOG_CATEGORY, "HTTP Response: " + response);

    if (response == null)
      return null;  // TODO : fix this, it is stupid - throw an exception

    int status = response.getStatusLine().getStatusCode();

    if (status != HttpURLConnection.HTTP_OK)
      return response;


    String controllerURL = AppSettingsModel.getSecuredServer(context);

    if (controllerURL == null || "".equals(controllerURL))
    {
      return null;  // TODO : fix this, it is stupid - throw an exception
    }

    String currentPanelIdentity = AppSettingsModel.getCurrentPanelIdentity(context);

    if (currentPanelIdentity == null || "".equals(currentPanelIdentity))
    {
      return null;  // TODO : fix this, it is stupid - throw an exception
    }

    String restfulPanelURL = controllerURL + "/rest/panel/" + HTTPUtil.encodePercentUri(currentPanelIdentity);

    Log.i(LOG_CATEGORY, "Getting panel URL " + restfulPanelURL);

    return ORConnection.checkURLWithHTTPProtocol(context, ORHttpMethod.GET, restfulPanelURL, true);
  }



  // Private Class Methods ------------------------------------------------------------------------


  /**
   * Check if the Controller URL is available.
   *
   * @param context   a global Android application context
   *
   * @return  returns the HTTP response from the attempt to connect to the configured controller
   *          or null, in case of failure (note that the HTTP response code may also include
   *          an error code from connection attempt).
   *
   * @throws IOException TODO
   */
  private static HttpResponse isControllerAvailable(Context context) throws IOException
  {
    if (!hasWifiAndControllerConfig(context))
      return null;

    String controllerURL = AppSettingsModel.getSecuredServer(context);

    return ORConnection.checkURLWithHTTPProtocol(context, ORHttpMethod.GET, controllerURL, false);
  }


  /**
   * Checks availability of WiFi netowrk and whether we have a controller URL configured.
   *
   * @param context   global Android application context
   *
   * @return  true if we can reach wifi and controller URL has been configured in app's settings,
   *          false otherwise
   */
  private static boolean hasWifiAndControllerConfig(Context context)
  {

    if (!IPAutoDiscoveryClient.isNetworkTypeWIFI)     // TODO : questionable use of global static field
    {
        return true;
    }

    // Do we have a WiFi connection? If not, give up...

    if (!canReachWifiNetwork(context))
    {
      // TODO : could ask user if they want to turn on WiFi at this point...

      return false;
    }

    // Has controller URL been configured...?

    String controllerURL = AppSettingsModel.getCurrentServer(context);

    Log.d(LOG_CATEGORY, "controllerURL: " + controllerURL);

    if (controllerURL == null || "".equals(controllerURL))
    {
      return false;
    }
    else
    {
      return true;
    }
  }

  /**
   * Detects the current WiFi status.
   *
   * @param ctx     global Android application context
   *
   * @return true if WiFi is available, false otherwise
   */
  private static boolean canReachWifiNetwork(Context ctx)
  {

    WifiManager wifiManager = (WifiManager)ctx.getSystemService(Context.WIFI_SERVICE);
    ConnectivityManager connectivityManager = (ConnectivityManager)ctx.getSystemService(Context.CONNECTIVITY_SERVICE);

    if (!wifiManager.isWifiEnabled() || wifiManager.getWifiState() != WifiManager.WIFI_STATE_ENABLED)
    {
      Log.i(LOG_CATEGORY, "WiFi not enabled or WiFi network not detected.");

      return false;
    }

    NetworkInfo wifiNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

    if (!wifiNetworkInfo.isAvailable())
    {
      Log.i(LOG_CATEGORY, "Wifi network detected but wasn't available.");

      return false;
    }

    else
    {
      return true;
    }
  }

}
