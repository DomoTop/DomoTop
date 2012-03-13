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

import java.util.List;

import org.jdom.Element;
import org.openremote.controller.command.Command;
import org.openremote.controller.command.CommandBuilder;
import org.openremote.controller.utils.CommandUtil;


/**
 * The Class TelnetCommandBuilder.
 *
 * @author Marcus 2009-4-26
 */
public class TelnetCommandBuilder implements CommandBuilder {

   /**
    * {@inheritDoc}
    */
   @SuppressWarnings("unchecked")
   public Command build(Element element) {
      TelnetCommand telnetEvent = new TelnetCommand();
      List<Element> propertyEles = element.getChildren("property", element.getNamespace());
      for(Element ele : propertyEles){
         if("name".equals(ele.getAttributeValue("name"))){
            telnetEvent.setName(ele.getAttributeValue("value"));
         } else if("port".equals(ele.getAttributeValue("name"))){
            telnetEvent.setPort(ele.getAttributeValue("value"));
         } else if("ipAddress".equals(ele.getAttributeValue("name"))){
            telnetEvent.setIp(ele.getAttributeValue("value"));
         } else if("command".equals(ele.getAttributeValue("name"))){
            telnetEvent.setCommand(CommandUtil.parseStringWithParam(element, ele.getAttributeValue("value")));
         } else if("timeout".equals(ele.getAttributeValue("timeout"))){
            telnetEvent.setTimeOut(CommandUtil.parseStringWithParam(element, ele.getAttributeValue("value")));
         } else if("statusFilter".equals(ele.getAttributeValue("name"))){
            telnetEvent.setResponseFilter(CommandUtil.parseStringWithParam(element, ele.getAttributeValue("value")));
         } else if("statusFilterGroup".equals(ele.getAttributeValue("name"))){
            telnetEvent.setResponseFilterGroup(CommandUtil.parseStringWithParam(element, ele.getAttributeValue("value")));
         } else if("statusDefault".equals(ele.getAttributeValue("name"))){
            telnetEvent.setStatusDefault(CommandUtil.parseStringWithParam(element, ele.getAttributeValue("value")));
         }
      }
      return telnetEvent;
   }

}
