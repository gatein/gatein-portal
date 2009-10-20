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

package org.exoplatform.webui.core.lifecycle;

import org.exoplatform.services.common.util.Stack;
import org.exoplatform.services.html.tidy.HTMLTidy;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

/**
 * Created by The eXo Platform SAS
 * May 10, 2007  
 */
public class HtmlValidator extends Writer
{

   public static final boolean DEBUG_MODE = false;

   private Writer finalWriter_;

   private HTMLTidy tidy_;

   private StringBuilder content_;

   private Stack<Integer> queue;

   public HtmlValidator(Writer w)
   {
      finalWriter_ = w;
      tidy_ = new HTMLTidy();
      content_ = new StringBuilder();
      queue = new Stack<Integer>();
   }

   public void close() throws IOException
   {
      finalWriter_.close();
   }

   public void flush() throws IOException
   {
      finalWriter_.flush();
   }

   public void startComponent()
   {
      queue.push(content_.length());
   }

   @Override
   public void write(char[] buf, int offset, int len) throws IOException
   {
      finalWriter_.write(buf, offset, len);
      content_.append(buf, offset, len);
   }

   public void endComponent() throws Exception
   {
      Integer start = queue.pop();
      String chunk = content_.substring(start);
      content_.delete(start, content_.length() - 1);

      List<String> messages = tidy_.check(chunk.toCharArray());
      if (messages.size() < 1)
         return;
      StringBuilder builder = new StringBuilder();

      for (String ele : messages)
      {
         builder.append(ele).append('\n');
      }
      throw new Exception(builder.toString());
   }

}
