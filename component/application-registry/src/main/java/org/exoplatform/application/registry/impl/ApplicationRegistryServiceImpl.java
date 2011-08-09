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
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.mop.api.content.ContentType;
import org.gatein.mop.api.content.Customization;
import org.gatein.pc.api.PortletInvoker;
import org.gatein.pc.api.info.MetaInfo;
import org.gatein.pc.api.info.PortletInfo;
import org.picocontainer.Startable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The fundamental reason that motives to use tasks is because of the JMX access that does not setup a context and
 * therefore the task either reuse the existing context setup by the portal or create a temporary context when accessed
 * by JMX.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ApplicationRegistryServiceImpl implements ApplicationRegistryService, Startable
{

   /** . */
   private static final String INTERNAL_PORTLET_TAG = "gatein_internal";

   /** . */
   private static final String REMOTE_CATEGORY_NAME = "remote";

   /** . */
   private List<ApplicationCategoriesPlugins> plugins;

   /** . */
   private final ChromatticManager manager;

   /** . */
   private final ChromatticLifeCycle lifeCycle;

   /** . */
   private final Logger log = LoggerFactory.getLogger(ApplicationRegistryServiceImpl.class);

   /** . */
   final POMSessionManager mopManager;

   /** Should match WSRPPortletInfo.PRODUCER_NAME_META_INFO_KEY */
   private static final String PRODUCER_NAME_META_INFO_KEY = "producer-name";
   public static final String PRODUCER_CATEGORY_NAME_SUFFIX = " Producer";

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
      ContentRegistry registry = session.findByPath(ContentRegistry.class, "app:applications");
      if (registry == null)
      {
         registry = session.insert(ContentRegistry.class, "app:applications");
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

      String category = fragments[0];
      String applicationName = fragments[1];

      // If the application name contained a beginning slash (which can happen with WSRP), we need to hack around the
      // hardcoding of portlet id expectations >_<
      if (fragments.length == 3 && applicationName.length() == 0)
      {
         applicationName = "/" + fragments[2];
      }
      return getApplication(category, applicationName);
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
         CategoryDefinition category = getOrCreateCategoryDefinition(categoryName, permissions);

         //
         for (Gadget ele : eXoGadgets)
         {
            String name = ele.getName();
            ContentDefinition app = category.getContentMap().get(name);
            if (app == null)
            {
               app = category.createContent(name, org.exoplatform.portal.pom.spi.gadget.Gadget.CONTENT_TYPE, name);
               app.setDisplayName(ele.getTitle());
               app.setDescription(ele.getDescription());
               app.setAccessPermissions(permissions);
            }
         }
      }
   }

   public Application createOrUpdateApplication(String categoryName, String definitionName, ContentType<?> contentType, String contentId, String displayName, String description, List<String> permissions)
   {
      CategoryDefinition definition = getOrCreateCategoryDefinition(categoryName, permissions);
      ContentDefinition content = definition.getContentMap().get(definitionName);
      if (content == null)
      {
         content = createContent(definitionName, contentType, contentId, displayName, description, permissions, definition);
      }
      else
      {
         content.setAccessPermissions(permissions);
         content.setDescription(description);
         content.setDisplayName(displayName);
      }
      return load(content);
   }

   private ContentDefinition createContent(String definitionName, ContentType<?> contentType, String contentId, String displayName, String description, List<String> permissions, CategoryDefinition category)
   {
      ContentDefinition content = category.createContent(definitionName, contentType, contentId);
      content.setDisplayName(displayName);
      content.setDescription(description);
      content.setAccessPermissions(permissions);
      return content;
   }

   private ContentDefinition createContentFrom(Application application, CategoryDefinition category)
   {
      return createContent(application.getApplicationName(), application.getType().getContentType(), application.getContentId(), application.getDisplayName(), application.getDescription(), application.getAccessPermissions(), category);
   }

   private CategoryDefinition getOrCreateCategoryDefinition(String categoryName, List<String> permissions)
   {
      ContentRegistry registry = getContentRegistry();
      CategoryDefinition category = registry.getCategory(categoryName);
      if (category == null)
      {
         category = registry.createCategory(categoryName);
         category.setDisplayName(categoryName);
         category.setDescription(categoryName);
         category.setAccessPermissions(permissions);
      }
      return category;
   }

   public void importAllPortlets() throws Exception
   {
      //
      log.info("About to import portlets in application registry");

      //
      ExoContainer manager = ExoContainerContext.getCurrentContainer();
      PortletInvoker portletInvoker = (PortletInvoker)manager.getComponentInstance(PortletInvoker.class);
      Set<org.gatein.pc.api.Portlet> portlets = portletInvoker.getPortlets();

      //
      portlet:
      for (org.gatein.pc.api.Portlet portlet : portlets)
      {
         PortletInfo info = portlet.getInfo();

         final Application application = createApplicationFrom(portlet);

         String portletApplicationName = info.getApplicationName();
         final String portletName = application.getApplicationName();

         MetaInfo metaInfo = portlet.getInfo().getMeta();
         LocalizedString keywordsLS = metaInfo.getMetaValue(MetaInfo.KEYWORDS);

         // Process keywords
         Set<String> categoryNames = new HashSet<String>();
         if (keywordsLS != null)
         {
            String keywords = keywordsLS.getDefaultString();
            if (keywords != null && keywords.length() != 0)
            {
               for (String categoryName : keywords.split(","))
               {
                  // Trim name
                  categoryName = categoryName.trim();
                  if (INTERNAL_PORTLET_TAG.equalsIgnoreCase(categoryName))
                  {
                     log.debug("Skipping portlet (" + portletApplicationName + "," + portletName + ") + tagged as internal");
                     continue portlet;
                  }
                  else
                  {
                     categoryNames.add(categoryName);
                  }
               }
            }
         }

         ArrayList<String> permissions = new ArrayList<String>();
         permissions.add(UserACL.EVERYONE);
         // If no keywords, use the portlet application name
         if (categoryNames.isEmpty())
         {
            categoryNames.add(portletApplicationName.trim());
         }

         // Additionally categorise the portlet as remote
         boolean remote = portlet.isRemote();
         if (remote)
         {
            categoryNames.add(REMOTE_CATEGORY_NAME);

            // add producer name to categories for easier finding of portlets for GTNPORTAL-823
            LocalizedString producerNameLS = metaInfo.getMetaValue(PRODUCER_NAME_META_INFO_KEY);
            if (producerNameLS != null)
            {
               categoryNames.add(producerNameLS.getDefaultString() + PRODUCER_CATEGORY_NAME_SUFFIX);
            }
         }

         //
         log.info("Importing portlet (" + portletApplicationName + "," + portletName + ") in categories " + categoryNames);

         // Process category names
         for (String categoryName : categoryNames)
         {
            CategoryDefinition category = getOrCreateCategoryDefinition(categoryName, permissions);

            //
            ContentDefinition app = category.getContentMap().get(portletName);
            if (app == null)
            {
               // Check if the portlet has already existed in this category
               List<Application> applications = load(category).getApplications();
               boolean isExist = false;
               for (Application categoryApp : applications)
               {
                  if (categoryApp.getContentId().equals(application.getContentId()))
                  {
                     isExist = true;
                     break;
                  }
               }

               if (!isExist)
               {
                  app = createContentFrom(application, category);
               }
            }
         }
      }
   }

   public Application createApplicationFrom(org.gatein.pc.api.Portlet portlet)
   {
      PortletInfo info = portlet.getInfo();

      String portletName = portlet.getContext().getId();

      portletName = sanitize(portletName);

      MetaInfo metaInfo = portlet.getInfo().getMeta();

      ArrayList<String> permissions = new ArrayList<String>();
      permissions.add(UserACL.EVERYONE);

      // Additionally categorise the portlet as remote
      boolean remote = portlet.isRemote();

      LocalizedString descriptionLS = metaInfo.getMetaValue(MetaInfo.DESCRIPTION);
      LocalizedString displayNameLS = metaInfo.getMetaValue(MetaInfo.DISPLAY_NAME);
      String displayName = getLocalizedStringValue(displayNameLS, portletName);

      ApplicationType appType;
      String contentId;
      if (remote)
      {
         appType = ApplicationType.WSRP_PORTLET;
         contentId = portlet.getContext().getId();
         displayName += REMOTE_DISPLAY_NAME_SUFFIX;  // add remote to display name to make it more obvious that the portlet is remote
      }
      else
      {
         appType = ApplicationType.PORTLET;
         contentId = info.getApplicationName() + "/" + info.getName();
      }

      Application app = new Application();
      app.setApplicationName(portletName);
      app.setType(appType);
      app.setDisplayName(displayName);
      app.setDescription(getLocalizedStringValue(descriptionLS, portletName));
      app.setAccessPermissions(permissions);
      app.setContentId(contentId);

      return app;
   }

   /**
    * Sanitizes the specified name in case it contains characters that would cause improper Application name.
    *
    * @param name
    * @return
    */
   private String sanitize(String name)
   {
      return name.replace('/', '_');
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
      Customization customization = contentDef.getCustomization();

      //
      ContentType<?> contentType = customization.getType();
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
      application.setStorageId(customization.getId());
      application.setContentId(customization.getContentId());
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
         else if (type == WSRP.CONTENT_TYPE)
         {
            return "/eXoResources/skin/sharedImages/Icon80x80/DefaultPortlet.png";
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
            if (this.getApplicationCategories().size() < 1)
            {
               for (ApplicationCategoriesPlugins plugin : plugins)
               {
                  plugin.run();
               }
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
