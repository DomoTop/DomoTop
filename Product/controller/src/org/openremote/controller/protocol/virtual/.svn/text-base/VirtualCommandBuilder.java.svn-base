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

import java.util.List;

import org.openremote.controller.command.CommandBuilder;
import org.openremote.controller.command.Command;
import org.openremote.controller.exception.NoSuchCommandException;
import org.jdom.Element;
import org.apache.log4j.Logger;

/**
 * Represents a virtual OpenRemote rooms/devices that can be used for demonstrations and
 * let users get quickly started without learning any particular automation protocol details.  <p>
 *
 * This is not any real protocol, but represents a simple abstraction of many procotols with
 * an address and command strings.
 *
 *
 * @author <a href="mailto:juha@openremote.org">Juha Lindfors</a>
 */
public class VirtualCommandBuilder implements CommandBuilder
{

  // Constants ------------------------------------------------------------------------------------

  /**
   * A common log category name intended to be used across all classes related to
   * OpenRemote virtual protocol implementation.
   */
  public final static String LOG_CATEGORY = "virtual";

  /**
   * String constant for parsing virtual protocol XML entries from controller.xml file.
   *
   * This constant is the expected property name value for virtual protocol addresses
   * (<code>{@value}</code>):
   *
   * <pre>{@code
   * <command protocol = "virtual" >
   *   <property name = "address" value = "1"/>
   *   <property name = "command" value = "ON"/>
   * </command>
   * }</pre>
   */
  public final static String XML_ADDRESS = "address";

  /**
   * String constant for parsing virtual protocol XML entries from controller.xml file.
   *
   * This constant is the expected property name value for virtual protocol commands ({@value}):
   *
   * <pre>{@code
   * <command protocol = "virtual" >
   *   <property name = "address" value = "1"/>
   *   <property name = "command" value = "ON"/>
   * </command>
   * }</pre>
   */
  public final static String XML_COMMAND = "command";



  // Class Members --------------------------------------------------------------------------------

  /**
   * Logging. Use common log category for all related classes.
   */
  private static Logger log = Logger.getLogger(LOG_CATEGORY);



  // Implements CommandBuilder --------------------------------------------------------------------

  /**
   * Parses the OpenRemote virtual room command XML snippets and builds a
   * corresponding virtual command instance.  <p>
   *
   * The expected XML structure is:
   *
   * <pre>{@code
   * <command protocol = "virtual" >
   *   <property name = "address" value = "1"/>
   *   <property name = "command" value = "ON"/>
   * </command>
   * }</pre>
   *
   *
   * @see VirtualCommand
   *
   * @throws org.openremote.controller.exception.NoSuchCommandException
   *            if the virtual command instance cannot be constructed from the XML snippet
   *            for any reason
   *
   * @return a virtual command instance with known configured properties set
   */
  public Command build(Element element)
  {
    String address = null;
    String command = null;

    // Properties come in as child elements...

    List<Element> propertyElements = element.getChildren(CommandBuilder.XML_ELEMENT_PROPERTY,
                                                         element.getNamespace());

    for (Element el : propertyElements)
    {
      String propertyName = el.getAttributeValue(CommandBuilder.XML_ATTRIBUTENAME_NAME);
      String propertyValue = el.getAttributeValue(CommandBuilder.XML_ATTRIBUTENAME_VALUE);

      if (XML_ADDRESS.equalsIgnoreCase(propertyName))
      {
        address = propertyValue;
      }
      else if (XML_COMMAND.equalsIgnoreCase(propertyName))
      {
        command = propertyValue;
      }
      else
      {
        log.warn(
            "Unknown virtual protocol property '<" + XML_ELEMENT_PROPERTY + " " +
            XML_ATTRIBUTENAME_NAME + " = \"" + propertyName + "\" " +
            XML_ATTRIBUTENAME_VALUE + " = \"" + propertyValue + "\"/>'."
        );
      }
    }

    // sanity checks...

    if (command == null || ("").equals(command))
    {
      throw new NoSuchCommandException(
         "OpenRemote virtual protocol command is missing '" + XML_COMMAND + "' property"
      );
    }

    if (address == null || ("").equals(address))
    {
      throw new NoSuchCommandException(
         "OpenRemote virtual protocol command is missing '" + XML_ADDRESS + "' property"
      );
    }

    String commandParam = element.getAttributeValue(Command.DYNAMIC_VALUE_ATTR_NAME);
    VirtualCommand cmd;

    if (commandParam == null || commandParam.equals(""))
    {
      cmd = new VirtualCommand(address, command);
    }
    else
    {
      cmd = new VirtualCommand(address, command, commandParam);
    }

    // Done!


    return cmd;
  }
}

