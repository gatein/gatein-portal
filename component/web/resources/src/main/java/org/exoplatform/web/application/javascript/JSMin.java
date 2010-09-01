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

package org.exoplatform.web.application.javascript;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.PushbackReader;
import java.io.Reader;
import java.io.Writer;

public class JSMin
{
   private static final int EOF = -1;

   private PushbackReader in;

   private Writer out;

   private int theA;

   private int theB;

   public JSMin(Reader in, Writer out)
   {
      this.in = new PushbackReader(in);
      this.out = out;
   }

   /**
    * isAlphanum -- return true if the character is a letter, digit, underscore,
    * dollar sign, or non-ASCII character.
    */
   static boolean isAlphanum(int c)
   {
      return ((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || (c >= 'A' && c <= 'Z') || c == '_' || c == '$'
         || c == '\\' || c > 126);
   }

   /**
    * get -- return the next character from stdin. Watch out for lookahead. If
    * the character is a control character, translate it to a space or linefeed.
    */
   int get() throws IOException
   {
      int c = in.read();

      if (c >= ' ' || c == '\n' || c == EOF)
      {
         return c;
      }

      if (c == '\r')
      {
         return '\n';
      }

      return ' ';
   }

   /**
    * Get the next character without getting it.
    */
   int peek() throws IOException
   {
      int lookaheadChar = in.read();
      in.unread(lookaheadChar);
      return lookaheadChar;
   }

   /**
    * next -- get the next character, excluding comments. peek() is used to see
    * if a '/' is followed by a '/' or '*'.
    */
   int next() throws IOException, UnterminatedCommentException
   {
      int c = get();
      if (c == '/')
      {
         switch (peek())
         {
            case '/' :
               for (;;)
               {
                  c = get();
                  if (c <= '\n')
                  {
                     return c;
                  }
               }

            case '*' :
               get();
               for (;;)
               {
                  switch (get())
                  {
                     case '*' :
                        if (peek() == '/')
                        {
                           get();
                           return ' ';
                        }
                        break;
                     case EOF :
                        throw new UnterminatedCommentException();
                  }
               }

            default :
               return c;
         }

      }
      return c;
   }

   /**
    * action -- do something! What you do is determined by the argument: <br/>
    * 1. Output A. Copy B to A. Get the next B. <br/>
    * 2. Copy B to A. Get the next B. (Delete A). <br/>
    * 3. Get the next B. (Delete B). action treats a string as a single character. <br/>
    * Wow! action recognizes a regular expression if it is preceded by ( or , or =.
    */

   void action(int d) throws IOException, UnterminatedRegExpLiteralException, UnterminatedCommentException,
      UnterminatedStringLiteralException
   {
      switch (d)
      {
         case 1 :
            out.write(theA);
         case 2 :
            theA = theB;

            if (theA == '\'' || theA == '"')
            {
               for (;;)
               {
                  out.write(theA);
                  theA = get();
                  if (theA == theB)
                  {
                     break;
                  }
                  if (theA <= '\n')
                  {
                     throw new UnterminatedStringLiteralException();
                  }
                  if (theA == '\\')
                  {
                     out.write(theA);
                     theA = get();
                  }
               }
            }

         case 3 :
            theB = next();
            if (theB == '/'
               && (theA == '(' || theA == ',' || theA == '=' || theA == ':' || theA == '[' || theA == '!'
                  || theA == '&' || theA == '|' || theA == '?' || theA == '{' || theA == '}' || theA == ';' || theA == '\n'))
            {
               out.write(theA);
               out.write(theB);
               for (;;)
               {
                  theA = get();
                  if (theA == '/')
                  {
                     break;
                  }
                  else if (theA == '\\')
                  {
                     out.write(theA);
                     theA = get();
                  }
                  else if (theA <= '\n')
                  {
                     throw new UnterminatedRegExpLiteralException();
                  }
                  out.write(theA);
               }
               theB = next();
            }
      }
   }

   /**
    * jsmin -- Copy the input to the output, deleting the characters which are
    * insignificant to JavaScript. Comments will be removed. Tabs will be
    * replaced with spaces. Carriage returns will be replaced with linefeeds.
    * Most spaces and linefeeds will be removed.
    */
   public void jsmin() throws IOException, UnterminatedRegExpLiteralException, UnterminatedCommentException,
      UnterminatedStringLiteralException
   {
      theA = '\n';
      action(3);
      while (theA != EOF)
      {
         switch (theA)
         {
            case ' ' :
               if (isAlphanum(theB))
               {
                  action(1);
               }
               else
               {
                  action(2);
               }
               break;
            case '\n' :
               switch (theB)
               {
                  case '{' :
                  case '[' :
                  case '(' :
                  case '+' :
                  case '-' :
                     action(1);
                     break;
                  case ' ' :
                     action(3);
                     break;
                  default :
                     if (isAlphanum(theB))
                     {
                        action(1);
                     }
                     else
                     {
                        action(2);
                     }
               }
               break;
            default :
               switch (theB)
               {
                  case ' ' :
                     if (isAlphanum(theA))
                     {
                        action(1);
                        break;
                     }
                     action(3);
                     break;
                  case '\n' :
                     switch (theA)
                     {
                        case '}' :
                        case ']' :
                        case ')' :
                        case '+' :
                        case '-' :
                        case '"' :
                        case '\'' :
                           action(1);
                           break;
                        default :
                           if (isAlphanum(theA))
                           {
                              action(1);
                           }
                           else
                           {
                              action(3);
                           }
                     }
                     break;
                  default :
                     action(1);
                     break;
               }
         }
      }
      out.flush();
   }

   class UnterminatedCommentException extends Exception
   {
   }

   class UnterminatedStringLiteralException extends Exception
   {
   }

   class UnterminatedRegExpLiteralException extends Exception
   {
   }

   public static void main(String arg[])
   {
      try
      {
         JSMin jsmin = new JSMin(new FileReader(arg[0]), new PrintWriter(System.out));
         jsmin.jsmin();
      }
      catch (FileNotFoundException e)
      {
         e.printStackTrace();
      }
      catch (ArrayIndexOutOfBoundsException e)
      {
         e.printStackTrace();
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
      catch (UnterminatedRegExpLiteralException e)
      {
         e.printStackTrace();
      }
      catch (UnterminatedCommentException e)
      {
         e.printStackTrace();
      }
      catch (UnterminatedStringLiteralException e)
      {
         e.printStackTrace();
      }
   }

}
