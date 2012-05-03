package org.openremote.modeler.domain;

import java.util.ArrayList;
import java.util.List;

public class ClientGroupList {
	private static ClientGroupList instance = null;
	private List<ClientGroup> groups = new ArrayList<ClientGroup>();
	
	/**
	 * Private constructor, use getInstance()
	 */
	private ClientGroupList() {
	}
	
	public void add(ClientGroup group) {
		groups.add(group);
	}
	
	public ClientGroup get(int id) {
		return groups.get(id);
	}
	
	public List<ClientGroup> getAll() {
		return groups;
	}
	
	public static ClientGroupList getInstance() {
		if(instance == null) {
			instance = new ClientGroupList();
			instance.add(new ClientGroup("test"));
			instance.add(new ClientGroup("test1"));
			instance.add(new ClientGroup("test2"));
			instance.add(new ClientGroup("test3"));
		}
		return instance;
	}
}
