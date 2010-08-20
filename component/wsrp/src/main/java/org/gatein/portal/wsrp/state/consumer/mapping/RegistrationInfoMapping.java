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

import org.chromattic.api.annotations.Create;
import org.chromattic.api.annotations.FormattedBy;
import org.chromattic.api.annotations.OneToMany;
import org.chromattic.api.annotations.PrimaryType;
import org.chromattic.api.annotations.Property;
import org.exoplatform.commons.utils.Safe;
import org.gatein.portal.wsrp.state.JCRPersister;
import org.gatein.portal.wsrp.state.mapping.RegistrationPropertyDescriptionMapping;
import org.gatein.wsrp.consumer.RegistrationInfo;
import org.gatein.wsrp.consumer.RegistrationProperty;
import org.gatein.wsrp.registration.RegistrationPropertyDescription;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
@PrimaryType(name = RegistrationInfoMapping.NODE_NAME)
@FormattedBy(JCRPersister.QNameFormatter.class)
public abstract class RegistrationInfoMapping
{
   public static final String NODE_NAME = "wsrp:registrationinfo";

   @Property(name = "consumername")
   public abstract String getConsumerName();

   public abstract void setConsumerName(String name);

   @Property(name = "handle")
   public abstract String getRegistrationHandle();

   public abstract void setRegistrationHandle(String handle);

   @Property(name = "state")
   public abstract InputStream getRegistrationState();

   public abstract void setRegistrationState(InputStream state);

   @OneToMany
   public abstract List<RegistrationPropertyMapping> getRegistrationProperties();

   @Create
   public abstract RegistrationPropertyMapping createRegistrationProperty(String propertyName);

   public void initFrom(RegistrationInfo regInfo)
   {
      setConsumerName(regInfo.getConsumerName());
      setRegistrationHandle(regInfo.getRegistrationHandle());
      byte[] bytes = regInfo.getRegistrationState();
      if (bytes != null && bytes.length > 0)
      {
         ByteArrayInputStream is = new ByteArrayInputStream(bytes);
         setRegistrationState(is);
      }

      // clear and recreate registration properties
      List<RegistrationPropertyMapping> rpms = getRegistrationProperties();
      rpms.clear();
      for (RegistrationProperty property : regInfo.getRegistrationProperties().values())
      {
         // create new RegistrationPropertyMapping for this RegistrationInfoMapping
         RegistrationPropertyMapping rpm = createRegistrationProperty(property.getName().toString());

         // add newly created RegistrationPropertyMapping to parent then initialize for JCR
         rpms.add(rpm);
         rpm.initFrom(property);
      }
   }

   RegistrationInfo toRegistrationInfo(RegistrationInfo initial)
   {
      initial.setConsumerName(getConsumerName());
      initial.setRegistrationHandle(getRegistrationHandle());
      initial.setRegistrationState(Safe.getBytes(getRegistrationState()));

      // registration properties
      for (RegistrationPropertyMapping rpm : getRegistrationProperties())
      {
         RegistrationProperty prop = initial.setRegistrationPropertyValue(rpm.getName(), rpm.getValue());

         RegistrationPropertyDescriptionMapping rpdm = rpm.getDescription();
         if (rpdm != null)
         {
            RegistrationPropertyDescription desc = rpdm.toRegistrationPropertyDescription();
            prop.setDescription(desc);
         }

         prop.setStatus(rpm.getStatus());

         // set RegistrationInfo as listener of property changes
         prop.setListener(initial);
      }

      return initial;
   }
}
