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
import org.gatein.mop.api.workspace.Navigation;
import org.gatein.mop.api.workspace.ObjectType;
import org.gatein.mop.api.workspace.Site;
import org.gatein.mop.api.workspace.Workspace;

import java.util.Collection;

import static org.exoplatform.portal.mop.navigation.Utils.objectType;

/**
 * todo : see if it makes sense to use a bloom filter for not found site black list
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
abstract class DataCache
{

   protected abstract void removeNodes(Collection<String> keys);

   protected abstract NodeData getNode(POMSession session, String key);

   protected abstract NavigationData getNavigation(POMSession session, SiteKey key);

   protected abstract void removeNavigation(SiteKey key);

   protected abstract void clear();

   final NodeData getNodeData(POMSession session, String nodeId)
   {
      NodeData data;
      if (session.isModified())
      {
         data = loadNode(session, nodeId);
      }
      else
      {
         data = getNode(session, nodeId);
      }
      return data;
   }

   final NavigationData getNavigationData(POMSession session, SiteKey key)
   {
      NavigationData data;
      if (session.isModified())
      {
         data = loadNavigation(session, key);
      }
      else
      {
         data = getNavigation(session, key);
      }

      //
      return data;
   }

   final void removeNodeData(POMSession session, Collection<String> ids)
   {
      removeNodes(ids);
   }

   final void removeNavigationData(POMSession session, SiteKey key)
   {
      removeNavigation(key);
   }

   protected final NodeData loadNode(POMSession session, String nodeId)
   {
      Navigation navigation = session.findObjectById(ObjectType.NAVIGATION, nodeId);
      if (navigation != null)
      {
         return new NodeData(navigation);
      }
      else
      {
         return null;
      }
   }


   protected final  NavigationData loadNavigation(POMSession session, SiteKey key)
   {
      Workspace workspace = session.getWorkspace();
      ObjectType<Site> objectType = objectType(key.getType());
      Site site = workspace.getSite(objectType, key.getName());
      if (site != null)
      {
         Navigation defaultNavigation = site.getRootNavigation().getChild("default");
         if (defaultNavigation != null)
         {
            return new NavigationData(key, defaultNavigation);
         }
         else
         {
            return NavigationData.EMPTY;
         }
      }
      else
      {
         return NavigationData.EMPTY;
      }
   }
}
