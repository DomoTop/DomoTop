/* OpenRemote, the Home of the Digital Home.
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
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.openremote.controller.Constants;
import org.openremote.controller.exception.ControlCommandException;
import org.openremote.controller.exception.InvalidCommandTypeException;
import org.openremote.controller.rest.support.json.JSONTranslator;
import org.openremote.controller.service.ControlCommandService;
import org.openremote.controller.spring.SpringContext;

/**
 * The Class Control Command REST Servlet.
 * 
 * @author Handy.Wang
 */
@SuppressWarnings("serial")
public class ControlCommandRESTServlet extends HttpServlet {
   
   private Logger logger = Logger.getLogger(ControlCommandRESTServlet.class.getName());
   
   private static ControlCommandService controlCommandService = 
      (ControlCommandService) SpringContext.getInstance().getBean("controlCommandService");
   
   /* (non-Javadoc)
    * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
    */
   @Override
   protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
      doPost(req, resp);
   }

   /* (non-Javadoc)
    * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
    */
   @Override
   protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
       
    // Set response MIME type and character encoding...

    response.setContentType(Constants.MIME_APPLICATION_XML);
    response.setCharacterEncoding(Constants.CHARACTER_ENCODING_UTF8);

    // Get the 'accept' header from client -- this will indicate whether we will send
    // application/xml or application/json response...

    String acceptHeader = request.getHeader(Constants.HTTP_ACCEPT_HEADER);

      String url = request.getRequestURL().toString();      
      String regexp = "rest\\/control\\/(\\d+)\\/(\\w+)";
      Pattern pattern = Pattern.compile(regexp);      
      Matcher matcher = pattern.matcher(url);      
      String controlID = null;
      String commandParam = null;
      
      PrintWriter output = response.getWriter();
      
      if (matcher.find()) {
         controlID = matcher.group(1);
         commandParam = matcher.group(2);
         try{
            if (isNotEmpty(controlID) && isNotEmpty(commandParam)) {
                  controlCommandService.trigger(controlID, commandParam);

                  // TODO : this just makes no sense -- why would you put HTTP 200 OK into an error document? chinese logic
                  output.print(JSONTranslator.translateXMLToJSON(acceptHeader, response, 200, RESTAPI.composeXMLErrorDocument(200, "SUCCESS")));
               } else {
                  throw new InvalidCommandTypeException(commandParam);
               }
         } catch (ControlCommandException e) {
            logger.error("ControlCommandException occurs", e);
            output.print(JSONTranslator.translateXMLToJSON(acceptHeader, response, e.getErrorCode(), RESTAPI.composeXMLErrorDocument(e.getErrorCode(), e.getMessage())));
         }
      } else {
         output.print(JSONTranslator.translateXMLToJSON(acceptHeader, response, 400, RESTAPI.composeXMLErrorDocument(400, "Bad REST Request, should be /rest/control/{control_id}/{commandParam}")));
      }
      output.flush();
   }
   
   /**
    * Checks if String parameter is not empty.
    * 
    * @param param the param
    * 
    * @return true, if parameter is not empty
    */
   private boolean isNotEmpty(String param) {
      return (param != null && !"".equals(param)) ? true : false;
   }

}
