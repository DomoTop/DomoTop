package org.openremote.android.console.bindings;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class ORGroup extends BusinessEntity {
	private String name;
	private long oid;
	
	public ORGroup(String name, long oid) {
		this.setName(name);
		this.setOid(oid);
	}

	public ORGroup(Node elementNode) {
		parse(elementNode);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getOid() {
		return oid;
	}

	public void setOid(long oid) {
		this.oid = oid;
	}
	
	private void parse(Node node) {
		NamedNodeMap nodeMap = node.getAttributes();
  	   	this.setOid(Integer.valueOf(nodeMap.getNamedItem(ID).getNodeValue()));
  	   	this.setName(nodeMap.getNamedItem(NAME).getNodeValue());
	}
}
