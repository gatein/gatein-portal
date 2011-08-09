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

package org.gatein.portal.api.impl.portal;

import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.pom.data.PortalData;
import org.gatein.api.content.ContentRegistry;
import org.gatein.api.portal.Portal;
import org.gatein.api.portal.Site;
import org.gatein.api.util.Type;
import org.gatein.portal.api.impl.GateInImpl;
import org.gatein.portal.api.impl.content.ContentRegistryImpl;

/** @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a> */
public class PortalImpl extends SiteImpl implements Portal
{

   public PortalImpl(PortalData portal, GateInImpl gateIn)
   {
      super(gateIn.siteId(getAPITypeFrom(portal.getKey()), portal.getKey().getId()), portal.getName(), gateIn);
   }

   public ContentRegistry getContentRegistry()
   {
      return new ContentRegistryImpl(getGateInImpl(), this);
   }

   public Type getType()
   {
      return Site.PORTAL;
   }

   protected SiteKey getSiteKey()
   {
      return SiteKey.portal(getName());
   }
}
