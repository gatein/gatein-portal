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

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.web.AbstractFilter;

import java.io.IOException;
import java.util.regex.Pattern;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * This filter allows the rest of the platform to add their own filters without changing the web.xml
 * file. It is based on {@link ExtensibleFilter} which is a component that supports plugin.
 * 
 * Created by The eXo Platform SAS
 * Author : Nicolas Filotto 
 *          nicolas.filotto@exoplatform.com
 * 25 sept. 2009  
 */
public class GenericFilter extends AbstractFilter
{

   private Pattern contextPathPattern;
   /**
    * @see javax.servlet.Filter#destroy()
    */
   public void destroy()
   {
   }
   
   @Override
   protected void afterInit(FilterConfig config) throws ServletException
   {
      ServletContext servletContext = this.getServletContext();
      contextPathPattern = Pattern.compile("[/]*" + servletContext.getContextPath() + "[/]*");
   }

   /**
    * This filter calls <code>doFilter</code> of the {@link ExtensibleFilter} of 
    * the current eXo container if it cans be found otherwise it releases filter
    */
   public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
      ServletException
   {
      ExoContainer container = getContainer();
      ExtensibleFilter filter = (ExtensibleFilter)container.getComponentInstanceOfType(ExtensibleFilter.class);
      if (filter == null)
      {
         chain.doFilter(request, response);
      }
      else
      {
         String path = contextPathPattern.matcher(((HttpServletRequest)request).getRequestURI()).replaceFirst("/");
         filter.doFilter(request, response, chain, path);
      }
   }
}
