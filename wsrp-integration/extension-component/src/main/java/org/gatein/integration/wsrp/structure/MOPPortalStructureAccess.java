/*
 * JBoss, a division of Red Hat
 * Copyright 2011, Red Hat Middleware, LLC, and individual
 * contributors as indicated by the @authors tag. See the
 * copyright.txt in the distribution for a full listing of
 * individual contributors.
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

package org.gatein.integration.wsrp.structure;

import org.exoplatform.portal.pom.config.POMSession;
import org.exoplatform.portal.pom.config.POMSessionManager;
import org.exoplatform.portal.pom.data.Mapper;
import org.exoplatform.portal.pom.data.PageKey;
import org.gatein.mop.api.workspace.ObjectType;
import org.gatein.mop.api.workspace.Page;
import org.gatein.mop.api.workspace.Site;
import org.gatein.mop.api.workspace.Workspace;
import org.gatein.mop.api.workspace.ui.UIWindow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class MOPPortalStructureAccess implements PortalStructureAccess
{
   private static final String PAGES_CHILD_NAME = "pages";
   private final POMSessionManager pomManager;

   public MOPPortalStructureAccess(POMSessionManager pomManager)
   {
      this.pomManager = pomManager;
   }

   public Collection<Page> getPages()
   {
      POMSession session = pomManager.getSession();
      Workspace workspace = session.getWorkspace();
      Collection<Site> sites = workspace.getSites(ObjectType.PORTAL_SITE);

      List<Page> pages = new ArrayList<Page>(sites.size() * 10);

      for (Site site : sites)
      {
         Page pagesRoot = getPagesFrom(site);
         if (pagesRoot != null)
         {
            Collection<Page> children = pagesRoot.getChildren();
            for (Page child : children)
            {
               pages.add(child);
            }
         }
      }

      return pages;
   }

   public UIWindow getWindowFrom(String uuid)
   {
      POMSession session = pomManager.getSession();
      return session.findObjectById(ObjectType.WINDOW, uuid);
   }

   public void saveChangesTo(UIWindow window)
   {
      POMSession session = pomManager.getSession();

      // mark page for cache invalidation otherwise DataCache will use the previous customization id when trying to set
      // the portlet state in UIPortlet.setState and will not find it resulting in an error
      Page page = window.getPage();
      session.scheduleForEviction(new PageKey("portal", page.getSite().getName(), page.getName()));

      // save
      session.save();
   }

   public Page getPageFrom(org.exoplatform.portal.config.model.Page portalPage)
   {
      POMSession session = pomManager.getSession();
      Site site = session.getWorkspace().getSite(Mapper.parseSiteType(portalPage.getOwnerType()), portalPage.getOwnerId());
      return getPagesFrom(site).getChild(portalPage.getName());
   }

   private Page getPagesFrom(Site site)
   {
      // a site contains a root page with templates and pages
      // more info at http://code.google.com/p/chromattic/wiki/MOPUseCases

      return site.getRootPage().getChild(PAGES_CHILD_NAME);
   }
}
