package org.openremote.modeler.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.openremote.modeler.domain.ClientGroup;
import org.openremote.modeler.service.BaseAbstractService;
import org.openremote.modeler.service.GroupService;

public class GroupServiceImpl extends BaseAbstractService<ClientGroup> implements GroupService {
	private static String SERIALIZED_FILE = "test.list";
	
	private List<ClientGroup> groups = new ArrayList<ClientGroup>();
	
	/**
	 * Private constructor, use getInstance()
	 */
	public GroupServiceImpl() {
		ArrayList<ClientGroup> serialized = readList();
		if(serialized != null) {
			groups = serialized;
		}
	}
	
	public void add(ClientGroup group) {
		groups.add(group);
		writeList();
	}
	
	public ClientGroup get(int id) {
		return groups.get(id);
	}
	
	public List<ClientGroup> getAll() {
		System.out.println(groups.size());

		return groups;
	}
	
	private void writeList() {
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(new FileOutputStream(new File(SERIALIZED_FILE)));
			oos.writeObject(groups);
		} catch (IOException e) {
			//Need logger
		} finally {
			if(oos != null) {
				try {
					oos.close();
				} catch (IOException e) {
					//Than shit is drama
				}
			}
		}
	}
	
	private ArrayList<ClientGroup> readList() {
		ObjectInputStream ois = null;
		Object o = null;
		try {
			ois = new ObjectInputStream(new FileInputStream(new File(SERIALIZED_FILE)));
			o = ois.readObject();
			ois.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		if(o != null && o instanceof ArrayList<?>) {
			return (ArrayList<ClientGroup>) o;
		} else {
			return null;
		}
	}
}
