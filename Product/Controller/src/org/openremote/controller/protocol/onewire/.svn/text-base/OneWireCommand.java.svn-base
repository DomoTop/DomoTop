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
package org.openremote.controller.protocol.onewire;

import org.openremote.controller.command.ExecutableCommand;
import org.openremote.controller.command.StatusCommand;
import org.openremote.controller.component.EnumSensorType;
import org.openremote.controller.component.Sensor;
import org.openremote.controller.utils.Logger;
import org.owfs.jowfsclient.Enums.*;
import org.owfs.jowfsclient.OwfsClient;
import org.owfs.jowfsclient.OwfsClientFactory;
import org.owfs.jowfsclient.OwfsException;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Map;

/**
 * 1-wire protocol implementation for OpenRemote. Program accesses 1-wire network (owserver) via
 * library jowfsclient.
 *
 * Parameters used:
 *
 * hostname        hostname of owserver
 * port            port where owserver is running, by default it is 4304
 * deviceAddress   address of the 1-wire device, such as /1F.E9E803000000/main/28.25E9E3010000
 * filename        or sensor attribute - is filename in owfs that holds values, such as
 *                 "temperature", "temperature9", "humidity" or similar
 * refreshTime     time interval in seconds between two calls to owserver; any request between
 *                 these two calls will use cached value. In fact, it is cache timeout value.
 *
 * @author <a href="mailto:jmisura@gmail.com">Jaroslav Misura</a>
 */
public class OneWireCommand implements ExecutableCommand, StatusCommand
{

  // Class Members --------------------------------------------------------------------------------

  private final static Logger logger = Logger.getLogger(OneWireCommandBuilder.ONEWIRE_PROTOCOL_LOG_CATEGORY);


  // Instance Fields ------------------------------------------------------------------------------

  private String hostname;
  private int port = 4304;
  private String deviceAddress;
  private String filename;
  private long refreshTime = 0;

  private String lastValue = UNKNOWN_STATUS;
  private long lastUpdateTimestamp = 0;



  // Constructors ---------------------------------------------------------------------------------

  public OneWireCommand(String hostname, int port, String deviceAddress, String filename, long refreshTime)
  {
    this.hostname = hostname;
    this.port = port;
    this.deviceAddress = deviceAddress;
    this.filename = filename;
    this.refreshTime = refreshTime;

    logger.debug("OneWireCommand created with values hostname=" + hostname +
               "; port=" + port + "; deviceAddress=" + deviceAddress + "; filename=" +filename +
               "; refreshTime=" + refreshTime);
  }


  // Implements StatusCommand ---------------------------------------------------------------------

  /**
   * Access value from 1-wire sensor. This method uses caching to avoid overloading 1-wire network with
   * too many accesses. After value is read it is stored together with timestamp. This value is returned in all
   * succeeding calls of read() until current time > timestamp + refresh timeout. If read fails (read from 1-wire
   * returns null or exception is thrown by jowfs library) last available value is returned. In this case, timestamp
   * is not updated, which means that next read() call will retry to get value from 1-wire.
   * <p>
   * Values from 1-wire sensors are processed following way:
   *
   * <ul>
   * <li>SWITCH type sensor returns "on" (when value in 1-Wire file is equal to "1") or "off" (any other value) strings
   * <li>LEVEL type sensor returns value rounded to INTEGER in range 0-100.
   * <li>RANGE type sensor returns value rounded to 1 decimal place within specified range.
   * <li>Any other type of sensor returns UNKNOWN_STATUS
   * </ul>
   *
   * @param sensorType
   *           sensor type
   * @param stateMap
   *           state map, key is state name, value is the returned raw string related to the state.
   *           NOTE: if sensor type is RANGE, this map only contains min/max states.
   * @return string value in format dependent on selected sensor type
   */
  public String read(EnumSensorType sensorType, Map<String, String> stateMap)
  {
    logger.debug("Reading sensor " + deviceAddress + "/" + filename);

    long timestamp = System.currentTimeMillis();

    // check if we should read now or use cached value
    if (timestamp > lastUpdateTimestamp + refreshTime)
    {
      logger.debug("1-Wire sensor " + deviceAddress + "/" + filename + " value is going to be read.");
      OwfsClient client = OwfsClientFactory.newOwfsClient(hostname, port, true);

      client.setDeviceDisplayFormat(OwDeviceDisplayFormat.OWNET_DDF_F_DOT_I);
      client.setBusReturn(OwBusReturn.OWNET_BUSRETURN_ON);
      client.setPersistence(OwPersistence.OWNET_PERSISTENCE_ON);
      client.setTemperatureScale(OwTemperatureScale.OWNET_TS_CELSIUS);
      String value = null;

      logger.debug("Client created, before call");

      try
      {
        value = client.read(deviceAddress+"/"+filename);
      }

      catch (OwfsException e)
      {
        logger.error("OneWire error, unable to read from OWSERVER.", e);
        value = null;
      }

      catch (IOException e)
      {
        logger.error("OneWire IO error, unable to read from OWSERVER.", e);
        value = null;
      }

      logger.debug("After client call, value = '"+value+"'");
      String valueFormatted = null;

      if (null == value)
      {
        // we were not able to read value, so let's use old one
        // do not update lastUpdateTimestamp, we will retry to read value in next turn
        return lastValue;
      }

      value = value.trim();



      switch (sensorType)
      {
        case SWITCH:
          valueFormatted = processSwitchSensor(value);
          break;
        case LEVEL:
          valueFormatted = processLevelSensor(value);
          break;
        case RANGE:
          valueFormatted = processRangeSensor(value, stateMap);
          break;
        default:
          valueFormatted = UNKNOWN_STATUS;
      }

      logger.debug("Sensor " + deviceAddress + "/" + filename + " returns value '"+ valueFormatted + "' (original = '"+value+"')");

      lastValue = valueFormatted;
      lastUpdateTimestamp = System.currentTimeMillis();

      return valueFormatted;
    }

    else
    {
      logger.debug(
          "1-Wire sensor " + deviceAddress + "/" + filename +
          " value is still within cache timeout limit -> No update."
      );

      return lastValue;
    }
  }


  // Implements ExecutableCommand -----------------------------------------------------------------

  /**
   * Currently useable only for 1-wire SWITCH devices. It reads current value of switch, negate it and write this
   * new value back to 1-wire device.
   *
   */
  public void send()
  {

    logger.debug("1-Wire sensor " + deviceAddress + "/" + filename + " value is going to be changed.");

    OwfsClient client = OwfsClientFactory.newOwfsClient(hostname, port, true);

    client.setDeviceDisplayFormat(OwDeviceDisplayFormat.OWNET_DDF_F_DOT_I);
    client.setBusReturn(OwBusReturn.OWNET_BUSRETURN_ON);
    client.setPersistence(OwPersistence.OWNET_PERSISTENCE_ON);
    client.setTemperatureScale(OwTemperatureScale.OWNET_TS_CELSIUS);

    try
    {
      String currentStatus = client.read(deviceAddress+"/"+filename);

      // Currently only write to switches is supported. If we read "1", then switch is "on" and we will write "off"=>"0"
      // otherwise switch is now "off", so we will write "on"=>"1"

      String newStatus = "1";
      if("1".equals(currentStatus))
      {
        newStatus = "0";
      }

      logger.debug("Current value of 1-Wire switch "+deviceAddress+"/"+filename+" is '"+currentStatus+
          "', new value will be '"+newStatus+"'.");

      client.write(deviceAddress+"/"+filename, newStatus);
    }
    catch (OwfsException e)
    {
      logger.error("OneWire error, unable to write to OWSERVER.", e);
    }
    catch (IOException e)
    {
      logger.error("OneWire IO error, unable to write to OWSERVER.", e);
    }
  }



  private String processRangeSensor(String value, Map<String, String> stateMap)
  {

    String valueFormatted = null;

    try
    {

      double doubleValue = Double.parseDouble(value);

      double rangeMin = Double.MIN_VALUE;
      double rangeMax = Double.MAX_VALUE;

      if (stateMap != null)
      {
        rangeMin = getRangeMin(stateMap.get(Sensor.RANGE_MIN_STATE));
        rangeMax = getRangeMax(stateMap.get(Sensor.RANGE_MAX_STATE));
      }

      if(doubleValue>rangeMax)
      {
        doubleValue = rangeMax;
      } else if(doubleValue<rangeMin)
      {
        doubleValue = rangeMin;
      }


      NumberFormat nf = new DecimalFormat("#0.0");
      valueFormatted = nf.format(doubleValue);
      return valueFormatted;

    } catch (NumberFormatException e) {
      logger.info("Unable to parse value '"+value+"' to process it as range, returning UNKNOWN_STATUS.");
      return UNKNOWN_STATUS;
    }

  }




  private String processLevelSensor(String value)
  {
    String valueFormatted = null;

    try
    {
      double doubleValue = Double.parseDouble(value);

      long longValue = Math.round(doubleValue);
      if(longValue>=100)
      {
        return "100";
      } else if(longValue<=0)
      {
        return "0";
      } else
      {
        return String.valueOf(longValue);
      }
    } catch(NumberFormatException e) {
      logger.info("Unable to parse value '"+value+"' to process it as level, returning UNKNOWN_STATUS.");
      return UNKNOWN_STATUS;
    }
  }

  private String processSwitchSensor(String value)
  {
    if ("1".equals(value))
    {
      return "on";
    } else
    {
      return "off";
    }
  }

  private double getRangeMin(String strValue)
  {
    try
    {
      return Double.parseDouble(strValue);
    } catch (NumberFormatException e) {
      logger.info("Unable to parse double from range min value '"+strValue+"'");
    }

    return Double.MIN_VALUE;
  }

  private double getRangeMax(String strValue)
  {
    try
    {
      return Double.parseDouble(strValue);
    } catch (NumberFormatException e) {
      logger.info("Unable to parse double from range max value '"+strValue+"'");
    }

    return Double.MAX_VALUE;
  }

}
