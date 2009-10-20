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

package org.exoplatform.services.text.unicode;

import java.util.BitSet;

/**
 * Accesses the Normalization Data used for Forms C and D.<br>
 * Copyright (c) 1998-1999 Unicode, Inc. All Rights Reserved.<br>
 * The Unicode Consortium makes no expressed or implied warranty of any
 * kind, and assumes no liability for errors or omissions.
 * No liability is assumed for incidental and consequential damages
 * in connection with or arising out of the use of the information here.
 * @author Mark Davis
 */
public class NormalizerData
{

   public static final int NOT_COMPOSITE = '\uFFFF';

   boolean getExcluded(char ch)
   {
      return isExcluded.get(ch);
   }

   String getRawDecompositionMapping(char ch)
   {
      return decompose.get(ch);
   }

   private IntHashtable canonicalClass;

   private IntStringHashtable decompose;

   private IntHashtable compose;

   private BitSet isCompatibility = new BitSet();

   private BitSet isExcluded = new BitSet();

   public int getCanonicalClass(char ch)
   {
      return canonicalClass.get(ch);
   }

   public char getPairwiseComposition(char first, char second)
   {
      return (char)compose.get((first << 16) | second);
   }

   public void getRecursiveDecomposition(boolean canonical, char ch, StringBuilder buffer)
   {
      String decomp = decompose.get(ch);
      if (decomp == null || (canonical && isCompatibility.get(ch)))
      {
         buffer.append(ch);
         return;
      }
      for (int i = 0; i < decomp.length(); ++i)
      {
         getRecursiveDecomposition(canonical, decomp.charAt(i), buffer);
      }
   }

   NormalizerData(IntHashtable canonicalClass, IntStringHashtable decompose, IntHashtable compose,
      BitSet isCompatibility, BitSet isExcluded)
   {
      this.canonicalClass = canonicalClass;
      this.decompose = decompose;
      this.compose = compose;
      this.isCompatibility = isCompatibility;
      this.isExcluded = isExcluded;
   }

}
