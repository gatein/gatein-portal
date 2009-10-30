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

package org.exoplatform.portal.resource.config.xml;

import org.exoplatform.portal.resource.config.tasks.AbstractSkinTask;
import org.exoplatform.portal.resource.config.tasks.PortalSkinTask;
import org.exoplatform.portal.resource.config.tasks.PortletSkinTask;
import org.exoplatform.portal.resource.config.tasks.ThemeTask;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * 
 * Created by eXoPlatform SAS
 *
 * Author: Minh Hoang TO - hoang281283@gmail.com
 *
 *      Sep 17, 2009
 */
public abstract class AbstractTaskXMLBinding
{

   /** Bind an XML element to a skin task */
   abstract public AbstractSkinTask xmlToTask(Element element);

   public static class PortalSkinTaskXMLBinding extends AbstractTaskXMLBinding
   {

      @Override
      public AbstractSkinTask xmlToTask(Element element)
      {
         if (!element.getTagName().equals(GateinResource.PORTAl_SKIN_TAG))
         {
            return null;
         }
         PortalSkinTask pTask = new PortalSkinTask();
         bindingCSSPath(pTask, element);
         bindingSkinName(pTask, element);

         return pTask;
      }

      private void bindingCSSPath(PortalSkinTask task, Element element)
      {
         NodeList nodes = element.getElementsByTagName(GateinResource.CSS_PATH_TAG);
         if (nodes == null || nodes.getLength() < 1)
         {
            return;
         }
         String cssPath = nodes.item(0).getFirstChild().getNodeValue();
         task.setCSSPath(cssPath);
      }

      private void bindingSkinName(PortalSkinTask task, Element element)
      {
         NodeList nodes = element.getElementsByTagName(GateinResource.SKIN_NAME_TAG);
         if (nodes == null || nodes.getLength() < 1)
         {
            return;
         }
         String skinName = nodes.item(0).getFirstChild().getNodeValue();
         task.setSkinName(skinName);
      }

   }

   public static class ThemeTaskXMLBinding extends AbstractTaskXMLBinding
   {
      @Override
      public AbstractSkinTask xmlToTask(Element element)
      {
         if (!element.getTagName().equals(GateinResource.WINDOW_STYLE_TAG))
         {
            return null;
         }
         ThemeTask tTask = new ThemeTask();

         bindingStyleName(tTask, element);
         bindingThemeNames(tTask, element);

         return tTask;
      }

      private void bindingStyleName(ThemeTask task, Element element)
      {
         NodeList nodes = element.getElementsByTagName(GateinResource.STYLE_NAME_TAG);
         if (nodes == null || nodes.getLength() < 1)
         {
            return;
         }
         String styleName = nodes.item(0).getFirstChild().getNodeValue();
         task.setStyleName(styleName);
      }

      private void bindingThemeNames(ThemeTask task, Element element)
      {
         NodeList nodes = element.getElementsByTagName(GateinResource.THEME_NAME_TAG);
         if (nodes == null)
         {
            return;
         }
         for (int i = nodes.getLength() - 1; i >= 0; i--)
         {
            task.addThemeName(nodes.item(i).getFirstChild().getNodeValue());
         }
      }
   }

   public static class PortletSkinTaskXMLBinding extends AbstractTaskXMLBinding
   {
      @Override
      public AbstractSkinTask xmlToTask(Element element)
      {
         if (!element.getTagName().equals(GateinResource.PORTLET_SKIN_TAG))
         {
            return null;
         }
         PortletSkinTask pTask = new PortletSkinTask();
         bindingApplicationName(pTask, element);
         bindingPortletName(pTask, element);
         bindingCSSPath(pTask, element);
         bindingSkinName(pTask, element);
         return pTask;
      }

      private void bindingApplicationName(PortletSkinTask task, Element element)
      {
         NodeList nodes = element.getElementsByTagName(GateinResource.APPLICATION_NAME_TAG);
         if (nodes == null || nodes.getLength() < 1)
         {
            return;
         }
         String applicationName = nodes.item(0).getFirstChild().getNodeValue();
         task.setApplicationName(applicationName);
      }

      private void bindingPortletName(PortletSkinTask task, Element element)
      {
         NodeList nodes = element.getElementsByTagName(GateinResource.PORTLET_NAME_TAG);
         if (nodes == null || nodes.getLength() < 1)
         {
            return;
         }
         String portletName = nodes.item(0).getFirstChild().getNodeValue();
         task.setPortletName(portletName);
      }

      private void bindingCSSPath(PortletSkinTask task, Element element)
      {
         NodeList nodes = element.getElementsByTagName(GateinResource.CSS_PATH_TAG);
         if (nodes == null || nodes.getLength() < 1)
         {
            return;
         }
         String cssPath = nodes.item(0).getFirstChild().getNodeValue();
         task.setCSSPath(cssPath);
      }

      private void bindingSkinName(PortletSkinTask task, Element element)
      {
         NodeList nodes = element.getElementsByTagName(GateinResource.SKIN_NAME_TAG);
         if (nodes == null || nodes.getLength() < 1)
         {
            return;
         }
         String skinName = nodes.item(0).getFirstChild().getNodeValue();
         task.setSkinName(skinName);
      }
   }

   public static class I18nTaskXMLBinding extends AbstractTaskXMLBinding
   {
      @Override
      public AbstractSkinTask xmlToTask(Element element)
      {
         return null;
      }
   }

}
