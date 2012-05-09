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

import java.util.List;

import org.openremote.modeler.client.rpc.AsyncServiceFactory;
import org.openremote.modeler.client.rpc.AsyncSuccessCallback;
import org.openremote.modeler.domain.ClientGroup;
import org.openremote.modeler.domain.Device;

import com.extjs.gxt.ui.client.data.BeanModel;

public class GroupBeanModelProxy {
   private GroupBeanModelProxy() {
   }
	/**
	 * Add a group to the list
	 */
   public static void add(final ClientGroup group, final AsyncSuccessCallback<ClientGroup> callback){
      if (group != null) {
         AsyncServiceFactory.getGroupRPCServiceAsync().add(group, new AsyncSuccessCallback<ClientGroup>() {

				@Override
				public void onSuccess(ClientGroup result) {
					callback.onSuccess(result);
				}

               });
      }
   }
	/**
	 * Retrieve all the groups
	 * @return A list containing all the groups
	 */
   public static void getAll(final AsyncSuccessCallback<List<ClientGroup>> callback){
	   AsyncServiceFactory.getGroupRPCServiceAsync().loadAll(new AsyncSuccessCallback<List<ClientGroup>>() {
			@Override
			public void onSuccess(List<ClientGroup> result) {
				callback.onSuccess(result);
			}	   
	   });
   }
}
