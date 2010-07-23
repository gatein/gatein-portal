/*
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
package org.exoplatform.portal.resource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:hoang281283@gmail.com">Minh Hoang TO</a>
 * @version $Id$
 * 
 */
public class SkinDependentManager
{

   private static Map<String, Set<String>> skinName_Apps_map = new HashMap<String, Set<String>>();

   private static Map<String, Set<String>> app_skinNames_map = new HashMap<String, Set<String>>();

   private static Map<String, List<SkinKey>> app_portalSkins_map = new HashMap<String, List<SkinKey>>();

   private static Map<String, List<SkinKey>> app_portletSkins_map = new HashMap<String, List<SkinKey>>();

   public static void addSkinDeployedInApp(String webApp, String skinName)
   {
      Set<String> skinsDefinedInApp = app_skinNames_map.get(webApp);
      if (skinsDefinedInApp == null)
      {
         skinsDefinedInApp = new HashSet<String>();
         skinsDefinedInApp.add(skinName);
         app_skinNames_map.put(webApp, skinsDefinedInApp);
         return;
      }
      skinsDefinedInApp.add(skinName);
   }

   public static void addDependentAppToSkinName(String skinName, String dependentApp)
   {
      Set<String> dependentApps = skinName_Apps_map.get(skinName);
      if (dependentApps == null)
      {
         dependentApps = new HashSet<String>();
         dependentApps.add(dependentApp);
         skinName_Apps_map.put(skinName, dependentApps);
         return;
      }
      dependentApps.add(dependentApp);
   }

   public static boolean skinNameIsRemovable(String skinName, String dependentApp)
   {
      Set<String> dependentApps = skinName_Apps_map.get(skinName);
      if (dependentApps == null)
      {
         return false;
      }
      // Remove the dependentApp defining 'skinName' skin
      dependentApps.remove(dependentApp);

      if (dependentApps.isEmpty())
      {
         skinName_Apps_map.remove(skinName);
         return true;
      }
      else
      {
         return false;
      }
   }

   public static void addPortalSkin(String webApp, String moduleName, String skinName)
   {
      addSkin(webApp, new SkinKey(moduleName, skinName), app_portalSkins_map);
   }

   public static void addPortletSkin(String webApp, String moduleName, String skinName)
   {
      addSkin(webApp, new SkinKey(moduleName, skinName), app_portletSkins_map);
   }

   public static List<SkinKey> getPortalSkins(String webApp)
   {
      return app_portalSkins_map.get(webApp);
   }

   public static List<SkinKey> getPortletSkins(String webApp)
   {
      return app_portletSkins_map.get(webApp);
   }

   public static Set<String> getSkinNames(String webApp)
   {
      return app_skinNames_map.get(webApp);
   }

   public static void clearAssociatedSkins(String webApp)
   {
      app_portalSkins_map.remove(webApp);
      app_portletSkins_map.remove(webApp);
   }

   public static void removeSkinName(String webApp, String skinName)
   {
      Set<String> skinsDefinedInApp = app_skinNames_map.get(webApp);
      if (skinsDefinedInApp == null)
      {
         return;
      }
      else
      {
         skinsDefinedInApp.remove(skinName);// TODO: Check the remove here
      }
   }

   private static void addSkin(String webApp, SkinKey key, Map<String, List<SkinKey>> map)
   {
      List<SkinKey> skinKeys = map.get(webApp);
      if (skinKeys == null)
      {
         skinKeys = new ArrayList<SkinKey>(5);
         skinKeys.add(key);
         map.put(webApp, skinKeys);
         return;
      }
      skinKeys.add(key);
   }
}
