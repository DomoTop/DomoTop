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

import java.util.ArrayList;
import java.util.List;

import org.openremote.modeler.client.event.SubmitEvent;
import org.openremote.modeler.client.listener.FormResetListener;
import org.openremote.modeler.client.listener.FormSubmitListener;
import org.openremote.modeler.client.model.ComboBoxDataModel;
import org.openremote.modeler.client.proxy.SensorBeanModelProxy;
import org.openremote.modeler.client.rpc.AsyncSuccessCallback;
import org.openremote.modeler.client.widget.FormWindow;
import org.openremote.modeler.client.widget.ComboBoxExt;
import org.openremote.modeler.client.widget.TreePanelBuilder;
import org.openremote.modeler.domain.CustomSensor;
import org.openremote.modeler.domain.Device;
import org.openremote.modeler.domain.DeviceCommand;
import org.openremote.modeler.domain.RangeSensor;
import org.openremote.modeler.domain.Sensor;
import org.openremote.modeler.domain.SensorCommandRef;
import org.openremote.modeler.domain.SensorType;
import org.openremote.modeler.domain.State;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FormEvent;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.AdapterField;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.CellEditor;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.EditorGrid;
import com.extjs.gxt.ui.client.widget.grid.EditorGrid.ClicksToEdit;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayout.HBoxLayoutAlign;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel;

public class SensorWindow extends FormWindow {

   private BeanModel sensorModel = null;
   protected TextField<String> nameField = new TextField<String>();
   protected TreePanel<BeanModel> commandSelectTree = null;
   protected ComboBoxExt typeList = new ComboBoxExt();
   private FieldSet customFieldSet = null;
   protected TextField<Integer> minField = new TextField<Integer>();
   protected TextField<Integer> maxField = new TextField<Integer>();
   protected EditorGrid<BeanModel> grid = null;
   private int stateRowIndex = -1;
   
   private Device device = null;
   public SensorWindow() {
      setHeading("New sensor");
      init();
      show();
   }
   public SensorWindow(BeanModel sensorModel) {
      this.sensorModel = sensorModel;
      setHeading("Edit sensor");
      init();
      show();
   }
   
   public SensorWindow(Device device){
      this.device = device;
      if(device==null){
         throw new NullPointerException("A sensor must belong to a device!");
      }
      setHeading("New sensor");
      init();
      show();
   }
   private void init() {
      setWidth(360);
      setAutoHeight(true);
      setLayout(new FlowLayout());
      form.setFrame(true);
      form.setHeaderVisible(false);
      form.setBorders(false);
      form.setLabelWidth(60);
      form.setFieldWidth(230);
      createButtons();
      createFields();
      add(form);
      addListenerToForm();
   }
   
   private void createFields() {
      nameField.setFieldLabel("Name");
      nameField.setAllowBlank(false);
      if (sensorModel != null) {
         nameField.setValue(((Sensor) sensorModel.getBean()).getName());
      }
      
      AdapterField commandField = new AdapterField(createCommandTreeView());
      commandField.setFieldLabel("Command");
      commandField.setBorders(true);
      
      SensorType[] sensorTypes = SensorType.values();
      typeList.setFieldLabel("Type");
      typeList.setEmptyText("--type--");
      for (int i = 0; i < sensorTypes.length; i++) {
         ComboBoxDataModel<SensorType> typeItem = new ComboBoxDataModel<SensorType>(sensorTypes[i].toString(),
               sensorTypes[i]);
         typeList.getStore().add(typeItem);
      }
      
      
      form.add(nameField);
      form.add(commandField);
      form.add(typeList);
      
      final FieldSet rangeSet = createRangeSet();
      form.add(rangeSet);
      rangeSet.hide();
      
      typeList.addSelectionChangedListener(new SelectionChangedListener<ModelData>() {
         @SuppressWarnings("unchecked")
         public void selectionChanged(SelectionChangedEvent<ModelData> se) {
            SensorType type = ((ComboBoxDataModel<SensorType>) se.getSelectedItem()).getData();
            if (type == SensorType.RANGE) {
               if (customFieldSet != null) {
                  customFieldSet.hide();
               }
               rangeSet.show();
            } else if (type == SensorType.CUSTOM) {
               rangeSet.hide();
               if (customFieldSet == null) {
                  customFieldSet = createCustomSet();
                  form.add(customFieldSet);
               }
               customFieldSet.show();
            } else {
               if (customFieldSet != null) {
                  customFieldSet.hide();
               }
               rangeSet.hide();
            }
            form.layout();
            layout();
         }
         
      });
      
      if (sensorModel != null) {
         Sensor sensor = sensorModel.getBean();
         if (sensor.getType() != null) {
            typeList.setValue(new ComboBoxDataModel<SensorType>(sensor.getType().toString(), sensor.getType()));
            if (sensor.getType() == SensorType.RANGE) {
               minField.setValue(((RangeSensor) sensor).getMin());
               maxField.setValue(((RangeSensor) sensor).getMax());
//               minField.disable();
//               maxField.disable();
            } else if (sensor.getType() == SensorType.CUSTOM) {
               List<State> states = ((CustomSensor) sensor).getStates();
               for (State state : states) {
                  grid.getStore().add(state.getBeanModel());
               }
//               customFieldSet.disable();
            }
         }
//         commandSelectTree.disable();
         typeList.disable();
         
      }
   }
   
   private LayoutContainer createCommandTreeView() {
      LayoutContainer deviceCommandTreeContainer = new LayoutContainer();
      deviceCommandTreeContainer.setSize(230, 120);
      deviceCommandTreeContainer.setLayout(new FitLayout());
      // overflow-auto style is for IE hack.
//      deviceCommandTreeContainer.addStyleName("overflow-auto");
      deviceCommandTreeContainer.setStyleAttribute("backgroundColor", "white");
      
      buildCommandSelectTree(device);
      deviceCommandTreeContainer.add(commandSelectTree);
      
      return deviceCommandTreeContainer;
   }
   
   protected void buildCommandSelectTree(Device device) {
      BeanModel selectedCommandModel = null;
      if (sensorModel != null) {
         Sensor sensor = sensorModel.getBean();
         if (sensor.getSensorCommandRef() != null) {
            selectedCommandModel = sensor.getSensorCommandRef().getDeviceCommand().getBeanModel();
         }
      }
      commandSelectTree = TreePanelBuilder.buildCommandTree(device!=null?device:((Sensor)sensorModel.getBean()).getDevice(), selectedCommandModel);
   }
   private FieldSet createRangeSet() {
      FieldSet rangeSet = new FieldSet();
      rangeSet.setWidth(300);
      FormLayout layout = new FormLayout();
      layout.setLabelWidth(50);
      rangeSet.setLayout(layout);
      rangeSet.setHeading("Range properties");
      
      minField.setFieldLabel("Min");
      minField.setAllowBlank(false);
      minField.setValue(0);
      
      maxField.setFieldLabel("Max");
      maxField.setAllowBlank(false);
      maxField.setValue(1);
      
      rangeSet.add(minField);
      rangeSet.add(maxField);
      
      return rangeSet;
   }
   
   private FieldSet createCustomSet() {
      FieldSet customSet = new FieldSet();
      customSet.setHeading("Custom state items");
      customSet.setWidth(300);
      
      LayoutContainer customStatesContainer = new LayoutContainer();
      customStatesContainer.setBorders(false);
      customStatesContainer.setSize(280, 110);
      HBoxLayout tabbarContainerLayout = new HBoxLayout();
      tabbarContainerLayout.setHBoxLayoutAlign(HBoxLayoutAlign.TOP);
      customStatesContainer.setLayout(tabbarContainerLayout);
      
      ContentPanel stateItemsContainer = new ContentPanel();
      stateItemsContainer.setBorders(false);
      stateItemsContainer.setBodyBorder(false);
      stateItemsContainer.setHeaderVisible(false);
      stateItemsContainer.setWidth(200);
      stateItemsContainer.setHeight(100);
      stateItemsContainer.setLayout(new FitLayout());
      stateItemsContainer.setScrollMode(Scroll.AUTOY);
      
      List<ColumnConfig> configs = new ArrayList<ColumnConfig>();
      ColumnConfig nameColumn = new ColumnConfig();
      nameColumn.setId("name");
      nameColumn.setHeader("Name");
      nameColumn.setWidth(100);
      
      ColumnConfig valueColumn = new ColumnConfig();
      valueColumn.setId("value");
      valueColumn.setHeader("Value");
      valueColumn.setWidth(100);

      TextField<String> text = new TextField<String>();
      text.setAllowBlank(false);
      nameColumn.setEditor(new CellEditor(text));  
      valueColumn.setEditor(new CellEditor(new TextField<String>()));
      configs.add(nameColumn);
      configs.add(valueColumn);
      
      grid = new EditorGrid<BeanModel>(new ListStore<BeanModel>(), new ColumnModel(configs));
      grid.setClicksToEdit(ClicksToEdit.TWO);
      grid.setAutoExpandColumn("name");
//      grid.setHideHeaders(true);
      stateItemsContainer.add(grid);
      grid.addListener(Events.RowClick, new Listener<GridEvent<BeanModel>>() {
         @Override
         public void handleEvent(GridEvent<BeanModel> be) {
            stateRowIndex = be.getRowIndex();
         }
      });
      grid.addListener(Events.AfterEdit, new Listener<GridEvent<BeanModel>>() {
         @Override
         public void handleEvent(GridEvent<BeanModel> be) {
            grid.getStore().commitChanges();
         }
      });
      
      LayoutContainer buttonsContainer = new LayoutContainer();
      buttonsContainer.setSize(80, 100);
      buttonsContainer.setBorders(false);
      buttonsContainer.setLayout(new RowLayout(Orientation.VERTICAL));
      
      Button addItemBtn = new Button("Add");
      addItemBtn.addSelectionListener(new SelectionListener<ButtonEvent>() {
         @Override
         public void componentSelected(ButtonEvent ce) {
            grid.stopEditing();
            grid.getStore().add(new State().getBeanModel());
            grid.startEditing(grid.getStore().getCount() - 1, 0);
         }
      });
      
      Button deleteItemBtn = new Button("Delete");
      deleteItemBtn.addSelectionListener(new SelectionListener<ButtonEvent>() {
         @Override
         public void componentSelected(ButtonEvent ce) {
            if (stateRowIndex != -1) {
               grid.getStore().remove(grid.getStore().getAt(stateRowIndex));
            }
         }
      });
      
      
      buttonsContainer.add(addItemBtn, new RowData(80, -1, new Margins(10)));
      buttonsContainer.add(deleteItemBtn, new RowData(80, -1, new Margins(10)));
      
      customStatesContainer.add(stateItemsContainer);
      customStatesContainer.add(buttonsContainer);
      
      AdapterField statesField = new AdapterField(customStatesContainer);
      statesField.setHideLabel(true);
      customSet.add(statesField);
      return customSet;
   }
   private void createButtons() {
      Button submitBtn = new Button("Submit");
      Button resetBtn = new Button("Reset");

      submitBtn.addSelectionListener(new FormSubmitListener(form));
      resetBtn.addSelectionListener(new FormResetListener(form));

      form.addButton(submitBtn);
      form.addButton(resetBtn);
   }
   
   private void addListenerToForm() {
      form.addListener(Events.BeforeSubmit, new Listener<FormEvent>() {
         @SuppressWarnings("unchecked")
         public void handleEvent(FormEvent be) {
            BeanModel selectedCommand = commandSelectTree.getSelectionModel().getSelectedItem();
            if (typeList.getValue() == null) {
               MessageBox.alert("Warn", "A sensor must have a type", null);
               typeList.focus();
               return;
            }
            if (selectedCommand == null || !(selectedCommand.getBean() instanceof DeviceCommand)) {
               MessageBox.alert("Warn", "A sensor must have a device command", null);
               commandSelectTree.focus();
               return;
            }
            
            // The duplicate code bellow is for transforming custom sensor  to server completely.
            SensorType type = ((ComboBoxDataModel<SensorType>) typeList.getValue()).getData();
            if (null == sensorModel) {
               Sensor sensor = new Sensor();
               if (type == SensorType.RANGE) {
                   sensor = new RangeSensor();
                  ((RangeSensor) sensor).setMin(Integer.valueOf(minField.getRawValue()));
                  ((RangeSensor) sensor).setMax(Integer.valueOf(maxField.getRawValue()));
               } else if (type == SensorType.CUSTOM) {
                  sensor = new CustomSensor();
                  List<BeanModel> states = grid.getStore().getModels();
                  for (BeanModel stateModel : states) {
                     State state = stateModel.getBean();
                     state.setSensor((CustomSensor)sensor);
                     ((CustomSensor)sensor).addState(state);
                  }
               } else if (type == SensorType.SWITCH) {
                  
               }
               sensor.setType(type);
               sensor.setName(nameField.getValue());
               
               DeviceCommand cmd = selectedCommand.getBean();
               SensorCommandRef sensorCommandRef = new SensorCommandRef();
               sensorCommandRef.setDeviceCommand(cmd);
               sensorCommandRef.setSensor(sensor);
               sensor.setSensorCommandRef(sensorCommandRef);
               
               sensor.setDevice(device);
               SensorBeanModelProxy.saveSensor(sensor, new AsyncSuccessCallback<Sensor>() {
                  public void onSuccess(Sensor result) {
                     fireEvent(SubmitEvent.SUBMIT, new SubmitEvent(result));
                  }
               });
            } else if (null != sensorModel) {
               Sensor sensor = sensorModel.getBean();
               if (type == SensorType.RANGE) {
                  ((RangeSensor) sensor).setMin(Integer.valueOf(minField.getRawValue()));
                  ((RangeSensor) sensor).setMax(Integer.valueOf(maxField.getRawValue()));
               } else if (type == SensorType.CUSTOM) {
                  ((CustomSensor) sensor).getStates().clear();
                  List<BeanModel> states = grid.getStore().getModels();
                  for (BeanModel stateModel : states) {
                     State state = stateModel.getBean();
                     state.setOid(0);
                     state.setSensor((CustomSensor) sensor);
                     ((CustomSensor) sensor).addState(state);
                  }
               }
               sensor.setName(nameField.getValue());
               
               DeviceCommand cmd = selectedCommand.getBean();
               SensorCommandRef sensorCommandRef = new SensorCommandRef();
               sensorCommandRef.setDeviceCommand(cmd);
               sensorCommandRef.setSensor(sensor);
               sensor.setSensorCommandRef(sensorCommandRef);
               SensorBeanModelProxy.updateSensor(sensor, new AsyncSuccessCallback<Sensor>() {
                  public void onSuccess(Sensor result) {
                     fireEvent(SubmitEvent.SUBMIT, new SubmitEvent(result));
                  }
               });
            }
         }

      });
   }
   
   
}
