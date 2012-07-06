/*
 * Copyright (C) 2012 eXo Platform SAS.
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
package org.exoplatform.web.application;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.json.JSONArray;

/**
 * @author <a href="trongtt@gmail.com">Trong Tran</a>
 * @version $Revision$
 */
public class RequireJS
{
   Log log = ExoLogger.getLogger(RequireJS.class);
   
   Map<String, String> depends;
   
   Set<String> noAlias;
   
   StringBuilder scripts;
   
   public RequireJS()
   {
      depends = new HashMap<String, String>();
      noAlias = new HashSet<String>();
      scripts = new StringBuilder();
   }

   public RequireJS require(String moduleId, String alias)
   {
      if (alias != null && !alias.trim().isEmpty())
      {
         alias = alias.trim();
         if (depends.containsKey(alias))
         {
            if (!depends.get(alias).equals(moduleId))
            {
               log.warn("There is already an alias named as {}", alias);
            }
         }
         else
         {
            depends.put(alias, moduleId);
         }         
      }
      else if (!depends.values().contains(moduleId))
      {
         log.warn("Adding requirejs module {} without alias", moduleId);
         noAlias.add(moduleId);         
      }
      return this;
   }

   public RequireJS addScripts(String scripts)
   {
      this.scripts.append(scripts);
      return this;
   }

   @Override
   public String toString()
   {
      StringBuilder builder = new StringBuilder();
      if (depends.size() > 0 || noAlias.size() > 0)
      {
         builder.append("require(");
         List<String> tmp = new LinkedList<String>(depends.values());
         tmp.addAll(noAlias);
         builder.append(new JSONArray(tmp)).append(",");         
         builder.append("function(").append(StringUtils.join(depends.keySet(), ",")).append(") {").append(this.scripts).append("});");
      }
      else
      {
         builder.append(this.scripts);
      }
      return builder.toString();
   }
}
