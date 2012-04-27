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
package org.openremote.modeler.client.widget.buildingmodeler;

import java.util.List;

import org.openremote.modeler.client.event.SubmitEvent;
import org.openremote.modeler.client.listener.FormResetListener;
import org.openremote.modeler.client.listener.FormSubmitListener;
import org.openremote.modeler.client.listener.SubmitListener;
import org.openremote.modeler.client.model.ComboBoxDataModel;
import org.openremote.modeler.client.proxy.BeanModelDataBase;
import org.openremote.modeler.client.proxy.SliderBeanModelProxy;
import org.openremote.modeler.client.rpc.AsyncSuccessCallback;
import org.openremote.modeler.client.utils.DeviceCommandSelectWindow;
import org.openremote.modeler.client.widget.FormWindow;
import org.openremote.modeler.client.widget.ComboBoxExt;
import org.openremote.modeler.domain.Device;
import org.openremote.modeler.domain.DeviceCommand;
import org.openremote.modeler.domain.DeviceCommandRef;
import org.openremote.modeler.domain.Sensor;
import org.openremote.modeler.domain.Slider;
import org.openremote.modeler.domain.SliderCommandRef;
import org.openremote.modeler.domain.SliderSensorRef;

import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FormEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.AdapterField;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
/**
 * 
 * @author Javen
 *
 */
public class SliderWindow extends FormWindow {
   public static final String SLIDER_NAME_FIELD_NAME = "name";
   public static final String SLIDER_SENSOR_FIELD_NAME = "sensor";
   public static final String SLIDER_SETVALUE_COMMMAND_FIELD_NAME="setValue";
   
   protected Slider slider = null;
   
   private TextField<String> nameField = new TextField<String>();
   protected ComboBox<ModelData> sensorField = new ComboBoxExt();
   protected Button setValueBtn = new Button("select");
   
   private boolean edit = false;
   
   public SliderWindow(Slider slider) {
      super();
      if (null != slider) {
         this.slider = slider;
         edit = true;
      } else {
         this.slider = new Slider();
         edit = false;
      }
      this.setHeading(edit ? "Edit Slider" : "New Slider");
      this.setSize(320, 240);
      
      createField();
      show();
   }
   
   public SliderWindow(Slider slider,Device device){
      super();
      if (null != slider) {
         this.slider = slider;
         edit = true;
      } else {
         this.slider = new Slider();
         edit = false;
      }
      if(device==null){
         throw new NullPointerException("A slider must belong to a device!");
      }
      this.slider.setDevice(device);
      this.setHeading(edit ? "Edit Slider" : "New Slider");
      this.setSize(320, 240);
      
      createField();
      show();
   }
   
   private void createField() {
      setWidth(380);
      setAutoHeight(true);
      setLayout(new FlowLayout());
      
      form.setWidth(370);
      
      nameField.setFieldLabel(SLIDER_NAME_FIELD_NAME);
      nameField.setName(SLIDER_NAME_FIELD_NAME);
      nameField.setAllowBlank(false);
      
      sensorField.setFieldLabel(SLIDER_SENSOR_FIELD_NAME);
      sensorField.setName(SLIDER_SENSOR_FIELD_NAME);
      
      ListStore<ModelData> sensorStore = new ListStore<ModelData>();
      List<BeanModel> sensors = BeanModelDataBase.sensorTable.loadAll();
      for (BeanModel sensorBean : sensors) {
         Sensor sensor = sensorBean.getBean();
         if (sensor.getDevice().equals(slider.getDevice())) {
            ComboBoxDataModel<Sensor> sensorRefSelector = new ComboBoxDataModel<Sensor>(sensor.getName(), sensor);
            sensorStore.add(sensorRefSelector);
         }
      }
      sensorField.setStore(sensorStore);
      sensorField.addSelectionChangedListener(new SensorSelectChangeListener());
      
      if (edit) {
         nameField.setValue(slider.getName());
         if (slider.getSliderSensorRef() != null) {
            sensorField.setValue(new ComboBoxDataModel<Sensor>(
                  slider.getSliderSensorRef().getSensor().getDisplayName(), slider.getSliderSensorRef().getSensor()));
         }
         setValueBtn.setText(slider.getSetValueCmd().getDisplayName());
         
//         sensorField.setEnabled(false);
//         setValueBtn.setEnabled(false);
      }
      
      AdapterField switchOnAdapter = new AdapterField(setValueBtn);
      switchOnAdapter.setFieldLabel(SLIDER_SETVALUE_COMMMAND_FIELD_NAME);
      
      Button submitBtn = new Button("Submit");
      Button resetButton = new Button("Reset");
      
      submitBtn.addSelectionListener(new FormSubmitListener(form));
      resetButton.addSelectionListener(new FormResetListener(form));
      
      form.add(nameField);
      form.add(sensorField);
      form.add(switchOnAdapter);
      
      form.addButton(submitBtn);
      form.addButton(resetButton);
      
      
      setValueBtn.addSelectionListener(new CommandSelectListener());
      
      form.addListener(Events.BeforeSubmit, new SliderSubmitListener());
      add(form);
   }
   
   
   class SliderSubmitListener implements Listener<FormEvent> {

      @Override
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
         if (!edit) {
            SliderBeanModelProxy.save(slider.getBeanModel(),new AsyncSuccessCallback<Slider>(){
               @Override
               public void onSuccess(Slider result) {
                  slider = result;
                  fireEvent(SubmitEvent.SUBMIT, new SubmitEvent(slider.getBeanModel()));
               }
            });
         } else {
            SliderCommandRef cmdRef = new SliderCommandRef(slider);
            cmdRef.setDeviceCommand(slider.getSetValueCmd().getDeviceCommand());
            cmdRef.setDeviceName(slider.getDevice().getName());
            slider.setSetValueCmd(cmdRef);
            SliderSensorRef sensorRef = new SliderSensorRef(slider);
            sensorRef.setSensor(slider.getSliderSensorRef().getSensor());
            
            slider.setSliderSensorRef(sensorRef);
            SliderBeanModelProxy.update(slider.getBeanModel(),new AsyncSuccessCallback<Slider>(){
               @Override
               public void onSuccess(Slider result) {
                  slider = result;
                  fireEvent(SubmitEvent.SUBMIT, new SubmitEvent(slider.getBeanModel()));
               }
            });
         }
      }
   }
   
   
   class CommandSelectListener extends SelectionListener<ButtonEvent> {
      @Override
      public void componentSelected(ButtonEvent ce) {
         final DeviceCommandSelectWindow selectCommandWindow = new DeviceCommandSelectWindow(SliderWindow.this.slider.getDevice());
         final Button command = ce.getButton();
         selectCommandWindow.addListener(SubmitEvent.SUBMIT, new SubmitListener() {
            @Override
            public void afterSubmit(SubmitEvent be) {
               BeanModel dataModel = be.<BeanModel> getData();
               DeviceCommandRef deviceCommandRef = null;
               if (dataModel.getBean() instanceof DeviceCommand) {
                  deviceCommandRef = new DeviceCommandRef((DeviceCommand) dataModel.getBean());
               } else if (dataModel.getBean() instanceof DeviceCommandRef) {
                  DeviceCommandRef ref = (DeviceCommandRef) dataModel.getBean();
                  deviceCommandRef = new DeviceCommandRef(ref.getDeviceCommand());
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
   
   class SensorSelectChangeListener extends SelectionChangedListener<ModelData> {

      @SuppressWarnings("unchecked")
      @Override
      public void selectionChanged(SelectionChangedEvent<ModelData> se) {
          ComboBoxDataModel<Sensor> sensorItem = (ComboBoxDataModel<Sensor>) se.getSelectedItem();
         SliderSensorRef sensorRef = new SliderSensorRef(slider);
         sensorRef.setSensor(sensorItem.getData());
         slider.setSliderSensorRef(sensorRef);
      }
   }
}
