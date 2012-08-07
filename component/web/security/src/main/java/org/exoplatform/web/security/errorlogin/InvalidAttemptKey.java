/******************************************************************************
 * JBoss, a division of Red Hat                                               *
 * Copyright 2010, Red Hat Middleware, LLC, and individual                    *
 * contributors as indicated by the @authors tag. See the                     *
 * copyright.txt in the distribution for a full listing of                    *
 * individual contributors.                                                   *
 *                                                                            *
 * This is free software; you can redistribute it and/or modify it            *
 * under the terms of the GNU Lesser General Public License as                *
 * published by the Free Software Foundation; either version 2.1 of           *
 * the License, or (at your option) any later version.                        *
 *                                                                            *
 * This software is distributed in the hope that it will be useful,           *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU           *
 * Lesser General Public License for more details.                            *
 *                                                                            *
 * You should have received a copy of the GNU Lesser General Public           *
 * License along with this software; if not, write to the Free                *
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA         *
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.                   *
 ******************************************************************************/
package org.exoplatform.web.security.errorlogin;

import org.exoplatform.commons.utils.Safe;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 * @version $Revision$
 */
class InvalidAttemptKey
{
   private final String sessionId;
   private final String username;
   private final String hostname;   
   
   private InvalidAttemptKey(String sessionId, String username, String hostname)
   {
      this.sessionId = sessionId;
      this.username = username;
      this.hostname = hostname;
   }
   
   public static InvalidAttemptKey createKey(InvalidLoginPolicy policy, String sessionId, String username, String hostname)
   {
      switch (policy)
      {
         case SESSION: return new InvalidAttemptKey(sessionId, null, null);
         
         case SESSION_AND_USER: return new InvalidAttemptKey(sessionId, username, null);
         
         case SERVER: return new InvalidAttemptKey(null, null, hostname);
         
         default: throw new IllegalArgumentException("Non-expected value of InvalidLoginPolicy.");
      }
   } 
   
   public boolean equals(Object obj)
   {
      if (obj == this)
      {
         return true;
      }
      if (obj instanceof InvalidAttemptKey)
      {
         InvalidAttemptKey that = (InvalidAttemptKey)obj;
         return Safe.equals(sessionId, that.sessionId) && Safe.equals(username, that.username) && Safe.equals(hostname, that.hostname);
      }
      return false;      
   }
   
   public int hashCode()
   {
      int result = 1234567;
      if (sessionId != null)
      {
         result = sessionId.hashCode();
      }
      if (username != null)
      {
         result = result ^ username.hashCode();
      }
      if (hostname != null)
      {
         result = result ^ hostname.hashCode();
      }
      return result;
   }

}

