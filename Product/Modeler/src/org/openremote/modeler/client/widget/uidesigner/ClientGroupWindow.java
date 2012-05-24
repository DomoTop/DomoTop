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

import java.util.Collection;
import java.util.List;

import org.openremote.modeler.client.icon.Icons;
import org.openremote.modeler.client.proxy.GroupBeanModelProxy;
import org.openremote.modeler.client.rpc.AsyncSuccessCallback;
import org.openremote.modeler.domain.ClientGroup;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.WindowEvent;
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
   private final Collection<ClientGroup> selectedGroups;
   
   private ListBox allGroupsBox;
   private ListBox selectedGroupsBox;
	
   /**
    * Instantiates a new clientgroup window.
 * @param collection 
    */
   public ClientGroupWindow(List<ClientGroup> groups, Collection<ClientGroup> selected) {
	  this.allGroups = groups;
	  if(selected != null) {
		  this.selectedGroups = selected;
	  } else {
		  this.selectedGroups = null;
	  }
	  
      initial();
   
      addButtonListener();
      
      show();
   }
   
   private void initButtons(LayoutContainer container) {
	  LayoutContainer buttonContainer = new LayoutContainer();    
	  buttonContainer.setStyleAttribute("padding-bottom", "10px");
	      
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
    			final int index = allGroupsBox.getSelectedIndex();
    			String name = allGroupsBox.getItemText(index);
    			ClientGroup group = null;
    			for(int i = 0; i < allGroups.size(); i++) {
    				if(name.equals(allGroups.get(i).getName())) {
    					group = allGroups.get(i);
    					break;
    				}
    			}
    			
    			GroupBeanModelProxy.delete(group, new AsyncSuccessCallback<ClientGroup>() {
    				public void onSuccess(ClientGroup result) {
    	    			allGroupsBox.removeItem(index);
    				}
				});
    		}
    	}
      });


      buttonContainer.add(delete);
      
      container.add(buttonContainer);  
   }
   
   private void initLists(LayoutContainer container) {
	  LayoutContainer groupContainer1 = new LayoutContainer();    
      groupContainer1.setStyleAttribute("float", "left");
      groupContainer1.setStyleAttribute("padding", "10px");
      groupContainer1.setTitle("Group List");
      groupContainer1.setBorders(true);
      
      allGroupsBox = new ListBox();
      allGroupsBox.setWidth("130px");
      allGroupsBox.setVisibleItemCount(10);      
      for(ClientGroup group:allGroups) { 
    	  if(!contains(selectedGroups, group.getName())) {
    		  allGroupsBox.addItem(group.getName());
    	  }
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
      
      for(ClientGroup group:selectedGroups) {
    	  selectedGroupsBox.addItem(group.getName());
      }
      
      groupContainer2.add(selectedGroupsBox); 
      
      LayoutContainer groupContainer3 = new LayoutContainer();
      groupContainer3.setStyleAttribute("float", "left");
      groupContainer3.setStyleAttribute("padding", "10px");
      groupContainer3.setStyleAttribute("margin", "20px");
      
      Button addAll = new Button("Add all >>");
      addAll.setStyleAttribute("padding-top", "10px");
      addAll.setWidth(80);
      
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
      addOne.setStyleAttribute("padding-top", "10px");
      addOne.setWidth(80);
      
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
      remove.setStyleAttribute("padding-top", "10px");
      remove.setWidth(80);
      
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
      removeAll.setStyleAttribute("padding-top", "10px");
      removeAll.setWidth(80);
      
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
      
      container.add(groupContainer1);
      container.add(groupContainer3);      
      container.add(groupContainer2);
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

      
      LayoutContainer container = new LayoutContainer();
      container.setStyleAttribute("padding", "10px");   
      
      initButtons(container);
      initLists(container);
      
      add(container);
   }
   
   private boolean contains(Collection<ClientGroup> groups, String group) {
	   if(groups != null) {
		   for(ClientGroup element: groups) {
			   if(element.getName().equals(group)) {
				   return true;
			   }
		   }
	   }
	   return false;
   }
   
   private void addButtonListener() {
	      addListener(Events.BeforeHide, new Listener<WindowEvent>() {
	         public void handleEvent(WindowEvent be) {
	            if (be.getButtonClicked() == getButtonById("ok")) {	            	
	            	for(int i = 0; i < selectedGroupsBox.getItemCount(); i++) {
	            		if(!contains(selectedGroups, selectedGroupsBox.getItemText(i))) {
	            			for(ClientGroup group: allGroups) {
	            				if(group.getName().equals(selectedGroupsBox.getItemText(i))) {
	            					if(selectedGroups != null) {
	            						selectedGroups.add(group);
	            					}
	            				}
	            			}
	            		} 
	            	}
	            	
	            	for(ClientGroup group: selectedGroups) {
	            		boolean add = false;
	            		for(int j = 0; j < selectedGroupsBox.getItemCount(); j++) {
	            			if(group.getName().equals(selectedGroupsBox.getItemText(j))) {
	            				add = true;
	            			}
	            		}
	            		
	            		if(!add) {
        					if(selectedGroups != null) {
        						selectedGroups.remove(group);
        					}	            		
        				}
	            	}
	            }
	         }
	      }); 
	   }
   
}
