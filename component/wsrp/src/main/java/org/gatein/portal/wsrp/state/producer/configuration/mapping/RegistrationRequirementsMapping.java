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

package org.gatein.portal.wsrp.state.producer.configuration.mapping;

import org.chromattic.api.annotations.Create;
import org.chromattic.api.annotations.PrimaryType;
import org.chromattic.api.annotations.OneToMany;
import org.chromattic.api.annotations.Property;
import org.gatein.portal.wsrp.state.mapping.RegistrationPropertyDescriptionMapping;
import org.gatein.wsrp.producer.config.ProducerRegistrationRequirements;
import org.gatein.wsrp.producer.config.impl.ProducerRegistrationRequirementsImpl;
import org.gatein.wsrp.registration.RegistrationPropertyDescription;

import java.util.List;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
@PrimaryType(name = RegistrationRequirementsMapping.NODE_NAME)
public abstract class RegistrationRequirementsMapping
{
   public static final String NODE_NAME = "wsrp:registrationrequirements";

   @Property(name = "registrationrequired")
   public abstract boolean getRegistrationRequired();

   public abstract void setRegistrationRequired(boolean requiresRegistration);

   @Property(name = "registrationrequiredforfulldescription")
   public abstract boolean getRegistrationRequiredForFullDescription();

   public abstract void setRegistrationRequiredForFullDescription(boolean fullServiceDescriptionRequiresRegistration);

   @OneToMany
   public abstract List<RegistrationPropertyDescriptionMapping> getRegistrationPropertyDescriptions();

   @Create
   public abstract RegistrationPropertyDescriptionMapping createRegistrationPropertyDescription(String propertyName);

   public void initFrom(ProducerRegistrationRequirements registrationRequirements)
   {
      setRegistrationRequired(registrationRequirements.isRegistrationRequired());
      setRegistrationRequiredForFullDescription(registrationRequirements.isRegistrationRequiredForFullDescription());

      List<RegistrationPropertyDescriptionMapping> rpdms = getRegistrationPropertyDescriptions();
      for (RegistrationPropertyDescription desc : registrationRequirements.getRegistrationProperties().values())
      {
         RegistrationPropertyDescriptionMapping rpdm = createRegistrationPropertyDescription(desc.getNameAsString());

         // attach first to parent, then init
         rpdms.add(rpdm);
         rpdm.initFrom(desc);
      }
   }

   public ProducerRegistrationRequirements toProducerRegistrationRequirements()
   {
      ProducerRegistrationRequirements req = new ProducerRegistrationRequirementsImpl();

      req.setRegistrationRequired(getRegistrationRequired());
      req.setRegistrationRequiredForFullDescription(getRegistrationRequiredForFullDescription());

      for (RegistrationPropertyDescriptionMapping rpdm : getRegistrationPropertyDescriptions())
      {
         req.addRegistrationProperty(rpdm.toRegistrationPropertyDescription());
      }

      return req;
   }
}
