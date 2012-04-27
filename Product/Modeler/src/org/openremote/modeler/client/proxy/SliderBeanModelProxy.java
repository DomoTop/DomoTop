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
import org.openremote.modeler.domain.Slider;

import com.extjs.gxt.ui.client.data.BeanModel;

public class SliderBeanModelProxy {
   private SliderBeanModelProxy() {
   }
   public static void loadAll(BeanModel sliderBean, final AsyncSuccessCallback<List<BeanModel>> callback) {
      if (sliderBean == null || sliderBean.getBean() instanceof TreeFolderBean) {
         AsyncServiceFactory.getSliderRPCServiceAsync().loadAll(new AsyncSuccessCallback<List<Slider>>() {
            @Override
            public void onSuccess(List<Slider> result) {
               List<BeanModel> sliderModels = Slider.createModels(result);
               BeanModelDataBase.sliderTable.insertAll(sliderModels);
               callback.onSuccess(sliderModels);
            }
         });
      } else {
         Slider slider = sliderBean.getBean();
         List<BeanModel> sliderBeanModels = new ArrayList<BeanModel>();
         sliderBeanModels.add(slider.getSetValueCmd().getBeanModel());
         callback.onSuccess(sliderBeanModels);
      }
   }
   
   public static void delete(final BeanModel beanModel, final AsyncSuccessCallback<Void> callback) {
      if (beanModel != null && beanModel.getBean() instanceof Slider) {
         AsyncServiceFactory.getSliderRPCServiceAsync().delete(((Slider) (beanModel.getBean())).getOid(),
               new AsyncSuccessCallback<Void>() {
                  @Override
                  public void onSuccess(Void result) {
                     BeanModelDataBase.sliderTable.delete(beanModel);
                     callback.onSuccess(result);
                  }
               });
      }
   }
   
   public static void save(final BeanModel beanModel,final AsyncSuccessCallback<Slider>callback){
      if (beanModel != null && beanModel.getBean() instanceof Slider) {
         AsyncServiceFactory.getSliderRPCServiceAsync().save((Slider) (beanModel.getBean()),
               new AsyncSuccessCallback<Slider>() {

                  @Override
                  public void onSuccess(Slider result) {
                     BeanModelDataBase.sliderTable.insert(beanModel);
                     callback.onSuccess(result);
                  }

               });
      }
   }
   
   public static void update(final BeanModel beanModel,final AsyncSuccessCallback<Slider>callback) {
      if (beanModel != null && beanModel.getBean() instanceof Slider) {
         AsyncServiceFactory.getSliderRPCServiceAsync().update((Slider) (beanModel.getBean()),
               new AsyncSuccessCallback<Slider>() {
                  @Override
                  public void onSuccess(Slider result) {
                     BeanModelDataBase.sliderTable.update(beanModel);
                     callback.onSuccess(result);
                  }

               });
      }
   }
}
