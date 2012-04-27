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
package org.openremote.modeler.client.listener;

import org.openremote.modeler.client.event.WidgetSelectChangeEvent;

import com.extjs.gxt.ui.client.event.Listener;

/**
 * The listener interface for receiving widgetSelectChange events.
 * The class that is interested in processing a widgetSelectChange
 * event implements this interface, and the object created
 * with that class is registered with a component using the
 * component's <code>addWidgetSelectChangeListener</code> method. When
 * the widgetSelectChange event occurs, that object's appropriate
 * method is invoked.
 * 
 * @see WidgetSelectChangeEvent
 */
public abstract class WidgetSelectChangeListener implements Listener<WidgetSelectChangeEvent> {

   @Override
   public void handleEvent(WidgetSelectChangeEvent be) {
      if (be.getType() == WidgetSelectChangeEvent.WIDGETSELECTCHANGEED) {
         changeSelect(be);
      }
   }
   
   public abstract void changeSelect(WidgetSelectChangeEvent be);

}
