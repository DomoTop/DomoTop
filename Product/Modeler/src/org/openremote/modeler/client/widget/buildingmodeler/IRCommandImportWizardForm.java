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

import java.util.List;

import org.openremote.modeler.client.event.SubmitEvent;
import org.openremote.modeler.client.proxy.DeviceBeanModelProxy;
import org.openremote.modeler.client.rpc.AsyncSuccessCallback;

import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FormEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Window;

/**
 * WizardForm for {@link IRCommandImportForm}, this is a part of {@link DeviceWizardWindow}.
 * 
 * @author Dan 2009-8-21
 */
public class IRCommandImportWizardForm extends IRCommandImportForm {

   
   /**
    * Instantiates a new iR command import wizard form.
    * 
    * @param wrapper
    *           the wrapper
    * @param deviceBeanModel
    *           the device bean model
    */
   public IRCommandImportWizardForm(Component wrapper, BeanModel deviceBeanModel) {
      super(wrapper, deviceBeanModel);
      setHeight(330);
      
   }

   /* (non-Javadoc)
    * @see org.openremote.modeler.client.widget.CommonForm#isNoButton()
    */
   @Override
   public boolean isNoButton() {
      return true;
   }

   /* (non-Javadoc)
    * @see com.extjs.gxt.ui.client.widget.Component#show()
    */
   @Override
   public void show() {
      super.show();
      ((Window) wrapper).setSize(600, 330);
   }

   /* (non-Javadoc)
    * @see org.openremote.modeler.client.widget.buildingmodeler.IRCommandImportForm#onSubmit(com.extjs.gxt.ui.client.widget.Component)
    */
   @Override
   protected void onSubmit(final Component wrapper) {
      addListener(Events.BeforeSubmit, new Listener<FormEvent>() {
         public void handleEvent(FormEvent be) {
            wrapper.mask("Please Wait...");
            if (codeGrid != null) {
               List<ModelData> modelDatas = codeGrid.getSelectionModel().getSelectedItems();
               if (modelDatas.isEmpty()) {
                  modelDatas = codeGrid.getStore().getModels();
               }
               for (ModelData modelData : modelDatas) {
                  modelData.set("sectionId", getSectionId());
               }
               AsyncSuccessCallback<BeanModel> callback = new AsyncSuccessCallback<BeanModel>() {
                  @Override
                  public void onSuccess(BeanModel deviceModel) {
                     wrapper.fireEvent(SubmitEvent.SUBMIT, new SubmitEvent(deviceModel));
                  }

               };
               if (getDevice().getOid() == 0L) {
                  DeviceBeanModelProxy.saveDeviceWithCommands(getDevice(), modelDatas, callback);
               } else {
                  //TODO update function
               }
            
            } else {
               MessageBox.alert("Warn", "Please select vendor, model first.", null);
               wrapper.unmask();
            }
         }
         
      });
   }
   
   
   

}
