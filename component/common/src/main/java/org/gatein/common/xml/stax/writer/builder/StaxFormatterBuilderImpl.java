/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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

package org.gatein.common.xml.stax.writer.builder;


import org.gatein.common.xml.stax.writer.formatting.SimpleFormatter;
import org.gatein.common.xml.stax.writer.formatting.XmlStreamingFormatter;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class StaxFormatterBuilderImpl implements StaxFormatterBuilder
{
   private Character indentCharacter;
   private Integer indentSize;
   private String newline;

   public StaxFormatterBuilder withIndentCharacter(char indentCharacter)
   {
      this.indentCharacter = indentCharacter;
      return this;
   }

   public StaxFormatterBuilder ofIndentSize(int indentSize)
   {
      this.indentSize = indentSize;
      return this;
   }

   public StaxFormatterBuilder withNewline(String newline)
   {
      this.newline = newline;
      return this;
   }

   public XmlStreamingFormatter build()
   {
      if (indentCharacter == null) throw new IllegalStateException("indent character is required value for this builder.");
      if (indentSize == null) throw new IllegalArgumentException("indent size is a required value for this builder.");
      if (newline == null) throw new IllegalArgumentException("newline is a required value for this builder.");

      return new SimpleFormatter(indentCharacter, indentSize, newline);
   }
}
