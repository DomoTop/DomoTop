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

import java.util.Map;
import java.net.URL;
import java.math.BigInteger;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;
import org.openremote.controller.command.ExecutableCommand;
import org.openremote.controller.command.StatusCommand;
import org.openremote.controller.component.EnumSensorType;
import org.openremote.controller.component.Sensor;
import org.openremote.controller.exception.ConversionException;

/**
 * TODO
 *
 * @author Marcus 2009-4-26
 * @author <a href="mailto:juha@openremote.org">Juha Lindfors</a>
 * @author Dan Cong
 */
public class HttpGetCommand implements ExecutableCommand, StatusCommand
{

  // Class Members --------------------------------------------------------------------------------

  /**
   * Common logging category.
   */
  private static Logger logger = Logger.getLogger(HttpGetCommandBuilder.HTTP_PROTOCOL_LOG_CATEGORY);


  
  // Instance Fields ------------------------------------------------------------------------------

  /** A name to identify command in controller.xml. */
  private String name;

  /** The url to perform the http get request on */
  private URL url;

  /** The username which is used for basic authentication */
  private String username;

  /** The password which is used for basic authentication */
  private byte[] password;



  // Constructors ---------------------------------------------------------------------------------

  public HttpGetCommand(URL url)
  {
    this.url = url;
  }

  public HttpGetCommand(URL url, String username, byte[] pwd)
  {
    this(url);

    this.username = username;
    this.password = pwd;
  }


  // Public Instance Methods ----------------------------------------------------------------------
  
  public URL getUrl()
  {
    return url;
  }


  public String getUsername()
  {
    return username;
  }



  // Implements ExecutableCommand -----------------------------------------------------------------

  @Override public void send()
  {
    requestURL();
  }


  // Implements StatusCommand ---------------------------------------------------------------------

  @Override public String read(EnumSensorType sensorType, Map<String, String> stateMap)
  {
    String rawResult = requestURL();

    if (sensorType == null)
    {
       return rawResult;
    }

    if ("".equals(rawResult))
    {
       return UNKNOWN_STATUS;
    }

    switch (sensorType)
    {
      case SWITCH:

        rawResult = rawResult.trim();

        if (rawResult.equalsIgnoreCase("on"))
          return "on";

        else if (rawResult.equalsIgnoreCase("off"))
          return "off";

        else
          return UNKNOWN_STATUS;

        
      case RANGE:

        try
        {
          int rangeMin = Integer.MIN_VALUE;
          int rangeMax = Integer.MAX_VALUE;

          int result = resolveResultAsInteger(rawResult);


          if (stateMap != null)
          {
            rangeMin = resolveRangeMinimum(stateMap.get(Sensor.RANGE_MIN_STATE));
            rangeMax = resolveRangeMaximum(stateMap.get(Sensor.RANGE_MAX_STATE));
          }

          return resolveToRangeSensorValue(result, rangeMin, rangeMax);
        }

        catch (ConversionException e)
        {
          return UNKNOWN_STATUS;
        }


      case LEVEL:

        try
        {
          return resolveToLevelSensorValue(resolveResultAsInteger(rawResult));
        }

        catch (ConversionException e)
        {
          return UNKNOWN_STATUS;
        }

        
      default://NOTE: if sensor type is RANGE, this map only contains min/max states.

        for (String state : stateMap.keySet())
        {
          if (rawResult.equals(stateMap.get(state)))
          {
             return state;
          }
        }

      break;
    }

    return rawResult;
  }



  // Private Instance Methods ---------------------------------------------------------------------

  private String requestURL()
  {
    DefaultHttpClient client = new DefaultHttpClient();

    if (getUsername() != null)
    {
       CredentialsProvider cred = new BasicCredentialsProvider();

       cred.setCredentials(
           new AuthScope(AuthScope.ANY),
           new UsernamePasswordCredentials(getUsername(), new String(password))
       );

       client.setCredentialsProvider(cred);
    }

    HttpGet httpget = new HttpGet(url.toExternalForm());

    String resp = "";

    try
    {
      ResponseHandler<String> responseHandler = new BasicResponseHandler();
      resp = client.execute(httpget, responseHandler);
      logger.info("received message: " + resp);
    }

    catch (Exception e)
    {
      logger.error("HttpGetCommand could not execute", e);
    }

    return resp;
  }


  private int resolveRangeMinimum(String min)
  {
    if (min == null || min.equals(""))
    {
      return Integer.MIN_VALUE;
    }

    try
    {
      BigInteger minimum = new BigInteger(min);
      BigInteger integerMin = new BigInteger(Integer.toString(Integer.MIN_VALUE));

      if (minimum.compareTo(integerMin) < 0)
      {
        return Integer.MIN_VALUE;
      }

      else
      {
        return minimum.intValue();
      }
    }

    catch (NumberFormatException e)
    {
      // TODO : log

      return Integer.MIN_VALUE;
    }
  }



  private int resolveRangeMaximum(String max)
  {
    if (max == null || max.equals(""))
    {
      return Integer.MAX_VALUE;
    }

    try
    {
      BigInteger maximum = new BigInteger(max);
      BigInteger integerMax = new BigInteger(Integer.toString(Integer.MAX_VALUE));

      if (maximum.compareTo(integerMax) > 0)
      {
        return Integer.MAX_VALUE;
      }

      else
      {
        return maximum.intValue();
      }
    }

    catch (NumberFormatException e)
    {
      // TODO : log

      return Integer.MAX_VALUE;
    }
  }


  private String resolveToLevelSensorValue(int result)
  {
    if (result > 100)
    {
      return "100";
    }

    else if (result < 0)
    {
      return "0";
    }

    else
    {
      return Integer.toString(result);
    }
  }



  private int resolveResultAsInteger(String rawResult) throws ConversionException
  {
    try
    {
      BigInteger min = new BigInteger(Integer.toString(Integer.MIN_VALUE));
      BigInteger max = new BigInteger(Integer.toString(Integer.MAX_VALUE));

      BigInteger result = new BigInteger(rawResult);

      if (result.compareTo(min) < 0)
      {
        return Integer.MIN_VALUE;
      }

      else if (result.compareTo(max) > 0)
      {
        return Integer.MAX_VALUE;
      }

      else
      {
        return result.intValue();
      }
    }

    catch (NumberFormatException e)
    {
      throw new ConversionException(
          "Cannot parse device return value to Java integer: " + e.getMessage(), e
      );
    }
  }


  private String resolveToRangeSensorValue(int result, int rangeMinimum, int rangeMaximum)
  {
    if (result < rangeMinimum)
    {
      return Integer.toString(rangeMinimum);
    }

    else if (result > rangeMaximum)
    {
      return Integer.toString(rangeMaximum);
    }

    else
    {
      return Integer.toString(result);
    }

  }
}
