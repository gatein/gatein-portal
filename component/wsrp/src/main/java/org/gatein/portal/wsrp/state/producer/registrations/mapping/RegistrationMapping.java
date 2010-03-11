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

package org.gatein.portal.wsrp.state.producer.registrations.mapping;

import org.chromattic.api.RelationshipType;
import org.chromattic.api.annotations.*;
import org.gatein.common.util.ParameterValidation;
import org.gatein.portal.wsrp.state.producer.registrations.JCRRegistrationPersistenceManager;
import org.gatein.registration.Registration;
import org.gatein.registration.RegistrationException;
import org.gatein.registration.RegistrationStatus;
import org.gatein.registration.spi.ConsumerSPI;
import org.gatein.registration.spi.RegistrationSPI;

import javax.xml.namespace.QName;
import java.util.Collections;
import java.util.Map;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
@PrimaryType(name = RegistrationMapping.NODE_NAME)
public abstract class RegistrationMapping
{
   public static final String NODE_NAME = "wsrp:registration";

   @Id
   public abstract String getPersistentKey();

   @ManyToOne(type = RelationshipType.PATH)
   @MappedBy("consumer")
   public abstract ConsumerMapping getConsumer();

   @Property(name = "status")
   public abstract RegistrationStatus getStatus();

   public abstract void setStatus(RegistrationStatus status);

   @Property(name = "registrationhandle")
   public abstract String getRegistrationHandle();

   public abstract void setRegistrationHandle(String handle);

   @OneToOne
   @Owner
   @MappedBy("properties")
   public abstract RegistrationPropertiesMapping getProperties();

   public abstract void setProperties(RegistrationPropertiesMapping rpm);

   @Create
   public abstract RegistrationPropertiesMapping createProperties();

   /**
    * At this point, this RegistrationMapping should already have been added to its parent
    *
    * @param registration
    */
   public void initFrom(Registration registration)
   {
      setStatus(registration.getStatus());
      setRegistrationHandle(registration.getRegistrationHandle());

      Map<QName, Object> properties = registration.getProperties();
      if (ParameterValidation.existsAndIsNotEmpty(properties))
      {
         RegistrationPropertiesMapping rpm = getProperties();
         if (rpm == null)
         {
            rpm = createProperties();
            setProperties(rpm);
         }
         rpm.initFrom(properties);
      }
   }

   public RegistrationSPI toRegistration(JCRRegistrationPersistenceManager persistenceManager, ConsumerSPI consumer) throws RegistrationException
   {
      RegistrationPropertiesMapping rpm = getProperties();
      Map<QName, Object> props = Collections.emptyMap();
      if (rpm != null)
      {
         props = rpm.toPropMap();
      }

      RegistrationSPI reg = persistenceManager.newRegistrationSPI(consumer, props, getPersistentKey());
      reg.setStatus(getStatus());
      reg.setRegistrationHandle(getRegistrationHandle());

      return reg;
   }
}
