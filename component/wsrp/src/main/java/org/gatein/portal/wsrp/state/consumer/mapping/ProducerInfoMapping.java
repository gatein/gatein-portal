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
import org.gatein.portal.wsrp.state.mapping.BaseMapping;
import org.gatein.wsrp.consumer.EndpointConfigurationInfo;
import org.gatein.wsrp.consumer.ProducerInfo;
import org.gatein.wsrp.consumer.RegistrationInfo;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
@PrimaryType(name = ProducerInfoMapping.NODE_NAME)
public abstract class ProducerInfoMapping implements BaseMapping<ProducerInfo>
{
   public static final String NODE_NAME = "wsrp:producerinfo";

   @OneToOne
   @Owner
   @MappedBy("endpoint")
   public abstract EndpointInfoMapping getEndpointInfo();

   @OneToOne
   @Owner
   @MappedBy("registration")
   public abstract RegistrationInfoMapping getRegistrationInfo();

   @Property(name = "producerid")
   public abstract String getId();

   public abstract void setId(String id);

   @Property(name = "cache")
   public abstract Integer getExpirationCacheSeconds();

   public abstract void setExpirationCacheSeconds(Integer expiration);

   @Property(name = "active", defaultValue = "false")
   public abstract boolean getActive();

   public abstract void setActive(boolean active);

   @Id
   public abstract String getKey();

   /* @Property(name = "available")
public abstract boolean getAvailable();

public abstract void setAvailable(boolean available);*/

   public void initFrom(ProducerInfo producerInfo)
   {
      setActive(producerInfo.isActive());
      setExpirationCacheSeconds(producerInfo.getExpirationCacheSeconds());
      setId(producerInfo.getId());

      EndpointInfoMapping eim = getEndpointInfo();
      eim.initFrom(producerInfo.getEndpointConfigurationInfo());

      RegistrationInfoMapping rim = getRegistrationInfo();
      RegistrationInfo regInfo = producerInfo.getRegistrationInfo();
      rim.initFrom(regInfo);
   }

   public ProducerInfo toModel(ProducerInfo initial)
   {
      // todo: should probably use a ProducerInfo implementation backed by mapping at some point
      ProducerInfo info = new ProducerInfo();

      // basic properties
      info.setKey(getKey());
      info.setId(getId());
      info.setActive(getActive());
      info.setExpirationCacheSeconds(getExpirationCacheSeconds());

      // endpoint
      EndpointConfigurationInfo endInfo = getEndpointInfo().toEndpointConfigurationInfo(info.getEndpointConfigurationInfo());
      info.setEndpointConfigurationInfo(endInfo);

      // registration
      RegistrationInfo regInfo = getRegistrationInfo().toRegistrationInfo(info.getRegistrationInfo());
      info.setRegistrationInfo(regInfo);

      return info;
   }
}
