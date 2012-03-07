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
package org.openremote.controller.service;

import java.io.InputStream;

/**
 * The service for file system operation.
 * 
 * @author Dan 2009-5-14
 */
public interface FileService {
   
   /**
    * Unzip.
    * 
    * @param inputStream the input stream
    * @param targetDir the target dir
    */
   boolean unzip(InputStream inputStream, String targetDir);
   
   /**
    * Upload config zip.
    * 
    * @param inputStream the input stream
    */
   boolean uploadConfigZip(InputStream inputStream);
   
   /**
    * Find resource.
    * 
    * @param relativePath the relative path
    * 
    * @return the input stream
    */
   InputStream findResource(String relativePath);
   
   /**
    * Sync configuration (openremote.zip) with Modeler.
    * 
    * @param username
    *           Modeler username
    * @param password
    *           Modeler password
    * @return true if success.
    */
   boolean syncConfigurationWithModeler(String username, String password);

}
