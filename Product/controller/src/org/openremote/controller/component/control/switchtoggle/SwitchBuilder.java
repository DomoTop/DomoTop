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
package org.openremote.controller.component.control.switchtoggle;

import java.util.List;

import org.jdom.Element;
import org.openremote.controller.command.Command;
import org.openremote.controller.command.ExecutableCommand;
import org.openremote.controller.component.ComponentBuilder;
import org.openremote.controller.component.Sensor;
import org.openremote.controller.component.control.Control;

/**
 * It is mainly responsible for build Switch control with control element and commandParam.
 * 
 * @author Handy.Wang 2009-10-23
 */
public class SwitchBuilder extends ComponentBuilder {

   /* (non-Javadoc)
    * @see org.openremote.controller.control.ControlBuilder#build(org.jdom.Element, java.lang.String)
    */
   @SuppressWarnings("unchecked")
   @Override
   public Control build(Element componentElement, String commandParam) {
      Switch switchToggle = new Switch();
      if (!switchToggle.isValidActionWith(commandParam)) {
         return switchToggle;
      }
      List<Element> operationElements = componentElement.getChildren();
      for (Element operationElement : operationElements) {
         if (isIncludedSensorElement(operationElement)) {
            Sensor sensor = parseSensor(componentElement, operationElement);
            switchToggle.setSensor(sensor);
         }
         if (commandParam.equalsIgnoreCase(operationElement.getName())) {
            List<Element> commandRefElements = operationElement.getChildren();
            for (Element commandRefElement : commandRefElements) {
               String commandID = commandRefElement.getAttributeValue(Control.REF_ATTRIBUTE_NAME);
               Element commandElement = remoteActionXMLParser.queryElementFromXMLById(componentElement.getDocument(),commandID);
               Command command = commandFactory.getCommand(commandElement);
               switchToggle.addExecutableCommand((ExecutableCommand) command);
            }      
            continue;
         }
      }
      return switchToggle;
   }

}
