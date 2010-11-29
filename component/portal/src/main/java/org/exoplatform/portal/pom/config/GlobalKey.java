/*
 * Copyright (C) 2009 eXo Platform SAS.
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

package org.exoplatform.portal.pom.config;

import java.io.Serializable;

/**
 * A global key wrapping a local key including the current repository id.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class GlobalKey implements Serializable
{

   public static GlobalKey wrap(String repositoryName, Serializable localKey)
   {
      return new GlobalKey(repositoryName, localKey);
   }

   /** . */
   private final String repositoryId;

   /** . */
   private final Serializable localKey;

   public GlobalKey(String repositoryId, Serializable localKey)
   {
      if (repositoryId == null)
      {
         throw new NullPointerException();
      }
      if (localKey == null)
      {
         throw new NullPointerException();
      }
      this.repositoryId = repositoryId;
      this.localKey = localKey;
   }

   public String getRepositoryId()
   {
      return repositoryId;
   }

   public Serializable getLocalKey()
   {
      return localKey;
   }

   @Override
   public int hashCode()
   {
      return repositoryId.hashCode() ^ localKey.hashCode();
   }

   @Override
   public boolean equals(Object obj)
   {
      if (obj == this)
      {
         return true;
      }
      if (obj instanceof GlobalKey)
      {
         GlobalKey that = (GlobalKey)obj;
         return repositoryId.equals(that.repositoryId) && localKey.equals(that.localKey);
      }
      return false;
   }

   @Override
   public String toString()
   {
      return "GlobalKey[repositoryId=" + repositoryId + ",localKey=" + localKey + "]";
   }
}
