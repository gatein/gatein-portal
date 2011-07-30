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

package org.exoplatform.portal.mop.management.operations.navigation;

import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.description.DescriptionService;
import org.exoplatform.portal.mop.management.exportimport.NavigationExportTask;
import org.exoplatform.portal.mop.navigation.NavigationService;
import org.gatein.management.api.ContentType;
import org.gatein.management.api.binding.BindingProvider;
import org.gatein.management.api.binding.Marshaller;
import org.gatein.management.api.exceptions.OperationException;
import org.gatein.management.api.exceptions.ResourceNotFoundException;
import org.gatein.management.api.operation.OperationContext;
import org.gatein.management.api.operation.ResultHandler;
import org.gatein.management.api.operation.model.ExportResourceModel;
import org.gatein.mop.api.workspace.Navigation;
import org.gatein.mop.api.workspace.Site;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class NavigationExportResource extends AbstractNavigationOperationHandler
{
   @Override
   protected void execute(OperationContext operationContext, ResultHandler resultHandler, Navigation navigation) throws ResourceNotFoundException, OperationException
   {
      Site site = navigation.getSite();
      String navUri = operationContext.getAddress().resolvePathTemplate("nav-uri");
      SiteKey siteKey = getSiteKey(site);

      DescriptionService descriptionService = operationContext.getRuntimeContext().getRuntimeComponent(DescriptionService.class);
      NavigationService navigationService = operationContext.getRuntimeContext().getRuntimeComponent(NavigationService.class);
      BindingProvider bindingProvider = operationContext.getBindingProvider();
      Marshaller<PageNavigation> marshaller = bindingProvider.getMarshaller(PageNavigation.class, ContentType.XML);

      NavigationExportTask exportTask = new NavigationExportTask(new NavigationKey(siteKey, navUri), navigationService, descriptionService, marshaller);

      resultHandler.completed(new ExportResourceModel(exportTask));
   }
}
