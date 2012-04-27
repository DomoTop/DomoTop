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

package org.openremote.modeler.client.proxy;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openremote.modeler.client.model.TreeFolderBean;
import org.openremote.modeler.client.rpc.AsyncServiceFactory;
import org.openremote.modeler.client.rpc.AsyncSuccessCallback;
import org.openremote.modeler.domain.DeviceMacro;
import org.openremote.modeler.domain.DeviceMacroItem;

import com.extjs.gxt.ui.client.data.BeanModel;


/**
 * The Class DeviceMacroBeanModelProxy.
 */
public class DeviceMacroBeanModelProxy {

   /**
    * Not be instantiated.
    */
   private DeviceMacroBeanModelProxy() {
   }

   /**
    * Load device maro.
    * 
    * @param deviceMacroBeanModel the device macro bean model
    * @param callback the callback
    */
   public static void loadDeviceMaro(BeanModel deviceMacroBeanModel,
         final AsyncSuccessCallback<List<BeanModel>> callback) {
      if (deviceMacroBeanModel == null || deviceMacroBeanModel.getBean() instanceof TreeFolderBean) {
         AsyncServiceFactory.getDeviceMacroServiceAsync().loadAll(new AsyncSuccessCallback<List<DeviceMacro>>() {
            public void onSuccess(List<DeviceMacro> result) {
               List<BeanModel> beanModels = DeviceMacro.createModels(result);
               BeanModelDataBase.deviceMacroTable.insertAll(beanModels);
               callback.onSuccess(beanModels);
            }
         });
      } else {
         AsyncServiceFactory.getDeviceMacroServiceAsync().loadDeviceMacroItems(
               (DeviceMacro) deviceMacroBeanModel.getBean(), new AsyncSuccessCallback<List<DeviceMacroItem>>() {
                  public void onSuccess(List<DeviceMacroItem> result) {
                     List<BeanModel> beanModels = DeviceMacroItem.createModels(result);
                     BeanModelDataBase.deviceMacroItemTable.insertAll(beanModels);
                     callback.onSuccess(beanModels);
                  }
               });
      }

   }

   /**
    * Save device macro.
    * 
    * @param deviceMacroName the device macro name
    * @param beanModels the beanModels
    * @param callback the callback
    */
   public static void saveDeviceMacro(String deviceMacroName, List<BeanModel> beanModels,
         final AsyncSuccessCallback<DeviceMacro> callback) {
      DeviceMacro deviceMacro = new DeviceMacro();
      deviceMacro.setName(deviceMacroName);
      deviceMacro.getDeviceMacroItems().addAll(getMacroItems(beanModels, deviceMacro));

      AsyncServiceFactory.getDeviceMacroServiceAsync().saveDeviceMacro(deviceMacro,
            new AsyncSuccessCallback<DeviceMacro>() {
               @Override
               public void onSuccess(DeviceMacro result) {
                  BeanModelDataBase.deviceMacroTable.insert(result.getBeanModel());
                  BeanModelDataBase.deviceMacroItemTable.insertAll(DeviceMacroItem.createModels(result
                        .getDeviceMacroItems()));
                  callback.onSuccess(result);
               }
            });
   }

   /**
    * Convert macro item.
    * 
    * @param beanModels the bean models
    * @param deviceMacro the device macro
    * 
    * @return the list< device macro item>
    */
   public static List<DeviceMacroItem> getMacroItems(List<BeanModel> beanModels, DeviceMacro deviceMacro) {
      List<DeviceMacroItem> deviceMacroItems = new ArrayList<DeviceMacroItem>();
      for (BeanModel beanModel : beanModels) {
         DeviceMacroItem macroItem = beanModel.getBean();
         macroItem.setParentDeviceMacro(deviceMacro);
         deviceMacroItems.add(macroItem);
      }
      return deviceMacroItems;
   }

   /**
    * Update device macro.
    * 
    * @param items the items
    * @param callback the callback
    * @param deviceMacroBeanModel the device macro bean model
    */
   public static void updateDeviceMacro(final BeanModel deviceMacroBeanModel, List<BeanModel> items,
         final AsyncSuccessCallback<DeviceMacro> callback) {
      DeviceMacro deviceMacro = deviceMacroBeanModel.getBean(); 
      Iterator<DeviceMacroItem> macroItemIterator = deviceMacro.getDeviceMacroItems().iterator();
      while (macroItemIterator.hasNext()) {
         DeviceMacroItem deviceMacroItem = macroItemIterator.next();
         BeanModelDataBase.deviceMacroItemTable.delete(deviceMacroItem.getOid());
         macroItemIterator.remove();
      }
      deviceMacro.getDeviceMacroItems().addAll(getMacroItems(items, deviceMacro));
      AsyncServiceFactory.getDeviceMacroServiceAsync().updateDeviceMacro(deviceMacro,
            new AsyncSuccessCallback<DeviceMacro>() {
               @Override
               public void onSuccess(DeviceMacro result) {
                  for (DeviceMacroItem deviceMacroItem : result.getDeviceMacroItems()) {
                     BeanModelDataBase.deviceMacroItemTable.insert(deviceMacroItem.getBeanModel());
                  }
                  BeanModelDataBase.deviceMacroTable.update(result.getBeanModel());
                  callback.onSuccess(result);
               }
            });
   }

   /**
    * Delete device macro.
    * 
    * @param deviceMacroBeanModel the device macro bean model
    * @param callback the callback
    */
   public static void deleteDeviceMacro(final BeanModel deviceMacroBeanModel, final AsyncSuccessCallback<Void> callback) {
      DeviceMacro deviceMacro = deviceMacroBeanModel.getBean();
      AsyncServiceFactory.getDeviceMacroServiceAsync().deleteDeviceMacro(deviceMacro.getOid(),
            new AsyncSuccessCallback<Void>() {
               @Override
               public void onSuccess(Void result) {
                  BeanModelDataBase.deviceMacroTable.delete(deviceMacroBeanModel);
                  callback.onSuccess(result);
               }
            });
   }
}
