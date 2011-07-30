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

package org.exoplatform.portal.mop.management.operations.site;

import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.management.operations.AbstractMopOperationHandler;
import org.gatein.management.api.PathAddress;
import org.gatein.management.api.exceptions.OperationException;
import org.gatein.management.api.exceptions.ResourceNotFoundException;
import org.gatein.management.api.operation.OperationContext;
import org.gatein.management.api.operation.ResultHandler;
import org.gatein.mop.api.workspace.ObjectType;
import org.gatein.mop.api.workspace.Site;
import org.gatein.mop.api.workspace.Workspace;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public abstract class AbstractSiteOperationHandler extends AbstractMopOperationHandler
{
   @Override
   protected final void execute(OperationContext operationContext, ResultHandler resultHandler, Workspace workspace, ObjectType<Site> siteType) throws ResourceNotFoundException, OperationException
   {
      String operationName = operationContext.getOperationName();
      PathAddress address = operationContext.getAddress();

      String siteName = address.resolvePathTemplate("site-name");
      if (siteName == null) throw new OperationException(operationName, "No site name specified.");

      SiteKey siteKey = getSiteKey(siteType, siteName);

      Site site = workspace.getSite(siteType, siteKey.getName());
      if (site == null) throw new ResourceNotFoundException("No site found for site " + siteKey);

      execute(operationContext, resultHandler, site);
   }

   protected abstract void execute(OperationContext operationContext, ResultHandler resultHandler, Site site) throws ResourceNotFoundException, OperationException;
}
