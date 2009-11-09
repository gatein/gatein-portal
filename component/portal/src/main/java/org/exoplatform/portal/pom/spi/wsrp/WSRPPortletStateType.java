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

import org.gatein.pc.api.PortletStateType;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class WSRPPortletStateType extends PortletStateType<WSRP>
{
   public static final WSRPPortletStateType instance = new WSRPPortletStateType();

   @Override
   public Class<WSRP> getJavaType()
   {
      return WSRP.class;
   }

   @Override
   public boolean equals(WSRP state1, WSRP state2)
   {
      return state1.equals(state2);
   }

   @Override
   public int hashCode(WSRP state)
   {
      return state.hashCode();
   }

   @Override
   public String toString(WSRP state)
   {
      return state.toString();
   }
}
