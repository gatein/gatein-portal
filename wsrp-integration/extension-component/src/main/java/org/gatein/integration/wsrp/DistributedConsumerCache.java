/*
* JBoss, a division of Red Hat
* Copyright 2008, Red Hat Middleware, LLC, and individual contributors as indicated
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* This is free software; you can redistribute it and/or modify it
* under the terms of the GNU Lesser General Public License as
* published by the Free Software Foundation; either version 2.1 of
* the License, or (at your option) any later version.
*
* This software is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
* Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with this software; if not, write to the Free
* Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
* 02110-1301 USA, or see the FSF site: http://www.fsf.org.
*/

package org.gatein.integration.wsrp;

import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.wsrp.WSRPConsumer;
import org.gatein.wsrp.consumer.registry.ConsumerCache;

import java.util.Collection;
import java.util.Collections;

/** @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a> */
public class DistributedConsumerCache implements ConsumerCache
{
   private static final Logger log = LoggerFactory.getLogger(DistributedConsumerCache.class);

   private ExoCache<String, WSRPConsumer> cache;
   private boolean invalidated;

   public DistributedConsumerCache(CacheService cacheService)
   {
      this.cache = cacheService.getCacheInstance(DistributedConsumerCache.class.getSimpleName());
   }

   public Collection<WSRPConsumer> getConsumers()
   {
      try
      {
         return (Collection<WSRPConsumer>)cache.getCachedObjects();
      }
      catch (Exception e)
      {
         log.debug(e);
         return Collections.emptyList();
      }
   }

   public WSRPConsumer getConsumer(String id)
   {
      return cache.get(id);
   }

   public WSRPConsumer removeConsumer(String id)
   {
      return cache.remove(id);
   }

   public void putConsumer(String id, WSRPConsumer consumer)
   {
      cache.put(id, consumer);
   }

   public void clear()
   {
      cache.clearCache();
   }

   public boolean isInvalidated()
   {
      return invalidated;
   }

   public void setInvalidated(boolean invalidated)
   {
      this.invalidated = invalidated;
   }

   public long getLastModified()
   {
      return 0;
   }

   public void initFromStorage()
   {
      //To change body of implemented methods use File | Settings | File Templates.
   }

   public void markAsModifiedNow()
   {
      //To change body of implemented methods use File | Settings | File Templates.
   }
}
