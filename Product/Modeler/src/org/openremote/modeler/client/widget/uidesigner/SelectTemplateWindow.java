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
import org.openremote.modeler.client.gxtextends.NestedJsonLoadResultReader;
import org.openremote.modeler.client.proxy.TemplateProxy;
import org.openremote.modeler.client.proxy.UtilsProxy;
import org.openremote.modeler.client.rpc.AsyncSuccessCallback;
import org.openremote.modeler.client.utils.ScreenFromTemplate;
import org.openremote.modeler.domain.Template;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.data.BaseListLoader;
import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.data.DataField;
import com.extjs.gxt.ui.client.data.ListLoadResult;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.ModelType;
import com.extjs.gxt.ui.client.data.ScriptTagProxy;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
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
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.form.Radio;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;
import com.extjs.gxt.ui.client.widget.layout.RowData;
/**
 * A window for select a template.
 * @author javen
 *
 */
public class SelectTemplateWindow extends Dialog{
   private ListView<BeanModel> templatesList = new ListView<BeanModel>();
   private String restURL = "";
   
   public SelectTemplateWindow(){
      setHeading("Select Template");
      setMinHeight(400);
      setWidth(240);
//      setLayout(new RowLayout(Orientation.VERTICAL));
      setModal(true);
      createShareView();
      initTemplatesList();
      showTemplateInfo();
      setButtons(Dialog.OKCANCEL);
      setHideOnButtonClick(true);
      addSelectListener();
      show();
   }
   
   
   private void initTemplatesList() {
      createPriviteTemplateListView();
      templatesList.setDisplayProperty("name");
      ContentPanel templatesContainer = new ContentPanel();
      templatesContainer.setSize(235, 300);
      templatesContainer.setScrollMode(Scroll.AUTOY);
      templatesContainer.setBorders(false);
      templatesContainer.setBodyBorder(false);
      templatesContainer.add(templatesList);
//      BorderLayoutData centerData = new BorderLayoutData(LayoutRegion.CENTER);
//      centerData.setMargins(new Margins(2));
      
      add(templatesContainer/*,centerData*/);
      layout();
   }
   
   private void createShareView(){
      FieldSet shareFieldSet = new FieldSet();
      RadioGroup shareRadioGroup = new RadioGroup();
      
      Radio shareNoneRadio = new Radio();
      shareNoneRadio.setBoxLabel("Private");
      shareNoneRadio.setValue(true);
      shareNoneRadio.addListener(Events.Change, new Listener<FieldEvent>(){

         @Override
         public void handleEvent(FieldEvent be) {
            Boolean showPrivate = (Boolean) be.getValue();
            if(showPrivate) {
               createPriviteTemplateListView();
            } else {
               createPublicTemplateListView();
            }
            
         }
         
      });
      
      Radio shareToAllRadio = new Radio();
      shareToAllRadio.setName("Public");
      shareToAllRadio.setBoxLabel("Public");
      
      shareRadioGroup.add(shareNoneRadio);
      shareRadioGroup.add(shareToAllRadio);
      
      shareFieldSet.add(shareRadioGroup);
      
      shareFieldSet.setHeading("Select");
//      shareFieldSet.setCollapsible(true);
      add(shareFieldSet);
   }
   private void showTemplateInfo() {
      final Html templateInfo = new Html("<p><b>Switch info</b></p>"); 
      templatesList.getSelectionModel().addSelectionChangedListener(new SelectionChangedListener<BeanModel>() {
         @Override
         public void selectionChanged(SelectionChangedEvent<BeanModel> se) {
            ModelData templateModel = se.getSelectedItem();
            if (templateModel != null) {
               Long oid = templateModel.get("id");
               String content = templateModel.get("content");
               String name = templateModel.get("name");
               
               Template template = new Template();
               template.setContent(content);
               template.setOid(oid);
               template.setName(name);
               String templateName = "<p><b>Template info</b></p>";
               if (template.getName() != null){
                  templateName = templateName + "<p>name: " + template.getName() + "</p>";
               }
               templateInfo.setHtml(templateName);
               
            }
         }
      });
      add(templateInfo, new RowData(1, -1, new Margins(4)));
   }
   
   private void addSelectListener(){
      addListener(Events.BeforeHide, new Listener<WindowEvent>() {

         @Override
         public void handleEvent(WindowEvent be) { if (be.getButtonClicked() == getButtonById("ok")) {
            ModelData templateModelData = templatesList.getSelectionModel().getSelectedItem();
            if (templateModelData == null) {
               MessageBox.alert("Error", "Please select a Template.", null);
               be.cancelBubble();
            } else {
               Long oid = templateModelData.get("id");
               String content = templateModelData.get("content");
               String name = templateModelData.get("name");
               
               Template template = new Template();
               template.setContent(content);
               template.setOid(oid);
               template.setName(name);
               TemplateProxy.buildScreenFromTemplate(template, new AsyncSuccessCallback<ScreenFromTemplate>(){

                  @Override
                  public void onSuccess(ScreenFromTemplate result) {
                     fireEvent(SubmitEvent.SUBMIT,new SubmitEvent(result.getScreen().getBeanModel()));
                  }
                  
               });
            }
         }
      }
         
      });
   }
   
   private void createPriviteTemplateListView(){
      UtilsProxy.getTemplatesListRestUrl(new AsyncSuccessCallback<String>() {

         @Override
         public void onSuccess(String result) {
            restURL = result;
            createView();
            layout();
         }

      });
   }
   
   private void createPublicTemplateListView(){
      UtilsProxy.getAllPublicTemplateRestURL(new AsyncSuccessCallback<String>(){

         @Override
         public void onSuccess(String result) {
            restURL = result;
            createView();
            layout();
         }
         
      });
   }
   private void createView(){
      /*
       * parse template from json. 
       */
      ModelType templateType = new ModelType();
      templateType.setRoot("templates.template");
      DataField idField = new DataField("id");
      idField.setType(Long.class);
      templateType.addField(idField);
      templateType.addField("content");
      templateType.addField("name");
      ScriptTagProxy<ListLoadResult<ModelData>> scriptTagProxy = new ScriptTagProxy<ListLoadResult<ModelData>>(restURL);
      NestedJsonLoadResultReader<ListLoadResult<ModelData>> reader = new NestedJsonLoadResultReader<ListLoadResult<ModelData>>(
            templateType);
      final BaseListLoader<ListLoadResult<BeanModel>> loader = new BaseListLoader<ListLoadResult<BeanModel>>(scriptTagProxy, reader);

      ListStore<BeanModel> store = new ListStore<BeanModel>(loader);
      loader.load();
      templatesList.setStore(store);
   }
}
