package org.openremote.modeler.client.proxy;



import java.util.Set;

import org.openremote.modeler.client.rpc.AsyncServiceFactory;
import org.openremote.modeler.client.rpc.AsyncSuccessCallback;
import org.openremote.modeler.domain.ConfigCategory;
import org.openremote.modeler.domain.ControllerConfig;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class ControllerConfigBeanModelProxy {
   
   public static void getConfigs(final ConfigCategory category, final AsyncCallback<Set<ControllerConfig>> callback) {
      AsyncServiceFactory.getControllerConfigPRCServiceAsync().getConfigsByCategoryForCurrentAccount(
            category.getName(), new AsyncSuccessCallback<Set<ControllerConfig>>() {

               @Override
               public void onSuccess(Set<ControllerConfig> result) {
                  callback.onSuccess(result);
               }

            });
   }
   
   public static void saveAllConfigs(final Set<ControllerConfig> configs,
         final AsyncCallback<Set<ControllerConfig>> callback) {
      AsyncServiceFactory.getControllerConfigPRCServiceAsync().saveAll(configs,
            new AsyncSuccessCallback<Set<ControllerConfig>>() {

               @Override
               public void onSuccess(Set<ControllerConfig> result) {
                  callback.onSuccess(result);
               }

               @Override
               public void onFailure(Throwable caught) {
                  callback.onFailure(caught);
               }

            });
   }
   
   public static void listAllMissingConfigs(final String categoryName,
         final AsyncCallback<Set<ControllerConfig>> callback) {
      AsyncServiceFactory.getControllerConfigPRCServiceAsync().listAllMissedConfigsByCategoryName(categoryName,
            new AsyncCallback<Set<ControllerConfig>>() {

               @Override
               public void onFailure(Throwable caught) {
                  callback.onFailure(caught);
               }

               @Override
               public void onSuccess(Set<ControllerConfig> result) {
                  callback.onSuccess(result);
               }

            });
   }
}
