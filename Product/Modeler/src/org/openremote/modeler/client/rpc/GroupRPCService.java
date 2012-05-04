package org.openremote.modeler.client.rpc;

import java.util.List;

import org.openremote.modeler.domain.ClientGroup;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("group.smvc")
public interface GroupRPCService extends RemoteService {

	void add(ClientGroup group);
}