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

package org.exoplatform.portal.mop.i18n;

import org.gatein.mop.api.workspace.WorkspaceObject;
import org.gatein.mop.spi.AdapterLifeCycle;

import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;

/**
 * An adapter that provides the i18n support for mop mixins, giving read/write access to default mixins and/or
 * i18n mixins.
 *
 * @author <a href="mailto:khoi.nguyen@exoplatform.com">Nguyen Duc Khoi</a>
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class I18NAdapter
{

   /** . */
   private final WorkspaceObject obj;

   private I18NAdapter(WorkspaceObject obj)
   {
      this.obj = obj;
   }

   public <M> M getMixin(Class<M> mixinType, boolean create) throws NullPointerException
   {
      if (mixinType == null)
      {
         throw new NullPointerException("No null mixin type accepted");
      }
      if (obj.isAdapted(mixinType) || create)
      {
         return obj.adapt(mixinType);
      }
      else
      {
         return null;
      }
   }

   public <M> void removeMixin(Class<M> mixinType) throws NullPointerException
   {
      if (mixinType == null)
      {
         throw new NullPointerException("No null mixin type accepted");
      }
      if (obj.isAdapted(mixinType))
      {
         obj.removeAdapter(mixinType);
      }
   }

   /**
    * Resolve the mixin for the specified locale.
    *
    * @param mixinType the expected mixin type
    * @param locale the locale
    * @param <M> the mixin generic type
    * @return the resolution or null if it cannot be resolved
    * @throws NullPointerException if any argument is null
    */
   public <M> Resolution<M> resolveI18NMixin(Class<M> mixinType, Locale locale) throws NullPointerException
   {
      if (mixinType == null)
      {
         throw new NullPointerException("No null mixin type accepted");
      }
      if (locale == null)
      {
         throw new NullPointerException("No null locale accepted");
      }
      if (locale.getLanguage().length() > 0 && obj.isAdapted(I18Nized.class))
      {
         I18Nized ized = obj.adapt(I18Nized.class);
         return ized.resolveMixin(mixinType, locale);
      }
      else
      {
         return null;
      }
   }

   public <M> M getI18NMixin(Class<M> mixinType, Locale locale, boolean create) throws NullPointerException, IllegalArgumentException
   {
      if (mixinType == null)
      {
         throw new NullPointerException("No null mixin type accepted");
      }
      if (locale == null)
      {
         throw new NullPointerException("No null locale accepted");
      }
      if (obj.isAdapted(I18Nized.class))
      {
         I18Nized ized = obj.adapt(I18Nized.class);
         return ized.getMixin(mixinType, locale, create);
      }
      else if (create)
      {
         I18Nized ized = obj.adapt(I18Nized.class);
         return ized.getMixin(mixinType, locale, true);
      }
      else
      {
         return null;
      }
   }

   public <M> Map<Locale, M> getI18NMixin(Class<M> mixinType) throws NullPointerException
   {
      if (mixinType == null)
      {
         throw new NullPointerException("No null mixin type accepted");
      }
      if (obj.isAdapted(I18Nized.class))
      {
         I18Nized ized = obj.adapt(I18Nized.class);
         return ized.getMixins(mixinType);
      }
      else
      {
         return null;
      }
   }

   public <M> M addI18NMixin(Class<M> mixinType, Locale locale) throws NullPointerException, IllegalArgumentException
   {
      if (mixinType == null)
      {
         throw new NullPointerException("No null mixin type accepted");
      }
      if (locale == null)
      {
         throw new NullPointerException("No null locale accepted");
      }
      I18Nized ized;
      if (obj.isAdapted(I18Nized.class))
      {
         ized = obj.adapt(I18Nized.class);
      }
      else
      {
         ized = obj.adapt(I18Nized.class);
      }
      return ized.getMixin(mixinType, locale, true);
   }

   public <M> Collection<Locale> removeI18NMixin(Class<M> mixinType)
   {
      if (mixinType == null)
      {
         throw new NullPointerException("No null mixin type accepted");
      }
      if (obj.isAdapted(I18Nized.class))
      {
         I18Nized ized = obj.adapt(I18Nized.class);
         return ized.removeMixin(mixinType);
      }
      else
      {
         return Collections.emptyList();
      }
   }

   public static class LifeCycle extends AdapterLifeCycle<WorkspaceObject, I18NAdapter>
   {
      @Override
      public I18NAdapter create(WorkspaceObject adaptee, Class<I18NAdapter> adapterType)
      {
         return new I18NAdapter(adaptee);
      }

      @Override
      public void destroy(I18NAdapter adapter, WorkspaceObject adaptee, Class<I18NAdapter> adapterType)
      {
      }
   }
}
