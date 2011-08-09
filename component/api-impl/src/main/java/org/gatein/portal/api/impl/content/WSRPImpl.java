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
import org.gatein.api.content.WSRP;
import org.gatein.api.id.Id;
import org.gatein.api.util.Type;
import org.gatein.portal.api.impl.GateInImpl;

/** @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a> */
public class WSRPImpl extends AbstractPortlet<WSRP> implements WSRP
{
   public WSRPImpl(Id<WSRP> id, org.gatein.pc.api.Portlet application, GateInImpl gateIn)
   {
      super(id, application, gateIn);
   }

   public Type<WSRP> getType()
   {
      return Content.WSRP;
   }
}
