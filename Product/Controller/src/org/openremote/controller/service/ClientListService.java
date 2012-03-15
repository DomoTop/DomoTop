package org.openremote.controller.service;

import java.util.List;

import org.openremote.controller.model.Client;

public interface ClientListService {
   
   List<Client> getClientList(String rootDir);
}
