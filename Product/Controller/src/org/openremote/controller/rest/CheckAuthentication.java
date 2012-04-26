package org.openremote.controller.rest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openremote.controller.service.ConfigurationService;
import org.openremote.controller.spring.SpringContext;

/**
 * Check if client authentication is enabled or not
 * @author <a href="mailto:melroy.van.den.berg@tass.nl">Melroy van den Berg</a>  
 */
@SuppressWarnings("serial")
public class CheckAuthentication extends RESTAPI 
{
   private static final ConfigurationService configurationService = (ConfigurationService) SpringContext.getInstance().getBean(
         "configurationService");
   private boolean isAuthenticationOn = false;
   
   /**
    * Check if client authentication is turn on via HTTPS
    * @Response 200 if authentication is enabled, otherwise 404
    */
   @Override
   protected void handleRequest(HttpServletRequest request, HttpServletResponse response)
   {
      isAuthenticationOn = configurationService.getBooleanItem("authentication");
      
      if(isAuthenticationOn) 
      {
         response.setStatus(200);
      } 
      else 
      {
         response.setStatus(404);
      }
      sendResponse(response, "");
   }
}
