/* OpenRemote, the Home of the Digital Home.
* Copyright 2008-2009, OpenRemote Inc.
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
package org.openremote.modeler.service;

import java.io.File;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;

import org.openremote.modeler.client.utils.PanelsAndMaxOid;
import org.openremote.modeler.domain.Panel;
import org.openremote.modeler.domain.Template;

/**
 * The Interface ResourceService.
 * 
 * @author Allen, Handy
 */

public interface ResourceService {
   
   /**
    * Download zip resource.
    * 
    * @param maxId the max id
    * @param sessionId the session id
    * @param activities the activities
    * 
    * @return the string
    */
   String downloadZipResource(long maxId, String sessionId, List<Panel> panels/*, List<Group> groups, List<Screen> screens*/);
   
   /**
    * Gets the irb file from zip.
    * 
    * @param inputStream the input stream
    * @param sessionId the session id
    * 
    * @return the irb file from zip
    */
   String getDotImportFileForRender(String sessionId, InputStream inputStream);
   
   /**
    * Upload image.
    * 
    * @param inputStream the input stream
    * @param fileName the file name
    * @param sessionId the session id
    * 
    * @return the file
    */
   File uploadImage(InputStream inputStream, String fileName, String sessionId);
   
   File uploadImage(InputStream inputStream, String fileName);
   
   /**
    * Gets the relative resource path.
    * 
    * @param sessionId the session id
    * @param fileName the file name
    * 
    * @return the relative resource path
    */
   String getRelativeResourcePath(String sessionId, String fileName);
   
   String getRelativeResourcePathByCurrentAccount(String fileName);
   
   String getPanelsJson(Collection<Panel> panels);
   
   void initResources(Collection<Panel> panels,long maxOid);
   
   PanelsAndMaxOid restore();
   
   boolean canRestore();
   
   void saveResourcesToBeehive(Collection<Panel> panels);
   void saveTemplateResourcesToBeehive(Template Template);
   void downloadResourcesForTemplate(long templateOid); 
   
   File getTemplateResource(Template template); 
}