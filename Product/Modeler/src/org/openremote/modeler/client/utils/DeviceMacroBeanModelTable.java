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
package org.openremote.modeler.client.utils;

import java.util.ArrayList;
import java.util.List;

import org.openremote.modeler.client.Constants;
import org.openremote.modeler.client.model.AutoListenableTreeStore;
import org.openremote.modeler.client.proxy.BeanModelDataBase;
import org.openremote.modeler.client.proxy.DeviceMacroBeanModelProxy;
import org.openremote.modeler.client.rpc.AsyncSuccessCallback;
import org.openremote.modeler.domain.BusinessEntity;
import org.openremote.modeler.domain.Device;
import org.openremote.modeler.domain.DeviceCommand;
import org.openremote.modeler.domain.DeviceCommandRef;
import org.openremote.modeler.domain.DeviceMacro;
import org.openremote.modeler.domain.DeviceMacroItem;
import org.openremote.modeler.domain.DeviceMacroRef;

import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.data.ChangeEvent;
import com.extjs.gxt.ui.client.data.ChangeListener;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.store.TreeStoreEvent;

/**
 * For store BeanModel in Frontend.<br/> Every operation to the table will notify the listener on it.<br/>
 * 
 * <br/>
 * 
 * The notification only support add,update,remove. <br/> Use {@link ChangeListener} to listen the state of this table.
 * Use changeEvent.getItem() to get the beanmodel which had changed.
 * 
 * @author allen.wei
 */
public class DeviceMacroBeanModelTable extends BeanModelTable {

private List<DeviceMacroInsertListener<BeanModel>> deviceMacroInsertListeners = new ArrayList<DeviceMacroInsertListener<BeanModel>> ();
   
   public void addDeviceMacroInsertListener(DeviceMacroInsertListener<BeanModel> deviceMacroInsertListener){
      deviceMacroInsertListeners.add(deviceMacroInsertListener);
   }
   
   public void removeDeviceMacroInsertListener(DeviceMacroInsertListener<BeanModel> deviceMacroInsertListener){
      deviceMacroInsertListeners.remove(deviceMacroInsertListener);
   }
   
   
   /**
    * Instantiates a new bean model table.
    */
   public DeviceMacroBeanModelTable() {
      super();
   }

   public void insertAndNotifyMacroInsertListener (BeanModel beanModel) {
      insert(beanModel);
      for(DeviceMacroInsertListener<BeanModel> listener : deviceMacroInsertListeners) {
         listener.handleInsert(beanModel);
      }
   }
   /**
    * {@inheritDoc}
    */
   @Override
   public void loadFromTable(BeanModel parent, final AsyncSuccessCallback<List<BeanModel>> asyncSuccessCallback) {
      DeviceMacroBeanModelProxy.loadDeviceMaro(parent, new AsyncSuccessCallback<List<BeanModel>>() {
         public void onSuccess(List<BeanModel> result) {
            asyncSuccessCallback.onSuccess(result);
         }            
      });
     
   }
   
   /* (non-Javadoc)
    * @see org.openremote.modeler.client.utils.BeanModelTable#addInsertListener(com.extjs.gxt.ui.client.data.BeanModel, com.extjs.gxt.ui.client.store.TreeStore)
    */
   @SuppressWarnings("unchecked")
   @Override
   public void addInsertListener(final BeanModel parentBeanModel, final TreeStore treeStore) {
      long parentOid = BeanModelDataBase.getSourceBeanModelId(parentBeanModel);
      
      addInsertListener(parentOid, new ChangeListener() {
         public void modelChanged(ChangeEvent event) {
            BeanModel sourceBeanModel = (BeanModel) event.getItem();
            if (event.getType() == BeanModelTable.ADD) {
               if (parentBeanModel == null) {
                  treeStore.add(sourceBeanModel, false);
               } else {
                  long sourceParentBeanModelOid = -1;
                  if (sourceBeanModel.getBean() instanceof DeviceMacro) {
                     treeStore.add(parentBeanModel, sourceBeanModel, false);
                     return;
                  } else if (sourceBeanModel.getBean() instanceof DeviceMacroItem) {
                     DeviceMacroItem deviceMacroItem = (DeviceMacroItem) sourceBeanModel.getBean();
                     sourceParentBeanModelOid = deviceMacroItem.getParentDeviceMacro().getOid();
                  }
                  if (((BusinessEntity) parentBeanModel.getBean()).getOid() == sourceParentBeanModelOid) {
                     treeStore.add(parentBeanModel, sourceBeanModel, false);
                  }
               }
            }
         }

      });
   }

   /*
    * (non-Javadoc)
    * 
    * @seeorg.openremote.modeler.client.utils.BeanModelTable#addChangeListener(org.openremote.modeler.client.model.
    * MagicTreeStore, org.openremote.modeler.client.utils.BeanModelTable, com.extjs.gxt.ui.client.store.TreeStoreEvent)
    */
   @SuppressWarnings("unchecked")
   @Override
   public void addChangeListener(final AutoListenableTreeStore treeStore, TreeStoreEvent<BeanModel> be) {
      for (final BeanModel targetBeanModel : be.getChildren()) {   
         ChangeListener changeListener = getChangeListenerFromMap(targetBeanModel);
         if (changeListener == null) {
            addChangeListener(BeanModelDataBase.getBeanModelId(targetBeanModel), new ChangeListener() {
               public void modelChanged(ChangeEvent event) {
                  BeanModel sourceBeanModel = (BeanModel) event.getItem();
                  if (event.getType() == UPDATE) {
                       targetBeanModel.set("name", sourceBeanModel.get("name"));
                     treeStore.update(targetBeanModel);
                  } else if (event.getType() == REMOVE) {
                     treeStore.remove(targetBeanModel);
                  }
               }
            });
            addDeviceMacroItemCascadeChangeListener(treeStore, targetBeanModel);
            putChangeListenerIntoMap(targetBeanModel, changeListener);
         }
      }
   }

   /**
    * Adds the cascade change listener.
    * 
    * @param treeStore the tree store
    * @param targetBeanModel the target bean model
    */
   @SuppressWarnings("unchecked")
   private void addDeviceMacroItemCascadeChangeListener(final AutoListenableTreeStore treeStore, final BeanModel targetBeanModel) {
      
      ChangeListener cascadeChangeLisntener = new ChangeListener() {
         public void modelChanged(ChangeEvent event) {
            if (event.getType() == UPDATE) {
               BeanModel sourceBeanModel = (BeanModel) event.getItem();

               if (sourceBeanModel.getBean() instanceof DeviceMacro) {
                  DeviceMacro deviceMacro = (DeviceMacro) sourceBeanModel.getBean();
                  DeviceMacroRef deviceMacroRef = (DeviceMacroRef) targetBeanModel.getBean();
                  deviceMacroRef.setTargetDeviceMacro(deviceMacro);
               } else if (sourceBeanModel.getBean() instanceof DeviceCommand) {
                  DeviceCommand deviceCommand = (DeviceCommand) sourceBeanModel.getBean();
                  DeviceCommandRef deviceCommandRef = (DeviceCommandRef) targetBeanModel.getBean();
                  deviceCommandRef.setDeviceCommand(deviceCommand);
               } else if (sourceBeanModel.getBean() instanceof Device) {
                  Device device = (Device) sourceBeanModel.getBean();
                  DeviceCommandRef targetDeviceCommandRef = (DeviceCommandRef) targetBeanModel.getBean();
                  targetDeviceCommandRef.setDeviceName(device.getName());
               }
               treeStore.update(targetBeanModel);
            } else if (event.getType() == REMOVE) {
               treeStore.remove(targetBeanModel);
            }
         }
      };
      
      if (targetBeanModel.getBean() instanceof DeviceMacroItem) {
         DeviceMacroItem deviceMacroItem = (DeviceMacroItem) targetBeanModel.getBean();
         if (deviceMacroItem instanceof DeviceMacroRef) {
            long deviceMacroOid = BeanModelDataBase.getOriginalDeviceMacroItemBeanModelId(targetBeanModel);
            BeanModelDataBase.deviceMacroTable.addChangeListener(deviceMacroOid, cascadeChangeLisntener);
         } else if (deviceMacroItem instanceof DeviceCommandRef) {
            long deviceOid = BeanModelDataBase.getSourceBeanModelId(targetBeanModel);
            long deviceCommandOid = BeanModelDataBase.getOriginalDeviceMacroItemBeanModelId(targetBeanModel);
            BeanModelDataBase.deviceTable.addChangeListener(deviceOid, cascadeChangeLisntener);
            BeanModelDataBase.deviceCommandTable.addChangeListener(deviceCommandOid, cascadeChangeLisntener);
         }
      }
   }
   
   /* (non-Javadoc)
    * @see org.openremote.modeler.client.utils.BeanModelTable#excuteNotify(com.extjs.gxt.ui.client.data.ChangeEvent)
    */
   @Override
   protected void excuteNotify(ChangeEvent evt) {
      BeanModel beanModel = (BeanModel) evt.getItem();
      if (beanModel.getBean() instanceof DeviceMacro) {
         if (insertListeners.get(Constants.MACROS_OID) != null) {
            insertListeners.get(Constants.MACROS_OID).modelChanged(evt);
         }
      } else if (beanModel.getBean() instanceof DeviceMacroItem) {
         DeviceMacroItem deviceMacroItem = (DeviceMacroItem) beanModel.getBean();
         ChangeListener changeListener = insertListeners.get(deviceMacroItem.getParentDeviceMacro().getOid());
         if (changeListener != null) {
            changeListener.modelChanged(evt);
         }
      }
   }

   public static interface DeviceMacroInsertListener<T> {
      void handleInsert(T beanModel);
   }
}
