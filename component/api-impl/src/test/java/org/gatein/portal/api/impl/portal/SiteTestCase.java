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

import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.pom.data.PageKey;
import org.exoplatform.portal.pom.data.PortalKey;
import org.gatein.api.portal.Site;
import org.gatein.api.util.GateInTypesResolver;
import org.gatein.api.util.Type;
import org.testng.annotations.Test;

/** @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a> */
public class SiteTestCase
{
   @Test
   public void checkThatTypesAreProperlyResolvedEvenIfClassIsNotLoaded()
   {
      Type portal = GateInTypesResolver.forName("portal", Site.class);
      assert portal != null;
   }

   @Test
   public void getAPITypeShouldProperlyResolve()
   {
      assert Site.DASHBOARD.equals(SiteImpl.getAPITypeFrom(new PageKey(SiteType.USER.getName(), "foo", "foo")));
      assert Site.GROUP.equals(SiteImpl.getAPITypeFrom(new PageKey(SiteType.GROUP.getName(), "foo", "foo")));
      assert Site.PORTAL.equals(SiteImpl.getAPITypeFrom(new PortalKey(SiteType.PORTAL.getName(), "foo")));
      assert Site.PORTAL.equals(SiteImpl.getAPITypeFrom(new PortalKey("portal", "foo")));
   }
}
