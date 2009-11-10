/*
* JBoss, a division of Red Hat
* Copyright 2008, Red Hat Middleware, LLC, and individual contributors as indicated
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
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

package org.exoplatform.portal.pom.spi.wsrp;

import org.chromattic.api.annotations.NodeMapping;
import org.chromattic.api.annotations.Property;

import java.io.InputStream;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
@NodeMapping(name = WSRPState.MOP_NODE_NAME)
public abstract class WSRPState
{
   static final String MOP_NODE_NAME = "mop:wsrpstate";

   @Property(name = "portletid")
   public abstract String getPortletId();

   public abstract void setPortletId(String portletHandle);

   @Property(name = "state")
   public abstract InputStream getState();

   public abstract void setState(InputStream state);

   @Property(name = "cloned")
   public abstract boolean getCloned();

   public abstract void setCloned(boolean cloned);
}
