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

package org.gatein.portal.wsrp.state.consumer.mapping;

import org.chromattic.api.annotations.*;
import org.gatein.portal.wsrp.state.mapping.RegistrationPropertyDescriptionMapping;
import org.gatein.wsrp.consumer.RegistrationProperty;
import org.gatein.wsrp.registration.RegistrationPropertyDescription;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
@PrimaryType(name = RegistrationPropertyMapping.NODE_NAME)
public abstract class RegistrationPropertyMapping
{
   public static final String NODE_NAME = "wsrp:registrationproperty";

   @Property(name = "name")
   public abstract String getName();

   public abstract void setName(String name);

   @Property(name = "value")
   public abstract String getValue();

   public abstract void setValue(String value);

   @OneToOne
   @Owner
   @MappedBy("description")
   public abstract RegistrationPropertyDescriptionMapping getDescription();

   public abstract void setDescription(RegistrationPropertyDescriptionMapping rpdm);

   @Create
   public abstract RegistrationPropertyDescriptionMapping createDescription();

   @Property(name = "status")
   public abstract RegistrationProperty.Status getStatus();

   public abstract void setStatus(RegistrationProperty.Status status);

   public void initFrom(RegistrationProperty property)
   {
      // set properties
      setName(property.getName());
      setStatus(property.getStatus());
      setValue(property.getValue());

      // description
      RegistrationPropertyDescription desc = property.getDescription();
      if (desc != null)
      {
         RegistrationPropertyDescriptionMapping rpdm = createDescription();
         setDescription(rpdm);
         rpdm.initFrom(desc);
      }
   }
}
