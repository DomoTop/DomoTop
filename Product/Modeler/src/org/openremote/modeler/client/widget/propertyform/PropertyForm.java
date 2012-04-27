/*
 * OpenRemote, the Home of the Digital Home. Copyright 2008-2009, OpenRemote Inc.
 * 
 * See the contributors.txt file in the distribution for a full listing of individual contributors.
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package org.openremote.modeler.client.widget.propertyform;

import org.openremote.modeler.client.event.WidgetDeleteEvent;
import org.openremote.modeler.client.icon.Icons;
import org.openremote.modeler.client.utils.PropertyEditable;
import org.openremote.modeler.client.utils.WidgetSelectionUtil;
import org.openremote.modeler.client.widget.component.ScreenTabbar;
import org.openremote.modeler.client.widget.component.ScreenTabbarItem;
import org.openremote.modeler.client.widget.uidesigner.ComponentContainer;
import org.openremote.modeler.client.widget.uidesigner.GridLayoutContainerHandle;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.google.gwt.core.client.GWT;

/**
 * The PropertyForm initialize the property form display style.
 */
public class PropertyForm extends FormPanel {
   private ComponentContainer componentContainer;

   public PropertyForm(ComponentContainer componentContainer) {
      this.componentContainer = componentContainer;
      setFrame(true);
      setHeaderVisible(false);
      setBorders(false);
      setBodyBorder(false);
      setPadding(2);
      setLabelWidth(90);
      setFieldWidth(150);
      setScrollMode(Scroll.AUTO);
   }

   protected void addDeleteButton() {
      if (componentContainer instanceof ComponentContainer) {
         final ComponentContainer componentContainer = (ComponentContainer) this.componentContainer;
         Button deleteButton = new Button("Delete From Screen");
         deleteButton.setIcon(((Icons) GWT.create(Icons.class)).delete());
         deleteButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
            public void componentSelected(ButtonEvent ce) {
               MessageBox.confirm("Delete", "Are you sure you want to delete?", new Listener<MessageBoxEvent>() {
                  public void handleEvent(MessageBoxEvent be) {
                     if (be.getButtonClicked().getItemId().equals(Dialog.YES)) {
                        if (componentContainer instanceof GridLayoutContainerHandle
                              || componentContainer instanceof ScreenTabbarItem
                              || componentContainer instanceof ScreenTabbar) {
                           componentContainer.fireEvent(WidgetDeleteEvent.WIDGETDELETE, new WidgetDeleteEvent());
                        } else {
                           ((ComponentContainer) componentContainer.getParent()).fireEvent(
                                 WidgetDeleteEvent.WIDGETDELETE, new WidgetDeleteEvent());
                        }
                        WidgetSelectionUtil.setSelectWidget(null);
                     }
                  }
               });
            }

         });
         add(deleteButton);
      }
   }
   
   public PropertyForm(PropertyEditable componentContainer) {
      setFrame(true);
      setHeaderVisible(false);
      setBorders(false);
      setBodyBorder(false);
      setPadding(2);
      setLabelWidth(60);
      setFieldWidth(100);
      setScrollMode(Scroll.AUTO);
   }
}
