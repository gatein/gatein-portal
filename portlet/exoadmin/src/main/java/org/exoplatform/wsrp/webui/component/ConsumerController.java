/*
 * JBoss, a division of Red Hat
 * Copyright 2010, Red Hat Middleware, LLC, and individual
 * contributors as indicated by the @authors tag. See the
 * copyright.txt in the distribution for a full listing of
 * individual contributors.
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

package org.exoplatform.wsrp.webui.component;

import org.gatein.pc.api.PortletInvokerException;
import org.gatein.wsrp.WSRPConsumer;
import org.gatein.wsrp.consumer.RefreshResult;
import org.gatein.wsrp.consumer.registry.ConsumerRegistry;

import java.util.List;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class ConsumerController
{
   private ConsumerRegistry registry;

   public ConsumerController(ConsumerRegistry registry)
   {
      this.registry = registry;
   }

   public ConsumerRegistry getRegistry()
   {
      return registry;
   }

   public WSRPConsumer getConsumer(String id)
   {
      return registry.getConsumer(id);
   }

   public List<WSRPConsumer> getConfiguredConsumers()
   {
      return registry.getConfiguredConsumers();
   }

   public RefreshResult refreshConsumer(WSRPConsumer consumer) throws PortletInvokerException
   {
      RefreshResult result = consumer.refresh(true);

      if (result.hasIssues())
      {
         // refresh had issues, we should deactivate this consumer
         registry.deactivateConsumerWith(consumer.getProducerId());
      }
      else
      {
         // activate the consumer if it's supposed to be active
         if (consumer.isActive())
         {
            registry.activateConsumerWith(consumer.getProducerId());
         }
         else
         {
            registry.deactivateConsumerWith(consumer.getProducerId());
         }
      }

      return result;
   }
}
