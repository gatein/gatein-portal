/*
 * JBoss, a division of Red Hat
 * Copyright 2012, Red Hat Middleware, LLC, and individual
 * contributors as indicated by the @authors tag. See the
 * copyright.txt in the distribution for a full listing of
 * individual contributors.
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

package org.gatein.common.classloader;

import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;

import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

/**
 * {@link ClassLoader}, which delegates work to list of delegates (Delegating classloaders), which are provided from constructor.
 * Order of delegates is important (First has biggest priority)
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class DelegatingClassLoader extends ClassLoader
{
   private final List<ClassLoader> delegates;
   private static final Logger log = LoggerFactory.getLogger(DelegatingClassLoader.class);

   public DelegatingClassLoader(ClassLoader... delegates)
   {
      super(Thread.currentThread().getContextClassLoader());

      if (delegates == null || delegates.length == 0)
      {
         throw new IllegalArgumentException("Some delegating classloaders needs to be provided");
      }

      this.delegates = Arrays.asList(delegates);
   }

   @Override
   public Class<?> loadClass(String name) throws ClassNotFoundException
   {
      Class cl;

      for (ClassLoader delegate : delegates)
      {
         try
         {
            cl = delegate.loadClass(name);
            if (cl != null)
            {
               return cl;
            }
         }
         catch (ClassNotFoundException ignore)
         {
         }

         if (log.isTraceEnabled())
         {
            log.trace("Class " + name + " not found with classloader: " + delegate + ". Trying other delegates");
         }
      }

      throw new ClassNotFoundException("Class " + name + " not found with any of delegates " + delegates);
   }

   @Override
   public InputStream getResourceAsStream(String name)
   {
      for (ClassLoader delegate : delegates)
      {
         InputStream is = delegate.getResourceAsStream(name);
         if (is != null)
         {
            return is;
         }

         if (log.isTraceEnabled())
         {
            log.trace("Resource " + name + " not found with classloader: " + delegate + ". Trying other delegates");
         }
      }

      return null;
   }

   @Override
   public URL getResource(String name)
   {
      for (ClassLoader delegate : delegates)
      {
         URL url = delegate.getResource(name);
         if (url != null)
         {
            return url;
         }

         if (log.isTraceEnabled())
         {
            log.trace("URL " + name + " not found with classloader: " + delegate + ". Trying other delegates");
         }
      }

      return null;
   }
}
