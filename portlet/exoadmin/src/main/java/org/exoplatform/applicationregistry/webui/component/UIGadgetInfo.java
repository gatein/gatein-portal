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

package org.exoplatform.applicationregistry.webui.component;

import org.apache.commons.io.IOUtils;
import org.exoplatform.application.gadget.Gadget;
import org.exoplatform.application.gadget.GadgetRegistryService;
import org.exoplatform.application.gadget.Source;
import org.exoplatform.application.gadget.SourceStorage;
import org.exoplatform.application.registry.Application;
import org.exoplatform.application.registry.ApplicationCategory;
import org.exoplatform.application.registry.ApplicationRegistryService;
import org.exoplatform.portal.webui.application.GadgetUtil;
import org.exoplatform.web.WebAppController;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.web.application.gadget.GadgetApplication;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by The eXo Platform SAS
 * Author : Pham Thanh Tung
 *          thanhtungty@gmail.com
 * Aug 22, 2008  
 */
@ComponentConfig(template = "app:/groovy/applicationregistry/webui/component/UIGadgetInfo.gtmpl", events = {
   @EventConfig(listeners = UIGadgetInfo.RefreshActionListener.class),
   @EventConfig(listeners = UIGadgetInfo.CopyActionListener.class),
   @EventConfig(listeners = UIGadgetInfo.EditActionListener.class),
   @EventConfig(listeners = UIGadgetInfo.ShowCategoriesActionListener.class)})
public class UIGadgetInfo extends UIContainer
{

   private Gadget gadget_;

   public UIGadgetInfo() throws Exception
   {
      UICategorySelector categorySelector = addChild(UICategorySelector.class, null, null);
      categorySelector.setRendered(false);
   }

   public Gadget getGadget()
   {
      return gadget_;
   }

   public void setGadget(Gadget gadget)
   {
      gadget_ = gadget;
   }

   public String getViewUrl()
   {
      return GadgetUtil.reproduceUrl(gadget_.getUrl(), gadget_.isLocal());
   }

   public String getEditUrl()
   {
      if (gadget_.isLocal())
         return GadgetUtil.getEditPath(gadget_.getUrl());
      return null;
   }

   public String getCategorieNames() throws Exception
   {
      ApplicationRegistryService appRegService = getApplicationComponent(ApplicationRegistryService.class);
      List<ApplicationCategory> allCategories = appRegService.getApplicationCategories();
      List<String> nameList = new ArrayList<String>();

      for (ApplicationCategory category : allCategories)
      {
         if (appRegService.getApplication(category.getName(), gadget_.getName()) != null)
         {
            nameList.add(category.getDisplayName());
         }
      }
      StringBuffer names = new StringBuffer("");
      for (String name : nameList)
      {
         names.append(name);
         if (name != nameList.get(nameList.size() - 1))
            names.append(", ");
      }
      return names.toString();
   }

   static public class RefreshActionListener extends EventListener<UIGadgetInfo>
   {

      public void execute(Event<UIGadgetInfo> event) throws Exception
      {
         UIGadgetInfo uiInfo = event.getSource();
         WebuiRequestContext ctx = event.getRequestContext();
         UIGadgetManagement uiManagement = uiInfo.getParent();
         Gadget gadget = uiInfo.getGadget();
         GadgetRegistryService service = uiInfo.getApplicationComponent(GadgetRegistryService.class);
         if (service.getGadget(gadget.getName()) == null)
         {
            UIApplication uiApp = ctx.getUIApplication();
            uiApp.addMessage(new ApplicationMessage("UIGadgetInfo.msg.gadgetNotExist", null));
            uiManagement.reload();
            return;
         }
         service.saveGadget(GadgetUtil.toGadget(gadget.getName(), gadget.getUrl(), gadget.isLocal()));
         uiManagement.initData();
         uiManagement.setSelectedGadget(gadget.getName());
         ctx.addUIComponentToUpdateByAjax(uiManagement);
      }

   }

   static public class CopyActionListener extends EventListener<UIGadgetInfo>
   {

      public void execute(Event<UIGadgetInfo> event) throws Exception
      {
         UIGadgetInfo uiInfo = event.getSource();
         UIGadgetManagement uiManagement = uiInfo.getParent();
         String url = uiInfo.getGadget().getUrl();
         String name = uiInfo.getGadget().getName();
         GadgetRegistryService service = uiInfo.getApplicationComponent(GadgetRegistryService.class);
         if (service.getGadget(name) == null)
         {
            UIApplication uiApp = event.getRequestContext().getUIApplication();
            uiApp.addMessage(new ApplicationMessage("UIGadgetInfo.msg.gadgetNotExist", null));
            uiManagement.reload();
            return;
         }
         URL urlObj = new URL(url);
         URLConnection conn = urlObj.openConnection();
         InputStream is = conn.getInputStream();
         SourceStorage sourceStorage = uiInfo.getApplicationComponent(SourceStorage.class);
         String fileName = name + ".xml";
         Source source = new Source(fileName, "application/xml", "UTF-8");
         source.setTextContent(IOUtils.toString(is, "UTF-8"));
         source.setLastModified(Calendar.getInstance());
         sourceStorage.saveSource(name, source);
         service.saveGadget(GadgetUtil.toGadget(name, sourceStorage.getSourceURI(name + "/" + fileName), true));
         WebAppController webController = uiManagement.getApplicationComponent(WebAppController.class);
         webController.removeApplication(GadgetApplication.EXO_GADGET_GROUP + "/" + name);
         uiManagement.initData();
         uiManagement.setSelectedGadget(name);
         event.getRequestContext().addUIComponentToUpdateByAjax(uiManagement);
      }

   }

   static public class EditActionListener extends EventListener<UIGadgetInfo>
   {

      public void execute(Event<UIGadgetInfo> event) throws Exception
      {
         UIGadgetInfo uiInfo = event.getSource();
         Gadget gadget = uiInfo.getGadget();

         UIGadgetManagement uiManagement = uiInfo.getParent();
         GadgetRegistryService service = uiInfo.getApplicationComponent(GadgetRegistryService.class);
         if (service.getGadget(gadget.getName()) == null)
         {
            UIApplication uiApp = event.getRequestContext().getUIApplication();
            uiApp.addMessage(new ApplicationMessage("UIGadgetInfo.msg.gadgetNotExist", null));
            uiManagement.reload();
            return;
         }
         SourceStorage sourceStorage = uiManagement.getApplicationComponent(SourceStorage.class);
         UIGadgetEditor uiEditor = uiManagement.createUIComponent(UIGadgetEditor.class, null, null);
         String fileName = gadget.getName() + ".xml";
         // get dir path of gadget 
         String gadgetUrl = gadget.getUrl();
         String[] gaggetUrlPart = gadgetUrl.split("/");
         String dirPath = gaggetUrlPart[gaggetUrlPart.length - 2];
         //String dirPath = gaggetUrlPart[gaggetUrlPart.length - 9];
         // get gadget's source: path = dir path + file name
         Source source = sourceStorage.getSource(dirPath + "/" + fileName);
         uiEditor.setSource(source);
         uiEditor.setDirPath(dirPath);
         uiManagement.getChildren().clear();
         uiManagement.addChild(uiEditor);
         event.getRequestContext().addUIComponentToUpdateByAjax(uiManagement);
      }

   }

   static public class ShowCategoriesActionListener extends EventListener<UIGadgetInfo>
   {
      @Override
      public void execute(Event<UIGadgetInfo> event) throws Exception
      {
         UIGadgetInfo gadgetInfo = event.getSource();
         Gadget gadget = gadgetInfo.getGadget();
         UICategorySelector selector = gadgetInfo.getChild(UICategorySelector.class);
         
         Application app = new Application();
         app.setApplicationName(gadget.getName());
         app.setApplicationType(org.exoplatform.web.application.Application.EXO_GADGET_TYPE);
         app.setDisplayName(gadget.getTitle());
         //app.setUri(gadget.getUrl());
         String description =
            (gadget.getDescription() == null || gadget.getDescription().length() < 1) ? gadget.getName() : gadget
               .getDescription();
         app.setDescription(description);
         app.setAccessPermissions(new ArrayList<String>());
         
         selector.setup(app);
         selector.setRendered(true);
         event.getRequestContext().addUIComponentToUpdateByAjax(event.getSource());
      }
   }

}