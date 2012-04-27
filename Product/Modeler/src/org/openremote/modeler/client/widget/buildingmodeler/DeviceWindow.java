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

import org.openremote.modeler.client.widget.CommonForm;
import org.openremote.modeler.client.widget.CommonWindow;
import org.openremote.modeler.domain.Device;
import org.openremote.modeler.selenium.DebugId;

import com.extjs.gxt.ui.client.data.BeanModel;


/**
 * The Class DeviceWindow.
 */
public class DeviceWindow extends CommonWindow {
   
   
   /** The device form. */
   private CommonForm deviceForm;
   /** The device model. */
   protected BeanModel deviceModel = null;
   
   
   /**
    * Instantiates a new device window.
    * 
    * @param deviceBeanModel the device model
    */
   public DeviceWindow(BeanModel deviceBeanModel) {
      super();
      this.deviceModel = deviceBeanModel;
      initial(deviceBeanModel);
      show();
      setFocusWidget(deviceForm.getFields().get(0));
   }
   
   /**
    * Initial.
    * 
    * @param deviceBeanModel the device bean model
    */
   protected void initial(BeanModel deviceBeanModel) {
      setSize(360, 200);
      setHeading(((Device) deviceBeanModel.getBean()).getName() == null ? "New Device" : "Edit Device");
      deviceForm = new DeviceInfoForm(this, deviceBeanModel);
      ensureDebugId(DebugId.NEW_DEVICE_WINDOW);
      add(deviceForm);
   }

}
