/* OpenRemote, the Home of the Digital Home.
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
package org.openremote.controller.protocol.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Map;
import java.util.StringTokenizer;
import org.apache.log4j.Logger;
import org.openremote.controller.command.ExecutableCommand;
import org.openremote.controller.command.StatusCommand;
import org.openremote.controller.component.EnumSensorType;

/**
 * The Socket Event.
 *
 * @author Rich Turner 2011-04-15
 */
public class UDPSocketCommand implements ExecutableCommand, StatusCommand {

   /** The logger. */
   private static Logger logger = Logger.getLogger(UDPSocketCommand.class.getName());

   /** A name to identify event in controller.xml. */
   private String name;

   /** A pipe separated list of command string that are sent over the socket */
   private String command;

   /** The IP to which the socket is opened */
   private String ip;

   /** The port that is opened */
   private String port;


   /**
    * Gets the command.
    *
    * @return the command
    */
   public String getCommand() {
      return command;
   }

   /**
    * Sets the command.
    *
    * @param command the new command
    */
   public void setCommand(String command) {
      this.command = command;
   }

   /**
    * Gets the name.
    *
    * @return the name
    */
   public String getName() {
      return name;
   }

   /**
    * Sets the name.
    *
    * @param name the new name
    */
   public void setName(String name) {
      this.name = name;
   }


   /**
    * Gets the ip
    * @return the ip
    */
   public String getIp() {
      return ip;
   }

   /**
    * Sets the ip
    * @param ip the new ip
    */
   public void setIp(String ip) {
      this.ip = ip;
   }

   /**
    * Gets the port
    * @return the port
    */
   public String getPort() {
      return port;
   }

	/**
	 * Sets the port
	 * @param port the new port
	 */
   public void setPort(String port) {
      this.port = port;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void send() {
      try {
      	// Create socket no need to specify port for sending
      	DatagramSocket socket = new DatagramSocket();
      	
      	// Create packet
      	InetAddress address = InetAddress.getByName(getIp());
      	int port = Integer.parseInt(getPort());
      	byte[] bytes = getCommand().getBytes();
      	DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, port);
      	
      	// Send the packet
      	socket.send(packet);
      } catch (Exception e) {
      
      }
   }

   @Override
   public String read(EnumSensorType sensoryType, Map<String, String> stateMap) {
      return "";
   }

}
