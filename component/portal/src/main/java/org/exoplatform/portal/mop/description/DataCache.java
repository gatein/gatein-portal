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

package org.exoplatform.portal.mop.description;

import org.exoplatform.portal.mop.Described;
import org.exoplatform.portal.mop.i18n.I18NAdapter;
import org.exoplatform.portal.mop.i18n.Resolution;
import org.exoplatform.portal.pom.config.POMSession;
import org.gatein.mop.api.workspace.WorkspaceObject;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
abstract class DataCache
{

   protected abstract void removeState(CacheKey key);

   protected abstract Described.State getState(POMSession session, CacheKey key);

   protected final CacheValue getValue(POMSession session, CacheKey key)
   {
      WorkspaceObject obj = session.findObjectById(key.id);
      I18NAdapter able = obj.adapt(I18NAdapter.class);
      Resolution<Described> res = able.resolveI18NMixin(Described.class, key.locale);
      if (res != null)
      {
         Described.State state = res.getMixin().getState();
         if (key.locale.equals(res.getLocale()))
         {
            CacheValue foo = new CacheValue(state);
            putValue(key, foo);
            return foo;
         }
         else
         {
            CacheValue origin = new CacheValue(state);
            CacheKey originKey = new CacheKey(res.getLocale(), key.id);
            putValue(originKey, origin);
            CacheValue foo = new CacheValue(originKey, origin.serial, state);
            putValue(key, foo);
            return foo;
         }
      }
      return null;
   }

   protected abstract void putValue(CacheKey key, CacheValue value);

}
