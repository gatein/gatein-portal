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

import org.exoplatform.portal.controller.resource.Resource;
import org.exoplatform.portal.controller.resource.ResourceId;
import org.exoplatform.portal.controller.resource.ResourceRequestHandler;
import org.exoplatform.web.WebAppController;
import org.exoplatform.web.controller.QualifiedName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class implements the {@link Comparable} interface, however the natural ordering provided here
 * is not consistent with equals, therefore this class should not be used as a key in a {@link java.util.TreeMap}
 * for instance.
 * 
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class ScriptResource extends Resource<ScriptResource> implements Comparable<ScriptResource>
{

   /** . */
   ScriptGraph graph;
   
   /** . */
   private final List<Module> modules;

   /** . */
   private final Map<QualifiedName, String> parameters;

   /** . */
   final HashMap<ResourceId, Boolean> dependencies;

   /** . */
   final HashSet<ResourceId> closure;

   ScriptResource(ScriptGraph graph, ResourceId id)
   {
      super(id);

      //
      Map<QualifiedName, String> parameters = new HashMap<QualifiedName, String>();
      parameters.put(WebAppController.HANDLER_PARAM, "script");
      parameters.put(ResourceRequestHandler.RESOURCE, id.getName());
      parameters.put(ResourceRequestHandler.SCOPE, id.getScope().name());

      //
      this.parameters = parameters;
      this.graph = graph;
      this.modules = new ArrayList<Module>();
      this.closure = new HashSet<ResourceId>();
      this.dependencies = new HashMap<ResourceId, Boolean>();
   }

   public boolean isEmpty()
   {
      return modules.isEmpty();
   }

   public Map<QualifiedName, String> getParameters()
   {
      return parameters;
   }

   public void addDependency(ResourceId dependencyId)
   {
      addDependency(dependencyId, false);
   }

   public void addDependency(ResourceId dependencyId, boolean onLoad)
   {
      ScriptResource dependency = graph.getResource(dependencyId);

      // Detect cycle
      if (dependency != null && dependency.closure.contains(id))
      {
         throw new IllegalStateException("Going to create a cycle");
      }

      // Update any entry that points to the source
      for (Map<String, ScriptResource> resources : graph.resources.values())
      {
         for (ScriptResource resource : resources.values())
         {
            if (resource.closure.contains(id))
            {
               resource.closure.add(dependencyId);
            }
         }
      }

      // That is important to make closure independent from building order of graph nodes.
      if(dependency != null)
      {
         closure.addAll(dependency.getClosure());
      }
      
      //Update the source's closure
      closure.add(dependencyId);
      
      //
      dependencies.put(dependencyId, onLoad);
   }

   public Set<ResourceId> getClosure()
   {
      return closure;
   }

   public Module addLocalModule(String contextPath, String name, String path, int priority)
   {
      Module module = new Module.Local(this, contextPath, name, path, priority);
      modules.add(module);
      return module;
   }

   public Module addRemoteModule(String contextPath, String name, String path, int priority)
   {
      Module module = new Module.Remote(this, contextPath, name, path, priority);
      modules.add(module);
      return module;
   }
   
   public Boolean isOnLoad(ResourceId dependencyId)
   {
      return dependencies.get(dependencyId);
   }

   public List<Module> removeModuleByName(String name)
   {
      ArrayList<Module> removed = new ArrayList<Module>();
      for (Iterator<Module> i = modules.iterator();i.hasNext();)
      {
         Module module = i.next();
         if (module.getName().equals(name))
         {
            removed.add(module);
            i.remove();
         }
      }
      return removed;
   }
   
   public Module getModule(String name)
   {
      for (Module module : modules)
      {
         if (module.getName().equals(name))
         {
            return module;
         }
      }
      return null;
   }

   public List<Module> removeModuleByContextPath(String contextPath)
   {
      ArrayList<Module> removed = new ArrayList<Module>();
      for (Iterator<Module> i = modules.iterator();i.hasNext();)
      {
         Module module = i.next();
         if (module.getContextPath().equals(contextPath))
         {
            removed.add(module);
            i.remove();
         }
      }
      return removed;
   }

   public List<Module> getModules()
   {
      return modules;
   }
   
   public List<String> getModulesNames()
   {
      ArrayList<String> names = new ArrayList<String>();
      for (Module script : modules)
      {
         names.add(script.getName());
      }
      return names;
   }

   public int compareTo(ScriptResource o)
   {
      if (closure.contains(o.id))
      {
         return 1;
      }
      else if (o.closure.contains(id))
      {
         return -1;
      }
      else
      {
         return 0;
      }
   }

   @Override
   public String toString()
   {
      return "ScriptResource[id=" + id + "]";
   }
}
