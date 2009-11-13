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

import org.exoplatform.portal.application.PortletPreferences;
import org.exoplatform.portal.pom.config.cache.DataAccessMode;
import org.exoplatform.portal.pom.config.cache.CacheableDataTask;
import org.exoplatform.portal.pom.data.Mapper;
import org.exoplatform.portal.pom.data.PortalData;
import org.exoplatform.portal.pom.config.AbstractPOMTask;
import org.exoplatform.portal.pom.config.POMSession;
import org.exoplatform.portal.pom.data.PortalKey;
import org.gatein.mop.api.workspace.ObjectType;
import org.gatein.mop.api.workspace.Page;
import org.gatein.mop.api.workspace.Site;
import org.gatein.mop.api.workspace.Workspace;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class PortalConfigTask extends AbstractPOMTask
{

   /** . */
   protected final PortalKey key;

   /** . */
   protected final ObjectType<? extends Site> type;

   protected PortalConfigTask(PortalKey key)
   {
      this.key = key;
      this.type = Mapper.parseSiteType(key.getType());
   }

   public static class Remove extends PortalConfigTask implements CacheableDataTask<PortalKey, PortalData>
   {

      public Remove(PortalKey key)
      {
         super(key);
      }

      public DataAccessMode getAccessMode()
      {
         return DataAccessMode.DESTROY;
      }

      public Class<PortalData> getValueType()
      {
         return PortalData.class;
      }

      public PortalKey getKey()
      {
         return key;
      }

      public void setValue(PortalData value)
      {
         throw new UnsupportedOperationException();
      }

      public PortalData getValue()
      {
         return null;
      }

      public void run(POMSession session)
      {
         Workspace workspace = session.getWorkspace();
         Site site = workspace.getSite(type, key.getId());
         if (site == null)
         {
            throw new NullPointerException("Could not remove non existing portal " + key.getId());
         }
         else
         {
            site.destroy();
         }
      }

      @Override
      public String toString()
      {
         return "PortalConfig.Remove[ownerType=" + key.getType() + ",ownerId=" + key.getId() + "]";
      }
   }

   public static class Save extends PortalConfigTask implements CacheableDataTask<PortalKey, PortalData>
   {

      /** . */
      private final PortalData config;

      /** . */
      private boolean overwrite;

      public Save(PortalData config, boolean overwrite)
      {
         super(config.getKey());

         //
         this.config = config;
         this.overwrite = overwrite;
      }

      public DataAccessMode getAccessMode()
      {
         return overwrite ? DataAccessMode.WRITE : DataAccessMode.CREATE;
      }

      public void setValue(PortalData value)
      {
         throw new UnsupportedOperationException();
      }

      public Class<PortalData> getValueType()
      {
         return PortalData.class;
      }

      public PortalData getValue()
      {
         return config;
      }

      public PortalKey getKey()
      {
         return key;
      }

      public void run(POMSession session) throws Exception
      {
         Workspace workspace = session.getWorkspace();
         Site site = workspace.getSite(type, key.getId());
         if (site != null)
         {
            if (!overwrite)
            {
               throw new IllegalArgumentException("Cannot create portal " + config.getName() + " that already exist");
            }
         }
         else
         {
            if (overwrite)
            {
               throw new IllegalArgumentException("Cannot update portal " + config.getName() + " that does not exist");
            }

            //
            site = workspace.addSite(type, config.getName());
            Page root = site.getRootPage();
            root.addChild("pages");
            root.addChild("templates");

            // Add pending preferences
            for (PortletPreferences prefs : session.getPortletPreferences(site))
            {
               new PortletPreferencesTask.Save(prefs).run(session);
            }
         }
         new Mapper(session).save(config, site);
      }

      @Override
      public String toString()
      {
         return "PortalConfig.Save[ownerType=" + key.getType() + ",ownerId=" + key.getId() + "]";
      }
   }

   public static class Load extends PortalConfigTask implements CacheableDataTask<PortalKey, PortalData>
   {

      /** . */
      private PortalData config;

      public Load(PortalKey key)
      {
         super(key);
      }

      public DataAccessMode getAccessMode()
      {
         return DataAccessMode.READ;
      }

      public PortalKey getKey()
      {
         return key;
      }

      public void setValue(PortalData value)
      {
         config = value;
      }

      public Class<PortalData> getValueType()
      {
         return PortalData.class;
      }

      public PortalData getValue()
      {
         return config;
      }

      public PortalData getConfig()
      {
         return config;
      }

      public void run(POMSession session)
      {
         Workspace workspace = session.getWorkspace();
         Site site = workspace.getSite(type, key.getId());
         if (site != null)
         {
            this.config = new Mapper(session).load(site);
         }
      }

      @Override
      public String toString()
      {
         return "PortalConfig.Load[ownerType=" + key.getType() + ",ownerId=" + key.getId() + "]";
      }
   }
}
