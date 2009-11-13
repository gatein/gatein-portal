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
import org.chromattic.api.annotations.OneToMany;
import org.chromattic.api.annotations.Property;

import java.io.InputStream;
import java.util.List;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
@NodeMapping(name = RegistrationInfoMapping.NODE_NAME)
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
}
