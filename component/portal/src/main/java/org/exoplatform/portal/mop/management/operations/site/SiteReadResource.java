/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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

package org.exoplatform.portal.mop.management.operations.site;

import org.gatein.management.api.exceptions.OperationException;
import org.gatein.management.api.exceptions.ResourceNotFoundException;
import org.gatein.management.api.operation.OperationContext;
import org.gatein.management.api.operation.ResultHandler;
import org.gatein.management.api.operation.model.ReadResourceModel;
import org.gatein.mop.api.workspace.Navigation;
import org.gatein.mop.api.workspace.ObjectType;
import org.gatein.mop.api.workspace.Page;
import org.gatein.mop.api.workspace.Site;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class SiteReadResource extends AbstractSiteOperationHandler
{
   @Override
   protected void execute(OperationContext operationContext, ResultHandler resultHandler, Site site) throws ResourceNotFoundException, OperationException
   {
      boolean pageOrNav = false;
      Set<String> children = new LinkedHashSet<String>(3);

      Page pages = site.getRootPage().getChild("pages");
      if (pages != null && !pages.getChildren().isEmpty())
      {
         children.add("pages");
         pageOrNav = true;
      }

      Navigation defaultNav = site.getRootNavigation().getChild("default");
      if (defaultNav != null && !defaultNav.getChildren().isEmpty())
      {
         children.add("navigation");
         pageOrNav = true;
      }

      if (pageOrNav)
      {
         if (site.getObjectType() == ObjectType.PORTAL_SITE)
         {
            children.add("portal");
         }
         else if (site.getObjectType() == ObjectType.GROUP_SITE)
         {
            children.add("group");
         }
         else if (site.getObjectType() == ObjectType.USER_SITE)
         {
            children.add("user");
         }
         else
         {
            throw new OperationException(operationContext.getOperationName(), "Unknown site type " + site.getObjectType());
         }
      }
      else
      {
         if (site.getObjectType() == ObjectType.GROUP_SITE)
         {
            Collection<? extends Site> groupsites = site.getWorkspace().getSites(site.getObjectType());
            for (Site groupsite : groupsites)
            {
               String siteName = site.getName();
               String groupName = groupsite.getName();
               if (siteName.equals(groupName)) continue;

               int index = groupName.indexOf(siteName);
               if (index == 0)
               {
                  children.add(groupName.substring(siteName.length(), groupName.length()));
               }
            }
         }
      }

      resultHandler.completed(new ReadResourceModel("Available artifacts for site " + getSiteKey(site), children));
   }
}
