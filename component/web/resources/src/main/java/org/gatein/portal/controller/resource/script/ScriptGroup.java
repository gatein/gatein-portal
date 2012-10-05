/*
 * Copyright (C) 2012 eXo Platform SAS.
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

package org.gatein.portal.controller.resource.script;

import java.util.HashSet;
import java.util.Set;

import org.gatein.portal.controller.resource.ResourceId;

/**
 * @author <a href="mailto:phuong.vu@exoplatform.com">Vu Viet Phuong</a>
 */
public class ScriptGroup extends BaseScriptResource<ScriptGroup>
{
   final Set<ResourceId> scripts;
   final String contextPath;

   ScriptGroup(ScriptGraph graph, ResourceId id, String contextPath)
   {
      super(graph, id);

      //
      this.scripts = new HashSet<ResourceId>();
      this.contextPath = contextPath;
   }
   
   void addDependency(ResourceId id)
   {
      scripts.add(id);
   }

   @Override
   public Set<ResourceId> getDependencies()
   {
      return scripts;
   }

   @Override
   public int hashCode()
   {
      return getId().hashCode();
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      ScriptGroup other = (ScriptGroup)obj;
      return getId().equals(other.getId());
   }   

}
