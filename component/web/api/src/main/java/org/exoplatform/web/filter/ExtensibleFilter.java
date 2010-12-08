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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * This class allows the rest of the platform to define new filters thanks to the external
 * plugins. 
 * 
 * Created by The eXo Platform SAS
 * Author : Nicolas Filotto 
 *          nicolas.filotto@exoplatform.com
 * 25 sept. 2009  
 */
public class ExtensibleFilter
{

   /**
    * List of all the sub filters
    */
   private volatile List<FilterDefinition> filters = Collections.unmodifiableList(new ArrayList<FilterDefinition>());

   /**
    * Adds new {@link FilterDefinition}
    */
   public void addFilterDefinitions(FilterDefinitionPlugin plugin)
   {
      addFilterDefinitions(plugin.getFilterDefinitions());
   }

   /**
    * Adds new {@link FilterDefinition}
    */
   void addFilterDefinitions(List<FilterDefinition> pluginFilters)
   {
      if (pluginFilters == null || pluginFilters.isEmpty())
      {
         // No filter to add
         return;
      }
      synchronized (this)
      {
         List<FilterDefinition> result = new ArrayList<FilterDefinition>(filters);
         result.addAll(pluginFilters);
         this.filters = Collections.unmodifiableList(result);
      }
   }

   public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain, String path)
      throws IOException, ServletException
   {
      ExtensibleFilterChain efChain = new ExtensibleFilterChain(chain, filters, path);
      efChain.doFilter(request, response);
   }
   
   private static class ExtensibleFilterChain implements FilterChain
   {

      private final FilterChain parentChain;

      private final Iterator<FilterDefinition> filters;
      
      private final String path;

      private ExtensibleFilterChain(FilterChain parentChain, List<FilterDefinition> filters, String path_)
      {
         this.parentChain = parentChain;
         this.filters = filters.iterator();
         this.path = path_;
      }

      public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException
      {
         while (filters.hasNext())
         {
            FilterDefinition filterDef = filters.next();
            if (filterDef.getMapping().match(path))
            {
               filterDef.getFilter().doFilter(request, response, this);
               return;
            }
         }
         parentChain.doFilter(request, response);
      }
   }
}
