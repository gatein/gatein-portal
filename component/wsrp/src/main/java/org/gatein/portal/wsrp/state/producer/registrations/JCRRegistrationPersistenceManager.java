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

package org.gatein.portal.wsrp.state.producer.registrations;

import org.chromattic.api.ChromatticSession;
import org.exoplatform.container.ExoContainer;
import org.gatein.portal.wsrp.state.JCRPersister;
import org.gatein.portal.wsrp.state.producer.registrations.mapping.ConsumerCapabilitiesMapping;
import org.gatein.portal.wsrp.state.producer.registrations.mapping.ConsumerGroupMapping;
import org.gatein.portal.wsrp.state.producer.registrations.mapping.ConsumerMapping;
import org.gatein.portal.wsrp.state.producer.registrations.mapping.ConsumersAndGroupsMapping;
import org.gatein.portal.wsrp.state.producer.registrations.mapping.RegistrationMapping;
import org.gatein.portal.wsrp.state.producer.registrations.mapping.RegistrationPropertiesMapping;
import org.gatein.registration.ConsumerGroup;
import org.gatein.registration.Registration;
import org.gatein.registration.RegistrationException;
import org.gatein.registration.impl.RegistrationPersistenceManagerImpl;
import org.gatein.registration.spi.ConsumerGroupSPI;
import org.gatein.registration.spi.ConsumerSPI;
import org.gatein.registration.spi.RegistrationSPI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class JCRRegistrationPersistenceManager extends RegistrationPersistenceManagerImpl
{
//   private NewJCRPersister persister;
   private JCRPersister persister;
   private ConsumersAndGroupsMapping mappings;

   public JCRRegistrationPersistenceManager(ExoContainer container) throws Exception
   {
      persister = new JCRPersister(container);

      List<Class> mappingClasses = new ArrayList<Class>(5);
      Collections.addAll(mappingClasses, ConsumersAndGroupsMapping.class, ConsumerMapping.class, ConsumerGroupMapping.class,
         RegistrationMapping.class, ConsumerCapabilitiesMapping.class, RegistrationPropertiesMapping.class);

      persister.initializeBuilderFor(mappingClasses);

//      persister = NewJCRPersister.getInstance(container);

      ChromatticSession session = persister.getSession();
      mappings = session.findByPath(ConsumersAndGroupsMapping.class, ConsumersAndGroupsMapping.NODE_NAME);
      if (mappings == null)
      {
         mappings = session.insert(ConsumersAndGroupsMapping.class, ConsumersAndGroupsMapping.NODE_NAME);
      }
      persister.closeSession(session, true);

      for (ConsumerGroupMapping cgm : mappings.getConsumerGroups())
      {
         internalAddConsumerGroup(cgm.toConsumerGroup(this));
      }

      for (ConsumerMapping cm : mappings.getConsumers())
      {
         internalAddConsumer(cm.toConsumer(this));
      }
   }

   @Override
   protected RegistrationSPI internalRemoveRegistration(String registrationId)
   {
      Registration registration = getRegistration(registrationId);
      remove(registration.getPersistentKey(), RegistrationMapping.class);

      return super.internalRemoveRegistration(registrationId);
   }

   @Override
   protected RegistrationSPI internalCreateRegistration(ConsumerSPI consumer, Map registrationProperties)
   {
      ChromatticSession session = persister.getSession();
      RegistrationSPI registration = null;
      try
      {
         ConsumerMapping cm = session.findById(ConsumerMapping.class, consumer.getPersistentKey());
         RegistrationMapping rm = cm.createAndAddRegistrationMappingFrom(null);
         registration = newRegistrationSPI(consumer, registrationProperties, rm.getPersistentKey());
         rm.initFrom(registration);
         persister.closeSession(session, true);
      }
      catch (Exception e)
      {
         e.printStackTrace(); // todo fix me
         persister.closeSession(session, false);
      }

      return registration;
   }

   @Override
   protected ConsumerSPI internalRemoveConsumer(String consumerId)
   {
      remove(consumerId, ConsumerMapping.class);

      return super.internalRemoveConsumer(consumerId);
   }

   private void remove(String id, Class clazz)
   {
      ChromatticSession session = persister.getSession();
      session.remove(session.findById(clazz, id));
      persister.closeSession(session, true);
   }

   @Override
   protected ConsumerSPI internalCreateConsumer(String consumerId, String consumerName)
   {
      ConsumerSPI consumer = super.internalCreateConsumer(consumerId, consumerName);

      ChromatticSession session = persister.getSession();
      mappings = session.findByPath(ConsumersAndGroupsMapping.class, ConsumersAndGroupsMapping.NODE_NAME); // todo: needed?
      try
      {
         ConsumerMapping cm = mappings.createConsumer(consumerId);
         mappings.getConsumers().add(cm);
         cm.initFrom(consumer);
         consumer.setPersistentKey(cm.getPersistentKey());
         persister.closeSession(session, true);
      }
      catch (Exception e)
      {
         e.printStackTrace(); // todo: fix me
         persister.closeSession(session, false);
      }

      return consumer;
   }

   @Override
   protected ConsumerGroupSPI internalRemoveConsumerGroup(String name)
   {
      try
      {
         ConsumerGroup group = getConsumerGroup(name);
         remove(group.getPersistentKey(), ConsumerGroupMapping.class);
      }
      catch (RegistrationException e)
      {
         throw new IllegalArgumentException("Couldn't remove ConsumerGroup '" + name + "'", e);
      }

      return super.internalRemoveConsumerGroup(name);
   }

   @Override
   protected ConsumerGroupSPI internalCreateConsumerGroup(String name)
   {
      ConsumerGroupSPI group = super.internalCreateConsumerGroup(name);

      ChromatticSession session = persister.getSession();
      try
      {
         ConsumerGroupMapping cgm = mappings.createConsumerGroup(name);
         mappings.getConsumerGroups().add(cgm);
         group.setPersistentKey(cgm.getPersistentKey());
         cgm.initFrom(group);
         persister.closeSession(session, true);
      }
      catch (Exception e)
      {
         e.printStackTrace();  // todo: fix me
         persister.closeSession(session, false);
      }

      return group;
   }
}
