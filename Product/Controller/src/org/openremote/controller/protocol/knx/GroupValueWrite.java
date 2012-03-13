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
package org.openremote.controller.protocol.knx;

import org.openremote.controller.command.ExecutableCommand;
import org.openremote.controller.command.CommandParameter;
import org.openremote.controller.protocol.knx.datatype.Bool;
import org.openremote.controller.protocol.knx.datatype.DataPointType;
import org.openremote.controller.exception.NoSuchCommandException;
import org.openremote.controller.exception.ConversionException;

/**
 * Write command representing KNX Group Value Write service. This class implements the
 * {@link ExecutableCommand} interface and therefore acts as an entry point in
 * controller/protocol SPI.
 *
 * @author <a href="mailto:juha@openremote.org">Juha Lindfors</a>
 */
class GroupValueWrite extends KNXCommand implements ExecutableCommand
{


  // Constants ------------------------------------------------------------------------------------

  final static int DIMCONTROL_INCREASE_VALUE = 7;
  final static int DIMCONTROL_DECREASE_VALUE = 7;

  
  // Class Members --------------------------------------------------------------------------------


  /**
   * Factory method for creating new KNX write command instances based on user configured
   * command name. The command name must be one of the pre-specified command names in this class.
   *
   * @param name      User-configured command name used in tools and configuration files. This
   *                  name is mapped to a typed KNX Application Protocol Data Unit instance.
   * @param dpt       KNX datapoint type associated with this command
   * @param mgr       Connection manager reference this command will use for transmission.
   * @param address   Destination group address for this command.
   * @param parameter parameter for this command or <tt>null</tt> if not available
   *
   * @return  a new KNX write command instance, or <code>null</code> if the lookup name could not
   *          be matched to any command
   */
  static GroupValueWrite createCommand(String name, DataPointType dpt, KNXIpConnectionManager mgr,
                                       GroupAddress address, CommandParameter parameter)
  {
    name = name.trim().toUpperCase();

    ApplicationProtocolDataUnit apdu = Lookup.get(name, parameter);

    if (apdu == null)
      return null;
    
    return new GroupValueWrite(mgr, address, apdu, dpt);
  }



  // Constructors ---------------------------------------------------------------------------------

  /**
   * Constructs a new KNXWriteCommand instance with a given connection manager, group address and
   * application protocol data unit.
   *
   * @param connectionManager   connection manager used to send this KNX command
   * @param groupAddress        destination group address for this command
   * @param apdu                APDU payload for this command
   * @param dpt                 KNX datapoint type associated with this command
   */
  private GroupValueWrite(KNXIpConnectionManager connectionManager, GroupAddress groupAddress,
                          ApplicationProtocolDataUnit apdu, DataPointType dpt)
  {
    super(connectionManager, groupAddress, apdu, dpt);
  }


  // Implements ExecutableCommand -----------------------------------------------------------------

  /**
   * {@inheritDoc}
   */
  public void send()
  {
    // delegate to super class...

    super.write(this);
  }
    

  // Nested Classes -------------------------------------------------------------------------------

  /**
   * Simple helper class to lookup user configured command names and match them to Java instances.
   */
  private static class Lookup
  {

    /**
     * Lookup from user defined command strings in the designer (from which they end up
     * into controller.xml) to type safe APDUs for KNX CEMI frames.
     *
     * @param   name        lookup name
     * @param   parameter   command parameter, or <tt>null</tt> if not available
     *
     * @return  complete application protocol data unit with control information (APCI) and data,
     *          or <tt>null</tt> if command was not found by name
     */
    private static ApplicationProtocolDataUnit get(String name, CommandParameter parameter)
    {
      /*
       * IMPLEMENTATION NOTE:
       *
       *   when new valid values for command names are added, the unit tests should be added
       *   accordingly into KNXCommandBuilderTest
       *
       * TODO : add unit tests for RANGE (ORJAVA-69)
       *
       * TODO : add the rest of the boolean datatypes (UP/DOWN, DISABLE/ENABLE, etc.) (ORCJAVA-70)
       * 
       * TODO :
       *        some commands (DIM_INCREASE|DECREASE, RANGE, etc) should also allow
       *        parameterization so can have pre-fixed values (use additional command property?)
       *        (ORCJAVA-71)
       */
      name = name.toUpperCase().trim();

      if (name.equals("ON") ||
          name.equals("SWITCH ON"))
      {
        return ApplicationProtocolDataUnit.WRITE_SWITCH_ON;
      }

      else if (name.equals("OFF") ||
               name.equals("SWITCH OFF"))
      {
        return ApplicationProtocolDataUnit.WRITE_SWITCH_OFF;
      }

      else if (name.equals("DIM_INCREASE")  ||
               name.equals("DIM INCREASE"))
      {
        return ApplicationProtocolDataUnit.create3BitDimControl(
            Bool.INCREASE,
            DIMCONTROL_INCREASE_VALUE
        );
      }

      else if (name.equals("DIM_DECREASE")  ||
               name.equals("DIM DECREASE"))
      {
        return ApplicationProtocolDataUnit.create3BitDimControl(
            Bool.DECREASE,
            DIMCONTROL_DECREASE_VALUE
        );
      }

      else if (name.equals("SCALE") ||
               name.equals("DIM"))
      {
        if (parameter == null)
        {
          throw new NoSuchCommandException("Missing value parameter for SCALE command.");
        }

        try
        {
          return ApplicationProtocolDataUnit.createScaling(parameter);
        }
        catch (ConversionException e)
        {
          throw new NoSuchCommandException(e.getMessage(), e);
        }
      }

      else if (name.equals("RANGE"))
      {
        if (parameter == null)
        {
          throw new NoSuchCommandException("Missing value parameter for RANGE command.");
        }

        try
        {
          return ApplicationProtocolDataUnit.createRange(parameter);
        }
        catch (ConversionException e)
        {
          throw new NoSuchCommandException(e.getMessage(), e);
        }
      }

      else
      {
        return null;                    // according to javadoc
      }
    }
  }
}
