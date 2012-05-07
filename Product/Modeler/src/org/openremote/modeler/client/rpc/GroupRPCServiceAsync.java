package org.openremote.modeler.client.rpc;

import java.util.List;

import org.openremote.modeler.domain.ClientGroup;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface GroupRPCServiceAsync  {
	
	void add(ClientGroup group, AsyncCallback<ClientGroup> callback);
	void loadAll(AsyncCallback<List<ClientGroup>> callback);
}