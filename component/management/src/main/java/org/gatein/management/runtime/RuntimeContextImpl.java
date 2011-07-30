package org.gatein.management.runtime;

import org.exoplatform.container.PortalContainer;
import org.gatein.management.api.RuntimeContext;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class RuntimeContextImpl implements RuntimeContext
{
   @Override
   public <T> T getRuntimeComponent(Class<T> componentClass)
   {
      return componentClass.cast(PortalContainer.getInstance().getComponentInstanceOfType(componentClass));
   }
}
