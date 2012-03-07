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

import org.jdom.Document;

/**
 * This service is for polling the status of components of hardwares people cared.<br /><br />
 * 
 * There are two steps:<br />
 * 1) While starting up <b>APP SERVER</b>(tomcat), init the <b>STATUSCACHE</b> with all sensor ids parsed from <b>CONTROLLER</b>.xml 
 * and their initialized statuses queried by <b>STATUSCOMMAND</b> from hardware.<br />
 * 2) Create looped threads initialized by <b>STATUSCOMMAND</b>, and these threads will scan the latest statuses of hardware every interval-time. 
 * And then update the statusCache if corresponding status changed. 
 * 
 * @author Handy.Wang 2010-03-17
 */
public interface PollingMachinesService {
   /** init the <b>STATUSCACHE</b> with all sensor ids parsed from <b>CONTROLLER</b>.xml and their statuses */
   public void initStatusCacheWithControllerXML(Document document);
   
   /** Create looped threads initialized by <b>STATUSCOMMAND</b> */
   public void startPollingMachineMultiThread();
}
