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
package org.openremote.controller.command;

import java.util.Properties;

import org.jdom.Element;
import org.openremote.controller.exception.CommandBuildException;
import org.openremote.controller.exception.NoSuchCommandBuilderException;
import org.springframework.context.support.ApplicationObjectSupport;


/**
 * A factory for creating Command objects.
 * 
 * @author Handy.Wang 2009-10-13
 */
public class CommandFactory extends ApplicationObjectSupport
{
   
   /** The command builders. */
   private Properties commandBuilders;
   
   /**
    * Gets the command.
    * 
    * @param element the element
    * 
    * @return the command
    */
   public Command getCommand(Element element)
   {
      if (element == null)
      {
         throw new CommandBuildException("Command DOM element is null.");
      }

      String protocolType = element.getAttributeValue(CommandBuilder.PROTOCOL_ATTRIBUTE_NAME);

      if (protocolType == null || "".equals(protocolType))
      {
         throw new CommandBuildException("Protocol type is null.");
      }

      String builder = commandBuilders.getProperty(protocolType);

      if (builder == null)
      {
         throw new NoSuchCommandBuilderException("Cannot find '" + protocolType + "Builder' by '" + protocolType + "' protocol.");
      }

      CommandBuilder commandBuilder = (CommandBuilder) getApplicationContext().getBean(builder);

      return commandBuilder.build(element);
   }

   /**
    * Sets the command builders.
    * 
    * @param commandBuilders the new command builders
    */
   public void setCommandBuilders(Properties commandBuilders)
   {
      this.commandBuilders = commandBuilders;
   }
}
