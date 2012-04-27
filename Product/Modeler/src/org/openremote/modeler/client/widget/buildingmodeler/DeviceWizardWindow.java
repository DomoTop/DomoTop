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

import java.util.Map;

import org.openremote.modeler.client.widget.CommonForm;
import org.openremote.modeler.client.widget.WizardWindow;
import org.openremote.modeler.domain.Device;
import org.openremote.modeler.domain.DeviceCommand;

import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.widget.form.FormPanel;


/**
 * Device Wizard Window for create a new device, including basic info and basic {@link DeviceCommand} import.
 * 
 * @author Dan 2009-8-21
 */
public class DeviceWizardWindow extends WizardWindow {


   /** The Step index DEVICE_INFO_STEP. */
   public static final int DEVICE_INFO_STEP = 0;
   
   /** The Step index IMPORT_IR_STEP. */
   public static final int IMPORT_IR_STEP = 1;
   
   
   /**
    * Instantiates a new device wizard window.
    * 
    * @param deviceBeanModel
    *           the device bean model
    */
   public DeviceWizardWindow(BeanModel deviceBeanModel) {
      super(deviceBeanModel);
      Device device = deviceBeanModel.getBean();
      session.put("device", device);
      setHeading("New Device");
      show();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected void initForms() {
      forms = new CommonForm[]{
            new DeviceInfoWizardForm(this, beanModel),
            new DeviceContentWizardForm(this, beanModel)
            };
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected void postProcess(int step, FormPanel currentForm) {
      switch (step) {
      case DEVICE_INFO_STEP:
         
         break;
      case IMPORT_IR_STEP:
         
         break;

      default:
         break;
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected void finish(int step, FormPanel currentForm) {
      DeviceContentWizardForm commandImportWizardForm = (DeviceContentWizardForm) forms[IMPORT_IR_STEP];
      DeviceInfoWizardForm deviceInfoWizardForm = (DeviceInfoWizardForm) forms[DEVICE_INFO_STEP];
      switch (step) {
      case DEVICE_INFO_STEP:
         
         break;
      case IMPORT_IR_STEP:
         Map<String, String> map = deviceInfoWizardForm.getFieldMap();
         Device device = commandImportWizardForm.getDevice();
         device.setName(map.get(DeviceInfoForm.DEVICE_NAME));
         device.setModel(map.get(DeviceInfoForm.DEVICE_MODEL));
         device.setVendor(map.get(DeviceInfoForm.DEVICE_VENDOR));
         commandImportWizardForm.setDevice(device);
         break;

      default:
         break;
      }
      super.finish(step, currentForm);
      
   }

   


}
