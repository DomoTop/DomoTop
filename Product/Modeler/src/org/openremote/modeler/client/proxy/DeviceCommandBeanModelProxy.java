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
package org.openremote.modeler.client.proxy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openremote.modeler.client.Constants;
import org.openremote.modeler.client.rpc.AsyncServiceFactory;
import org.openremote.modeler.client.rpc.AsyncSuccessCallback;
import org.openremote.modeler.client.widget.buildingmodeler.DeviceCommandWindow;
import org.openremote.modeler.domain.Device;
import org.openremote.modeler.domain.DeviceCommand;
import org.openremote.modeler.domain.Protocol;
import org.openremote.modeler.domain.ProtocolAttr;

import com.extjs.gxt.ui.client.data.BeanModel;
import com.extjs.gxt.ui.client.data.ModelData;


/**
 * The Class DeviceCommandBeanModelProxy.
 */
public class DeviceCommandBeanModelProxy {
   
   /**
    * Not be instantiated.
    */
   private DeviceCommandBeanModelProxy() {
   }

   /**
    * Save device command.
    * 
    * @param device the device
    * @param map the map
    * @param callback the callback
    */
   public static void saveDeviceCommand(Device device, Map<String, String> map, final AsyncSuccessCallback<BeanModel> callback) {
      DeviceCommand deviceCommand = new DeviceCommand();
      deviceCommand.setName(map.get(DeviceCommandWindow.DEVICE_COMMAND_NAME));
      deviceCommand.setDevice(device);
      deviceCommand.setProtocol(careateProtocol(map, deviceCommand));
      device.getDeviceCommands().add(deviceCommand);
      
      AsyncServiceFactory.getDeviceCommandServiceAsync().save(deviceCommand, new AsyncSuccessCallback<DeviceCommand>() {
         public void onSuccess(DeviceCommand deviceCommand) {
            BeanModel deviceCommandModel = deviceCommand.getBeanModel();
            BeanModelDataBase.deviceCommandTable.insert(deviceCommandModel);
            callback.onSuccess(deviceCommandModel);
         }
      });
   }

   /**
    * @param map
    * @param deviceCommand
    * @return
    */
   public static Protocol careateProtocol(Map<String, String> map, DeviceCommand deviceCommand) {
      Protocol protocol = new Protocol();
      protocol.setType(map.get(DeviceCommandWindow.DEVICE_COMMAND_PROTOCOL));
      protocol.setDeviceCommand(deviceCommand);
      
      for (String key : map.keySet()) {
         if (DeviceCommandWindow.DEVICE_COMMAND_NAME.equals(key) || DeviceCommandWindow.DEVICE_COMMAND_PROTOCOL.equals(key)) {
            continue;
         }
         ProtocolAttr protocolAttr = new ProtocolAttr();
         protocolAttr.setName(key);
         protocolAttr.setValue((map.get(key)));
         protocolAttr.setProtocol(protocol);
         protocol.getAttributes().add(protocolAttr);
      }
      return protocol;
   }
   
   /**
    * Update device command.
    * 
    * @param deviceCommand the device command
    * @param map the map
    * @param callback the callback
    */
   public static void updateDeviceCommand(final DeviceCommand deviceCommand, Map<String, String> map,
         final AsyncSuccessCallback<BeanModel> callback) {
      deviceCommand.setName(map.get(DeviceCommandWindow.DEVICE_COMMAND_NAME));
      deviceCommand.setProtocol(careateProtocol(map, deviceCommand));
      AsyncServiceFactory.getDeviceCommandServiceAsync().update(deviceCommand, new AsyncSuccessCallback<DeviceCommand>() {
         public void onSuccess(DeviceCommand result) {
            BeanModel deviceCommandModel = result.getBeanModel();
            BeanModelDataBase.deviceCommandTable.update(deviceCommandModel);
            callback.onSuccess(deviceCommandModel);
         }
      });
   }
   
   /**
    * Save all device commands.
    * 
    * @param device the device
    * @param datas the datas
    * @param callback the callback
    */
   public static void saveAllDeviceCommands(Device device, List<ModelData> datas, final AsyncSuccessCallback<List<BeanModel>> callback) {
      List<DeviceCommand> deviceCommands = convert2DeviceCommand(device, datas);
      AsyncServiceFactory.getDeviceCommandServiceAsync().saveAll(deviceCommands, new AsyncSuccessCallback<List<DeviceCommand>>() {
         public void onSuccess(List<DeviceCommand> deviceCommands) {
            List<BeanModel> deviceCommandModels = DeviceCommand.createModels(deviceCommands);
            BeanModelDataBase.deviceCommandTable.insertAll(deviceCommandModels);
            callback.onSuccess(deviceCommandModels);
         }
      });
   }
   
   /**
    * Convert to device command.
    * 
    * @param device
    *           the device
    * @param datas
    *           the datas
    * 
    * @return the list< device command>
    */
   public static List<DeviceCommand> convert2DeviceCommand(Device device, List<ModelData> datas) {
      List<DeviceCommand> deviceCommands = new ArrayList<DeviceCommand>();
      for (ModelData m : datas) {
         Protocol protocol = new Protocol();
         protocol.setType(Constants.INFRARED_TYPE);

         ProtocolAttr nameAttr = new ProtocolAttr();
         nameAttr.setName("name");
         nameAttr.setValue(m.get("remoteName").toString());
         nameAttr.setProtocol(protocol);
         protocol.getAttributes().add(nameAttr);

         ProtocolAttr commandAttr = new ProtocolAttr();
         commandAttr.setName("command");
         commandAttr.setValue(m.get("name").toString());
         commandAttr.setProtocol(protocol);
         protocol.getAttributes().add(commandAttr);

         DeviceCommand deviceCommand = new DeviceCommand();
         deviceCommand.setDevice(device);
         deviceCommand.setProtocol(protocol);
         deviceCommand.setName(m.get("name").toString());
         deviceCommand.setSectionId(m.get("sectionId").toString());

         protocol.setDeviceCommand(deviceCommand);

         device.getDeviceCommands().add(deviceCommand);

         deviceCommands.add(deviceCommand);
      }
      return deviceCommands;
   }
   
   /**
    * Delete device command.
    * 
    * @param deviceCommnadModel the device commnad model
    * @param callback the callback
    */
   public static void deleteDeviceCommand(BeanModel deviceCommnadModel, final AsyncSuccessCallback<Boolean> callback) {
      final DeviceCommand deviceCommand = deviceCommnadModel.getBean();
      AsyncServiceFactory.getDeviceCommandServiceAsync().deleteCommand(deviceCommand.getOid(), new AsyncSuccessCallback<Boolean>() {
         public void onSuccess(Boolean result) {
            if (result) {
               BeanModelDataBase.deviceCommandTable.delete(deviceCommand.getOid());
            }
            callback.onSuccess(result);
         }
      });
   }
   
   /**
    * Load all the device commands a device has. 
    * @param device
    * @param callback
    */
   public static void loadDeviceCmdFromDevice(Device device,final AsyncSuccessCallback<List<DeviceCommand>>callback){
      AsyncServiceFactory.getDeviceCommandServiceAsync().loadByDevice(device.getOid(), new AsyncSuccessCallback<List<DeviceCommand>>(){

         @Override
         public void onSuccess(List<DeviceCommand> result) {
            BeanModelDataBase.deviceCommandTable.insertAll(DeviceCommand.createModels(result));
            callback.onSuccess(result);
         }
         
      });
   }
}
