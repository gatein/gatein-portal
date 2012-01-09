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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
   
   public Collection<ScriptResource> resolve(Collection<ResourceId> ids, FetchMode fetchMode)
   {
      Set<ScriptResource> determined = new HashSet<ScriptResource>();
      
      //
      for (ResourceId id : ids)
      {
         ScriptResource resource = getResource(id);
         if (resource != null && resource.fetchMode == fetchMode)
         {
            determined.add(resource);
            for (ResourceId dependencyId : resource.closure)
            {
               ScriptResource dependency = getResource(dependencyId);
               if (dependency != null)
               {
                  determined.add(dependency);
               }
            }
         }
      }
      
      //
      List<ScriptResource> sorted = new ArrayList<ScriptResource>(determined);
      Collections.sort(sorted);
      
      //
      return sorted;
   }

   public Map<ScriptResource, FetchMode> resolve(Collection<ResourceId> ids)
   {
      LinkedHashMap<ScriptResource, FetchMode> map = new LinkedHashMap<ScriptResource, FetchMode>();
      
      //
      for (ScriptResource onLoad : resolve(ids, FetchMode.ON_LOAD))
      {
         map.put(onLoad, FetchMode.ON_LOAD);
      }

      //
      for (ScriptResource onLoad : resolve(ids, FetchMode.IMMEDIATE))
      {
         map.put(onLoad, FetchMode.IMMEDIATE);
      }

      //
      return map;
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
