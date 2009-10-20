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

import static org.exoplatform.portal.pom.config.Utils.split;

import org.exoplatform.portal.config.model.Mapper;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.pom.config.AbstractPOMTask;
import org.exoplatform.portal.pom.config.POMSession;
import org.gatein.mop.api.workspace.Navigation;
import org.gatein.mop.api.workspace.ObjectType;
import org.gatein.mop.api.workspace.Site;
import org.gatein.mop.api.workspace.Workspace;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class PageNavigationTask extends AbstractPOMTask
{

   /** . */
   protected final String owner;

   /** . */
   protected final String ownerType;

   /** . */
   protected final String ownerId;

   /** . */
   protected final ObjectType<? extends Site> siteType;

   protected PageNavigationTask(String owner)
   {
      String[] chunks = split("::", owner);
      if (chunks.length != 2)
      {
         throw new IllegalArgumentException("Wrong owner format should be ownerType::ownerId was " + owner);
      }

      //
      this.ownerType = chunks[0];
      this.ownerId = chunks[1];
      this.siteType = Mapper.parseSiteType(ownerType);
      this.owner = owner;
   }

   public static class Load extends PageNavigationTask
   {

      /** . */
      private PageNavigation pageNav;

      public Load(String owner)
      {
         super(owner);
      }

      public PageNavigation getPageNavigation()
      {
         return pageNav;
      }

      public void run(POMSession session) throws Exception
      {
         Workspace workspace = session.getWorkspace();
         Site site = workspace.getSite(siteType, ownerId);
         if (site != null)
         {
            Navigation nav = site.getRootNavigation();
            Navigation defaultNav = nav.getChild("default");
            if (defaultNav != null)
            {
               pageNav = new Mapper(session).load(defaultNav);
            }
         }
         else
         {
            System.out.println("Cannot load page navigation " + owner + " as the corresponding portal " + ownerId
               + " with type " + siteType + " does not exist");
         }
      }
   }

   public static class Save extends PageNavigationTask
   {

      /** . */
      private final PageNavigation pageNav;

      /** . */
      private final boolean overwrite;

      public Save(PageNavigation pageNav, boolean overwrite)
      {
         super(pageNav.getOwner());

         //
         this.pageNav = pageNav;
         this.overwrite = overwrite;
      }

      public void run(POMSession session) throws Exception
      {
         Workspace workspace = session.getWorkspace();
         Site site = workspace.getSite(siteType, ownerId);
         if (site == null)
         {
            throw new IllegalArgumentException("Cannot insert page navigation " + owner
               + " as the corresponding portal " + ownerId + " with type " + siteType + " does not exist");
         }

         // Delete node descendants first
         Navigation nav = site.getRootNavigation();

         //
         Navigation defaultNav = nav.getChild("default");
         if (defaultNav == null)
         {
            defaultNav = nav.addChild("default");
         }

         //
         new Mapper(session).save(pageNav, defaultNav);
      }

   }

   public static class Remove extends PageNavigationTask
   {

      public Remove(PageNavigation pageNav)
      {
         super(pageNav.getOwner());
      }

      public void run(POMSession session) throws Exception
      {
         Workspace workspace = session.getWorkspace();
         Site site = workspace.getSite(siteType, ownerId);
         if (site == null)
         {
            throw new IllegalArgumentException("Cannot insert page navigation " + owner
               + " as the corresponding portal " + ownerId + " with type " + siteType + " does not exist");
         }

         // Delete descendants
         Navigation nav = site.getRootNavigation();

         //
         Navigation defaultNav = nav.getChild("default");
         if (defaultNav != null)
         {
            defaultNav.destroy();
         }
      }
   }
}