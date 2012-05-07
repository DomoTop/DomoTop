package org.openremote.modeler.domain;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

import org.openremote.modeler.client.utils.IDUtil;

@Entity
@Table(name = "clientgroup")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "dtype")
@DiscriminatorValue("CLIENT_GROUP")
public class ClientGroup extends BusinessEntity 
{
	private static final long serialVersionUID = -2459473632461178339L;
	private String name;
		
	@SuppressWarnings("unused")
	private ClientGroup() {
		super();
	}
	
	public ClientGroup(String name) {
		super();
		this.setName(name);
		this.setOid(IDUtil.nextID());
	}
		
	@Column(nullable = false)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
