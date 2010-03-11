/*
 * JBoss, a division of Red Hat
 * Copyright 2010, Red Hat Middleware, LLC, and individual
 * contributors as indicated by the @authors tag. See the
 * copyright.txt in the distribution for a full listing of
 * individual contributors.
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

package org.gatein.portal.wsrp.state.producer.state.mapping;

import org.chromattic.api.annotations.Create;
import org.chromattic.api.annotations.FindById;
import org.chromattic.api.annotations.FormattedBy;
import org.chromattic.api.annotations.OneToMany;
import org.chromattic.api.annotations.PrimaryType;
import org.gatein.portal.wsrp.state.JCRPersister;

import java.util.Collection;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
@PrimaryType(name = PortletStateContextsMapping.NODE_NAME)
@FormattedBy(JCRPersister.PortletNameFormatter.class)
public abstract class PortletStateContextsMapping
{
   public static final String NODE_NAME = "pc:producerstates";

   @OneToMany
   public abstract Collection<PortletStateContextMapping> getPortletStateContexts();

   @Create
   public abstract PortletStateContextMapping createPortletStateContext(String id);

   @FindById
   public abstract PortletStateContextMapping findPortletStateContextById(String id);
}
