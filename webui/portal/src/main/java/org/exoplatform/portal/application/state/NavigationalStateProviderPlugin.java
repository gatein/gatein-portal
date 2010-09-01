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

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.portal.config.model.PageNode;
import org.exoplatform.portal.webui.application.UIPortlet;
import org.exoplatform.portal.webui.page.UIPage;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.Util;

/**
 * @author <a href="mailto:hoang281283@gmail.com">Minh Hoang TO</a>
 * @date
 */

public class NavigationalStateProviderPlugin extends AbstractContextualPropertyProviderPlugin
{

   private final String NAMESPACE_URI;
   
   private final String PREFIX;
   
   public NavigationalStateProviderPlugin(InitParams params) throws Exception
   {
      NAMESPACE_URI = params.getValueParam("namespaceURI").getValue();
      PREFIX = params.getValueParam("prefix").getValue();
   }
   
   @Override
   public Map<QName, String[]> getProperties(UIPortlet portletWindow)
   {
      Map<QName, String[]> whatThisPluginProvides = new HashMap<QName, String[]>();

      try
      {
         UIPortal currentSite = Util.getUIPortalApplication().getShowedUIPortal();
         PageNode currentNode = currentSite.getSelectedNode();
         
         //Provides current node URI
         whatThisPluginProvides.put(new QName(NAMESPACE_URI, "navigation_uri", PREFIX), new String[]{currentNode.getUri()});
         
         //Provides current page name
         UIPage currentPage = currentSite.getUIPage(currentNode.getPageReference());
         if(currentPage != null)
         {
            whatThisPluginProvides.put(new QName(NAMESPACE_URI, "page_name", PREFIX), new String[]{currentPage.getTitle()});
         }
         
         //Provides current site type
         whatThisPluginProvides.put(new QName(NAMESPACE_URI, "site_type", PREFIX), new String[]{currentSite.getOwnerType()});
         
         //Provides current site name
         whatThisPluginProvides.put(new QName(NAMESPACE_URI, "site_name", PREFIX), new String[]{currentSite.getOwner()});
         
         return whatThisPluginProvides;
      }
      catch (Exception ex)
      {
         ex.printStackTrace();
         return null;
      }
   }

}
