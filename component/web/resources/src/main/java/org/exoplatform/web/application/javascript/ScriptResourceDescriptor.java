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

package org.exoplatform.web.application.javascript;

import org.exoplatform.portal.controller.resource.ResourceId;
import org.exoplatform.portal.controller.resource.script.FetchMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class ScriptResourceDescriptor
{

   /** . */
   final ResourceId id;

   /** . */
   final List<Locale> supportedLocales;
   
   /** . */
   final List<Javascript> modules;

   /** . */
   final List<DependencyDescriptor> dependencies;

   /** . */
   FetchMode fetchMode;

   public ScriptResourceDescriptor(ResourceId id, FetchMode fetchMode)
   {
      this.id = id;
      this.modules = new ArrayList<Javascript>();
      this.dependencies = new ArrayList<DependencyDescriptor>();
      this.supportedLocales = new ArrayList<Locale>();
      this.fetchMode = fetchMode;
   }

   public ResourceId getId()
   {
      return id;
   }

   public List<Locale> getSupportedLocales()
   {
      return supportedLocales;
   }

   public List<Javascript> getModules()
   {
      return modules;
   }

   public List<DependencyDescriptor> getDependencies()
   {
      return dependencies;
   }
}
