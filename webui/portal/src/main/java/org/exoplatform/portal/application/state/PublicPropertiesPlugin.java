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
package org.exoplatform.portal.application.state;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.portal.mop.user.UserNode;
import org.exoplatform.portal.webui.application.UIPortlet;
import org.exoplatform.portal.webui.page.UIPage;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.Util;

import java.util.Map;

import javax.xml.namespace.QName;

/**
 * This plugin setup properties that are publicly supported, hence this is part of a public API
 * and once published its contract must not change whatsoever.
 *
 * @author <a href="mailto:hoang281283@gmail.com">Minh Hoang TO</a>
 */
public class PublicPropertiesPlugin extends AbstractContextualPropertyProviderPlugin
{

   /** . */
   private final QName navigationURIQName;

   /** . */
   private final QName pageNameQName;

   /** . */
   private final QName siteTypeQName;

   /** . */
   private final QName siteNameQName;

   /** . */
   private final QName windowShowInfoBarQName;

   /** . */
   private final QName windowHeight;

   /** . */
   private final QName windowWidth;

   public PublicPropertiesPlugin(InitParams params) throws Exception
   {
      super(params);

      //
      this.navigationURIQName = new QName(namespaceURI, "navigation_uri");
      this.pageNameQName = new QName(namespaceURI, "page_name");
      this.siteTypeQName = new QName(namespaceURI, "site_type");
      this.siteNameQName = new QName(namespaceURI, "site_name");
      this.windowWidth = new QName(namespaceURI, "window_width");
      this.windowHeight = new QName(namespaceURI, "window_height");
      this.windowShowInfoBarQName = new QName(namespaceURI, "window_show_info_bar");
   }

   @Override
   public void getProperties(UIPortlet portletWindow, Map<QName, String[]> properties)
   {
      try
      {
         UIPortal currentSite = Util.getUIPortalApplication().getCurrentSite();
         UserNode currentNode = currentSite.getSelectedUserNode();
         
         // Navigation related properties
         addProperty(properties, navigationURIQName, currentNode.getURI());

         // Page related properties
         UIPage currentPage = currentSite.getUIPage(currentNode.getPageRef());
         if(currentPage != null)
         {
            addProperty(properties, pageNameQName, currentPage.getTitle());
         }
         
         // Site related properties
         addProperty(properties, siteTypeQName, currentSite.getSiteType().getName());
         addProperty(properties, siteNameQName, currentSite.getName());

         // Window related properties
         addProperty(properties, windowShowInfoBarQName, Boolean.toString(portletWindow.getShowInfoBar()));
         addProperty(properties, windowWidth, portletWindow.getWidth());
         addProperty(properties, windowHeight, portletWindow.getHeight());
      }
      catch (Exception ex)
      {
         log.error("Could not obtain contextual properties for portlet " + portletWindow, ex);
      }
   }
}
