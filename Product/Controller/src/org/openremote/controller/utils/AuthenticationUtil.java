package org.openremote.controller.utils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.openremote.controller.Constants;
import org.openremote.controller.service.ConfigurationService;

public class AuthenticationUtil {
   public static final String AUTH_SESSION = "authenticated";
   
   private final static Logger logger = Logger.getLogger(Constants.LOGIN_SERVLET_LOG_CATEGORY);

   
   public static boolean isAuth(HttpServletRequest request, ConfigurationService service) {
      HttpSession session = request.getSession(true);
      String attr = (String) session.getAttribute(AUTH_SESSION);
      
      
            
      if(attr != null) {
         String username = service.getItem("composer_username");
         String password = service.getItem("composer_password");
         String timestamp = service.getItem("session_timestamp");
         
         String md5 = MD5Util.generateMD5Sum((username + timestamp + password).getBytes());
         return md5.equals(attr);
      } else {
         return false;
      }
   }   
}
