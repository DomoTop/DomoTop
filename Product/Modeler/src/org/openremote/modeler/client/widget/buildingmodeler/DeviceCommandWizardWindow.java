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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openremote.modeler.client.event.SubmitEvent;
import org.openremote.modeler.client.model.ComboBoxDataModel;
import org.openremote.modeler.client.proxy.DeviceCommandBeanModelProxy;
import org.openremote.modeler.domain.Device;
import org.openremote.modeler.domain.DeviceCommand;

import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FormEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.form.Field;

public class DeviceCommandWizardWindow extends DeviceCommandWindow {

   public DeviceCommandWizardWindow(Device device) {
      super(device);
      form.removeAllListeners();
      onSubmit(device);
   }
   
   private void onSubmit(final Device device) {
      form.addListener(Events.BeforeSubmit, new Listener<FormEvent>() {
         @SuppressWarnings("unchecked")
         public void handleEvent(FormEvent be) {
            List<Field<?>> list = form.getFields();
            Map<String, String> attrMap = new HashMap<String, String>();
            for (Field<?> f : list) {
               if (DEVICE_COMMAND_PROTOCOL.equals(f.getName())) {
                  Field<BaseModelData> p = (Field<BaseModelData>) f;
                  attrMap.put(DEVICE_COMMAND_PROTOCOL, p.getValue().get(ComboBoxDataModel.getDisplayProperty())
                        .toString());
               } else {
                  if (f.getValue() != null && !"".equals(f.getValue().toString())) {
                     attrMap.put(f.getName(), f.getValue().toString());
                  }
               }
            }
            DeviceCommand deviceCommand = new DeviceCommand();
            deviceCommand.setName(attrMap.get(DEVICE_COMMAND_NAME));
            deviceCommand.setDevice(device);
            deviceCommand.setProtocol(DeviceCommandBeanModelProxy.careateProtocol(attrMap, deviceCommand));
            fireEvent(SubmitEvent.SUBMIT, new SubmitEvent(deviceCommand));
            if (hideWindow) {
               hide();
            } else {
               hideWindow = true;
            }
         }
      });
   }

}
