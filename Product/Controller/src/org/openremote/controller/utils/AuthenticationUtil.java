package org.openremote.controller.utils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class AuthenticationUtil {
   public static final String AUTH_SESSION = "authenticated";
   
   public static boolean isAuth(HttpServletRequest request) {
      HttpSession session = request.getSession(true);
      Boolean attr = (Boolean) session.getAttribute(AUTH_SESSION);
      if(attr != null) {
         return attr;
      } else {
         return false;
      }
   }   
}
