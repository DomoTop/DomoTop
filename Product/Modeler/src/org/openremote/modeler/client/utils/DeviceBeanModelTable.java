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
import org.openremote.modeler.client.proxy.DeviceBeanModelProxy;
import org.openremote.modeler.client.rpc.AsyncSuccessCallback;
import org.openremote.modeler.domain.BusinessEntity;
import org.openremote.modeler.domain.Device;
import org.openremote.modeler.domain.DeviceCommand;

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
public class DeviceBeanModelTable extends BeanModelTable {
   
   private List<DeviceInsertListener<BeanModel>> deviceInsertListener = new ArrayList<DeviceInsertListener<BeanModel>> ();
   
   public void addDeviceInsertListener(DeviceInsertListener<BeanModel> insertListener){
      deviceInsertListener.add(insertListener);
   }
   
   public void removeInsertListener(DeviceInsertListener<BeanModel> insertListener){
      deviceInsertListener.remove(insertListener);
   }
   
   

   public void insertAndNotifyDeviceInsertListener(BeanModel beanModel) {
      insert(beanModel);
      for(DeviceInsertListener<BeanModel> listener : deviceInsertListener) {
         listener.handleInsert(beanModel);
      }
   }
   /**
    * Instantiates a new bean model table.
    */
   public DeviceBeanModelTable() {
      super();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void loadFromTable(BeanModel parent, final AsyncSuccessCallback<List<BeanModel>> asyncSuccessCallback) {
      DeviceBeanModelProxy.loadDeviceAndCommand(parent, new AsyncSuccessCallback<List<BeanModel>>() {
         public void onSuccess(List<BeanModel> result) {
            asyncSuccessCallback.onSuccess(result);
         }            
      });
     
   }
   
   /**
    * {@inheritDoc}
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
                  long sourceParentBeanModelOid = -1L;
                  if (sourceBeanModel.getBean() instanceof Device) {
                     treeStore.add(parentBeanModel, sourceBeanModel, false);
                  } else if (sourceBeanModel.getBean() instanceof DeviceCommand) {
                     sourceParentBeanModelOid = ((DeviceCommand) sourceBeanModel.getBean()).getDevice().getOid();
                  }
                  if (((BusinessEntity) parentBeanModel.getBean()).getOid() == sourceParentBeanModelOid) {
                     treeStore.add(parentBeanModel, sourceBeanModel, false);
                  }
               }
            }
         }

      });
   }
   
   /**
    * {@inheritDoc}
    */
   @SuppressWarnings("unchecked")
   @Override
   public void addChangeListener(final AutoListenableTreeStore treeStore, TreeStoreEvent<BeanModel> be) {
      for (final BeanModel targetBeanModel : be.getChildren()) {
         ChangeListener changeListener =  getChangeListenerFromMap(targetBeanModel);
         if (changeListener == null) {
            changeListener = new ChangeListener() {
               public void modelChanged(ChangeEvent event) {
                  BeanModel sourceBeanModel = (BeanModel) event.getItem();
                  if (event.getType() == UPDATE) {
                     for (String propertyName : sourceBeanModel.getPropertyNames()) {
                        targetBeanModel.set(propertyName, sourceBeanModel.get(propertyName));
                     }
                     treeStore.update(targetBeanModel);
                  } else if (event.getType() == REMOVE) {
                     treeStore.remove(targetBeanModel);
                  }
               }
            };
            putChangeListenerIntoMap(targetBeanModel, changeListener);
            addChangeListener(BeanModelDataBase.getBeanModelId(targetBeanModel), changeListener);
         }
      }
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   protected void excuteNotify(ChangeEvent evt) {
      BeanModel beanModel = (BeanModel) evt.getItem();
      if (beanModel.getBean() instanceof Device) {
         if (insertListeners.get(Constants.DEVICES_OID) != null) {
            insertListeners.get(Constants.DEVICES_OID).modelChanged(evt);
         }
      } else if (beanModel.getBean() instanceof DeviceCommand) {
         DeviceCommand deviceCommand = (DeviceCommand) beanModel.getBean();
         ChangeListener changeListener = insertListeners.get(deviceCommand.getDevice().getOid());
         if (changeListener != null) {
            changeListener.modelChanged(evt);
         }
      }
   }
   
   public static interface DeviceInsertListener<T> {
      void handleInsert(T beanModel);
   }
}
