/**
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

package org.exoplatform.commons.utils;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Extends the @link{AtomicLong} to contain positive longs. If no initial value
 * is provided when the object is created then the value is -1 to indicate that
 * it is not yet initialized.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 * @todo move to common utils
 */
public class AtomicPositiveLong extends AtomicLong
{

   /**
    * Create an atomic positive long with an inital provided value.
    *
    * @param initialValue the initial value
    * @throws IllegalArgumentException if the value is negative
    */
   public AtomicPositiveLong(long initialValue) throws IllegalArgumentException
   {
      super(initialValue);

      //
      if (initialValue == -1)
      {
         throw new IllegalArgumentException();
      }
   }

   /**
    * Create an atomic positive long with no initial value.
    */
   public AtomicPositiveLong()
   {
      super(-1);
   }

   /**
    * Update the value if the new value is greater than the previous one or if the long is not initialized.
    *
    * @param newValue the new value
    * @throws IllegalArgumentException if the new value is negative
    */
   public void setIfGreater(long newValue) throws IllegalArgumentException
   {
      if (newValue < 0)
      {
         throw new IllegalArgumentException();
      }
      while (true)
      {
         long oldValue = get();
         if (newValue > oldValue || oldValue == -1)
         {
            compareAndSet(oldValue, newValue);
         }
         else
         {
            break;
         }
      }
   }

   /**
    * Update the value if the new value is lower than the previous one or if the long is not initialized.
    *
    * @param newValue the new value
    * @throws IllegalArgumentException if the new value is negative
    */
   public void setIfLower(long newValue) throws IllegalArgumentException
   {
      if (newValue < 0)
      {
         throw new IllegalArgumentException();
      }
      while (true)
      {
         long oldValue = get();
         if (newValue < oldValue || oldValue == -1)
         {
            compareAndSet(oldValue, newValue);
         }
         else
         {
            break;
         }
      }
   }
}
