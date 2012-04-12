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
import org.gatein.wci.WebApp;

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

   /** . */
   protected final MainResourceResolver mainResolver;

   /** . */
   protected final ResourceCompressor compressor;

   /** . */
   protected final Map<String, WebApp> contexts;
   
   public AbstractResourceService(ResourceCompressor compressor)
   {
      this.compressor = compressor;
      this.mainResolver = new MainResourceResolver();
      this.contexts = new HashMap<String, WebApp>();
   }

   /**
    * Add a resource resolver to plug external resolvers.
    * 
    * @param resolver a resolver to add
    */
   public void addResourceResolver(ResourceResolver resolver)
   {
      mainResolver.resolvers.addIfAbsent(resolver);
   }

   public void registerContext(WebApp app)
   {
      mainResolver.registerContext(app.getServletContext());
      contexts.put(app.getContextPath(), app);
   }
   
   public void unregisterServletContext(WebApp app)
   {
      mainResolver.removeServletContext(app.getServletContext());
      contexts.remove(app.getContextPath());
   }
}
