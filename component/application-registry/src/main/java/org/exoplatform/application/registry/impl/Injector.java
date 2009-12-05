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
package org.exoplatform.application.registry.impl;

import org.chromattic.api.event.LifeCycleListener;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class Injector implements LifeCycleListener
{

   /** . */
   private final ApplicationRegistryServiceImpl registry;

   public Injector(ApplicationRegistryServiceImpl registry)
   {
      this.registry = registry;
   }

   public void created(Object o)
   {
   }

   public void loaded(String id, String path, String name, Object o)
   {
      if (o instanceof CategoryDefinition)
      {
         ((CategoryDefinition)o).registry = registry;
      }
   }

   public void added(String id, String path, String name, Object o)
   {
      if (o instanceof CategoryDefinition)
      {
         ((CategoryDefinition)o).registry = registry;
      }
   }

   public void removed(String id, String path, String name, Object o)
   {
      if (o instanceof CategoryDefinition)
      {
         ((CategoryDefinition)o).registry = null;
      }
   }
}
