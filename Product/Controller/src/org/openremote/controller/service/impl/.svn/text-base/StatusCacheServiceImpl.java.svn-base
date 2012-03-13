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
package org.openremote.controller.service.impl;

import java.util.Map;
import java.util.Set;

import org.openremote.controller.service.StatusCacheService;
import org.openremote.controller.statuscache.StatusCache;
/**
 * Implementation of StatusCacheService.
 * 
 * @author Handy.Wang 2009-10-21
 */
public class StatusCacheServiceImpl implements StatusCacheService{
   
   private StatusCache cache;
   
   @Override
   public String getStatusBySensorId(Integer sensorId) {
      return cache.queryStatusBySensorlId(sensorId);
   }

   @Override
   public Map<Integer, String> queryStatuses(Set<Integer> sensorIds) {
      return cache.queryStatuses(sensorIds);
   }

   @Override
   public void saveOrUpdateStatus(Integer sensorId, String newStatus) {
      cache.saveOrUpdateStatus(sensorId, newStatus);
   }
   
   @Override
   public void clearAllStatusCache() {
      cache.clear();
   }

   public void setCache(StatusCache cache) {
      this.cache = cache;
   }
   
}
