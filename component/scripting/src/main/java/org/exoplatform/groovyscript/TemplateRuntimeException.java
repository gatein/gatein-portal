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
package org.exoplatform.groovyscript;

/**
 * A *checked* exception that denotes a Groovy runtime exception.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TemplateRuntimeException extends Exception
{

   /** . */
   private final String templateId;

   /** . */
   private final TextItem textItem;

   public TemplateRuntimeException(String templateId, TextItem textItem, String message, Throwable cause)
   {
      super(message, cause);

      //
      this.templateId = templateId;
      this.textItem = textItem;
   }

   public TemplateRuntimeException(String templateId, TextItem textItem, Throwable cause)
   {
      super(cause);

      //
      this.templateId = templateId;
      this.textItem = textItem;
   }

   public TextItem getTextItem()
   {
      return textItem;
   }

   public Integer getLineNumber()
   {
      return textItem != null ? textItem.getPosition().getLine() : null;
   }

   public String getText()
   {
      return textItem != null ? textItem.getData() : null;
   }

   @Override
   public String getMessage()
   {
      StringBuilder sb = new StringBuilder("Groovy template exception");
      if (textItem != null)
      {
         sb.append(" at ").append(textItem);
      }
      if (templateId != null)
      {
         sb.append(" for template ").append(templateId);
      }
      return sb.toString();
   }
}
