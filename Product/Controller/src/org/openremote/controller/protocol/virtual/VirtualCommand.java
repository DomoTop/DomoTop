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
package org.openremote.controller.protocol.virtual;

import java.util.Map;
import java.util.HashMap;

import org.openremote.controller.command.StatusCommand;
import org.openremote.controller.command.ExecutableCommand;
import org.openremote.controller.component.EnumSensorType;
import org.apache.log4j.Logger;

/**
 * OpenRemote virtual command implementation.  <p>
 *
 * Maintains a virtual-machine-wide state for each address. TODO <p>
 *
 *
 * @author <a href="mailto:juha@openremote.org">Juha Lindfors</a>
 */
public class VirtualCommand implements ExecutableCommand, StatusCommand
{

  // Class Members --------------------------------------------------------------------------------

  /**
   * Map of address to state.
   */
  private final static Map<String, String>virtualDevices = new HashMap<String, String>(20);

  /**
   * Logging. Use common log category for all related classes.
   */
  private final static Logger log = Logger.getLogger(VirtualCommandBuilder.LOG_CATEGORY);




  // Instance Fields ------------------------------------------------------------------------------

  /**
   * The address for this particular 'command' instance.
   */
  private String address = null;

  /**
   * The command string for this particular 'command' instance.
   */
  private String command = null;


  private String commandParam = null;



  // Constructors ---------------------------------------------------------------------------------

  public VirtualCommand(String address, String command)
  {
    this.address = address;
    this.command = command;
  }

  public VirtualCommand(String address, String command, String commandParam)
  {
    this(address, command);

    this.commandParam = commandParam;
  }


  // Implements ExecutableCommand -----------------------------------------------------------------

  public void send()
  {
    if (commandParam == null)
    {
      virtualDevices.put(address, command);
    }

    else
    {
      virtualDevices.put(address, commandParam);
    }

  }



  // Implements StatusCommand ---------------------------------------------------------------------

  public String read(EnumSensorType sensorType, Map<String, String> stateMap)
  {
    String state = virtualDevices.get(address);

    switch (sensorType)
    {

      case SWITCH:

        if (state == null)
        {
          return "off";
        }

        else if (state.trim().equalsIgnoreCase("on"))
        {
          return "on";
        }

        else if (state.trim().equalsIgnoreCase("off"))
        {
          return "off";
        }

        else
        {
          log.warn("Was expecting either 'on' or 'off' for 'switch' type sensor, got " + state);

          return "off";
        }


      case LEVEL:

        if (state == null)
        {
          return "0";
        }

        else
        {
          try
          {
            int value = Integer.parseInt(state.trim());

            if (value > 100)
              return "100";

            if (value < 0)
              return "0";

            return "" + value;
          }
          catch (NumberFormatException e)
          {
            log.warn("Can't parse LEVEL sensor value into a valid number: " + e.getMessage(), e);

            return "0";
          }
        }


      case RANGE:

        if (state == null)
        {
          return "0";
        }

        else
        {
          try
          {
            int value = Integer.parseInt(state.trim());

            return "" + value;
          }
          catch (NumberFormatException e)
          {
            log.warn("Can't parse RANGE sensor value into a valid number: " + e.getMessage(), e);

            return "0";
          }
        }


      default:

        return "";
    }
  }
}

