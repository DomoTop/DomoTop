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

import org.openremote.modeler.client.event.SubmitEvent;
import org.openremote.modeler.client.widget.TreePanelBuilder;
import org.openremote.modeler.domain.Device;
import org.openremote.modeler.domain.DeviceCommand;
import org.openremote.modeler.domain.DeviceCommandRef;

import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.WindowEvent;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel;
/**
 * 
 * @author Javen
 *
 */
public class DeviceCommandSelectWindow extends Dialog{
   
   protected TreePanel<BeanModel> deviceCommandTree;
   
   public DeviceCommandSelectWindow(Device device){
      setHeading("Select Command");
      setMinHeight(260);
      setWidth(200);
      setLayout(new FitLayout());
      setModal(true);
      initCommandTree(device);
      setButtons(Dialog.OKCANCEL);
      setHideOnButtonClick(true);
      addButtonListener();
      show();
   }

   private void initCommandTree(Device device) {
      ContentPanel deviceCommandPanel = new ContentPanel();
      deviceCommandPanel.setBorders(false);
      deviceCommandPanel.setBodyBorder(false);
      deviceCommandPanel.setHeaderVisible(false);
      if (deviceCommandTree == null) {
         createCommandTree(device);
         deviceCommandPanel.add(deviceCommandTree);
      }
      // overflow-auto style is for IE hack.
      deviceCommandPanel.addStyleName("overflow-auto");
      add(deviceCommandPanel);
   }
   
   protected void createCommandTree(Device device) {
      deviceCommandTree = TreePanelBuilder.buildCommandTree(device, null);
   }
   
   private void addButtonListener() {
      addListener(Events.BeforeHide, new Listener<WindowEvent>() {
         public void handleEvent(WindowEvent be) {
            if (be.getButtonClicked() == getButtonById("ok")) {
               BeanModel beanModel = deviceCommandTree.getSelectionModel().getSelectedItem();
               if (beanModel == null) {
                  MessageBox.alert("Error", "Please select a command.", null);
                  be.cancelBubble();
               } else {
                  if ((beanModel.getBean() instanceof DeviceCommand) || (beanModel.getBean() instanceof DeviceCommandRef)) {
                     fireEvent(SubmitEvent.SUBMIT, new SubmitEvent(beanModel));
                  } else {
                     MessageBox.alert("Error", "Please select a command.", null);
                     be.cancelBubble();
                  }
               }
            }
         }
      }); 
   }
}
