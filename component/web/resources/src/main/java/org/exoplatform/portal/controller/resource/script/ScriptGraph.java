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
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

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

   /**
    * <p></p>Resolve a collection of pair of resource id and fetch mode, each entry of the map will be processed
    * in the order specified by the iteration of the {@link java.util.Map#entrySet()}. For a given pair
    * the fetch mode may be null or not. When the fetch mode is null, the default fetch mode of the resource
    * is used. When the fetch mode is not null, this fetch mode may override the resource fetch mode if it implies
    * this particular fetch mode. This algorithm tolerates the absence of resources, for instance if a resource
    * is specified (among the pairs or by a transitive dependency) and does not exist, the resource will be skipped.</p>
    *
    * @param pairs the pairs to resolve
    * @return the resources sorted
    */
   public Map<ScriptResource, FetchMode> resolve(Map<ResourceId, FetchMode> pairs)
   {
      FetchMap<ResourceId> determined = new FetchMap<ResourceId>();

      //
      for (Map.Entry<ResourceId, FetchMode> pair : pairs.entrySet())
      {
         ResourceId id = pair.getKey();
         FetchMode fetchMode = pair.getValue();
         ScriptResource resource = getResource(id);
         
         //
         if (resource != null)
         {
            // Which fetch mode should we use ?
            if (fetchMode == null || resource.fetchMode.implies(fetchMode))
            {
               fetchMode = resource.fetchMode;
            }
            
            //
            if (determined.add(id, fetchMode))
            {
               for (ResourceId dependencyId : resource.closure)
               {
                  ScriptResource dependency = getResource(dependencyId);
                  if (dependency != null)
                  {
                     determined.add(dependencyId, fetchMode);
                  }
               }
            }
         }
      }
      
      // Sort results
      ArrayList<ScriptResource> resources = new ArrayList<ScriptResource>(determined.size());
      for (ResourceId id : determined.keySet())
      {
         resources.add(getResource(id));
      }
      Collections.sort(resources);
      
      //
      LinkedHashMap<ScriptResource, FetchMode> result = new LinkedHashMap<ScriptResource, FetchMode>(determined.size());
      for (ScriptResource resource : resources)
      {
         result.put(resource, determined.get(resource.getId()));
      }
      

      //
      return result;
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
      return addResource(id, FetchMode.IMMEDIATE);
   }

   public ScriptResource addResource(ResourceId id, FetchMode fetchMode)
   {
      Map<String, ScriptResource> map = resources.get(id.getScope());
      String name = id.getName();
      ScriptResource resource = map.get(name);
      if (resource == null)
      {
         map.put(name, resource = new ScriptResource(this, id, fetchMode));
      }
      else if (fetchMode == FetchMode.IMMEDIATE && resource.fetchMode != FetchMode.IMMEDIATE)
      {
         // We should somehow have a method on FetchMode called "implies" that would provide
         // relationship between fetch modes, but for now it's ok, but later it may be useful
         // if we have additional fetch mode like "on-click".
         resource.fetchMode = FetchMode.IMMEDIATE;
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
