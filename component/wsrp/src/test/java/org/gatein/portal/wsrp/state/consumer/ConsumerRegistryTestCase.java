/*
 * JBoss, a division of Red Hat
 * Copyright 2009, Red Hat Middleware, LLC, and individual
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

package org.gatein.portal.wsrp.state.consumer;

import org.exoplatform.component.test.*;
import org.gatein.pc.federation.impl.FederatingPortletInvokerService;
import org.gatein.wsrp.WSRPConsumer;
import org.gatein.wsrp.consumer.ConsumerException;
import org.gatein.wsrp.consumer.EndpointConfigurationInfo;
import org.gatein.wsrp.consumer.ProducerInfo;
import org.gatein.wsrp.consumer.RegistrationInfo;

import java.util.Collection;

/**
 * This is essentially the same class as org.gatein.wsrp.state.consumer.ConsumerRegistryTestCase in WSRP consumer module
 * tests.
 *
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
@ConfiguredBy({
   @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.test.jcr-configuration.xml"),
   @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.wsrp-configuration.xml")
})
public class ConsumerRegistryTestCase extends AbstractKernelTest
{
   private JCRConsumerRegistry registry;

   @Override
   protected void setUp() throws Exception
   {
      registry = new JCRConsumerRegistry(getContainer());
      registry.setFederatingPortletInvoker(new FederatingPortletInvokerService());
   }

   public void testCreateAndGet()
   {
      String id = "test";
      WSRPConsumer consumer = registry.createConsumer(id, null, null);
      assertNotNull(consumer);
      assertEquals(id, consumer.getProducerId());
      ProducerInfo info = consumer.getProducerInfo();
      assertNotNull(info);
      assertEquals(consumer.getProducerId(), info.getId());
      EndpointConfigurationInfo endpoint = info.getEndpointConfigurationInfo();
      assertNotNull(endpoint);
      RegistrationInfo regInfo = info.getRegistrationInfo();
      assertTrue(regInfo.isUndetermined());

      WSRPConsumer fromRegistry = registry.getConsumer(id);
      assertNotNull(fromRegistry);
      assertEquals(consumer.getProducerId(), fromRegistry.getProducerId());
      ProducerInfo fromRegistryInfo = fromRegistry.getProducerInfo();
      assertNotNull(fromRegistryInfo);
      assertEquals(fromRegistry.getProducerId(), fromRegistryInfo.getId());
      assertNotNull(fromRegistryInfo.getEndpointConfigurationInfo());
      assertTrue(fromRegistryInfo.getRegistrationInfo().isUndetermined());

      assertEquals(info.getId(), fromRegistryInfo.getId());
      assertEquals(info.getEndpointConfigurationInfo(), fromRegistryInfo.getEndpointConfigurationInfo());
      assertEquals(info.getRegistrationInfo(), fromRegistryInfo.getRegistrationInfo());

      Collection consumers = registry.getConfiguredConsumers();
      assertNotNull(consumers);
      assertEquals(1, consumers.size());
      assertTrue(consumers.contains(consumer));
   }

   public void testGetConsumer()
   {
      assertNull(registry.getConsumer("inexistent"));
   }

   public void testGetProducerInfoByKey()
   {
      WSRPConsumer consumer = registry.createConsumer("id", null, null);
      ProducerInfo info = consumer.getProducerInfo();

      String key = info.getKey();
      assertNotNull(key);

      assertEquals(info, registry.getProducerInfoByKey(key));
   }

   public void testDoubleRegistrationOfConsumerWithSameId()
   {
      String id = "foo";

      registry.createConsumer(id, null, null);

      try
      {
         registry.createConsumer(id, null, null);
         fail("Shouldn't be possible to create a consumer with an existing id");
      }
      catch (ConsumerException expected)
      {
      }
   }

   public void testDelete()
   {
      String id = "id";

      WSRPConsumer consumer = registry.createConsumer(id, null, null);
      assertEquals(consumer, registry.getConsumer(id));

      String key = consumer.getProducerInfo().getKey();

      registry.destroyConsumer(id);

      assertNull(registry.getConsumer(id));
      assertNull(registry.getProducerInfoByKey(key));
   }

   public void testUpdateProducerInfo()
   {
      // create a foo consumer
      String id = "foo";
      WSRPConsumer consumer = registry.createConsumer(id, null, null);
      ProducerInfo info = consumer.getProducerInfo();
      String key = info.getKey();

      // change the id on the consumer's producer info and save it
      info.setId("bar");
      registry.updateProducerInfo(info);

      assertNull(registry.getConsumer(id));
      assertEquals(info, consumer.getProducerInfo());
      assertEquals(info, registry.getProducerInfoByKey(key));
      assertEquals(consumer, registry.getConsumer("bar"));
   }
}
