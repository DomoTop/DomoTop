package org.openremote.modeler.server;

import java.util.List;
import java.util.ArrayList;

import org.openremote.modeler.client.rpc.GroupRPCService;
import org.openremote.modeler.domain.ClientGroup;
import org.openremote.modeler.service.GroupService;

import com.extjs.gxt.ui.client.widget.grid.ColumnHeader.Group;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

public class GroupController extends BaseGWTSpringControllerWithHibernateSupport implements GroupRPCService  {
	private GroupService groupService;
	
	public ClientGroup add(ClientGroup group) {
		groupService.add(group);
		return group;
	}
	
	public List<ClientGroup> loadAll() {
		return groupService.getAll();
	}
	
	public void setGroupService(GroupService groupService) {
		this.groupService = groupService;
	}
}