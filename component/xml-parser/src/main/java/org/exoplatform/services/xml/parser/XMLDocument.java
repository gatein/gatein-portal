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

package org.exoplatform.services.xml.parser;

/**
 * Created by eXoPlatform Studio
 * Author : Nhu Dinh Thuan
 *          nhudinhthuan@yahoo.com
 * Mar 13, 2006
 */
public class XMLDocument
{

   private XMLNode root;

   private XMLNode xmlType;

   private boolean encode = false;

   public XMLDocument(XMLNode root)
   {
      this.root = root;
   }

   public XMLNode getRoot()
   {
      return root;
   }

   public XMLNode getXmlType()
   {
      return xmlType;
   }

   public void setXmlType(XMLNode xmlType)
   {
      this.xmlType = xmlType;
   }

   public String getTextValue()
   {
      StringBuilder builder = new StringBuilder();
      if (xmlType != null)
      {
         builder.append('<').append(xmlType.getNodeValue()).append(">\n");
      }
      else
      {
         builder.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
      }
      root.buildValue(builder, encode);
      return builder.toString();
   }

   public boolean isEncode()
   {
      return encode;
   }

   public void setEncode(boolean encode)
   {
      this.encode = encode;
   }
}
