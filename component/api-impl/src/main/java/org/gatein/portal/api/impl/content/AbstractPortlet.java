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

package org.gatein.portal.api.impl.content;

import org.gatein.api.content.Content;
import org.gatein.api.id.Id;
import org.gatein.portal.api.impl.GateInImpl;
import org.gatein.portal.api.impl.IdentifiableImpl;

/** @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a> */
public abstract class AbstractPortlet<T extends Content<T>> extends IdentifiableImpl<T> implements Content<T>
{
   private final org.gatein.pc.api.Portlet portlet;

   public AbstractPortlet(Id<T> id, org.gatein.pc.api.Portlet application, GateInImpl gateIn)
   {
      super(id, application.getInfo().getName(), gateIn);
      this.portlet = application;
   }

   @Override
   public String toString()
   {
      return getType().getName() + " Portlet '" + getName() + "' @" + getId();
   }

}
