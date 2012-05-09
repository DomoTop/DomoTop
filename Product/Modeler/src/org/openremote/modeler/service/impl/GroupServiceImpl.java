package org.openremote.modeler.service.impl;

import java.util.List;

import org.hibernate.Hibernate;
import org.openremote.modeler.domain.ClientGroup;
import org.openremote.modeler.service.BaseAbstractService;
import org.openremote.modeler.service.GroupService;
/**
 * Class to handle all the groups to this account
 * @author <a href="mailto:vincent.kriek@tass.nl">Vincent Kriek</a>
 */
public class GroupServiceImpl extends BaseAbstractService<ClientGroup> implements GroupService {
	/**
	 * Add a group to the list of groups used in this session
	 * @param group The group you want to add
	 * @return The group that was added to the list
	 */
	public ClientGroup add(ClientGroup group) {
		genericDAO.save(group);
		Hibernate.initialize(group);
		return group;
	}
   /**
    * Retrieve a list with all the groups currently in use.
    * @return All the groups
    */
	public List<ClientGroup> getAll() {
		List<ClientGroup> groups = genericDAO.loadAll(ClientGroup.class);
		return groups;
	}
}
