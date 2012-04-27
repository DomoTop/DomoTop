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
package org.openremote.modeler.client.widget;

import org.openremote.modeler.client.Constants;
import org.openremote.modeler.client.icon.Icons;
import org.openremote.modeler.client.model.AutoListenableTreeStore;
import org.openremote.modeler.client.model.TreeFolderBean;
import org.openremote.modeler.client.proxy.BeanModelDataBase;
import org.openremote.modeler.client.utils.BeanModelTable;
import org.openremote.modeler.domain.CommandDelay;
import org.openremote.modeler.domain.Device;
import org.openremote.modeler.domain.DeviceCommand;
import org.openremote.modeler.domain.DeviceCommandRef;
import org.openremote.modeler.domain.DeviceMacro;
import org.openremote.modeler.domain.DeviceMacroItem;
import org.openremote.modeler.domain.DeviceMacroRef;

import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.data.ModelIconProvider;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

/**
 * The Class is used for create tree.
 */
public class AutoListenableTreePanelBuilder {

   /**
    * Not be instantiated.
    */
   private AutoListenableTreePanelBuilder() {
   }

   /** The Constant icon. */
   private static final Icons ICON = GWT.create(Icons.class);

   /** The device command treestore. */
   private static TreeStore<BeanModel> devicesAndMacrosTreeStore = null;
   
   /**
    * Builds a device command tree.
    * 
    * @return the a new device command tree
    */
   public static TreePanel<BeanModel> buildDevicesAndMacrosTree() {
      initTreeStore();      
      return initDevicesAndMacrosTree();
   }

   /**
    * Inits the tree store.
    */
   @SuppressWarnings("unchecked")
   private static void initTreeStore() {
      if (devicesAndMacrosTreeStore == null) {
        devicesAndMacrosTreeStore = new AutoListenableTreeStore<BeanModel>() {
           
           @Override
           protected BeanModelTable getBeanModelTable(BeanModel beanModel) {
               if (beanModel.getBean() instanceof TreeFolderBean) {
                  TreeFolderBean treeFolderBean = (TreeFolderBean) beanModel.getBean();
                  if (Constants.DEVICES.equals(treeFolderBean.getType())) {
                     return BeanModelDataBase.deviceTable;
                  } else if (Constants.MACROS.equals(treeFolderBean.getType())) {
                     return BeanModelDataBase.deviceMacroTable;
                  }
               } else if (beanModel.getBean() instanceof Device) {
                  return BeanModelDataBase.deviceCommandTable;
               } else if (beanModel.getBean() instanceof DeviceMacro) {
                  return BeanModelDataBase.deviceMacroItemTable;
               }
               return null;
            }

         @Override
         protected boolean isReallyHasChildren(BeanModel beanModel) {
            if (beanModel.getBean() instanceof DeviceCommand) {
               return false;
            }
            if (beanModel.getBean() instanceof DeviceMacroItem) {
               return false;
            }
            return  true;
         }
        };
        
      }
      createFolders();
   }
   
   /**
    * Creates the folders.
    */
   private static void createFolders() {
      TreeFolderBean devicesBean = new TreeFolderBean();
      devicesBean.setDisplayName("Devices");
      devicesBean.setType(Constants.DEVICES);
      devicesBean.setOid(Constants.DEVICES_OID);
      TreeFolderBean macrosBean = new TreeFolderBean();
      macrosBean.setDisplayName("Macros");
      macrosBean.setType(Constants.MACROS);
      macrosBean.setOid(Constants.MACROS_OID);
      devicesAndMacrosTreeStore.add(devicesBean.getBeanModel(), true);
      devicesAndMacrosTreeStore.add(macrosBean.getBeanModel(), true);
   }

   /**
    * Inits the devices and macros tree.
    * 
    * @return the tree panel< bean model>
    */
   private static TreePanel<BeanModel> initDevicesAndMacrosTree() {
      TreePanel<BeanModel> devicesAndMacrosTree = new TreePanel<BeanModel>(devicesAndMacrosTreeStore);
      devicesAndMacrosTree.setBorders(false);
      devicesAndMacrosTree.setStateful(true);
      devicesAndMacrosTree.setDisplayProperty("displayName");
      devicesAndMacrosTree.setStyleAttribute("overflow", "auto");
      devicesAndMacrosTree.setHeight("100%");
      devicesAndMacrosTree.setIconProvider(new ModelIconProvider<BeanModel>() {
         public AbstractImagePrototype getIcon(BeanModel beanModel) {
            if (beanModel.getBean() instanceof Device) {
               return ICON.device();
            } else if ((beanModel.getBean() instanceof DeviceCommand)
                  || (beanModel.getBean() instanceof DeviceCommandRef)) {
               return ICON.deviceCmd();
            } else if ((beanModel.getBean() instanceof DeviceMacro) || (beanModel.getBean() instanceof DeviceMacroRef)) {
               return ICON.macroIcon();
            } else if (beanModel.getBean() instanceof CommandDelay) {
               return ICON.delayIcon();
            } else if (beanModel.getBean() instanceof TreeFolderBean) {
               if (((TreeFolderBean) beanModel.getBean()).getType().equals(Constants.DEVICES)) {
                  return ICON.devicesRoot();
               } else if (((TreeFolderBean) beanModel.getBean()).getType().equals(Constants.MACROS)) {
                  return ICON.macrosRoot();
               }
            }
            return ICON.folder();
         }
      });
      return devicesAndMacrosTree;
   }
}
