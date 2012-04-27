/* OpenRemote, the Home of the Digital Home.
* Copyright 2008-2010, OpenRemote Inc.
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
package org.openremote.modeler.client.widget.buildingmodeler;

import java.util.List;

import org.openremote.modeler.client.event.DeviceWizardEvent;
import org.openremote.modeler.client.event.SubmitEvent;
import org.openremote.modeler.client.icon.Icons;
import org.openremote.modeler.client.listener.SubmitListener;
import org.openremote.modeler.client.model.ComboBoxDataModel;
import org.openremote.modeler.domain.CustomSensor;
import org.openremote.modeler.domain.Device;
import org.openremote.modeler.domain.DeviceCommand;
import org.openremote.modeler.domain.RangeSensor;
import org.openremote.modeler.domain.Sensor;
import org.openremote.modeler.domain.SensorCommandRef;
import org.openremote.modeler.domain.SensorType;
import org.openremote.modeler.domain.State;

import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.data.ModelIconProvider;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FormEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.AdapterField;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

public class SensorWizardWindow extends SensorWindow {

   private Device device;
   public SensorWizardWindow(Device device) {
      super(device);
      this.device = device;
      addNewCommandButton();
      form.removeAllListeners();
      addCommandSelectListeners();
      onSubmit();
   }
   
   @Override
   protected void buildCommandSelectTree(Device device) {
      TreeStore<BeanModel> commandTreeStore = new TreeStore<BeanModel>();
      for (DeviceCommand deviceCommand : device.getDeviceCommands()) {
         commandTreeStore.add(deviceCommand.getBeanModel(), false);
      }
      commandSelectTree = new TreePanel<BeanModel>(commandTreeStore);
      commandSelectTree.setBorders(false);
      commandSelectTree.setStateful(true);
      commandSelectTree.setDisplayProperty("displayName");
      commandSelectTree.setStyleAttribute("overflow", "auto");
      commandSelectTree.setHeight("100%");
      commandSelectTree.setIconProvider(new ModelIconProvider<BeanModel>() {
         public AbstractImagePrototype getIcon(BeanModel thisModel) {
            return ((Icons) GWT.create(Icons.class)).deviceCmd();
         }

      });
   }
   private void addNewCommandButton() {
      Button newCommandButton = new Button("New command..");
      newCommandButton.addSelectionListener(new NewCommandListener());
      AdapterField newCommandField = new AdapterField(newCommandButton);
      newCommandField.setLabelSeparator("");
      form.insert(newCommandField, 2);
      layout();
   }
   
   private void addCommandSelectListeners() {
   }
   private void onSubmit() {
      form.addListener(Events.BeforeSubmit, new Listener<FormEvent>() {
         @SuppressWarnings("unchecked")
         public void handleEvent(FormEvent be) {
               Sensor sensor = new Sensor();
               if (typeList.getValue() != null) {
                  SensorType type = ((ComboBoxDataModel<SensorType>) typeList.getValue()).getData();
                  if (type == SensorType.RANGE) {
                     sensor = new RangeSensor();
                     ((RangeSensor) sensor).setMin(Integer.valueOf(minField.getRawValue()));
                     ((RangeSensor) sensor).setMax(Integer.valueOf(maxField.getRawValue()));
                  } else if (type == SensorType.CUSTOM) {
                     sensor = new CustomSensor();
                     List<BeanModel> states = grid.getStore().getModels();
                     for (BeanModel stateModel : states) {
                        State state = stateModel.getBean();
                        state.setSensor((CustomSensor) sensor);
                        ((CustomSensor) sensor).addState(state);
                     }
                  }
                  sensor.setType(type);
               }else {
                  MessageBox.alert("Warn", "A sensor must have a type", null);
                  typeList.focus();
                  return;
               }
               sensor.setDevice(device);
               sensor.setName(nameField.getValue());
               sensor.setAccount(DeviceContentWizardForm.account);
               BeanModel selectedCommand = commandSelectTree.getSelectionModel().getSelectedItem();
               if (selectedCommand != null && selectedCommand.getBean() instanceof DeviceCommand) {
                  DeviceCommand cmd = selectedCommand.getBean();
                  SensorCommandRef sensorCommandRef = new SensorCommandRef();
                  sensorCommandRef.setDeviceCommand(cmd);
                  sensorCommandRef.setSensor(sensor);
                  sensor.setSensorCommandRef(sensorCommandRef);
               } else {
                  MessageBox.alert("Warn", "A sensor must have a device command", null);
                  commandSelectTree.focus();
                  return;
               }
               fireEvent(SubmitEvent.SUBMIT, new SubmitEvent(sensor));
//               SensorBeanModelProxy.saveSensor(sensor, new AsyncSuccessCallback<Sensor>() {
//                  public void onSuccess(Sensor result) {
//                     fireEvent(SubmitEvent.SUBMIT, new SubmitEvent(result));
//                  }
//
//               });
         }

      });
   }
   
   private final class NewCommandListener extends SelectionListener<ButtonEvent> {
      @Override
      public void componentSelected(ButtonEvent ce) {
         DeviceCommandWizardWindow deviceCommandWizardWindow = new DeviceCommandWizardWindow(device);
         deviceCommandWizardWindow.addListener(SubmitEvent.SUBMIT, new SubmitListener() {
            @Override
            public void afterSubmit(SubmitEvent be) {
               DeviceCommand deviceCommand = be.getData();
               device.getDeviceCommands().add(deviceCommand);
               BeanModel deviceCommandModel = deviceCommand.getBeanModel();
               commandSelectTree.getStore().add(deviceCommandModel, false);
               commandSelectTree.getSelectionModel().select(deviceCommandModel, false);
               fireEvent(DeviceWizardEvent.ADD_CONTENT, new DeviceWizardEvent(deviceCommandModel));
               Info.display("Info", "Create command " + deviceCommand.getName() + " success");
            }
         });
      }
   }
}
