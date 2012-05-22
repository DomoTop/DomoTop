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
package org.openremote.modeler.client.widget.uidesigner;

import org.openremote.modeler.client.icon.Icons;

import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.ListBox;

/**
 * The Class ClientGroupWindow, used to select groups.
 */
public class ClientGroupWindow extends Dialog {

   /**
    * Instantiates a new clientgroup window.
    */
   public ClientGroupWindow() {
      initial();
      show();
   }
   
   private void initial() {
      setMinHeight(350);
      setWidth(500);
      setHeading("Select group(s)");
      setModal(true);
      setLayout(new BorderLayout());
      setButtons(Dialog.OKCANCEL);  
      setHideOnButtonClick(true);
      setBodyBorder(false);
      setStyleAttribute("padding", "10px");
      
      LayoutContainer buttonContainer = new LayoutContainer();     
      
      Button add = new Button("Add");
      add.setIcon(((Icons) GWT.create(Icons.class)).add());
      add.setStyleAttribute("display", "inline");
      add.setStyleAttribute("padding-right", "10px");
      buttonContainer.add(add);
      
      Button edit = new Button("Edit");
      edit.setIcon(((Icons) GWT.create(Icons.class)).edit());
      edit.setStyleAttribute("display", "inline");
      edit.setStyleAttribute("padding-right", "10px");
      buttonContainer.add(edit);
      
      Button delete = new Button("Delete");
      delete.setIcon(((Icons) GWT.create(Icons.class)).delete());
      delete.setStyleAttribute("display", "inline");
      delete.setStyleAttribute("padding-right", "10px");
      buttonContainer.add(delete);
      
      add(buttonContainer);
      
      LayoutContainer groupContainer1 = new LayoutContainer();    
      groupContainer1.setStyleAttribute("float", "left");
      groupContainer1.setStyleAttribute("padding", "10px");
      groupContainer1.setTitle("Group List");
      groupContainer1.setBorders(true);
      
      ListBox list = new ListBox();
      list.setWidth("130px");
      list.setVisibleItemCount(10);      
      for(int i = 0; i < 10; i++) {
    	  list.addItem("test");
      }  
      groupContainer1.add(list);
      
      add(groupContainer1);
      
      LayoutContainer groupContainer3 = new LayoutContainer();
      groupContainer3.setStyleAttribute("float", "left");
      groupContainer3.setStyleAttribute("padding", "10px");
      groupContainer3.setStyleAttribute("margin", "20px");
      groupContainer3.setBorders(true);
      
      Button addAll = new Button("Add all >>");
      addAll.setWidth(50);
      groupContainer3.add(addAll);
      
      Button addOne = new Button("Add >");
      addOne.setWidth(50);
      groupContainer3.add(addOne);
      
      Button remove = new Button("Remove <");
      remove.setWidth(50);
      groupContainer3.add(remove);
      
      Button removeAll = new Button("Remove all <<");
      removeAll.setWidth(50);
      groupContainer3.add(removeAll);
      
      add(groupContainer3);      
     
      LayoutContainer groupContainer2 = new LayoutContainer();     
      groupContainer2.setStyleAttribute("padding", "10px");
      groupContainer2.setStyleAttribute("float", "left");
      groupContainer2.setTitle("Selected Groups");
      groupContainer2.setBorders(true);
      
      ListBox list2 = new ListBox();
      list2.setWidth("130px");
      list2.setVisibleItemCount(10); 
      for(int i = 0; i < 10; i++) {
    	  list2.addItem("test");
      }
      groupContainer2.add(list2); 
      
      add(groupContainer2);
   }
   
}
