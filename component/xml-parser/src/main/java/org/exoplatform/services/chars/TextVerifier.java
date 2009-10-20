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

package org.exoplatform.services.chars;

public class TextVerifier
{

   public boolean startIn(String line, String[] pattern)
   {
      for (String ele : pattern)
         if (line.startsWith(ele))
            return true;
      return false;
   }

   public boolean endIn(String line, String[] pattern)
   {
      for (String ele : pattern)
         if (line.endsWith(ele))
            return true;
      return false;
   }

   public boolean existIn(String line, String[] pattern)
   {
      for (String ele : pattern)
         if (line.indexOf(ele) > -1)
            return true;
      return false;
   }

   public boolean existAll(String line, String[] pattern)
   {
      for (String ele : pattern)
         if (!(line.indexOf(ele) > -1))
            return false;
      return true;
   }

   public boolean equalsIn(String line, String[] pattern)
   {
      for (String ele : pattern)
         if (line.equals(ele))
            return true;
      return false;
   }

   public boolean startOrEnd(String line, String[] start, String[] end)
   {
      return startIn(line, start) || endIn(line, end);
   }

   public boolean startAndEnd(String line, String[] start, String[] end)
   {
      return startIn(line, start) && endIn(line, end);
   }

   public boolean startOrEndOrExist(String line, String[] start, String[] end, String[] exist)
   {
      return startIn(line, start) || endIn(line, end) || existIn(line, exist);
   }

   public boolean startAndEndAndExist(String line, String[] start, String[] end, String[] exist)
   {
      return startIn(line, start) && endIn(line, end) && existIn(line, exist);
   }

   public boolean startAndEndAndExistAll(String line, String[] start, String[] end, String[] exist)
   {
      return startIn(line, start) && endIn(line, end) && existAll(line, exist);
   }

}
