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

package org.exoplatform.test;

/**
 * Created by The eXo Platform SARL
 * Author : Lai Van Khoi
 *          laivankhoi46pm1@yahoo.com
 * Nov 28, 2006  
 */
import org.exoplatform.services.html.HTMLNode;
import org.exoplatform.services.html.parser.HTMLParser;

import java.io.File;
import java.util.List;

public class TestParser extends BasicTestCase
{

   public static void print(String text, HTMLNode element)
   {
      List<HTMLNode> children = element.getChildren();
      for (HTMLNode node : children)
         System.out.print(text + " " + node);
   }

   public void testParser() throws Exception
   {
      HTMLNode node =
         HTMLParser.createDocument(new File("C:\\Documents and Settings\\exo\\Desktop\\130033.htm"), "utf-8").getRoot();
      //System.out.println(node.getTextValue());
      //print("", node);
      assertEquals(node.getTextValue(), node.getTextValue());
   }
}
