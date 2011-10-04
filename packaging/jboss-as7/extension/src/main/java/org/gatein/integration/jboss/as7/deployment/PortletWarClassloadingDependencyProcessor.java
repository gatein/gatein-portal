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
package org.gatein.integration.jboss.as7.deployment;

import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.as.server.deployment.module.ModuleDependency;
import org.jboss.as.server.deployment.module.ModuleSpecification;
import org.jboss.as.web.deployment.TldsMetaData;
import org.jboss.metadata.web.spec.TldMetaData;
import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoader;

import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class PortletWarClassloadingDependencyProcessor implements DeploymentUnitProcessor
{

   private static final ModuleIdentifier GATEIN_WCI = ModuleIdentifier.create("org.gatein.wci");
   private static final ModuleIdentifier GATEIN_PC = ModuleIdentifier.create("org.gatein.pc");
   private static final ModuleIdentifier JAVAX_PORTLET = ModuleIdentifier.create("javax.portlet.api");

   private List<TldMetaData> tldMetas;

   public PortletWarClassloadingDependencyProcessor(List<TldMetaData> tldMetaData)
   {
      this.tldMetas = tldMetaData;
   }

   @Override
   public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException
   {
      final DeploymentUnit deploymentUnit = phaseContext.getDeploymentUnit();
      if (deploymentUnit.getAttachment(PortletWarKey.INSTANCE) == null)
      {
         return; // Skip non portlet deployments
      }

      final ModuleLoader moduleLoader = Module.getBootModuleLoader();
      final ModuleSpecification moduleSpecification = deploymentUnit.getAttachment(Attachments.MODULE_SPECIFICATION);

      // Add module dependencies
      moduleSpecification.addSystemDependency(new ModuleDependency(moduleLoader, GATEIN_WCI, false, false, false));
      moduleSpecification.addSystemDependency(new ModuleDependency(moduleLoader, GATEIN_PC, false, false, false));
      moduleSpecification.addSystemDependency(new ModuleDependency(moduleLoader, JAVAX_PORTLET, false, false, false));

      // Provide tlds for portlet taglibs
      provideTlds(deploymentUnit);
   }

   private void provideTlds(DeploymentUnit deploymentUnit)
   {
      TldsMetaData tsmd = deploymentUnit.getAttachment(TldsMetaData.ATTACHMENT_KEY);
      if (tsmd == null)
      {
         throw new IllegalStateException("Attachment not present: TldsMetaData");
      }

      Map<String, TldMetaData> tlds = tsmd.getTlds();
      if (tlds == null)
      {
         throw new IllegalStateException("TldsMetaData.tlds == null");
      }

      for (TldMetaData tld : tldMetas)
      {
         tlds.put(tld.getUri(), tld);
      }
   }

   @Override
   public void undeploy(DeploymentUnit context)
   {
   }
}
