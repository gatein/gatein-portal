/*
 * Copyright (C) 2011 eXo Platform SAS.
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

package org.exoplatform.component.test.web;

import org.gatein.common.NotYetImplemented;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Enumeration;
import java.util.Set;

/**
 * URL based implementation. Disclaimer : does not work with jar URLs.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class ServletContextImpl implements ServletContext
{

   /** . */
   private final URL base;

   /** The path of this context. */
   private final String path;

   /** . */
   private final String name;

   public ServletContextImpl(File root, String path, String name) throws MalformedURLException
   {
      this(root.toURI().toURL(), path, name);
   }

   public ServletContextImpl(Class<?> root, String path, String name)
   {
      this(root.getResource(""), path, name);
   }

   public ServletContextImpl(URL base, String path, String name)
   {
      if (base == null)
      {
         throw new NullPointerException("No null base URL accepted");
      }
      if (path == null)
      {
         throw new NullPointerException("No null path accepted");
      }
      if (name == null)
      {
         throw new NullPointerException("No null name accepted");
      }

      //
      this.base = base;
      this.path = path;
      this.name = name;
   }

   public String getContextPath()
   {
      return path;
   }

   public URL getResource(String path) throws MalformedURLException
   {
      if (path.length() == 0 || path.charAt(0) != '/')
      {
         throw new MalformedURLException(path + "does not start with /");
      }
      try
      {
         URI relative = new URI(path.substring(1));
         URI uri = base.toURI().resolve(relative);
         if (new File(uri).exists())
         {
            return uri.toURL();
         }
         else
         {
            return null;
         }
      }
      catch (Exception e)
      {
         MalformedURLException ex = new MalformedURLException("Cannot build URL");
         ex.initCause(e);
         throw ex;
      }
   }

   public InputStream getResourceAsStream(String path)
   {
      try
      {
         URL url = getResource(path);
         if (url != null)
         {
            return url.openStream();
         }
      }
      catch (Exception ignore)
      {
      }
      return null;
   }

   public String getServletContextName()
   {
      return name;
   }

   public ServletContext getContext(String uripath)
   {
      throw new NotYetImplemented();
   }

   public int getMajorVersion()
   {
      throw new NotYetImplemented();
   }

   public int getMinorVersion()
   {
      throw new NotYetImplemented();
   }

   public String getMimeType(String file)
   {
      throw new NotYetImplemented();
   }

   public Set getResourcePaths(String path)
   {
      throw new NotYetImplemented();
   }

   public RequestDispatcher getRequestDispatcher(String path)
   {
      throw new NotYetImplemented();
   }

   public RequestDispatcher getNamedDispatcher(String name)
   {
      throw new NotYetImplemented();
   }

   public Servlet getServlet(String name) throws ServletException
   {
      throw new NotYetImplemented();
   }

   public Enumeration getServlets()
   {
      throw new NotYetImplemented();
   }

   public Enumeration getServletNames()
   {
      throw new NotYetImplemented();
   }

   public void log(String msg)
   {
   }

   public void log(Exception exception, String msg)
   {
   }

   public void log(String message, Throwable throwable)
   {
   }

   public String getRealPath(String path)
   {
      throw new NotYetImplemented();
   }

   public String getServerInfo()
   {
      throw new NotYetImplemented();
   }

   public String getInitParameter(String name)
   {
      throw new NotYetImplemented();
   }

   public Enumeration getInitParameterNames()
   {
      throw new NotYetImplemented();
   }

   public Object getAttribute(String name)
   {
      throw new NotYetImplemented();
   }

   public Enumeration getAttributeNames()
   {
      throw new NotYetImplemented();
   }

   public void setAttribute(String name, Object object)
   {
      throw new NotYetImplemented();
   }

   public void removeAttribute(String name)
   {
      throw new NotYetImplemented();
   }
}
