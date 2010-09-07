/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.exoplatform.portal.application.localization;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

/**
 * This class is an alternative for {@link javax.servlet.http.HttpServletRequestWrapper}.
 * One reason for favoring it is to avoid situations when servlet container decides to replace
 * the delegate via {@link javax.servlet.ServletRequestWrapper#setRequest(javax.servlet.ServletRequest)}. 
 *
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class BaseHttpRequestWrapper implements HttpServletRequest
{

   private HttpServletRequest delegate;

   /**
    * Create new instance
    * @param request delegate {@link HttpServletRequest}
    */
   public BaseHttpRequestWrapper(HttpServletRequest request)
   {
      delegate = request;
   }

   /**
    * @see javax.servlet.http.HttpServletRequest#getAuthType()
    */
   public String getAuthType()
   {
      return delegate.getAuthType();
   }

   /**
    * @see javax.servlet.http.HttpServletRequest#getCookies()
    */
   public Cookie[] getCookies()
   {
      return delegate.getCookies();
   }

   /**
    * @see javax.servlet.http.HttpServletRequest#getDateHeader(String)
    */
   public long getDateHeader(String name)
   {
      return delegate.getDateHeader(name);
   }

   /**
    * @see javax.servlet.http.HttpServletRequest#getHeader(String)
    */
   public String getHeader(String name)
   {
      return delegate.getHeader(name);
   }

   /**
    * @see javax.servlet.http.HttpServletRequest#getHeaders(String)
    */
   public Enumeration getHeaders(String name)
   {
      return delegate.getHeaders(name);
   }

   /**
    * @see javax.servlet.http.HttpServletRequest#getHeaderNames()
    */
   public Enumeration getHeaderNames()
   {
      return delegate.getHeaderNames();
   }

   /**
    * @see javax.servlet.http.HttpServletRequest#getIntHeader(String)
    */
   public int getIntHeader(String name)
   {
      return delegate.getIntHeader(name);
   }

   /**
    * @see javax.servlet.http.HttpServletRequest#getMethod()
    */
   public String getMethod()
   {
      return delegate.getMethod();
   }

   /**
    * @see javax.servlet.http.HttpServletRequest#getPathInfo()
    */
   public String getPathInfo()
   {
      return delegate.getPathInfo();
   }

   /**
    * @see javax.servlet.http.HttpServletRequest#getPathTranslated()
    */
   public String getPathTranslated()
   {
      return delegate.getPathTranslated();
   }

   /**
    * @see javax.servlet.http.HttpServletRequest#getContextPath()
    */
   public String getContextPath()
   {
      return delegate.getContextPath();
   }

   /**
    * @see javax.servlet.http.HttpServletRequest#getQueryString()
    */
   public String getQueryString()
   {
      return delegate.getQueryString();
   }

   /**
    * @see javax.servlet.http.HttpServletRequest#getRemoteUser()
    */
   public String getRemoteUser()
   {
      return delegate.getRemoteUser();
   }

   /**
    * @see javax.servlet.http.HttpServletRequest#isUserInRole(String)
    */
   public boolean isUserInRole(String role)
   {
      return delegate.isUserInRole(role);
   }

   /**
    * @see javax.servlet.http.HttpServletRequest#getUserPrincipal()
    */
   public Principal getUserPrincipal()
   {
      return delegate.getUserPrincipal();
   }

   /**
    * @see javax.servlet.http.HttpServletRequest#getRequestedSessionId()
    */
   public String getRequestedSessionId()
   {
      return delegate.getRequestedSessionId();
   }

   /**
    * @see javax.servlet.http.HttpServletRequest#getRequestURI()
    */
   public String getRequestURI()
   {
      return delegate.getRequestURI();
   }

   /**
    * @see javax.servlet.http.HttpServletRequest#getRequestURL()
    */
   public StringBuffer getRequestURL()
   {
      return delegate.getRequestURL();
   }

   /**
    * @see javax.servlet.http.HttpServletRequest#getServletPath()
    */
   public String getServletPath()
   {
      return delegate.getServletPath();
   }

   /**
    * @see javax.servlet.http.HttpServletRequest#getSession(boolean)
    */
   public HttpSession getSession(boolean create)
   {
      return delegate.getSession(create);
   }

   /**
    * @see javax.servlet.http.HttpServletRequest#getSession()
    */
   public HttpSession getSession()
   {
      return delegate.getSession();
   }

   /**
    * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdValid()
    */
   public boolean isRequestedSessionIdValid()
   {
      return delegate.isRequestedSessionIdValid();
   }

   /**
    * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromCookie()
    */
   public boolean isRequestedSessionIdFromCookie()
   {
      return delegate.isRequestedSessionIdFromCookie();
   }

   /**
    * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromURL()
    */
   public boolean isRequestedSessionIdFromURL()
   {
      return delegate.isRequestedSessionIdFromURL();
   }

   /**
    * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromUrl()
    */
   public boolean isRequestedSessionIdFromUrl()
   {
      return delegate.isRequestedSessionIdFromUrl();
   }

   /**
    * @see javax.servlet.http.HttpServletRequest#getAttribute(String)
    */
   public Object getAttribute(String name)
   {
      return delegate.getAttribute(name);
   }

   /**
    * @see javax.servlet.http.HttpServletRequest#getAttributeNames()
    */
   public Enumeration getAttributeNames()
   {
      return delegate.getAttributeNames();
   }

   /**
    * @see javax.servlet.http.HttpServletRequest#getCharacterEncoding()
    */
   public String getCharacterEncoding()
   {
      return delegate.getCharacterEncoding();
   }

   /**
    * @see javax.servlet.http.HttpServletRequest#setCharacterEncoding(String)
    */
   public void setCharacterEncoding(String env) throws UnsupportedEncodingException
   {
      delegate.setCharacterEncoding(env);
   }

   /**
    * @see javax.servlet.http.HttpServletRequest#getContentLength()
    */
   public int getContentLength()
   {
      return delegate.getContentLength();
   }

   /**
    * @see javax.servlet.http.HttpServletRequest#getContentType()
    */
   public String getContentType()
   {
      return delegate.getContentType();
   }

   /**
    * @see javax.servlet.http.HttpServletRequest#getInputStream()
    */
   public ServletInputStream getInputStream() throws IOException
   {
      return delegate.getInputStream();
   }

   /**
    * @see javax.servlet.http.HttpServletRequest#getParameter(String)
    */
   public String getParameter(String name)
   {
      return delegate.getParameter(name);
   }

   /**
    * @see javax.servlet.http.HttpServletRequest#getParameterNames()
    */
   public Enumeration getParameterNames()
   {
      return delegate.getParameterNames();
   }

   /**
    * @see javax.servlet.http.HttpServletRequest#getParameterValues(String)
    */
   public String[] getParameterValues(String name)
   {
      return delegate.getParameterValues(name);
   }

   /**
    * @see javax.servlet.http.HttpServletRequest#getParameterMap()
    */
   public Map getParameterMap()
   {
      return delegate.getParameterMap();
   }

   /**
    * @see javax.servlet.http.HttpServletRequest#getProtocol()
    */
   public String getProtocol()
   {
      return delegate.getProtocol();
   }

   /**
    * @see javax.servlet.http.HttpServletRequest#getScheme()
    */
   public String getScheme()
   {
      return delegate.getScheme();
   }

   /**
    * @see javax.servlet.http.HttpServletRequest#getServerName()
    */
   public String getServerName()
   {
      return delegate.getServerName();
   }

   /**
    * @see javax.servlet.http.HttpServletRequest#getServerPort()
    */
   public int getServerPort()
   {
      return delegate.getServerPort();
   }

   /**
    * @see javax.servlet.http.HttpServletRequest#getReader()
    */
   public BufferedReader getReader() throws IOException
   {
      return delegate.getReader();
   }

   /**
    * @see javax.servlet.http.HttpServletRequest#getRemoteAddr()
    */
   public String getRemoteAddr()
   {
      return delegate.getRemoteAddr();
   }

   /**
    * @see javax.servlet.http.HttpServletRequest#getRemoteHost()
    */
   public String getRemoteHost()
   {
      return delegate.getRemoteHost();
   }

   /**
    * @see javax.servlet.http.HttpServletRequest#setAttribute(String, Object)
    */
   public void setAttribute(String name, Object o)
   {
      delegate.setAttribute(name, o);
   }

   /**
    * @see javax.servlet.http.HttpServletRequest#removeAttribute(String)
    */
   public void removeAttribute(String name)
   {
      delegate.removeAttribute(name);
   }

   /**
    * @see javax.servlet.http.HttpServletRequest#getLocale()
    */
   public Locale getLocale()
   {
      return delegate.getLocale();
   }

   /**
    * @see javax.servlet.http.HttpServletRequest#getLocales()
    */
   public Enumeration getLocales()
   {
      return delegate.getLocales();
   }

   /**
    * @see javax.servlet.http.HttpServletRequest#isSecure()
    */
   public boolean isSecure()
   {
      return delegate.isSecure();
   }

   /**
    * @see javax.servlet.http.HttpServletRequest#getRequestDispatcher(String)
    */
   public RequestDispatcher getRequestDispatcher(String path)
   {
      return delegate.getRequestDispatcher(path);
   }

   /**
    * @see javax.servlet.http.HttpServletRequest#getRealPath(String)
    */
   public String getRealPath(String path)
   {
      return delegate.getRealPath(path);
   }

   /**
    * @see javax.servlet.http.HttpServletRequest#getRemotePort()
    */
   public int getRemotePort()
   {
      return delegate.getRemotePort();
   }

   /**
    * @see javax.servlet.http.HttpServletRequest#getLocalName()
    */
   public String getLocalName()
   {
      return delegate.getLocalName();
   }

   /**
    * @see javax.servlet.http.HttpServletRequest#getLocalAddr()
    */
   public String getLocalAddr()
   {
      return delegate.getLocalAddr();
   }

   /**
    * @see javax.servlet.http.HttpServletRequest#getLocalPort()
    */
   public int getLocalPort()
   {
      return delegate.getLocalPort();
   }

   /**
    * Get the underlying request - the delegate
    */
   public HttpServletRequest getRequest()
   {
      return delegate;
   }
}
