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

package org.exoplatform.services.html;

/**
 *  Author : Nhu Dinh Thuan
 *          Email:nhudinhthuan@yahoo.com
 * Aug 12, 2006
 */
public final class Group
{

   public static interface Default
   {
   }

   public static interface Flow
   {
   }

   public static interface Block extends Flow
   {
   }

   public static interface Fontstyle extends Inline
   {
   }

   public static interface Formctrl extends Inline
   {
   }

   public static interface Heading extends Block
   {
   }

   public static interface List extends Block
   {
   }

   public interface Phrase extends Inline
   {
   }

   public static interface Special extends Inline
   {
   }

   public static interface Inline extends Flow
   {
   }

   public static interface HtmlContent
   {
   }

   public static interface HeadContent
   {
   }

   public static interface HeadMisc
   {
   }

   public static interface Table
   {
   }

   public static interface Preformatted extends Block
   {
   }

}
