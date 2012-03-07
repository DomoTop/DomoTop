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
package org.openremote.controller.component;
import java.util.List;
import java.util.NoSuchElementException;

import org.jdom.Element;
import org.openremote.controller.command.CommandFactory;
import org.openremote.controller.command.RemoteActionXMLParser;
import org.openremote.controller.command.StatusCommand;
import org.openremote.controller.component.control.Control;
import org.openremote.controller.exception.NoSuchCommandException;

/**
 * The Class ComponentBuilder.
 * 
 * @author Handy.Wang 2009-10-15
 */
public abstract class ComponentBuilder {
    
    /** The remote action xml parser. */
    protected RemoteActionXMLParser remoteActionXMLParser;
    
    /** The command factory. */
    protected CommandFactory commandFactory;
    
    @SuppressWarnings("unchecked")
    protected Sensor parseSensor(Element componentElement, Element sensorIncludeElement) {
       Element sensorElementRef = sensorIncludeElement;//(Element) sensorIncludeElement.getChildren().get(0);
       String sensorID = sensorElementRef.getAttributeValue(Component.REF_ATTRIBUTE_NAME);
       Sensor sensor = new Sensor();
       Element sensorElement = remoteActionXMLParser.queryElementFromXMLById(componentElement.getDocument(), sensorID);
       if (sensorElement != null) {
          List<Element> sensorSubElements = sensorElement.getChildren();
          for (Element sensorSubElement : sensorSubElements) {
             if (isIncludingStatusCommand(sensorSubElement)) {
                String statusCommandID = sensorSubElement.getAttributeValue(Component.REF_ATTRIBUTE_NAME);
                Element statusCommandElement = remoteActionXMLParser.queryElementFromXMLById(componentElement.getDocument(),statusCommandID);
                if (statusCommandElement != null) {
                   StatusCommand statusCommand = (StatusCommand) commandFactory.getCommand(statusCommandElement);
                   sensor = new Sensor(statusCommand);
                   sensor.setSensorID(Integer.parseInt(sensorID));
                } else {
                   throw new NoSuchCommandException("Cannot find that command with id = " + statusCommandID);
                }
                break;
             }
          }
       } else {
          throw new NoSuchElementException("No such sensor with id " + sensorID);
       }
       return sensor;
    }
    
    private boolean isIncludingStatusCommand(Element sensorSubElement) {
       return Component.INCLUDE_ELEMENT_NAME.equalsIgnoreCase(sensorSubElement.getName()) && Component.COMMAND_ELEMENT_NAME.equalsIgnoreCase(sensorSubElement.getAttributeValue(Component.INCLUDE_TYPE_ATTRIBUTE_NAME));
    }
    
    protected boolean isIncludedSensorElement(Element childElementOfControl) {
       boolean isIncludeChildElememntOfControl = Control.INCLUDE_ELEMENT_NAME.equalsIgnoreCase(childElementOfControl.getName());
       boolean isIncludedSensor = Control.INCLUDE_TYPE_SENSOR.equals(childElementOfControl.getAttributeValue(Control.INCLUDE_TYPE_ATTRIBUTE_NAME));
       return isIncludeChildElememntOfControl && isIncludedSensor;
    }
    
    /**
     * Builds the component.
     * 
     * @param componentElement the component element
     * @param commandParam the command param
     * 
     * @return the component
     */
    public abstract Component build(Element componentElement, String commandParam);

    /**
     * Sets the remote action xml parser.
     * 
     * @param remoteActionXMLParser the new remote action xml parser
     */
    public void setRemoteActionXMLParser(RemoteActionXMLParser remoteActionXMLParser) {
        this.remoteActionXMLParser = remoteActionXMLParser;
    }

    /**
     * Sets the command factory.
     * 
     * @param commandFactory the new command factory
     */
    public void setCommandFactory(CommandFactory commandFactory) {
        this.commandFactory = commandFactory;
    }

}
