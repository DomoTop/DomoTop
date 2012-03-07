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
package org.openremote.controller.statuscache;

import org.apache.log4j.Logger;
import org.openremote.controller.command.StatusCommand;
import org.openremote.controller.component.Sensor;
import org.openremote.controller.service.StatusCacheService;

/**
 * 
 * @author Handy.Wang 2009-03-17
 */
public class PollingMachineThread extends Thread
{

  private Sensor sensor;
	private StatusCacheService statusCacheService;
	private String lastStatus = StatusCommand.UNKNOWN_STATUS;
	private static final long INTERVAL = 500;
	private boolean alive = true;
	private static Logger logger = Logger.getLogger(PollingMachineThread.class);
	
	/** milliseconds */
	private long pollingMachineInterval;
	
  public PollingMachineThread(Sensor sensor, StatusCacheService statusCacheService)
  {
    this(INTERVAL, sensor, statusCacheService);
  }
	
	public PollingMachineThread(long pollingMachineInterval, Sensor sensor,
                              StatusCacheService statusCacheService)
  {
    this.pollingMachineInterval = pollingMachineInterval;
    this.sensor = sensor;
    this.statusCacheService = statusCacheService;
  }


	@Override public void run()
  {
System.out.println(" -------- Started thread for sensor " + sensor);

		while (alive)
    {
		   lastStatus = sensor.readStatus();
			statusCacheService.saveOrUpdateStatus(sensor.getSensorID(), lastStatus);

			try
      {
				Thread.sleep(pollingMachineInterval);
			}
      catch (InterruptedException e)
      {
			   logger.error("PollingMachineThread is interrupted", e);

        // TODO : must be fixed to interrupt correctly
			}
		}
	}
	
	public void kill()
  {
	   this.alive = false;
	}

   public String getLastStatus() {
      return lastStatus;
   }

   public Sensor getSensor() {
      return sensor;
   }
	
}
