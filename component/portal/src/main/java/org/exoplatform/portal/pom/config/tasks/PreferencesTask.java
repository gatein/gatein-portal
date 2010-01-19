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

import org.exoplatform.portal.config.NoSuchDataException;
import org.exoplatform.portal.pom.config.POMSession;
import org.exoplatform.portal.pom.config.POMTask;
import org.exoplatform.portal.pom.config.cache.CacheableDataTask;
import org.exoplatform.portal.pom.config.cache.DataAccessMode;
import org.gatein.mop.api.content.Customization;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class PreferencesTask<S>
{

   /** . */
   //   private static final Object NULL_PREFS = new Object();
   public static class GetContentId<S> extends PreferencesTask<S> implements POMTask<String>
   {

      /** . */
      private final String storageId;

      public GetContentId(String storageId)
      {
         this.storageId = storageId;
      }

      public String run(POMSession session) throws Exception
      {
         Customization<S> customization = (Customization<S>)session.findCustomizationById(storageId);
         if (customization == null) {
            throw new NoSuchDataException("Can not find " + storageId);
         }
         return customization.getContentId();
      }
   }

   public static class Load<S> extends PreferencesTask<S> implements CacheableDataTask<String, S>
   {

      /** . */
      private final String storageId;

      /** . */
      private final Class<S> valueType;

      public Load(String storageId, Class<S> valueType)
      {
         this.storageId = storageId;
         this.valueType = valueType;
      }

      public DataAccessMode getAccessMode()
      {
         return DataAccessMode.READ;
      }

      public Class<S> getValueType()
      {
         return valueType;
      }

      public String getKey()
      {
         return storageId;
      }

      public S run(POMSession session) throws Exception
      {
         Customization<S> customization = (Customization<S>)session.findCustomizationById(storageId);
         if (customization == null)
            return null;
         return customization.getVirtualState();
      }

      @Override
      public String toString()
      {
         return "PreferencesTask.Load[id=" + storageId + "]";
      }
   }

   public static class Save<S> extends PreferencesTask<S> implements CacheableDataTask<String, Void>
   {

      /** . */
      private final String storageId;

      /** . */
      private final S prefs;

      public Save(String storageId, S prefs)
      {
         this.storageId = storageId;
         this.prefs = prefs;
      }

      public DataAccessMode getAccessMode()
      {
         return DataAccessMode.WRITE;
      }

      public Class<Void> getValueType()
      {
         return Void.class;
      }

      public String getKey()
      {
         return storageId;
      }

      public Void run(POMSession session) throws Exception
      {

         Customization<S> customization = (Customization<S>)session.findCustomizationById(storageId);
         if (customization == null) {
            throw new NoSuchDataException("Can not find " + storageId);
         }
         if (prefs != null)
         {
            customization.setState(prefs);
         }
         else
         {
            customization.setState(null);
         }

         //
         return null;
      }

      @Override
      public String toString()
      {
         return "PreferencesTask.Save[id=" + storageId + "]";
      }
   }
}
