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

import java.util.ArrayList;
import java.util.List;

import org.openremote.controller.command.ExecutableCommand;

/**
 * The Class State.
 * 
 * @author Handy.Wang 2009-10-15
 */
public class State {

    /** The executable commands. */
    private List<ExecutableCommand> executableCommands;
    
    /**
     * Instantiates a new state.
     */
    public State() {
        super();
        executableCommands = new ArrayList<ExecutableCommand>();
    }

    /**
     * Adds the command.
     * 
     * @param executableCommand the executable command
     */
    public void addExecutableCommand(ExecutableCommand executableCommand) {
        executableCommands.add(executableCommand);
    }

    /**
     * Gets the executable commands.
     * 
     * @return the executable commands
     */
    public List<ExecutableCommand> getExecutableCommands() {
        return executableCommands;
    }

    /**
     * Sets the executable commands.
     * 
     * @param executableCommands the new executable commands
     */
    public void setExecutableCommands(List<ExecutableCommand> executableCommands) {
        this.executableCommands = executableCommands;
    }

}
