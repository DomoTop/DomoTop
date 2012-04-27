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
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.openremote.modeler.client.Constants;
import org.openremote.modeler.client.event.DoubleClickEvent;
import org.openremote.modeler.client.event.PropertyEditEvent;
import org.openremote.modeler.client.event.SubmitEvent;
import org.openremote.modeler.client.gxtextends.SelectionServiceExt;
import org.openremote.modeler.client.gxtextends.SourceSelectionChangeListenerExt;
import org.openremote.modeler.client.gxtextends.TreePanelDragSourcePanelTreeExt;
import org.openremote.modeler.client.gxtextends.TreePanelDropTargetPanelTreeExt;
import org.openremote.modeler.client.icon.Icons;
import org.openremote.modeler.client.listener.ConfirmDeleteListener;
import org.openremote.modeler.client.listener.EditDelBtnSelectionListener;
import org.openremote.modeler.client.listener.PanelTreeStoreChangeListener;
import org.openremote.modeler.client.listener.SubmitListener;
import org.openremote.modeler.client.proxy.BeanModelDataBase;
import org.openremote.modeler.client.proxy.UtilsProxy;
import org.openremote.modeler.client.rpc.AsyncSuccessCallback;
import org.openremote.modeler.client.utils.BeanModelTable;
import org.openremote.modeler.client.utils.DeviceBeanModelTable;
import org.openremote.modeler.client.utils.DeviceMacroBeanModelTable;
import org.openremote.modeler.client.utils.IDUtil;
import org.openremote.modeler.client.utils.PropertyEditableFactory;
import org.openremote.modeler.client.utils.ScreenFromTemplate;
import org.openremote.modeler.client.widget.TreePanelBuilder;
import org.openremote.modeler.client.widget.component.ScreenPropertyEditable;
import org.openremote.modeler.domain.Device;
import org.openremote.modeler.domain.DeviceMacro;
import org.openremote.modeler.domain.Group;
import org.openremote.modeler.domain.GroupRef;
import org.openremote.modeler.domain.Panel;
import org.openremote.modeler.domain.ScreenPair;
import org.openremote.modeler.domain.ScreenPairRef;
import org.openremote.modeler.domain.component.UITabbarItem;
import org.openremote.modeler.exception.UIRestoreException;

import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.data.ChangeEvent;
import com.extjs.gxt.ui.client.data.ChangeListener;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Element;

/**
 * The ProfilePanel .
 */
public class ProfilePanel extends ContentPanel {

   private TreePanel<BeanModel> panelTree;
   private Icons icon = GWT.create(Icons.class);
   private SelectionServiceExt<BeanModel> selectionService;
   private ScreenPanel screenPanel = null;
   private boolean initialized = false;
   /**
    * Instantiates a new profile panel.
    */
   public ProfilePanel(ScreenPanel screenPanel) {
      this.screenPanel = screenPanel;
      selectionService = new SelectionServiceExt<BeanModel>();
      setHeading("Panel");
      setIcon(icon.panelIcon());
      setLayout(new FitLayout());
      createMenu();
      createPanelTree();
      new TreePanelDragSourcePanelTreeExt(panelTree);
      new TreePanelDropTargetPanelTreeExt(panelTree);
   }

   /**
    * Creates the menu.
    */
   private void createMenu() {
      ToolBar toolBar = new ToolBar();
      List<Button> editDelBtns = new ArrayList<Button>();
      toolBar.add(createNewBtn());

      Button editBtn = createEditBtn();
      editBtn.setEnabled(false);
      Button deleteBtn = createDeleteBtn();
      deleteBtn.setEnabled(false);

      toolBar.add(editBtn);
      toolBar.add(deleteBtn);
      editDelBtns.add(editBtn);
      editDelBtns.add(deleteBtn);
      selectionService.addListener(new EditDelBtnSelectionListener(editDelBtns) {
         @Override
         protected boolean isEditableAndDeletable(List<BeanModel> sels) {
            BeanModel selectModel = sels.get(0);
            if (selectModel != null) {
               return true;
            }
            return false;
         }
      });

      setTopComponent(toolBar);
   }

   /**
    * Creates the screen tree.
    */
   private void createPanelTree() {
      panelTree = TreePanelBuilder.buildPanelTree(screenPanel);
      selectionService.addListener(new SourceSelectionChangeListenerExt(panelTree.getSelectionModel()));
      selectionService.register(panelTree.getSelectionModel());
      LayoutContainer treeContainer = new LayoutContainer() {
         @Override
         protected void onRender(Element parent, int index) {
            super.onRender(parent, index);
//            addTreeStoreEventListener();
            new PanelTreeStoreChangeListener(panelTree);
            add(panelTree);
            initTreeWithAutoSavedPanels();
            panelTree.addListener(DoubleClickEvent.DOUBLECLICK, new Listener<DoubleClickEvent>() {
               public void handleEvent(DoubleClickEvent be) {
                  editSelectedModel();
               }
               
            });
            
            panelTree.addListener(PropertyEditEvent.PropertyEditEvent, new Listener<PropertyEditEvent>() {
               public void handleEvent(PropertyEditEvent be) {
                  if (be.getPropertyEditable() instanceof ScreenPropertyEditable) {
                     ((ScreenPropertyEditable)be.getPropertyEditable()).setScreenTab(screenPanel.getScreenItem());
                  }
                  ProfilePanel.this.fireEvent(PropertyEditEvent.PropertyEditEvent,be);
               }
               
            });
         }
      };
      // overflow-auto style is for IE hack.
      treeContainer.addStyleName("overflow-auto");
      treeContainer.setStyleAttribute("backgroundColor", "white");
      treeContainer.setBorders(false);
      add(treeContainer);

   }

   private void initTreeWithAutoSavedPanels() {
      UtilsProxy.loadPanelsFromSession(new AsyncSuccessCallback<Collection<Panel>>() {
         @Override
         public void onSuccess(Collection<Panel> panels) {
            if (panels.size() > 0) {
               initModelDataBase(panels);
               panelTree.getStore().removeAll();
               new PanelTreeStoreChangeListener(panelTree);
               for (Panel panel : panels) {
                  BeanModel panelBeanModel = panel.getBeanModel();
                  panelTree.getStore().add(panelBeanModel, false);
                  for (GroupRef groupRef : panel.getGroupRefs()) {
                     panelTree.getStore().add(panelBeanModel, groupRef.getBeanModel(), false);
                     for (ScreenPairRef screenRef : groupRef.getGroup().getScreenRefs()) {
                        panelTree.getStore().add(groupRef.getBeanModel(), screenRef.getBeanModel(), false);
                     }
                  }
               }
               panelTree.expandAll();
               BeanModelDataBase.screenTable.addInsertListener(Constants.SCREEN_TABLE_OID, new ChangeListener() {
                  public void modelChanged(ChangeEvent event) {
                     if (event.getType() == BeanModelTable.ADD) {
                        BeanModel beanModel = (BeanModel) event.getItem();
                        if (beanModel.getBean() instanceof ScreenPair) {
                           screenPanel.setScreenItem(new ScreenTab((ScreenPair) beanModel.getBean()));
                        }
                     }
                  }

               });
            } else {
               panelTree.unmask();
            }
            UtilsProxy.loadMaxID(new AsyncSuccessCallback<Long>() {
               @Override
               public void onSuccess(Long maxID) {
                  if (maxID > 0) {              // set the layout component's max id after refresh page.
                     IDUtil.setCurrentID(maxID.longValue());
                  }
                  initialized = true;
               }
               
            });
         }
         @Override
         public void onFailure(Throwable caught) {
            if (caught instanceof UIRestoreException) {
               initialized = true;
            }
            panelTree.unmask();
            super.onFailure(caught);
            super.checkTimeout(caught);
         }

         private void initModelDataBase(Collection<Panel> panels) {
            BeanModelDataBase.panelTable.clear();
            BeanModelDataBase.groupTable.clear();
            BeanModelDataBase.screenTable.clear();
            Set<Group> groups = new LinkedHashSet<Group>();
            Set<ScreenPair> screens = new LinkedHashSet<ScreenPair>();
            for (Panel panel : panels) {
               List<GroupRef> groupRefs = panel.getGroupRefs();
               for (GroupRef groupRef : groupRefs) {
                  groups.add(groupRef.getGroup());
               }
               BeanModelDataBase.panelTable.insert(panel.getBeanModel());
            }

            for (Group group : groups) {
               List<ScreenPairRef> screenRefs = group.getScreenRefs();
               for (ScreenPairRef screenRef : screenRefs) {
                  screens.add(screenRef.getScreen());
                  BeanModelDataBase.screenTable.insert(screenRef.getScreen().getBeanModel());
               }
               BeanModelDataBase.groupTable.insert(group.getBeanModel());
            }
            
         }
      });
      
   }
   
   /**
    * Creates the new btn.
    * 
    * @return the button
    */
   private Button createNewBtn() {
      Button newButton = new Button("New");
      newButton.setIcon(icon.add());
      Menu newMenu = new Menu();
      newMenu.add(createNewPanelMenuItem());
      newMenu.add(createCustomPanelMenuItem());
      final MenuItem newGroupMenu = createNewGroupMenuItem();
      newMenu.add(newGroupMenu);
      final MenuItem newScreenMenu = createNewScreenMenuItem();
      newMenu.add(newScreenMenu);
      final MenuItem newScreenFromTemplateMenu = createNewScreenFromTemplateMenuItem();
      newMenu.add(newScreenFromTemplateMenu);
//      final MenuItem configTabbarItem = createConfigTabbarMenuItem();
//      newMenu.add(configTabbarItem);
      newMenu.addListener(Events.BeforeShow, new Listener<MenuEvent>() {
         @Override
         public void handleEvent(MenuEvent be) {
//            boolean enabled = false;
//            BeanModel selectedBeanModel = panelTree.getSelectionModel().getSelectedItem();
//            if (selectedBeanModel != null && !(selectedBeanModel.getBean() instanceof ScreenPairRef)) {
//               enabled = true;
//            }
//            configTabbarItem.setEnabled(enabled);
            if (BeanModelDataBase.panelTable.loadAll().size() > 0) {
               newGroupMenu.setEnabled(true);
            } else {
               newGroupMenu.setEnabled(false);
            }
            if (BeanModelDataBase.groupTable.loadAll().size() > 0) {
               newScreenMenu.setEnabled(true);
               newScreenFromTemplateMenu.setEnabled(true);
            } else {
               newScreenMenu.setEnabled(false);
               newScreenFromTemplateMenu.setEnabled(false);
            }
         }
         
      });
      newButton.setMenu(newMenu);
      return newButton;
   }

   /**
    * Creates the edit btn.
    * 
    * @return the button
    */
   private Button createEditBtn() {
      Button editBtn = new Button("Edit");
      editBtn.setIcon(icon.edit());
      editBtn.addSelectionListener(new SelectionListener<ButtonEvent>() {
         @Override
         public void componentSelected(ButtonEvent ce) {
            editSelectedModel();
         }
      });
      return editBtn;
   }

   /**
    * Edits the group.
    * 
    * @param panelBeanModel
    *           the group bean model
    */
   private void editPanel(final BeanModel panelBeanModel) {
      Panel panel = panelBeanModel.getBean();
      if (Constants.CUSTOM_PANEL.equals(panel.getType())) {
         final CustomPanelWindow editCustomPanelWindow = new CustomPanelWindow(panel);
         editCustomPanelWindow.addListener(SubmitEvent.SUBMIT, new SubmitListener() {
            @Override
            public void afterSubmit(SubmitEvent be) {
               editCustomPanelWindow.hide();
               Panel panel = be.<Panel> getData();
               panelTree.getStore().update(panel.getBeanModel());
               for (GroupRef groupRef : panel.getGroupRefs()) {
                  for (ScreenPairRef screenRef : groupRef.getGroup().getScreenRefs()) {
                     ScreenPair screenPair = screenRef.getScreen();
                     screenPair.setTouchPanelDefinition(panel.getTouchPanelDefinition());
                     BeanModelDataBase.screenTable.update(screenPair.getBeanModel());
                  }
               }
               Info.display("Info", "Edit panel " + panel.getName() + " success.");
            }
         });
      } else {
         final PanelWindow panelWindow = new PanelWindow(panelBeanModel);
         panelWindow.addListener(SubmitEvent.SUBMIT, new SubmitListener() {
            @Override
            public void afterSubmit(SubmitEvent be) {
               panelWindow.hide();
               Panel panel = be.<Panel> getData();
               panelTree.getStore().update(panel.getBeanModel());
               Info.display("Info", "Edit panel " + panel.getName() + " success.");
               ProfilePanel.this.fireEvent(PropertyEditEvent.PropertyEditEvent,new PropertyEditEvent(PropertyEditableFactory.getPropertyEditable(panelBeanModel,panelTree)));
            }
         });
      }
   }
   private void editGroup(final BeanModel groupRefBeanModel) {
      final GroupEditWindow groupEditWindow = new GroupEditWindow(groupRefBeanModel);
      groupEditWindow.addListener(SubmitEvent.SUBMIT, new SubmitListener() {
         @Override
         public void afterSubmit(SubmitEvent be) {
            groupEditWindow.hide();
            BeanModel groupRefModel = be.getData();
            GroupRef groupRef = groupRefModel.getBean();
            panelTree.getStore().removeAll(groupRefModel);
            for (ScreenPairRef screenRef : groupRef.getGroup().getScreenRefs()) {
               if (screenRef.getScreen().getRefCount() > 1) {
                  BeanModelDataBase.screenTable.update(screenRef.getScreen().getBeanModel());
               }
               panelTree.getStore().add(groupRefModel, screenRef.getBeanModel(), false);
            }
            panelTree.getStore().update(groupRefModel);
            BeanModelDataBase.groupTable.update(groupRef.getGroup().getBeanModel());
            panelTree.setExpanded(groupRefModel, true);
            panelTree.getSelectionModel().select(groupRefModel, false);
            BeanModelDataBase.screenTable.clearUnuseData();
            Info.display("Info", "Edit Group " + groupRef.getGroup().getName() + " success.");
            ProfilePanel.this.fireEvent(PropertyEditEvent.PropertyEditEvent,new PropertyEditEvent(PropertyEditableFactory.getPropertyEditable(groupRefBeanModel,panelTree)));
         }
      });
   }
   private void editScreen(final BeanModel screenRefBeanModel) {
      final ScreenWindow screenWizard = new ScreenWindow(screenRefBeanModel, ScreenWindow.Operation.EDIT);
      screenWizard.addListener(SubmitEvent.SUBMIT, new SubmitListener() {
         @Override
         public void afterSubmit(SubmitEvent be) {
            screenWizard.hide();
            ScreenPairRef screenRef = be.<ScreenPairRef> getData();
            panelTree.getStore().update(screenRef.getBeanModel());
            BeanModelDataBase.screenTable.update(screenRef.getScreen().getBeanModel());
            Info.display("Info", "Edit screen " + screenRef.getScreen().getName() + " success.");
            ProfilePanel.this.fireEvent(PropertyEditEvent.PropertyEditEvent,new PropertyEditEvent(PropertyEditableFactory.getPropertyEditable(screenRefBeanModel,panelTree)));
         }
         
      });
   }
   /**
    * Creates the delete btn.
    * 
    * @return the button
    */
   private Button createDeleteBtn() {
      Button deleteBtn = new Button("Delete");
      deleteBtn.setIcon(icon.delete());
      deleteBtn.addSelectionListener(new ConfirmDeleteListener<ButtonEvent>() {
         @Override
         public void onDelete(ButtonEvent ce) {
            List<BeanModel> selectedModels = panelTree.getSelectionModel().getSelectedItems();
            for (BeanModel selectedModel : selectedModels) {
               if (selectedModel != null && selectedModel.getBean() instanceof Panel) {
                  Panel panel = selectedModel.getBean();
                  BeanModelDataBase.panelTable.delete(selectedModel);
                  panelTree.getStore().remove(selectedModel);
                  for (GroupRef groupRef : panel.getGroupRefs()) {
                     groupRef.getGroup().releaseRef();
                     if (groupRef.getGroup().getRefCount() == 0) {
                        BeanModelDataBase.groupTable.delete(groupRef.getGroupId());
                     }
                     for (ScreenPairRef screenRef : groupRef.getGroup().getScreenRefs()) {
                        screenRef.getScreen().releaseRef();
                        if (screenRef.getScreen().getRefCount() == 0) {
                           BeanModelDataBase.screenTable.delete(screenRef.getScreenId());
                        } else if (screenRef.getScreen().getRefCount() == 1) {
                           BeanModelDataBase.screenTable.update(screenRef.getScreen().getBeanModel());
                        }
                     }
                  }
                  Info.display("Info", "Delete panel " + selectedModel.get("name") + " success.");
               } else if (selectedModel != null && selectedModel.getBean() instanceof GroupRef) {
                  panelTree.getStore().remove(selectedModel);
                  GroupRef groupRef = selectedModel.getBean();
                  groupRef.getPanel().removeGroupRef(groupRef);
                  groupRef.getGroup().releaseRef();
                  if (groupRef.getGroup().getRefCount() == 0) {
                     BeanModelDataBase.groupTable.delete(groupRef.getGroupId());
                  }
                  for (ScreenPairRef screenRef : groupRef.getGroup().getScreenRefs()) {
                     screenRef.getScreen().releaseRef();
                     if (screenRef.getScreen().getRefCount() == 0) {
                        BeanModelDataBase.screenTable.delete(screenRef.getScreenId());
                     } else if (screenRef.getScreen().getRefCount() == 1) {
                        BeanModelDataBase.screenTable.update(screenRef.getScreen().getBeanModel());
                     }
                  }
               } else if (selectedModel != null && selectedModel.getBean() instanceof ScreenPairRef) {
                  ScreenPairRef screenRef = (ScreenPairRef) selectedModel.getBean();
                  screenRef.getGroup().removeScreenRef(screenRef);
                  panelTree.getStore().remove(selectedModel);
                  screenRef.getScreen().releaseRef();
                  if (screenRef.getScreen().getRefCount() == 0) {
                     BeanModelDataBase.screenTable.delete(screenRef.getScreenId());
                  } else if (screenRef.getScreen().getRefCount() == 1) {
                     BeanModelDataBase.screenTable.update(screenRef.getScreen().getBeanModel());
                  }
               }
            }
         }
      });
      return deleteBtn;
   }

   private void afterCreatePanel(Panel panel) {
      BeanModel panelBeanModel = panel.getBeanModel();
      panelTree.getStore().add(panelBeanModel, false);
      for (GroupRef groupRef : panel.getGroupRefs()) {
         panelTree.getStore().add(panelBeanModel, groupRef.getBeanModel(), false);
         for (ScreenPairRef screenRef : groupRef.getGroup().getScreenRefs()) {
            panelTree.getStore().add(groupRef.getBeanModel(), screenRef.getBeanModel(), false);
         }
      }
      panelTree.setExpanded(panelBeanModel, true, true);
      panelTree.getSelectionModel().select(panelBeanModel, false);
      Info.display("Info", "Create Panel " + panel.getName() + " success.");
   }

   private MenuItem createNewPanelMenuItem() {
      MenuItem newPanelItem = new MenuItem("New Panel");
      newPanelItem.setIcon(icon.panelIcon());
      newPanelItem.addSelectionListener(new SelectionListener<MenuEvent>() {
         public void componentSelected(MenuEvent ce) {
            final PanelWindow panelWindow = new PanelWindow();
            panelWindow.addListener(SubmitEvent.SUBMIT, new SubmitListener() {
               @Override
               public void afterSubmit(SubmitEvent be) {
                  panelWindow.hide();
                  afterCreatePanel(be.<Panel> getData());
               }

            });
            
         }
      });
      return newPanelItem;
   }
   
   private MenuItem createCustomPanelMenuItem() {
      MenuItem customPanelItem = new MenuItem("Custom Panel");
      customPanelItem.setIcon(icon.panelIcon());
      customPanelItem.addSelectionListener(new SelectionListener<MenuEvent>() {
         public void componentSelected(MenuEvent ce) {
            final CustomPanelWindow customPanelWindow = new CustomPanelWindow();
            customPanelWindow.addListener(SubmitEvent.SUBMIT, new SubmitListener() {
               @Override
               public void afterSubmit(SubmitEvent be) {
                  customPanelWindow.hide();
                  afterCreatePanel(be.<Panel> getData());
               }

            });
            
         }
      });
      return customPanelItem;
   }
   private MenuItem createNewGroupMenuItem() {
      MenuItem newGroupItem = new MenuItem("New Group");
      newGroupItem.setIcon(icon.groupIcon());
      newGroupItem.addSelectionListener(new SelectionListener<MenuEvent>() {
         @Override
         public void componentSelected(MenuEvent ce) {
            final Group group = new Group();
            group.setOid(IDUtil.nextID());
            GroupRef groupRef = new GroupRef(group);
            BeanModel selectedBeanModel = panelTree.getSelectionModel().getSelectedItem();
            if (selectedBeanModel != null) {
               Panel panel = null;
               if (selectedBeanModel.getBean() instanceof Panel) {
                  panel = (Panel) selectedBeanModel.getBean();
               } else if (selectedBeanModel.getBean() instanceof GroupRef) {
                  panel = (Panel) panelTree.getStore().getParent(selectedBeanModel).getBean();
               } else if (selectedBeanModel.getBean() instanceof ScreenPairRef) {
                  panel = (Panel) panelTree.getStore().getParent(panelTree.getStore().getParent(selectedBeanModel)).getBean();
               }
               groupRef.setPanel(panel);
               group.setParentPanel(panel);
            }
            final GroupWizardWindow  groupWizardWindow = new GroupWizardWindow(groupRef.getBeanModel());
            groupWizardWindow.addListener(SubmitEvent.SUBMIT, new SubmitListener() {
               @Override
               public void afterSubmit(SubmitEvent be) {
                  groupWizardWindow.hide();
                  BeanModel groupRefModel = be.getData();
                  GroupRef groupRef = groupRefModel.getBean();
                  group.setParentPanel(groupRef.getPanel());
                  panelTree.getStore().add(groupRef.getPanel().getBeanModel(), groupRefModel, false);
                  for (ScreenPairRef screenRef : groupRef.getGroup().getScreenRefs()) {
                     if (screenRef.getScreen().getRefCount() > 1) {
                        BeanModelDataBase.screenTable.update(screenRef.getScreen().getBeanModel());
                     }
                     panelTree.getStore().add(groupRefModel, screenRef.getBeanModel(), false);
                  }
                  BeanModelDataBase.groupTable.insert(groupRef.getGroup().getBeanModel());
                  panelTree.setExpanded(groupRefModel, true);
                  panelTree.getSelectionModel().select(groupRefModel, false);
                  Info.display("Info", "Add Group " + groupRef.getGroup().getName() + " success.");
                  
               }
            });
         }
         
      });
      return newGroupItem;
   }

   private MenuItem createNewScreenFromTemplateMenuItem() {
      MenuItem newScreenItem = new MenuItem("New Screen From Template ");
      newScreenItem.setIcon(icon.screenIcon());
      newScreenItem.addSelectionListener(new SelectionListener<MenuEvent>() {
         public void componentSelected(MenuEvent ce) {
            BeanModel selectedItem = panelTree.getSelectionModel().getSelectedItem();
            
            if (selectedItem == null) {
               MessageBox.alert("Warn", "A group should be selected! ", null);
               return;
            } else if (selectedItem.getBean() instanceof ScreenPairRef) {
               selectedItem = panelTree.getStore().getParent(selectedItem);
            } else if (selectedItem.getBean() instanceof Panel) {
               selectedItem = panelTree.getStore().getChild(selectedItem, 0);
            }
            final GroupRef groupRef = selectedItem.getBean();
            final NewScreenFromTemplateWindow screenWindow = new NewScreenFromTemplateWindow();
            screenWindow.addListener(SubmitEvent.SUBMIT, new SubmitListener() {
               @Override
               public void afterSubmit(SubmitEvent be) {
                  screenWindow.hide();
                  ScreenPairRef screenRef = null;
                  if (be.getData() instanceof ScreenFromTemplate) {
                     ScreenFromTemplate screenFromTemplate = be.<ScreenFromTemplate> getData();
                     ScreenPair screen = screenFromTemplate.getScreen();
                     screen.setTouchPanelDefinition(groupRef.getPanel().getTouchPanelDefinition());
                     screen.setParentGroup(groupRef.getGroup());
                     screenRef = new ScreenPairRef(screen);
                     screenRef.setTouchPanelDefinition(screen.getTouchPanelDefinition());
                     screenRef.setOid(IDUtil.nextID());
                     groupRef.getGroup().addScreenRef(screenRef);
                     screenRef.setGroup(groupRef.getGroup());
                     updatePanelTree(screenRef);
                     BeanModelDataBase.screenTable.insert(screen.getBeanModel());
                     // ----------rebuild command
                     Set<Device> devices = screenFromTemplate.getDevices();
                     for (Device device : devices) {
                        ((DeviceBeanModelTable) BeanModelDataBase.deviceTable)
                              .insertAndNotifyDeviceInsertListener(device.getBeanModel());
                     }

                     Set<DeviceMacro> macros = screenFromTemplate.getMacros();
                     for (DeviceMacro macro : macros) {
                        ((DeviceMacroBeanModelTable) BeanModelDataBase.deviceMacroTable)
                              .insertAndNotifyMacroInsertListener(macro.getBeanModel());
                     }
                  }
               }

               private void updatePanelTree(ScreenPairRef screenRef) {
                  panelTree.getStore().add(groupRef.getBeanModel(), screenRef.getBeanModel(), false);
                  panelTree.setExpanded(groupRef.getBeanModel(), true);
                  panelTree.getSelectionModel().select(screenRef.getBeanModel(), false);
               }

            });
         }
      });
      return newScreenItem;
   }
   private MenuItem createNewScreenMenuItem() {
      MenuItem newScreenItem = new MenuItem("New Screen");
      newScreenItem.setIcon(icon.screenIcon());
      newScreenItem.addSelectionListener(new SelectionListener<MenuEvent>() {
         public void componentSelected(MenuEvent ce) {
            BeanModel selectItem = panelTree.getSelectionModel().getSelectedItem();
            if (selectItem != null) {
               if (selectItem.getBean() instanceof ScreenPairRef) {
                  selectItem = panelTree.getStore().getParent(selectItem);
               } else if (selectItem.getBean() instanceof Panel) {
                  selectItem = panelTree.getStore().getChild(selectItem, 0);
               }
            }
            final ScreenWindow screenWindow = new ScreenWindow(selectItem);
            screenWindow.addListener(SubmitEvent.SUBMIT, new SubmitListener() {
               @Override
               public void afterSubmit(SubmitEvent be) {
                  screenWindow.hide();
                  ScreenPairRef screenRef = null;
                  if (be.getData() instanceof ScreenPairRef) {
                     screenRef = be.<ScreenPairRef>getData();
                     updatePanelTree(screenRef);
                     BeanModelDataBase.screenTable.insert(screenRef.getScreen().getBeanModel());
                  }
               }
               
               private void updatePanelTree(ScreenPairRef screenRef) {
                  panelTree.getStore().add(screenWindow.getSelectedGroupRefModel(), screenRef.getBeanModel(), false);
                  panelTree.setExpanded(screenWindow.getSelectedGroupRefModel(), true);
                  panelTree.getSelectionModel().select(screenRef.getBeanModel(), false);
               }

            });

         }
      });
      return newScreenItem;
   }

   @SuppressWarnings("unused")
   private MenuItem createConfigTabbarMenuItem() {
      MenuItem configTabbarItem = new MenuItem("Config tabbar");
      configTabbarItem.setIcon(icon.tabbarConfigIcon());
      configTabbarItem.addSelectionListener(new SelectionListener<MenuEvent>() {
         @Override
         public void componentSelected(MenuEvent ce) {
            final BeanModel selectItem = panelTree.getSelectionModel().getSelectedItem();
            if (selectItem != null) {
               if (selectItem.getBean() instanceof Panel) {
                  final TabbarWindow tabbarWindow = new TabbarWindow(true, ((Panel) selectItem.getBean())
                        .getTabbarItems(), (Panel) selectItem.getBean());
                  tabbarWindow.addListener(SubmitEvent.SUBMIT, new SubmitListener() {
                     @Override
                     public void afterSubmit(SubmitEvent be) {
                    	 List<UITabbarItem> tabbarItems = be.<List<UITabbarItem>>getData();
                    	 ((Panel) selectItem.getBean()).setTabbarItems(tabbarItems);
                    	 if (tabbarItems.size() > 0) {
                           for (GroupRef groupRef : ((Panel) selectItem.getBean()).getGroupRefs()) {
                              for (ScreenPairRef screenRef : groupRef.getGroup().getScreenRefs()) {
//                                 screenRef.getScreen().setHasTabbar(true);
                                 BeanModelDataBase.screenTable.update(screenRef.getScreen().getBeanModel());
                              }
                           }
                        } else {
                           for (GroupRef groupRef : ((Panel) selectItem.getBean()).getGroupRefs()) {
                              for (ScreenPairRef screenRef : groupRef.getGroup().getScreenRefs()) {
//                                 screenRef.getScreen().setHasTabbar(false);
                                 BeanModelDataBase.screenTable.update(screenRef.getScreen().getBeanModel());
                              }
                           }
                        }
                        tabbarWindow.hide();
                     }
                  });
               } else if (selectItem.getBean() instanceof GroupRef) {
                  final Group group = ((GroupRef) selectItem.getBean()).getGroup();
                  final TabbarWindow tabbarWindow = new TabbarWindow(false, group.getTabbarItems(),
                        ((GroupRef) selectItem.getBean()).getPanel());
                  tabbarWindow.addListener(SubmitEvent.SUBMIT, new SubmitListener() {
                     @Override
                     public void afterSubmit(SubmitEvent be) {
                        List<UITabbarItem> tabbarItems = be.<List<UITabbarItem>> getData();
                        group.setTabbarItems(tabbarItems);
                        if (tabbarItems.size() > 0) {
                           for (ScreenPairRef screenRef : group.getScreenRefs()) {
//                              screenRef.getScreen().setHasTabbar(true);
                              BeanModelDataBase.screenTable.update(screenRef.getScreen().getBeanModel());
                           }
                        } else {
                           for (ScreenPairRef screenRef : group.getScreenRefs()) {
//                              screenRef.getScreen().setHasTabbar(false);
                              BeanModelDataBase.screenTable.update(screenRef.getScreen().getBeanModel());
                           }
                        }

                        tabbarWindow.hide();
                     }
                  });
               }
               panelTree.getStore().update(selectItem);
            }
         }
      });
      return configTabbarItem;
   }
   public TreePanel<BeanModel> getPanelTree() {
      return panelTree;
   }

   public void setPanelTree(TreePanel<BeanModel> panelTree) {
      this.panelTree = panelTree;
   }

   /**
    * 
    */
   private void editSelectedModel() {
      BeanModel selectedModel = panelTree.getSelectionModel().getSelectedItem();
      if (selectedModel != null) {
         if (selectedModel.getBean() instanceof Panel) {
            editPanel(selectedModel);
         } else if (selectedModel.getBean() instanceof GroupRef) {
            editGroup(selectedModel);
         } else if (selectedModel.getBean() instanceof ScreenPairRef) {
            editScreen(selectedModel);
         }
      }
   }

   public boolean isInitialized() {
      return initialized;
   }
}
