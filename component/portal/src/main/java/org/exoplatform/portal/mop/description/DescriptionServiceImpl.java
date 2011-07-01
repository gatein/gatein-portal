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
import org.exoplatform.portal.pom.config.POMSession;
import org.exoplatform.portal.pom.config.POMSessionManager;
import org.gatein.mop.api.workspace.WorkspaceObject;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class DescriptionServiceImpl implements DescriptionService
{

   /** . */
   private final POMSessionManager manager;

   /** . */
   private DataCache cache;

   public DescriptionServiceImpl(POMSessionManager manager)
   {
      this(manager, new SimpleDataCache());
   }

   public DescriptionServiceImpl(POMSessionManager manager, DataCache cache)
   {
      this.manager = manager;
      this.cache = cache;
   }

   public Described.State resolveDescription(String id, Locale locale) throws NullPointerException
   {
      return resolveDescription(id, null, locale);
   }

   public Described.State resolveDescription(String id, Locale locale2, Locale locale1) throws NullPointerException
   {
      if (id == null)
      {
         throw new NullPointerException("No null id accepted");
      }
      if (locale1 == null)
      {
         throw new NullPointerException("No null locale accepted");
      }

      //
      POMSession session = manager.getSession();
      Described.State state = resolveDescription(session, id, locale1);
      if (state == null && locale2 != null)
      {
         state = resolveDescription(session, id, locale2);
      }
      return state;
   }

   private Described.State resolveDescription(POMSession session, String id, Locale locale) throws NullPointerException
   {
      return cache.getState(session, new CacheKey(locale, id));
   }

   public Described.State getDescription(String id, Locale locale)
   {
      if (id == null)
      {
         throw new NullPointerException("No null id accepted");
      }
      if (locale == null)
      {
         throw new NullPointerException("No null locale accepted");
      }
      POMSession session = manager.getSession();
      WorkspaceObject obj = session.findObjectById(id);
      I18NAdapter able = obj.adapt(I18NAdapter.class);
      Described desc = able.getI18NMixin(Described.class, locale, false);
      return desc != null ? desc.getState() : null;
   }

   public void setDescription(String id, Locale locale, Described.State description)
   {
      if (id == null)
      {
         throw new NullPointerException("No null id accepted");
      }
      if (locale == null)
      {
         throw new NullPointerException("No null locale accepted");
      }
      POMSession session = manager.getSession();
      WorkspaceObject obj = session.findObjectById(id);
      I18NAdapter able = obj.adapt(I18NAdapter.class);
      Described desc = able.getI18NMixin(Described.class, locale, true);
      desc.setState(description);
   }

   public Described.State getDescription(String id)
   {
      if (id == null)
      {
         throw new NullPointerException("No null id accepted");
      }
      POMSession session = manager.getSession();
      WorkspaceObject obj = session.findObjectById(id);
      I18NAdapter able = obj.adapt(I18NAdapter.class);
      Described desc = able.getMixin(Described.class, false);
      return desc != null ? desc.getState() : null;
   }

   public void setDescription(String id, Described.State description)
   {
      if (id == null)
      {
         throw new NullPointerException("No null id accepted");
      }
      POMSession session = manager.getSession();
      WorkspaceObject obj = session.findObjectById(id);
      I18NAdapter able = obj.adapt(I18NAdapter.class);
      if (description != null)
      {
         Described desc = able.getMixin(Described.class, true);
         desc.setState(description);
      }
      else
      {
         able.removeMixin(Described.class);
      }
   }

   public Map<Locale, Described.State> getDescriptions(String id)
   {
      if (id == null)
      {
         throw new NullPointerException("No null id accepted");
      }
      POMSession session = manager.getSession();
      WorkspaceObject obj = session.findObjectById(id);
      I18NAdapter able = obj.adapt(I18NAdapter.class);
      Map<Locale, Described> mixins = able.getI18NMixin(Described.class);
      Map<Locale, Described.State> names = null;
      if (mixins != null)
      {
         names = new HashMap<Locale, Described.State>(mixins.size());
         for (Map.Entry<Locale, Described> entry : mixins.entrySet())
         {
            names.put(entry.getKey(), entry.getValue().getState());
         }
      }
      return names;
   }

   public void setDescriptions(String id, Map<Locale, Described.State> descriptions)
   {
      if (id == null)
      {
         throw new NullPointerException("No null id accepted");
      }
      POMSession session = manager.getSession();
      WorkspaceObject obj = session.findObjectById(id);
      I18NAdapter able = obj.adapt(I18NAdapter.class);
      Collection<Locale> locales = able.removeI18NMixin(Described.class);
      for (Locale locale : locales)
      {
         cache.removeState(new CacheKey(locale, id));
      }
      for (Map.Entry<Locale, Described.State> entry : descriptions.entrySet())
      {
         Described described = able.addI18NMixin(Described.class, entry.getKey());
         described.setState(entry.getValue());
      }
   }
}
