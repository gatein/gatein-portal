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
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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

   public Collection<ScriptResource> resolve(Collection<ResourceId> ids)
   {
      // First build the closure
      HashSet<ResourceId> closure = new HashSet<ResourceId>();
      for (ResourceId id : ids)
      {
         ScriptResource resource = getResource(id);
         if (resource != null)
         {
            closure.add(id);
            closure.addAll(resource.closure);
         }
      }
      
      // Now we create the set
      List<ScriptResource> resources = new ArrayList<ScriptResource>();
      for (ResourceId id : closure)
      {
         ScriptResource resource = getResource(id);
         resources.add(resource);
      }
      
      // And we sort it
      Collections.sort(resources, new Comparator<ScriptResource>()
      {
         public int compare(ScriptResource o1, ScriptResource o2)
         {
            if (o1.closure.contains(o2.getId()))
            {
               return 1;
            }
            else if (o2.closure.contains(o1.getId()))
            {
               return -1;
            }
            else
            {
               return 0;
            }
         }
      });

      //
      return resources;
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
