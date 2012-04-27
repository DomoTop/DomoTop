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

import java.util.LinkedHashSet;
import java.util.Set;

import org.openremote.modeler.client.Constants;
import org.openremote.modeler.client.event.SubmitEvent;
import org.openremote.modeler.client.model.ComboBoxDataModel;
import org.openremote.modeler.client.proxy.ControllerConfigBeanModelProxy;
import org.openremote.modeler.client.rpc.AsyncSuccessCallback;
import org.openremote.modeler.domain.ConfigCategory;
import org.openremote.modeler.domain.ControllerConfig;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.google.gwt.user.client.rpc.AsyncCallback;
/**
 * A tab item for configuring the controller under a specific category. 
 * @author javen
 *
 */
public class ControllerConfigTabItem extends TabItem {

   private ConfigCategory category;
   private Set<ControllerConfig> configs = null;
   private Set<ControllerConfig> newConfigs = null;               //new configurations after the Controller-Config-2.0-M7.xml updated. 
   private Text hintContent = new Text();
   private FormPanel configContainer = new FormPanel();
   private FieldSet hintFieldSet = new FieldSet();
   
   public ControllerConfigTabItem(ConfigCategory category){
      this.category = category;
      this.setHeight(500);
      this.setScrollMode(Scroll.AUTO);
      
      setText(category.getName());
      setClosable(true);
      setLayout(new FitLayout());  
      
      FormLayout layout = new FormLayout();
      layout.setLabelWidth(200);
      layout.setDefaultWidth(400);
      
      configContainer.setLayout(layout);
      configContainer.setAutoHeight(true);
      configContainer.setScrollMode(Scroll.AUTOY);
      configContainer.setButtonAlign(HorizontalAlignment.RIGHT);
      configContainer.setBorders(false);
      configContainer.setBodyBorder(false);
      configContainer.setLabelWidth(55);
      configContainer.setPadding(5);
      configContainer.setHeaderVisible(false);
      configContainer.setWidth(500);
      configContainer.setHeight(300);
      Button submitBtn =   new Button("Submit");
      submitBtn.addSelectionListener(new SaveListener());
      
      configContainer.addButton(submitBtn);
      add(configContainer);
      
      hintFieldSet.setLayout(new FitLayout());
      hintFieldSet.setHeading("Hint");
      
      
      hintContent.setText(category.getDescription());
      hintContent.setWidth("100%");
      hintContent.setHeight("34%");
      hintContent.setStyleAttribute("fontSize", "11px");
      hintContent.setStyleAttribute("fontFamily", Constants.DEFAULT_FONT_FAMILY);
      setStyleAttribute("overflowY", "auto");
      initForm();
   }
   
   
   private void initForm() {
      if (configs == null) {
         ControllerConfigBeanModelProxy.getConfigs(category, new AsyncSuccessCallback<Set<ControllerConfig>>() {

            @Override
            public void onSuccess(Set<ControllerConfig> result) {
               configs = result;
               for (ControllerConfig config : configs) {
                  createProperty(config,false);
               }

               if (newConfigs == null) {
                  ControllerConfigBeanModelProxy.listAllMissingConfigs(category.getName(),
                        new AsyncCallback<Set<ControllerConfig>>() {

                           @Override
                           public void onFailure(Throwable caught) {
                              Info.display("Error", "Failed to load new controller configuration. ");
                           }

                           @Override
                           public void onSuccess(Set<ControllerConfig> result) {
                              newConfigs = result;
                              for (ControllerConfig config : newConfigs) {
                                 createProperty(config, true);
                              }
                              if (newConfigs.size() > 0) {
                                 LabelField label = new LabelField();
                                 label.setHideLabel(true);
                                 label.setText("(new configuration is marked as red)");
                                 label.setStyleAttribute("fontSize", "11px");
                                 label.setStyleAttribute("fontFamily", Constants.DEFAULT_FONT_FAMILY);
                                 configContainer.add(label);
                                 
                                 Info.display("Info",
                                 "The controller has be updated, you need to update your configurations.");
                              }
                              hintFieldSet.add(hintContent);
                              configContainer.add(hintFieldSet);
                              layout();
                           }

                        });
               }
               layout();
            }
         });
      }
   }


   public ConfigCategory getCategory() {
      return category;
   }


   public void setCategory(ConfigCategory category) {
      this.category = category;
   }
   
   private void createProperty(ControllerConfig config,boolean isNewConfig) {
      if (config.getOptions().trim().length() == 0) {
         TextField<String> configValueField = new TextField<String>();
         if (isNewConfig) {
            configValueField.setFieldLabel("<font color=\"red\">"+config.getName()+"</font>");
         } else {
            configValueField.setFieldLabel(config.getName());
         }
         configValueField.setName(config.getName());
         configValueField.setValue(config.getValue());
         configValueField.setRegex(config.getValidation());
         configValueField.getMessages().setRegexText("This property must match: " + config.getValidation());
         addUpdateListenerToTextField(config,configValueField);
         configContainer.add(configValueField);
      } else {
         final ComboBox<ModelData> optionComboBox = new ComboBox<ModelData>();
         ListStore<ModelData> store = new ListStore<ModelData>();
         String[] options = config.optionsArray();
         for (int i = 0; i < options.length; i++) {
            ComboBoxDataModel<String> option = new ComboBoxDataModel<String>(options[i], options[i]);
            store.add(option);
         }
         optionComboBox.setValue(new ComboBoxDataModel<String>(config.getValue(),config.getValue()));
         optionComboBox.setStore(store);
         optionComboBox.setDisplayField(ComboBoxDataModel.getDisplayProperty());
         if (isNewConfig) {
            optionComboBox.setFieldLabel("<font color=\"red\">"+config.getName()+"</font>");
         } else {
            optionComboBox.setFieldLabel(config.getName());
         }
         optionComboBox.setName(config.getName() + "Options");
         optionComboBox.setAllowBlank(false);
         addUpdateListenerToComboBox(config,optionComboBox);
         configContainer.add(optionComboBox);
      }
   }
   
   private void addUpdateListenerToTextField(final ControllerConfig config,final TextField<String> configValueField){
      configValueField.addListener(Events.Blur, new Listener<BaseEvent>() {
         @Override
         public void handleEvent(BaseEvent be) {
            if(configValueField.isValid()){
               config.setValue(configValueField.getValue());
               hintContent.setText(category.getDescription());
            }
         }
      });
      configValueField.addListener(Events.Focus, new Listener<BaseEvent>() {
         @Override
         public void handleEvent(BaseEvent be) {
            hintContent.setText(config.getHint());
         }
      });
   }
   
   private void addUpdateListenerToComboBox(final ControllerConfig config,final ComboBox<ModelData> configValueComboBox){
      configValueComboBox.addSelectionChangedListener(new SelectionChangedListener<ModelData>() {

         @SuppressWarnings("unchecked")
         @Override
         public void selectionChanged(SelectionChangedEvent<ModelData> se) {
            ComboBoxDataModel<String> optionItem = (ComboBoxDataModel<String>) se.getSelectedItem();;
            String option = optionItem.getData();
            config.setValue(option);
         }
         
      });
      configValueComboBox.addListener(Events.Focus, new Listener<BaseEvent>() {
         @Override
         public void handleEvent(BaseEvent be) {
            hintContent.setText(config.getHint());
         }
      });
      
      configValueComboBox.addListener(Events.Blur, new Listener<BaseEvent>() {
         @Override
         public void handleEvent(BaseEvent be) {
            hintContent.setText(category.getDescription());
         }
      });
   }
   
 class SaveListener extends SelectionListener<ButtonEvent>{
   @Override
   public void componentSelected(ButtonEvent ce) {
      Set<ControllerConfig> allConfigs = new LinkedHashSet<ControllerConfig>();
      allConfigs.addAll(configs);
      allConfigs.addAll(newConfigs);
      ControllerConfigBeanModelProxy.saveAllConfigs(allConfigs, new AsyncSuccessCallback<Set<ControllerConfig>>(){

         @Override
         public void onSuccess(Set<ControllerConfig> result) {
            configs = result;
            Info.display("save", "Property saved successfully");
            fireEvent(SubmitEvent.SUBMIT,new SubmitEvent(this));
         }
         
      });
   }
    
 }
}
