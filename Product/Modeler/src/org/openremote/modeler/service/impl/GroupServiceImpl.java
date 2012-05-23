package org.openremote.modeler.service.impl;

import java.util.List;

import org.hibernate.Hibernate;
import org.openremote.modeler.domain.ClientGroup;
import org.openremote.modeler.service.BaseAbstractService;
import org.openremote.modeler.service.GroupService;
import org.openremote.modeler.service.UserService;
/**
 * Class to handle all the groups to this account
 * @author <a href="mailto:vincent.kriek@tass.nl">Vincent Kriek</a>
 */
public class GroupServiceImpl extends BaseAbstractService<ClientGroup> implements GroupService {
	private UserService userService;
	/**
	 * Add a group to the list of groups used in this session
	 * @param group The group you want to add
	 * @return The group that was added to the list
	 */
	public ClientGroup add(ClientGroup group) {
		group.setAccount(userService.getAccount());
		genericDAO.save(group);
		Hibernate.initialize(group);
		return group;
	}
   /**
    * Retrieve a list with all the groups currently in use.
    * @return All the groups
    */
	public List<ClientGroup> getAll() {
		List<ClientGroup> groups = userService.getAccount().getGroups();
		return groups;
	}
	/**
	 * Deletes a clientgroup 
	 * @param group The group you want to delete
	 */
	public ClientGroup delete(ClientGroup group) {
		genericDAO.delete(group);
		return group;
	}
	
	public void setUserService(UserService service) {
		this.userService = service;
	}
}
