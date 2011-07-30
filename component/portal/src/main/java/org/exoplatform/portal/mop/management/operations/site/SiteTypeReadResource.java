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

import org.exoplatform.portal.mop.management.operations.AbstractMopOperationHandler;
import org.gatein.management.api.exceptions.OperationException;
import org.gatein.management.api.exceptions.ResourceNotFoundException;
import org.gatein.management.api.operation.OperationContext;
import org.gatein.management.api.operation.ResultHandler;
import org.gatein.management.api.operation.model.ReadResourceModel;
import org.gatein.mop.api.workspace.Navigation;
import org.gatein.mop.api.workspace.ObjectType;
import org.gatein.mop.api.workspace.Page;
import org.gatein.mop.api.workspace.Site;
import org.gatein.mop.api.workspace.Workspace;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class SiteTypeReadResource extends AbstractMopOperationHandler
{
   @Override
   protected void execute(OperationContext operationContext, ResultHandler resultHandler, Workspace workspace, ObjectType<Site> siteType) throws ResourceNotFoundException, OperationException
   {
      Collection<Site> sites = workspace.getSites(siteType);
      Set<String> children = new LinkedHashSet<String>(sites.size());
      for (Site site : sites)
      {
         boolean pageOrNav = false;
         Page pages = site.getRootPage().getChild("pages");
         if (pages != null && !pages.getChildren().isEmpty())
         {
            pageOrNav = true;
         }
         Navigation defaultNav = site.getRootNavigation().getChild("default");
         if (defaultNav != null && !defaultNav.getChildren().isEmpty())
         {
            pageOrNav = true;
         }

         //TODO: Until invalid site entries without a leading slash is corrected, this is needed to ignore them.
         if (siteType == ObjectType.GROUP_SITE)
         {
            String name = site.getName();
            if (name.charAt(0) == '/' && pageOrNav)
            {
               children.add(site.getName());
            }
         }
         else if (pageOrNav)
         {
            children.add(site.getName());
         }
      }
      resultHandler.completed(new ReadResourceModel("Available sites for site type '" + getSiteType(siteType).getName() + "'", children));
   }
}
