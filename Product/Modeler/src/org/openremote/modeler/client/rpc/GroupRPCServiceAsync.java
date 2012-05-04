package org.openremote.modeler.client.rpc;

import org.openremote.modeler.domain.ClientGroup;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface GroupRPCServiceAsync  {
	
	void add(ClientGroup group, AsyncCallback<Void> callback);
}