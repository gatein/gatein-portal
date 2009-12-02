/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.application.registry.mop;

import org.chromattic.api.event.LifeCycleListener;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class Injector implements LifeCycleListener
{

   /** . */
   private final MOPApplicationRegistryService registry;

   public Injector(MOPApplicationRegistryService registry)
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
