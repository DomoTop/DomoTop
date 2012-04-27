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
package org.openremote.modeler.client.widget.uidesigner;

import org.openremote.modeler.client.Constants;
import org.openremote.modeler.client.gxtextends.TreePanelDragSourceMacroDragExt;
import org.openremote.modeler.client.widget.AutoListenableTreePanelBuilder;
import org.openremote.modeler.domain.DeviceCommand;
import org.openremote.modeler.domain.DeviceMacro;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.event.DNDEvent;
import com.extjs.gxt.ui.client.event.DNDListener;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel;

/**
 * The Class DevicesAndMacrosPanel.
 */
public class DevicesAndMacrosPanel extends ContentPanel {

   /** The devicesAndMacrosTree. */
   private TreePanel<BeanModel> devicesAndMacrosTree = null;

   /** The devicesAndMacrosListContainer. */
   private LayoutContainer devicesAndMacrosTreeContainer = null;

   /**
    * Instantiates a devicesAndMacrosPanel.
    */
   public DevicesAndMacrosPanel() {
      setHeading("Devices/Macros");
      setLayout(new FitLayout());
      createDevicesAndMacrosTree();
   }

   /**
    * Creates the devicesAndMacrosTree.
    */
   private void createDevicesAndMacrosTree() {
      initDevicesAndMacrosTreeContainer();
      initDevicesAndMacrosTree();
   }

   /**
    * Inits the devicesAndMacrosTreeContainer.
    */
   private void initDevicesAndMacrosTreeContainer() {
      devicesAndMacrosTreeContainer = new LayoutContainer();
      devicesAndMacrosTreeContainer.setScrollMode(Scroll.AUTO);
      devicesAndMacrosTreeContainer.setStyleAttribute("backgroundColor", "white");
      devicesAndMacrosTreeContainer.setBorders(false);
      devicesAndMacrosTreeContainer.setLayoutOnChange(true);
      devicesAndMacrosTreeContainer.setHeight("100%");
      add(devicesAndMacrosTreeContainer);
   }

   /**
    * Inits the devicesAndMacrosTree.
    */
   private void initDevicesAndMacrosTree() {
      if (devicesAndMacrosTree == null) {
         devicesAndMacrosTree = AutoListenableTreePanelBuilder.buildDevicesAndMacrosTree();
         addTreeDragEventListener();
         devicesAndMacrosTreeContainer.add(devicesAndMacrosTree);
      }
   }

   /**
    * Adds the TreeDragEventListener.
    */
   private void addTreeDragEventListener() {
      TreePanelDragSourceMacroDragExt dragSource = new TreePanelDragSourceMacroDragExt(devicesAndMacrosTree);
      dragSource.addDNDListener(new DNDListener() {
         @SuppressWarnings("unchecked")
         @Override
         public void dragStart(DNDEvent e) {
            TreePanel<BeanModel> tree = (TreePanel<BeanModel>) e.getComponent();
            BeanModel beanModel = tree.getSelectionModel().getSelectedItem();
            e.setCancelled(true);
            if ((beanModel.getBean() instanceof DeviceCommand) || (beanModel.getBean() instanceof DeviceMacro)) {
               e.setCancelled(false);
            }
            super.dragStart(e);
         }

      });
      dragSource.setGroup(Constants.BUTTON_DND_GROUP);
   }
}
