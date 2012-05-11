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

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.openremote.controller.Constants;
import org.openremote.controller.exception.ControlCommandException;
import org.openremote.controller.service.ClientService;
import org.openremote.controller.service.ConfigurationService;
import org.openremote.controller.spring.SpringContext;

import sun.security.x509.X509Cert;

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
 * @author <a href="mailto:melroy.van.den.berg@tass.nl">Melroy van den Berg</a>
 */
@SuppressWarnings("serial")
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
  private static final ClientService clientService = (ClientService) SpringContext.getInstance().getBean("clientService");
  private static final ConfigurationService configurationService = (ConfigurationService) SpringContext.getInstance().getBean("configurationService");
  private static final String CA_ALIAS = "ca.alias";
  private static final String ERROR_INVALID_DN = "INVALID_DN";
  private static final String ERROR_DATE_EXPIRED = "DATE_EXPIRED";
  
  protected String getChain(String username) throws Exception
  {
     username = URLDecoder.decode(username, "UTF-8");
    String rootCAPath = configurationService.getItem("ca_path");
    String keystore = rootCAPath + "/server.jks";
    
    StringBuffer sb = new StringBuffer();
    sb.append(Constants.STATUS_XML_HEADER);
   
    sb.append("\n<chain>\n<server>\n");

    try { 
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(new FileInputStream(keystore), "password".toCharArray());
        Certificate certificate = ks.getCertificate(CA_ALIAS);
        sb.append(new String(Base64.encodeBase64(certificate.getEncoded())));
    } catch (KeyStoreException e) {
        logger.error(e.getMessage());
    } catch (NoSuchAlgorithmException e) {
        logger.error(e.getMessage());
    } catch (CertificateException e) {
        logger.error(e.getMessage());
    }

    sb.append("</server>\n<client>\n");
    
   try
   {
      Certificate certificate = clientService.getClientCertificate(username);
      if(certificate != null)
      {
         // Check client certificate
         //if(clientService.(dn, datum)
         X509Certificate x509cert = (X509Certificate) certificate;
         Principal dname = x509cert.getSubjectDN();
         Date notAfterDate = x509cert.getNotAfter();
         
         if(clientService.isClientValid(dname.toString()))
         {
            if(clientService.isClientDateValid(notAfterDate))
            {
               sb.append(new String(Base64.encodeBase64(certificate.getEncoded())));
            }
            else
            {
               throw new Exception(ERROR_DATE_EXPIRED);               
            }
         }
         else
         {
            throw new Exception(ERROR_INVALID_DN);
         }
      }
      else
      {
         logger.error("Client certificate is not found/null.");
      }
   } catch (CertificateEncodingException e) {
      logger.error(e.getMessage());
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
            sendResponse(response, getChain(matcher.group(1)));
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
    catch (IOException e) 
    {
        logger.error("Failed to get certificate: " + e.getMessage());
        response.setStatus(404);
        sendResponse(response, "No certificate by that name");
    } catch (Exception e) {
       if(e.getMessage().equals(ERROR_INVALID_DN))
       {        
          logger.error("Certificate has an invalid DN.");
          response.setStatus(431);     
          sendResponse(response, "Invalid DN");
       } else if(e.getMessage().equals(ERROR_DATE_EXPIRED)) {
          logger.error("Certificate has been expired.");
          response.setStatus(432); 
          sendResponse(response, "Date expired"); 
       }   
   }
  }
}
