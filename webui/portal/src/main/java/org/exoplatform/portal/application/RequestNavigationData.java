/*
 * Copyright (C) 2011 eXo Platform SAS.
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
package org.exoplatform.portal.application;

import org.exoplatform.web.controller.QualifiedName;

/**
* @author <a href="hoang281283@gmail.com">Minh Hoang TO</a>
* @date 11/4/11
*/
public class RequestNavigationData
{
   public final static QualifiedName REQUEST_PATH = QualifiedName.create("gtn", "path");

   public final static QualifiedName REQUEST_SITE_TYPE = QualifiedName.create("gtn", "sitetype");

   public final static QualifiedName REQUEST_SITE_NAME = QualifiedName.create("gtn", "sitename");

   protected final String siteType;

   protected final String siteName;

   protected final String path;

   public RequestNavigationData(String siteType, String siteName, String path)
   {
      this.siteType = siteType != null? siteType : "";
      this.siteName = siteName != null? siteName : "";
      this.path = path != null? path : "";
   }

   @Override
   public boolean equals(Object obj)
   {
      if(obj == null || !(obj instanceof RequestNavigationData))
      {
         return false;
      }
      else
      {
         RequestNavigationData data = (RequestNavigationData)obj;
         return siteType.equals(data.siteType) && siteName.equals(data.siteName) && path.equals(data.path);
      }
   }
}
