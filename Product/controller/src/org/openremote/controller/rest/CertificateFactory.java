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

import java.util.ArrayList;

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


  protected String generateCertificate(String username) throws IOException, InterruptedException
  {
    String certname = username + System.currentTimeMillis(); 
    String keytool = "/usr/bin/keytool";
    File certloc = new File(profileService.getAllPanels() + "/certificates/");

    String servercertloc = "/usr/share/tomcat6/cert/";

    String bksloc = certloc.getPath() + "/BKS.jar";
    String bksprovider = "org.bouncycastle.jce.provider.BouncyCastleProvider";

    String exitcodes = "";
    
    //Try to create server certificate if it not already exist, should be configured as path, later TODO
    ProcessBuilder pb = new ProcessBuilder(keytool, "-genkeypair", "-alias", "servercert","-keyalg","RSA","-dname","CN=OpenRemote,OU=Controller,O=OpenRemote inc,L=NY,S=NY,C=US","-keypass","password","-keystore","server.jks","-storepass","password");
    pb.directory(certloc);

    Process p = pb.start();
    p.waitFor();
    exitcodes += p.exitValue() + " ";

    //Generate user certificate
    pb.command(keytool,"-genkeypair","-alias",certname,"-keystore",certname + ".bks","-storetype","BKS","-keyalg","RSA","-dname","CN="+certname+",OU=Unit,O=Organization,L=City,S=State,C=US","-keypass","password","-storepass","password","-provider",bksprovider,"-providerpath",bksloc);
    
    p = pb.start();
    p.waitFor();
    exitcodes += p.exitValue() + " ";

    //Generate CSR for the user certificate to sign by our server certificate
    pb.command(keytool, "-certreq", "-alias", certname, "-file", certname + ".csr", "-keystore", certname + ".bks", "-storepass", "password", "-provider",bksprovider,"-providerpath",bksloc, "-storetype", "bks");

    p = pb.start();
    p.waitFor();
    exitcodes += p.exitValue() + " ";

    File serverkey = new File(servercertloc + "server.pem");
    if(!serverkey.exists()) {
        pb.command(keytool, "-importkeystore","-srckeystore",servercertloc + "server.jks","-destkeystore",servercertloc + "temp.p12","-srcstoretype","JKS","-deststoretype","PKCS12","-srcstorepass","password","-deststorepass","password","-srcalias","servercert","-destalias","servercert","-srckeypass","password","-destkeypass","password","-noprompt");

        p = pb.start();
        p.waitFor();
        exitcodes += p.exitValue() + " ";

        pb.command("/usr/bin/openssl", "pkcs12", "-in", servercertloc + "temp.p12", "-out", "server.pem", "-passin pass:password", "-passout pass:password");
        p = pb.start();
        p.waitFor();
        exitcodes += p.exitValue() + " ";
    }

    pb.command("/usr/bin/openssl", "x509", "-req", "-days", "365", "-in", certname + ".csr", "-CA", serverkey.getPath(), "-CAcreateserial", "-out", certname + ".cer", "-passin", "pass:password", "-extensions", "v3_usr");
    p = pb.start();
    p.waitFor();
    exitcodes += p.exitValue() + " ";


    File file = new File(certloc, "/" + certname + ".bks");
    FileInputStream fin = new FileInputStream(file);
    byte[] bindata = new byte[(int)file.length()];
    int ch, i = 0;
    while((ch = fin.read()) != -1) {
        bindata[i++] = (byte)ch;
    }
    fin.close();

    return exitcodes;
    //return new String(Base64.encodeBase64(bindata));
    //return Base64.encodeBase64(new String(bindata));
  }

  // Implement REST API ---------------------------------------------------------------------------


  @Override protected void handleRequest(HttpServletRequest request, HttpServletResponse response)
  {
    try
    {
        String base64cert = generateCertificate("vincent");
        sendResponse(response, base64cert);
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
