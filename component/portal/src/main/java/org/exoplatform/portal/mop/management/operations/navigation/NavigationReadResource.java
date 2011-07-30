package org.exoplatform.portal.mop.management.operations.navigation;

import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.navigation.NavigationContext;
import org.exoplatform.portal.mop.navigation.NavigationService;
import org.exoplatform.portal.mop.navigation.NodeContext;
import org.gatein.management.api.exceptions.ResourceNotFoundException;
import org.gatein.management.api.operation.OperationContext;
import org.gatein.management.api.operation.ResultHandler;
import org.gatein.management.api.operation.model.ReadResourceModel;
import org.gatein.mop.api.workspace.Navigation;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class NavigationReadResource extends AbstractNavigationOperationHandler
{
   @Override
   protected void execute(OperationContext operationContext, ResultHandler resultHandler, Navigation defaultNavigation)
   {
      SiteKey siteKey = getSiteKey(defaultNavigation.getSite());
      String navUri = operationContext.getAddress().resolvePathTemplate("nav-uri");

      NavigationService navigationService = operationContext.getRuntimeContext().getRuntimeComponent(NavigationService.class);
      NavigationContext navigation = navigationService.loadNavigation(siteKey);

      Set<String> children = new LinkedHashSet<String>();

      NodeContext<NodeContext<?>> node = NavigationUtils.loadNode(navigationService, navigation, navUri);
      if (node == null)
      {
         throw new ResourceNotFoundException("Navigation node not found for navigation uri '" + navUri +"'");
      }

      for (NodeContext child : node.getNodes())
      {
         children.add(child.getName());
      }

      ReadResourceModel model = new ReadResourceModel("Navigation nodes available at this resource.", children);
      resultHandler.completed(model);
   }
}
