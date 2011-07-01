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

package org.exoplatform.portal.config.model;

import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.pom.config.Utils;
import org.exoplatform.portal.pom.data.PortalData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * May 13, 2004
 * @author: Tuan Nguyen
 * @email:   tuan08@users.sourceforge.net
 * @version: $Id: PortalConfig.java,v 1.7 2004/08/06 03:02:29 tuan08 Exp $
 **/
public class PortalConfig extends ModelObject
{

   final public static String USER_TYPE = SiteType.USER.getName();

   final public static String GROUP_TYPE = SiteType.GROUP.getName();

   final public static String PORTAL_TYPE = SiteType.PORTAL.getName();

   final public static Container DEFAULT_LAYOUT = initDefaultLayout();

   private String name;

   /** Added for new POM . */
   private String type;

   private String locale;

   private String label;
   
   private String description;
   
   private String[] accessPermissions;

   private String editPermission;

   private Properties properties;

   private String skin;

   private Container portalLayout;

   private transient boolean modifiable;

   public PortalConfig()
   {
      this(PORTAL_TYPE);
   }

   public PortalConfig(String type)
   {
      this(type, null);
   }

   public PortalConfig(String type, String ownerId)
   {
      this(type, ownerId, null);
   }

   public PortalConfig(String type, String ownerId, String storageId)
   {
      super(storageId);

      //
      this.type = type;
      this.name = ownerId;
      this.portalLayout = new Container();
   }

   public PortalConfig(PortalData data)
   {
      super(data.getStorageId());

      //
      this.name = data.getName();
      this.type = data.getType();
      this.locale = data.getLocale();
      this.label = data.getLabel();
      this.description = data.getDescription();
      this.accessPermissions = data.getAccessPermissions().toArray(new String[data.getAccessPermissions().size()]);
      this.editPermission = data.getEditPermission();
      this.properties = new Properties(data.getProperties());
      this.skin = data.getSkin();
      this.portalLayout = new Container(data.getPortalLayout());
   }

   public String getType()
   {
      return type;
   }

   public void setType(String type)
   {
      this.type = type;
   }

   public String getName()
   {
      return name;
   }

   public void setName(String s)
   {
      name = s;
   }

   public String getLocale()
   {
      return locale;
   }

   public void setLocale(String s)
   {
      locale = s;
   }

   public String[] getAccessPermissions()
   {
      return accessPermissions;
   }

   public void setAccessPermissions(String[] s)
   {
      accessPermissions = s;
   }

   public String getEditPermission()
   {
      return editPermission;
   }

   public void setEditPermission(String editPermission)
   {
      this.editPermission = editPermission;
   }

   public String getSkin()
   {
      if (skin == null || skin.length() < 1)
         return "Default";
      return skin;
   }

   public void setSkin(String s)
   {
      skin = s;
   }

   public Container getPortalLayout()
   {
      return portalLayout;
   }

   public void setPortalLayout(Container container)
   {
      portalLayout = container;
   }

   public boolean isModifiable()
   {
      return modifiable;
   }

   public void setModifiable(boolean b)
   {
      modifiable = b;
   }

   public Properties getProperties()
   {
      return properties;
   }

   public void setProperties(Properties props)
   {
      properties = props;
   }

   public String getProperty(String name)
   {
      if (name == null)
         throw new NullPointerException();
      if (properties == null || !properties.containsKey(name))
         return null;
      return properties.get(name);
   }

   public String getProperty(String name, String defaultValue)
   {
      String value = getProperty(name);
      if (value != null)
         return value;
      return defaultValue;
   }

   public void setProperty(String name, String value)
   {
      if (name == null || properties == null)
         throw new NullPointerException();
      if (value == null)
         properties.remove(name);
      else
         properties.setProperty(name, value);
   }

   public void removeProperty(String name)
   {
      if (name == null || properties == null)
         throw new NullPointerException();
      properties.remove(name);
   }

   public String getSessionAlive()
   {
      return getProperty(PortalProperties.SESSION_ALIVE, PortalProperties.SESSION_ON_DEMAND);
   }

   public void setSessionAlive(String type)
   {
      setProperty(PortalProperties.SESSION_ALIVE, type);
   }

   public void setDescription(String description)
   {
      this.description = description;
   }

   public String getDescription()
   {
      return description;
   }

   public void setLabel(String label)
   {
      this.label = label;
   }

   public String getLabel()
   {
      return label;
   }

   static public class PortalConfigSet
   {
      private ArrayList<PortalConfig> portalConfigs;

      public ArrayList<PortalConfig> getPortalConfigs()
      {
         return portalConfigs;
      }

      public void setPortalConfigs(ArrayList<PortalConfig> list)
      {
         portalConfigs = list;
      }
   }

   @Override
   public String toString()
   {
      return "PortalConfig[name=" + name + ",type=" + type + "]";
   }

   /**
    * Retuns Container that contains only PageBody
    * @return
    */
   private static Container initDefaultLayout()
   {
      Container container = new Container();
      ArrayList<ModelObject> children = new ArrayList<ModelObject>();
      children.add(new PageBody());
      container.setChildren(children);
      return container;
   }

   public PortalData build()
   {
      List<String> accessPermissions = Utils.safeImmutableList(this.accessPermissions);
      Map<String, String> properties = Utils.safeImmutableMap(this.properties);
      return new PortalData(
         storageId,
         name,
         type,
         locale,
         label,
         description,
         accessPermissions,
         editPermission,
         properties,
         skin,
         portalLayout.build());
   }
}
