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

import org.exoplatform.portal.config.model.NavigationFragment;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.mop.management.exportimport.NavigationExportTask;
import org.gatein.management.api.ContentType;
import org.gatein.management.api.ManagedResource;
import org.gatein.management.api.PathAddress;
import org.gatein.management.api.PathTemplateFilter;
import org.gatein.management.api.binding.BindingProvider;
import org.gatein.management.api.binding.Marshaller;
import org.gatein.management.api.exceptions.OperationException;
import org.gatein.management.api.operation.OperationContext;
import org.gatein.management.api.operation.OperationContextDelegate;
import org.gatein.management.api.operation.OperationHandler;
import org.gatein.management.api.operation.OperationNames;
import org.gatein.management.api.operation.ResultHandler;
import org.gatein.management.api.operation.StepResultHandler;
import org.gatein.management.api.operation.model.ExportResourceModel;
import org.gatein.management.api.operation.model.ExportTask;
import org.gatein.management.api.operation.model.ReadResourceModel;

import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class FilteredNavigationExportResource
{
   protected void execute(OperationContext operationContext, ResultHandler resultHandler, PathTemplateFilter filter)
   {
      BindingProvider bindingProvider = operationContext.getBindingProvider();
      Marshaller<PageNavigation> marshaller = bindingProvider.getMarshaller(PageNavigation.class, ContentType.XML);

      final ManagedResource resource = operationContext.getManagedResource();
      final PathAddress address = operationContext.getAddress();
      final String operationName = operationContext.getOperationName();

      StepResultHandler<PageNavigation> stepResultHandler = new StepResultHandler<PageNavigation>(address)
      {
         @Override
         public void failed(String failureDescription)
         {
            if (address.equals(getCurrentAddress()))
            {
               throw new OperationException(operationName, "Navigation export failed. Reason: " + failureDescription);
            }
            else
            {
               throw new OperationException(operationName, "Navigation export failed. Reason: " + failureDescription + " [Step Address: " + getCurrentAddress() + "]");
            }
         }

         @Override
         protected void doCompleted(PageNavigation result)
         {
            if (getResults().isEmpty())
            {
               super.doCompleted(result);
            }
            else
            {
               PageNavigation navigation = getResults().get(0);
               merge(navigation, result);
            }
         }
      };

      try
      {
         executeHandlers(resource, operationContext, address, OperationNames.READ_CONFIG_AS_XML, stepResultHandler, filter, true);
         List<PageNavigation> results = stepResultHandler.getResults();
         if (results.isEmpty())
         {
            resultHandler.completed(new ExportResourceModel(Collections.<ExportTask>emptyList()));
         }
         else
         {
            NavigationExportTask task = new NavigationExportTask(stepResultHandler.getResults().get(0), marshaller);
            resultHandler.completed(new ExportResourceModel(task));
         }
      }
      catch (OperationException e)
      {
         throw new OperationException(e.getOperationName(), getStepMessage(e, address, stepResultHandler), e);
      }
      catch (Throwable t)
      {
         throw new OperationException(operationName, getStepMessage(t, address, stepResultHandler), t);
      }
   }

   private void executeHandlers(ManagedResource resource, final OperationContext operationContext, PathAddress address, String operationName, StepResultHandler<PageNavigation> stepResultHandler, PathTemplateFilter filter, boolean root)
   {
      //resource.getSubResource(address);
      OperationHandler handler = resource.getOperationHandler(address, operationName);
      System.out.println("Filter: " + filter + " accepted ? " + address.accepts(filter) + " for address " + address + " and nav-uri template " + address.resolvePathTemplate("nav-uri"));
      if (handler != null && !root && address.accepts(filter))
      {
         handler.execute(operationContext, stepResultHandler);
      }
      else
      {
         OperationHandler readResource = resource.getOperationHandler(address, OperationNames.READ_RESOURCE);
         BasicResultHandler readResourceResult = new BasicResultHandler();
         readResource.execute(new OperationContextDelegate(operationContext)
         {
            @Override
            public String getOperationName()
            {
               return OperationNames.READ_RESOURCE;
            }
         }, readResourceResult);
         if (readResourceResult.getFailureDescription() != null)
         {
            throw new OperationException(operationName, "Failure '" + readResourceResult.getFailureDescription() + "' encountered executing " + OperationNames.READ_RESOURCE);
         }

         Object model = readResourceResult.getResult();
         if (! (model instanceof ReadResourceModel) )
         {
            throw new RuntimeException("Was expecting " + ReadResourceModel.class + " to be returned for operation " + OperationNames.READ_RESOURCE + " at address " + address);
         }

         for (String child : ((ReadResourceModel) model).getChildren())
         {
            final PathAddress childAddress = address.append(child);
            OperationContext childContext = new OperationContextDelegate(operationContext)
            {
               @Override
               public PathAddress getAddress()
               {
                  return childAddress;
               }
            };
            executeHandlers(resource, childContext, childAddress, operationName, stepResultHandler.next(childAddress), filter, false);
         }
      }
   }

   private String getStepMessage(Throwable t, PathAddress originalAddress, StepResultHandler<PageNavigation> stepResultHandler)
   {
      String message = (t.getMessage() == null) ? "Step operation failure" : t.getMessage();
      if (originalAddress.equals(stepResultHandler.getCurrentAddress()))
      {
         return message;
      }
      else
      {
         return message + " [Step Address: " + stepResultHandler.getCurrentAddress() + "]";
      }
   }

   private void merge(PageNavigation navigation, PageNavigation result)
   {
      for (NavigationFragment fragment : result.getFragments())
      {
         if (fragment.getParentURI() != null)
         {
            NavigationFragment found = findFragment(navigation, fragment.getParentURI());
            if (found == null)
            {
               navigation.addFragment(fragment);
            }
            else
            {
               found.getNodes().addAll(fragment.getNodes());
            }
         }
         else
         {
            navigation.addFragment(fragment);
         }
      }
   }

   private NavigationFragment findFragment(PageNavigation navigation, String parentUri)
   {
      for (NavigationFragment fragment : navigation.getFragments())
      {
         if (fragment.getParentURI().equals(parentUri)) return fragment;
      }

      return null;
   }

   private static class BasicResultHandler implements ResultHandler
   {
      private Object result;
      private String failureDescription;

      @Override
      public void completed(Object result)
      {
         this.result = result;
      }

      @Override
      public void failed(String failureDescription)
      {
         this.failureDescription = failureDescription;
      }

      public Object getResult()
      {
         return result;
      }

      public String getFailureDescription()
      {
         return failureDescription;
      }
   }
}
