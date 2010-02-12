/*
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

package org.exoplatform.management;

import org.exoplatform.management.data.RestResource;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.management.spi.ManagedResource;
import org.exoplatform.management.spi.ManagementProvider;
import org.exoplatform.services.rest.resource.ResourceContainer;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
@Path("management")
public class RestManagementProvider implements ResourceContainer, ManagementProvider
{

   /** . */
   private final ExoContainerContext context;

   /** . */
   private final Map<ResourceKey, RestResource> resourceMap = new HashMap<ResourceKey, RestResource>();

   public RestManagementProvider(ExoContainerContext context)
   {
      this.context = context;
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public ValueWrapper list()
   {
      List<String> list = new ArrayList<String>();
      for (RestResource mr : resourceMap.values())
      {
         list.add(mr.getName());
      }
      return ValueWrapper.wrap(list);
   }

   @Path("{resource}")
   public RestResource dispatch(@PathParam("resource") String resourceName)
   {
      return resourceMap.get(new ResourceKey(resourceName));
   }

   // ManagementProvider implementation ******************************************************************************** 

   public Object manage(ManagedResource managedResource)
   {
      Object resource = managedResource.getResource();

      //
      Rest annotation = resource.getClass().getAnnotation(Rest.class);

      //
      if (annotation != null)
      {
         String name = annotation.value();
         ResourceKey key = new ResourceKey(name);
         resourceMap.put(key, new RestResource(name, managedResource));
         return key;
      }

      //
      return null;
   }

   public void unmanage(Object key)
   {
      resourceMap.remove(key);
   }
}
