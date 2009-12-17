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
package org.exoplatform.application.registry.impl;

import org.chromattic.api.ChromatticSession;
import org.exoplatform.application.gadget.Gadget;
import org.exoplatform.application.gadget.GadgetRegistryService;
import org.exoplatform.application.registry.Application;
import org.exoplatform.application.registry.ApplicationCategoriesPlugins;
import org.exoplatform.application.registry.ApplicationCategory;
import org.exoplatform.application.registry.ApplicationRegistryService;
import org.exoplatform.commons.chromattic.ChromatticLifeCycle;
import org.exoplatform.commons.chromattic.ChromatticManager;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.model.ApplicationType;
import org.exoplatform.portal.pom.config.POMSessionManager;
import org.exoplatform.portal.pom.spi.portlet.Portlet;
import org.exoplatform.portal.pom.spi.wsrp.WSRP;
import org.gatein.common.i18n.LocalizedString;
import org.gatein.common.util.Tools;
import org.gatein.mop.api.content.ContentType;
import org.gatein.mop.api.content.Customization;
import org.gatein.pc.api.PortletInvoker;
import org.gatein.pc.api.info.MetaInfo;
import org.gatein.pc.api.info.PortletInfo;
import org.picocontainer.Startable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * The fundamental reason that motives to use tasks is because of the JMX access that does not
 * setup a context and therefore the task either reuse the existing context setup by the portal
 * or create a temporary context when accessed by JMX.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ApplicationRegistryServiceImpl implements ApplicationRegistryService, Startable
{

   /** . */
   private static final String REMOTE_CATEGORY_NAME = "remote";

   /** . */
   private List<ApplicationCategoriesPlugins> plugins;

   /** . */
   private final ChromatticManager manager;

   /** . */
   private final ChromatticLifeCycle lifeCycle;

   /** . */
   final POMSessionManager mopManager;

   public ApplicationRegistryServiceImpl(ChromatticManager manager, POMSessionManager mopManager)
   {
      ApplicationRegistryChromatticLifeCycle lifeCycle = (ApplicationRegistryChromatticLifeCycle)manager.getLifeCycle("app");
      lifeCycle.registry = this;

      //
      this.manager = manager;
      this.lifeCycle = lifeCycle;
      this.mopManager = mopManager;
   }

   public ContentRegistry getContentRegistry()
   {
      ChromatticSession session = lifeCycle.getChromattic().openSession();
      ContentRegistry registry = session.findByPath(ContentRegistry.class, "contents");
      if (registry == null)
      {
         registry = session.insert(ContentRegistry.class, "contents");
      }
      return registry;
   }

   public void initListener(ComponentPlugin com) throws Exception
   {
      if (com instanceof ApplicationCategoriesPlugins)
      {
         if (plugins == null)
         {
            plugins = new ArrayList<ApplicationCategoriesPlugins>();
         }
         plugins.add((ApplicationCategoriesPlugins)com);
      }
   }

   public List<ApplicationCategory> getApplicationCategories(
      final Comparator<ApplicationCategory> sortComparator,
      String accessUser,
      final ApplicationType<?>... appTypes) throws Exception
   {
      final List<ApplicationCategory> categories = new ArrayList<ApplicationCategory>();

      //
      ContentRegistry registry = getContentRegistry();

      //
      for (CategoryDefinition categoryDef : registry.getCategoryList())
      {
         ApplicationCategory category = load(categoryDef, appTypes);
         categories.add(category);
      }

      //
      if (sortComparator != null)
      {
         Collections.sort(categories, sortComparator);
      }

      //
      return categories;
   }

   public List<ApplicationCategory> getApplicationCategories(String accessUser, ApplicationType<?>... appTypes) throws Exception
   {
      return getApplicationCategories(null, accessUser, appTypes);
   }

   public List<ApplicationCategory> getApplicationCategories() throws Exception
   {
      return getApplicationCategories(null);
   }

   public List<ApplicationCategory> getApplicationCategories(Comparator<ApplicationCategory> sortComparator) throws Exception
   {
      return getApplicationCategories(sortComparator, null);
   }

   public ApplicationCategory getApplicationCategory(final String name) throws Exception
   {
      ContentRegistry registry = getContentRegistry();

      //
      CategoryDefinition categoryDef = registry.getCategory(name);
      if (categoryDef != null)
      {
         ApplicationCategory applicationCategory = load(categoryDef);
         return applicationCategory;
      }

      //
      return null;
   }

   public void save(final ApplicationCategory category) throws Exception
   {
      ContentRegistry registry = getContentRegistry();

      //
      String categoryName = category.getName();

      //
      CategoryDefinition categoryDef = registry.getCategory(categoryName);
      if (categoryDef == null)
      {
         categoryDef = registry.createCategory(categoryName);
      }

      //
      categoryDef.setDisplayName(category.getDisplayName());
      categoryDef.setCreationDate(category.getCreatedDate());
      categoryDef.setLastModificationDate(category.getModifiedDate());
      categoryDef.setDescription(category.getDescription());
      categoryDef.setAccessPermissions(category.getAccessPermissions());
   }

   public void remove(final ApplicationCategory category) throws Exception
   {
      ContentRegistry registry = getContentRegistry();
      registry.getCategoryMap().remove(category.getName());
   }

   public List<Application> getApplications(ApplicationCategory category, ApplicationType<?>... appTypes) throws Exception
   {
      return getApplications(category, null, appTypes);
   }

   public List<Application> getApplications(
      final ApplicationCategory category,
      final Comparator<Application> sortComparator,
      final ApplicationType<?>... appTypes) throws Exception
   {
      ContentRegistry registry = getContentRegistry();

      //
      CategoryDefinition categoryDef = registry.getCategory(category.getName());
      List<Application> applications = load(categoryDef, appTypes).getApplications();

      //
      if (sortComparator != null)
      {
         Collections.sort(applications, sortComparator);
      }

      //
      return applications;
   }

   public List<Application> getAllApplications() throws Exception
   {
      List<Application> applications = new ArrayList<Application>();
      List<ApplicationCategory> categories = getApplicationCategories();
      for (ApplicationCategory category : categories)
      {
         applications.addAll(getApplications(category));
      }
      return applications;
   }

   public Application getApplication(String id) throws Exception
   {
      String[] fragments = id.split("/");
      if (fragments.length < 2)
      {
         throw new Exception("Invalid Application Id: [" + id + "]");
      }
      return getApplication(fragments[0], fragments[1]);
   }

   public Application getApplication(final String category, final String name) throws Exception
   {
      ContentRegistry registry = getContentRegistry();

      //
      CategoryDefinition categoryDef = registry.getCategory(category);
      if (categoryDef != null)
      {
         ContentDefinition contentDef = categoryDef.getContentMap().get(name);
         if (contentDef != null)
         {
            return load(contentDef);
         }
      }

      //
      return null;
   }

   public void save(final ApplicationCategory category, final Application application) throws Exception
   {
      ContentRegistry registry = getContentRegistry();

      //
      String categoryName = category.getName();
      CategoryDefinition categoryDef = registry.getCategory(categoryName);
      if (categoryDef == null)
      {
         categoryDef = registry.createCategory(categoryName);
         save(category, categoryDef);
      }

      //
      ContentDefinition contentDef = null;
      CategoryDefinition applicationCategoryDef = registry.getCategory(application.getCategoryName());
      String applicationName = application.getApplicationName();
      if (applicationCategoryDef != null)
      {
         contentDef = applicationCategoryDef.getContentMap().get(applicationName);
      }
      if (contentDef == null)
      {
         String contentId = application.getContentId();
         ContentType<?> contentType = application.getType().getContentType();
         String definitionName = application.getApplicationName();
         contentDef = categoryDef.createContent(definitionName, contentType, contentId);
      }
      else
      {
         // A JCR move actually
         categoryDef.getContentList().add(contentDef);
      }

      // Update state
      save(application, contentDef);
   }

   public void update(final Application application) throws Exception
   {
      ContentRegistry registry = getContentRegistry();

      //
      String categoryName = application.getCategoryName();
      CategoryDefinition categoryDef = registry.getCategory(categoryName);
      if (categoryDef == null)
      {
         throw new IllegalStateException();
      }

      //
      ContentDefinition contentDef = categoryDef.getContentMap().get(application.getApplicationName());
      if (contentDef == null)
      {
         throw new IllegalStateException();
      }

      // Update state
      save(application, contentDef);
   }

   public void remove(final Application app) throws Exception
   {
      if (app == null)
      {
         throw new NullPointerException();
      }

      //
      ContentRegistry registry = getContentRegistry();

      //
      String categoryName = app.getCategoryName();
      CategoryDefinition categoryDef = registry.getCategory(categoryName);

      //
      if (categoryDef != null)
      {

         String contentName = app.getApplicationName();
         categoryDef.getContentMap().remove(contentName);
      }
   }

   public void importExoGadgets() throws Exception
   {
      ContentRegistry registry = getContentRegistry();

      //
      ExoContainer container = ExoContainerContext.getCurrentContainer();
      GadgetRegistryService gadgetService = (GadgetRegistryService)container.getComponentInstanceOfType(GadgetRegistryService.class);
      List<Gadget> eXoGadgets = gadgetService.getAllGadgets();

      //
      if (eXoGadgets != null)
      {
         ArrayList<String> permissions = new ArrayList<String>();
         permissions.add(UserACL.EVERYONE);
         String categoryName = "Gadgets";

         //
         CategoryDefinition category = registry.getCategory(categoryName);
         if (category == null)
         {
            category = registry.createCategory(categoryName);
            category.setDisplayName(categoryName);
            category.setDescription(categoryName);
            category.setAccessPermissions(permissions);
         }

         //
         for (Gadget ele : eXoGadgets)
         {
            ContentDefinition app = category.getContentMap().get(ele.getName());
            if (app == null)
            {
               app = category.createContent(ele.getName(), org.exoplatform.portal.pom.spi.gadget.Gadget.CONTENT_TYPE, ele.getName());
               app.setDisplayName(ele.getTitle());
               app.setDescription(ele.getDescription());
               app.setAccessPermissions(permissions);
            }
         }
      }
   }

   public void importAllPortlets() throws Exception
   {
      ContentRegistry registry = getContentRegistry();

      //
      ExoContainer manager = ExoContainerContext.getCurrentContainer();
      PortletInvoker portletInvoker = (PortletInvoker)manager.getComponentInstance(PortletInvoker.class);
      Set<org.gatein.pc.api.Portlet> portlets = portletInvoker.getPortlets();

      //
      for (org.gatein.pc.api.Portlet portlet : portlets)
      {
         PortletInfo info = portlet.getInfo();
         String portletApplicationName = info.getApplicationName();
         String portletName = info.getName();

         // Need to sanitize portlet and application names in case they contain characters that would
         // cause an improper Application name
         portletApplicationName = portletApplicationName.replace('/', '_');
         portletName = portletName.replace('/', '_');

         LocalizedString keywordsLS = info.getMeta().getMetaValue(MetaInfo.KEYWORDS);

         String[] categoryNames = null;
         if (keywordsLS != null)
         {
            String keywords = keywordsLS.getDefaultString();
            if (keywords != null && keywords.length() != 0)
            {
               categoryNames = keywords.split(",");
            }
         }

         if (categoryNames == null || categoryNames.length == 0)
         {
            categoryNames = new String[]{portletApplicationName};
         }

         if (portlet.isRemote())
         {
            categoryNames = Tools.appendTo(categoryNames, REMOTE_CATEGORY_NAME);
         }

         //
         for (String categoryName : categoryNames)
         {
            categoryName = categoryName.trim();

            //
            CategoryDefinition category = registry.getCategory(categoryName);

            //
            if (category == null)
            {
               category = registry.createCategory(categoryName);
               category.setDisplayName(categoryName);
            }

            //
            ContentDefinition app = category.getContentMap().get(portletName);
            if (app == null)
            {
               LocalizedString descriptionLS = portlet.getInfo().getMeta().getMetaValue(MetaInfo.DESCRIPTION);
               LocalizedString displayNameLS = portlet.getInfo().getMeta().getMetaValue(MetaInfo.DISPLAY_NAME);

               // julien: ????
               // getLocalizedStringValue(descriptionLS, portletName);

               ContentType<?> contentType;
               String contentId;
               if (portlet.isRemote())
               {
                  contentType = WSRP.CONTENT_TYPE;
                  contentId = portlet.getContext().getId();
               }
               else
               {
                  contentType = Portlet.CONTENT_TYPE;
                  contentId = info.getApplicationName() + "/" + info.getName();
               }


               //
               app = category.createContent(portletName, contentType, contentId);
               app.setDisplayName(getLocalizedStringValue(displayNameLS, portletName));
               app.setDescription(getLocalizedStringValue(descriptionLS, portletName));
            }
         }
      }
   }

   private boolean isApplicationType(Application app, ApplicationType<?>... appTypes)
   {
      if (appTypes == null || appTypes.length == 0)
      {
         return true;
      }
      for (ApplicationType<?> appType : appTypes)
      {
         if (appType.equals(app.getType()))
         {
            return true;
         }
      }
      return false;
   }

   private void save(ApplicationCategory category, CategoryDefinition categoryDef)
   {
      categoryDef.setDisplayName(category.getDisplayName());
      categoryDef.setDescription(category.getDescription());
      categoryDef.setAccessPermissions(category.getAccessPermissions());
      categoryDef.setCreationDate(category.getCreatedDate());
      categoryDef.setLastModificationDate(category.getModifiedDate());
   }

   private ApplicationCategory load(CategoryDefinition categoryDef, ApplicationType<?>... appTypes)
   {
      ApplicationCategory category = new ApplicationCategory();

      //
      category.setName(categoryDef.getName());
      category.setDisplayName(categoryDef.getDisplayName());
      category.setDescription(categoryDef.getDescription());
      category.setAccessPermissions(new ArrayList<String>(categoryDef.getAccessPermissions()));
      category.setCreatedDate(categoryDef.getCreationDate());
      category.setModifiedDate(categoryDef.getLastModificationDate());

      //
      for (ContentDefinition contentDef : categoryDef.getContentList())
      {
         Application application = load(contentDef);
         if (isApplicationType(application, appTypes))
         {
            category.getApplications().add(application);
         }
      }

      //
      return category;
   }

   private void save(Application application, ContentDefinition contentDef)
   {
      contentDef.setDisplayName(application.getDisplayName());
      contentDef.setDescription(application.getDescription());
      contentDef.setAccessPermissions(application.getAccessPermissions());
      contentDef.setCreationDate(application.getCreatedDate());
      contentDef.setLastModificationDate(application.getModifiedDate());
   }

   private Application load(ContentDefinition contentDef)
   {
      ContentType<?> contentType = contentDef.getCustomization().getType();
      ApplicationType<?> applicationType = ApplicationType.getType(contentType);

      //
      Application application = new Application();
      application.setId(contentDef.getCategory().getName() + "/" + contentDef.getName());
      application.setCategoryName(contentDef.getCategory().getName());
      application.setType(applicationType);
      application.setApplicationName(contentDef.getName());
      application.setIconURL(getApplicationIconURL(contentDef));
      application.setDisplayName(contentDef.getDisplayName());
      application.setDescription(contentDef.getDescription());
      application.setAccessPermissions(new ArrayList<String>(contentDef.getAccessPermissions()));
      application.setCreatedDate(contentDef.getCreationDate());
      application.setModifiedDate(contentDef.getLastModificationDate());
      application.setStorageId(contentDef.getCustomization().getId());
      application.setContentId(contentDef.getCustomization().getContentId());
      return application;
   }

   private String getLocalizedStringValue(LocalizedString localizedString, String portletName)
   {
      if (localizedString == null || localizedString.getDefaultString() == null)
      {
         return portletName;
      }
      else
      {
         return localizedString.getDefaultString();
      }
   }

   private static String getApplicationIconURL(ContentDefinition contentDef)
   {
      Customization customization = contentDef.getCustomization();
      if (customization != null)
      {
         ContentType type = customization.getType();
         String contentId = customization.getContentId();
         if (type == Portlet.CONTENT_TYPE)
         {
            String[] chunks = contentId.split("/");
            if (chunks.length == 2)
            {
               return "/" + chunks[0] + "/skin/DefaultSkin/portletIcons/" + chunks[1] + ".png";
            }
         }
         else if (type == org.exoplatform.portal.pom.spi.gadget.Gadget.CONTENT_TYPE)
         {
            return "/" + "eXoGadgets" + "/skin/DefaultSkin/portletIcons/" + contentId + ".png";
         }
      }

      //
      return null;
   }

   public void start()
   {
      if (plugins != null)
      {
         RequestLifeCycle.begin(manager);
         boolean save = false;
         try
         {
            for (ApplicationCategoriesPlugins plugin : plugins)
            {
               plugin.run();
            }
            save = true;
         }
         catch (Exception e)
         {
            // log.error(e);
            e.printStackTrace();
         }
         finally
         {
            // lifeCycle.closeContext(context, true);
            manager.getSynchronization().setSaveOnClose(save);
            RequestLifeCycle.end();
         }
      }
   }

   public void stop()
   {
   }
}
