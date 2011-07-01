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

package org.exoplatform.portal.webui.page;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PageBody;
import org.exoplatform.portal.mop.user.UserNode;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.portal.UIPortalComponent;
import org.exoplatform.portal.webui.util.PortalDataMapper;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIComponentDecorator;

/**
 * May 19, 2006
 */
@ComponentConfig(template = "system:/groovy/portal/webui/page/UIPageBody.gtmpl")
public class UIPageBody extends UIComponentDecorator
{

   private UIPortalComponent maximizedUIComponent;

   private String storageId;

   public UIPageBody(PageBody model) throws Exception
   {
      setId("UIPageBody");
   }

   public String getStorageId()
   {
      return storageId;
   }

   public void setStorageId(String storageId)
   {
      this.storageId = storageId;
   }

   public UIPageBody() throws Exception
   {
      setId("UIPageBody");
   }

   public void init(PageBody model) throws Exception
   {
      setId("UIPageBody");
   }

   public void setPageBody(UserNode pageNode, UIPortal uiPortal) throws Exception
   {
      WebuiRequestContext context = Util.getPortalRequestContext();
      uiPortal.setMaximizedUIComponent(null);
      
      UIPage uiPage;
      uiPage = getUIPage(pageNode, uiPortal, context);
      if (uiPage == null)
      {
         setUIComponent(null);
         return;
      }
      
      if (uiPage.isShowMaxWindow())
      {
         uiPortal.setMaximizedUIComponent(uiPage);
      }
      else
      {
         UIComponent maximizedComponent = uiPortal.getMaximizedUIComponent();
         if (maximizedComponent != null && maximizedComponent instanceof UIPage)
         {
            uiPortal.setMaximizedUIComponent(null);
         }
         maximizedComponent = this.getMaximizedUIComponent();
         if (maximizedComponent != null && maximizedComponent instanceof UIPage)
         {
            this.setMaximizedUIComponent(null);
         }
      }
      setUIComponent(uiPage);
   }

   /**
    * Return cached UIPage or a newly built UIPage
    * 
    * @param pageReference
    * @param page
    * @param uiPortal
    * @return
    */
   private UIPage getUIPage(UserNode pageNode, UIPortal uiPortal, WebuiRequestContext context)
      throws Exception
   {
      Page page = null;
      String pageReference = null;
      
      if (pageNode != null)
      {
         pageReference = pageNode.getPageRef();
         if (pageReference != null)
         {
            ExoContainer appContainer = context.getApplication().getApplicationServiceContainer();
            UserPortalConfigService userPortalConfigService =
               (UserPortalConfigService)appContainer.getComponentInstanceOfType(UserPortalConfigService.class);
            page = userPortalConfigService.getPage(pageReference, context.getRemoteUser());
         }
      }
      
      //The page has been deleted
      if(page == null)
      {
         //Clear the UIPage from cache in UIPortal
         uiPortal.clearUIPage(pageReference);
         return null;
      }
      
      UIPage uiPage = uiPortal.getUIPage(pageReference);
      if (uiPage != null)
      {
         return uiPage;
      }
                                                                                                  
      UIPageFactory clazz =  UIPageFactory.getInstance(page.getFactoryId());
      uiPage = clazz.createUIPage(context);
      
      PortalDataMapper.toUIPage(uiPage, page);
      uiPortal.setUIPage(page.getId(), uiPage);

      return uiPage;
   }
   
   public void processRender(WebuiRequestContext context) throws Exception
   {
      if (maximizedUIComponent != null && Util.getUIPortalApplication().getModeState() % 2 == 0)
      {
         maximizedUIComponent.processRender((WebuiRequestContext)WebuiRequestContext.getCurrentInstance());
         return;
      }
      
      super.processRender(context);
   }

   /**
    * Retrieve the UIApplication which is maximized to cover whole the PageBody
    * 
    * @return the maximized portlet
    */
   public UIPortalComponent getMaximizedUIComponent()
   {
      return maximizedUIComponent;
   }

   /**
    * Store the portlet maximized into the current PageBody
    * 
    * @param uiMaximizedComponent the portlet to be stored in UIPageBody
    */
   public void setMaximizedUIComponent(UIPortalComponent uiMaximizedComponent)
   {
      this.maximizedUIComponent = uiMaximizedComponent;
   }

}
