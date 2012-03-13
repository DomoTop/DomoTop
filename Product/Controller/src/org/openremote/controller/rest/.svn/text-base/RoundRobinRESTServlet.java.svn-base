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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.openremote.controller.Constants;
import org.openremote.controller.exception.roundrobin.RoundRobinException;
import org.openremote.controller.rest.support.json.JSONTranslator;
import org.openremote.controller.service.RoundRobinService;
import org.openremote.controller.spring.SpringContext;

/**
 * REST servlet for providing round robin groupmembers, etc.
 * 
 * @author Handy.Wang 2009-12-23
 */
@SuppressWarnings("serial")
public class RoundRobinRESTServlet extends HttpServlet {
   
   private Logger logger = Logger.getLogger(this.getClass().getName());
   
   private RoundRobinService roundRobinService = (RoundRobinService) SpringContext.getInstance().getBean("roundRobinService");

   public RoundRobinRESTServlet() {
      super();
   }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	   logger.info("Start RoundRobin group member REST service. at " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));

    // Set response MIME type and character encoding...

    response.setCharacterEncoding(Constants.CHARACTER_ENCODING_UTF8);
    response.setContentType(Constants.MIME_APPLICATION_XML);
	   
    // Get the 'accept' header from client -- this will indicate whether we will send
    // application/xml or application/json response...

    String acceptHeader = request.getHeader(Constants.HTTP_ACCEPT_HEADER);


      String url = request.getRequestURL().toString();
      String regexp = "rest\\/servers";
      Pattern pattern = Pattern.compile(regexp);
      Matcher matcher = pattern.matcher(url);
      PrintWriter printWriter = response.getWriter();
      if (matcher.find()) {
         try {
            Set<String> groupMemberControllerAppURLSet = roundRobinService.discoverGroupMembersAppURL();
            String serversXML = roundRobinService.constructServersXML(groupMemberControllerAppURLSet);
            printWriter.println(JSONTranslator.translateXMLToJSON(acceptHeader, response, serversXML));
            logger.info("Finished RoundRobin group member REST service.  at " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "\n");
         } catch (RoundRobinException e) {
            printWriter.print(JSONTranslator.translateXMLToJSON(acceptHeader, response, e.getErrorCode(), RESTAPI.composeXMLErrorDocument(e.getErrorCode(), e.getMessage())));
         }
      } else {
         printWriter.print(JSONTranslator.translateXMLToJSON(acceptHeader, response, RoundRobinException.INVALID_ROUND_ROBIN_URL, RESTAPI.composeXMLErrorDocument(RoundRobinException.INVALID_ROUND_ROBIN_URL, "Invalid round robin rul " + url)));
      }
      printWriter.flush();
	}

}
