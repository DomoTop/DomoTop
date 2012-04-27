package org.openremote.modeler.client.proxy;



import java.util.Set;

import org.openremote.modeler.client.rpc.AsyncServiceFactory;
import org.openremote.modeler.client.rpc.AsyncSuccessCallback;
import org.openremote.modeler.domain.ConfigCategory;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class ConfigCategoryBeanModelProxy {
   public static void getAllCategory(final AsyncCallback<Set<ConfigCategory>> callback){
      AsyncServiceFactory.getConfigCategoryRPCServiceAsync().getCategories(new AsyncSuccessCallback<Set<ConfigCategory>>(){
         @Override
         public void onSuccess(Set<ConfigCategory> result) {
            BeanModelDataBase.configCategoryTable.insertAll(ConfigCategory.createModels(result));
            callback.onSuccess(result);
         }
         
      });
   }
   
}
