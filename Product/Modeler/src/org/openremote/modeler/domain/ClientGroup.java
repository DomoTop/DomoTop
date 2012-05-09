package org.openremote.modeler.domain;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.openremote.modeler.client.utils.IDUtil;

import flexjson.JSON;

/**
 * A class which represents a Group. These groups can be linked to a UIComponent, to enforce rights on these components
 * @author <a href="mailto:vincent.kriek@tass.nl">Vincent Kriek</a>
 */
@Entity
@Table(name = "clientgroup")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "dtype")
@DiscriminatorValue("CLIENT_GROUP")
public class ClientGroup extends BusinessEntity 
{
	private static final long serialVersionUID = -2459473632461178339L;
	private String name;
	private Account account;
		
	@SuppressWarnings("unused")
	private ClientGroup() {
		super();
	}
	
	/**
	 * Instantiate a ClientGroup using a new unique ID and given name
	 * @param name The name of the new group
	 */
	public ClientGroup(String name) {
		super();
		this.setName(name);
		this.setOid(IDUtil.nextID());
	}
	
	/**
	 * @return The name of the group
	 */
	@Column(nullable = false)
	public String getName() {
		return name;
	}

	/**
	 * Set a new name to the group
	 * @param name the new name
	 */
	public void setName(String name) {
		this.name = name;
	}

    @Override 
    public int hashCode() {
       return (int) getOid();
    }
    
    @ManyToOne
    @JSON(include = false)
    public Account getAccount() {
       return account;
    }

    public void setAccount(Account account) {
       this.account = account;
    }
}
