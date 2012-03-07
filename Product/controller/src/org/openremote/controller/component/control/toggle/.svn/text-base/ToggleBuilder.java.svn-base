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
package org.openremote.controller.component.control.toggle;

import org.jdom.Element;
import org.openremote.controller.component.ComponentBuilder;
import org.openremote.controller.component.control.Control;

/**
 * The Class ToggleBuilder.
 * 
 * @author Handy.Wang 2009-10-15
 */
public class ToggleBuilder extends ComponentBuilder {
   
    /* (non-Javadoc)
     * @see org.openremote.controller.control.ControlBuilder#build(org.jdom.Element, java.lang.String)
     */
   @Override
    public Control build(Element toggleElement, String commandParam) {
      Toggle toggle = new Toggle();
      if (!toggle.isValidActionWith(commandParam)) {
         return toggle;
      }
//      
//      int operation = -1;
//      if (!Control.STATUS_ELEMENT_NAME.equals(commandParam)) {
//         try {
//            operation = Integer.parseInt(commandParam);
//         } catch (NumberFormatException e) {
//            e.printStackTrace();
//            return new Toggle();
//         }
//      }
//      
//      List<Element> subElements = toggleElement.getChildren();
//      for (int i = 0; i < subElements.size(); i++) {
//         Element element = subElements.get(i);         
//         if (commandParam.equalsIgnoreCase(element.getName()) && Control.STATUS_ELEMENT_NAME.equalsIgnoreCase(element.getName())) {
//            // status element
//            Element commandElementRef = (Element) element.getChildren().get(0);
//            String statusCommandID = commandElementRef.getAttributeValue(Control.REF_ATTRIBUTE_NAME);
//            Element statusCommandElement = remoteActionXMLParser.queryElementFromXMLById(toggleElement.getDocument(),statusCommandID);
//            if (statusCommandElement != null) {
//               StatusCommand statusCommand = (StatusCommand) commandFactory.getCommand(statusCommandElement);
//               toggle.setSensor(new Sensor(statusCommand));
//               break;
//            } else {
//               throw new NoSuchCommandException("Cannot find that command with id = " + statusCommandID);
//            }
//         } else {
//            // non-status elements
//            if (operation == i && !Control.STATUS_ELEMENT_NAME.equalsIgnoreCase(element.getName())) {
//               List<Element> commandRefElements = element.getChildren();
//               for (Element commandRefElement : commandRefElements) {
//                  String commandID = commandRefElement.getAttributeValue(Control.REF_ATTRIBUTE_NAME);
//                  Element commandElement = remoteActionXMLParser.queryElementFromXMLById(toggleElement.getDocument(),commandID);
//                  Command command = commandFactory.getCommand(commandElement);
//                  toggle.addExecutableCommand((ExecutableCommand) command);
//               }
//               break;
//            }
//            continue;
//         }
//      }
      return toggle;
    }
}
