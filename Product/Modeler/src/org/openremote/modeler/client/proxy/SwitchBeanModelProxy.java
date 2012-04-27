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
import java.util.List;

import org.openremote.modeler.client.model.TreeFolderBean;
import org.openremote.modeler.client.rpc.AsyncServiceFactory;
import org.openremote.modeler.client.rpc.AsyncSuccessCallback;
import org.openremote.modeler.domain.Switch;

import com.extjs.gxt.ui.client.data.BeanModel;

public class SwitchBeanModelProxy {
   private SwitchBeanModelProxy() {
   }
   public static void loadAll(BeanModel switchBean, final AsyncSuccessCallback<List<BeanModel>> callback) {
      if (switchBean == null || switchBean.getBean() instanceof TreeFolderBean) {
         AsyncServiceFactory.getSwitchRPCServiceAsync().loadAll(new AsyncSuccessCallback<List<Switch>>() {
            @Override
            public void onSuccess(List<Switch> result) {
               List<BeanModel> switchBeanModels = Switch.createModels(result);
               BeanModelDataBase.switchTable.insertAll(switchBeanModels);
               callback.onSuccess(switchBeanModels);
            }
         });
      } else {
         Switch switchToggle = switchBean.getBean();
         List<BeanModel> commandBeanModels = new ArrayList<BeanModel>();
         commandBeanModels.add(switchToggle.getSwitchCommandOnRef().getBeanModel());
         commandBeanModels.add(switchToggle.getSwitchCommandOffRef().getBeanModel());

         callback.onSuccess(commandBeanModels);
      }
   }
   
   public static void delete(final BeanModel beanModel, final AsyncSuccessCallback<Void> callback) {
      if (beanModel != null && beanModel.getBean() instanceof Switch) {
         AsyncServiceFactory.getSwitchRPCServiceAsync().delete(((Switch) (beanModel.getBean())).getOid(),
               new AsyncSuccessCallback<Void>() {
                  @Override
                  public void onSuccess(Void result) {
                     BeanModelDataBase.switchTable.delete(beanModel);
                     callback.onSuccess(result);
                  }
               });
      }
   }
   
   public static void save(final BeanModel beanModel, final AsyncSuccessCallback<Switch> callback) {
      if (beanModel != null && beanModel.getBean() instanceof Switch) {
         AsyncServiceFactory.getSwitchRPCServiceAsync().save((Switch) (beanModel.getBean()),
               new AsyncSuccessCallback<Switch>() {

                  @Override
                  public void onSuccess(Switch result) {
                     BeanModelDataBase.switchTable.insert(beanModel);
                     callback.onSuccess(result);
                  }

               });
      }
   }
   
   public static void update(final BeanModel beanModel, final AsyncSuccessCallback<Switch> callback) {
      if (beanModel != null && beanModel.getBean() instanceof Switch) {
         AsyncServiceFactory.getSwitchRPCServiceAsync().update((Switch) (beanModel.getBean()),
               new AsyncSuccessCallback<Switch>() {
                  @Override
                  public void onSuccess(Switch result) {
                     BeanModelDataBase.switchTable.update(beanModel);
                     callback.onSuccess(result);
                  }

               });
      }
   }
}
