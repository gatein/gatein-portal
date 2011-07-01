/*
 * Copyright (C) 2010 eXo Platform SAS.
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

package org.exoplatform.portal.mop.user;

import org.exoplatform.commons.utils.Safe;
import org.exoplatform.portal.mop.Visibility;

import java.util.EnumSet;
import java.util.Set;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class UserNodeFilterConfig
{

   /** . */
   final Set<Visibility> visibility;

   /** . */
   final boolean authorizationCheck;

   /** . */
   final boolean temporalCheck;

   public UserNodeFilterConfig(Builder builder)
   {
      if (builder == null)
      {
         throw new NullPointerException();
      }

      //
      this.visibility = Safe.unmodifiableSet(builder.withVisibility);
      this.authorizationCheck = builder.withAuthorizationCheck;
      this.temporalCheck = builder.withTemporalCheck;
   }

   public Set<Visibility> getVisibility()
   {
      return visibility;
   }

   public boolean getAuthorizationCheck()
   {
      return authorizationCheck;
   }

   public boolean getTemporalCheck()
   {
      return temporalCheck;
   }

   public static Builder builder()
   {
      return new Builder();
   }

   public static Builder builder(UserNodeFilterConfig predicate)
   {
      return new Builder(predicate);
   }

   public static class Builder
   {

      /** . */
      private Set<Visibility> withVisibility = null;

      /** . */
      private boolean withAuthorizationCheck = false;

      /** . */
      private boolean withTemporalCheck = false;

      private Builder()
      {
         this.withVisibility = null;
         this.withAuthorizationCheck = false;
         this.withTemporalCheck = false;
      }

      private Builder(UserNodeFilterConfig predicate)
      {
         this.withVisibility = predicate.visibility;
         this.withAuthorizationCheck = predicate.authorizationCheck;
         this.withTemporalCheck = predicate.temporalCheck;
      }

      public Builder withVisibility(Visibility first, Visibility... rest)
      {
         withVisibility = EnumSet.of(first, rest);
         return this;
      }

      public Builder withVisibility(Visibility first)
      {
         withVisibility = EnumSet.of(first);
         return this;
      }

      public Builder withoutVisibility()
      {
         withVisibility = null;
         return this;
      }

      public Builder withTemporalCheck()
      {
         this.withTemporalCheck = true;
         return this;
      }

      public Builder withoutTemporalCheck()
      {
         this.withTemporalCheck = false;
         return this;
      }

      public Builder withAuthorizationCheck()
      {
         this.withAuthorizationCheck = true;
         return this;
      }

      public Builder withoutAuthorizationChek()
      {
         this.withAuthorizationCheck = false;
         return this;
      }

      public UserNodeFilterConfig build()
      {
         return new UserNodeFilterConfig(this);
      }
   }
}
