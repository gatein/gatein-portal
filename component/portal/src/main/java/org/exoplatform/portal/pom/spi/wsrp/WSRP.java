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

import org.gatein.mop.api.content.ContentType;

import java.io.Serializable;
import java.util.Arrays;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class WSRP implements Serializable
{
   public static final ContentType<WSRP> CONTENT_TYPE = new ContentType<WSRP>("application/wsrp", WSRP.class);

   private byte[] state;
   private String portletId;
   private boolean isCloned;

   public byte[] getState()
   {
      return state;
   }

   public void setState(byte[] state)
   {
      this.state = state;
   }

   public String getPortletId()
   {
      return portletId;
   }

   public void setPortletId(String portletId)
   {
      this.portletId = portletId;
   }

   public boolean isCloned()
   {
      return isCloned;
   }

   public void setCloned(boolean cloned)
   {
      isCloned = cloned;
   }

   @Override
   public boolean equals(Object o)
   {
      if (this == o)
      {
         return true;
      }
      if (!(o instanceof WSRP))
      {
         return false;
      }

      WSRP wsrp = (WSRP)o;

      if (portletId != null ? !portletId.equals(wsrp.portletId) : wsrp.portletId != null)
      {
         return false;
      }
      return Arrays.equals(state, wsrp.state);

   }

   @Override
   public int hashCode()
   {
      int result = state != null ? Arrays.hashCode(state) : 0;
      result = 31 * result + (portletId != null ? portletId.hashCode() : 0);
      return result;
   }

   @Override
   public String toString()
   {
      return "WSRP[portletId='" + portletId + "', state=" + state + ']';
   }
}
