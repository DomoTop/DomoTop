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
package org.openremote.controller.servlet;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openremote.controller.service.FileService;
import org.openremote.controller.service.ServiceContext;
import org.springframework.util.FileCopyUtils;

/**
 * The Servlet to get the files in resource folder.
 * 
 * @author Dan 2009-6-9
 */
@SuppressWarnings("serial")
public class ResourceServlet extends HttpServlet
{

  private static FileService fileService = ServiceContext.getFileResourceService();

  @Override protected void doGet(HttpServletRequest request, HttpServletResponse response)
                                                            throws ServletException, IOException
  {
    String relativePath = request.getPathInfo();
    InputStream is = fileService.findResource(relativePath);

    if (is != null)
    {
      FileCopyUtils.copy(is, response.getOutputStream());
    }

    else
    {
      response.sendError(HttpServletResponse.SC_NOT_FOUND, relativePath);
    }
  }

}
