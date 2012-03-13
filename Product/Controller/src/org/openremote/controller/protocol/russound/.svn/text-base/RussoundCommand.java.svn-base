/*
 * OpenRemote, the Home of the Digital Home. Copyright 2008-2011, OpenRemote Inc.
 * 
 * See the contributors.txt file in the distribution for a full listing of individual contributors.
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package org.openremote.controller.protocol.russound;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.openremote.controller.RussoundConfiguration;
import org.openremote.controller.command.ExecutableCommand;
import org.openremote.controller.command.StatusCommand;
import org.openremote.controller.component.EnumSensorType;
import org.openremote.controller.exception.NoSuchCommandException;
import org.openremote.controller.service.ServiceContext;
import org.openremote.controller.spring.SpringContext;

/**
 * Parses the Russound command XML element and builds a corresponding Russound command instance.
 * <p>
 * 
 * The expected XML structure is:
 * 
 * <pre>
 * @code
 * <command id="" protocol = "russound" >
 *   <property name="command" value="" />
 * </command>
 * }
 * </pre>
 * 
 * Additional properties not listed here are ignored.
 * 
 * @throws NoSuchCommandException if the Russound command instance cannot be constructed from the
 *           XML element for any reason
 * 
 * @return an immutable Russound command instance with known configured properties set
 */
public class RussoundCommand implements ExecutableCommand, StatusCommand
{

  /** The logger. */
  private static Logger logger = Logger.getLogger(RussoundCommand.class.getName());

  enum ConnectionType
  {
    RS232, UDP
  };

  /** The command to perform the http get request on */
  private String command;

  /**
   * Gets the command
   * 
   * @return the command
   */
  public String getCommand()
  {
    return command;
  }

  /**
   * Sets the command
   * 
   * @param command the new command
   */
  public void setCommand(String command)
  {
    this.command = command;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void send()
  {
    sendCommand();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String read(EnumSensorType sensoryType, Map<String, String> stateMap)
  {
    // Not implemented, return an empty message
    return "";
  }

  private void sendCommand()
  {
    try
    {
      RussoundConfiguration conf = ServiceContext.getRussoundConfiguration();

      ConnectionType connectionType = ConnectionType.valueOf(conf.getConnectionType());

      byte[] dataBytes = getCommandAsByteArray();

      logger.info("Command: " + getCommand() + " DataBytes: " + dataBytes);

      if (ConnectionType.UDP == connectionType)
      {
        InetAddress udpAddress = InetAddress.getByName(conf.getUdpIp());
        int udpPort = conf.getUdpPort();

        sendCommandUDP(dataBytes, udpAddress, udpPort);
      } else
      {
        String comPort = conf.getComPort();

        sendCommandComPort(dataBytes, comPort);
      }
    } catch (Exception e)
    {
      logger.error("Could not send command: " + getCommand(), e);
    }
  }

  private void sendCommandComPort(byte[] dataBytes, String comPort)
  {
    try
    {
      CommPortIdentifier id = CommPortIdentifier.getPortIdentifier(comPort);
      SerialPort serialPort = (SerialPort) id.open("ORBController", 2000);
      serialPort.setSerialPortParams(19200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
          SerialPort.PARITY_NONE);
      OutputStream outputStream = serialPort.getOutputStream();
      outputStream.write(dataBytes);
      outputStream.close();
      serialPort.close();
    } catch (Exception e)
    {
      logger.error("Error sending serial command", e);
    }
  }

  private void sendCommandUDP(byte[] dataBytes, InetAddress udpAddress, int udpPort)
  {
    Socket socket = null;
    try
    {
      DatagramSocket clientSocket = new DatagramSocket();
      DatagramPacket sendPacket = new DatagramPacket(dataBytes, dataBytes.length, udpAddress,
          udpPort);
      clientSocket.send(sendPacket);
    } catch (Exception e)
    {
      logger.error("Error sending serial command over UDP", e);
    } finally
    {
      if (socket != null)
      {
        try
        {
          socket.close();
        } catch (IOException e)
        {
          logger.error("Error closing socket", e);
        }
      }
    }
  }

  private byte[] getCommandAsByteArray()
  {
    Properties russoundCommands = (Properties) SpringContext.getInstance()
        .getBean("russoundCommands");

    String commandHexString = russoundCommands.getProperty(getCommand());
    if (commandHexString == null)
    {
      throw new NoSuchCommandException("No Command: " + getCommand());
    }
    byte[] dataBytes = hexStringToByteArray(commandHexString.replaceAll(" ", "").toLowerCase());
    return dataBytes;
  }

  private byte[] hexStringToByteArray(String s)
  {
    int len = s.length();
    byte[] data = new byte[len / 2];
    for (int i = 0; i < len; i += 2)
    {
      data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(
          s.charAt(i + 1), 16));
    }
    return data;
  }
}
