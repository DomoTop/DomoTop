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
package org.openremote.modeler.client.widget;

import java.util.ArrayList;
import java.util.List;

import org.openremote.modeler.client.Constants;
import org.openremote.modeler.client.event.SubmitEvent;
import org.openremote.modeler.client.icon.Icons;
import org.openremote.modeler.client.listener.FormSubmitListener;
import org.openremote.modeler.client.listener.SubmitListener;
import org.openremote.modeler.client.model.ComboBoxDataModel;
import org.openremote.modeler.client.rpc.AsyncServiceFactory;
import org.openremote.modeler.client.rpc.AsyncSuccessCallback;
import org.openremote.modeler.domain.User;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.IconAlign;
import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FormEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.SimpleComboValue;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.FormPanel.LabelAlign;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.EditorGrid;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.core.client.GWT;

/**
 * The Class AccountManageWindow.
 */
public class AccountManageWindow extends Dialog {
   private Icons icons = GWT.create(Icons.class);
   private EditorGrid<BeanModel> invitedUsersGrid = null;
   private long cureentUserId = 0;
   public AccountManageWindow(long cureentUserId) {
      this.cureentUserId = cureentUserId;
      setHeading("Account management");
      setHideOnButtonClick(true);
      setButtonAlign(HorizontalAlignment.CENTER);
      setAutoHeight(true);
      setButtons("");
      setWidth(452);
      setMinHeight(280);
      addInviteUserButton();
      addInvitedUsers();
      createAccessUserGrid();
      show();
   }
   
   private void addInviteUserButton() {
      Button inviteUserButton = new Button("Invite other users");
      inviteUserButton.setIcon(icons.add());
      inviteUserButton.setIconAlign(IconAlign.LEFT);
      inviteUserButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
         public void componentSelected(ButtonEvent ce) {
            final InviteUserWindow inviteUserWindow = new InviteUserWindow();
            inviteUserWindow.addListener(SubmitEvent.SUBMIT, new SubmitListener() {
               public void afterSubmit(SubmitEvent be) {
                  inviteUserWindow.hide();
                  User user = be.getData();
                  if (user != null) {
                     if (invitedUsersGrid == null) {
                        createInvitedUserGrid();
                     }
                     invitedUsersGrid.stopEditing();
                     invitedUsersGrid.getStore().insert(user.getBeanModel(), 0);
                     invitedUsersGrid.startEditing(0, 1);
                  }
               }
            });
         }
      });
      add(inviteUserButton);
   }
   
   private void addInvitedUsers() {
      AsyncServiceFactory.getUserRPCServiceAsync().getPendingInviteesByAccount(new AsyncSuccessCallback<List<User>>() {
         public void onSuccess(List<User> invitedUsers) {
            if (invitedUsers.size() > 0) {
               createInvitedUserGrid();
               invitedUsersGrid.getStore().add(User.createModels(invitedUsers));
            }
         }
      });
   }
   
   private void createInvitedUserGrid() {
      List<ColumnConfig> invitedUserConfigs = new ArrayList<ColumnConfig>();
      invitedUserConfigs.add(new ColumnConfig("email", "Invited user", 180));
      
       GridCellRenderer<BeanModel> comboRenderer = new GridCellRenderer<BeanModel>() {
         public Object render(final BeanModel model, String property, ColumnData config, final int rowIndex,
               final int colIndex, ListStore<BeanModel> store, Grid<BeanModel> grid) {
            return createRoleCombo(model, property);
         }
      };
      
      GridCellRenderer<BeanModel> buttonRenderer = new GridCellRenderer<BeanModel>() {
         public Object render(final BeanModel model, String property, ColumnData config, final int rowIndex,
               final int colIndex, final ListStore<BeanModel> store, Grid<BeanModel> grid) {
            return createDeleteButton(model, store);
         }
      };
      
      ColumnConfig roleColumn = new ColumnConfig("role", "Role", 190);
      roleColumn.setRenderer(comboRenderer);
      invitedUserConfigs.add(roleColumn);
      
      ColumnConfig actionColumn = new ColumnConfig("delete", "Delete", 50);
      actionColumn.setRenderer(buttonRenderer);
      invitedUserConfigs.add(actionColumn);
      
      invitedUsersGrid = new EditorGrid<BeanModel>(new ListStore<BeanModel>(), new ColumnModel(invitedUserConfigs));
      ContentPanel pendingContainer = new ContentPanel();
      pendingContainer.setBodyBorder(false);
      pendingContainer.setHeading("Pending invitations");
      pendingContainer.setLayout(new FitLayout());
      pendingContainer.setSize(440, 150);
      pendingContainer.add(invitedUsersGrid);
      insert(pendingContainer, 1);
      layout();
      center();
   }

   private void createAccessUserGrid() {
      List<ColumnConfig> accessUserConfigs = new ArrayList<ColumnConfig>();
      
       GridCellRenderer<BeanModel> comboRenderer = new GridCellRenderer<BeanModel>() {
         public Object render(final BeanModel model, String property, ColumnData config, final int rowIndex,
               final int colIndex, ListStore<BeanModel> store, Grid<BeanModel> grid) {
            if (cureentUserId != (Long) model.get("oid")) {
               return createRoleCombo(model, property);
            } else {
               return (String) model.get(property);
            }
            
         }
      };
      
      GridCellRenderer<BeanModel> buttonRenderer = new GridCellRenderer<BeanModel>() {
         public Object render(final BeanModel model, String property, ColumnData config, final int rowIndex,
               final int colIndex, final ListStore<BeanModel> store, Grid<BeanModel> grid) {
            Button deleteButton = createDeleteButton(model, store);
            if (cureentUserId == (Long) model.get("oid")) {
               deleteButton.disable();
               deleteButton.hide();
            }
            return deleteButton;
         }
      };
      
      GridCellRenderer<BeanModel> emailRenderer = new GridCellRenderer<BeanModel>() {
         public Object render(final BeanModel model, String property, ColumnData config, final int rowIndex,
               final int colIndex, final ListStore<BeanModel> store, Grid<BeanModel> grid) {
            String html = (String) model.get(property);
            if (cureentUserId == (Long) model.get("oid")) {
               html += "<b> - me</b>";
            }
            return "<span title='" + (String) model.get("username") + "'>" + html + "</span>";
         }
      };
      
      ColumnConfig emailColumn = new ColumnConfig("email", "OpenRemote user", 180);
      emailColumn.setSortable(false);
      emailColumn.setRenderer(emailRenderer);
      accessUserConfigs.add(emailColumn);
      
      ColumnConfig roleColumn = new ColumnConfig("role", "Role", 190);
      roleColumn.setSortable(false);
      roleColumn.setRenderer(comboRenderer);
      accessUserConfigs.add(roleColumn);
      
      ColumnConfig actionColumn = new ColumnConfig("delete", "Delete", 50);
      actionColumn.setSortable(false);
      actionColumn.setRenderer(buttonRenderer);
      accessUserConfigs.add(actionColumn);
      
      final EditorGrid<BeanModel> accessUsersGrid = new EditorGrid<BeanModel>(new ListStore<BeanModel>(), new ColumnModel(accessUserConfigs)) {
         @Override
         protected void afterRender() {
            super.afterRender();
            layout();
            center();
            this.mask("Loading users...");
         }
      };
      
      ContentPanel accessUsersContainer = new ContentPanel();
      accessUsersContainer.setBodyBorder(false);
      accessUsersContainer.setHeading("Users with account access");
      accessUsersContainer.setLayout(new FitLayout());
      accessUsersContainer.setStyleAttribute("paddingTop", "5px");
      accessUsersContainer.setSize(440, 150);
      accessUsersContainer.add(accessUsersGrid);
      add(accessUsersContainer);
      AsyncServiceFactory.getUserRPCServiceAsync().getAccountAccessUsers(new AsyncSuccessCallback<List<User>>() {
         public void onSuccess(List<User> accessUsers) {
            if (accessUsers.size() > 0) {
               accessUsersGrid.getStore().add(User.createModels(accessUsers));
               accessUsersGrid.unmask();
            }
         }
         public void onFailure(Throwable caught) {
            super.onFailure(caught);
            accessUsersGrid.unmask();
         }
      });
   }
   
   private SimpleComboBox<String> createRoleCombo(final BeanModel model, String property) {
      SimpleComboBox<String> combo = new SimpleComboBox<String>();
      combo.setWidth(182);
      combo.setForceSelection(true);
      combo.setEditable(false);
      combo.setTriggerAction(TriggerAction.ALL);
      combo.add(Constants.ROLE_ADMIN_DISPLAYNAME);
      combo.add(Constants.ROLE_MODELER_DISPLAYNAME);
      combo.add(Constants.ROLE_DESIGNER_DISPLAYNAME);
      combo.add(Constants.ROLE_MODELER_DESIGNER_DISPLAYNAME);
      combo.setValue(combo.findModel((String) model.get(property)));
      combo.addSelectionChangedListener(new SelectionChangedListener<SimpleComboValue<String>>(){
         public void selectionChanged(SelectionChangedEvent<SimpleComboValue<String>> se) {
            final String roleStrs = se.getSelectedItem().getValue();
            if (!roleStrs.equals(model.get("role"))) {
               AsyncServiceFactory.getUserRPCServiceAsync().updateUserRoles(((User)model.getBean()).getOid(), roleStrs, new AsyncSuccessCallback<User>() {
                  public void onSuccess(User user) {
                     ((User)model.getBean()).setRoles(user.getRoles());
                     Info.display("Change role", "Change role to " + roleStrs + " success.");
                  }
               });
               
            }
         }
         
      });
      return combo;
   }

   private Button createDeleteButton(final BeanModel model, final ListStore<BeanModel> store) {
      Button deleteButton = new Button();
      deleteButton.setIcon(icons.delete());
      deleteButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
         public void componentSelected(ButtonEvent ce) {
            AsyncServiceFactory.getUserRPCServiceAsync().deleteUser(((User)model.getBean()).getOid(), new AsyncSuccessCallback<Void>() {
               public void onSuccess(Void result) {
                  store.remove(model);
                  Info.display("Delete user", "Delete user " + model.get("username").toString() + " success.");
               }
            });
         }
      });
      return deleteButton;
   }

   private class InviteUserWindow extends FormWindow {
      public InviteUserWindow() {
         setSize(370, 150);
         setHeading("Invite user");
         form.setLabelAlign(LabelAlign.RIGHT);
         createFields();
         createButtons(this);
         add(form);
         show();
      }
      private void createFields() {
         final TextField<String> emailField = new TextField<String>();
         emailField.setFieldLabel("Email address");
         emailField.setAllowBlank(false);
         emailField.setRegex("^[a-zA-Z0-9_\\.]+@[a-zA-Z0-9-]+\\.[a-zA-Z]+$");
         emailField.getMessages().setRegexText("Please input a correct email.");
         
         final ComboBoxExt roleList = new ComboBoxExt();
         roleList.setFieldLabel("Role");
         roleList.getStore().add(new ComboBoxDataModel<String>(Constants.ROLE_ADMIN_DISPLAYNAME, Constants.ROLE_ADMIN_DISPLAYNAME));
         roleList.getStore().add(new ComboBoxDataModel<String>(Constants.ROLE_MODELER_DISPLAYNAME, Constants.ROLE_MODELER_DISPLAYNAME));
         roleList.getStore().add(new ComboBoxDataModel<String>(Constants.ROLE_DESIGNER_DISPLAYNAME, Constants.ROLE_DESIGNER_DISPLAYNAME));
         roleList.getStore().add(new ComboBoxDataModel<String>(Constants.ROLE_MODELER_DESIGNER_DISPLAYNAME, Constants.ROLE_MODELER_DESIGNER_DISPLAYNAME));
         roleList.setValue(new ComboBoxDataModel<String>(Constants.ROLE_MODELER_DISPLAYNAME, Constants.ROLE_MODELER_DISPLAYNAME));
         form.add(emailField);
         form.add(roleList);
         
         form.addListener(Events.BeforeSubmit, new Listener<FormEvent>() {
            public void handleEvent(FormEvent be) {
               form.mask("sending email...");
               AsyncServiceFactory.getUserRPCServiceAsync().inviteUser(emailField.getValue(),
                     roleList.getValue().get("data").toString(), new AsyncSuccessCallback<User>() {
                        public void onSuccess(User user) {
                           form.unmask();
                           fireEvent(SubmitEvent.SUBMIT, new SubmitEvent(user));
                        }
                        public void onFailure(Throwable caught) {
                           super.onFailure(caught);
                           form.unmask();
                        }
                        
                     });
            }
         });
      }
      
      private void createButtons(final InviteUserWindow window) {
         Button send = new Button("Send invitation");
         send.addSelectionListener(new FormSubmitListener(form));
         Button cancel = new Button("Cancel");
         cancel.addSelectionListener(new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent ce) {
               window.hide();
            }
         });
         form.addButton(send);
         form.addButton(cancel);
      }
   }
}
