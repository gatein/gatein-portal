/**
 * Copyright (C) 2010 eXo Platform SAS.
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
package org.exoplatform.web.url.navigation;

import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.mop.user.UserNode;

/**
 * A class that contains combination of a portal name and a page node
 * to determine the target URL
 * 
 * @author <a href="trongtt@gmail.com">Trong Tran</a>
 * @version $Revision$
 */
public class NavigationResource
{

   /** . */
   private final SiteType siteType;
   
   /** . */
   private final String siteName;
   
   /** . */
   private final String nodeURI;

   public NavigationResource(UserNode node)
   {
      this(node.getNavigation().getKey().getType(), node.getNavigation().getKey().getName(), node.getURI());
   }

   public NavigationResource(SiteKey siteKey, String nodeURI)
   {
      this(siteKey.getType(), siteKey.getName(), nodeURI);
   }

   public NavigationResource(SiteType siteType, String portalName, String nodeURI)
   {
      this.siteType = siteType;
      this.siteName = portalName;
      this.nodeURI = nodeURI;
   }

   public SiteType getSiteType()
   {
      return siteType;
   }
   
   public String getSiteName()
   {
      return siteName;
   }

   public String getNodeURI()
   {
      return nodeURI;
   }
}
