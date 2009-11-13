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
import org.chromattic.api.annotations.OneToOne;
import org.chromattic.api.annotations.Property;
import org.chromattic.api.annotations.RelatedMappedBy;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
@NodeMapping(name = ProducerInfoMapping.NODE_NAME)
public abstract class ProducerInfoMapping
{
   public static final String NODE_NAME = "wsrp:producerinfo";

   @OneToOne
   @RelatedMappedBy("endpoint")
   public abstract EndpointInfoMapping getEndpointInfo();

   @OneToOne
   @RelatedMappedBy("registration")
   public abstract RegistrationInfoMapping getRegistrationInfo();

   @Property(name = "producerid")
   public abstract String getProducerId();

   public abstract void setProducerId(String id);

   @Property(name = "cache")
   public abstract Integer getExpirationCacheSeconds();

   public abstract void setExpirationCacheSeconds(Integer expiration);

   @Property(name = "active")
   public abstract boolean getActive();

   public abstract void setActive(boolean active);

   @Property(name = "available")
   public abstract boolean getAvailable();

   public abstract void setAvailable(boolean available);
}
