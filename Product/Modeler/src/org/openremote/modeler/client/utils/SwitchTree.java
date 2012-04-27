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

import java.util.List;

import org.openremote.modeler.client.icon.Icons;
import org.openremote.modeler.client.proxy.SwitchBeanModelProxy;
import org.openremote.modeler.client.rpc.AsyncSuccessCallback;
import org.openremote.modeler.domain.CommandRefItem;
import org.openremote.modeler.domain.Switch;

import com.extjs.gxt.ui.client.data.BaseTreeLoader;
import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.data.ModelIconProvider;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

public class SwitchTree {

   private static final Icons ICON = GWT.create(Icons.class);
   
   private static TreePanel<BeanModel> switchTree = null;
   
   private static TreeStore<BeanModel> switchTreeStore = null;
   private SwitchTree() {
   }
   
   /**
    * Gets the single instance of SwitchTree.
    * For show the tree quickly. Common used in select switch window.
    */
   public static TreePanel<BeanModel> getInstance() {
      if (switchTree == null) {
         switchTree = buildSwitchTree();
      }
      return switchTree;
   }
   
   /**
    * Builds the switch tree.
    * The tree is new, and the store is the same.
    */
   public static TreePanel<BeanModel> buildSwitchTree() {
      if (switchTreeStore == null) {
         RpcProxy<List<BeanModel>> loadSwitchRPCProxy = new RpcProxy<List<BeanModel>>() {

            protected void load(Object o, final AsyncCallback<List<BeanModel>> listAsyncCallback) {
               SwitchBeanModelProxy.loadAll((BeanModel) o, new AsyncSuccessCallback<List<BeanModel>>() {

                  public void onSuccess(List<BeanModel> result) {
                     listAsyncCallback.onSuccess(result);
                  }
               });
            }
         };
         BaseTreeLoader<BeanModel> loadSensorTreeLoader = new BaseTreeLoader<BeanModel>(loadSwitchRPCProxy) {
            @Override
            public boolean hasChildren(BeanModel beanModel) {
               if (beanModel.getBean() instanceof Switch) {
                  return true;
               }
               return false;
            }
         };
         switchTreeStore = new TreeStore<BeanModel>(loadSensorTreeLoader);
      }

      final TreePanel<BeanModel> tree = new TreePanel<BeanModel>(switchTreeStore);
      tree.setStateful(true);
      tree.setBorders(false);
      tree.setHeight("100%");
      tree.setDisplayProperty("displayName");
      
      tree.setIconProvider(new ModelIconProvider<BeanModel>() {
         public AbstractImagePrototype getIcon(BeanModel thisModel) {
            if (thisModel.getBean() instanceof Switch) {
               return ICON.switchIcon();
            } else if (thisModel.getBean() instanceof CommandRefItem) {
               return ICON.deviceCmd();
            } else {
               return ICON.switchIcon();
            }
         }
      });
      return tree;
   }

}
