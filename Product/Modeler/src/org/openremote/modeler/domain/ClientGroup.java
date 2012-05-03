package org.openremote.modeler.domain;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.openremote.modeler.client.utils.IDUtil;

@SuppressWarnings("serial")
@Entity
@Table(name = "clientgroup")
public class ClientGroup extends BusinessEntity {
	private String name;
	
	public ClientGroup(String name) {
		this.setName(name);
		this.setOid(IDUtil.nextID());
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
