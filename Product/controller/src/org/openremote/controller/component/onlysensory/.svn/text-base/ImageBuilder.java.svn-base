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
package org.openremote.controller.component.onlysensory;

import java.util.List;

import org.jdom.Element;
import org.openremote.controller.component.Component;
import org.openremote.controller.component.ComponentBuilder;
import org.openremote.controller.component.Sensor;

/**
 * This class is used to build a Image by parse controll.xml
 * 
 * @author Handy
 *
 */
public class ImageBuilder extends ComponentBuilder {

   @SuppressWarnings("unchecked")
   @Override
   public Component build(Element componentElement, String commandParam) {
      Image image = new Image();
      if (!image.isValidActionWith(commandParam)) {
         return image;
      }
      List<Element> operationElements = componentElement.getChildren(); 
      for (Element operationElement : operationElements) {
         if (isIncludedSensorElement(operationElement)) {
            Sensor sensor = parseSensor(componentElement, operationElement);
            image.setSensor(sensor);
         }
      }
      return image;
   }

}
