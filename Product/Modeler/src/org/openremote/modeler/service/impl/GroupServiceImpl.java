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
package org.openremote.modeler.service.impl;

import java.util.ArrayList;

import org.openremote.modeler.domain.MyGroup;
import org.openremote.modeler.service.BaseAbstractService;
import org.openremote.modeler.service.GroupService;

public class GroupServiceImpl extends BaseAbstractService<MyGroup> implements GroupService {
	private ArrayList<MyGroup> groups;
	
	public GroupServiceImpl() {
		groups = new ArrayList<MyGroup>();
		for(int i = 0; i < 20; i++) {
			add(new MyGroup("test"+i));
		}
	}
	
	public void add(MyGroup group) {
		groups.add(group);
   	}
	
	public ArrayList<MyGroup> get() {
		return groups;
	}   
}