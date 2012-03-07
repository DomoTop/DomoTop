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

import java.util.List;
import java.util.logging.Logger;
import java.net.URL;
import java.net.MalformedURLException;

import org.jdom.Element;
import org.openremote.controller.command.Command;
import org.openremote.controller.command.CommandBuilder;
import org.openremote.controller.utils.CommandUtil;
import org.openremote.controller.Constants;
import org.openremote.controller.exception.NoSuchCommandException;


/**
 * Builds HTTP GET command from XML element.
 * example:
 * <pre>
 * {@code 
 * <command id="xxx" protocol="httpGet">
 *      <property name="url" value="http://127.0.0.1:8080/xxx/light1_on" />
 * </command>
 * }
 * </pre>
 *
 * @author Marcus 2009-4-26
 * @author <a href="mailto:juha@openremote.org">Juha Lindfors</a>
 */
public class HttpGetCommandBuilder implements CommandBuilder
{


  // Constants ------------------------------------------------------------------------------------

  /**
   * Common log category name for all HTTP protocol related logging.
   */
  public final static String HTTP_PROTOCOL_LOG_CATEGORY =
      Constants.CONTROLLER_PROTOCOL_LOG_CATEGORY + "http";


  // Class Members --------------------------------------------------------------------------------

  /**
   * Logger for this HTTP protocol implementation.
   */
  private final static Logger log = Logger.getLogger(HTTP_PROTOCOL_LOG_CATEGORY);


  // Implements CommandBuilder --------------------------------------------------------------------
  
   @SuppressWarnings("unchecked")
   public Command build(Element element)
   {
      List<Element> propertyEles = element.getChildren("property", element.getNamespace());

     String urlAsString = null;
     String username = null;
     String password = null;

      for(Element ele : propertyEles)
      {
         if("url".equals(ele.getAttributeValue("name")))
         {
           urlAsString = CommandUtil.parseStringWithParam(element, ele.getAttributeValue("value"));
         }

         else if("name".equals(ele.getAttributeValue("name")))
         {
           // getCmd.setName(ele.getAttributeValue("value"));
         }

         else if ("username".equals(ele.getAttributeValue("name")))
         {
           username = ele.getAttributeValue("value");
         }

         /**************************************************************************************
          *
          * TODO:
          *
          * THE USE OF PASSWORDS IN CONFIGURATION IS INHERENTLY UNSAFE AND SHOULD BE AVOIDED
          *
          * We need a better mechanism to handle sensitive configuration data.
          *
          **************************************************************************************/
         else if ("password".equals(ele.getAttributeValue("name")))
         {
           password = ele.getAttributeValue("value");
         }
      }

     URL url;

     try
     {
       url = new URL(urlAsString);

       if (username == null || password == null)
       {
         return new HttpGetCommand(url);
       }

       else
       {
         return new HttpGetCommand(url, username, password.getBytes());
       }
     }
     catch (MalformedURLException e)
     {
       // TODO:
       //
       //  bind the JUL logging to runtime log implementation...

       //log.warn("Configuration error in HTTP protocol URL: " + e.getMessage(), e);

       throw new NoSuchCommandException("Invalid URL: " + e.getMessage(), e);
     }
   }

}
