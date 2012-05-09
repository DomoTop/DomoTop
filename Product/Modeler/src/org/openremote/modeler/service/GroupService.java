/* OpenRemote, the Home of the Digital Home.
* Copyright 2008-2009, OpenRemote Inc.
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
package org.openremote.modeler.service;

import java.util.List;

import org.openremote.modeler.domain.ClientGroup;
/**
 * Class to handle all the groups to this account
 * @author <a href="mailto:vincent.kriek@tass.nl">Vincent Kriek</a>
 */
public interface GroupService {
	/**
	 * Add a group to the list of groups used in this session
	 * @param group The group you want to add
	 * @return The group that was added to the list
	 */
   public ClientGroup add(ClientGroup group);
   /**
    * Retrieve a list with all the groups currently in use.
    * @return All the groups
    */
   public List<ClientGroup> getAll();
}