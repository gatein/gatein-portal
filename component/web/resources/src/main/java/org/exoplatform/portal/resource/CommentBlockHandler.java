/*
 * Copyright (C) 2011 eXo Platform SAS.
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
package org.exoplatform.portal.resource;

import java.io.IOException;

/**
 * Designed to plugged into SkipCommentReader for custom handling of comment block
 *
 * @author <a href="hoang281283@gmail.com">Minh Hoang TO</a>
 * @date 6/28/11
 */
public abstract class CommentBlockHandler
{

   public abstract void handle(CharSequence commentBlock, SkipCommentReader reader) throws IOException;


   /**
    * A handler that push back content of comment block into the cache
    * if content of comment block is
    *
    *    orientation=lt  or orientation=rt
    */
   public static class OrientationCommentBlockHandler extends CommentBlockHandler
   {

      private static final String LT = "orientation=lt";

      private static final String RT = "orientation=rt";

      @Override
      public void handle(CharSequence commentBlock, SkipCommentReader reader) throws IOException
      {
         if(findInterestingContentIn(commentBlock))
         {
            reader.pushback(commentBlock);
            reader.setNumberOfCommingEscapes(commentBlock.length()); /* The comment block won't be skipped */
         }
      }

      /**
       * Return true if content of comment block is either
       *
       * orientation=lt or orientation=rt
       *
       * @param commentBlock
       * @return
       */
      private boolean findInterestingContentIn(CharSequence commentBlock)
      {

         int indexOfFirstO = 0;

         while(indexOfFirstO < commentBlock.length())
         {
            if(commentBlock.charAt(indexOfFirstO) == 'o')
            {
               break;
            }
            else
            {
               indexOfFirstO++;
            }
         }

         if(commentBlock.length() <= (indexOfFirstO + LT.length()))
         {
            return false;
         }
         for(int i = 0; i < LT.length(); i++)
         {
            if(commentBlock.charAt(indexOfFirstO + i) != LT.charAt(i) && i != (LT.length() -2))
            {
               return false;
            }
         }
         return commentBlock.charAt(indexOfFirstO + LT.length() - 2) == 'l'
                || commentBlock.charAt(indexOfFirstO + LT.length() - 2) == 'r';
      }

   }
}
