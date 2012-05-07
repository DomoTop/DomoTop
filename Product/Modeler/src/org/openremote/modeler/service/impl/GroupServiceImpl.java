package org.openremote.modeler.service.impl;

import java.util.List;

import org.hibernate.Hibernate;
import org.openremote.modeler.domain.ClientGroup;
import org.openremote.modeler.service.BaseAbstractService;
import org.openremote.modeler.service.GroupService;

public class GroupServiceImpl extends BaseAbstractService<ClientGroup> implements GroupService {

	public ClientGroup add(ClientGroup group) {
		genericDAO.save(group);
		Hibernate.initialize(group);
		return group;
	}
	
	public List<ClientGroup> getAll() {
		List<ClientGroup> groups = genericDAO.loadAll(ClientGroup.class);
		return groups;
	}
}
