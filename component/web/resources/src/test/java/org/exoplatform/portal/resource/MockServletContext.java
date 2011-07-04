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
package org.exoplatform.portal.resource;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Set;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

/**
 * @author <a href="hoang281283@gmail.com">Minh Hoang TO</a>
 * @date 6/29/11
 */
public class MockServletContext implements ServletContext
{

   private String contextName;

   private ClassLoader loader;

   public MockServletContext(String contextName, ClassLoader loader)
   {
      this.contextName = contextName;
      this.loader = loader;
   }

   public String getContextPath()
   {
      return "/" + contextName;
   }

   public ServletContext getContext(String s)
   {
      return null;
   }

   public int getMajorVersion()
   {
      return 0;
   }

   public int getMinorVersion()
   {
      return 0;
   }

   public String getMimeType(String s)
   {
      return null;
   }

   public Set getResourcePaths(String s)
   {
      return null;
   }

   public URL getResource(String s) throws MalformedURLException
   {
      return loader.getResource(contextName + s);
   }

   public InputStream getResourceAsStream(String s)
   {
      return loader.getResourceAsStream(contextName + s);
   }

   public RequestDispatcher getRequestDispatcher(String s)
   {
      return null;
   }

   public RequestDispatcher getNamedDispatcher(String s)
   {
      return null;
   }

   public Servlet getServlet(String s) throws ServletException
   {
      return null;
   }

   public Enumeration getServlets()
   {
      return null;
   }

   public Enumeration getServletNames()
   {
      return null;
   }

   public void log(String s)
   {
   }

   public void log(Exception e, String s)
   {
   }

   public void log(String s, Throwable throwable)
   {
   }

   public String getRealPath(String s)
   {
      return null;
   }

   public String getServerInfo()
   {
      return null;
   }

   public String getInitParameter(String s)
   {
      return null;
   }

   public Enumeration getInitParameterNames()
   {
      return null;
   }

   public Object getAttribute(String s)
   {
      return null;
   }

   public Enumeration getAttributeNames()
   {
      return null;
   }

   public void setAttribute(String s, Object o)
   {
   }

   public void removeAttribute(String s)
   {
   }

   public String getServletContextName()
   {
      return contextName;
   }
}
