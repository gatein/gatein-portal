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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class defined all the variables needed to define a {@link Filter}
 * 
 * Created by The eXo Platform SAS
 * Author : Nicolas Filotto 
 *          nicolas.filotto@exoplatform.com
 * 25 sept. 2009  
 */
public class FilterDefinition
{

   /**
    * The filter.
    */
   private Filter filter;

   /**
    * The filter mapping.
    */
   private volatile FilterMapping mapping;

   /**
    * The list of patterns that will defined the {@link FilterMapping}
    */
   private List<String> patterns;

   public FilterDefinition()
   {
   }

   public FilterDefinition(Filter filter, List<String> patterns)
   {
      this.filter = filter;
      this.patterns = patterns;
   }

   public Filter getFilter()
   {
      return filter;
   }

   public FilterMapping getMapping()
   {
      if (mapping == null)
      {
         synchronized (this)
         {
            if (mapping == null)
            {
               this.mapping = new PatternMapping(patterns);
               this.patterns = null;
            }
         }
      }
      return mapping;
   }

   /**
    * This class is used to defined a mapping based on a list of regular expression 
    */
   private static class PatternMapping implements FilterMapping
   {

      /**
       * the list of regular expressions
       */
      private final List<Pattern> patterns;

      private PatternMapping(List<String> strPatterns)
      {
         if (strPatterns == null || strPatterns.isEmpty())
         {
            throw new IllegalArgumentException("The list of patterns cannot be empty");
         }
         this.patterns = new ArrayList<Pattern>(strPatterns.size());
         for (String sPattern : strPatterns)
         {
            patterns.add(Pattern.compile(sPattern));
         }
      }

      /**
       * @return <code>true</code> if at least one pattern matches
       */
      public boolean match(String path)
      {
         for (int i = 0, length = patterns.size(); i < length; i++)
         {
            Pattern pattern = patterns.get(i);
            Matcher matcher = pattern.matcher(path);
            if (matcher.matches())
            {
               return true;
            }
         }
         return false;
      }

   }
}
