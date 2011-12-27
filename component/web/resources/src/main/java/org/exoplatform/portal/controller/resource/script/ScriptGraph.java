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

package org.exoplatform.portal.controller.resource.script;

import org.exoplatform.portal.controller.resource.ResourceId;
import org.exoplatform.portal.controller.resource.ResourceScope;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class ScriptGraph
{

   /** . */
   final EnumMap<ResourceScope, Map<String, ScriptResource>> resources;

   public ScriptGraph()
   {
      EnumMap<ResourceScope, Map<String, ScriptResource>> resources = new EnumMap<ResourceScope, Map<String, ScriptResource>>(ResourceScope.class);
      for (ResourceScope scope : ResourceScope.values())
      {
         resources.put(scope, new HashMap<String, ScriptResource>());
      }
      
      //
      this.resources = resources;
   }

   public Map<ScriptResource, Boolean> resolve(Collection<ResourceId> ids)
   {
      // First build the closure
      Map<ScriptResource, Boolean> closure = new HashMap<ScriptResource, Boolean>();

      //
      for (ResourceId id : ids)
      {
         ScriptResource resource = getResource(id);
         if (resource != null)
         {
            // Add the resource 
            closure.put(resource, false);
            
            // Add the resource closure
            for (ResourceId dependencyId : resource.closure)
            {
               ScriptResource dependency = getResource(dependencyId);

               //
               if (dependency != null)
               {
                  Boolean onLoad = closure.get(dependency);

                  //
                  if (onLoad != null && !onLoad)
                  {
                     // It's already for immediate loading
                  }
                  else
                  {
                     onLoad = resource.dependencies.get(dependencyId);

                     // We don't know if we will need this resource immediatly so
                     // we assume it is onload, another dependency will set it to
                     // false later in this loop
                     if (onLoad == null)
                     {
                        onLoad = true;
                     }

                     //
                     closure.put(dependency, onLoad);
                  }
               }
               else
               {
                  // Warn somehow since we cannot resolve the resource
               }
            }
         }
      }
      
      //
      ArrayList<ScriptResource> keys = new ArrayList<ScriptResource>(closure.keySet());
      Collections.sort(keys);
      
      // There is a valid reason to not use a TreeMap directly: it does not work :-)
      // more seriously, we have a natural ordering but with inconsistent equals, please
      // see Comparable#compareTo javadoc
      LinkedHashMap<ScriptResource, Boolean> ret = new LinkedHashMap<ScriptResource, Boolean>();
      for (ScriptResource key : keys)
      {
         ret.put(key, closure.get(key));
      }
      return ret;
   }

   public ScriptResource getResource(ResourceId id)
   {
      return getResource(id.getScope(), id.getName());
   }

   public ScriptResource getResource(ResourceScope scope, String name)
   {
      return resources.get(scope).get(name);
   }

   public Iterable<ScriptResource> getResources(ResourceScope scope)
   {
      return resources.get(scope).values();
   }

   public ScriptResource addResource(ResourceId id)
   {
      Map<String, ScriptResource> map = resources.get(id.getScope());
      String name = id.getName();
      ScriptResource resource = map.get(name);
      if (resource == null)
      {
         map.put(name, resource = new ScriptResource(this, id));
      }
      return resource;
   }
   
   public ScriptResource removeResource(ResourceId id)
   {
      ScriptResource removed = resources.get(id.getScope()).remove(id.getName());
      if (removed != null)
      {
         removed.graph = null;
      }
      return removed;
   }
}
