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

package org.exoplatform.dashboard.webui.component;

import org.exoplatform.application.registry.Application;
import org.exoplatform.application.registry.ApplicationCategory;
import org.exoplatform.application.registry.ApplicationRegistryService;
import org.exoplatform.portal.config.model.ApplicationType;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

@ComponentConfig(template = "classpath:groovy/dashboard/webui/component/UIDashboardSelectContainer.gtmpl", lifecycle = UIFormLifecycle.class)
public class UIDashboardSelectContainer extends UIContainer
{

   private List<ApplicationCategory> categories;

   private ApplicationCategory selectedCategory;

   public UIDashboardSelectContainer() throws Exception
   {
      addChild(UIAddGadgetForm.class, null, null);
   }

   public void setSelectedCategory(ApplicationCategory category)
   {
      selectedCategory = category;
   }

   public ApplicationCategory getSelectedCategory()
   {
      return selectedCategory;
   }

   public final List<ApplicationCategory> getCategories() throws Exception
   {
      ApplicationRegistryService service = getApplicationComponent(ApplicationRegistryService.class);

      String remoteUser = ((WebuiRequestContext)WebuiRequestContext.getCurrentInstance()).getRemoteUser();
      List<ApplicationCategory> listCategories =
         service.getApplicationCategories(remoteUser, ApplicationType.GADGET);

      Iterator<ApplicationCategory> appCateIte = listCategories.iterator();
      while (appCateIte.hasNext())
      {
         ApplicationCategory cate = appCateIte.next();
         List<Application> listGadgets = cate.getApplications();
         if (listGadgets == null || listGadgets.size() == 0)
         {
            appCateIte.remove();
            continue;
         }
      }
      Collections.sort(listCategories, new Comparator<ApplicationCategory>()
      {
         public int compare(ApplicationCategory cate1, ApplicationCategory cate2)
         {
            return cate1.getDisplayName().compareToIgnoreCase(cate2.getDisplayName());
         }
      });
      categories = listCategories;
      return categories;
   }

   public void setCategories(final List<ApplicationCategory> categories) throws Exception
   {
      this.categories = categories;
   }

   public List<Application> getGadgetsOfCategory(final ApplicationCategory appCategory) throws Exception
   {
      List<Application> listGadgets = appCategory.getApplications();
      Collections.sort(listGadgets, new Comparator<Application>()
      {
         public int compare(Application app1, Application app2)
         {
            return app1.getDisplayName().compareToIgnoreCase(app2.getDisplayName());
         }
      });
      return listGadgets;
   }

}
