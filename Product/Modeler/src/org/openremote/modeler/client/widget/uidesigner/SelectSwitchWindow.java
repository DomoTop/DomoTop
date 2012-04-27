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

import org.openremote.modeler.client.event.SubmitEvent;
import org.openremote.modeler.client.proxy.BeanModelDataBase;
import org.openremote.modeler.domain.Switch;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.WindowEvent;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.ListView;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;

public class SelectSwitchWindow extends Dialog {

   private ListView<BeanModel> switchList = new ListView<BeanModel>();
   public SelectSwitchWindow() {
      setHeading("Select Switch");
      setMinHeight(320);
      setWidth(240);
      setLayout(new RowLayout(Orientation.VERTICAL));
      setModal(true);
      initSwitchList();
      initSwitchInfo();
      setButtons(Dialog.OKCANCEL);
      setHideOnButtonClick(true);
      addButtonListener();
      show();
   }

   private void initSwitchList() {
      ContentPanel switchListContainer = new ContentPanel();
      switchListContainer.setSize(240, 150);
      switchListContainer.setBorders(false);
      switchListContainer.setBodyBorder(false);
      switchListContainer.setHeaderVisible(false);
      // overflow-auto style is for IE hack.
      switchListContainer.addStyleName("overflow-auto");
      
      ListStore<BeanModel> store = new ListStore<BeanModel>();
      store.add(BeanModelDataBase.switchTable.loadAll());
      switchList.setStore(store);
      switchList.setDisplayProperty("displayName");
      switchList.setStyleAttribute("overflow", "auto");
      switchList.setBorders(false);
      switchList.setHeight(150);
      switchListContainer.add(switchList);
      add(switchListContainer, new RowData(1, -1, new Margins(4)));
   }
   
   private void initSwitchInfo() {
      final Html switchInfoHtml = new Html("<p><b>Switch info</b></p>"); 
      switchList.getSelectionModel().addSelectionChangedListener(new SelectionChangedListener<BeanModel>() {
         @Override
         public void selectionChanged(SelectionChangedEvent<BeanModel> se) {
            BeanModel selectedSwitchModel = se.getSelectedItem();
            if (selectedSwitchModel != null) {
               Switch switchToggle = selectedSwitchModel.getBean();
               String switchInfo = "<p><b>Switch info</b></p>";
               if (switchToggle.getSwitchCommandOnRef() != null){
                  switchInfo = switchInfo + "<p>On: " + switchToggle.getSwitchCommandOnRef().getDisplayName() + "</p>";
               }
               if (switchToggle.getSwitchCommandOffRef() != null) {
                  switchInfo = switchInfo + "<p>Off: " + switchToggle.getSwitchCommandOffRef().getDisplayName() + "</p>";
               }
               if (switchToggle.getSwitchSensorRef() != null) {
                  switchInfo = switchInfo + "<p>Sensor: " + switchToggle.getSwitchSensorRef().getDisplayName() + "</p>";
               }
               switchInfoHtml.setHtml(switchInfo);
            }
         }
      });
      add(switchInfoHtml, new RowData(1, -1, new Margins(4)));
   }
   private void addButtonListener() {
      addListener(Events.BeforeHide, new Listener<WindowEvent>() {
         public void handleEvent(WindowEvent be) {
            if (be.getButtonClicked() == getButtonById("ok")) {
               BeanModel beanModel = switchList.getSelectionModel().getSelectedItem();
               if (beanModel == null) {
                  MessageBox.alert("Error", "Please select a switch.", null);
                  be.cancelBubble();
               } else {
                  if (beanModel.getBean() instanceof Switch) {
                     fireEvent(SubmitEvent.SUBMIT, new SubmitEvent(beanModel));
                  } else {
                     MessageBox.alert("Error", "Please select a switch.", null);
                     be.cancelBubble();
                  }
               }
            }
         }
      }); 
   }


}
