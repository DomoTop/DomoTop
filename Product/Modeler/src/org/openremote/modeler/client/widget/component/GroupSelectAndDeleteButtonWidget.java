/* OpenRemote, the Home of the Digital Home.
* Copyright 2008-2010, OpenRemote Inc.
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
package org.openremote.modeler.client.widget.component;

import java.util.List;

import org.openremote.modeler.client.icon.Icons;
import org.openremote.modeler.domain.ClientGroup;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.ListBox;

/**
 * The Class SelectAndDeleteButtonWidget is store a select button and a delete button in a layout container.
 */
public class GroupSelectAndDeleteButtonWidget extends LayoutContainer {
   public static String NO_GROUP_ITEM = "---";

   private ListBox select = new ListBox();
   private Button deleteButton = new Button();
   
   public GroupSelectAndDeleteButtonWidget() {
      select.setWidth("125");
      select.setVisibleItemCount(1);
      deleteButton.setIcon(((Icons) GWT.create(Icons.class)).add());
      deleteButton.setStyleAttribute("float", "left");
      deleteButton.setStyleAttribute("paddingLeft", "3px");
      add(select);
      add(deleteButton);
   }
   
   public void addChangeHandler( ChangeHandler handler) {
	this.select.addChangeHandler(handler);
   }
   
   public void setGroups(List<ClientGroup> groups, String currentGroup) {
	   select.clear();
	   select.addItem(NO_GROUP_ITEM);
	   
	   for(int i = 0; i < groups.size(); i++) {
		   String group = groups.get(i).getName();
		   select.addItem(group);
		   if(group.equals(currentGroup)) {
			   select.setSelectedIndex(i + 1);
		   }
	   }
   }
   
   public void addAddListener(SelectionListener<ButtonEvent> selectionListener) {
      this.deleteButton.addSelectionListener(selectionListener);
   }
	
	public void addItem(String result) {
		select.addItem(result);
	}
}
