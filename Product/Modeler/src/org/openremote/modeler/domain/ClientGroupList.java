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
		System.out.println(groups.size());
	}
	
	public ClientGroup get(int id) {
		return groups.get(id);
	}
	
	public List<ClientGroup> getAll() {
		System.out.println(groups.size());

		return groups;
	}
	
	public static ClientGroupList getInstance() {
		if(instance == null) {
			System.out.println("\n\n\n\n\n\n\n\nNEW INSTANCE\n\n\n\n\n\n\n");
			instance = new ClientGroupList();
		}
		return instance;
	}
}
