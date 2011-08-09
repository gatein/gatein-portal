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

package org.gatein.portal.api.impl;

import org.gatein.api.GateIn;
import org.gatein.api.id.Id;
import org.gatein.api.id.Identifiable;

/** @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a> */
public class IdentifiableImpl<T extends Identifiable<T>> implements Identifiable<T>
{
   private final Id<T> id;
   private final String name;
   private final String displayName;
   private final GateInImpl gateIn;

   public IdentifiableImpl(Id<T> id, String name, GateInImpl gateIn)
   {
      this.id = id;
      this.name = name;
      this.gateIn = gateIn;
      this.displayName = name; // todo: fix-me
   }

   public Id<T> getId()
   {
      return id;
   }

   public String getName()
   {
      return name;
   }

   public String getDisplayName()
   {
      return displayName;
   }

   public GateIn getGateIn()
   {
      return getGateInImpl();
   }

   protected GateInImpl getGateInImpl()
   {
      return gateIn;
   }
}
