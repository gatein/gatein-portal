package org.exoplatform.portal.mop.management.operations.page;

import org.exoplatform.portal.config.DataStorage;
import org.gatein.management.api.exceptions.OperationException;
import org.gatein.management.api.exceptions.ResourceNotFoundException;
import org.gatein.management.api.operation.OperationContext;
import org.gatein.management.api.operation.ResultHandler;
import org.gatein.management.api.operation.model.ReadResourceModel;
import org.gatein.mop.api.workspace.Page;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class PageReadResource extends AbstractPageOperationHandler
{
   @Override
   protected void execute(OperationContext operationContext, ResultHandler resultHandler, Page rootPage) throws ResourceNotFoundException, OperationException
   {
      String pageName = operationContext.getAddress().resolvePathTemplate("page-name");
      if (pageName == null)
      {
         Collection<Page> pageList = rootPage.getChildren();
         Set<String> children = new LinkedHashSet<String>(pageList.size());
         for (Page page : pageList)
         {
            children.add(page.getName());
         }

         resultHandler.completed(new ReadResourceModel("List of all available pages for site '" + rootPage.getSite().getName() +"'", children));
      }
      else
      {
         DataStorage dataStorage = operationContext.getRuntimeContext().getRuntimeComponent(DataStorage.class);
         PageKey pageKey = new PageKey(getSiteKey(rootPage.getSite()), pageName);

         if (PageUtils.getPage(dataStorage, pageKey, operationContext.getOperationName()) == null)
         {
            throw new ResourceNotFoundException("No page found for " + pageKey);
         }

         resultHandler.completed(new ReadResourceModel("List of child pages for page '" + pageName +"'", Collections.<String>emptySet()));
      }
   }
}
