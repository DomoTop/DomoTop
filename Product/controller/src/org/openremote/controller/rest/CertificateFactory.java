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
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
public class CertificateFactory extends RESTAPI
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


  // TODO :
  //  reduce API dependency and lookup service implementation through either an service container
  //  or short term servlet application context

  private final static ProfileService profileService = (ProfileService) SpringContext.getInstance().getBean(
       "profileService");



  // Implement REST API ---------------------------------------------------------------------------


  @Override protected void handleRequest(HttpServletRequest request, HttpServletResponse response)
  {
    try
    {
        String certname = "vincent" + System.currentTimeMillis(); 
        String keytool = "/usr/bin/keytool";
        File certloc = new File(profileService.getAllPanels() + "/certificates/");

        String bksloc = certloc.getPath() + "/BKS.jar";
        String bksprovider = "org.bouncycastle.jce.provider.BouncyCastleProvider";

        String exitcodes = "";
        
        //Try to create server certificate if it not already exist, should be configured as path, later TODO
        ProcessBuilder pb = new ProcessBuilder(keytool, "-genkeypair", "-alias", "servercert","-keyalg","RSA","-dname","CN=Web Server,OU=Unit,O=Organization,L=City,S=State,C=US","-keypass","password","-keystore","server.jks","-storepass","password");
        pb.directory(certloc);

        Process p = pb.start();
        p.waitFor();
        exitcodes += p.exitValue() + " ";

        //Generate user certificate
        pb.command(keytool,"-genkeypair","-alias",certname,"-keystore",certname + ".bks","-storetype","BKS","-keyalg","RSA","-dname","CN="+certname+",OU=Unit,O=Organization,L=City,S=State,C=US","-keypass","password","-storepass","password","-provider",bksprovider,"-providerpath",bksloc);
        
        p = pb.start();
        p.waitFor();
        exitcodes += p.exitValue() + " ";

        //Export user certificate to file for sending to android, could happen later, TODO
        pb.command(keytool,"-exportcert","-alias",certname,"-file",certname +".cer","-keystore",certname+".bks","-storetype","BKS","-storepass","password","-provider",bksprovider,"-providerpath",bksloc);
        
        p = pb.start();
        p.waitFor();
        exitcodes += p.exitValue() + " ";

        //Import user certificate into server certificate        
        pb.command(keytool,"-importcert","-keystore","server.jks","-alias",certname,"-file",certname+".cer","-v","-trustcacerts","-noprompt","-storepass","password");
        
        p = pb.start();
        p.waitFor();
        exitcodes += p.exitValue() + " ";

        sendResponse(response, exitcodes);
    }

    catch (ControlCommandException e)
    {
      logger.error("failed to get all the panels", e);

      // TODO :
      //   this might well break the JSON client code -- but can't know for sure cause chinese
      //   are too effin dumb to write proper tests
      //
      // response.setStatus(e.getErrorCode());

      sendResponse(response, e.getErrorCode(), e.getMessage());
    }
    catch (InterruptedException e) 
    {
        logger.error("Failed to create certificate");
        sendResponse(response, "Interupted");
    }
    catch (IOException e) 
    {
        logger.error("Failed to create certificate");
 //       sendResponse(response, "IOException" + profileService.getAllPanels() + "/certificates/create.sh");
        sendResponse(response, e.getMessage());
    }
  }

}
