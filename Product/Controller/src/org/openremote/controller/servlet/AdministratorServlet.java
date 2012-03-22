/*
 * OpenRemote, the Home of the Digital Home.
 * Copyright 2008-2012, OpenRemote Inc.
 *
 * See the contributors.txt file in the distribution for a
 * full listing of individual contributors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.openremote.controller.servlet;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.openremote.controller.Constants;
import org.openremote.controller.ControllerConfiguration;
import org.openremote.controller.model.Client;
import org.openremote.controller.service.ClientService;
import org.openremote.controller.spring.SpringContext;
import org.openremote.controller.utils.ResultSetUtil;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.ObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateSequenceModel;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.List;

/**
 * This servlet is used to show the list of clients, 
 * manage the clients (accept, revoke and putting the client in a group)
 * 
 * @author <a href="mailto:melroy.van.den.berg@tass.nl">Melroy van den Berg</a>
 * 
 */
@SuppressWarnings("serial")
public class AdministratorServlet extends HttpServlet
{
  /**
   * Common log category for HTTP
   */
  // @TODO Fix own logger
  private final static Logger logger = Logger.getLogger(Constants.REST_ALL_PANELS_LOG_CATEGORY);

  private static ClientService clientService = (ClientService) SpringContext.getInstance().getBean("clientService");

  /**
   * Transform a client list array into a HTML template.
   * 
   * @param clients List of Clients
   * @return HTML formatted text in the administrator template
   */  
  private String setErrorInTemplate(String error)
  {
     Map<String, Object> root = new HashMap<String, Object>();
      
     String result = "";
     
     // Read the XML file and process the template using FreeMarker
     try 
     {     
        root.put( "errorMessage", error );
        result = freemarkerDo(root, "administrator.ftl");
     }
     catch(Exception e) 
     {
        result = "<h1>Template Exception</h1>";
        result += e.getLocalizedMessage();
        logger.error(e.getLocalizedMessage());
     }
     return result;
  }  
  
  /**
   * 
   * @param resultSet
   * @return
   */
  private String setResultListInTemplate(ResultSet resultSet)
  {
     Map<String, Object> root = new HashMap<String, Object>();
    
     Collection clients = null;
     
     String result = "";
     
     try 
     {
        clients = ResultSetUtil.getMaps(resultSet);
     } 
     catch (SQLException e)
     {
         logger.error("SQLException: " + e.getMessage());
     }
     
     Iterator itr = clients.iterator(); 
     while(itr.hasNext()) {
         Object element = itr.next(); 
         logger.error("element: "  + element.toString());
     }
     
     try 
     {     
        root.put( "clients", clients );
        result = freemarkerDo(root, "administrator.ftl");
     }
     catch(Exception e) 
     {
        result = "<h1>Template Exception</h1>";
        result += e.getLocalizedMessage();
        logger.error(e.getLocalizedMessage());
     }
     return result;
  }
  
  
  /**
   * Transform a client list array into a HTML template.
   * 
   * @param clients List of Clients
   * @return HTML formatted text in the administrator template
   */  
  private String setListInTemplate(List<Client> clients)
  {
     Map<String, Object> root = new HashMap<String, Object>();
      
     String result = "";
     
     try 
     {     
        root.put( "clients", clients );
        result = freemarkerDo(root, "administrator.ftl");
     }
     catch(Exception e) 
     {
        result = "<h1>Template Exception</h1>";
        result += e.getLocalizedMessage();
        logger.error(e.getLocalizedMessage());
     }
     return result;
  }

  /**
   *  Process a template using FreeMarker and print the results
   * @param root HashMap with data
   * @param template the name of the template file
   * @return HTML template with the specified data
   * @throws Exception FreeMarker exception
   */
  static String freemarkerDo(Map root, String template) throws Exception
  {
     Configuration cfg = new Configuration();
     // Read the XML file and process the template using FreeMarker
     ControllerConfiguration configuration = ControllerConfiguration.readXML();
     
     cfg.setDirectoryForTemplateLoading(new File(configuration.getResourcePath()));
     //cfg.setClassForTemplateLoading( FreemarkerUtils.class, "/templates" );
     cfg.setObjectWrapper( new DefaultObjectWrapper() );
     // @TODO : cfg.setObjectWrapper(ObjectWrapper.DEFAULT_WRAPPER);
     Template temp = cfg.getTemplate(template);
     StringWriter out = new StringWriter();
     temp.process( root, out );

     return out.getBuffer().toString();
  }

  
  /**
   * Get request handler for the administrator URI
   * @param request the request from HTTP servlet
   * @param reponse where the respond can be written to
   */  
  @Override 
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
                                                              throws ServletException, IOException
  {
     PrintWriter printWriter = response.getWriter();
     
     try
     {
        ResultSet clients = clientService.getClients();

        if(clients != null)
        { 
           //printWriter.print(setErrorInTemplate("No clients in the database. Number of clients: " + clientService.getNumClients()));
           
           printWriter.print(setResultListInTemplate(clients));

           /*
           while (clients.next()) 
           {
              printWriter.print(clients.getString("client_serial") + " (" + clients.getString("client_pincode") + ")");
           }*/
  
        }
        else
        {
           printWriter.print(setErrorInTemplate("Database problem."));
        }
        response.setStatus(200);
     }
     /*
     catch (SQLException e) 
     {
        response.setStatus(406);
        logger.error("SQL Exception: " + e.getMessage());
     }*/
     catch (NullPointerException e)
     {
        response.setStatus(406);
        logger.error("NullPointer in Administrator Servlet: ", e);
     }
  }
}
