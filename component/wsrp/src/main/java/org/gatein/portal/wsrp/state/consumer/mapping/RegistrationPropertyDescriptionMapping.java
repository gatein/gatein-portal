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

package org.gatein.portal.wsrp.state.consumer.mapping;

import org.chromattic.api.annotations.NodeMapping;
import org.chromattic.api.annotations.Property;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
@NodeMapping(name = RegistrationPropertyDescriptionMapping.NODE_NAME)
public abstract class RegistrationPropertyDescriptionMapping
{
   public static final String NODE_NAME = "wsrp:registrationpropertydescription";

   // todo: this should really be a QName
   @Property(name = "name")
   public abstract String getName();

   public abstract void setName(String name);

   // todo: this should really be a QName
   @Property(name = "type")
   public abstract String getType();

   public abstract void setType(String type);


   // todo: this should really be a LocalizedString
   @Property(name = "description")
   public abstract String getDescription();

   public abstract void setDescription(String description);

   // todo: this should really be a LocalizedString
   @Property(name = "hint")
   public abstract String getHint();

   public abstract void setHint(String hint);

   // todo: this should really be a LocalizedString
   @Property(name = "label")
   public abstract String getLabel();

   public abstract void setLabel(String label);
}
