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

import java.util.ArrayList;
import java.util.List;

import org.openremote.modeler.client.icon.Icons;
import org.openremote.modeler.client.proxy.GroupBeanModelProxy;
import org.openremote.modeler.client.rpc.AsyncSuccessCallback;
import org.openremote.modeler.domain.ClientGroup;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.ListBox;

import com.extjs.gxt.ui.client.event.Listener;

/**
 * The Class ClientGroupWindow, used to select groups.
 */
public class ClientGroupWindow extends Dialog {
   private List<ClientGroup> allGroups;
   private List<ClientGroup> selectedGroups = new ArrayList<ClientGroup>();
   
   private ListBox allGroupsBox;
   private ListBox selectedGroupsBox;
	
   /**
    * Instantiates a new clientgroup window.
    */
   public ClientGroupWindow(List<ClientGroup> groups) {
	  this.allGroups = groups;
	   
      initial();
      initButtons();
      initLists();
      show();

   }
   
   private void initButtons() {
	  LayoutContainer buttonContainer = new LayoutContainer();     
	      
      Button add = new Button("Add");
      add.setIcon(((Icons) GWT.create(Icons.class)).add());
      add.setStyleAttribute("display", "inline");
      add.setStyleAttribute("padding-right", "10px");
      
      add.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce) {

				MessageBox.prompt("Add a new group", "Enter the name of your new group. This name has to be unique", 
						          new Listener<MessageBoxEvent>() {
									@Override
									public void handleEvent(MessageBoxEvent be) {
										GroupBeanModelProxy.add(new ClientGroup(be.getValue()), new AsyncSuccessCallback<ClientGroup>() {
											@Override
											public void onSuccess(ClientGroup result) {
												allGroups.add(result);
												if(allGroupsBox != null) {
													allGroupsBox.addItem(result.getName());
												}
											}
										});
									}
								});
			}
		});
      
      buttonContainer.add(add);
      
//WHAT DOES THIS DO?  
//	      Button edit = new Button("Edit");
//	      edit.setIcon(((Icons) GWT.create(Icons.class)).edit());
//	      edit.setStyleAttribute("display", "inline");
//	      edit.setStyleAttribute("padding-right", "10px");
//	      buttonContainer.add(edit);
      
      Button delete = new Button("Delete");
      delete.setIcon(((Icons) GWT.create(Icons.class)).delete());
      delete.setStyleAttribute("display", "inline");
      delete.setStyleAttribute("padding-right", "10px");
      
      delete.addSelectionListener(new SelectionListener<ButtonEvent>() {
    	@Override
    	public void componentSelected(ButtonEvent ce) {
    		if(allGroupsBox != null) {
    			int index = allGroupsBox.getSelectedIndex();
    			String name = allGroupsBox.getItemText(index);
    			allGroupsBox.removeItem(index);
    			
    			//GroupBeanModelProxy.delete();
    		}
    		//TODO: REFRESH LISTBOX
    	}
      });


      buttonContainer.add(delete);
      
      add(buttonContainer);
      
   }
   
   private void initLists() {
	   LayoutContainer groupContainer1 = new LayoutContainer();    
      groupContainer1.setStyleAttribute("float", "left");
      groupContainer1.setStyleAttribute("padding", "10px");
      groupContainer1.setTitle("Group List");
      groupContainer1.setBorders(true);
      
      allGroupsBox = new ListBox();
      allGroupsBox.setWidth("130px");
      allGroupsBox.setVisibleItemCount(10);      
      for(int i = 0; i < allGroups.size(); i++) {
    	  allGroupsBox.addItem(allGroups.get(i).getName());
      }  
      groupContainer1.add(allGroupsBox);
      
      
      
      LayoutContainer groupContainer2 = new LayoutContainer();     
      groupContainer2.setStyleAttribute("padding", "10px");
      groupContainer2.setStyleAttribute("float", "left");
      groupContainer2.setTitle("Selected Groups");
      groupContainer2.setBorders(true);
      
      selectedGroupsBox = new ListBox();
      selectedGroupsBox.setWidth("130px");
      selectedGroupsBox.setVisibleItemCount(10); 
      groupContainer2.add(selectedGroupsBox); 
      
      LayoutContainer groupContainer3 = new LayoutContainer();
      groupContainer3.setStyleAttribute("float", "left");
      groupContainer3.setStyleAttribute("padding", "10px");
      groupContainer3.setStyleAttribute("margin", "20px");
      
      Button addAll = new Button("Add all >>");
      addAll.setWidth(50);
      
      addAll.addSelectionListener(new SelectionListener<ButtonEvent>() {
		@Override
		public void componentSelected(ButtonEvent ce) {
			for(int i = allGroupsBox.getItemCount() - 1; i >= 0; i--) {
				selectedGroupsBox.addItem(allGroupsBox.getItemText(i));
				allGroupsBox.removeItem(i);
			}
		}
      });
      
      groupContainer3.add(addAll);
      
      Button addOne = new Button("Add >");
      addOne.setWidth(50);
      
      addOne.addSelectionListener(new SelectionListener<ButtonEvent>() {
      	@Override
      	public void componentSelected(ButtonEvent ce) {
      		int index = allGroupsBox.getSelectedIndex();
      		selectedGroupsBox.addItem(allGroupsBox.getItemText(index));
      		allGroupsBox.removeItem(index);
      	}
      });
         
      
      groupContainer3.add(addOne);
      
      Button remove = new Button("Remove <");
      remove.setWidth(50);
      
      remove.addSelectionListener(new SelectionListener<ButtonEvent>() {
    	@Override
    	public void componentSelected(ButtonEvent ce) {
    		int index = selectedGroupsBox.getSelectedIndex();
    		allGroupsBox.addItem(selectedGroupsBox.getItemText(index));
    		selectedGroupsBox.removeItem(index);
    	}
      });
      
      groupContainer3.add(remove);
      
      Button removeAll = new Button("Remove all <<");
      removeAll.setWidth(50);
      
      removeAll.addSelectionListener(new SelectionListener<ButtonEvent>() {
		@Override
		public void componentSelected(ButtonEvent ce) {
			for(int i = selectedGroupsBox.getItemCount() - 1; i >= 0; i--) {
				allGroupsBox.addItem(selectedGroupsBox.getItemText(i));
				selectedGroupsBox.removeItem(i);
			}
		}
      });
      
      groupContainer3.add(removeAll);
      
      add(groupContainer1);
      add(groupContainer3);      
      add(groupContainer2);
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
      
      
     
   }
   
}
