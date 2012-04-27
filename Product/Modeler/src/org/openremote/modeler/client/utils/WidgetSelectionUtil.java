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
package org.openremote.modeler.client.utils;

import org.openremote.modeler.client.event.WidgetSelectChangeEvent;
import org.openremote.modeler.client.listener.WidgetSelectChangeListener;
import org.openremote.modeler.client.widget.uidesigner.ComponentContainer;

/**
 * The SelectedWidgetContainer for store selected widget and fire widgetSelectChange event.
 */
public class WidgetSelectionUtil {

   private WidgetSelectionUtil() {
   }
   private static ComponentContainer currentSelectedWidget;
   private static WidgetSelectChangeListener widgetSelectChangeListener;
   
   public static void setChangeListener(WidgetSelectChangeListener listener) {
      widgetSelectChangeListener = listener;
   }
   
   public static void setSelectWidget(ComponentContainer selectedWidget) {
      if (currentSelectedWidget != null) {
         currentSelectedWidget.removeStyleName("button-border");
      }
      if (selectedWidget != null) {
         selectedWidget.addStyleName("button-border");
         
         // add tab index and focus it, for catch keyboard "delete" event in Firefox.
         if (selectedWidget.isRendered()) {
            selectedWidget.el().dom.setPropertyInt("tabIndex", 0);
         }
         selectedWidget.focus();
      }
      currentSelectedWidget = selectedWidget;
      widgetSelectChangeListener.handleEvent(new WidgetSelectChangeEvent(selectedWidget));
   }
   
}
