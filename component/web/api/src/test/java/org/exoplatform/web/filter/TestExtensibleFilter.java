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

package org.exoplatform.web.filter;

import org.exoplatform.component.test.AbstractGateInTest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Created by The eXo Platform SAS
 * Author : Nicolas Filotto 
 *          nicolas.filotto@exoplatform.com
 * 25 sept. 2009  
 */
public class TestExtensibleFilter extends AbstractGateInTest
{

   public void testDoFilter() throws IOException, ServletException
   {
      String pathRequest = "/testPath";
      ExtensibleFilter exFilter = new ExtensibleFilter();
      MockFilterOKTF mockFilterOKTF = new MockFilterOKTF();
      MockFilterOKWTF mockFilterOKWTF = new MockFilterOKWTF();
      MockFilterChain chain = new MockFilterChain();
      exFilter.addFilterDefinitions(Arrays.asList(getFilterDefinition(mockFilterOKTF),
         getFilterDefinition(mockFilterOKWTF)));
      exFilter.doFilter(new MockServletRequest(), null, chain, pathRequest);
      assertTrue(mockFilterOKTF.start);
      assertTrue(mockFilterOKTF.end);
      assertTrue(mockFilterOKWTF.start);
      assertTrue(mockFilterOKWTF.end);
      assertTrue(chain.called);
      exFilter = new ExtensibleFilter();
      mockFilterOKTF = new MockFilterOKTF();
      mockFilterOKWTF = new MockFilterOKWTF();
      chain = new MockFilterChain();
      exFilter.addFilterDefinitions(Arrays.asList(getFilterDefinition(mockFilterOKTF),
         getFilterDefinition(mockFilterOKWTF), getFilterDefinition(new MockFilterKO())));
      exFilter.doFilter(new MockServletRequest(), null, chain, pathRequest);
      assertTrue(mockFilterOKTF.start);
      assertTrue(mockFilterOKTF.end);
      assertTrue(mockFilterOKWTF.start);
      assertTrue(mockFilterOKWTF.end);
      assertFalse(chain.called);
      exFilter = new ExtensibleFilter();
      mockFilterOKTF = new MockFilterOKTF();
      mockFilterOKWTF = new MockFilterOKWTF();
      chain = new MockFilterChain();
      exFilter.addFilterDefinitions(Arrays.asList(getFilterDefinition(mockFilterOKTF),
         getFilterDefinition(mockFilterOKWTF), getFilterDefinition(new MockFilterKOIO())));
      try
      {
         exFilter.doFilter(new MockServletRequest(), null, chain, pathRequest);
         fail("IOException is expected");
      }
      catch (IOException e)
      {
      }
      assertTrue(mockFilterOKTF.start);
      assertTrue(mockFilterOKTF.end);
      assertTrue(mockFilterOKWTF.start);
      assertFalse(mockFilterOKWTF.end);
      assertFalse(chain.called);
      exFilter = new ExtensibleFilter();
      mockFilterOKTF = new MockFilterOKTF();
      mockFilterOKWTF = new MockFilterOKWTF();
      chain = new MockFilterChain();
      exFilter.addFilterDefinitions(Arrays.asList(getFilterDefinition(mockFilterOKTF),
         getFilterDefinition(mockFilterOKWTF), getFilterDefinition(new MockFilterKOSE())));
      try
      {
         exFilter.doFilter(new MockServletRequest(), null, chain, pathRequest);
         fail("ServletException is expected");
      }
      catch (ServletException e)
      {
      }
      assertTrue(mockFilterOKTF.start);
      assertTrue(mockFilterOKTF.end);
      assertTrue(mockFilterOKWTF.start);
      assertFalse(mockFilterOKWTF.end);
      assertFalse(chain.called);
      exFilter = new ExtensibleFilter();
      mockFilterOKTF = new MockFilterOKTF();
      mockFilterOKWTF = new MockFilterOKWTF();
      chain = new MockFilterChain();
      exFilter.addFilterDefinitions(Arrays.asList(getFilterDefinition(mockFilterOKTF),
         getFilterDefinition(mockFilterOKWTF), getFilterDefinition(new MockFilterKORE())));
      try
      {
         exFilter.doFilter(new MockServletRequest(), null, chain, pathRequest);
         fail("RuntimeException is expected");
      }
      catch (RuntimeException e)
      {
      }
      assertTrue(mockFilterOKTF.start);
      assertTrue(mockFilterOKTF.end);
      assertTrue(mockFilterOKWTF.start);
      assertFalse(mockFilterOKWTF.end);
      assertFalse(chain.called);
      exFilter = new ExtensibleFilter();
      mockFilterOKTF = new MockFilterOKTF();
      mockFilterOKWTF = new MockFilterOKWTF();
      chain = new MockFilterChain();
      exFilter.addFilterDefinitions(Arrays.asList(getFilterDefinition(mockFilterOKTF),
         getFilterDefinition(mockFilterOKWTF), getFilterDefinition(new MockFilterKOER())));
      try
      {
         exFilter.doFilter(new MockServletRequest(), null, chain, pathRequest);
         fail("Error is expected");
      }
      catch (Error e)
      {
      }
      assertTrue(mockFilterOKTF.start);
      assertTrue(mockFilterOKTF.end);
      assertTrue(mockFilterOKWTF.start);
      assertFalse(mockFilterOKWTF.end);
      assertFalse(chain.called);
      exFilter = new ExtensibleFilter();
      mockFilterOKTF = new MockFilterOKTF();
      mockFilterOKWTF = new MockFilterOKWTF();
      MockFilterOKTF mockFilterOKTF2 = new MockFilterOKTF();
      chain = new MockFilterChain();
      exFilter.addFilterDefinitions(Arrays.asList(getFilterDefinition(mockFilterOKTF),
         getFilterDefinition(mockFilterOKWTF), getFilterDefinition(new MockFilterKOIO()),
         getFilterDefinition(mockFilterOKTF2)));
      try
      {
         exFilter.doFilter(new MockServletRequest(), null, chain, pathRequest);
         fail("IOException is expected");
      }
      catch (IOException e)
      {
      }
      assertTrue(mockFilterOKTF.start);
      assertTrue(mockFilterOKTF.end);
      assertTrue(mockFilterOKWTF.start);
      assertFalse(mockFilterOKWTF.end);
      assertFalse(chain.called);
      assertFalse(mockFilterOKTF2.start);
      assertFalse(mockFilterOKTF2.end);
   }

   private FilterDefinition getFilterDefinition(Filter filter)
   {
      return new FilterDefinition(filter, Collections.singletonList(".*"));
   }

   private static class MockFilterChain implements FilterChain
   {
      private boolean called;

      public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException
      {
         called = true;
      }
   }

   private static class MockFilterOKTF implements Filter
   {

      private boolean start;

      private boolean end;

      public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
         ServletException
      {
         try
         {
            start = true;
            chain.doFilter(request, response);
         }
         finally
         {
            end = true;
         }
      }
   }

   private static class MockFilterOKWTF implements Filter
   {

      private boolean start;

      private boolean end;

      public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
         ServletException
      {
         start = true;
         chain.doFilter(request, response);
         end = true;
      }
   }

   private static class MockFilterKO implements Filter
   {
      public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
         ServletException
      {
      }
   }

   private static class MockFilterKOIO implements Filter
   {

      public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
         ServletException
      {
         throw new IOException();
      }
   }

   private static class MockFilterKOSE implements Filter
   {

      public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
         ServletException
      {
         throw new ServletException();
      }
   }

   private static class MockFilterKORE implements Filter
   {

      public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
         ServletException
      {
         throw new RuntimeException();
      }
   }

   private static class MockFilterKOER implements Filter
   {

      public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
         ServletException
      {
         throw new Error();
      }
   }

   @SuppressWarnings("unchecked")
   private static class MockServletRequest implements HttpServletRequest
   {

      public String getAuthType()
      {

         return null;
      }

      public String getContextPath()
      {

         return null;
      }

      public Cookie[] getCookies()
      {

         return null;
      }

      public long getDateHeader(String name)
      {

         return 0;
      }

      public String getHeader(String name)
      {

         return null;
      }

      public Enumeration getHeaderNames()
      {

         return null;
      }

      public Enumeration getHeaders(String name)
      {

         return null;
      }

      public int getIntHeader(String name)
      {

         return 0;
      }

      public String getMethod()
      {

         return null;
      }

      public String getPathInfo()
      {
         return null;
      }

      public String getPathTranslated()
      {

         return null;
      }

      public String getQueryString()
      {

         return null;
      }

      public String getRemoteUser()
      {

         return null;
      }

      public String getRequestURI()
      {

         return "/";
      }

      public StringBuffer getRequestURL()
      {

         return null;
      }

      public String getRequestedSessionId()
      {

         return null;
      }

      public String getServletPath()
      {

         return null;
      }

      public HttpSession getSession()
      {

         return null;
      }

      public HttpSession getSession(boolean create)
      {

         return null;
      }

      public Principal getUserPrincipal()
      {

         return null;
      }

      public boolean isRequestedSessionIdFromCookie()
      {

         return false;
      }

      public boolean isRequestedSessionIdFromURL()
      {

         return false;
      }

      public boolean isRequestedSessionIdFromUrl()
      {

         return false;
      }

      public boolean isRequestedSessionIdValid()
      {

         return false;
      }

      public boolean isUserInRole(String role)
      {

         return false;
      }

      public Object getAttribute(String name)
      {

         return null;
      }

      public Enumeration getAttributeNames()
      {

         return null;
      }

      public String getCharacterEncoding()
      {

         return null;
      }

      public int getContentLength()
      {

         return 0;
      }

      public String getContentType()
      {

         return null;
      }

      public ServletInputStream getInputStream() throws IOException
      {

         return null;
      }

      public String getLocalAddr()
      {

         return null;
      }

      public String getLocalName()
      {

         return null;
      }

      public int getLocalPort()
      {

         return 0;
      }

      public Locale getLocale()
      {

         return null;
      }

      public Enumeration getLocales()
      {

         return null;
      }

      public String getParameter(String name)
      {

         return null;
      }

      public Map getParameterMap()
      {

         return null;
      }

      public Enumeration getParameterNames()
      {

         return null;
      }

      public String[] getParameterValues(String name)
      {

         return null;
      }

      public String getProtocol()
      {

         return null;
      }

      public BufferedReader getReader() throws IOException
      {

         return null;
      }

      public String getRealPath(String path)
      {

         return null;
      }

      public String getRemoteAddr()
      {

         return null;
      }

      public String getRemoteHost()
      {

         return null;
      }

      public int getRemotePort()
      {

         return 0;
      }

      public RequestDispatcher getRequestDispatcher(String path)
      {

         return null;
      }

      public String getScheme()
      {

         return null;
      }

      public String getServerName()
      {

         return null;
      }

      public int getServerPort()
      {

         return 0;
      }

      public boolean isSecure()
      {

         return false;
      }

      public void removeAttribute(String name)
      {

      }

      public void setAttribute(String name, Object o)
      {

      }

      public void setCharacterEncoding(String env) throws UnsupportedEncodingException
      {

      }

   }
}
