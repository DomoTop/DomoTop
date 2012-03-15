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

import java.security.cert.X509Certificate;

import java.lang.ProcessBuilder;
import java.lang.InterruptedException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.BufferedWriter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;

import org.apache.log4j.Logger;
import org.openremote.controller.Constants;
import org.openremote.controller.exception.ControlCommandException;
import org.openremote.controller.service.ProfileService;
import org.openremote.controller.spring.SpringContext;

/**
 * This servlet implements the REST API '/rest/certificate/create' functionality which creates
 * a certificate when a call has been done.  <p>
 *
 * See <a href = "http://www.openremote.org/display/docs/Controller+2.0+HTTP-REST-XML">
 * Controller 2.0 REST XML API<a> and
 * <a href = "http://openremote.org/display/docs/Controller+2.0+HTTP-REST-JSONP">Controller 2.0
 * REST JSONP API</a> for more details.
 *
 * @author <a href="mailto:vincent.kriek@tass.nl">Vincent Kriek</a>
 */
public class FindCertificateByID extends RESTAPI
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

  protected String signCsr(String username) throws IOException, InterruptedException
  {
    String certname = username; 
    String openssl = "/usr/bin/openssl";
    String caloc = "/usr/share/tomcat6/cert/ca/certs/";
    
    StringBuffer sb = new StringBuffer();
    sb.append(Constants.STATUS_XML_HEADER);
   
    sb.append("\n<chain>\n<server>\n");

    FileReader fr = new FileReader(caloc + "myca.crt");
    BufferedReader reader = new BufferedReader(fr);
    
    String st = "";
    while((st = reader.readLine()) != null) {
       sb.append(st + "\n");
    }

    sb.append("</server>\n<client>\n");
    fr = new FileReader(caloc + username + ".crt");
    reader = new BufferedReader(fr);

    boolean cert = false;
    while((st = reader.readLine()) != null) {
       if(cert) {
           sb.append(st + "\n");
       } else {
           if(st.startsWith("-----BEGIN CERTIFICATE-----")) {
               cert = true;
               sb.append(st + "\n");
           }
       }
    }

    sb.append("</client>\n</chain>");
    sb.append(Constants.STATUS_XML_TAIL);

    return sb.toString();
  }

  // Implement REST API ---------------------------------------------------------------------------
  @Override protected void handleRequest(HttpServletRequest request, HttpServletResponse response)
  {
    try
    {
        String url = request.getRequestURL().toString().trim();
        String regexp = "rest\\/cert\\/get\\/(.*)";
        Pattern pattern = Pattern.compile(regexp);
        Matcher matcher = pattern.matcher(url);
        if(matcher.find()) 
            sendResponse(response, signCsr(matcher.group(1)));
        else
            sendResponse(response, "NO MATCH FIND");
    }

    catch (ControlCommandException e)
    {
      logger.error("failed to get all the panels", e);

      // TODO :
      //   this might well break the JSON client code -- but can't know for sure cause chinese
      //   are too effin dumb to write proper tests
      //
       response.setStatus(e.getErrorCode());

      //sendResponse(response, e.getErrorCode(), e.getMessage());
    }
//    catch (InterruptedException e) 
//    {
//        logger.error("Failed to create certificate");
//        //sendResponse(response, "Interupted");
//    }
    catch (Exception e) 
    {
        logger.error("Failed to create certificate");
        sendResponse(response, "FAILURE");
       // sendResponse(response, "IOException" + profileService.getAllPanels() + "/certificates/create.sh");
        //sendResponse(response, e.getMessage());
    }
  }
}
