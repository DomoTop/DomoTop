package org.openremote.modeler.client.rpc;

import java.util.List;

import org.openremote.modeler.domain.ClientGroup;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
/**
 * A class representing a Remote Procedure call to handle Groups
 * @author <a href="mailto:vincent.kriek@tass.nl">Vincent Kriek</a>
 */
@RemoteServiceRelativePath("group.smvc")
public interface GroupRPCService extends RemoteService {
	/**
	 * Add a group to the list
	 */
	ClientGroup add(ClientGroup group);
	/**
	 * Retrieve all the groups
	 * @return A list containing all the groups
	 */
	List<ClientGroup> loadAll();
}