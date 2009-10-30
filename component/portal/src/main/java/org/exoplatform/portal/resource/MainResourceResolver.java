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

package org.exoplatform.portal.resource;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.servlet.ServletContext;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
class MainResourceResolver implements ResourceResolver
{

   final Map<String, SimpleResourceContext> contexts;

   final CopyOnWriteArrayList<ResourceResolver> resolvers;

   final Map<SkinKey, SkinConfig> skins;

   public MainResourceResolver(String portalContainerName, Map<SkinKey, SkinConfig> skins)
   {
      this.skins = skins;
      this.contexts = new HashMap<String, SimpleResourceContext>();
      this.resolvers = new CopyOnWriteArrayList<ResourceResolver>();

      //
      resolvers.add(new CompositeResourceResolver(portalContainerName, skins));
   }

   SimpleResourceContext registerContext(ServletContext servletContext)
   {
      String key = "/" + servletContext.getServletContextName();
      SimpleResourceContext ctx = contexts.get(key);
      if (ctx == null)
      {
         ctx = new SimpleResourceContext(key, servletContext);
         contexts.put(ctx.getContextPath(), ctx);
      }
      return ctx;
   }

   public Resource resolve(String path)
   {
      for (ResourceResolver resolver : resolvers)
      {
         Resource res = resolver.resolve(path);
         if (res != null)
         {
            return res;
         }
      }

      //
      int i1 = path.indexOf("/", 2);
      String targetedContextPath = path.substring(0, i1);
      SimpleResourceContext context = contexts.get(targetedContextPath);
      return context.getResource(path.substring(i1));
   }
}
