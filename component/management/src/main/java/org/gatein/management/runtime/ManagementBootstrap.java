package org.gatein.management.runtime;

import org.gatein.management.api.ManagementService;
import org.picocontainer.Startable;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class ManagementBootstrap implements Startable
{
   private ManagementService service;

   public ManagementBootstrap(ManagementService service)
   {
      this.service = service;
   }

   @Override
   public void start()
   {
      service.load();
   }

   @Override
   public void stop()
   {
      service.unload();
   }
}
