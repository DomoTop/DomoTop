/*
 * OpenRemote, the Home of the Digital Home.
 * Copyright 2008-2011, OpenRemote Inc.
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
package org.openremote.controller.rest;

import java.io.IOException;
import java.security.Principal;
import java.security.cert.X509Certificate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.openremote.controller.Constants;
import org.openremote.controller.exception.ControlCommandException;
import org.openremote.controller.service.ClientService;
import org.openremote.controller.spring.SpringContext;

/**
 * This servlet implements the REST API '/rest/device/group' functionality which creates
 * a certificate when a call has been done.  <p>
 *
 * REST servlet for getting the group from the device/client certificate
 *
 * @author <a href="mailto:melroy.van.den.berg@tass.nl">Melroy van den Berg</a>
 */
@SuppressWarnings("serial")
public class FindGroupFromDevice extends RESTAPI
{

  /*
   *  IMPLEMENTATION NOTES:
   *
   *    - This adheres to the current 2.0 version of the HTTP/REST/XML and HTTP/REST/JSON APIs.
   *      There's currently no packaging or REST URL distinction for supported API versions.
   *      Later versions of the Controller may support multiple revisions of the API depending
   *      on client request. Appropriate implementation changes should be made then.
   *                                                                                      [JPL]
   */


  // Class Members --------------------------------------------------------------------------------

  /**
   * Common log category for HTTP REST API.
   */
  private final static Logger logger = Logger.getLogger(Constants.REST_ALL_PANELS_LOG_CATEGORY);
  private static final ClientService clientService = (ClientService) SpringContext.getInstance().getBean("clientService");

  protected String getGroup(Principal DN) throws IOException
  {
     return clientService.getGroupName(DN.toString());    
  }

  // Implement REST API ---------------------------------------------------------------------------
  @Override protected void handleRequest(HttpServletRequest request, HttpServletResponse response)
  {
     Principal DN = null;
    try
    {
       if(request.getAuthType() == HttpServletRequest.CLIENT_CERT_AUTH)
       {
          // Obtain the certificate from the request, if any
          X509Certificate[] certs = null;
          if (request != null)
          {
              certs = (X509Certificate[]) request
                      .getAttribute("javax.servlet.request.X509Certificate");
          }

          if ((certs == null) || (certs.length == 0))
          {
             logger.error("No certificate found - however Client authentication is enabled. Something is wrong...");
          }
          else
          {
             DN = certs[0].getSubjectDN();
          }         
       }

       if(DN != null)
       {
          String groupName = getGroup(DN);
          if(!groupName.isEmpty())
          {
             response.setStatus(200);
             sendResponse(response, groupName);
          }
          else
          {
             response.setStatus(404);
             sendResponse(response, "");             
          }
       }
       else
       {
          response.setStatus(404);
          sendResponse(response, "");          
       }
    }

    catch (ControlCommandException e)
    {
      logger.error("failed to get groups", e);

      // TODO :
      //   this might well break the JSON client code -- but can't know for sure cause chinese
      //   are too effin dumb to write proper tests
      //
       response.setStatus(e.getErrorCode());

      //sendResponse(response, e.getErrorCode(), e.getMessage());
    }
    catch (IOException e) 
    {
        logger.error("Failed to get group: " + e.getMessage());
        response.setStatus(404);
        sendResponse(response, "Problem with getting group");
    }
  }
}
