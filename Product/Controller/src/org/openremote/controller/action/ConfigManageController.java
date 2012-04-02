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
package org.openremote.controller.action;

import java.util.List;
import java.util.ArrayList;

import java.io.IOException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.net.URI;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpResponse;
import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.commons.codec.binary.Base64;

import org.openremote.controller.ControllerConfiguration;
import org.openremote.controller.Constants;
import org.openremote.controller.exception.BeehiveNotAvailableException;
import org.openremote.controller.exception.ControlCommandException;
import org.openremote.controller.exception.ForbiddenException;
import org.openremote.controller.exception.ResourceNotFoundException;
import org.openremote.controller.service.ControllerXMLChangeService;
import org.openremote.controller.service.FileService;
import org.openremote.controller.spring.SpringContext;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.springframework.security.providers.encoding.Md5PasswordEncoder;

import org.openremote.controller.utils.PathUtil;


import org.openremote.controller.service.ConfigurationService;

/**
 * The controller for Configuration management.
 * 
 * @author Dan 2009-5-14
 */
public class ConfigManageController extends MultiActionController {
   
   /** The file service. */
   private FileService fileService;
   
   private ControllerConfiguration configuration;
   
   /** MUST use <code>SpringContext</code> to keep the same context as <code>InitCachedStatusDBListener</code> */
   private ControllerXMLChangeService controllerXMLChangeService = (ControllerXMLChangeService) SpringContext
         .getInstance().getBean("controllerXMLChangeService");

   private ConfigurationService configurationService = (ConfigurationService) SpringContext
         .getInstance().getBean("configurationService");
   /**
    * Upload zip.
    * 
    * @param request the request
    * @param response the response
    * 
    * @return the model and view
    * 
    * @throws IOException Signals that an I/O exception has occurred.
    * @throws ServletRequestBindingException the servlet request binding exception
    */
   public ModelAndView uploadZip(HttpServletRequest request, HttpServletResponse response) throws IOException,
         ServletRequestBindingException {
      try {
         if (configuration.isResourceUpload()) {
            MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
            boolean success = fileService.uploadConfigZip(multipartRequest.getFile("zip_file").getInputStream());
            if (success) {
               controllerXMLChangeService.refreshController();
            }
            response.getWriter().print(success ? Constants.OK : null);
         } else {
            response.getWriter().print("disabled");
         }
      } catch (ControlCommandException e) {
         response.getWriter().print(e.getMessage());
      }
      return null;
   }
   
   public ModelAndView syncOnline(HttpServletRequest request, HttpServletResponse response) throws IOException,
         ServletRequestBindingException {
      String username = request.getParameter("username");
      String password = request.getParameter("password");
      boolean success = false;
      try {
         success = fileService.syncConfigurationWithModeler(username, password);
         if (success) {
            controllerXMLChangeService.refreshController();
            saveUsername(username);
         }
         response.getWriter().print(success ? Constants.OK : null);
      } catch (ForbiddenException e) {
         response.getWriter().print("forbidden");
      } catch (BeehiveNotAvailableException e) {
         response.getWriter().print("n/a");
      } catch (ResourceNotFoundException e) {
         response.getWriter().print("missing");
      } catch (ControlCommandException e) {
         response.getWriter().print(e.getMessage());
      }
      return null;
   }

   public ModelAndView checkOnline(HttpServletRequest request, HttpServletResponse response) throws IOException,
         ServletRequestBindingException {
      String username = request.getParameter("username");
      String password = request.getParameter("password");

      HttpClient httpClient = new DefaultHttpClient();
      HttpGet httpGet = new HttpGet(PathUtil.addSlashSuffix(configuration.getBeehiveRESTRootUrl()) + "user/" + username
            + "/openremote.zip");
      
      httpGet.setHeader(Constants.HTTP_AUTHORIZATION_HEADER, Constants.HTTP_BASIC_AUTHORIZATION
            + encode(username, password));

      try {
         HttpResponse resp= httpClient.execute(httpGet);
         if (200 == resp.getStatusLine().getStatusCode()) {
            response.getWriter().print(Constants.OK);
         } else if (401 == resp.getStatusLine().getStatusCode()) {
            response.getWriter().print("wrong");
         } else if (404 == resp.getStatusLine().getStatusCode()) {
             response.getWriter().print("missing");
         }
      } catch (IOException e) {
         response.getWriter().print(e.getMessage());
      }
      return null;
   }

   private void saveUsername(String username) {
      configurationService.addItem("composer_username", username);
   }

   public ModelAndView refreshController(HttpServletRequest request, HttpServletResponse response) throws IOException,
         ServletRequestBindingException {
      try {
         response.getWriter().print(controllerXMLChangeService.refreshController() ? Constants.OK : "failed");
      } catch (ControlCommandException e) {
         response.getWriter().print(e.getMessage());
      }
      return null;
   }

   /**
    * Sets the file service.
    * 
    * @param fileService the new file service
    */
   public void setFileService(FileService fileService) {
      this.fileService = fileService;
   }

   /**
    * Sets the configuration.
    * 
    * @param configuration the new configuration
    */
   public void setConfiguration(ControllerConfiguration configuration) {
      this.configuration = configuration;
   }

   private String encode(String username, String password) {
      Md5PasswordEncoder encoder = new Md5PasswordEncoder();
      String encodedPwd = encoder.encodePassword(password, username);
      if (username == null || encodedPwd == null) {
         return null;
      }
      return new String(Base64.encodeBase64((username + ":" + encodedPwd).getBytes()));
   }
}
