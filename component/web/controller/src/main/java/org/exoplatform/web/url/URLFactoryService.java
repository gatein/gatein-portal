/*
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

package org.exoplatform.web.url;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class URLFactoryService extends URLFactory
{

   /** . */
   private final Map<ResourceType<?,?>, URLFactoryPlugin> plugins;

   public URLFactoryService()
   {
      this.plugins = new HashMap<ResourceType<?,?>, URLFactoryPlugin>();
   }

   @Override
   public <R, U extends PortalURL<R, U>> U newURL(
      ResourceType<R, U> resourceType,
      URLContext context) throws NullPointerException
   {
      if (resourceType == null)
      {
         throw new NullPointerException("No null resource type accepted");
      }

      // Can't really make that checked
      @SuppressWarnings("unchecked")
      URLFactoryPlugin<R, U> plugin = (URLFactoryPlugin<R,U>)plugins.get(resourceType);

      //
      return plugin != null ? plugin.newURL(context) : null;
   }

   public void addPlugin(URLFactoryPlugin plugin)
   {
      plugins.put(plugin.getResourceType(), plugin);
   }
}
