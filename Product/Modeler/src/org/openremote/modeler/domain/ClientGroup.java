package org.openremote.modeler.domain;

public class ClientGroup extends BusinessEntity {
	private String name;
	
	public ClientGroup(String name) {
		this.setName(name);
	}
	
	public ClientGroup(String name, int i) {
		this.setName(name);
		this.setOid(i);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
