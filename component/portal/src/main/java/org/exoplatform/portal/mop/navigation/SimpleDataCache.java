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

package org.exoplatform.portal.mop.navigation;

import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.pom.config.POMSession;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A simple implementation for unit testing purpose.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class SimpleDataCache extends DataCache
{

   /** . */
   protected Map<SiteKey, NavigationData> navigations;

   /** . */
   protected Map<String, NodeData> nodes;

   public SimpleDataCache()
   {
      this.navigations = new ConcurrentHashMap<SiteKey, NavigationData>();
      this.nodes = new ConcurrentHashMap<String, NodeData>();
   }

   @Override
   protected void removeNodes(Collection<String> keys)
   {
      nodes.keySet().removeAll(keys);
   }

   @Override
   protected NodeData getNode(POMSession session, String key)
   {
      NodeData node = nodes.get(key);
      if (node == null)
      {
         node = loadNode(session, key);
         if (node != null)
         {
            nodes.put(key, node);
         }
      }
      return node;
   }

   @Override
   protected void removeNavigation(SiteKey key)
   {
      navigations.remove(key);
   }

   @Override
   protected NavigationData getNavigation(POMSession session, SiteKey key)
   {
      NavigationData navigation = navigations.get(key);
      if (navigation == null)
      {
         navigation = loadNavigation(session, key);
         if (navigation != null)
         {
            navigations.put(key, navigation);
         }
      }
      return navigations.get(key);
   }

   @Override
   protected void clear()
   {
      navigations.clear();
      nodes.clear();
   }
}
