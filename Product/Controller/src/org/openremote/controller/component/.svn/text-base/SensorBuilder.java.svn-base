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
package org.openremote.controller.component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;
import org.openremote.controller.Constants;
import org.openremote.controller.command.CommandFactory;
import org.openremote.controller.command.NoStatusCommand;
import org.openremote.controller.command.RemoteActionXMLParser;
import org.openremote.controller.command.StatusCommand;

/**
 * Sensor Builder via XML element.
 * 
 * <ol>
 * <li> RANGE sensor
 * <pre>{@code
 *<sensor id="568" name="temperature_sensor" type="range">
 *  <include type="command" ref="631" />
 *  <min value="-50" />
 *  <max value="100" />
 *</sensor>
 * }
 * </pre>
 * </li>
 * <li> LEVEL sensor, returns percent value, ([0..100])
 * <pre>{@code
 *<sensor id="568" name="temperature_sensor" type="level">
 *  <include type="command" ref="631" />
 *  <min value="0" />
 *  <max value="100" />
 *</sensor>
 * }
 * </pre>
 * </li>
 * <li> SWITCH sensor
 * <pre>{@code
 *<sensor id="570" name="light_sensor" type="switch">
 *  <include type="command" ref="633" />
 *  <state name="on" />
 *  <state name="off" />
 *</sensor>
 * }
 * </pre>
 * </li>
 * <li> CUSTOM sensor
 * <pre>{@code
 *<sensor id="570" name="light_sensor" type="custom">
 *  <include type="command" ref="633" />
 *  <state name="on" />
 *  <state name="off" />
 *  <state name="dim30" />
 *  <state name="dim50" />
 *</sensor>
 * }
 * </pre>
 * </li>
 * 
 * @author Dan Cong
 * 
 */
public class SensorBuilder {
   
   private RemoteActionXMLParser remoteActionXMLParser;
   
   private CommandFactory commandFactory;

   /**
    * Builds sensor from XML element like this:
    * 
    * @param document specified JDOM document of controller.xml, use default controller.xml if it's null.
    * @param element JDOM element for sensor
    * @return sensor
    */
   public Sensor build(Document document, Element sensorElement) {
      String sensorID = sensorElement.getAttributeValue("id");
      return new Sensor(Integer.parseInt(sensorID), sensorElement.getAttributeValue(Constants.SENSOR_TYPE_ATTRIBUTE_NAME), 
            getStatusCommand(document, sensorElement), getStateMap(sensorElement));
   }
   
   /**
    * Builds sensor from XML element like this, using default controller.xml, 
    * 
    * @param element JDOM element for sensor
    * @return sensor
    */
   public Sensor build(Element sensorElement) {
      return build(null, sensorElement);
   }
   
   @SuppressWarnings("unchecked")
   private StatusCommand getStatusCommand(Document document, Element sensorElement) {
      
      List<Element>childrenOfSensor = sensorElement.getChildren();
      String commandElementId = "";
      for (Element childOfSensor:childrenOfSensor) {
        if ("include".equalsIgnoreCase(childOfSensor.getName()) && "command".equalsIgnoreCase(childOfSensor.getAttributeValue("type"))) {
           commandElementId = childOfSensor.getAttributeValue("ref");
           break;
        }
      }
      if ("".equals(commandElementId)) {
        return new NoStatusCommand();
      }
      Element commandElement = null;
      if (document == null) {
         commandElement = remoteActionXMLParser.queryElementFromXMLById(commandElementId);
      } else {
         commandElement = remoteActionXMLParser.queryElementFromXMLById(document, commandElementId);
      }
      StatusCommand statusCommand = (StatusCommand) commandFactory.getCommand(commandElement);
      return statusCommand;
   }
   
   @SuppressWarnings("unchecked")
   private static Map<String, String> getStateMap(Element sensorElement) {
      HashMap<String, String> stateMap = new HashMap<String, String>();
      List<Element>sensorChildren = sensorElement.getChildren();
      for (Element sensorChild : sensorChildren) {
        if ("state".equalsIgnoreCase(sensorChild.getName())) {
           stateMap.put(sensorChild.getAttributeValue("name"), sensorChild.getAttributeValue("value"));
        } else if ("min".equalsIgnoreCase(sensorChild.getName())) {
           stateMap.put(Sensor.RANGE_MIN_STATE, sensorChild.getAttributeValue("value"));
        } else if ("max".equalsIgnoreCase(sensorChild.getName())) {
           stateMap.put(Sensor.RANGE_MAX_STATE, sensorChild.getAttributeValue("value"));
        }
      }
      return stateMap;
   }

   public void setRemoteActionXMLParser(RemoteActionXMLParser remoteActionXMLParser) {
      this.remoteActionXMLParser = remoteActionXMLParser;
   }
   public void setCommandFactory(CommandFactory commandFactory) {
      this.commandFactory = commandFactory;
   }


}
