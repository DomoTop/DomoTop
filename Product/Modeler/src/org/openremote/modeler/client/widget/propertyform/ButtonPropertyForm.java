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

import java.util.ArrayList;

import org.openremote.modeler.client.event.SubmitEvent;
import org.openremote.modeler.client.listener.SubmitListener;
import org.openremote.modeler.client.widget.IconPreviewWidget;
import org.openremote.modeler.client.widget.NavigateFieldSet;
import org.openremote.modeler.client.widget.component.ImageSelectAdapterField;
import org.openremote.modeler.client.widget.component.ScreenButton;
import org.openremote.modeler.client.widget.uidesigner.ChangeIconWindow;
import org.openremote.modeler.client.widget.uidesigner.PropertyPanel;
import org.openremote.modeler.client.widget.uidesigner.SelectCommandWindow;
import org.openremote.modeler.domain.DeviceCommand;
import org.openremote.modeler.domain.DeviceCommandRef;
import org.openremote.modeler.domain.DeviceMacro;
import org.openremote.modeler.domain.DeviceMacroRef;
import org.openremote.modeler.domain.Group;
import org.openremote.modeler.domain.UICommand;
import org.openremote.modeler.domain.component.ImageSource;
import org.openremote.modeler.domain.component.Navigate;
import org.openremote.modeler.domain.component.UIButton;
import org.openremote.modeler.domain.component.Navigate.ToLogicalType;

import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.FieldSetEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.AdapterField;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.CheckBoxGroup;
import com.extjs.gxt.ui.client.widget.form.TextField;

/**
 * A panel for display screen button properties.
 */
public class ButtonPropertyForm extends PropertyForm {
   private CheckBox repeat = new CheckBox();
   private NavigateFieldSet navigateSet = null;
   
   public ButtonPropertyForm(ScreenButton screenButton, UIButton uiButton) {
      super(screenButton);
      addFields(screenButton, uiButton);
      super.addDeleteButton();
   }
   private void addFields(final ScreenButton screenButton, final UIButton uiButton) {
      // initial name field.
      final TextField<String> name = new TextField<String>();
      name.setFieldLabel("Name");
      name.setValue(uiButton.getName());
      name.addListener(Events.Blur, new Listener<BaseEvent>() {
         @Override
         public void handleEvent(BaseEvent be) {
            screenButton.setName(name.getValue());
         }
      });
      
      // initial command field.
      final Button command = new Button("Select");
      command.addSelectionListener(new SelectionListener<ButtonEvent>() {
         @Override
         public void componentSelected(ButtonEvent ce) {
            SelectCommandWindow selectCommandWindow = new SelectCommandWindow();
            selectCommandWindow.addListener(SubmitEvent.SUBMIT, new SubmitListener() {
               @Override
               public void afterSubmit(SubmitEvent be) {
                  BeanModel dataModel = be.<BeanModel> getData();
                  UICommand uiCommand = null;
                  if (dataModel.getBean() instanceof DeviceCommand) {
                     uiCommand = new DeviceCommandRef((DeviceCommand) dataModel.getBean());
                  } else if (dataModel.getBean() instanceof DeviceMacro) {
                     uiCommand = new DeviceMacroRef((DeviceMacro) dataModel.getBean());
                  }
                  uiButton.setUiCommand(uiCommand);
                  command.setText(uiCommand.getDisplayName());
               }
            });
         }
      });
      if (uiButton.getUiCommand() != null) {
         command.setText(uiButton.getUiCommand().getDisplayName());
      }
      AdapterField adapterCommand = new AdapterField(command);
      adapterCommand.setFieldLabel("Command");
      
      // initial navigate properties
      final Navigate navigate = uiButton.getNavigate();
      Group parentGroup = screenButton.getScreenCanvas().getScreen().getScreenPair().getParentGroup();
      if (parentGroup != null) {
         navigateSet = new NavigateFieldSet(navigate, parentGroup.getParentPanel().getGroups());
      } else {
         navigateSet = new NavigateFieldSet(navigate, new ArrayList<Group>());
      }
      navigateSet.setCheckboxToggle(true);
      navigateSet.addListener(Events.BeforeExpand, new Listener<FieldSetEvent>() {
         @Override
         public void handleEvent(FieldSetEvent be) {
            uiButton.setRepeate(false);
            repeat.setValue(false);
            if (!navigate.isSet()) {
               navigate.setToLogical(ToLogicalType.login);
            }
            navigateSet.update(navigate);
         }
         
      });
      navigateSet.addListener(Events.BeforeCollapse, new Listener<FieldSetEvent>() {
         @Override
         public void handleEvent(FieldSetEvent be) {
            navigate.clear();
         }
      });
      if (navigate.isSet()) {
         navigateSet.fireEvent(Events.BeforeExpand);
      } else {
         navigateSet.collapse();
      }
      
      final ImageSelectAdapterField defaultImageField = new ImageSelectAdapterField("Image");
      if (uiButton.getImage() != null) {
         defaultImageField.setText(uiButton.getImage().getImageFileName());
      }
      defaultImageField.addSelectionListener(new SelectionListener<ButtonEvent>() {
         @Override
         public void componentSelected(ButtonEvent ce) {
            final ImageSource image = uiButton.getImage();
            ChangeIconWindow selectImageONWindow = new ChangeIconWindow(createIconPreviewWidget(screenButton, image), screenButton.getWidth());
            selectImageONWindow.addListener(SubmitEvent.SUBMIT, new SubmitListener() {
               @Override
               public void afterSubmit(SubmitEvent be) {
                  String imageUrl = be.getData();
                  screenButton.setIcon(imageUrl);
                  if (image != null) {
                     image.setSrc(imageUrl);
                  } else {
                     uiButton.setImage(new ImageSource(imageUrl));
                  }
                  defaultImageField.setText(uiButton.getImage().getImageFileName());
               }
            });
         }
      });
      defaultImageField.addDeleteListener(new SelectionListener<ButtonEvent>() {
         @Override
         public void componentSelected(ButtonEvent ce) {
            if (uiButton.getImage() != null) {
               defaultImageField.removeImageText();
               uiButton.setImage(null);
               screenButton.removeIcon();
            }
         }
      });
      
      final ImageSelectAdapterField pressImageField = new ImageSelectAdapterField("PressImage");
      if (uiButton.getPressImage() != null) {
         pressImageField.setText(uiButton.getPressImage().getImageFileName());
      }
      pressImageField.addSelectionListener(new SelectionListener<ButtonEvent>() {
         @Override
         public void componentSelected(ButtonEvent ce) {
            final ImageSource onPressImage = uiButton.getPressImage();
            ChangeIconWindow selectImageONWindow = new ChangeIconWindow(createIconPreviewWidget(screenButton, onPressImage), screenButton.getWidth());
            selectImageONWindow.addListener(SubmitEvent.SUBMIT, new SubmitListener() {
               @Override
               public void afterSubmit(SubmitEvent be) {
                  String onPressImageUrl = be.getData();
                  if (onPressImage != null) {
                     onPressImage.setSrc(onPressImageUrl);
                  } else {
                     uiButton.setPressImage(new ImageSource(onPressImageUrl));
                  }
                  pressImageField.setText(uiButton.getPressImage().getImageFileName());
               }
            });
         }
      });
      pressImageField.addDeleteListener(new SelectionListener<ButtonEvent>() {
         @Override
         public void componentSelected(ButtonEvent ce) {
            if (uiButton.getPressImage() != null) {
               pressImageField.removeImageText();
               uiButton.setPressImage(null);
            }
         }
      });
      
      CheckBoxGroup repeatCheckBoxGroup = new CheckBoxGroup();
      repeatCheckBoxGroup.setFieldLabel("Repeat");
      repeat.setValue(uiButton.isRepeate());
      repeat.addListener(Events.Change, new Listener<FieldEvent>() {
         @Override
         public void handleEvent(FieldEvent be) {
            if (repeat.getValue() == true) {
               if (uiButton.getNavigate() != null) {
                  uiButton.getNavigate().setToGroup(-1L);
                  uiButton.getNavigate().setToLogical(null);
               }
            }
            navigateSet.collapse();
            uiButton.setRepeate(repeat.getValue());
         }
      });
      repeatCheckBoxGroup.add(repeat); 
      add(name);
      add(adapterCommand);
      add(defaultImageField);
      add(pressImageField);
      add(repeatCheckBoxGroup);
      add(navigateSet);
      
   }
   /**
    * @param screenButton
    * @param imageSource
    * @return
    */
   private IconPreviewWidget createIconPreviewWidget(final ScreenButton screenButton, final ImageSource imageSource) {
      IconPreviewWidget previewWidget = new IconPreviewWidget(screenButton.getWidth(), screenButton.getHeight());
      previewWidget.setText(screenButton.getName());
      if (imageSource != null) {
         previewWidget.setIcon(imageSource.getSrc());
      }
      return previewWidget;
   }
   @Override
   protected void afterRender() {
      super.afterRender();
      ((PropertyPanel)this.getParent()).setHeading("Button properties");
   }
   
}
