package org.openremote.modeler.server;

import java.util.List;

import org.openremote.modeler.client.rpc.GroupRPCService;
import org.openremote.modeler.domain.ClientGroup;
import org.openremote.modeler.domain.ClientGroupList;

import com.extjs.gxt.ui.client.widget.grid.ColumnHeader.Group;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

public class GroupController extends BaseGWTSpringControllerWithHibernateSupport implements GroupRPCService  {
	
	public void add(ClientGroup group) {
		//ClientGroupList.getInstance().add(group);
		//clientService.save(group);
	}
}