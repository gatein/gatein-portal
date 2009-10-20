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

package org.exoplatform.application.registry.jcr;

import org.exoplatform.application.gadget.Gadget;
import org.exoplatform.application.gadget.GadgetRegistryService;
import org.exoplatform.application.registry.Application;
import org.exoplatform.application.registry.ApplicationCategoriesPlugins;
import org.exoplatform.application.registry.ApplicationCategory;
import org.exoplatform.application.registry.ApplicationRegistryService;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.registry.RegistryEntry;
import org.exoplatform.services.jcr.ext.registry.RegistryService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.MembershipHandler;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.web.application.gadget.GadgetApplication;
import org.gatein.common.i18n.LocalizedString;
import org.gatein.common.util.Tools;
import org.gatein.pc.api.Portlet;
import org.gatein.pc.api.PortletInvoker;
import org.gatein.pc.api.info.MetaInfo;
import org.gatein.pc.api.info.PortletInfo;
import org.picocontainer.Startable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

/**
 * Created by The eXo Platform SARL
 * Author : Tung Pham
 *          thanhtungty@gmail.com
 * Nov 23, 2007
 */
public class ApplicationRegistryServiceImpl implements ApplicationRegistryService, Startable
{

   static final private String APPLICATION_REGISTRY = "ApplicationRegistry";

   static final private String CATEGORY_DATA = "CategoryData";

   static final private String APPLICATIONS = "applications";

   private Log log = ExoLogger.getLogger("ApplicationRegistryService");

   RegistryService regService_;

   DataMapper mapper_ = new DataMapper();

   private List<ApplicationCategoriesPlugins> plugins;

   private static final String REMOTE_CATEGORY_NAME = "remote";

   public ApplicationRegistryServiceImpl(RegistryService service) throws Exception
   {
      regService_ = service;
   }

   public List<ApplicationCategory> getApplicationCategories(String accessUser, String... appTypes) throws Exception
   {
      List<ApplicationCategory> categories = getApplicationCategories();
      Iterator<ApplicationCategory> cateItr = categories.iterator();
      ExoContainer container = ExoContainerContext.getCurrentContainer();
      OrganizationService orgService =
         (OrganizationService)container.getComponentInstanceOfType(OrganizationService.class);
      UserACL acl = (UserACL)container.getComponentInstanceOfType(UserACL.class);
      while (cateItr.hasNext())
      {
         ApplicationCategory cate = cateItr.next();
         //TODO: dang.tung: filer category application
         if (!hasAccessPermission(orgService, acl, accessUser, cate))
         {
            cateItr.remove();
            continue;
         }
         List<Application> applications = getApplications(cate, appTypes);
         Iterator<Application> appIterator = applications.iterator();
         while (appIterator.hasNext())
         {
            Application app = appIterator.next();
            if (!hasAccessPermission(orgService, acl, accessUser, app))
               appIterator.remove();
         }
         cate.setApplications(applications);
      }
      return categories;
   }

   public List<ApplicationCategory> getApplicationCategories() throws Exception
   {
      return getApplicationCategories(null);
   }

   public List<ApplicationCategory> getApplicationCategories(Comparator<ApplicationCategory> sortComparator)
      throws Exception
   {
      SessionProvider sessionProvider = SessionProvider.createSystemProvider();
      Node regNode = regService_.getRegistry(sessionProvider).getNode();
      Session session = regNode.getSession();
      StringBuilder builder = new StringBuilder("select * from " + DataMapper.EXO_REGISTRYENTRY_NT);
      generateScript(builder, "jcr:path", (regNode.getPath() + "/" + RegistryService.EXO_APPLICATIONS + "/"
         + APPLICATION_REGISTRY + "/%"));
      generateScript(builder, DataMapper.TYPE, ApplicationCategory.class.getSimpleName());
      QueryManager queryManager = session.getWorkspace().getQueryManager();
      Query query = queryManager.createQuery(builder.toString(), "sql");
      QueryResult result = query.execute();
      NodeIterator itr = result.getNodes();
      List<ApplicationCategory> categories = new ArrayList<ApplicationCategory>();
      while (itr.hasNext())
      {
         Node cateNode = itr.nextNode();
         String entryPath = cateNode.getPath().substring(regNode.getPath().length() + 1);
         RegistryEntry entry = regService_.getEntry(sessionProvider, entryPath);
         ApplicationCategory cate = mapper_.toApplicationCategory(entry.getDocument());
         categories.add(cate);
      }
      sessionProvider.close();
      if (sortComparator != null)
         Collections.sort(categories, sortComparator);
      return categories;
   }

   private void generateScript(StringBuilder sql, String name, String value)
   {
      if (value == null || value.length() < 1)
         return;
      if (sql.indexOf(" where") < 0)
         sql.append(" where ");
      else
         sql.append(" and ");
      value = value.replace('*', '%');
      sql.append(name).append(" like '").append(value).append("'");
   }

   public ApplicationCategory getApplicationCategory(String name) throws Exception
   {
      String categoryDataPath = getCategoryPath(name) + "/" + CATEGORY_DATA;
      SessionProvider sessionProvider = SessionProvider.createSystemProvider();
      RegistryEntry entry;
      try
      {
         entry = regService_.getEntry(sessionProvider, categoryDataPath);
      }
      catch (PathNotFoundException e)
      {
         return null;
      }
      catch (RepositoryException ie)
      {
         log.error("Could not create application category '" + name + "' with path '" + categoryDataPath + "'", ie);
         throw ie;
      }
      finally
      {
         sessionProvider.close();
      }
      return mapper_.toApplicationCategory(entry.getDocument());
   }

   public void save(ApplicationCategory category) throws Exception
   {
      String categoryPath = getCategoryPath(category.getName());
      SessionProvider sessionProvider = SessionProvider.createSystemProvider();
      try
      {
         RegistryEntry entry;
         try
         {
            entry = regService_.getEntry(sessionProvider, categoryPath + "/" + CATEGORY_DATA);
         }
         catch (PathNotFoundException ie)
         {
            entry = new RegistryEntry(CATEGORY_DATA);
            regService_.createEntry(sessionProvider, categoryPath, entry);
         }
         mapper_.map(entry.getDocument(), category);
         regService_.recreateEntry(sessionProvider, categoryPath, entry);
      }
      finally
      {
         sessionProvider.close();
      }
   }

   public void remove(ApplicationCategory category) throws Exception
   {
      String categoryDataPath = getCategoryPath(category.getName()) + "/" + CATEGORY_DATA;
      SessionProvider sessionProvider = SessionProvider.createSystemProvider();
      //remove all applications of this group
      for (Application app : getApplications(category))
      {
         remove(app);
      }
      //remove category data
      regService_.removeEntry(sessionProvider, categoryDataPath);
      sessionProvider.close();
   }

   public List<Application> getAllApplications() throws Exception
   {
      List<Application> applications = new ArrayList<Application>();
      List<ApplicationCategory> categories = getApplicationCategories();
      for (ApplicationCategory cate : categories)
      {
         applications.addAll(getApplications(cate));
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
      String applicationPath = getCategoryPath(fragments[0]) + "/" + APPLICATIONS + "/" + fragments[1];
      SessionProvider sessionProvider = SessionProvider.createSystemProvider();
      RegistryEntry entry;
      try
      {
         entry = regService_.getEntry(sessionProvider, applicationPath);
      }
      catch (PathNotFoundException ie)
      {
         sessionProvider.close();
         return null;
      }
      Application application = mapper_.toApplication(entry.getDocument());
      sessionProvider.close();
      return application;
   }

   public Application getApplication(String category, String name) throws Exception
   {
      String applicationPath = getCategoryPath(category) + "/" + APPLICATIONS + "/" + name;
      SessionProvider sessionProvider = SessionProvider.createSystemProvider();
      RegistryEntry entry;
      try
      {
         entry = regService_.getEntry(sessionProvider, applicationPath);
      }
      catch (PathNotFoundException ie)
      {
         sessionProvider.close();
         return null;
      }
      Application application = mapper_.toApplication(entry.getDocument());
      sessionProvider.close();
      return application;
   }

   public List<Application> getApplications(ApplicationCategory category, String... appTypes) throws Exception
   {

      return getApplications(category, null, appTypes);
   }

   @SuppressWarnings("unchecked")
   public List<Application> getApplications(ApplicationCategory category, Comparator<Application> sortComparator,
      String... appTypes) throws Exception
   {

      SessionProvider sessionProvider = SessionProvider.createSystemProvider();
      Node regNode = regService_.getRegistry(sessionProvider).getNode();
      Session session = regNode.getSession();
      String appsPath = regNode.getPath() + "/" + getCategoryPath(category.getName()) + "/" + APPLICATIONS;
      Node appsNode;
      try
      {
         appsNode = (Node)session.getItem(appsPath);
      }
      catch (PathNotFoundException pnfe)
      {
         sessionProvider.close();
         return new ArrayList<Application>();
      }
      NodeIterator itr = appsNode.getNodes();
      List<Application> applications = new ArrayList<Application>();
      while (itr.hasNext())
      {
         Node appNode = itr.nextNode();
         String entryPath = appNode.getPath().substring(regNode.getPath().length() + 1);
         RegistryEntry entry = regService_.getEntry(sessionProvider, entryPath);
         Application app = mapper_.toApplication(entry.getDocument());
         if (isApplicationType(app, appTypes))
            applications.add(app);
      }
      sessionProvider.close();
      if (sortComparator != null)
         Collections.sort(applications, sortComparator);
      return applications;
   }

   //TODO: dang.tung
   public void importExoGadgets() throws Exception
   {
      ExoContainer container = ExoContainerContext.getCurrentContainer();
      GadgetRegistryService gadgetService =
         (GadgetRegistryService)container.getComponentInstanceOfType(GadgetRegistryService.class);
      List<Gadget> eXoGadgets = gadgetService.getAllGadgets();
      if (eXoGadgets == null || eXoGadgets.size() < 1)
      {
         return;
      }
      ArrayList<String> permissions = new ArrayList<String>();
      permissions.add(UserACL.EVERYONE);
      String categoryName = GadgetApplication.EXO_GADGET_GROUP;
      ApplicationCategory category = getApplicationCategory(categoryName);
      if (category == null)
      {
         category = new ApplicationCategory();
         category.setName(categoryName);
         category.setDisplayName(categoryName);
         category.setDescription(categoryName);
         category.setAccessPermissions(permissions);
         save(category);
      }

      for (Gadget ele : eXoGadgets)
      {
         Application app = getApplication(category.getName() + "/" + ele.getName());
         if (app == null)
         {
            app = convertApplication(ele);
            app.setAccessPermissions(permissions);
            save(category, app);
         }
      }
   }

   public void importAllPortlets() throws Exception
   {

      ExoContainer manager = ExoContainerContext.getCurrentContainer();

      PortletInvoker portletInvoker = (PortletInvoker)manager.getComponentInstance(PortletInvoker.class);
      Set<Portlet> portlets = portletInvoker.getPortlets();

      for (Portlet portlet : portlets)
      {
         PortletInfo info = portlet.getInfo();
         String portletApplicationName = info.getApplicationName();
         String portletName = info.getName();

         // need to sanitize portlet and application names in case they contain characters that would cause an improper Application name
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
            ApplicationCategory category;

            categoryName = categoryName.trim();

            category = getApplicationCategory(categoryName);
            if (category == null)
            {
               category = new ApplicationCategory();
               category.setName(categoryName);
               category.setDisplayName(categoryName);
               save(category);
            }

            Application app = getApplication(categoryName + "/" + portletName);
            if (app != null)
            {
               continue;
            }
            LocalizedString descriptionLS = portlet.getInfo().getMeta().getMetaValue(MetaInfo.DESCRIPTION);
            LocalizedString displayNameLS = portlet.getInfo().getMeta().getMetaValue(MetaInfo.DISPLAY_NAME);

            getLocalizedStringValue(descriptionLS, portletName);

            app = new Application();
            app.setApplicationName(portletName);
            app.setApplicationGroup(portletApplicationName);
            app.setCategoryName(categoryName);

            String applicationType = org.exoplatform.web.application.Application.EXO_PORTLET_TYPE;
            if (portlet.isRemote())
            {
               applicationType = org.exoplatform.web.application.Application.WSRP_TYPE;
            }
            app.setApplicationType(applicationType);

            app.setDisplayName(getLocalizedStringValue(displayNameLS, portletName));
            app.setDescription(getLocalizedStringValue(descriptionLS, portletName));

            app.setUri(portlet.getContext().getId());

            save(category, app);
         }
      }
   }

   public void remove(Application app) throws Exception
   {
      String applicationPath =
         getCategoryPath(app.getCategoryName()) + "/" + APPLICATIONS + "/" + app.getApplicationName();
      SessionProvider sessionProvider = SessionProvider.createSystemProvider();
      regService_.removeEntry(sessionProvider, applicationPath);
      sessionProvider.close();
   }

   public void save(ApplicationCategory category, Application application) throws Exception
   {
      //prepare category
      String cateName = category.getName();
      String categoryPath = getCategoryPath(cateName);
      SessionProvider sessionProvider = SessionProvider.createSystemProvider();
      try
      {
         RegistryEntry entry;
         try
         {
            entry = regService_.getEntry(sessionProvider, categoryPath + "/" + CATEGORY_DATA);
         }
         catch (PathNotFoundException ie)
         {
            entry = new RegistryEntry(CATEGORY_DATA);
            mapper_.map(entry.getDocument(), category);
            regService_.createEntry(sessionProvider, categoryPath, entry);
         }

         //save application
         application.setCategoryName(cateName);
         String applicationSetPath = getCategoryPath(cateName) + "/" + APPLICATIONS;
         String appName = application.getApplicationName();
         try
         {
            entry = regService_.getEntry(sessionProvider, applicationSetPath + "/" + appName);
         }
         catch (PathNotFoundException ie)
         {
            entry = new RegistryEntry(appName);
            regService_.createEntry(sessionProvider, applicationSetPath, entry);
         }
         mapper_.map(entry.getDocument(), application);
         regService_.recreateEntry(sessionProvider, applicationSetPath, entry);
      }
      finally
      {
         sessionProvider.close();
      }
   }

   public void update(Application application) throws Exception
   {
      String applicationSetPath = getCategoryPath(application.getCategoryName()) + "/" + APPLICATIONS;
      SessionProvider sessionProvider = SessionProvider.createSystemProvider();
      RegistryEntry entry =
         regService_.getEntry(sessionProvider, applicationSetPath + "/" + application.getApplicationName());
      mapper_.map(entry.getDocument(), application);
      regService_.recreateEntry(sessionProvider, applicationSetPath, entry);
      sessionProvider.close();
   }

   public void clearAllRegistries() throws Exception
   {
      for (ApplicationCategory cate : getApplicationCategories())
      {
         remove(cate);
      }
   }

   public void initListener(ComponentPlugin com) throws Exception
   {
      if (com instanceof ApplicationCategoriesPlugins)
      {
         if (plugins == null)
            plugins = new ArrayList<ApplicationCategoriesPlugins>();
         plugins.add((ApplicationCategoriesPlugins)com);
      }
   }

   public void start()
   {
      try
      {
         if (plugins == null)
            return;
         for (ApplicationCategoriesPlugins plugin : plugins)
            plugin.run();
      }
      catch (Exception e)
      {
         log.error(e);
      }
   }

   public void stop()
   {
   }

   //-------------------------------------Util function-------------------------------/
   private boolean hasAccessPermission(OrganizationService orgService, UserACL acl, String remoteUser, Application app)
      throws Exception
   {
      if (acl.getSuperUser().equals(remoteUser))
         return true;
      List<String> permissions = app.getAccessPermissions();
      if (permissions == null)
         return false;
      for (String ele : permissions)
      {
         if (hasViewPermission(orgService, acl, remoteUser, ele))
            return true;
      }
      return false;
   }

   //TODO: dang.tung: check ApplicationCategory permission
   private boolean hasAccessPermission(OrganizationService orgService, UserACL acl, String remoteUser,
      ApplicationCategory app) throws Exception
   {
      if (acl.getSuperUser().equals(remoteUser))
         return true;
      List<String> permissions = app.getAccessPermissions();
      if (permissions == null)
         return false;
      for (String ele : permissions)
      {
         if (hasViewPermission(orgService, acl, remoteUser, ele))
            return true;
      }
      return false;
   }

   private boolean hasViewPermission(OrganizationService orgService, UserACL acl, String remoteUser, String expPerm)
      throws Exception
   {
      if (UserACL.EVERYONE.equals(expPerm))
         return true;
      String[] temp = expPerm.split(":");
      if (temp.length < 2)
         return false;
      String membership = temp[0].trim();
      String groupId = temp[1].trim();
      MembershipHandler handler = orgService.getMembershipHandler();
      if (membership == null || "*".equals(membership))
      {
         Collection<?> c = handler.findMembershipsByUserAndGroup(remoteUser, groupId);
         if (c == null)
            return false;
         return c.size() > 0;
      }
      return handler.findMembershipByUserGroupAndType(remoteUser, groupId, membership) != null;
   }

   private String getCategoryPath(String categoryName)
   {
      return RegistryService.EXO_APPLICATIONS + "/" + APPLICATION_REGISTRY + "/" + categoryName;
   }

   private boolean isApplicationType(Application app, String... appTypes)
   {
      if (appTypes == null || appTypes.length < 1)
         return true;
      for (String appType : appTypes)
      {
         if (appType.equals(app.getApplicationType()))
            return true;
      }
      return false;
   }

   private Application convertApplication(Gadget gadget)
   {
      Application returnApplication = new Application();
      returnApplication.setApplicationGroup(GadgetApplication.EXO_GADGET_GROUP);
      returnApplication.setApplicationType(org.exoplatform.web.application.Application.EXO_GADGET_TYPE);
      returnApplication.setApplicationName(gadget.getName());
      returnApplication.setCategoryName(GadgetApplication.EXO_GADGET_GROUP);
      returnApplication.setDisplayName(gadget.getTitle());
      returnApplication.setDescription(gadget.getDescription());
      returnApplication.setUri(gadget.getUrl());
      return returnApplication;
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

}
