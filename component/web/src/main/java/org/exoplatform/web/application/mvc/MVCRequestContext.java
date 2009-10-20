/**
 * Copyright (C) 2009 eXo Platform SAS.
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

package org.exoplatform.web.application.mvc;

import org.exoplatform.services.resources.Orientation;
import org.exoplatform.web.application.Application;
import org.exoplatform.web.application.RequestContext;
import org.exoplatform.web.application.URLBuilder;

/**
 * Created by The eXo Platform SAS
 * Apr 23, 2007
 */
public class MVCRequestContext extends RequestContext
{
   public MVCRequestContext(Application app, RequestContext parent)
   {
      super(app);
      setParentAppRequestContext(parent);
   }

   public Orientation getOrientation()
   {
      return null;
   }

   public String getRequestParameter(String arg0)
   {
      return null;
   }

   public String[] getRequestParameterValues(String arg0)
   {
      return null;
   }

   public URLBuilder getURLBuilder()
   {
      return null;
   }

   public boolean useAjax()
   {
      return false;
   }
}