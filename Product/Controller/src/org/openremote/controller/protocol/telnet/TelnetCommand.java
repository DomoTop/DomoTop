/* OpenRemote, the Home of the Digital Home.
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
package org.openremote.controller.protocol.telnet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.regex.PatternSyntaxException;
import org.apache.commons.net.telnet.TelnetClient;
import org.apache.log4j.Logger;
import org.openremote.controller.command.ExecutableCommand;
import org.openremote.controller.command.StatusCommand;
import org.openremote.controller.component.EnumSensorType;

/**
 * The Telnet Event.
 * 
 * @author Marcus 2009-4-26
 * @modified by Rich Turner 2011-2-5
 */
public class TelnetCommand implements ExecutableCommand, StatusCommand {

   /** The logger. */
   private static Logger logger = Logger.getLogger(TelnetCommand.class.getName());

   /** The default timeout used to wait for a result */
   public static final int DEFAULT_TIMEOUT = 1;

   /** A name to identify event in controller.xml. */
   private String name;

   /**
    * A pipe separated list of command strings that are sent over the connection It must have the format
    * <waitFor>|<send>|<waitFor>|<send>
    */
   private String command;

   /** The IP to which the socket is opened */
   private String ip;

   /** The port that is opened */
   private String port;

   /** The response from read command */
   private String response;

   /** The response filter */
   private String responseFilter;
   
   /** The response fliter group */
   private Integer responseFilterGroup = 0;
   
   /** The status default response */
   private String statusDefault;
   
   /** The wait timeout period in seconds */
   private Integer timeOut = DEFAULT_TIMEOUT;
   
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
    * @param command
    *           the new command
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
    * @param name
    *           the new name
    */
   public void setName(String name) {
      this.name = name;
   }

   /**
    * Gets the ip
    * 
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
    * 
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
    * Sets the response string 
    * @param response the new response
    */
   public void setResponse(String response) {
      this.response = response;
   }

   /**
    * Gets the response 
    * @return the response
    */
   public String getResponse() {
      return response;
   }

   /**
    * Sets the response filter
    * @param responseFilter the new responseFilter
    */   
   public void setResponseFilter(String responseFilter) {
      this.responseFilter = responseFilter;
   }
   
   /**
    * Gets the responseFilter
    * @return the responseFilter
    */
   public String getResponseFilter() {
      return responseFilter;  
   }

   /**
    * Sets the timeOut
    * @param timeOut the new timeOut
    */   
   public void setTimeOut(String timeOut) {
      try {
         Integer tempValue = Integer.parseInt(timeOut.trim());
         if (tempValue > 0) {
            this.timeOut = tempValue;
         }
      }
      catch (NumberFormatException e) {
         logger.error("time out property in controller.xml is not an integer", e);
      }
   }
   
   /**
    * Gets the timeOut
    * @return the timeOut
    */
   public Integer getTimeOut() {
      return timeOut;  
   }
      
   /**
    * Sets the response filter group
    * @param responseFilterGroup the new responseFilterGroup
    */
   public void setResponseFilterGroup(String responseFilterGroup) {
      try {
         this.responseFilterGroup = Integer.parseInt(responseFilterGroup.trim());
      }
      catch (NumberFormatException e) {
         logger.error("filter group property in controller.xml is not an integer", e);
      }
   }

   /**
    * Gets the response filter group
    * @return the response
    */
   public Integer getResponseFilterGroup() {
      return responseFilterGroup;   
   }

   /**
    * Sets the status default
    * @param statusDefault the new statusDefault
    */   
   public void setStatusDefault(String statusDefault) {
      this.statusDefault = statusDefault;  
   }
   
   /**
    * Gets the statusDefault
    * @return the statusDefault
    */
   public String getStatusDefault() {
      return statusDefault;
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public void send() {
      send(false);
   }

   public void send(boolean readResponse) {
      TelnetClient tc = null;
      if (readResponse) {
         setResponse("");
      }
      try {
         tc = new TelnetClient();
         tc.connect(getIp(), Integer.parseInt(getPort()));
         StringTokenizer st = new StringTokenizer(getCommand(), "|");
         int count = 0;
         if(getCommand().startsWith("|")) {
            count++;
         }
         String waitFor = "";
         while (st.hasMoreElements()) {
            String cmd = (String) st.nextElement();
            if (count % 2 == 0) {
               waitFor = cmd;
               if (!"null".equals(cmd)) {
                  waitForString(cmd, tc);
               }
            } else {
               sendString(cmd, tc);
               if (readResponse) {
                  readString(waitFor, tc);
               }
            }
            count++;
         }
      } catch (Exception e) {
         logger.error("could not perform telnetEvent", e);
      } finally {
         if (tc != null) {
            try {
               tc.disconnect();
            } catch (IOException e) {
               logger.error("could not disconnect from telnet", e);
            }
         }
      }
   }

   /**
    * Read from the telnet session until the string we are waiting for is found or the timeout has been reached. The
    * timeout is 1 second
    * 
    * @param s The string to wait on
    * @param tc The instance of the TelnetClient
    */
   private void waitForString(String s, TelnetClient tc) throws Exception {
      InputStream is = tc.getInputStream();
      StringBuffer sb = new StringBuffer();
      Calendar endTime = Calendar.getInstance();
      endTime.add(Calendar.SECOND, getTimeOut());
      while (sb.toString().indexOf(s) == -1) {
         while (Calendar.getInstance().before(endTime) && is.available() == 0) {
            Thread.sleep(250);
         }
         if (is.available() == 0) {
            logger.info("WaitForString read before running into timeout: " + sb.toString());
            throw new Exception("waitForString response timed-out waiting for \"" + s + "\"");
         }
         sb.append((char) is.read());
      }
      logger.info("WaitForString received: " + sb.toString());
   }

   /**
    * Write this string to the telnet session.
    * 
    * @param the string to write
    * @param tc The instance of the TelnetClient
    */
   private void sendString(String s, TelnetClient tc) throws Exception {
      OutputStream os = tc.getOutputStream();
      os.write((s + "\n").getBytes());
      logger.info("send: " + s);
      os.flush();
   }

   private void readString(String s, TelnetClient tc) throws Exception {
      InputStream is = tc.getInputStream();
      StringBuffer sb = new StringBuffer();
      Calendar endTime = Calendar.getInstance();
      endTime.add(Calendar.SECOND, getTimeOut());
      while (sb.toString().indexOf(s) == -1) {
         while (Calendar.getInstance().before(endTime) && is.available() == 0) {
            Thread.sleep(250);
         }
         if (is.available() == 0) {
            if ("null".equals(s)) {
               break;
            }
            logger.info("Read before running into timeout: " + sb.toString());
            throw new Exception("Response timed-out waiting for \"" + s + "\"");
         }
         sb.append((char) is.read());
      }
      logger.info("received: " + sb.toString());
      this.response += sb.toString() + "\n";
   }

   @Override
   public String read(EnumSensorType sensorType, Map<String, String> statusMap) {
	   String readResponse = statusDefault;
	   String filteredResponse = "";
	   send(true);
	   
	   try {
   	   if ("".equals(getResponseFilter()) || getResponseFilter() == null) {
      	   filteredResponse = getResponse();
   	   } else {
            Pattern p = Pattern.compile(getResponseFilter(), Pattern.CANON_EQ | Pattern.UNICODE_CASE);
            Matcher m = p.matcher(getResponse());
            boolean b = m.matches();
            if (b) {
               String matchedGroup = m.group(getResponseFilterGroup());
               if (matchedGroup != null) {
                  filteredResponse = matchedGroup;  
               }
            } else {
               logger.error("Telnet Read Status: No Match using Regex: '" + getResponseFilter() + "' on response from command '" + getCommand() + "'");
            } 
         }            
      }
      catch (PatternSyntaxException e) {
         System.out.println("Telnet Read Status: REGEX ERROR");
         logger.error("Telnet Read Status: Invalid filter expression", e);
      }
      
      if (!"".equals(filteredResponse)) {
         switch (sensorType) {
            // Switch: on or off response needed
            case SWITCH:
               filteredResponse.replaceAll("1|on", "true");
               Boolean bool = Boolean.parseBoolean(filteredResponse);
               if (bool) {
                  readResponse = "on";
               } else {
                  readResponse = "off";  
               }
               break;
            case LEVEL:
            case RANGE:
               try {
                  Integer intVal = Integer.parseInt(filteredResponse);
                  readResponse = filteredResponse;
               }
               catch (PatternSyntaxException e) {
                  logger.info("Can't convert filteredResponse to type Integer: " + e);
               }
               break;
            default:
               readResponse = filteredResponse;  
         }
      }

      return readResponse;
   }
}
