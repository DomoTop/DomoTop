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
package org.openremote.controller.protocol.lutron;

import java.util.Map;

import org.apache.log4j.Logger;
import org.openremote.controller.command.ExecutableCommand;
import org.openremote.controller.command.StatusCommand;
import org.openremote.controller.component.EnumSensorType;
import org.openremote.controller.exception.NoSuchCommandException;

/**
 * 
 * @author <a href="mailto:eric@openremote.org">Eric Bariaux</a>
 *
 */
public class KeypadCommand extends LutronHomeWorksCommand implements ExecutableCommand, StatusCommand {

  // Class Members --------------------------------------------------------------------------------

  /**
   * Lutron logger. Uses a common category for all Lutron related logging.
   */
  private final static Logger log = Logger.getLogger(LutronHomeWorksCommandBuilder.LUTRON_LOG_CATEGORY);

  public static LutronHomeWorksCommand createCommand(String name, LutronHomeWorksGateway gateway, LutronHomeWorksAddress address, Integer scene, Integer key, Integer level) {
    // Check for mandatory attributes
    if (address == null) {
      throw new NoSuchCommandException("Address is required for any Keypad command");
    }

    if (key == null) {
      throw new NoSuchCommandException("Key is required for any Keypad command");
    }

    if (!address.isValidKeypadAddress()) {
      throw new NoSuchCommandException("Address must be one of a Keypad");
    }

    return new KeypadCommand(name, gateway, address, key);
  }

  // Private Instance Fields ----------------------------------------------------------------------

  /**
   * Destination address for this command.
   */
  private LutronHomeWorksAddress address;

  /**
   * Number of key on the keypad this command must act upon.
   */
  private Integer key;

  // Constructors ---------------------------------------------------------------------------------

  public KeypadCommand(String name, LutronHomeWorksGateway gateway, LutronHomeWorksAddress address, Integer key) {
    super(name, gateway);
    this.address = address;
    this.key = key;
  }

  // Implements ExecutableCommand -----------------------------------------------------------------

  /**
   * {@inheritDoc}
   */
  public void send() {
    log.info(">>KeypadCommand.send");
    try {
      Keypad keypad = (Keypad) gateway.getHomeWorksDevice(address, Keypad.class);
      log.info("Will execute command for keypad " + keypad);
      if ("PRESS".equals(name)) {
        keypad.press(key);
      } else if ("RELEASE".equals(name)) {
        keypad.release(key);
      } else if ("HOLD".equals(name)) {
        keypad.hold(key);
      } else if ("DOUBLE_TAP".equals(name)) {
        keypad.doubleTap(key);
      }
    } catch (LutronHomeWorksDeviceException e) {
      log.error("Impossible to get device", e);
    }
  }

  // Implements StatusCommand -------------------------------------------------------------------

  @Override
  public String read(EnumSensorType sensorType, Map<String, String> stateMap) {
    try {
      Keypad keypad = (Keypad) gateway.getHomeWorksDevice(address, Keypad.class);
      if (keypad == null) {
        // This should never happen as above command is supposed to create device
        log.warn("Gateway could not create a Keypad we're receiving feedback for (" + address + ")");
        return "";
      }
      if (keypad.getLedStatuses() == null) {
        // We don't have any information about the state yet, ask Lutron processor about it
        keypad.queryLedStatus();
        return "";
      }
      if (keypad.getLedStatuses()[key - 1] == null) {
        // We don't have any information about the state yet, ask Lutron processor about it
        keypad.queryLedStatus();
        return "";
      }
      if (sensorType == EnumSensorType.SWITCH) {
        return (keypad.getLedStatuses()[key - 1] == 1) ? "on" : "off";
      } else {
        log.warn("Query Keypad status for incompatible sensor type " + sensorType);
        return "";
      }
    } catch (LutronHomeWorksDeviceException e) {
      log.error("Impossible to get device", e);
    }
    return "";
  }

}
