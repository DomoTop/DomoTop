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
import org.openremote.modeler.client.proxy.DeviceBeanModelProxy;
import org.openremote.modeler.client.rpc.AsyncSuccessCallback;
import org.openremote.modeler.client.widget.CommonForm;
import org.openremote.modeler.client.widget.TreePanelBuilder;
import org.openremote.modeler.domain.Account;
import org.openremote.modeler.domain.CommandRefItem;
import org.openremote.modeler.domain.Device;
import org.openremote.modeler.domain.DeviceCommand;
import org.openremote.modeler.domain.Sensor;
import org.openremote.modeler.domain.SensorRefItem;
import org.openremote.modeler.domain.Slider;
import org.openremote.modeler.domain.Switch;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FormEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.AdapterField;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayout.HBoxLayoutAlign;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel;

public class DeviceContentWizardForm extends CommonForm {

   public static Account account;
   private Device device = null;
   private TreePanel<BeanModel> deviceContentTree;
   private Component wrapper;
   public DeviceContentWizardForm(Component wrapper, BeanModel deviceBeanModel) {
      super();
      this.wrapper = wrapper;
      this.device = deviceBeanModel.getBean();
      setHeight(240);
      init();
   }
   
   private void init() {
      FormLayout formLayout = new FormLayout();
      formLayout.setLabelAlign(LabelAlign.TOP);
      formLayout.setLabelWidth(200);
      formLayout.setDefaultWidth(340);
      setLayout(formLayout);
      
      AdapterField contentField = new AdapterField(createContentContainer());
      contentField.setFieldLabel("Commands,sensors,switches and sliders");
      add(contentField);
      onSubmit();
      getAccount();
   }

   private void getAccount() {
      DeviceBeanModelProxy.getAccount(new AsyncSuccessCallback <Account>(){
         public void onSuccess(Account result) {
            account = result;
         }
      });
   }
   
   private LayoutContainer createContentContainer() {
      LayoutContainer contentContainer = new LayoutContainer();
      contentContainer.setBorders(false);
      contentContainer.setSize(340, 170);
      HBoxLayout tabbarContainerLayout = new HBoxLayout();
      tabbarContainerLayout.setHBoxLayoutAlign(HBoxLayoutAlign.TOP);
      contentContainer.setLayout(tabbarContainerLayout);
      
      LayoutContainer contentItemsContainer = new LayoutContainer();
      contentItemsContainer.setBorders(true);
      contentItemsContainer.setWidth(230);
      contentItemsContainer.setHeight(160);
      contentItemsContainer.setLayout(new FitLayout());
      contentItemsContainer.setStyleAttribute("backgroundColor", "white");
      // overflow-auto style is for IE hack.
      contentItemsContainer.addStyleName("overflow-auto");
      
      TreeStore<BeanModel> deviceContentTreeStore = new TreeStore<BeanModel>();
      deviceContentTree = TreePanelBuilder.buildDeviceContentTree(deviceContentTreeStore);
      contentItemsContainer.add(deviceContentTree);
      
      LayoutContainer buttonsContainer = new LayoutContainer();
      buttonsContainer.setSize(110, 160);
      buttonsContainer.setBorders(false);
      buttonsContainer.setLayout(new RowLayout(Orientation.VERTICAL));
      
      Button addCommandBtn = new Button("Add command");
      addCommandBtn.addSelectionListener(new AddCommandListener());
      
      Button addSensorBtn = new Button("Add sensor");
      addSensorBtn.addSelectionListener(new AddSensorListener());
      
      Button addSwitchBtn = new Button("Add switch");
      addSwitchBtn.addSelectionListener(new AddSwitchListener());
      
      Button addSliderBtn = new Button("Add slider");
      addSliderBtn.addSelectionListener(new AddSliderListener());
      
      Button deleteBtn = new Button("Delete");
      deleteBtn.addSelectionListener(new DeleteContentListener());
      
      buttonsContainer.add(addCommandBtn, new RowData(110, -1, new Margins(5)));
      buttonsContainer.add(addSensorBtn, new RowData(110, -1, new Margins(5)));
      buttonsContainer.add(addSwitchBtn, new RowData(110, -1, new Margins(5)));
      buttonsContainer.add(addSliderBtn, new RowData(110, -1, new Margins(5)));
      buttonsContainer.add(deleteBtn, new RowData(110, -1, new Margins(5)));
      
      contentContainer.add(contentItemsContainer);
      contentContainer.add(buttonsContainer);
      return contentContainer;
   }
   
   @Override
   public boolean isNoButton() {
      return true;
   }
   
   @Override
   public void show() {
      super.show();
      ((Window) wrapper).setSize(390, 240);
   }
   
   public Device getDevice() {
      return device;
   }
   public void setDevice(Device device) {
      this.device = device;
   }
   
   private void onSubmit() {
      addListener(Events.BeforeSubmit, new Listener<FormEvent>() {
         public void handleEvent(FormEvent be) {
            DeviceBeanModelProxy.saveDeviceWithContents(device, new AsyncSuccessCallback<BeanModel>() {
               @Override
               public void onSuccess(BeanModel deviceModel) {
                  wrapper.fireEvent(SubmitEvent.SUBMIT, new SubmitEvent(deviceModel));
               }
            });
         }
         
      });
   }
   


   private final class AddCommandListener extends SelectionListener<ButtonEvent> {
      @Override
      public void componentSelected(ButtonEvent ce) {
         DeviceCommandWizardWindow deviceCommandWizardWindow = new DeviceCommandWizardWindow(device);
         deviceCommandWizardWindow.addListener(SubmitEvent.SUBMIT, new SubmitListener() {
            @Override
            public void afterSubmit(SubmitEvent be) {
               DeviceCommand deviceCommand = be.getData();
               device.getDeviceCommands().add(deviceCommand);
               deviceContentTree.getStore().add(deviceCommand.getBeanModel(), false);
               Info.display("Info", "Create command " + deviceCommand.getName() + " success");
            }
         });
      }
   }
   
   private final class AddSensorListener extends SelectionListener<ButtonEvent> {
      @Override
      public void componentSelected(ButtonEvent ce) {
         final SensorWizardWindow sensorWizardWindow = new SensorWizardWindow(device);
         sensorWizardWindow.addListener(SubmitEvent.SUBMIT, new SubmitListener() {
            @Override
            public void afterSubmit(SubmitEvent be) {
               Sensor sensor = be.getData();
               device.getSensors().add(sensor);
               deviceContentTree.getStore().add(sensor.getBeanModel(), false);
               sensorWizardWindow.hide();
            }
         });
         sensorWizardWindow.addListener(DeviceWizardEvent.ADD_CONTENT, new DeviceWizardListener() {
            @Override
            public void afterAdd(DeviceWizardEvent be) {
               BeanModel deviceCommandModel = be.getData();
               deviceContentTree.getStore().add(deviceCommandModel, false);
            }
         });
      }
   }
   
   private final class AddSwitchListener extends SelectionListener<ButtonEvent> {
      @Override
      public void componentSelected(ButtonEvent ce) {
         final SwitchWizardWidnow switchWizardWindow = new SwitchWizardWidnow(device);
         switchWizardWindow.addListener(SubmitEvent.SUBMIT, new SubmitListener() {
            @Override
            public void afterSubmit(SubmitEvent be) {
               Switch switchToggle = be.getData();
               device.getSwitchs().add(switchToggle);
               deviceContentTree.getStore().add(switchToggle.getBeanModel(), false);
               switchWizardWindow.hide();
            }
         });
         switchWizardWindow.addListener(DeviceWizardEvent.ADD_CONTENT, new DeviceWizardListener() {
            @Override
            public void afterAdd(DeviceWizardEvent be) {
               BeanModel beanModel = be.getData();
               if (beanModel != null) {
                  deviceContentTree.getStore().add(beanModel, false);
               }
            }
         });
      }
   }
   
   private final class AddSliderListener extends SelectionListener<ButtonEvent> {
      @Override
      public void componentSelected(ButtonEvent ce) {
         final SliderWizardWindow sliderWizardWindow = new SliderWizardWindow(device);
         sliderWizardWindow.addListener(SubmitEvent.SUBMIT, new SubmitListener() {
            @Override
            public void afterSubmit(SubmitEvent be) {
               Slider slider = be.getData();
               device.getSliders().add(slider);
               deviceContentTree.getStore().add(slider.getBeanModel(), false);
               sliderWizardWindow.hide();
            }
         });
         sliderWizardWindow.addListener(DeviceWizardEvent.ADD_CONTENT, new DeviceWizardListener() {
            @Override
            public void afterAdd(DeviceWizardEvent be) {
               BeanModel beanModel = be.getData();
               if (beanModel != null) {
                  deviceContentTree.getStore().add(beanModel, false);
               }
            }
         });
      }
   }
   
   private final class DeleteContentListener extends SelectionListener<ButtonEvent> {
      @Override
      public void componentSelected(ButtonEvent ce) {
         List<BeanModel> selectedModels = deviceContentTree.getSelectionModel().getSelectedItems();
         for (BeanModel beanModel : selectedModels) {
            if (beanModel.getBean() instanceof DeviceCommand) {
               List<CommandRefItem> commandRefItems = device.getCommandRefItems();
               for (CommandRefItem commandRefItem : commandRefItems) {
                  if (commandRefItem.getDeviceCommand() == beanModel.getBean()) {
                     MessageBox.alert("Warn", "The command cann't be delete, because it was refrenced by other sensor, switch or slider.", null);
                     return;
                  }
               }
               device.getDeviceCommands().remove(beanModel.getBean());
            } else if (beanModel.getBean() instanceof Sensor) {
               List<SensorRefItem> sensorRefItems = device.getSensorRefItems();
               for (SensorRefItem sensorRefItem : sensorRefItems) {
                  if (sensorRefItem.getSensor() == beanModel.getBean()) {
                     MessageBox.alert("Warn", "The sensor cann't be delete, because it was refrenced by other switch or slider.", null);
                     return;
                  }
               }
               device.getSensors().remove(beanModel.getBean());
            } else if(beanModel.getBean() instanceof Slider) {
               device.getSliders().remove(beanModel.getBean());
            } else if(beanModel.getBean() instanceof Switch) {
               deviceContentTree.getStore().remove(beanModel);
               device.getSwitchs().remove(beanModel.getBean());
            }
            deviceContentTree.getStore().remove(beanModel);
         }
      }
   }
}
