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
import org.openremote.controller.exception.ControllerException;
import org.openremote.controller.rest.support.json.JSONTranslator;
import org.openremote.controller.service.StatusCommandService;
import org.openremote.controller.spring.SpringContext;

/**
 * The Class Status Command REST Servlet.
 * 
 * This servlet is responsible for 
 *   parsing RESTful url "http://xxx.xxx.xxx/controller/rest/status/{sensor_id},{sensor_id}...",
 *   building StatusCommand,
 *   status query with stateful StatusCommand,
 *   and conpose status result into XML formatted data to RESTful service client.
 * 
 * @author Handy.Wang
 */
@SuppressWarnings("serial")
public class StatusCommandRESTServlet extends HttpServlet {

    /** The logger. */
    private Logger logger = Logger.getLogger(StatusCommandRESTServlet.class.getName());

    /** The control command service. */
    private static StatusCommandService statusCommandService = (StatusCommandService) SpringContext.getInstance().getBean("statusCommandService");

    /*
     * (non-Javadoc)
     * @see
     * javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest
     * , javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {
        doPost(req, resp);
    }

    /*
     * (non-Javadoc)
     * @see
     * javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest
     * , javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

      // Set response MIME type and character encoding...

        response.setCharacterEncoding(Constants.CHARACTER_ENCODING_UTF8);
        response.setContentType(Constants.MIME_APPLICATION_XML);


      // Get the 'accept' header from client -- this will indicate whether we will send
      // application/xml or application/json response...

      String acceptHeader = request.getHeader(Constants.HTTP_ACCEPT_HEADER);


        String url = request.getRequestURL().toString();
        String regexp = "rest\\/status\\/(.*)";
        Pattern pattern = Pattern.compile(regexp);
        Matcher matcher = pattern.matcher(url);
        String unParsedSensorIDs = null;

        PrintWriter printWriter = response.getWriter();
        if (matcher.find()) {
            unParsedSensorIDs = matcher.group(1);
            try {
                if (unParsedSensorIDs != null && !"".equals(unParsedSensorIDs)) {
                    printWriter.write(JSONTranslator.translateXMLToJSON(acceptHeader, response, statusCommandService.readFromCache(unParsedSensorIDs)));
                }
            } catch (ControllerException e) {
                logger.error("CommandException occurs", e);
                printWriter.print(JSONTranslator.translateXMLToJSON(acceptHeader, response, e.getErrorCode(), RESTAPI.composeXMLErrorDocument(e.getErrorCode(), e.getMessage())));
            }
        } else {
            printWriter.print(JSONTranslator.translateXMLToJSON(acceptHeader, response, 400, RESTAPI.composeXMLErrorDocument(400, "Bad REST Request, should be /rest/status/{sensor_id},{sensor_id}...")));
        }
        printWriter.flush();
    }
}
