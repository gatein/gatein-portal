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

import org.exoplatform.portal.pom.config.Utils;
import org.exoplatform.portal.pom.data.ApplicationData;
import org.exoplatform.portal.pom.data.ModelData;
import org.exoplatform.portal.pom.spi.gadget.Gadget;
import org.exoplatform.portal.pom.spi.portlet.Portlet;
import org.exoplatform.portal.pom.spi.wsrp.WSRP;

/**
 * May 13, 2004
 * @author: Tuan Nguyen
 * @email:   tuan08@users.sourceforge.net
 * @version: $Id: Portlet.java,v 1.7 2004/09/30 01:00:05 tuan08 Exp $
 **/
public class Application<S> extends ModelObject
{

   /** The application state. */
   private ApplicationState<S> state;

   private String id;

   private String title;

   private String icon;

   private String description;

   private boolean showInfoBar;

   private boolean showApplicationState = true;

   private boolean showApplicationMode = true;

   private String theme;

   private String width;

   private String height;

   private Properties properties;

   private String[] accessPermissions;

   private boolean isModifiable;

   /** We cannot allow the type to change once the object is created. */
   private final ApplicationType<S> type;

   public Application(ApplicationData<S> data)
   {
      super(data.getStorageId());

      // For now here, need to make a real NAME and
      // remove disturbing storage name
      this.storageName = data.getStorageName();

      //
      this.state = data.getState();
      this.id = data.getId();
      this.title = data.getTitle();
      this.icon = data.getIcon();
      this.description = data.getDescription();
      this.showInfoBar = data.isShowInfoBar();
      this.showApplicationState = data.isShowApplicationState();
      this.showApplicationMode = data.isShowApplicationMode();
      this.theme = data.getTheme();
      this.width = data.getWidth();
      this.height = data.getHeight();
      this.properties = new Properties(data.getProperties());
      this.accessPermissions = data.getAccessPermissions().toArray(new String[data.getAccessPermissions().size()]);
      this.type = data.getType();
   }

   public Application(ApplicationType<S> type, String storageId)
   {
      super(storageId);

      //
      this.type = type;
   }

   public Application(ApplicationType<S> type)
   {
      super();

      //
      this.type = type;
   }

   public ApplicationType<S> getType()
   {
      return type;
   }

   public String getWidth()
   {
      return width;
   }

   public void setWidth(String s)
   {
      width = s;
   }

   public String getHeight()
   {
      return height;
   }

   public void setHeight(String s)
   {
      height = s;
   }

   public String getId()
   {
      return id;
   }

   public void setId(String value)
   {
      id = value;
   }

   public String[] getAccessPermissions()
   {
      return accessPermissions;
   }

   public void setAccessPermissions(String[] accessPermissions)
   {
      this.accessPermissions = accessPermissions;
   }

   public boolean isModifiable()
   {
      return isModifiable;
   }

   public void setModifiable(boolean modifiable)
   {
      isModifiable = modifiable;
   }

   public ApplicationState<S> getState()
   {
      return state;
   }

   public void setState(ApplicationState<S> value)
   {
      state = value;
   }

   public boolean getShowInfoBar()
   {
      return showInfoBar;
   }

   public void setShowInfoBar(boolean b)
   {
      showInfoBar = b;
   }

   public boolean getShowApplicationState()
   {
      return showApplicationState;
   }

   public void setShowApplicationState(boolean b)
   {
      showApplicationState = b;
   }

   public boolean getShowApplicationMode()
   {
      return showApplicationMode;
   }

   public void setShowApplicationMode(boolean b)
   {
      showApplicationMode = b;
   }

   public String getIcon()
   {
      return icon;
   }

   public void setIcon(String value)
   {
      icon = value;
   }

   public String getDescription()
   {
      return description;
   }

   public void setDescription(String des)
   {
      description = des;
   }

   public String getTitle()
   {
      return title;
   }

   public void setTitle(String value)
   {
      title = value;
   }

   public Properties getProperties()
   {
      if (properties == null)
         properties = new Properties();
      return properties;
   }

   public void setProperties(Properties properties)
   {
      this.properties = properties;
   }

   public String getTheme()
   {
      return theme;
   }

   public void setTheme(String theme)
   {
      this.theme = theme;
   }

   @Override
   public ModelData build()
   {
      return new ApplicationData<S>(
         storageId,
         storageName,
         getType(),
         state,
         id,
         title,
         icon,
         description,
         showInfoBar,
         showApplicationState,
         showApplicationMode,
         theme,
         width,
         height,
         Utils.safeImmutableMap(properties),
         Utils.safeImmutableList(accessPermissions)
      );
   }

   public static Application<Gadget> createGadgetApplication(ApplicationData<Gadget> data)
   {
      return new Application<Gadget>(data);
   }

   public static Application<Gadget> createGadgetApplication(String storageId)
   {
      return new Application<Gadget>(ApplicationType.GADGET, storageId);
   }

   public static Application<Gadget> createGadgetApplication()
   {
      return new Application<Gadget>(ApplicationType.GADGET);
   }

   public static Application<Portlet> createPortletApplication(ApplicationData<Portlet> data)
   {
      return new Application<Portlet>(data);
   }

   public static Application<Portlet> createPortletApplication(String storageId)
   {
      return new Application<Portlet>(ApplicationType.PORTLET, storageId);
   }

   public static Application<Portlet> createPortletApplication()
   {
      return new Application<Portlet>(ApplicationType.PORTLET);
   }

   public static Application<WSRP> createWSRPApplication(ApplicationData<WSRP> wsrpwsrpIdApplicationData)
   {
      return new Application<WSRP>(wsrpwsrpIdApplicationData);
   }

   public static Application<WSRP> createWSRPApplication()
   {
      return new Application<WSRP>(ApplicationType.WSRP_PORTLET);
   }

   public static Application<WSRP> createWSRPApplication(String storageId)
   {
      return new Application<WSRP>(ApplicationType.WSRP_PORTLET, storageId);
   }
}