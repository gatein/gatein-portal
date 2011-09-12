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

package org.exoplatform.portal.mop.management.operations.page;

import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.management.exportimport.PageExportTask;
import org.gatein.management.api.ContentType;
import org.gatein.management.api.PathAddress;
import org.gatein.management.api.PathTemplateFilter;
import org.gatein.management.api.binding.BindingProvider;
import org.gatein.management.api.exceptions.OperationException;
import org.gatein.management.api.exceptions.ResourceNotFoundException;
import org.gatein.management.api.operation.OperationContext;
import org.gatein.management.api.operation.ResultHandler;
import org.gatein.management.api.operation.model.ExportResourceModel;
import org.gatein.management.api.operation.model.ExportTask;
import org.gatein.mop.api.workspace.Page;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class PageExportResource extends AbstractPageOperationHandler
{
   @Override
   protected void execute(OperationContext operationContext, ResultHandler resultHandler, Page pages) throws ResourceNotFoundException, OperationException
   {
      SiteKey siteKey = getSiteKey(pages.getSite());

      DataStorage dataStorage = operationContext.getRuntimeContext().getRuntimeComponent(DataStorage.class);
      BindingProvider bindingProvider = operationContext.getBindingProvider();

      Collection<Page> pagesList = pages.getChildren();
      List<ExportTask> tasks = new ArrayList<ExportTask>(pagesList.size());

      PageExportTask pageExportTask =
         new PageExportTask(siteKey, dataStorage, bindingProvider.getMarshaller(
            org.exoplatform.portal.config.model.Page.PageSet.class, ContentType.XML));

      String pageName = operationContext.getAddress().resolvePathTemplate("page-name");
      for (Page page : pagesList)
      {
         if (pageName == null)
         {
            PathAddress pageAddress = operationContext.getAddress().append(page.getName());
            // We need to look up the subresource because this sets the path template resolver to be used by the filter.
            operationContext.getManagedResource().getSubResource(pageAddress);

            PathTemplateFilter filter;
            try
            {
               filter = PathTemplateFilter.parse(operationContext.getAttributes().getValues("filter"));
            }
            catch (ParseException e)
            {
               throw new OperationException(operationContext.getOperationName(), "Could not parse filter attributes.", e);
            }

            if (pageAddress.accepts(filter))
            {
               pageExportTask.addPageName(page.getName());
            }
         }
         else if (pageName.equals(page.getName()))
         {
            pageExportTask.addPageName(page.getName());
         }
      }

      if (pageExportTask.getPageNames().isEmpty() && pageName != null)
      {
         throw new ResourceNotFoundException("No page found for " + new PageKey(siteKey, pageName));
      }
      else if (pageExportTask.getPageNames().isEmpty())
      {
         resultHandler.completed(new ExportResourceModel(Collections.<ExportTask>emptyList()));
      }
      else
      {
         tasks.add(pageExportTask);
         resultHandler.completed(new ExportResourceModel(tasks));
      }
   }
}
