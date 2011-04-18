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
package org.exoplatform.application.gadget.impl;

import org.chromattic.api.annotations.Create;
import org.chromattic.api.annotations.FormattedBy;
import org.chromattic.api.annotations.NamingPrefix;
import org.chromattic.api.annotations.PrimaryType;
import org.chromattic.api.annotations.OneToMany;
import org.chromattic.ext.format.BaseEncodingObjectFormatter;

import java.util.Collection;
import java.util.Map;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
@PrimaryType(name = "app:gadgetregistry")
@FormattedBy(BaseEncodingObjectFormatter.class)
@NamingPrefix("app")
public abstract class GadgetRegistry
{

   @OneToMany
   protected abstract Map<String, GadgetDefinition> getDefinitions();

   @Create
   protected abstract GadgetDefinition createGadget();

/*
   @Create
   protected abstract NTFolder createFolder();
*/

   public Collection<GadgetDefinition> getGadgets()
   {
      return getDefinitions().values();
   }

   public GadgetDefinition getGadget(String name)
   {
      return getDefinitions().get(name);
   }

   public GadgetDefinition addGadget(String name)
   {
      if (name == null)
      {
         throw new NullPointerException();
      }
      GadgetDefinition def = createGadget();
      getDefinitions().put(name, def);
      return def;
   }

   public void removeGadget(String name)
   {
      getDefinitions().put(name, null);
   }
}
