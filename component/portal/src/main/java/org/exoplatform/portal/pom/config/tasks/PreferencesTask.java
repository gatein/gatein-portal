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

package org.exoplatform.portal.pom.config.tasks;

import org.exoplatform.portal.config.model.PersistentApplicationState;
import org.exoplatform.portal.pom.config.AbstractPOMTask;
import org.exoplatform.portal.pom.config.POMSession;
import org.exoplatform.portal.pom.config.cache.CacheableDataTask;
import org.exoplatform.portal.pom.config.cache.DataAccessMode;
import org.gatein.mop.api.content.Customization;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class PreferencesTask<S> extends AbstractPOMTask
{

   /** . */
   private static final Object NULL_PREFS = new Object();

   public static class Load<S> extends PreferencesTask<S> implements CacheableDataTask<PersistentApplicationState<S>, Object>
   {

      /** . */
      private final PersistentApplicationState<S> state;

      /** . */
      private S prefs;

      public Load(PersistentApplicationState<S> state)
      {
         this.state = state;
      }

      public DataAccessMode getAccessMode()
      {
         return DataAccessMode.READ;
      }

      public void setValue(Object value)
      {
         if (value != NULL_PREFS)
         {
            prefs = (S)value;
         }
      }

      public Class<Object> getValueType()
      {
         return Object.class;
      }

      public Object getValue()
      {
         return prefs == null ? NULL_PREFS : prefs;
      }

      public PersistentApplicationState<S> getKey()
      {
         return state;
      }

      public void run(POMSession session) throws Exception
      {
         String id = state.getStorageId();
         Customization<S> customization = (Customization<S>)session.findCustomizationById(id);
         prefs = customization.getVirtualState();
      }

      public S getState()
      {
         return prefs;
      }

      @Override
      public String toString()
      {
         return "PreferencesTask.Load[state=" + state.getStorageId() + "]";
      }
   }

   public static class Save<S> extends PreferencesTask<S> implements CacheableDataTask<PersistentApplicationState<S>, Object>
   {

      /** . */
      private final PersistentApplicationState<S> state;

      /** . */
      private final S prefs;

      public Save(PersistentApplicationState<S> state, S prefs)
      {
         this.state = state;
         this.prefs = prefs;
      }

      public DataAccessMode getAccessMode()
      {
         return DataAccessMode.WRITE;
      }

      public void setValue(Object value)
      {
         throw new UnsupportedOperationException();
      }

      public Class<Object> getValueType()
      {
         return Object.class;
      }

      public Object getValue()
      {
         return prefs == null ? NULL_PREFS : prefs ;
      }

      public PersistentApplicationState<S> getKey()
      {
         return state;
      }

      public void run(POMSession session) throws Exception
      {

         String id = state.getStorageId();

         Customization<S> customization = (Customization<S>)session.findCustomizationById(id);

         if (prefs != null)
         {
            customization.setState(prefs);
         }
         else
         {
            customization.setState(null);
         }
      }

      @Override
      public String toString()
      {
         return "PreferencesTask.Save[state=" + state.getStorageId() + "]";
      }
   }
}
