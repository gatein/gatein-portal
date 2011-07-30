/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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

package org.exoplatform.portal.mop.management.operations;

import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.SiteType;
import org.gatein.mop.api.workspace.ObjectType;
import org.gatein.mop.api.workspace.Site;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class Utils
{

   private Utils()
   {
   }

   public static ObjectType<Site> getObjectType(SiteType siteType)
   {
      switch (siteType)
      {
         case PORTAL:
            return ObjectType.PORTAL_SITE;
         case GROUP:
            return ObjectType.GROUP_SITE;
         case USER:
            return ObjectType.USER_SITE;
         default:
            return null;
      }
   }

   public static SiteType getSiteType(ObjectType<? extends Site> objectType)
   {
      if (ObjectType.PORTAL_SITE == objectType)
      {
         return SiteType.PORTAL;
      }
      else if (ObjectType.GROUP_SITE == objectType)
      {
         return SiteType.GROUP;
      }
      else if (ObjectType.USER_SITE == objectType)
      {
         return SiteType.USER;
      }
      else
      {
         return null;
      }
   }

   public static SiteType getSiteType(String siteType)
   {
      if (siteType == null) return null;

      return SiteType.valueOf(siteType.toUpperCase());
   }


   public static SiteKey siteKey(String siteType, String siteName)
   {
      SiteType st = getSiteType(siteType);
      return siteKey(st, siteName);
   }

   public static SiteKey siteKey(SiteType siteType, String siteName)
   {
      switch (siteType)
      {
         case PORTAL:
            return SiteKey.portal(siteName);
         case GROUP:
            if (siteName.charAt(0) != '/') siteName = "/" + siteName;
            return SiteKey.group(siteName);
         case USER:
            return SiteKey.user(siteName);
         default:
            return null;

      }
   }

   public static SiteKey siteKey(Site site)
   {
      return siteKey(getSiteType(site.getObjectType()), site.getName());
   }
}
