package org.openremote.controller.rest;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.openremote.controller.Constants;
import org.openremote.controller.service.ClientService;
import org.openremote.controller.spring.SpringContext;

/**
 * Check if a certificate is still valid
 * @author <a href="mailto:vincent.kriek@tass.nl">Vincent Kriek</a>  
 */
public class CheckCertificate extends RESTAPI {
   private static final ClientService clientService = (ClientService) SpringContext.getInstance().getBean("clientService");
   private final static Logger logger = Logger.getLogger(Constants.REST_ALL_PANELS_LOG_CATEGORY);

   @Override
   protected void handleRequest(HttpServletRequest request, HttpServletResponse response) {
      String url = request.getRequestURL().toString().trim();
      String regexp = "rest\\/cert\\/check\\/(.*)";
      Pattern pattern = Pattern.compile(regexp);
      Matcher matcher = pattern.matcher(url);
      
      if(matcher.find()) {
         try {
            StringBuilder xml = new StringBuilder();
            xml.append(Constants.STATUS_XML_HEADER);
                        
            String dname = URLDecoder.decode(matcher.group(1), "UTF-8");
            logger.error(dname);
            if(clientService.isClientValid(dname)) {
               xml.append("<valid>true</valid>");
            } else {
               xml.append("<valid>false</valid>");
               response.setStatus(501);
            }
            xml.append(Constants.STATUS_XML_TAIL);
            sendResponse(response, xml.toString());
         } catch (UnsupportedEncodingException e) {
            sendResponse(response, -1, e.getMessage());
         }
      }
   }
}
