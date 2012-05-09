package org.openremote.modeler.server;

import java.util.List;
import java.util.ArrayList;

import org.openremote.modeler.client.rpc.GroupRPCService;
import org.openremote.modeler.domain.ClientGroup;
import org.openremote.modeler.service.GroupService;

import com.extjs.gxt.ui.client.widget.grid.ColumnHeader.Group;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * Class to handle request to the GroupRPC service
 * @author <a href="mailto:vincent.kriek@tass.nl">Vincent Kriek</a>
 */
public class GroupController extends BaseGWTSpringControllerWithHibernateSupport implements GroupRPCService  {
	private GroupService groupService;
	/**
	 * Add a group to the list
	 */
	public ClientGroup add(ClientGroup group) {
		groupService.add(group);
		return group;
	}
	/**
	 * Retrieve all the groups
	 * @return A list containing all the groups
	 */
	public List<ClientGroup> loadAll() {
		return groupService.getAll();
	}
	/**
	 * Link the groupService to this controller, used in magic by tomcat, don't call.
	 * @param groupService The groupser
	 */
	public void setGroupService(GroupService groupService) {
		this.groupService = groupService;
	}
}