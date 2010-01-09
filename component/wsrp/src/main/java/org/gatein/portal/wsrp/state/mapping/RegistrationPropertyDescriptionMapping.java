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

package org.gatein.portal.wsrp.state.mapping;

import org.chromattic.api.annotations.PrimaryType;
import org.chromattic.api.annotations.Property;
import org.gatein.common.util.ParameterValidation;
import org.gatein.wsrp.registration.LocalizedString;
import org.gatein.wsrp.registration.RegistrationPropertyDescription;

import javax.xml.namespace.QName;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
@PrimaryType(name = RegistrationPropertyDescriptionMapping.NODE_NAME)
public abstract class RegistrationPropertyDescriptionMapping
{
   public static final String NODE_NAME = "wsrp:registrationpropertydescription";

   @Property(name = "name")
   public abstract String getName(); // todo: this should really be a QName

   public abstract void setName(String name);


   @Property(name = "type")
   public abstract String getType(); // todo: this should really be a QName

   public abstract void setType(String type);

   @Property(name = "description")
   public abstract String getDescription(); // todo: this should really be a LocalizedString

   public abstract void setDescription(String description);

   @Property(name = "hint")
   public abstract String getHint(); // todo: this should really be a LocalizedString

   public abstract void setHint(String hint);

   @Property(name = "label")
   public abstract String getLabel(); // todo: this should really be a LocalizedString

   public abstract void setLabel(String label);

   public void initFrom(RegistrationPropertyDescription desc)
   {
      LocalizedString description = desc.getDescription();
      if (description != null)
      {
         setDescription(description.getValue());
      }
      LocalizedString hint = desc.getHint();
      if (hint != null)
      {
         setHint(hint.getValue());
      }
      LocalizedString label = desc.getLabel();
      if (label != null)
      {
         setLabel(label.getValue());
      }

      // convert QNames to Strings
      setName(desc.getName().toString());
      setType(desc.getType().toString());
   }

   public RegistrationPropertyDescription toRegistrationPropertyDescription()
   {
      RegistrationPropertyDescription desc = new RegistrationPropertyDescription(getName(), QName.valueOf(getType()));
      String description = getDescription();
      if (!ParameterValidation.isNullOrEmpty(description))
      {
         desc.setDefaultDescription(description);
      }
      String hint = getHint();
      if (!ParameterValidation.isNullOrEmpty(hint))
      {
         desc.setHint(new LocalizedString(hint));
      }
      String label = getLabel();
      if (!ParameterValidation.isNullOrEmpty(label))
      {
         desc.setLabel(new LocalizedString(label));
      }

      return desc;
   }
}
