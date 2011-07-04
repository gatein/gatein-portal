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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

/**
 *
 * A subclass of BufferedReader which skip the comment block
 *
 * @author <a href="hoang281283@gmail.com">Minh Hoang TO</a>
 * @date 6/27/11
 */
public class SkipCommentReader extends BufferedReader
{

   private final StringBuilder pushbackCache;

   private final static int EOF = -1;

   private State cursorState;

   private CommentBlockHandler commentBlockHandler;

   /* The number of next comming characters that won't be skipped even if they are in a comment block */
   private int numberOfCommingEscapes;

   public SkipCommentReader(Reader reader)
   {
      this(reader, null);
   }

   public SkipCommentReader(Reader reader, CommentBlockHandler handler)
   {
      super(reader);
      pushbackCache = new StringBuilder();
      cursorState = State.ENCOUNTING_ORDINARY_CHARACTER;
      this.commentBlockHandler = handler;
   }

   /**
    * Recursive method that read a single character from underlying reader. Encountered comment block
    * is escaped automatically.
    *
    * @return
    * @throws IOException
    */
   public int readSingleCharacter() throws IOException
   {
      int readingChar = readLikePushbackReader();
      if(readingChar == EOF)
      {
         return EOF;
      }

      if(numberOfCommingEscapes > 0)
      {
         numberOfCommingEscapes--;
         return readingChar;
      }

      switch (readingChar)
      {
         case '/':
            int nextCharToRead = read();
            if (nextCharToRead == '*')
            {
               this.cursorState = SkipCommentReader.State.ENCOUNTING_COMMENT_BLOCK_OPENING_TAG;
               advanceToEscapeCommentBlock();
               return readSingleCharacter();
            }
            else
            {
               this.cursorState = SkipCommentReader.State.ENCOUNTING_FORWARD_SLASH;
               pushbackCache.append((char)nextCharToRead);
               return '/';
            }

         case '*':
            if (this.cursorState == SkipCommentReader.State.ENCOUNTING_FORWARD_SLASH)
            {
               this.cursorState = SkipCommentReader.State.ENCOUNTING_COMMENT_BLOCK_OPENING_TAG;
               advanceToEscapeCommentBlock();
               return readSingleCharacter();
            }
            else
            {
               this.cursorState = SkipCommentReader.State.ENCOUNTING_ASTERIK;
               return '*';
            }

         default:
            this.cursorState = SkipCommentReader.State.ENCOUNTING_ORDINARY_CHARACTER;
            return readingChar;
      }

   }

   /**
    * Read from the pushback cache first, then underlying reader
    */
   private int readLikePushbackReader() throws IOException
   {
      if(pushbackCache.length() > 0)
      {
         int readingChar = pushbackCache.charAt(0);
         pushbackCache.deleteCharAt(0);
         return readingChar;
      }
      return read();
   }

   /**
    * Advance in comment block until we reach a comment block closing tag
    */
   private void advanceToEscapeCommentBlock() throws IOException
   {
      if(cursorState != SkipCommentReader.State.ENCOUNTING_COMMENT_BLOCK_OPENING_TAG)
      {
         throw new IllegalStateException("This method should be invoked only if we are entering a comment block");
      }

      int readingChar = read();
      StringBuilder commentBlock = new StringBuilder("/*");

      LOOP:
      while(readingChar != EOF)
      {
         commentBlock.append((char)readingChar);
         if(readingChar == '/')
         {
            if(this.cursorState == SkipCommentReader.State.ENCOUNTING_ASTERIK)
            {
               this.cursorState = SkipCommentReader.State.ENCOUNTING_COMMENT_BLOCK_CLOSING_TAG;
               break LOOP; //We 've just escaped the comment block
            }
            else
            {
               this.cursorState = SkipCommentReader.State.ENCOUNTING_FORWARD_SLASH;
            }
         }
         else
         {
            this.cursorState = (readingChar == '*')? SkipCommentReader.State.ENCOUNTING_ASTERIK : SkipCommentReader.State.ENCOUNTING_ORDINARY_CHARACTER;
         }
         readingChar = read();
      }

      if(commentBlockHandler != null)
      {
         commentBlockHandler.handle(commentBlock, this);
      }
   }

   @Override
   public String readLine() throws IOException
   {
      StringBuilder builder = new StringBuilder();
      int nextChar = readSingleCharacter();
      if(nextChar == EOF)
      {
         return null;
      }

      while(nextChar != EOF)
      {
         if(nextChar == '\n' || nextChar == '\r')
         {
            break;
         }
         builder.append((char)nextChar);
         nextChar = readSingleCharacter();
      }

      return builder.toString().trim();
   }

   /**
    * Used for JUnit tests
    * @return
    */
   public State getCursorState()
   {
      return this.cursorState;
   }

   public void setCommentBlockHandler(CommentBlockHandler commentBlockHandler)
   {
      this.commentBlockHandler = commentBlockHandler;
   }

   public void setNumberOfCommingEscapes(int numberOfCommingEscapes)
   {
      this.numberOfCommingEscapes = numberOfCommingEscapes;
   }

   public void pushback(CharSequence sequence)
   {
      this.pushbackCache.append(sequence);
   }

   public enum State
   {
      ENCOUNTING_FORWARD_SLASH,

      ENCOUNTING_ASTERIK,

      ENCOUNTING_COMMENT_BLOCK_OPENING_TAG,

      ENCOUNTING_COMMENT_BLOCK_CLOSING_TAG,

      ENCOUNTING_ORDINARY_CHARACTER
   }
}
