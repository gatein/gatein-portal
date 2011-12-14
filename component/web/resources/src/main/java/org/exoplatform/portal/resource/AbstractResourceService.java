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

import org.exoplatform.portal.resource.compressor.ResourceCompressor;
import org.exoplatform.web.application.javascript.JavascriptConfigService;

import javax.servlet.ServletContext;
import java.util.HashMap;
import java.util.Map;

/**
 * An abstract class for resource services in Portal like {@link SkinService}
 * and {@link JavascriptConfigService}
 * 
 * @author <a href="trongtt@gmail.com">Trong Tran</a>
 * @version $Revision$
 */
public abstract class AbstractResourceService
{
   protected final MainResourceResolver mainResolver;

   protected final ResourceCompressor compressor;
   
   protected final Map<String, ServletContext> contexts;
   
   public AbstractResourceService(ResourceCompressor compressor)
   {
      this.compressor = compressor;
      this.mainResolver = new MainResourceResolver();
      this.contexts = new HashMap<String, ServletContext>();
   }

   /**
    * Add a resource resolver to plug external resolvers.
    * 
    * @param resolver
    *           a resolver to add
    */
   public void addResourceResolver(ResourceResolver resolver)
   {
      mainResolver.resolvers.addIfAbsent(resolver);
   }

   /**
    * Registry ServletContext into MainResourceResolver of SkinService
    * @param sContext
    *          ServletContext will be registried
    */
   public void registerContext(ServletContext sContext)
   {
      mainResolver.registerContext(sContext);
      contexts.put(sContext.getContextPath(), sContext);
   }
   
   /**
    * unregister a {@link ServletContext} into {@link MainResourceResolver} of {@link SkinService} 
    * 
    * @param servletContext ServletContext will unregistered
    */
   public void unregisterServletContext(ServletContext servletContext)
   {
      mainResolver.removeServletContext(servletContext);
      contexts.remove(servletContext.getContextPath());
   }
}
