/*
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

package org.exoplatform.component.test.logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.LogRecord;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public enum PatternElementType
{

   LEVEL('l') {
   @Override
   Object get(LogRecord record)
   {
      return record.getLevel().toString();
   }},

   MESSAGE('m') {
   @Override
   Object get(LogRecord record)
   {
      return record.getMessage();
   }},

   THREAD_NAME('t') {
   @Override
   Object get(LogRecord record)
   {
      return record.getThreadID();
   }},

   DATE('d') {
   @Override
   Object get(LogRecord record)
   {
      return record.getMillis();
   }},

   LOGGER_NAME('n') {
   @Override
   Object get(LogRecord record)
   {
      String name = record.getLoggerName();
      return name.substring(name.lastIndexOf('.') + 1);
   }},

   LOGGER_FQN('N') {
   @Override
   Object get(LogRecord record)
   {
      return record.getLoggerName();
   }},

   CLASS_NAME('c') {
   @Override
   Object get(LogRecord record)
   {
      String sourceClassName = record.getSourceClassName();
      return sourceClassName.substring(sourceClassName.lastIndexOf('.') + 1);
   }},

   CLASS_FQN('C') {
   @Override
   Object get(LogRecord record)
   {
      return record.getSourceClassName();
   }},

   METHOD_NAME('m') {
   @Override
   Object get(LogRecord record)
   {
      return record.getSourceMethodName();
   }},

   THROWABLE_MESSAGE('t') {
   @Override
   Object get(LogRecord record)
   {
      final Throwable t = record.getThrown();
      return (t == null) ? "" : t.getMessage();
   }},

   THROWABLE_TRACE('T') {
      @Override
      Object get(LogRecord record)
      {
         String o = "";
         final Throwable t = record.getThrown();
         if (t != null)
         {
            StringWriter buffer = new StringWriter();
            PrintWriter writer = new PrintWriter(buffer);
            t.printStackTrace(writer);
            writer.close();
            o = buffer.toString();
         }
         return o;
      }};

   /** . */
   private static final PatternElementType[] all = PatternElementType.values();

   /** . */
   public static final String LINE_SEPARATOR = System.getProperty("line.separator");

   /** . */
   final char blah;

   PatternElementType(char blah)
   {
      this.blah = blah;
   }

   abstract Object get(LogRecord record);

   static Object[] getRecordValue(LogRecord record)
   {
      Object[] objs = new Object[all.length];
      for (int i = 0;i < all.length;i++)
      {
         PatternElementType eltType = all[i];
         objs[i] = eltType.get(record);
      }
      return objs;

   }

   static String computeMPFPattern(String format)
   {
      for (int i = 0;i < all.length;i++)
      {
         PatternElementType elt = all[i];
         format = format.replaceAll("%" + elt.blah, "" + i);
      }
      return format;
   }
}
