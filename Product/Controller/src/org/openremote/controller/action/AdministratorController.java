/* OpenRemote, the Home of the Digital Home.
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
package org.openremote.controller.action;

import java.awt.Panel;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openremote.controller.Constants;
import org.openremote.controller.ControllerConfiguration;
import org.openremote.controller.exception.BeehiveNotAvailableException;
import org.openremote.controller.exception.ControlCommandException;
import org.openremote.controller.exception.ForbiddenException;
import org.openremote.controller.exception.ResourceNotFoundException;
import org.openremote.controller.model.Group;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

/**
 * The controller for Administrator management.
 * 
 * @author <a href="mailto:melroy.van.den.berg@tass.nl">Melroy van den Berg</a> 2012
 */
public class AdministratorController extends MultiActionController {
      
   //private static final String rootCADir = ControllerConfiguration.readXML().getCaPath();
   private static final String rootCADir = "/usr/share/tomcat6/cert/ca";
   
   private static final String openssl = "openssl";
   private static final String rm = "rm";
   private static final String CRTDir = "certs";
   private static final String CSRDir = "csr";

   /**
    * Accept or deny user 
    */  
   public ModelAndView changeUserStatus(HttpServletRequest request, HttpServletResponse response) throws IOException,
         ServletRequestBindingException {
      String action = request.getParameter("action");
      String clientID = request.getParameter("clientid");
      String clientFileName = request.getParameter("clientfile"); // @TODO: Get file name via client id using a database
      String clientUsername = clientFileName.substring(0, clientFileName.lastIndexOf('.'));

      try 
      {   
         int result = -1;
         if(action.equals("accept")) // trust
         {
            result = executeOpenSSLCommand(clientUsername, true);
         }
         else if(action.equals("deny")) // revoke
         {
            result = executeOpenSSLCommand(clientUsername, false);            
         }

         if(result == 0)
         {
            // @TODO: Add the serial ID to the database and use index.txt
            // to get the right list in the administrator Panel.
            
            // If successfully revoked, than remove the certificate
            if(action.equals("deny"))
            {
               if(executeDeleteCommand(clientUsername) == 0)
               {
                  response.getWriter().print(Constants.OK + "-" + clientID + "-" + action);
               }
               else
               {
                  response.getWriter().print("Certificate is successfully revoked, but couldn't be removed.");
               }
            }
            else if(action.equals("accept"))
            {
               response.getWriter().print(Constants.OK + "-" + clientID + "-" + action);
            }
         }
         else
         {
            if(executeDeleteCommand(clientUsername) == 0)
            {
               response.getWriter().print("OpenSSL command failed, exit with exit code: " + result);
            }
            else
            {
               response.getWriter().print("OpenSSL command failed, exit with exit code: " + result + ". \n<br/>Plus the certificate couldn't be removed.");
            }
         }         
      } catch (NullPointerException e) {
         response.getWriter().print("nullpointer");
      } catch (InterruptedException e) {
         response.getWriter().print("interrupt");
      }
      return null;
   }
   
   private int executeOpenSSLCommand(String username, boolean accept) throws NullPointerException, IOException, InterruptedException
   {
      List<String> command = new ArrayList<String>();
      command.add(openssl); // command
      if(accept) // Signing
      {
         command.add("ca");
         command.add("-batch");
         command.add("-passin");
         command.add("pass:password");
         command.add("-config");
         command.add("openssl.my.cnf");
         command.add("-policy");
         command.add("policy_anything");
         command.add("-out");
         command.add(CRTDir + "/" + username + ".crt");      
         command.add("-in"); 
         command.add(CSRDir + "/" + username + ".csr");
      }
      else // revoke
      {
         command.add("ca");
         command.add("-passin");
         command.add("pass:password");
         command.add("-config");
         command.add("openssl.my.cnf");
         command.add("-revoke");
         command.add(CRTDir + "/" + username + ".crt");        
      }
      
      ProcessBuilder pb = new ProcessBuilder(command);
      pb.directory(new File(rootCADir));

      Process p = null;
      StringBuffer buffer = new StringBuffer();

      p = pb.start();
      p.waitFor();
      /*
      BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
       
      String line = null;
      while ( (line = br.readLine()) != null) {
          buffer.append(line).append("\n");
      }*/
      return p.exitValue();
   }  
   
   private int executeDeleteCommand(String username) throws NullPointerException, IOException, InterruptedException
   {
      List<String> command = new ArrayList<String>();
      command.add(rm); // command
      command.add(CRTDir + "/" + username + ".crt");
      
      ProcessBuilder pb = new ProcessBuilder(command);
      pb.directory(new File(rootCADir));

      Process p = null;
      p = pb.start();
      p.waitFor();
      return p.exitValue();
   }  
}
