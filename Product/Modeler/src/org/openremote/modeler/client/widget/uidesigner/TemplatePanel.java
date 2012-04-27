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
package org.openremote.modeler.client.widget.uidesigner;

import java.util.ArrayList;
import java.util.List;

import org.openremote.modeler.client.event.SubmitEvent;
import org.openremote.modeler.client.gxtextends.SelectionServiceExt;
import org.openremote.modeler.client.gxtextends.SourceSelectionChangeListenerExt;
import org.openremote.modeler.client.icon.Icons;
import org.openremote.modeler.client.listener.ConfirmDeleteListener;
import org.openremote.modeler.client.listener.EditDelBtnSelectionListener;
import org.openremote.modeler.client.listener.SubmitListener;
import org.openremote.modeler.client.model.TreeFolderBean;
import org.openremote.modeler.client.proxy.TemplateProxy;
import org.openremote.modeler.client.rpc.AsyncSuccessCallback;
import org.openremote.modeler.client.widget.TreePanelBuilder;
import org.openremote.modeler.client.widget.buildingmodeler.TemplateCreateWindow;
import org.openremote.modeler.domain.ScreenPair;
import org.openremote.modeler.domain.Template;
import org.openremote.modeler.selenium.DebugId;

import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The Template panel.
 *
 * @author Javen
 * @author <a href = "mailto:juha@openremote.org">Juha Lindfors</a>
 *
 */
public class TemplatePanel extends ContentPanel {
   private TreePanel<BeanModel> templateTree = TreePanelBuilder.buildTemplateTree(this);
   
   private ScreenPanel templateEditPanel = null;
   
   private Template templateInEditing = null;
   
   private ScreenTab editTabItem = null;
   
   private static final int AUTO_SAVE_INTERVAL_MS = 30000;

   private Timer timer;

   private Icons icon = GWT.create(Icons.class);
   
   private SelectionServiceExt<BeanModel> selectionService;
   
   public TemplatePanel(ScreenPanel templateEditPanel) {
      this.templateEditPanel = templateEditPanel;
      selectionService = new SelectionServiceExt<BeanModel>();
      setExpanded(false);
      setHeading("Template");
      setIcon(icon.templateIcon());
      setLayout(new FitLayout());
      createMenu();
      createTreeContainer();
      createAutoSaveTimer();
   }

   /**
    * Creates the menu.
    */
   private void createMenu() {
      ToolBar toolBar = new ToolBar();
      List<Button> menuButtons = new ArrayList<Button>();
      toolBar.add(createNewTemplateMenuItem());

      Button editBtn = createEditTemplateMenuItem();
      editBtn.setEnabled(false);
      
      Button deleteBtn = createDeleteBtn();
      
      List<Button> editDelBtns = new ArrayList<Button>();
      editDelBtns.add(editBtn);
      editDelBtns.add(deleteBtn);
      deleteBtn.setEnabled(false); 
      
      selectionService.addListener(new EditDelBtnSelectionListener(editDelBtns) {
         @Override
         protected boolean isEditableAndDeletable(List<BeanModel> sels) {
            BeanModel selectModel = sels.get(0);
            if (selectModel != null && selectModel.getBean() instanceof Template) {
               return true;
            }
            return false;
         }
      });
      
      toolBar.add(editBtn);
      toolBar.add(deleteBtn);
      menuButtons.add(deleteBtn);
      setTopComponent(toolBar);
   }


   /**
    * Creates the delete button.
    * 
    * @return the button
    */
   private Button createDeleteBtn() {
      Button deleteBtn = new Button("Delete");
      deleteBtn.setIcon(icon.delete());

      deleteBtn.addSelectionListener(new ConfirmDeleteListener<ButtonEvent>() {
         @Override
         public void onDelete(ButtonEvent ce) {
            List<BeanModel> templateBeanModels = templateTree.getSelectionModel().getSelectedItems();
            if (templateBeanModels == null || templateBeanModels.size() == 0) {
               MessageBox.alert("Error", "Please select a template.", null);
               ce.cancelBubble();
            } else {
               for (final BeanModel templateBeanModel : templateBeanModels) {
                  Template template = templateBeanModel.getBean();
                  Long oid = template.getOid();
                  TemplateProxy.deleteTemplateById(oid, new AsyncSuccessCallback<Boolean>() {
                     @Override
                     public void onSuccess(Boolean success) {
                        if (success) {
                           templateInEditing = null;
                           templateTree.getStore().remove(templateBeanModel);
                           if (editTabItem != null) {
                              templateEditPanel.remove(editTabItem);
                              templateEditPanel.closeCurrentScreenTab();
                              editTabItem = null;
                              templateEditPanel.layout();
                           }
                           Info.display("Delete Template", "Template deleted successfully.");
                        }
                     }

                     @Override
                     public void onFailure(Throwable caught) {
                        MessageBox.alert("Error", "Failed to delete template :\"" + caught.getMessage() + "\"", null);
                        super.checkTimeout(caught);
                     }

                  });

               }
            }
         }
      });
      return deleteBtn;
   }

   private Button createEditTemplateMenuItem() {
      Button editTempalteMenuItem = new Button("Edit");
      editTempalteMenuItem.setIcon(icon.edit());
      editTempalteMenuItem.addSelectionListener(new SelectionListener<ButtonEvent>() {
         public void componentSelected(ButtonEvent ce) {
            BeanModel selectedBean= templateTree.getSelectionModel().getSelectedItem();
            if( selectedBean == null || !(selectedBean.getBean() instanceof Template)) {
               MessageBox.alert("Warn","A template must be selected!",null);
               return;
            }
            //remember the share type information before being updated. 
            
            final BeanModel privateTemplateTopNode = templateTree.getStore().getChild(0);
            final BeanModel publicTemplateTopNode = templateTree.getStore().getChild(1);
            final Template template = selectedBean.getBean();
            final boolean shareType = template.isShared();
            
            final TemplateCreateWindow templateCreateWindow = new TemplateCreateWindow(template);
            
            templateCreateWindow.addListener(SubmitEvent.SUBMIT, new SubmitListener() {
               @Override
               public void afterSubmit(SubmitEvent be) {
                  Template t = be.getData();
                  template.setContent(t.getContent());
                  template.setScreen(t.getScreen());
                  template.setKeywords(t.getKeywords());
                  template.setShared(t.isShared());
                  if (t.isShared() == shareType) {
                     templateTree.getStore().update(template.getBeanModel());
                  } else {
                     templateTree.getStore().remove(template.getBeanModel());
                     BeanModel parentNode = template.isShared()?publicTemplateTopNode:privateTemplateTopNode;
                     templateTree.getStore().add(parentNode, template.getBeanModel(),false);
                  }
                  editTabItem = new ScreenTab(template.getScreen());
                  templateEditPanel.setScreenItem(editTabItem);
               }

            });

         }
      });
      return editTempalteMenuItem;
   }
   private Button createNewTemplateMenuItem() {
      Button newTemplateMenuItem = new Button("New");
      newTemplateMenuItem.setIcon(icon.add());
      newTemplateMenuItem.addSelectionListener(new SelectionListener<ButtonEvent>() {
         public void componentSelected(ButtonEvent ce) {
            final BeanModel privateTemplateTopNode = templateTree.getStore().getChild(0);
            final BeanModel publicTemplateTopNode = templateTree.getStore().getChild(1);
            BeanModel selectedModel = templateTree.getSelectionModel().getSelectedItem();
            boolean isShare = false;
            if (selectedModel != null) {
               if (selectedModel.getBean() instanceof TreeFolderBean && publicTemplateTopNode == selectedModel) {
                  isShare = true;
               } else if (selectedModel.getBean() instanceof Template && ((Template) selectedModel.getBean()).isShared()) {
                  isShare = true;
               }
            }
            final TemplateCreateWindow templateCreateWindow = new TemplateCreateWindow(isShare);
            templateCreateWindow.addListener(SubmitEvent.SUBMIT, new SubmitListener() {
               @Override
               public void afterSubmit(SubmitEvent be) {
                  Template template = be.getData();
                  if (template.isShared()) {
                     templateTree.getStore().add(publicTemplateTopNode, template.getBeanModel(),false);
                  } else {
                     templateTree.getStore().add(privateTemplateTopNode, template.getBeanModel(),false);
                  }
                  layout();
               }

            });

         }
      });
      return newTemplateMenuItem;
   }

   
   private void createTreeContainer() {
      LayoutContainer treeContainer = new LayoutContainer() {
         @Override
         protected void onRender(Element parent, int index) {
            super.onRender(parent, index);
            add(templateTree);
         }
         
      };
      treeContainer.ensureDebugId(DebugId.DEVICE_TREE_CONTAINER);
   // overflow-auto style is for IE hack.
      treeContainer.addStyleName("overflow-auto");
      treeContainer.setStyleAttribute("backgroundColor", "white");
      treeContainer.setBorders(false);
      add(treeContainer);
      
      selectionService.addListener(new SourceSelectionChangeListenerExt(templateTree.getSelectionModel()));
      selectionService.register(templateTree.getSelectionModel());
      
   }
   
   
   private void createAutoSaveTimer() {
      timer = new Timer() {
         @Override
         public void run() {
            saveTemplateUpdates();
         }
      };
      timer.scheduleRepeating(AUTO_SAVE_INTERVAL_MS);
      
   }
   
   public void saveTemplateUpdates() {
      if (templateInEditing != null) {
         TemplateProxy.updateTemplate(templateInEditing, new AsyncCallback<Template>(){

            @Override
            public void onFailure(Throwable caught) {
               Info.display("Error","Update template: "+templateInEditing.getName()+" failed");
            }

            @Override
            public void onSuccess(Template result) {
               if (result != null && result.getOid() == templateInEditing.getOid()) {
                  templateInEditing.setContent(result.getContent());
                  Info.display("Success", "Save template " + templateInEditing.getName()+" successfully !");
                  // stop auto-saving when the template preview tab has been closed. 
                  if (editTabItem != null && templateEditPanel.indexOf(editTabItem) == -1) {
                     templateInEditing = null;
                  }
               }
               
            }
            
         });
      }
   }

   public Template getTemplateInEditing() {
      return templateInEditing;
   }

   public synchronized void  setTemplateInEditing(final Template templateInEditing) {
      if (templateInEditing != null && this.templateInEditing != null ) {
         if (templateInEditing.getOid() == this.templateInEditing.getOid()) return;
      }
      if (this.templateInEditing != null) {
         // 1, save previous template.
         mask("Saving previous template.....");
         TemplateProxy.updateTemplate(this.templateInEditing, new AsyncCallback<Template>() {

            @Override
            public void onFailure(Throwable caught) {
               unmask();
               Info.display("Error", "Update template: " + TemplatePanel.this.templateInEditing.getName() + " failed");
               buildScreen(templateInEditing);
            }

            @Override
            public void onSuccess(Template result) {
               // 2, make sure the content for the previous template be updated. 
               if (result.getOid() == TemplatePanel.this.templateInEditing.getOid()){
                  TemplatePanel.this.templateInEditing.setContent(result.getContent());
                  Info.display("Success", "Save template " + TemplatePanel.this.templateInEditing.getName() + " successfully !");
               }
               mask("Building screen and downloading resources ...");
               // 3, edit another template.
               buildScreen(templateInEditing);
            }

            
         });
      } else {
         mask("Building screen and downloading resources ...");
         this.templateInEditing = templateInEditing;
         buildScreen(templateInEditing);
      }
   }
   
   private void buildScreen(final Template templateInEditing) {
      if (templateInEditing.getScreen() != null) {
         unmask();
         editTabItem = new ScreenTab(templateInEditing.getScreen());
         templateEditPanel.setScreenItem(editTabItem);
         templateInEditing.setScreen(templateInEditing.getScreen());
         TemplatePanel.this.templateInEditing = templateInEditing;
         return;
      }
      
      TemplateProxy.buildScreen(templateInEditing, new AsyncCallback<ScreenPair>() {

         @Override
         public void onFailure(Throwable caught) {
            MessageBox.alert("Error", "Failed to preview Template: " + templateInEditing.getName(), null);
            unmask();
         }

         @Override
         public void onSuccess(ScreenPair screen) {
            unmask();
            editTabItem = new ScreenTab(screen);
            templateEditPanel.setScreenItem(editTabItem);
            templateInEditing.setScreen(screen);
            TemplatePanel.this.templateInEditing = templateInEditing;
         }

      });
   }

}
