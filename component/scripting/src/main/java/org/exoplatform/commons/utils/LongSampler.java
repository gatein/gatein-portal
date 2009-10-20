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

/**
 * An object that sample long values.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 * @todo move to common utils
 */
public class LongSampler extends BoundedBuffer<Long>
{

   public LongSampler(int maxSize)
   {
      super(maxSize);
   }

   /**
    * Returns the average value.
    *
    * @return the average
    */
   public double average()
   {
      long sumTime = 0;
      int size = 0;
      for (long value : this)
      {
         sumTime += value;
         size++;
      }
      return size == 0 ? 0 : (double)sumTime / (double)size;
   }

   /**
    * Returns the number of values which are greater or equals to the threshold value.
    *
    * @param threshold the threshold value
    * @return the count of values above the provided threshold
    */
   public int countAboveThreshold(long threshold)
   {
      System.out.println("bbb" + threshold);
      int count = 0;
      for (long value : this)
      {
         System.out.println("aaaa" + value);
         if (value >= threshold)
         {
            count++;
         }
      }
      return count;
   }
}
