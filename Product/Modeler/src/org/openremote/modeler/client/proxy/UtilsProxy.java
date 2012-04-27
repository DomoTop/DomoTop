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

import java.util.Collection;
import java.util.List;

import org.openremote.modeler.client.model.AutoSaveResponse;
import org.openremote.modeler.client.rpc.AsyncServiceFactory;
import org.openremote.modeler.client.rpc.AsyncSuccessCallback;
import org.openremote.modeler.client.utils.PanelsAndMaxOid;
import org.openremote.modeler.domain.Group;
import org.openremote.modeler.domain.Panel;
import org.openremote.modeler.domain.Screen;
import org.openremote.modeler.domain.component.UISlider;

import com.extjs.gxt.ui.client.data.BeanModel;
import com.google.gwt.user.client.rpc.AsyncCallback;


/**
 * The Class DeviceBeanModelProxy.
 * 
 * @author handy.wang
 */
public class UtilsProxy {
   
   /**
    * Not be instantiated.
    */
   private UtilsProxy() {
   }

   /**
    * Load device.
    * 
    * @param callback the callback
    * @param maxId the max id
    * @param activityList the activity list
    */
   public static void exportFiles(long maxId, List<Panel> panelList, final AsyncSuccessCallback<String> callback) {
      AsyncServiceFactory.getUtilsRPCServiceAsync().exportFiles(maxId, panelList, new AsyncSuccessCallback<String>() {
         @Override
         public void onSuccess(String exportURL) {
            callback.onSuccess(exportURL);
         }
      });
   
   }

   /**
    * Auto save ui designer layout json.
    * 
    * @param groups the activities
    * @param callback the callback
    */
   public static void autoSaveUiDesignerLayout(Collection<Panel> panels, long maxID, final AsyncSuccessCallback<AutoSaveResponse> callback) {
      AsyncServiceFactory.getUtilsRPCServiceAsync().autoSaveUiDesignerLayout(panels,  maxID, new AsyncSuccessCallback<AutoSaveResponse>() {
         @Override
         public void onSuccess(AutoSaveResponse result) {
            callback.onSuccess(result);
         }

         @Override
         public void onFailure(Throwable caught) {
            callback.onFailure(caught);
         }
         
      });
   }
   
   public static void saveUiDesignerLayout(Collection<Panel> panels, long maxID, final AsyncSuccessCallback<AutoSaveResponse> callback) {
      AsyncServiceFactory.getUtilsRPCServiceAsync().saveUiDesignerLayout(panels,  maxID, new AsyncSuccessCallback<AutoSaveResponse>() {
         @Override
         public void onSuccess(AutoSaveResponse result) {
            callback.onSuccess(result);
         }

         @Override
         public void onFailure(Throwable caught) {
            callback.onFailure(caught);
         }
         
      });
   }
   public static void restore(final AsyncCallback<PanelsAndMaxOid> callback) {
      AsyncServiceFactory.getUtilsRPCServiceAsync().restore(new AsyncSuccessCallback<PanelsAndMaxOid>() {
         @Override
         public void onSuccess(PanelsAndMaxOid result) {
            callback.onSuccess(result);
         }

         @Override
         public void onFailure(Throwable caught) {
            callback.onFailure(caught);
         }
         
      });
   }
   
   public static void canRestore(final AsyncCallback<Boolean> callback) {
      AsyncServiceFactory.getUtilsRPCServiceAsync().canRestore(new AsyncSuccessCallback<Boolean>() {
         @Override
         public void onSuccess(Boolean result) {
            callback.onSuccess(result);
         }

         @Override
         public void onFailure(Throwable caught) {
            callback.onFailure(caught);
         }
         
      });
   }
   
   /**
    * Gets the beehive rest icon url.
    * 
    * @param callback the callback
    * 
    */
   public static void getBeehiveRestIconUrl(final AsyncSuccessCallback<String> callback) {
      AsyncServiceFactory.getUtilsRPCServiceAsync().beehiveRestIconUrl(new AsyncSuccessCallback<String>() {
         @Override
         public void onSuccess(String result) {
            callback.onSuccess(result);
         }
         
      });
   }
   
   public static void loadPanelsFromSession(final AsyncSuccessCallback<Collection<Panel>> callback) {
      AsyncServiceFactory.getUtilsRPCServiceAsync().loadPanelsFromSession(new AsyncSuccessCallback<Collection<Panel>>() {
         @Override
         public void onSuccess(Collection<Panel> panels) {
            callback.onSuccess(panels);
         }

         @Override
         public void onFailure(Throwable caught) {
            callback.onFailure(caught);
         }
         
      });
   }
   
   public static void loadGroupsFromSession(final AsyncSuccessCallback<List<Group>> callback) {
      AsyncServiceFactory.getUtilsRPCServiceAsync().loadGroupsFromSession(new AsyncSuccessCallback<List<Group>>() {
         @Override
         public void onSuccess(List<Group> groups) {
            callback.onSuccess(groups);
         }
      });
   }
   
   public static void loadScreensFromSession(final AsyncSuccessCallback<List<Screen>> callback) {
      AsyncServiceFactory.getUtilsRPCServiceAsync().loadScreensFromSession(new AsyncSuccessCallback<List<Screen>>() {
         @Override
         public void onSuccess(List<Screen> screens) {
            callback.onSuccess(screens);
         }
      });
   }
   
   /**
    * Load layout component's max id from session.
    * 
    */
   public static void loadMaxID(final AsyncSuccessCallback<Long> callback) {
      AsyncServiceFactory.getUtilsRPCServiceAsync().loadMaxID(new AsyncSuccessCallback<Long>() {
         @Override
         public void onSuccess(Long maxID) {
            callback.onSuccess(maxID);
         }
      });
   }
   
   public static void downLoadImage(String url, final AsyncCallback<String> callback) {
      AsyncServiceFactory.getUtilsRPCServiceAsync().downLoadImage(url, new AsyncSuccessCallback<String>() {
         @Override
         public void onSuccess(String result) {
            callback.onSuccess(result);
         }
      });
   }
   
   public static void getTemplatesListRestUrl(final AsyncCallback <String> callback){
      AsyncServiceFactory.getConfigurationRPCServiceAsync().getTemplatesListRestUrl(new AsyncSuccessCallback<String>(){

         @Override
         public void onSuccess(String result) {
           callback.onSuccess(result);
         }
         
      });
   }
   
   public static void getAllPublicTemplateRestURL(final AsyncCallback <String> callback){
      AsyncServiceFactory.getConfigurationRPCServiceAsync().getAllPublicTemplateRestUrl(new AsyncSuccessCallback<String>(){

         @Override
         public void onSuccess(String result) {
           callback.onSuccess(result);
         }

         @Override
         public void onFailure(Throwable caught) {
            callback.onFailure(caught);
         }
         
      });
   }
   
   public static void roteImages(final UISlider uiSlider, final AsyncCallback <UISlider> callback) {
      AsyncServiceFactory.getUtilsRPCServiceAsync().rotateImage(uiSlider, new AsyncCallback<UISlider>(){

         @Override
         public void onFailure(Throwable caught) {
            callback.onFailure(caught);
         }

         @Override
         public void onSuccess(UISlider result) {
            callback.onSuccess(result);
         }
         
      });
   }

   public static void getAccountRelativePath(final AsyncCallback <String> callback) {
      AsyncServiceFactory.getUtilsRPCServiceAsync().getAccountPath(new AsyncCallback <String>() {
         public void onFailure(Throwable caught) {
            callback.onFailure(caught);
         }

         public void onSuccess(String result) {
            callback.onSuccess(result);
         }
         
      });
   }
   
   public static void getOnTestLineURL(final AsyncCallback<String> callback) {
      AsyncServiceFactory.getUtilsRPCServiceAsync().getOnLineTestURL(new AsyncCallback <String>() {
         public void onFailure(Throwable caught) {
            callback.onFailure(caught);
         }

         public void onSuccess(String result) {
            callback.onSuccess(result);
         }
         
      });
   }
   
   public static boolean isPanelNameAvailable(String panelName) {
      List<BeanModel> panelModels = BeanModelDataBase.panelTable.loadAll();
      for (BeanModel panelModel : panelModels) {
         if (panelName.equals(panelModel.get("name").toString())) {
            return false;
         }
      }
      return true;
   }
}
