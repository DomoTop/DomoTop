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
package org.openremote.controller.service;

import org.openremote.controller.ControllerConfiguration;
import org.openremote.controller.LutronHomeWorksConfig;
import org.openremote.controller.RoundRobinConfiguration;
import org.openremote.controller.RussoundConfiguration;
import org.openremote.controller.command.RemoteActionXMLParser;
import org.openremote.controller.statuscache.StatusCache;

/**
 * This class defines an abstract service context without compile time links to any particular
 * service container implementation. <p>
 *
 * Service context can be instantiated/registered only once per VM/classloader. The subclasses
 * are expected to register themselves directly from their constructor via
 * {@link #registerServiceContext(ServiceContext)} method. Only one instance can be registered. <p>
 *
 * The actual service provider is bound at runtime and can therefore vary according to deployment
 * environment.
 *
 * @author <a href="mailto:juha@openremote.org">Juha Lindfors</a>
 */
public abstract class ServiceContext
{


  // TODO :
  //        the various configuration related services (e.g. lutron) need a better API (higher
  //        degree of de-typing)



  // Constants ------------------------------------------------------------------------------------

  /**
   * Mutex for service context singleton to protect against multi-thread race conditions.
   */
  private final static Object SINGLETON_MUTEX = new Object();


  // Enums ----------------------------------------------------------------------------------------

  public static enum ServiceName
  {
    CONTROLLER_XML_PARSER("remoteActionXMLParser"),

    CONTROLLER_CONFIGURATION("configuration"),

    ROUND_ROBIN_CONFIGURATION("roundRobinConfig"),

    LUTRON_HOMEWORKS_CONFIGURATION("lutronHomeWorksConfig"),

    RUSSOUND_CONFIGURATION("russoundConfiguration"),

    DEVICE_POLLING("pollingMachinesService"),

    DEVICE_STATE_CACHE("statusCache"),

    FILE_RESOURCE_SERVICE("fileService");


    private String springBeanName;

    private ServiceName(String springBeanName)
    {
      this.springBeanName = springBeanName;
    }

    public String getSpringBeanName()
    {
      return springBeanName;
    }
  }


  // Class Members --------------------------------------------------------------------------------

  /**
   * Service context singleton instance.
   */
  private static ServiceContext singletonInstance;



  public static ControllerConfiguration getControllerConfiguration()
  {
    try
    {
      return (ControllerConfiguration)getInstance().getService(ServiceName.CONTROLLER_CONFIGURATION);
    }

    catch (ClassCastException e)
    {
      throw new Error(
          "Controller Configuration service implementation has had an incompatible change.", e
      );
    }
  }

  public static RoundRobinConfiguration getRoundRobinConfiguration()
  {
    try
    {
      return (RoundRobinConfiguration)getInstance().getService(ServiceName.ROUND_ROBIN_CONFIGURATION);
    }

    catch (ClassCastException e)
    {
      throw new Error(
          "Roundrobin Configuration service implementation has had an incompatible change.", e
      );
    }
  }

  public static LutronHomeWorksConfig getLutronHomeWorksConfiguration()
  {
    try
    {
      return (LutronHomeWorksConfig)getInstance().getService(ServiceName.LUTRON_HOMEWORKS_CONFIGURATION);
    }

    catch (ClassCastException e)
    {
      throw new Error(
          "Lutron HomeWorks Configuration service has had an incompatible change.", e
      );
    }
  }

  public static RussoundConfiguration getRussoundConfiguration()
  {
    try
    {
      return (RussoundConfiguration)getInstance().getService(ServiceName.RUSSOUND_CONFIGURATION);
    }

    catch (ClassCastException e)
    {
      throw new Error(
          "Lutron HomeWorks Configuration service has had an incompatible change.", e
      );
    }
  }

  public static RemoteActionXMLParser getControllerXMLParser()
  {
    try
    {
      return (RemoteActionXMLParser)getInstance().getService(ServiceName.CONTROLLER_XML_PARSER);
    }

    catch (ClassCastException e)
    {
      throw new Error(
          "Controller XML parser service implementation has had an incompatible change.", e
      );
    }
  }


  public static PollingMachinesService getDevicePollingService()
  {
    try
    {
      return (PollingMachinesService)getInstance().getService(ServiceName.DEVICE_POLLING);
    }

    catch (ClassCastException e)
    {
      throw new Error(
          "Device polling service implementation has had an incompatible change.", e
      );
    }
  }

  public static StatusCache getDeviceStateCache()
  {
    try
    {
      return (StatusCache)getInstance().getService(ServiceName.DEVICE_STATE_CACHE);
    }

    catch (ClassCastException e)
    {
      throw new Error(
          "Device state cache service implementation has had an incompatible change.", e
      );
    }
  }


  public static FileService getFileResourceService()
  {
    try
    {
      return (FileService)getInstance().getService(ServiceName.FILE_RESOURCE_SERVICE);
    }

    catch (ClassCastException e)
    {
      throw new Error(
          "File resource service implementation has had an incompatible change.", e
      );
    }
  }


  public static ServiceContext getInstance()    // TODO : go to private once spring context getinstance has been removed
  {
    synchronized (SINGLETON_MUTEX)
    {
      if (singletonInstance == null)
      {
        throw new IllegalStateException(
            "An attempt was made to access service context before it was initialized."
        );
      }

      else
      {
        return singletonInstance;
      }
    }
  }


  protected static void registerServiceContext(ServiceContext ctx) throws InstantiationException
  {
    synchronized (SINGLETON_MUTEX)
    {
      if (singletonInstance != null)
      {
        throw new InstantiationException(
            "A service context has already been initialized and registered. " +
            "This registration can only be done once."
        );
      }

      else
      {
        singletonInstance = ctx;
      }
    }
  }


  // Constructors ---------------------------------------------------------------------------------

  protected ServiceContext()
  {
    
  }


  public abstract Object getService(ServiceName name);


}
