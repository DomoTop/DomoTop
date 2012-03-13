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
import java.io.BufferedWriter;

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
    String certname = username + System.currentTimeMillis() / 1000; 
    String keytool = "/usr/bin/keytool";
    String openssl = "/usr/bin/openssl";
    File certloc = new File(profileService.getAllPanels() + "/certificates/");

    String servercertloc = "/usr/share/tomcat6/cert/";
    String caloc = "/usr/share/tomcat6/cert/myCA/";

    String bksloc = certloc.getPath() + "/BKS.jar";
    String bksprovider = "org.bouncycastle.jce.provider.BouncyCastleProvider";

    String exitcodes = "";
    
    //Generate user certificate
    ProcessBuilder pb = new ProcessBuilder(keytool,"-genkeypair","-alias",certname,"-keystore",certname + ".bks","-storetype","BKS","-keyalg","RSA","-dname","CN="+certname+",OU=Unit,O=Organization,L=City,S=State,C=US","-keypass","password","-storepass","password","-provider",bksprovider,"-providerpath",bksloc);
    pb.directory(certloc);
    
    Process p = pb.start();
    p.waitFor();
    exitcodes += p.exitValue() + " ";

    //Generate CSR for the user certificate to sign by our server certificate
    pb.command(keytool, "-certreq", "-alias", certname, "-file", certname + ".csr", "-keystore", certname + ".bks", "-storepass", "password", "-provider",bksprovider,"-providerpath",bksloc, "-storetype", "bks");

    p = pb.start();
    p.waitFor();
    exitcodes += p.exitValue() + " ";

    pb.directory(new File(caloc));
    pb.command(openssl, "ca", "-batch", "-passin", "pass:password", "-config", "openssl.my.cnf", "-policy", "policy_anything", "-out", certloc.getPath() + "/" + certname + ".crt", "-infiles",certloc.getPath() + "/" + certname + ".csr");

    p = pb.start();
    p.waitFor();
    exitcodes += p.exitValue() + " ";

    mergeChain(
            new File(certloc.getPath() + "/" + certname + ".crt"),
            new File(caloc + "certs/myca.crt"),
            new File(certloc.getPath() + "/" + certname + ".chain"));

    pb = new ProcessBuilder(keytool,"-importcert","-trustcacerts" ,"-noprompt","-alias",certname,"-keystore",certname + ".bks","-storetype","BKS","-provider",bksprovider,"-providerpath",bksloc,"-storepass","password","-file",certloc.getPath() + "/" + certname + ".chain");
    pb.directory(certloc);

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

    return new String(Base64.encodeBase64(bindata));
    //return exitcodes;// + new String(bindata);
    //return Base64.encodeBase64(new String(bindata));
  }

  // Implement REST API ---------------------------------------------------------------------------

  protected String mergeChain(File clientcert, File servercert, File chain) throws IOException
  {
    BufferedWriter out = new BufferedWriter(new FileWriter(chain));

    FileReader fr = new FileReader(servercert);
    BufferedReader reader = new BufferedReader(fr);
    String st = "";
    while((st = reader.readLine()) != null) {
        out.write(st + "\n");
    }

    fr = new FileReader(clientcert);
    reader = new BufferedReader(fr);
    boolean cert = false;
    while((st = reader.readLine()) != null) {
        if(cert) {
            out.write(st + "\n");
        } else {
            if(st.startsWith("-----BEGIN CERTIFICATE-----")) {
                cert = true;
                out.write(st + "\n");
            }
        }
    }

    out.close();

    return chain.getPath();
  }

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
