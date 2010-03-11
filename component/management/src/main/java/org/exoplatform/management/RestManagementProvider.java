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
import org.exoplatform.management.rest.annotations.RESTEndpoint;
import org.exoplatform.management.spi.ManagedResource;
import org.exoplatform.management.spi.ManagementProvider;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.services.rest.resource.ResourceContainer;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
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

   /** . */
   private final UserACL acl;

   public RestManagementProvider(ExoContainerContext context, UserACL acl)
   {
      this.context = context;
      this.acl = acl;
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public Object list()
   {
      // Apply security here
      if (!acl.hasPermission("*:/platform/administrators"))
      {
         return Response.status(Response.Status.FORBIDDEN);
      }

      //
      List<String> list = new ArrayList<String>();
      for (RestResource mr : resourceMap.values())
      {
         list.add(mr.getName());
      }
      return ValueWrapper.wrap(list);
   }

   @Path("{resource}")
   public Object dispatch(@PathParam("resource") String resourceName)
   {
      // Apply security here
      if (!acl.hasPermission("*:/platform/administrators"))
      {
         return Response.status(Response.Status.FORBIDDEN);
      }

      //
      return resourceMap.get(new ResourceKey(resourceName));
   }

   // ManagementProvider implementation ******************************************************************************** 

   public Object manage(ManagedResource managedResource)
   {
      Object resource = managedResource.getResource();

      //
      RESTEndpoint annotation = resource.getClass().getAnnotation(RESTEndpoint.class);

      //
      if (annotation != null)
      {
         String name = annotation.path();
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
