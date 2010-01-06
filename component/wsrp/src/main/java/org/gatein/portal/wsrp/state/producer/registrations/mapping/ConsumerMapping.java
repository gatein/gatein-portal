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

package org.gatein.portal.wsrp.state.producer.registrations.mapping;

import org.chromattic.api.RelationshipType;
import org.chromattic.api.annotations.Create;
import org.chromattic.api.annotations.FindById;
import org.chromattic.api.annotations.Id;
import org.chromattic.api.annotations.ManyToOne;
import org.chromattic.api.annotations.MappedBy;
import org.chromattic.api.annotations.PrimaryType;
import org.chromattic.api.annotations.OneToMany;
import org.chromattic.api.annotations.OneToOne;
import org.chromattic.api.annotations.Property;
import org.chromattic.api.annotations.RelatedMappedBy;
import org.gatein.portal.wsrp.state.producer.registrations.JCRRegistrationPersistenceManager;
import org.gatein.registration.Consumer;
import org.gatein.registration.ConsumerGroup;
import org.gatein.registration.Registration;
import org.gatein.registration.RegistrationException;
import org.gatein.registration.RegistrationStatus;
import org.gatein.registration.spi.ConsumerSPI;

import java.util.List;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
@PrimaryType(name = ConsumerMapping.NODE_NAME)
public abstract class ConsumerMapping
{
   public static final String NODE_NAME = "wsrp:consumer";

   @Id
   public abstract String getPersistentKey();

   @Property(name = "name")
   public abstract String getName();

   public abstract void setName(String name);

   @Property(name = "id")
   public abstract String getId();

   public abstract void setId(String id);

   @Property(name = "consumeragent")
   public abstract String getConsumerAgent();

   public abstract void setConsumerAgent(String consumerAgent);

   @Property(name = "status")
   public abstract RegistrationStatus getStatus();

   public abstract void setStatus(RegistrationStatus status);

   @OneToMany
   @RelatedMappedBy("consumer")
   public abstract List<RegistrationMapping> getRegistrations();

   @Create
   public abstract RegistrationMapping createRegistration(String path);

   @ManyToOne(type = RelationshipType.PATH)
   @MappedBy("group")
   public abstract ConsumerGroupMapping getGroup();

   public abstract void setGroup(ConsumerGroupMapping group);

   @OneToOne
   @MappedBy("capabilities")
   public abstract ConsumerCapabilitiesMapping getCapabilities();

   @FindById
   public abstract ConsumerGroupMapping findGroupById(String id);

   @FindById
   public abstract RegistrationMapping findRegistrationById(String id);

   public RegistrationMapping createAndAddRegistrationMappingFrom(Registration registration)
   {
      RegistrationMapping rm;
      if (registration != null)
      {
         // check if the registration has already been persisted and it should already be associated to this ConsumerMapping
         String key = registration.getPersistentKey();
         if (key != null)
         {
            rm = findRegistrationById(key);
         }
         else
         {
            // else create the registration, add to parent
            rm = createRegistration("" + System.nanoTime());
            getRegistrations().add(rm);
         }

         // then init
         rm.initFrom(registration);
      }
      else
      {
         // only create the registration and add to parent
         rm = createRegistration("" + System.nanoTime());
         getRegistrations().add(rm);
      }

      return rm;
   }

   public void initFrom(Consumer consumer)
   {
      setName(consumer.getName());
      setId(consumer.getId());
      setConsumerAgent(consumer.getConsumerAgent());
      setStatus(consumer.getStatus());

      ConsumerGroup group = consumer.getGroup();
      if (group != null)
      {
         ConsumerGroupMapping cgm = findGroupById(group.getPersistentKey());
         setGroup(cgm);
      }

      ConsumerCapabilitiesMapping ccm = getCapabilities();
      ccm.initFrom(consumer.getCapabilities());

      try
      {
         for (Registration reg : consumer.getRegistrations())
         {
            createAndAddRegistrationMappingFrom(reg);
         }
      }
      catch (RegistrationException e)
      {
         throw new RuntimeException(e);
      }
   }

   public ConsumerSPI toConsumer(JCRRegistrationPersistenceManager persistenceManager) throws RegistrationException
   {
      ConsumerSPI consumer = persistenceManager.newConsumerSPI(getId(), getName());
      consumer.setConsumerAgent(getConsumerAgent());
      consumer.setStatus(getStatus());
      consumer.setPersistentKey(getPersistentKey());

      consumer.setCapabilities(getCapabilities().toConsumerCapabilities());

      ConsumerGroupMapping cgm = getGroup();
      if (cgm != null)
      {
         consumer.setGroup(persistenceManager.getConsumerGroup(cgm.getName()));
      }

      for (RegistrationMapping rm : getRegistrations())
      {
         consumer.addRegistration(rm.toRegistration(persistenceManager, consumer));
      }

      return consumer;
   }
}
