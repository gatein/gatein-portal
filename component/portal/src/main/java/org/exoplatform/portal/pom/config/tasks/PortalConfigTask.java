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
import org.exoplatform.portal.config.model.Mapper;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.pom.config.AbstractPOMTask;
import org.exoplatform.portal.pom.config.POMSession;
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
   protected final String name;

   /** . */
   protected final ObjectType<? extends Site> type;

   protected PortalConfigTask(String type, String name)
   {
      this.type = Mapper.parseSiteType(type);
      this.name = name;
   }

   public static class Remove extends PortalConfigTask
   {

      public Remove(String type, String name)
      {
         super(type, name);
      }

      public void run(POMSession session)
      {
         Workspace workspace = session.getWorkspace();
         Site site = workspace.getSite(type, name);
         if (site == null)
         {
            throw new NullPointerException("Could not remove non existing portal " + name);
         }
         else
         {
            site.destroy();
         }
      }
   }

   public static class Save extends PortalConfigTask
   {

      /** . */
      private final PortalConfig config;

      /** . */
      private boolean overwrite;

      public Save(PortalConfig config, boolean overwrite)
      {
         super(config.getType(), config.getName());

         //
         this.config = config;
         this.overwrite = overwrite;
      }

      public void run(POMSession session) throws Exception
      {
         Workspace workspace = session.getWorkspace();
         Site site = workspace.getSite(type, name);
         if (site != null)
         {
            if (!overwrite)
            {
               throw new IllegalArgumentException("Cannot create portal " + config.getName() + " that already exist");
            }
         }
         else
         {
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
   }

   public static class Load extends PortalConfigTask
   {

      /** . */
      private PortalConfig config;

      public Load(String type, String name)
      {
         super(type, name);
      }

      public PortalConfig getConfig()
      {
         return config;
      }

      public void run(POMSession session)
      {
         Workspace workspace = session.getWorkspace();
         Site site = workspace.getSite(type, name);
         if (site != null)
         {
            this.config = new Mapper(session).load(site);
         }
      }
   }
}
