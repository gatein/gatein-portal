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

package org.exoplatform.services.chars.chardet;

public abstract class EUCStatistics
{

   float[] mFirstByteFreq;

   float mFirstByteStdDev;

   float mFirstByteMean;

   float mFirstByteWeight;

   float[] mSecondByteFreq;

   float mSecondByteStdDev;

   float mSecondByteMean;

   float mSecondByteWeight;

   public float[] mFirstByteFreq()
   {
      return mFirstByteFreq;
   }

   public float mFirstByteStdDev()
   {
      return mFirstByteStdDev;
   }

   public float mFirstByteMean()
   {
      return mFirstByteMean;
   }

   public float mFirstByteWeight()
   {
      return mFirstByteWeight;
   }

   public float[] mSecondByteFreq()
   {
      return mSecondByteFreq;
   }

   public float mSecondByteStdDev()
   {
      return mSecondByteStdDev;
   }

   public float mSecondByteMean()
   {
      return mSecondByteMean;
   }

   public float mSecondByteWeight()
   {
      return mSecondByteWeight;
   }

   //public abstract float[] mFirstByteFreq() ;
   //public abstract float   mFirstByteStdDev();
   //public abstract float   mFirstByteMean();
   //public abstract float   mFirstByteWeight();
   //public abstract float[] mSecondByteFreq();
   //public abstract float   mSecondByteStdDev();
   //public abstract float   mSecondByteMean();
   //public abstract float   mSecondByteWeight();

}
