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

public abstract class Verifier
{

   static final byte eStart = (byte)0;

   static final byte eError = (byte)1;

   static final byte eItsMe = (byte)2;

   static final int eidxSft4bits = 3;

   static final int eSftMsk4bits = 7;

   static final int eBitSft4bits = 2;

   static final int eUnitMsk4bits = 0x0000000F;

   protected int[] cclass;

   protected int[] states;

   protected int stFactor;

   protected String charset;

   Verifier()
   {
   }

   public int[] cclass()
   {
      return cclass;
   }

   public int[] states()
   {
      return states;
   }

   public int stFactor()
   {
      return stFactor;
   }

   public String charset()
   {
      return charset;
   }

   public boolean isUCS2()
   {
      return false;
   };

   public static byte getNextState(Verifier v, byte b, byte s)
   {

      return (byte)(0xFF & (((v.states()[(((s * v.stFactor() + (((v.cclass()[((b & 0xFF) >> Verifier.eidxSft4bits)]) >> ((b & Verifier.eSftMsk4bits) << Verifier.eBitSft4bits)) & Verifier.eUnitMsk4bits)) & 0xFF) >> Verifier.eidxSft4bits)]) >> ((((s
         * v.stFactor() + (((v.cclass()[((b & 0xFF) >> Verifier.eidxSft4bits)]) >> ((b & Verifier.eSftMsk4bits) << Verifier.eBitSft4bits)) & Verifier.eUnitMsk4bits)) & 0xFF) & Verifier.eSftMsk4bits) << Verifier.eBitSft4bits)) & Verifier.eUnitMsk4bits));

   }

}
