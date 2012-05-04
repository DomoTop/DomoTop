package org.openremote.modeler.domain;

public class MyGroup extends BusinessEntity {
	private String name;
	
	public MyGroup(String name) {
		this.setName(name);
	}
	
	public MyGroup(String name, int i) {
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
