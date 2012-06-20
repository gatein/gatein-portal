/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
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
package org.gatein.integration.jboss.as7;

import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class DeploymentDirHandler
{
   private static final Logger log = Logger.getLogger("org.gatein");

   private static final String GATEIN_EAR = "gatein.ear";

   private static final PathElement SCANNER_GATEIN_PATH_ELEMENT = PathElement.pathElement("scanner","gatein");
   private static final PathElement DEPLOYMENT_SCANNER_SUBSYSTEM_PATH_ELEMENT = PathElement.pathElement("subsystem", "deployment-scanner");
   private static final PathElement GATEIN_PATH_SUBSYSTEM_ELEMENT = PathElement.pathElement("subsystem", "gatein");

   public static void handleDeploymentDir(OperationContext context, GateInConfiguration config)
   {
      boolean hasGateInScanner = context.getRootResource().navigate(PathAddress.pathAddress(DEPLOYMENT_SCANNER_SUBSYSTEM_PATH_ELEMENT)).hasChild(SCANNER_GATEIN_PATH_ELEMENT);

      if (hasGateInScanner)
      {
         ModelNode scannerModel = context.getRootResource().navigate(
            PathAddress.pathAddress(DEPLOYMENT_SCANNER_SUBSYSTEM_PATH_ELEMENT, SCANNER_GATEIN_PATH_ELEMENT)).getModel();

         String path = scannerModel.get(Constants.PATH).asString();
         String relativeTo = scannerModel.get(Constants.RELATIVE_TO).asString();

         File absolute;
         if (relativeTo != null)
         {
            String propVal = System.getProperty(relativeTo);
            if (propVal != null)
            {
               absolute = new File(propVal, path);
            }
            else
            {
               throw new IllegalStateException("Unresolvable relativeTo property of 'gatein' deployment-scanner: " + relativeTo);
            }
         }
         else
         {
            absolute = new File(path);
         }

         // list files in deployments dir
         String[] filesList = absolute.list(new FilenameFilter()
         {
            @Override
            public boolean accept(File dir, String name)
            {
               return name.endsWith(".war") || name.endsWith(".ear");
            }
         });

         if (filesList == null)
         {
            log.warn("'gatein' deployment-scanner directory (" + absolute + ") is empty. Looks like invalid configuration!");
            filesList = new String[0];
         }
         Set<String> filesSet = new HashSet(Arrays.asList(filesList));

         // look in archives for definition with main=true
         String gateinEar = config.getGateInEarName();

         // we are overriding any previously set config
         config.clearDeploymentArchives();

         if (gateinEar != null)
         {
            // if found, and the archive doesn't exist throw exception
            if (!filesSet.contains(gateinEar))
               throw new IllegalStateException("The declared main archive (" + gateinEar + ") doesn't exist in " + absolute);
         }
         else
         {
            gateinEar = GATEIN_EAR;
         }

         for (String name: filesSet)
         {
            config.addDeploymentArchive(name, name.equals(gateinEar));
         }
         log.info("Using 'gatein' deployment-scanner. Note that gatein subsystem <deployment-archives> section entries will be ignored except the one marked main='true'.");
      }
      else
      {
         // we also support a mode where there is no gatein-specific deployment scanner
         log.warn("Separate deployment-scanner for 'gatein' not configured. Make sure to have gatein subsystem <deployment-archives> section properly configured.");
      }
   }
}
