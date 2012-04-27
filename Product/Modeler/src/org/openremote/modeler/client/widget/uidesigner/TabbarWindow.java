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

import java.util.ArrayList;
import java.util.List;

import org.openremote.modeler.client.event.SelectEvent;
import org.openremote.modeler.client.event.SubmitEvent;
import org.openremote.modeler.client.listener.SelectListener;
import org.openremote.modeler.client.utils.ImageSourceValidator;
import org.openremote.modeler.client.widget.CommonWindow;
import org.openremote.modeler.client.widget.ImageUploadField;
import org.openremote.modeler.client.widget.NavigateFieldSet;
import org.openremote.modeler.domain.Panel;
import org.openremote.modeler.domain.component.ImageSource;
import org.openremote.modeler.domain.component.Navigate;
import org.openremote.modeler.domain.component.UITabbarItem;
import org.openremote.modeler.domain.component.Navigate.ToLogicalType;

import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FormEvent;
import com.extjs.gxt.ui.client.event.ListViewEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.ListView;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.AdapterField;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.form.FormPanel.LabelAlign;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayout.HBoxLayoutAlign;

public class TabbarWindow extends CommonWindow {

   public static final String TABBAR_ITEM_IMAGE = "tabbarItemImage";
   private List<UITabbarItem> tabbarItems = null;
   private ListView<BeanModel> tabbarItemListView;
   private UITabbarItem selectTabbarItem = new UITabbarItem();
   public TabbarWindow(boolean isGlobal, List<UITabbarItem> tabbarItems, Panel panel) {
      super();
      this.tabbarItems = tabbarItems;
      init(isGlobal, panel);
      show();
   }
   
   private void init(boolean isGlobal, Panel panel) {
      if (isGlobal) {
         setHeading("Global tabbar");
      } else {
         setHeading("Local tabbar");
      }
      setWidth(385);
      setAutoHeight(true);
      FormLayout formLayout = new FormLayout();
      formLayout.setLabelAlign(LabelAlign.TOP);
      formLayout.setLabelWidth(200);
      formLayout.setDefaultWidth(350);
      setLayout(formLayout);
      createFields(panel);
      createButtons();
      setBodyStyleName("padding-top-left-10px");
   }
   private void createFields(Panel panel) {
      AdapterField tabbarField = new AdapterField(createTabbarContainer(tabbarItems));
      tabbarField.setFieldLabel("Tabbar item list");
      
      AdapterField tabbarItemPropertyField = new AdapterField(createTabbarItemPropertyForm(panel));
      tabbarItemPropertyField.setFieldLabel("Selected tabbar item properties");
      
      add(tabbarField);
      add(tabbarItemPropertyField);
   }
   private LayoutContainer createTabbarContainer(List<UITabbarItem> tabbarItems) {
      LayoutContainer tabbarContainer = new LayoutContainer();
      tabbarContainer.setBorders(false);
      tabbarContainer.setSize(340, 140);
      HBoxLayout tabbarContainerLayout = new HBoxLayout();
      tabbarContainerLayout.setHBoxLayoutAlign(HBoxLayoutAlign.TOP);
      tabbarContainer.setLayout(tabbarContainerLayout);
      
      ContentPanel tabbarItemsContainer = new ContentPanel();
      tabbarItemsContainer.setBorders(false);
      tabbarItemsContainer.setBodyBorder(false);
      tabbarItemsContainer.setHeaderVisible(false);
      tabbarItemsContainer.setWidth(260);
      tabbarItemsContainer.setHeight(130);
      tabbarItemsContainer.setLayout(new FitLayout());
      // overflow-auto style is for IE hack.
      tabbarItemsContainer.addStyleName("overflow-auto");
      
      tabbarItemListView = new ListView<BeanModel>();
      
      final ListStore<BeanModel> store = new ListStore<BeanModel>();
      for (UITabbarItem tabbarItem : tabbarItems) {
         store.add(tabbarItem.getBeanModel());
      }
      tabbarItemListView.setStore(store);
      tabbarItemListView.setDisplayProperty("displayName");
      tabbarItemListView.setStyleAttribute("overflow", "auto");
      tabbarItemsContainer.add(tabbarItemListView);
      
      LayoutContainer buttonsContainer = new LayoutContainer();
      buttonsContainer.setSize(80, 130);
      buttonsContainer.setBorders(false);
      buttonsContainer.setLayout(new RowLayout(Orientation.VERTICAL));
      
      Button addItemBtn = new Button("Add");
      addItemBtn.addSelectionListener(new SelectionListener<ButtonEvent>() {
         @Override
         public void componentSelected(ButtonEvent ce) {
            UITabbarItem tabbarItem = new UITabbarItem();
            store.add(tabbarItem.getBeanModel());
         }
      });
      
      Button deleteItemBtn = new Button("Delete");
      deleteItemBtn.addSelectionListener(new SelectionListener<ButtonEvent>() {
         @Override
         public void componentSelected(ButtonEvent ce) {
            BeanModel selectedItem = tabbarItemListView.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
               store.remove(selectedItem);
            }
         }
      });
      
      Button itemUpBtn = new Button("Up");
      itemUpBtn.addSelectionListener(new SelectionListener<ButtonEvent>() {
         @Override
         public void componentSelected(ButtonEvent ce) {
            BeanModel selectedItem = tabbarItemListView.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
               int index = store.indexOf(selectedItem);
               if (index > 0) {
                  store.remove(selectedItem);
                  store.insert(selectedItem, index - 1);
                  tabbarItemListView.getSelectionModel().select(selectedItem, false);
               }
            }
         }
      });
      
      Button itemDownBtn = new Button("Down");
      itemDownBtn.addSelectionListener(new SelectionListener<ButtonEvent>() {
         @Override
         public void componentSelected(ButtonEvent ce) {
            BeanModel selectedItem = tabbarItemListView.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
               int index = store.indexOf(selectedItem);
               if (index < store.getCount() - 1) {
                  store.remove(selectedItem);
                  store.insert(selectedItem, index + 1);
                  tabbarItemListView.getSelectionModel().select(selectedItem, false);
               }
            }
         }
      });
      
      buttonsContainer.add(addItemBtn, new RowData(80, -1, new Margins(5)));
      buttonsContainer.add(deleteItemBtn, new RowData(80, -1, new Margins(5)));
      buttonsContainer.add(itemUpBtn, new RowData(80, -1, new Margins(5)));
      buttonsContainer.add(itemDownBtn, new RowData(80, -1, new Margins(5)));
      
      tabbarItemListView.addListener(Events.Select, new Listener<ListViewEvent<BeanModel>>() {
         @Override
         public void handleEvent(ListViewEvent<BeanModel> be) {
            selectTabbarItem = (UITabbarItem) be.getModel().getBean();
            fireEvent(SelectEvent.SELECT, new SelectEvent(selectTabbarItem));
         }
         
      });
      tabbarContainer.add(tabbarItemsContainer);
      tabbarContainer.add(buttonsContainer);
      return tabbarContainer;
   }
   
   private FieldSet createTabbarItemPropertyForm(Panel panel) {
      FieldSet tabbarItemForm = new FieldSet();
      tabbarItemForm.setBorders(true);
      tabbarItemForm.setWidth(340);
      tabbarItemForm.setStyleAttribute("paddingTop", "2px");
      tabbarItemForm.setStyleAttribute("paddingBottom", "2px");
      FormLayout lay = new FormLayout();
      lay.setLabelAlign(LabelAlign.TOP);
      lay.setLabelWidth(80);
      lay.setDefaultWidth(200);
      tabbarItemForm.setLayout(lay);
      
      // initial name field.
      final TextField<String> nameField = new TextField<String>();
      nameField.setFieldLabel("Name");
      nameField.addListener(Events.Blur, new Listener<BaseEvent>() {
         @Override
         public void handleEvent(BaseEvent be) {
            selectTabbarItem.setName(nameField.getValue());
            tabbarItemListView.getStore().update(selectTabbarItem.getBeanModel());
         }
      });
      
      final FormPanel imageForm = new FormPanel();
      imageForm.setHeaderVisible(false);
      imageForm.setBorders(false);
      imageForm.setBodyBorder(false);
      imageForm.setPadding(0);
      imageForm.setLabelAlign(LabelAlign.TOP);
      final ImageUploadField imageField = new ImageUploadField(null) {
         @Override
         protected void onChange(ComponentEvent ce) {
            super.onChange(ce);
            if (!imageForm.isValid()) {
             return;
            }
            imageForm.submit();
         }
         
      };
      imageField.setActionToForm(imageForm);
      imageForm.add(imageField);
      imageForm.addListener(Events.Submit, new Listener<FormEvent>() {
         public void handleEvent(FormEvent be) {
            ImageSource image = selectTabbarItem.getImage();
            String imageSrc =  ImageSourceValidator.validate(be.getResultHtml());
            if (image == null) {
               image = new ImageSource(imageSrc);
            } else {
               image.setSrc(imageSrc);
            }
            selectTabbarItem.setImage(image);
            be.cancelBubble();
         }
      });
      
      // initial navigate properties
      final NavigateFieldSet navigateSet = new NavigateFieldSet(new Navigate(), panel.getGroups());
      tabbarItemForm.add(nameField);
      tabbarItemForm.add(imageForm);
      tabbarItemForm.add(navigateSet);
      
      addListener(SelectEvent.SELECT, new SelectListener() {
         @Override
         public void afterSelect(SelectEvent be) {
            UITabbarItem tabbarItem = be.getData();
            nameField.setValue(tabbarItem.getName());
            if (tabbarItem.getImage() != null) {
               String imageSrc = tabbarItem.getImage().getSrc();
               imageField.setValue(imageSrc.substring(imageSrc.lastIndexOf("/") + 1));
            } else {
               imageField.setValue("");
            }
            Navigate navigate = tabbarItem.getNavigate();
            if (!navigate.isSet()) {
               navigate.setToLogical(ToLogicalType.login);
            }
            navigateSet.update(tabbarItem.getNavigate());
         }
         
      });
      return tabbarItemForm;
   }
   private void createButtons() {
      Button okBtn = new Button("OK");
      Button cancelBtn = new Button("Cancel");

      okBtn.addSelectionListener(new SelectionListener<ButtonEvent>() {
         @Override
         public void componentSelected(ButtonEvent ce) {
            if (tabbarItems == null) {
               tabbarItems = new ArrayList<UITabbarItem>();
            }
            tabbarItems.clear();
            
            for (BeanModel tabbarItemModel : tabbarItemListView.getStore().getModels()) {
               UITabbarItem tabbarItem = (UITabbarItem) tabbarItemModel.getBean();
               if (!tabbarItem.getNavigate().isSet()) {
                  MessageBox.alert("ERROR", "Tabbar item \"" + tabbarItem.getName() + "\" is required to set navigate.", null);
                  return;
               }
               tabbarItems.add(tabbarItem);
            }
            fireEvent(SubmitEvent.SUBMIT, new SubmitEvent(tabbarItems));
         }
         
      });
      cancelBtn.addSelectionListener(new SelectionListener<ButtonEvent>() {
         @Override
         public void componentSelected(ButtonEvent ce) {
            TabbarWindow.this.hide();
         }
      });

      addButton(okBtn);
      addButton(cancelBtn);
   }
}
