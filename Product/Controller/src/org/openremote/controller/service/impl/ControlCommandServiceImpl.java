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
package org.openremote.controller.service.impl;

import java.security.Principal;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.openremote.controller.Constants;
import org.openremote.controller.command.ExecutableCommand;
import org.openremote.controller.command.RemoteActionXMLParser;
import org.openremote.controller.component.ComponentFactory;
import org.openremote.controller.component.control.Control;
import org.openremote.controller.exception.InvalidGroupException;
import org.openremote.controller.exception.NoSuchCommandException;
import org.openremote.controller.exception.NoSuchComponentException;
import org.openremote.controller.service.ClientService;
import org.openremote.controller.service.ControlCommandService;
import org.openremote.controller.service.GroupService;
import org.openremote.controller.utils.MacrosIrDelayUtil;


/**
 * The implementation for ControlCommandService class.
 * 
 * @author Handy.Wang
 * @author Melroy.van.den.Berg 2012
 */
public class ControlCommandServiceImpl implements ControlCommandService {

   private final static Logger logger = Logger.getLogger(Constants.SERVICE_LOG_CATEGORY);
   
   private RemoteActionXMLParser remoteActionXMLParser;
   
   private ComponentFactory componentFactory;
   
   private ClientService clientService;
   
   /**
    * {@inheritDoc}
    */
   public void trigger(String controlID, String commandParam) {
      
      Control control = getControl(controlID, commandParam);
      List<ExecutableCommand> executableCommands = control.getExecutableCommands();
      MacrosIrDelayUtil.ensureDelayForIrCommand(executableCommands);
      for (ExecutableCommand executableCommand : executableCommands) {
         if (executableCommand != null) {
            executableCommand.send();
         } else {
            throw new NoSuchCommandException("ExecutableCommand is null");
         }
      }
      
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public void trigger(String controlID, String commandParam, Principal DN) 
   {      
      // Get the group name from the control component
      List<String>groupElementIDs = getGroupsFromComponent(controlID);
      List<String>groupNames = new ArrayList<String>();
      Element groupElement = null;
      String clientGroupName = "";
      boolean allowed = false;
      
      for (String groupElementID:groupElementIDs)
      {
         groupElement = remoteActionXMLParser.queryElementFromXMLById(groupElementID);
         String groupName = groupElement.getAttributeValue("name");
         groupNames.add(groupName);
      }
      
      clientGroupName = clientService.getGroupName(DN.toString());
      if(!clientGroupName.isEmpty())
      {
         for (String groupName:groupNames)
         {
            if(clientGroupName.equals(groupName))
            {
               allowed = true;
               break;
            }
         }
      }
      
      if(!allowed)
      {
         throw new InvalidGroupException("The command you try to execute is not allowed (you are in the wrong group)");
      }
      
      this.trigger(controlID, commandParam);
   }
   
   
   private Control getControl(String controlID, String commandParam) {
      Element controlElement = remoteActionXMLParser.queryElementFromXMLById(controlID);
      if (controlElement == null) {
         throw new NoSuchComponentException("No such component id :" + controlID);
      }
      return (Control) componentFactory.getComponent(controlElement, commandParam);
   }
   
   @SuppressWarnings("unchecked")
   private List<String> getGroupsFromComponent(String componentID) 
   {
      Element componentElement = remoteActionXMLParser.queryElementFromXMLById(componentID);

      List<Element>childerenOfComponent = componentElement.getChildren();
      List<String> groupElementIDs = new ArrayList<String>();
      String groupElementId = "";
      for (Element childOfComponent:childerenOfComponent)
      {
         if (Control.INCLUDE_ELEMENT_NAME.equalsIgnoreCase(childOfComponent.getName()) && Control.INCLUDE_TYPE_GROUP.equalsIgnoreCase(childOfComponent.getAttributeValue("type"))) 
         {
            groupElementId = childOfComponent.getAttributeValue("ref");
            groupElementIDs.add(groupElementId);
         }
      }
      
      if (groupElementIDs.size() <= 0) {
         logger.info("No groups for component ID: " + componentID);
      }
      return groupElementIDs;     
   }
   
   
   public void setRemoteActionXMLParser(RemoteActionXMLParser remoteActionXMLParser) {
      this.remoteActionXMLParser = remoteActionXMLParser;
   }

   public void setComponentFactory(ComponentFactory componentFactory) {
      this.componentFactory = componentFactory;
   }
   
   public void setClientService(ClientService clientService) {
      this.clientService = clientService;
   }
}
