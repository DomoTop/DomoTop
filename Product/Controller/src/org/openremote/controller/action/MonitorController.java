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
package org.openremote.controller.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openremote.controller.config.ControllerXMLListenSharingData;
import org.openremote.controller.spring.SpringContext;
import org.openremote.controller.statuscache.StatusCache;
import org.openremote.controller.service.ServiceContext;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * Debug monitor that shows all sensors and polling treads detail, including {@link StatusCache}
 * and {@link ControllerXMLListenSharingData}.
 * 
 * @author Dan Cong
 */
public class MonitorController extends AbstractController
{

  private ControllerXMLListenSharingData controllerXMLListenSharingData = (ControllerXMLListenSharingData) SpringContext
       .getInstance().getBean("controllerXMLListenSharingData");

  private StatusCache cache = ServiceContext.getDeviceStateCache();


  @Override protected ModelAndView handleRequestInternal(HttpServletRequest req,
                                                        HttpServletResponse res) throws Exception
  {
    ModelAndView mav = new ModelAndView("monitor");

    mav.addObject("threads", controllerXMLListenSharingData.getPollingMachineThreads());
    mav.addObject("xmlChanged", controllerXMLListenSharingData.getIsControllerXMLChanged());
    mav.addObject("controller.xml", controllerXMLListenSharingData.getControllerXMLFileContent());
    mav.addObject("panel.xml", controllerXMLListenSharingData.getPanelXMLFileContent());
    mav.addObject("records", cache.getChangedStatusTable().getRecordList());
    mav.addObject("sensors", controllerXMLListenSharingData.getSensors());

    return mav;
  }

}

