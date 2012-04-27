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
package org.openremote.modeler.client.widget.propertyform;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openremote.modeler.client.event.SubmitEvent;
import org.openremote.modeler.client.listener.SubmitListener;
import org.openremote.modeler.client.model.ComboBoxDataModel;
import org.openremote.modeler.client.utils.ImageSourceValidator;
import org.openremote.modeler.client.utils.SensorLink;
import org.openremote.modeler.client.utils.WidgetSelectionUtil;
import org.openremote.modeler.client.widget.ComboBoxExt;
import org.openremote.modeler.client.widget.component.ImageUploadAdapterField;
import org.openremote.modeler.client.widget.component.ScreenImage;
import org.openremote.modeler.client.widget.uidesigner.PropertyPanel;
import org.openremote.modeler.client.widget.uidesigner.SelectSensorWindow;
import org.openremote.modeler.domain.CustomSensor;
import org.openremote.modeler.domain.Sensor;
import org.openremote.modeler.domain.SensorType;
import org.openremote.modeler.domain.State;
import org.openremote.modeler.domain.component.ImageSource;
import org.openremote.modeler.domain.component.UIImage;
import org.openremote.modeler.domain.component.UILabel;

import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.FormEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.AdapterField;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;

/**
 * A panel for display screen Image properties.
 */
public class ImagePropertyForm extends PropertyForm {
   private ScreenImage screenImage = null;
   private transient Operation operation=Operation.UPLOAD_IMAGE;
   private FieldSet statesPanel; 
   
   private State customSensorState = null;
   
   public ImagePropertyForm(ScreenImage screenImage) {
      super(screenImage);
      this.screenImage = screenImage;
      addFields(screenImage);
      addListenersToForm();
      createSensorStates();
      super.addDeleteButton();
   }
   private void addFields(final ScreenImage screenImage) {
      this.setLabelWidth(70);
      final UIImage uiImage = screenImage.getUiImage();
      
      final Button sensorSelectBtn = new Button("Select");
      if(screenImage.getUiImage().getSensor()!=null){
         sensorSelectBtn.setText(screenImage.getUiImage().getSensor().getDisplayName());
      }
      sensorSelectBtn.addSelectionListener(new SelectionListener<ButtonEvent>() {
         @Override
         public void componentSelected(ButtonEvent ce) {
            SelectSensorWindow selectSensorWindow = new SelectSensorWindow();
            selectSensorWindow.addListener(SubmitEvent.SUBMIT, new SubmitListener() {
               @Override
               public void afterSubmit(SubmitEvent be) {
                  BeanModel dataModel = be.<BeanModel> getData();
                  Sensor sensor = dataModel.getBean();
                  uiImage.setSensorAndInitSensorLink(sensor);
                  sensorSelectBtn.setText(sensor.getDisplayName());
                  if (sensor.getType() == SensorType.SWITCH || sensor.getType()==SensorType.CUSTOM) {
                     statesPanel.show();
                     createSensorStates();
                  } else {
                     statesPanel.hide();
                  }
                  screenImage.clearSensorStates();
               }
            });
         }
      });
      
     
      ComboBox<ModelData> labelBox = createLabelSelector();
      
      add(createImageUploader());
      AdapterField sensorAdapter = new AdapterField(sensorSelectBtn);
      sensorAdapter.setFieldLabel("Sensor");
      add(sensorAdapter);
      
      add(labelBox);
      
      statesPanel = new FieldSet();
      FormLayout layout = new FormLayout();
      layout.setLabelWidth(65);
      layout.setDefaultWidth(150);
      statesPanel.setLayout(layout);
      statesPanel.setHeading("Sensor State");
      add(statesPanel);
      Sensor sensor = screenImage.getUiImage().getSensor();
      if (sensor == null) {
         statesPanel.hide();
      } else if (sensor.getType() != SensorType.SWITCH && sensor.getType() != SensorType.CUSTOM) {
         statesPanel.hide();
      }
   }
   @SuppressWarnings("unchecked")
   private ComboBox<ModelData> createLabelSelector() {
      ComboBox<ModelData> labelBox = new ComboBoxExt();
      labelBox.setFieldLabel("FallbackLabel");
      Collection<UILabel> labelsonScreen = (Collection<UILabel>) screenImage.getScreenCanvas().getScreen().getAllUIComponentByType(UILabel.class);
      ListStore<ModelData> labelStore = new ListStore<ModelData>();
      for (UILabel label : labelsonScreen) {
         if (!label.isRemoved()) {
            ComboBoxDataModel<UILabel> labelModel = new ComboBoxDataModel<UILabel>(label.getDisplayName(), label);
            labelStore.add(labelModel);
         }
      }
      //set the label for the image. 
      if(screenImage.getUiImage().getLabel()!=null && !screenImage.getUiImage().getLabel().isRemoved()){
         labelBox.setValue(new ComboBoxDataModel<UILabel>(screenImage.getUiImage().getLabel().getDisplayName(),screenImage.getUiImage().getLabel()));
      }
      labelBox.setStore(labelStore);
      labelBox.addSelectionChangedListener(new SelectionChangedListener<ModelData>(){

         @Override
         public void selectionChanged(SelectionChangedEvent<ModelData> se) {
            ComboBoxDataModel<ModelData> labelData = (ComboBoxDataModel<ModelData>) se.getSelectedItem();
            UILabel label = (UILabel) labelData.getData();
            screenImage.getUiImage().setLabel(label);
         }
         
      });
      return labelBox;
   }
   
   private ImageUploadAdapterField createImageUploader() {
      final ImageUploadAdapterField imageSrcField = new ImageUploadAdapterField(null);
      imageSrcField.addUploadListener(Events.OnChange, new Listener<FieldEvent>() {
         public void handleEvent(FieldEvent be) {
            if (!isValid()) {
               return;
            }
            operation = Operation.UPLOAD_IMAGE;
            imageSrcField.setActionToForm(ImagePropertyForm.this);
            submit();
            screenImage.getScreenCanvas().mask("Uploading image...");
         }
      });
      
      imageSrcField.addDeleteListener(new SelectionListener<ButtonEvent>() {
         public void componentSelected(ButtonEvent ce) {
            if (!UIImage.DEFAULT_IMAGE_URL.equals(screenImage.getUiImage().getImageSource().getSrc())){
               screenImage.setImageSource(new ImageSource(UIImage.DEFAULT_IMAGE_URL));
               WidgetSelectionUtil.setSelectWidget(null);
               WidgetSelectionUtil.setSelectWidget(screenImage);
            }
         }
      });
      imageSrcField.setImage(screenImage.getUiImage().getImageSource().getImageFileName());
      imageSrcField.setFieldLabel("Image");
      return imageSrcField;
   }
   
   private void addListenersToForm() {
      addListener(Events.Submit, new Listener<FormEvent>() {
         @Override
         public void handleEvent(FormEvent be) {
            String imageURL = ImageSourceValidator.validate(be.getResultHtml());
            SensorLink sensorLink = screenImage.getUiImage().getSensorLink();
            Map<String,String> sensorAttrMap = new HashMap<String,String>();
            
            if (!"".equals(imageURL)) {
               if(operation == Operation.UPLOAD_IMAGE){
                  screenImage.setImageSource(new ImageSource(imageURL));
               }else {
                  switch (operation){
                  case UPLOAD_SWITCH_ON_IMAGE:
                     sensorAttrMap.put("name","on");
                     break;
                  case UPLOAD_SWITCH_OFF_IMAGE:
                     sensorAttrMap.put("name","off");
                     break;
                  default:
                     sensorAttrMap.put("name",customSensorState.getName());
                  }
                  sensorAttrMap.put("value", imageURL.substring(imageURL.lastIndexOf("/")+1));
                  sensorLink.addOrUpdateChildForSensorLinker("state", sensorAttrMap);
                  screenImage.clearSensorStates();
               }
            }
            screenImage.getScreenCanvas().unmask();
         }
      });
   }
   private void createSensorStates(){
      statesPanel.removeAll();
      SensorLink sensorLink = screenImage.getUiImage().getSensorLink();
      if(screenImage.getUiImage().getSensor()!=null && screenImage.getUiImage().getSensor().getType()==SensorType.SWITCH){
         final ImageUploadAdapterField onImageUploadField = new ImageUploadAdapterField("switchOnImage");
         onImageUploadField.addUploadListener(Events.OnChange, new Listener<FieldEvent>() {
            public void handleEvent(FieldEvent be) {
               if (!isValid()) {
                  return;
               }
               operation = Operation.UPLOAD_SWITCH_ON_IMAGE;
               onImageUploadField.setActionToForm(ImagePropertyForm.this);
               submit();
               screenImage.getScreenCanvas().mask("Uploading image...");
            }
         });
         
         onImageUploadField.addDeleteListener(new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent ce) {
               removeSensorImage("on");
            }
         });
         onImageUploadField.setFieldLabel("on");
         
         final ImageUploadAdapterField offImageUploadField = new ImageUploadAdapterField("switchOffImage");
         offImageUploadField.addUploadListener(Events.OnChange, new Listener<FieldEvent>() {
            public void handleEvent(FieldEvent be) {
               if (!isValid()) {
                  return;
               }
               operation = Operation.UPLOAD_SWITCH_OFF_IMAGE;
               offImageUploadField.setActionToForm(ImagePropertyForm.this);
               submit();
               screenImage.getScreenCanvas().mask("Uploading image...");
            }
         });
         
         offImageUploadField.addDeleteListener(new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent ce) {
               removeSensorImage("off");
            }
         });
         offImageUploadField.setFieldLabel("off");
         
         if(sensorLink!=null){
            onImageUploadField.setImage(sensorLink.getStateValueByStateName("on"));
            offImageUploadField.setImage(sensorLink.getStateValueByStateName("off"));
         }
         statesPanel.add(onImageUploadField);
         statesPanel.add(offImageUploadField);
      }else if(screenImage.getUiImage().getSensor()!=null && screenImage.getUiImage().getSensor().getType() == SensorType.CUSTOM){
         CustomSensor customSensor = (CustomSensor) screenImage.getUiImage().getSensor();
         List<State> states = customSensor.getStates();
         for(final State state: states){
            final ImageUploadAdapterField imageUploaderField = new ImageUploadAdapterField(state.getName());
            imageUploaderField.addUploadListener(Events.OnChange, new Listener<FieldEvent>() {
               public void handleEvent(FieldEvent be) {
                  if (!isValid()) {
                     return;
                  }
                  operation = Operation.OTHER;
                  customSensorState = state;
                  imageUploaderField.setActionToForm(ImagePropertyForm.this);
                  submit();
                  screenImage.getScreenCanvas().mask("Uploading image...");
               }
            });
            imageUploaderField.addDeleteListener(new SelectionListener<ButtonEvent>() {
               public void componentSelected(ButtonEvent ce) {
                  removeSensorImage(state.getName());
               }
            });
            imageUploaderField.setFieldLabel(state.getName());
            
            if(sensorLink!=null){
               imageUploaderField.setImage(sensorLink.getStateValueByStateName(state.getName()));
            }
            statesPanel.add(imageUploaderField);
            
         }
      }
      statesPanel.layout(true);
   }
   
   static enum Operation{
      UPLOAD_IMAGE,UPLOAD_SWITCH_ON_IMAGE,UPLOAD_SWITCH_OFF_IMAGE,OTHER;
   }
   
   
   @Override
   protected void afterRender() {
      super.afterRender();
      ((PropertyPanel)this.getParent()).setHeading("Image properties");
   }
   
   private void removeSensorImage(String stateName) {
      String sensorValue = screenImage.getUiImage().getSensorLink().getStateValueByStateName(stateName);
      if (!"".equals(sensorValue)) {
         screenImage.clearSensorStates();
         Map<String,String> sensorAttrMap = new HashMap<String,String>();
         sensorAttrMap.put("name", stateName);
         sensorAttrMap.put("value", sensorValue);
         screenImage.getUiImage().getSensorLink().removeChildForSensorLinker("state", sensorAttrMap);
         createSensorStates();
      }
   }
}
