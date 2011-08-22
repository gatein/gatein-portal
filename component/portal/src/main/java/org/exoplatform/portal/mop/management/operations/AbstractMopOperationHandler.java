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

package org.exoplatform.portal.mop.management.operations;

import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.pom.config.POMSession;
import org.exoplatform.portal.pom.config.POMSessionManager;
import org.gatein.management.api.PathAddress;
import org.gatein.management.api.exceptions.OperationException;
import org.gatein.management.api.exceptions.ResourceNotFoundException;
import org.gatein.management.api.operation.OperationContext;
import org.gatein.management.api.operation.OperationHandler;
import org.gatein.management.api.operation.ResultHandler;
import org.gatein.mop.api.workspace.ObjectType;
import org.gatein.mop.api.workspace.Site;
import org.gatein.mop.api.workspace.Workspace;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public abstract class AbstractMopOperationHandler implements OperationHandler
{
   @Override
   public final void execute(OperationContext operationContext, ResultHandler resultHandler) throws ResourceNotFoundException, OperationException
   {
      String operationName = operationContext.getOperationName();
      PathAddress address = operationContext.getAddress();

      String siteType = address.resolvePathTemplate("site-type");
      if (siteType == null) throw new OperationException(operationName, "Site type was not specified.");

      ObjectType<Site> objectType = Utils.getObjectType(Utils.getSiteType(siteType));
      if (objectType == null)
      {
         throw new ResourceNotFoundException("No site type found for " + siteType);
      }

      POMSessionManager mgr = operationContext.getRuntimeContext().getRuntimeComponent(POMSessionManager.class);
      POMSession session = mgr.getSession();
      if (session == null) throw new OperationException(operationName, "MOP session was null");

      Workspace workspace = session.getWorkspace();
      if (workspace == null) throw new OperationException(operationName, "MOP workspace was null");

      execute(operationContext, resultHandler, workspace, objectType);
   }

   protected abstract void execute(OperationContext operationContext, ResultHandler resultHandler,
                                   Workspace workspace, ObjectType<Site> siteType) throws ResourceNotFoundException, OperationException;


   protected SiteType getSiteType(ObjectType<? extends Site> objectType)
   {
      return Utils.getSiteType(objectType);
   }

   protected SiteKey getSiteKey(ObjectType<? extends Site> objectType, String name)
   {
      return Utils.siteKey(Utils.getSiteType(objectType), name);
   }

   protected SiteKey getSiteKey(Site site)
   {
      return getSiteKey(site.getObjectType(), site.getName());
   }
}
