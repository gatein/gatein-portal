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

import java.text.MessageFormat;
import java.util.logging.Formatter;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;

/**
 * <p>A more flexible formatter than what is provided by the Java runtime. This log formatter is used only for unit
 * tests as it is not optimized for performances nor robustness.</p>
 *
 * <p>The formatter is parameterized with a pattern based on the pattern defined by the {@link java.text.MessageFormat}
 * class replacing the format elements by pattern element types. Each pattern element type replaces an integer in the
 * pattern string. A pattern element type symbolize an information computed from a {@link LogRecord}
 * object when one needs to be formatted.</p>
 *
 * <p>The existing pattern element types are:
 * <ul>
 * <li>l: the level returned by {@link LogRecord#getLevel()}</li>
 * <li>m: the message returned by {@link LogRecord#getMessage()}</li>
 * <li>t: the thread id returned by {@link LogRecord#getThreadID()}</li>
 * <li>d: the number of milliseconds since 1970 returned by {@link LogRecord#getMillis()}</li>
 * <li>n: the logger name obtained as the simple name (in the sense of {@link Class#getSimpleName()}) of the value
 * returned by {@link LogRecord#getLoggerName()}</li>
 * <li>N: the logger name returned by {@link LogRecord#getLoggerName()}</li>
 * <li>c: the source class name obtained as the simple name (in the sense of {@link Class#getSimpleName()}) of the value
 * returned by {@link LogRecord#getSourceClassName()}</li>
 * <li>N: the source class name returned by {@link LogRecord#getSourceClassName()}</li>
 * <li>t: the throwable message name returned by {@link LogRecord#getThrown()} when it is not null or the empty string</li>
 * <li>T: the formatted throwable stack trace returned by {@link LogRecord#getThrown()} when it is not null or the empty string</li>
 * </ul>
 * </p>
 *
 * <p>The pattern is configurable by <i>pattern</p> property of the formatter otheriwse the default pattern {@link #DEFAULT_PATTERN}
 * is used.</p>.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class PatternFormatter extends Formatter
{

   /** . */
   public static final String DEFAULT_PATTERN = "[{%d,date,HH:mm:ss-SSS}][{%l}] {%n} {%m} {%T}";

   /** . */
   private final String mfPattern;

   public PatternFormatter()
   {
      LogManager manager = LogManager.getLogManager();
      String format = manager.getProperty(PatternFormatter.class.getName() + ".pattern");
      if (format == null)
      {
         format = DEFAULT_PATTERN;
      }
      else
      {
         format = format.trim();
      }
      mfPattern = PatternElementType.computeMPFPattern(format);
   }

   @Override
   public String format(LogRecord record)
   {
      MessageFormat mf = new MessageFormat(mfPattern);
      Object[] objs = PatternElementType.getRecordValue(record);
      return mf.format(objs) + PatternElementType.LINE_SEPARATOR;
   }
}
