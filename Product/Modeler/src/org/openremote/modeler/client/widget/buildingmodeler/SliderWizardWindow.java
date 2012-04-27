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
import org.openremote.modeler.client.listener.DeviceWizardListener;
import org.openremote.modeler.client.listener.SubmitListener;
import org.openremote.modeler.client.model.ComboBoxDataModel;
import org.openremote.modeler.client.utils.DeviceCommandWizardSelectWindow;
import org.openremote.modeler.domain.Device;
import org.openremote.modeler.domain.DeviceCommand;
import org.openremote.modeler.domain.DeviceCommandRef;
import org.openremote.modeler.domain.Sensor;
import org.openremote.modeler.domain.SliderCommandRef;
import org.openremote.modeler.domain.SliderSensorRef;

import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FormEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.AdapterField;
import com.extjs.gxt.ui.client.widget.form.Field;

public class SliderWizardWindow extends SliderWindow {

   private Device device;
   public SliderWizardWindow(Device device) {
      super(null, device);
      this.device = device;
      initSensorFiled();
      addNewSensorButton();
      addCommandSelectListener();
      form.removeAllListeners();
      onSubmit();
   }

   private void addNewSensorButton() {
      Button newSensorButton = new Button("New sensor..");
      newSensorButton.addSelectionListener(new NewSensorListener());
      AdapterField newSensorField = new AdapterField(newSensorButton);
      newSensorField.setLabelSeparator("");
      form.insert(newSensorField, 2);
      layout();
   }
   
   private void initSensorFiled() {
      ListStore<ModelData> sensorStore = sensorField.getStore();
      sensorStore.removeAll();
      for (Sensor sensor : device.getSensors()) {
         ComboBoxDataModel<Sensor> sensorRefSelector = new ComboBoxDataModel<Sensor>(sensor.getName(), sensor);
         sensorStore.add(sensorRefSelector);
      }
   }
   private void addCommandSelectListener() {
      setValueBtn.removeAllListeners();
      setValueBtn.addSelectionListener(new CommandSelectionListener());
   }
   private void onSubmit() {
      form.addListener(Events.BeforeSubmit, new Listener<FormEvent>() {
         public void handleEvent(FormEvent be) {
            if (slider.getSetValueCmd() == null) {
               MessageBox.alert("Slider", "The slider must have a command to control its value", null);
               return;
            }
            List<Field<?>> fields = form.getFields();
            for (Field<?> field : fields) {
               if (SLIDER_NAME_FIELD_NAME.equals(field.getName())) {
                  slider.setName(field.getValue().toString());
                  break;
               }
            }
            slider.setAccount(DeviceContentWizardForm.account);
            fireEvent(SubmitEvent.SUBMIT, new SubmitEvent(slider));
         }
         
      });
   }
   
   private final class NewSensorListener extends SelectionListener<ButtonEvent> {
      @Override
      public void componentSelected(ButtonEvent ce) {
         final SensorWizardWindow sensorWizardWindow = new SensorWizardWindow(device);
         sensorWizardWindow.addListener(SubmitEvent.SUBMIT, new SubmitListener() {
            @Override
            public void afterSubmit(SubmitEvent be) {
               Sensor sensor = be.getData();
               device.getSensors().add(sensor);
               SliderSensorRef sliderSensorRef = new SliderSensorRef(slider);
               sliderSensorRef.setSensor(sensor);
               slider.setSliderSensorRef(sliderSensorRef);
               ComboBoxDataModel<Sensor> sensorRefSelector = new ComboBoxDataModel<Sensor>(sensor.getName(), sensor);
               sensorField.getStore().add(sensorRefSelector);
               sensorField.setValue(sensorRefSelector);
               sensorWizardWindow.hide();
               fireEvent(DeviceWizardEvent.ADD_CONTENT, new DeviceWizardEvent(sensor.getBeanModel()));
            }
         });
         sensorWizardWindow.addListener(DeviceWizardEvent.ADD_CONTENT, new DeviceWizardListener() {
            @Override
            public void afterAdd(DeviceWizardEvent be) {
               fireEvent(DeviceWizardEvent.ADD_CONTENT, new DeviceWizardEvent(be.getData()));
            }
         });
      }
   }
   
   private final class CommandSelectionListener extends SelectionListener<ButtonEvent> {
      @Override
      public void componentSelected(ButtonEvent ce) {
         final DeviceCommandWizardSelectWindow selectCommandWindow = new DeviceCommandWizardSelectWindow(device);
         final Button command = ce.getButton();
         selectCommandWindow.addListener(SubmitEvent.SUBMIT, new SubmitListener() {
            @Override
            public void afterSubmit(SubmitEvent be) {
               BeanModel dataModel = be.<BeanModel> getData();
               DeviceCommandRef deviceCommandRef = null;
               if (dataModel.getBean() instanceof DeviceCommand) {
                  deviceCommandRef = new DeviceCommandRef((DeviceCommand) dataModel.getBean());
               } else {
                  MessageBox.alert("error", "A slider can only have command instead of macor", null);
                  return;
               }
               command.setText(deviceCommandRef.getDeviceCommand().getDisplayName());
               SliderCommandRef sliderCommandRef = new SliderCommandRef(slider);
               sliderCommandRef.setDeviceCommand(deviceCommandRef.getDeviceCommand());
               slider.setSetValueCmd(sliderCommandRef);
            }
         });
      }
   }
}
