/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.groovyscript;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TextItem extends SectionItem
{

   /** . */
   private final String data;

   public TextItem(Position pos, String data)
   {
      super(pos);

      //
      if (data == null)
      {
         throw new NullPointerException();
      }

      //
      this.data = data;
   }

   public String getData()
   {
      return data;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (obj == this)
      {
         return true;
      }
      if (obj instanceof TextItem)
      {
         TextItem that = (TextItem)obj;
         return data.equals(that.data);
      }
      return false;
   }

   @Override
   public String toString()
   {
      return "DataText[pos=" + getPosition() + ",data=" + data + "]";
   }
}
