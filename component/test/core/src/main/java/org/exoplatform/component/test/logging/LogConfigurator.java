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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.URL;
import java.util.Map;
import java.util.Properties;
import java.util.logging.LogManager;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>A logger configuration for the the java logging system. It attempts to load the logging configuration from
 * the classpath under the name <i>gatein-logging.properties</i>. If the configuration cannot happen for some reason
 * (the file cannot be loaded for instance) then an exception is thrown that will be logged by the java logging
 * system and configuration will happen by other means defined by the java logging system (which means RTFM).</p>
 *
 * <p>When the property file is loaded, string interpolation happens on the values using the format <i>${}</i>. When
 * interpolation occurs it happens with the system properties. This is useful to configure the output directory
 * of the {@link java.util.logging.FileHandler} class with the maven target directory.</p>
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class LogConfigurator
{

   /** The interpolation pattern. */
   private static final Pattern INTERPOLATION_PATTERN = Pattern.compile("\\$\\{([^}]+)\\}");

   public LogConfigurator() throws Exception
   {
      LogManager manager = LogManager.getLogManager();

      //
      boolean configured = false;
      URL url = Thread.currentThread().getContextClassLoader().getResource("gatein-logging.properties");
      if (url != null)
      {
         try
         {
            InputStream in = url.openStream();

            //
            Properties props = new Properties();
            props.load(in);

            //
            for (Map.Entry<?, ?> entry : props.entrySet())
            {
               // A necessity here...
               Map.Entry<String, String> entry2 = (Map.Entry<String, String>)entry;

               //
               String value = entry2.getValue();
               Matcher matcher = INTERPOLATION_PATTERN.matcher(value);
               StringBuffer builder = new StringBuffer();
               while (matcher.find())
               {
                  String matched = matcher.group(1);
                  String repl = System.getProperty(matched);
                  if (repl == null)
                  {
                     repl = matched;
                  }
                  matcher.appendReplacement(builder, repl);
               }
               matcher.appendTail(builder);
               entry2.setValue(builder.toString());
            }

            //
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            props.store(baos, null);
            baos.close();
            in = new ByteArrayInputStream(baos.toByteArray());

            //
            manager.readConfiguration(in);
            configured = true;
         }
         catch (Throwable t)
         {
            throw new UndeclaredThrowableException(t, "Could not configure logging " +
               "from gatein-logging.properties file");
         }
      }

      // In case something bad happened we reconfigure with default
      if (!configured)
      {
         throw new Exception("Could not configure logging " +
            "from gatein-logging.properties file");
      }
   }
}
