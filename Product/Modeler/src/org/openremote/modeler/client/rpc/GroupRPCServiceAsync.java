package org.openremote.modeler.client.rpc;

import java.util.List;

import org.openremote.modeler.domain.ClientGroup;

import com.google.gwt.user.client.rpc.AsyncCallback;
/**
 * A class representing a Remote Procedure call to handle Groups
 * @author <a href="mailto:vincent.kriek@tass.nl">Vincent Kriek</a>
 */
public interface GroupRPCServiceAsync  {
	/**
	 * Add a group to the list
	 */	
	void add(ClientGroup group, AsyncCallback<ClientGroup> callback);
	/**
	 * Retrieve all the groups
	 * @return A list containing all the groups
	 */
	void loadAll(AsyncCallback<List<ClientGroup>> callback);
	/**
	 * Deletes a clientgroup 
	 * @param group The group you want to delete
	 */
	void delete(ClientGroup group, AsyncCallback<ClientGroup> callback);
}