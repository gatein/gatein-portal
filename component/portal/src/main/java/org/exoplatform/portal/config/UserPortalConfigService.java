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

package org.exoplatform.portal.config;

import org.exoplatform.commons.utils.LazyPageList;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.portal.config.model.Application;
import org.exoplatform.portal.config.model.Container;
import org.exoplatform.portal.config.model.ModelObject;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PageNode;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.config.model.TransientApplicationState;
import org.exoplatform.portal.pom.data.ModelChange;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.OrganizationService;
import org.picocontainer.Startable;

import java.util.*;

/**
 * Created by The eXo Platform SAS Apr 19, 2007 This service is used to load the PortalConfig, Page config and
 * Navigation config for a given user.
 */
public class UserPortalConfigService implements Startable
{
   private DataStorage storage_;

   private UserACL userACL_;

   private OrganizationService orgService_;

   private NewPortalConfigListener newPortalConfigListener_;
   
   private Log log = ExoLogger.getLogger("Portal:UserPortalConfigService");

   public UserPortalConfigService(
      UserACL userACL, DataStorage storage,
      OrganizationService orgService) throws Exception
   {
      this.storage_ = storage;
      this.orgService_ = orgService;
      this.userACL_ = userACL;
   }

   /**
    * <p> Build and returns an instance of <tt>UserPortalConfig</tt>. </p>
    * <p/>
    * <p> To return a valid config, the current thread must be associated with an identity that will grant him access to
    * the portal as returned by the {@link UserACL#hasPermission(org.exoplatform.portal.config.model.PortalConfig)}
    * method. </p>
    * <p/>
    * <p> The navigation loaded on the <tt>UserPortalConfig<tt> object are obtained according to the specified user
    * argument. The portal navigation is always loaded. If the specified user is null then the navigation of the guest
    * group as configured by {@link org.exoplatform.portal.config.UserACL#getGuestsGroup()} is also loaded, otherwise
    * the navigations are loaded according to the following rules:
    * <p/>
    * <ul> <li>The navigation corresponding to the user is loaded.</li> <li>When the user is root according to the value
    * returned by {@link org.exoplatform.portal.config.UserACL#getSuperUser()} then the navigation of all groups are
    * loaded.</li> <li>When the user is not root, then all its groups are added except the guest group as configued per
    * {@link org.exoplatform.portal.config.UserACL#getGuestsGroup()}.</li> </ul>
    * <p/>
    * All the navigations are sorted using the value returned by {@link org.exoplatform.portal.config.model.PageNavigation#getPriority()}.
    * </p>
    *
    * @param portalName the portal name
    * @param accessUser the user name
    * @return the config
    * @throws Exception any exception
    */
   public UserPortalConfig getUserPortalConfig(String portalName, String accessUser) throws Exception
   {
      PortalConfig portal = storage_.getPortalConfig(portalName);
      if (portal == null || !userACL_.hasPermission(portal))
      {
         return null;
      }

      List<PageNavigation> navigations = new ArrayList<PageNavigation>();
      PageNavigation navigation = storage_.getPageNavigation(PortalConfig.PORTAL_TYPE, portalName);
      if (navigation != null)
      {
         navigation.setModifiable(userACL_.hasPermission(portal.getEditPermission()));
         navigations.add(navigation);
      }

      if (accessUser == null)
      {
         // navigation = getPageNavigation(PortalConfig.GROUP_TYPE,
         // userACL_.getGuestsGroup());
         // if (navigation != null)
         // navigations.add(navigation);
      }
      else
      {
         navigation = storage_.getPageNavigation(PortalConfig.USER_TYPE, accessUser);
         if (navigation != null)
         {
            navigation.setModifiable(true);
            navigations.add(navigation);
         }

         Collection<?> groups = null;
         if (userACL_.getSuperUser().equals(accessUser))
         {
            groups = orgService_.getGroupHandler().getAllGroups();
         }
         else
         {
            groups = orgService_.getGroupHandler().findGroupsOfUser(accessUser);
         }
         for (Object group : groups)
         {
            Group m = (Group)group;
            String groupId = m.getId().trim();
            if (groupId.equals(userACL_.getGuestsGroup()))
            {
               continue;
            }
            navigation = storage_.getPageNavigation(PortalConfig.GROUP_TYPE, groupId);
            if (navigation == null)
            {
               continue;
            }
            navigation.setModifiable(userACL_.hasEditPermission(navigation));
            navigations.add(navigation);
         }
      }
      Collections.sort(navigations, new Comparator<PageNavigation>()
      {
         public int compare(PageNavigation nav1, PageNavigation nav2)
         {
            return nav1.getPriority() - nav2.getPriority();
         }
      });

      return new UserPortalConfig(portal, navigations);
   }

   /**
    * Compute and returns the list that the specified user can manage. If the user is root then all existing groups are
    * returned otherwise the list is computed from the groups in which the user has a configured membership. The
    * membership is configured from the value returned by {@link org.exoplatform.portal.config.UserACL#getMakableMT()}
    *
    * @param remoteUser the user to get the makable navigations
    * @param withSite true if a site must exist 
    * @return the list of groups
    * @throws Exception any exception
    */
   public List<String> getMakableNavigations(String remoteUser, boolean withSite) throws Exception
   {
      Collection<Group> groups;
      if (remoteUser.equals(userACL_.getSuperUser()))
      {
         groups = orgService_.getGroupHandler().getAllGroups();
      }
      else
      {
         groups = orgService_.getGroupHandler().findGroupByMembership(remoteUser, userACL_.getMakableMT());
      }

      //
      List<String> list = new ArrayList<String>();
      if (groups != null)
      {
         Set<String> existingNames = null;
         if (withSite)
         {
            existingNames = new HashSet<String>();
            Query<PortalConfig> q = new Query<PortalConfig>("group", null, PortalConfig.class);
            LazyPageList<PortalConfig> lpl = storage_.find(q);
            for (PortalConfig groupSite : lpl.getAll())
            {
               existingNames.add(groupSite.getName());
            }
         }

         //
         for (Group group : groups)
         {
            String groupId = group.getId().trim();
            if (existingNames == null || existingNames.contains(groupId))
            {
               list.add(groupId);
            }
         }
      }

      //
      return list;
   }

   /**
    * Create a user site for the specified user. It will perform the following:
    * <ul>
    * <li>create the user site by calling {@link #createUserPortalConfig(String, String, String)} which may create
    * a site or not according to the default configuration</li>
    * <li>if not site exists then it creates a site then it creates an empty site</li>
    * <li>if not navigation exists for the user site then it creates an empty navigation</li>
    * </ul>
    *
    * @param userName the user name
    * @throws Exception a nasty exception
    */
   public void createUserSite(String userName) throws Exception 
   {
      // Create the portal from the template
      createUserPortalConfig(PortalConfig.USER_TYPE, userName, "user");

      // Need to insert the corresponding user site if needed
      PortalConfig cfg = storage_.getPortalConfig(PortalConfig.USER_TYPE, userName);
      if (cfg == null)
      {
         cfg = new PortalConfig(PortalConfig.USER_TYPE);
         cfg.setPortalLayout(new Container());
         cfg.setName(userName);
         storage_.create(cfg);
      }

      // Create a blank navigation if needed
      PageNavigation navigation = storage_.getPageNavigation(PortalConfig.USER_TYPE, userName);
      if (navigation == null)
      {
         PageNavigation pageNav = new PageNavigation();
         pageNav.setOwnerType(PortalConfig.USER_TYPE);
         pageNav.setOwnerId(userName);
         pageNav.setPriority(5);
         pageNav.setNodes(new ArrayList<PageNode>());
         storage_.create(pageNav);
      }
   }

   /**
    * Create a group site for the specified group. It will perform the following:
    * <ul>
    * <li>create the group site by calling {@link #createUserPortalConfig(String, String, String)} which may create
    * a site or not according to the default configuration</li>
    * <li>if not site exists then it creates a site then it creates an empty site</li>
    * </ul>
    *
    * @param groupId the group id
    * @throws Exception a nasty exception
    */
   public void createGroupSite(String groupId) throws Exception
   {
      // Create the portal from the template
      createUserPortalConfig(PortalConfig.GROUP_TYPE, groupId, "group");

      // Need to insert the corresponding group site
      PortalConfig cfg = storage_.getPortalConfig(PortalConfig.GROUP_TYPE, groupId);
      if (cfg == null)
      {
         cfg = new PortalConfig(PortalConfig.GROUP_TYPE);
         cfg.setPortalLayout(new Container());
         cfg.setName(groupId);
         storage_.create(cfg);
      }
   }

   /**
    * This method should create a the portal config, pages and navigation according to the template name.
    *
    * @param siteType the site type
    * @param siteName the Site name
    * @param template   the template to use
    * @throws Exception any exception
    */
   public void createUserPortalConfig(String siteType, String siteName, String template) throws Exception
   {
      String templatePath = newPortalConfigListener_.getTemplateConfig(siteType, template);

      NewPortalConfig portalConfig = new NewPortalConfig(templatePath);
      portalConfig.setTemplateName(template);
      portalConfig.setOwnerType(siteType);

      if (!portalConfig.getOwnerType().equals(PortalConfig.USER_TYPE))
      {
         newPortalConfigListener_.createPortletPreferences(portalConfig, siteName);
      }
      newPortalConfigListener_.createPortalConfig(portalConfig, siteName);
      newPortalConfigListener_.createPage(portalConfig, siteName);
      newPortalConfigListener_.createPageNavigation(portalConfig, siteName);
   }

   /**
    * This method removes the PortalConfig, Page and PageNavigation that belong to the portal in the database.
    *
    * @param portalName the portal name
    * @throws Exception any exception
    */
   public void removeUserPortalConfig(String portalName) throws Exception
   {
      removeUserPortalConfig(PortalConfig.PORTAL_TYPE, portalName);
   }

   /**
    * This method removes the PortalConfig, Page and PageNavigation that belong to the portal in the database.
    *
    * @param ownerType the owner type
    * @param ownerId   the portal name
    * @throws Exception any exception
    */
   public void removeUserPortalConfig(String ownerType, String ownerId) throws Exception
   {
      PortalConfig config = storage_.getPortalConfig(ownerType, ownerId);
      if (config != null)
      {
         storage_.remove(config);
      }
   }

   /**
    * This method should update the PortalConfig object
    * 
    * @deprecated This method is not useful anymore. The preferred way to do this is 
    * using directly {@link org.exoplatform.portal.config.DataStorage#save(PortalConfig)}
    * 
    * @param portal
    * @throws Exception
    */
   @Deprecated
   public void update(PortalConfig portal) throws Exception
   {
      storage_.save(portal);
   }

   /**
    * This method load the page according to the pageId and returns.
    *
    * @param pageId the page id
    * @return the page
    * @throws Exception any exception
    */
   public Page getPage(String pageId) throws Exception
   {
      if (pageId == null)
      {
         return null;
      }
      return storage_.getPage(pageId); // TODO: pageConfigCache_ needs to be
   }

   /**
    * This method load the page according to the pageId and returns it if the current thread is associated with an
    * identity that allows to view the page according to the {@link UserACL#hasPermission(org.exoplatform.portal.config.model.Page)}
    * method.
    *
    * @param pageId     the page id
    * @param accessUser never used
    * @return the page
    * @throws Exception any exception
    */
   public Page getPage(String pageId, String accessUser) throws Exception
   {
      Page page = getPage(pageId);
      if (page == null || !userACL_.hasPermission(page))
      {
         return null;
      }
      return page;
   }

   /**
    * Removes a page and broadcast an event labelled as {@link org.exoplatform.portal.config.UserPortalConfigService#PAGE_REMOVED}
    * when the removal is successful.
    * 
    * @deprecated This method is not useful anymore. The preferred way to do this is 
    * using directly {@link org.exoplatform.portal.config.DataStorage#remove(Page)}

    *
    * @param page the page to remove
    * @throws Exception any exception
    */
   @Deprecated
   public void remove(Page page) throws Exception
   {
      storage_.remove(page);
   }

   /**
    * Creates a page and broadcast an event labelled as {@link org.exoplatform.portal.config.UserPortalConfigService#CREATE_PAGE_EVENT}
    * when the creation is successful.
    * 
    * @deprecated This method is not useful anymore. The preferred way to do this is 
    * using directly {@link org.exoplatform.portal.config.DataStorage#create(Page)}
    *
    * @param page the page to create
    * @throws Exception any exception
    */
   @Deprecated
   public void create(Page page) throws Exception
   {
      storage_.create(page);
   }

   /**
    * Updates a page and broadcast an event labelled as {@link org.exoplatform.portal.config.UserPortalConfigService#PAGE_UPDATED}
    * when the creation is successful.
    * 
    * @deprecated This method is not useful anymore. The preferred way to do this is 
    * using directly {@link org.exoplatform.portal.config.DataStorage#save(Page)}

    *
    * @param page the page to update
    * @return the list of model changes that occured
    * @throws Exception any exception
    */
   @Deprecated
   public List<ModelChange> update(Page page) throws Exception
   {
      List<ModelChange> changes = storage_.save(page);
      return changes;
   }

   /**
    * Creates a navigation and broadcast an event labelled as {@link org.exoplatform.portal.config.UserPortalConfigService#CREATE_NAVIGATION_EVENT}
    * when the creation is successful.
    * 
    * @deprecated This method is not useful anymore. The preferred way to do this is 
    * using directly {@link org.exoplatform.portal.config.DataStorage#create(PageNavigation)}
    *
    * @param navigation the navigation to create
    * @throws Exception any exception
    */
   @Deprecated
   public void create(PageNavigation navigation) throws Exception
   {
      storage_.create(navigation);
   }

   /**
    * Updates a page navigation broadcast an event labelled as {@link org.exoplatform.portal.config.UserPortalConfigService#NAVIGATION_UPDATED}
    * when the creation is successful.
    * 
    * @deprecated This method is not useful anymore. The preferred way to do this is 
    * using directly {@link org.exoplatform.portal.config.DataStorage#save(PageNavigation)}
    *
    * @param navigation the navigation to update
    * @throws Exception any exception
    */
   @Deprecated
   public void update(PageNavigation navigation) throws Exception
   {
      storage_.save(navigation);
   }

   /**
    * Removes a navigation and broadcast an event labelled as {@link org.exoplatform.portal.config.UserPortalConfigService#NAVIGATION_REMOVED}
    * when the removal is successful.
    * 
    * @deprecated This method is not useful anymore. The preferred way to do this is 
    * using directly {@link org.exoplatform.portal.config.DataStorage#remove(PageNavigation)}
    *
    * @param navigation the navigation to remove
    * @throws Exception any exception
    */
   @Deprecated
   public void remove(PageNavigation navigation) throws Exception
   {
      storage_.remove(navigation);
   }

   /**
    * @deprecated This method is not useful anymore. The preferred way to do this is 
    * using directly {@link org.exoplatform.portal.config.DataStorage#getPageNavigation(String, String)}
    * 
    * @param ownerType
    * @param id
    * @return
    * @throws Exception
    */
   @Deprecated
   public PageNavigation getPageNavigation(String ownerType, String id) throws Exception
   {
      PageNavigation navigation = storage_.getPageNavigation(ownerType, id);
      return navigation;
   }

   /**
    * This method creates new page from an existing page and links new page to a PageNode.
    *
    * @param nodeName
    * @param nodeLabel
    * @param pageId
    * @param ownerType
    * @param ownerId
    * @return
    * @throws Exception
    */
   public PageNode createNodeFromPageTemplate(String nodeName, String nodeLabel, String pageId, String ownerType,
                                              String ownerId) throws Exception
   {
      Page page = storage_.clonePage(pageId, nodeName, ownerType, ownerId);
      PageNode pageNode = new PageNode();
      if (nodeLabel == null || nodeLabel.trim().length() < 1)
      {
         nodeLabel = nodeName;
      }
      pageNode.setName(nodeName);
      pageNode.setLabel(nodeLabel);
      pageNode.setPageReference(page.getPageId());
      return pageNode;
   }

   /**
    * Clones a page.
    * 
    * @deprecated This method is not useful anymore. The preferred way to do this is 
    * using directly {@link org.exoplatform.portal.config.DataStorage#clonePage(String, String, String, String)}
    *
    * @param pageId    the id of the page to clone
    * @param pageName  the new page name
    * @param ownerType the new page owner type
    * @param ownerId   the new page owner id
    * @return the newly created page
    * @throws Exception any exception
    */
   @Deprecated
   public Page renewPage(String pageId, String pageName, String ownerType, String ownerId) throws Exception
   {
      return storage_.clonePage(pageId, ownerType, ownerId, pageName);
   }

   /**
    * Creates a page from an existing template.
    *
    * @param temp      the template name
    * @param ownerType the new owner type
    * @param ownerId   the new owner id
    * @return the page
    * @throws Exception any exception
    */
   public Page createPageTemplate(String temp, String ownerType, String ownerId) throws Exception
   {
      Page page = newPortalConfigListener_.createPageFromTemplate(ownerType, ownerId, temp);
      updateOwnership(page, ownerType, ownerId);
      return page;
   }

   /**
    * Load all navigation that user has edit permission.
    *
    * @return the navigation the user can edit
    * @throws Exception any exception
    */
   public List<PageNavigation> loadEditableNavigations() throws Exception
   {
      Query<PageNavigation> query = new Query<PageNavigation>(PortalConfig.GROUP_TYPE, null, PageNavigation.class);
      List<PageNavigation> navis = storage_.find(query, new Comparator<PageNavigation>()
      {
         public int compare(PageNavigation pconfig1, PageNavigation pconfig2)
         {
            return pconfig1.getOwnerId().compareTo(pconfig2.getOwnerId());
         }
      }).getAll();

      //
      List<PageNavigation> navigations = new ArrayList<PageNavigation>();
      for (PageNavigation ele : navis)
      {
         if (userACL_.hasEditPermission(ele))
         {
            navigations.add(ele);
         }
      }
      return navigations;
   }

   /**
    * Returns the list of group ids having navigation.
    *
    * @return the group id having navigation
    * @throws Exception any exception
    */
   public Set<String> findGroupHavingNavigation() throws Exception
   {
      Query<PageNavigation> query = new Query<PageNavigation>(PortalConfig.GROUP_TYPE, null, PageNavigation.class);
      Set<String> groupIds = new HashSet<String>();
      List<PageNavigation> navis = storage_.find(query).getAll();
      for (PageNavigation ele : navis)
      {
         groupIds.add(ele.getOwnerId());
      }
      return groupIds;
   }

   /**
    * Returns the list of all portal names.
    *
    * @return the list of all portal names
    * @throws Exception any exception
    */
   public List<String> getAllPortalNames() throws Exception
   {
      List<String> list = storage_.getAllPortalNames();
      for (Iterator<String> i = list.iterator();i.hasNext();)
      {
         String name = i.next();
         PortalConfig config = storage_.getPortalConfig(name);
         if (config == null || !userACL_.hasPermission(config))
         {
            i.remove();
         }
      }
      return list;
   }

   /**
    * Update the ownership recursively on the model graph.
    *
    * @param object    the model object graph root
    * @param ownerType the new owner type
    * @param ownerId   the new owner id
    */
   private void updateOwnership(ModelObject object, String ownerType, String ownerId)
   {
      if (object instanceof Container)
      {
         Container container = (Container)object;
         if (container instanceof Page)
         {
            Page page = (Page)container;
            page.setOwnerType(ownerType);
            page.setOwnerId(ownerId);
         }
         for (ModelObject child : container.getChildren())
         {
            updateOwnership(child, ownerType, ownerId);
         }
      }
      else if (object instanceof Application)
      {
         Application application = (Application)object;
         TransientApplicationState applicationState = (TransientApplicationState)application.getState();
         if (applicationState != null
            && (applicationState.getOwnerType() == null || applicationState.getOwnerId() == null))
         {
            applicationState.setOwnerType(ownerType);
            applicationState.setOwnerId(ownerId);
         }
      }
   }

   public void initListener(ComponentPlugin listener)
   {
      if (listener instanceof NewPortalConfigListener)
      {
         synchronized (this)
         {
            if (newPortalConfigListener_ == null)
            {
               this.newPortalConfigListener_ = (NewPortalConfigListener)listener;
            }
            else
            {
               newPortalConfigListener_.mergePlugin((NewPortalConfigListener)listener);
            }
         }
      }
   }

   public void start()
   {
      try
      {
         if (newPortalConfigListener_ == null)
         {
            return;
         }

         //
         RequestLifeCycle.begin(PortalContainer.getInstance());

         newPortalConfigListener_.run();
      }
      catch (Exception e)
      {
         log.error("Could not import initial data", e);

      }
      finally
      {
         RequestLifeCycle.end();
      }
   }

   public void stop()
   {
   }

   public String getDefaultPortal()
   {
      return newPortalConfigListener_.getDefaultPortal();
   }
}
