/*
* JBoss, a division of Red Hat
* Copyright 2008, Red Hat Middleware, LLC, and individual contributors as indicated
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
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

package org.gatein.portal.api.impl;

import org.exoplatform.application.gadget.GadgetRegistryService;
import org.exoplatform.application.gadget.SourceStorage;
import org.exoplatform.application.registry.Application;
import org.exoplatform.application.registry.ApplicationRegistryService;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.portal.config.Query;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.ApplicationType;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.mop.description.DescriptionService;
import org.exoplatform.portal.mop.navigation.NavigationService;
import org.exoplatform.portal.pc.ExoKernelIntegration;
import org.exoplatform.portal.pom.data.ModelDataStorage;
import org.exoplatform.portal.pom.data.PageData;
import org.exoplatform.portal.pom.data.PageKey;
import org.exoplatform.portal.pom.data.PortalData;
import org.exoplatform.portal.pom.data.PortalKey;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.GroupHandler;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.MembershipEntry;
import org.gatein.api.GateIn;
import org.gatein.api.content.Category;
import org.gatein.api.content.Content;
import org.gatein.api.content.Gadget;
import org.gatein.api.content.ManagedContent;
import org.gatein.api.content.Portlet;
import org.gatein.api.content.WSRP;
import org.gatein.api.id.Context;
import org.gatein.api.id.GenericContext;
import org.gatein.api.id.Id;
import org.gatein.api.id.Identifiable;
import org.gatein.api.portal.Navigation;
import org.gatein.api.portal.Page;
import org.gatein.api.portal.Portal;
import org.gatein.api.portal.Site;
import org.gatein.api.util.Filter;
import org.gatein.api.util.IterableIdentifiableCollection;
import org.gatein.api.util.Type;
import org.gatein.common.util.ParameterValidation;
import org.gatein.pc.federation.FederatingPortletInvoker;
import org.gatein.portal.api.impl.id.ComplexApplicationContext;
import org.gatein.portal.api.impl.id.ComplexApplicationId;
import org.gatein.portal.api.impl.portal.DashboardSiteImpl;
import org.gatein.portal.api.impl.portal.GroupSiteImpl;
import org.gatein.portal.api.impl.portal.PageImpl;
import org.gatein.portal.api.impl.portal.PortalImpl;
import org.gatein.portal.api.impl.util.AdaptedIterableIdentifiableCollection;
import org.gatein.portal.api.impl.util.AggregatedIterableIdentifiableCollection;
import org.picocontainer.Startable;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/** @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a> */
public class GateInImpl implements GateIn, Startable, LifecycleManager
{
   private static final Query<PortalData> PORTALS = new Query<PortalData>(SiteType.PORTAL.getName(), null, PortalData.class);

   private static final String GROUP_CHARS = "\\w|-|_";

   public static final String SITE_TYPE_COMPONENT = "type";
   public static final String SITE_NAME_COMPONENT = "name";
   public static final String APPLICATION_COMPONENT = "application";
   public static final String PORTLET_COMPONENT = "portlet";
   public static final String INVOKER_COMPONENT = "invoker";
   public static final String CATEGORY_COMPONENT = "category";

   public static final Pattern INVOKER_COMPONENT_PATTERN = Pattern.compile("\\w+");

   public static final Context LOCAL_PORTLET_CONTEXT = GenericContext.builder().named("Local Portlet")
      .requiredComponent(APPLICATION_COMPONENT, Identifiable.class, Pattern.compile("(" + GROUP_CHARS + "|\\.)+"))
      .requiredComponent(PORTLET_COMPONENT, Portlet.class, Pattern.compile("\\w+"))
      .withDefaultSeparator("/").build();
   private static final Context WSRP_PORTLET_CONTEXT = GenericContext.builder().named("WSRP Portlet")
      .requiredComponent(INVOKER_COMPONENT, Identifiable.class, INVOKER_COMPONENT_PATTERN)
      .requiredComponent("portletcontext", WSRP.class, Pattern.compile("(" + GROUP_CHARS + ")+"))
      .withDefaultSeparator(".").build();

   public static final Context SITE_CONTEXT = GenericContext.builder().named("Site")
      .requiredComponent(SITE_TYPE_COMPONENT, Identifiable.class, Pattern.compile(Site.PORTAL_TYPE_NAME + "|" + Site.GROUP_TYPE_NAME + "|" + Site.DASHBOARD_TYPE_NAME))
      .requiredComponent(SITE_NAME_COMPONENT, Site.class, Pattern.compile("(" + GROUP_CHARS + "|\\/)+"))
      .optionalComponent("page", Page.class, Pattern.compile("\\w+"))
      .withDefaultSeparator("::").build();
   public static final Context GROUP_CONTEXT = GenericContext.builder().named("Group")
      .requiredUnboundedHierarchicalComponent("group", Identifiable.class, Pattern.compile("(" + GROUP_CHARS + ")+"))
      .withDefaultSeparator("/").requireSeparatorInFirstPosition().build();
   private static final Context GADGET_CONTEXT = GenericContext.builder().named("Gadget")
      .requiredComponent("name", Gadget.class, Pattern.compile("\\w+")).build();
   private static final Context USER_CONTEXT = GenericContext.builder().named("User")
      .requiredComponent("name", Identifiable.class, Pattern.compile("[a-zA-Z0-9]+")).build();
   private static final Context CATEGORY_CONTEXT = GenericContext.builder().named("Category")
      .requiredComponent("name", Category.class, Pattern.compile("\\w+")).build();
   public static final Context MANAGED_CONTENT_CONTEXT = GenericContext.builder().named("ManagedContent")
      .requiredComponent(CATEGORY_COMPONENT, Category.class, Pattern.compile("\\w+"))
      .requiredComponent("name", ManagedContent.class, Pattern.compile("(" + GROUP_CHARS + "|\\.)+"))
      .requiredComponent("content", Content.class, Pattern.compile(".+"))
      .withDefaultSeparator(":")
      .build();
   public static final String NAVIGATION_COMPONENT = "navigation";
   public static final String NAVIGATION_SITE_COMPONENT = "site id";
   public final static Context NAVIGATION_CONTEXT = GenericContext.builder().named("Navigation")
      .requiredComponent(NAVIGATION_SITE_COMPONENT, Site.class, Pattern.compile("(" + GROUP_CHARS + "|::|\\/)+"))
      .requiredComponent(NAVIGATION_COMPONENT, Navigation.class, Pattern.compile("[a-z0-9]+"))
      .withDefaultSeparator("@")
      .build();

   private ExoContainer container;
   private ModelDataStorage dataStorage;
   private ApplicationRegistryService registryService;
   private GadgetRegistryService gadgetService;
   private SourceStorage sourceStorage;
   private UserPortalConfigService configService;
   private Map<Type, Object> properties = new HashMap<Type, Object>(7);
   private LifecycleManager lcManager = GateIn.NO_OP_MANAGER;
   private FederatingPortletInvoker portletInvoker;

   public GateInImpl(ExoContainerContext context, InitParams params, ConfigurationManager configurationManager, ExoKernelIntegration exoKernelIntegration)
   {
      container = context.getContainer();
   }

   public static Id<? extends Content> getContentIdFrom(Application application)
   {
      ApplicationType type = application.getType();
      Class<Content> contentClass = getContentClassFor(type);
      if (Gadget.class.isAssignableFrom(contentClass))
      {
         return staticGadgetId(application.getContentId());
      }
      else if (WSRP.class.isAssignableFrom(contentClass))
      {
         return parseWSRPPortletId(application.getContentId());
      }
      else if (Portlet.class.isAssignableFrom(contentClass))
      {
         return parsePortletId(application.getContentId());
      }
      else
      {
         throw new IllegalArgumentException("Unknown application type: " + type);
      }
   }

   public static <T extends Content> Class<T> getContentClassFor(ApplicationType type)
   {
      if (ApplicationType.GADGET.equals(type))
      {
         return (Class<T>)Gadget.class;
      }
      else if (ApplicationType.PORTLET.equals(type))
      {
         return (Class<T>)Portlet.class;
      }
      else if (ApplicationType.WSRP_PORTLET.equals(type))
      {
         return (Class<T>)WSRP.class;
      }
      else
      {
         throw new IllegalArgumentException("Unknown ApplicationType: " + type);
      }
   }

   public <T> T getProperty(Type<T> property)
   {
      if (property == null)
      {
         return null;
      }

      Class<T> type = property.getValueType();
      Object o = properties.get(property);
      return type.cast(o);
   }

   public <T> void setProperty(Type<T> property, T value)
   {
      if (property != null)
      {
         if (GateIn.LIFECYCLE_MANAGER.equals(property))
         {
            lcManager = GateIn.LIFECYCLE_MANAGER.getValueType().cast(value);
         }
         properties.put(property, value);
      }
   }

   public IterableIdentifiableCollection<Portal> getPortals()
   {
      try
      {
         begin();
         final List<PortalData> portals = dataStorage.find(PORTALS).getAll();

         return new AdaptedIterableIdentifiableCollection<PortalData, Portal>(portals.size(), portals.iterator())
         {
            public Portal adapt(PortalData old)
            {
               return new PortalImpl(old, GateInImpl.this);
            }

            public boolean contains(Id<Portal> id)
            {
               return getPortalDataFor(id) != null;
            }
         };
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
      finally
      {
         end();
      }
   }

   public Portal getPortal(Id<Portal> portalId)
   {
      PortalData portalData = getPortalDataFor(portalId);
      return new PortalImpl(portalData, this);
   }

   public Portal getDefaultPortal()
   {
      return getPortal(siteId(Site.PORTAL, "classic")); // todo: check
   }

   public IterableIdentifiableCollection<? extends Site> getSites()
   {
      IterableIdentifiableCollection<Site> groupSites = getGroupSites();
      IterableIdentifiableCollection<Portal> portals = getPortals();
      IterableIdentifiableCollection<Site> dashboards = getDashboards();

      AggregatedIterableIdentifiableCollection collection = new AggregatedIterableIdentifiableCollection();
      collection.addCollection(groupSites);
      collection.addCollection(portals);
      collection.addCollection(dashboards);

      return collection;
   }

   private IterableIdentifiableCollection<Site> getDashboards()
   {
      try
      {
         begin();
         final UserHandler userHandler = getOrganizationService().getUserHandler();

         // todo: optimize
         List<User> users = userHandler.getUserPageList(1000).getAll();

         // todo: check for correctness
         return new AdaptedIterableIdentifiableCollection<User, Site>(users.size(), users.iterator())
         {

            public Site adapt(User old)
            {
               return getDashboard(userId(old.getUserName()));
            }

            public boolean contains(Id<Site> t)
            {
               try
               {
                  return dataStorage.loadDashboard(t.toString()) != null;
               }
               catch (Exception e)
               {
                  return false;
               }
            }
         };
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
      finally
      {
         end();
      }
   }

   public IterableIdentifiableCollection<Site> getGroupSites()
   {
      try
      {
         begin();

         final GroupHandler groupHandler = getOrganizationService().getGroupHandler();
         Collection groups = groupHandler.getAllGroups();

         return new AdaptedIterableIdentifiableCollection<Object, Site>(groups.size(), groups.iterator())
         {
            public boolean contains(Id<Site> siteId)
            {
               try
               {
                  return groupHandler.findGroupById(siteId.toString()) != null;
               }
               catch (Exception e)
               {
                  return false;
               }
            }

            public Site adapt(Object old)
            {
               Group group = (Group)old;
               return getGroupSite(GROUP_CONTEXT.parse(group.getId()));
            }
         };
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
      finally
      {
         end();
      }
   }

   public Site getGroupSite(Id groupId)
   {
      String groupName = groupId.toString();
      Id<Site> siteId = siteId(Site.GROUP, groupName);

      return new GroupSiteImpl(siteId, groupName, this);
   }

   public IterableIdentifiableCollection<Site> getGroupSites(Id userId)
   {
      final GroupHandler groupHandler = getOrganizationService().getGroupHandler();
      try
      {
         begin();
         final String id = userId.toString();
         Collection groups = groupHandler.findGroupsOfUser(id);

         return new AdaptedIterableIdentifiableCollection<Object, Site>(groups.size(), groups.iterator())
         {
            public boolean contains(Id<Site> siteId)
            {
               try
               {
                  Group group = groupHandler.findGroupById(siteId.toString());
                  return group != null && !groupHandler.findGroupByMembership(id, null).isEmpty();
               }
               catch (Exception e)
               {
                  return false;
               }
            }

            public Site adapt(Object old)
            {
               Group group = (Group)old;
               return getGroupSite(GROUP_CONTEXT.parse(group.getId()));
            }
         };
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
      finally
      {
         end();
      }
   }

   public IterableIdentifiableCollection<Portal> getPortalSites(Id userId)
   {
      try
      {
         begin();
         final List<PortalData> portalDatas = dataStorage.find(PORTALS).getAll();

         // first build Identity based on user id so that we can check its permissions using UserACL... ugh! :(
         final String user = userId.toString();
         final Collection membershipsByUser = getOrganizationService().getMembershipHandler().findMembershipsByUser(user);
         Collection<MembershipEntry> membershipEntries = new ArrayList<MembershipEntry>(membershipsByUser.size());
         for (Object o : membershipsByUser)
         {
            Membership membership = (Membership)o;
            membershipEntries.add(new MembershipEntry(membership.getGroupId(), membership.getMembershipType()));
         }
         final Identity identity = new Identity(user, membershipEntries);

         final List<Portal> portals = new ArrayList<Portal>(portalDatas.size());
         final Filter<PortalData> filter = new Filter<PortalData>()
         {
            @Override
            public boolean accept(PortalData item)
            {
               return getUserACL().hasPermission(identity, new PortalConfig(item));
            }
         };

         for (PortalData portalData : portalDatas)
         {
            if (filter.accept(portalData))
            {
               portals.add(new PortalImpl(portalData, this));
            }
         }

         return new AdaptedIterableIdentifiableCollection<Portal, Portal>(portals.size(), portals.iterator())
         {
            public Portal adapt(Portal old)
            {
               return old;
            }

            public boolean contains(Id<Portal> id)
            {
               final PortalData portalData = getPortalDataFor(id);
               return portalData != null && filter.accept(portalData);
            }
         };
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
      finally
      {
         end();
      }
   }

   public Site getDashboard(Id userId)
   {
      String user = userId.toString();
      Id<Site> siteId = siteId(Site.DASHBOARD, user);

      return new DashboardSiteImpl(siteId, user, this);
   }

   public <T extends Identifiable> T get(Id<T> id)
   {
      Class<T> type = id.getIdentifiableType();

      Object result = null;

      if (Portal.class.equals(type))
      {
         result = getPortal((Id<Portal>)id);
      }
      else if (Page.class.equals(type))
      {
         try
         {
            begin();
            PageData pageData = dataStorage.getPage(PageKey.create(id.toString()));
            result = new PageImpl(pageData, id.getParent(), this);
         }
         catch (Exception e)
         {
            throw new RuntimeException(e);
         }
         finally
         {
            end();
         }
      }
      else if (Site.class.equals(type))
      {
         Id<Site> siteId = (Id<Site>)id;
         result = getSite(siteId, null);
      }
      else if (Content.class.isAssignableFrom(type))
      {
         // todo: split by types and optimize by calling portlet invoker or gadget registry directly
         final IterableIdentifiableCollection<Portal> portals = getPortals();
         for (Portal portal : portals)
         {
            result = portal.getContentRegistry().get((Id<? extends Content>)id);
            if (result != null)
            {
               break;
            }
         }
      }
      else if (Category.class.equals(type))
      {
         // todo: optimize by adding portal id to category id (so that appropriate content registry can be retrieved) and calling application registry directly
         final IterableIdentifiableCollection<Portal> portals = getPortals();
         for (Portal portal : portals)
         {
            result = portal.getContentRegistry().getCategory(id.toString());
            if (result != null)
            {
               break;
            }
         }
      }
      else if (ManagedContent.class.equals(type))
      {
         final String categoryId = id.getComponent(CATEGORY_COMPONENT);
         final Category category = get(categoryId(categoryId));
         result = category.getManagedContent(id.getComponent("name"));
      }
      else if (Navigation.class.equals(type))
      {
         final String siteStringId = id.getComponent(NAVIGATION_SITE_COMPONENT);
         final Id<Site> siteId = SITE_CONTEXT.parse(siteStringId, Site.class);
         final Site site = getSite(siteId, null);
         //todo: need to get all the parents of a Navigation to be able to load it so we would need to add the complete path to the Id to be able to resolve Navigations properly
         result = site.getNavigation().get(id.getComponent(NAVIGATION_COMPONENT));

         throw new UnsupportedOperationException("Id<" + type.getSimpleName() + "> not yet supported");
      }
      else
      {
         throw new UnsupportedOperationException("Id<" + type.getSimpleName() + "> not yet supported");
      }

      return type.cast(result);
   }

   public <T extends Site> T getSite(Id<T> siteId, Type<T> type)
   {
      final String siteType = siteId.getComponent(SITE_TYPE_COMPONENT);
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(siteType, "Site type", "A valid Site Id");

      if (Site.PORTAL.equals(siteType))
      {
         return (T)getPortal((Id<Portal>)siteId);
      }
      else if (Site.GROUP_TYPE_NAME.equals(siteType))
      {
         return (T)getGroupSite(groupId(siteId.getComponent(SITE_NAME_COMPONENT)));
      }
      else if (Site.DASHBOARD_TYPE_NAME.equals(siteType))
      {
         return (T)getDashboard(userId(siteId.getComponent(SITE_NAME_COMPONENT)));
      }
      else
      {
         throw new IllegalArgumentException("Unknown Site type: " + siteType);
      }
   }

   public Id userId(String user)
   {
      return USER_CONTEXT.create(user);
   }

   public Id groupId(String root, String... children)
   {
      return GROUP_CONTEXT.create(root, children);
   }

   public Id<Portlet> portletId(String application, String portlet)
   {
      return LOCAL_PORTLET_CONTEXT.create(Portlet.class, application, portlet);
   }

   public static Id<Portlet> parsePortletId(String contentId)
   {
      if (contentId.contains(ComplexApplicationId.START))
      {
         return ComplexApplicationContext.INSTANCE.parse(contentId, Portlet.class);
      }
      else
      {
         return LOCAL_PORTLET_CONTEXT.parse(contentId, Portlet.class);
      }
   }

   public Id<WSRP> wsrpPortletId(String invoker, String portlet)
   {
      return staticWSRPPortletId(invoker, portlet);
   }

   private static Id<WSRP> staticWSRPPortletId(String invoker, String portlet)
   {
      return WSRP_PORTLET_CONTEXT.create(WSRP.class, invoker, portlet);
   }

   public static Id<WSRP> parseWSRPPortletId(String compositeId)
   {
      return WSRP_PORTLET_CONTEXT.parse(compositeId, WSRP.class);
   }

   public Id<Gadget> gadgetId(String gadgetName)
   {
      return staticGadgetId(gadgetName);
   }

   private static Id<Gadget> staticGadgetId(String gadgetName)
   {
      return GADGET_CONTEXT.create(Gadget.class, gadgetName);
   }

   public Id<Gadget> gadgetId(URI uri)
   {
      return GADGET_CONTEXT.create(Gadget.class, uri.toString());
   }

   public <T extends Content> Id<ManagedContent> managedContentId(Id<Category> categoryId, String name, Id<T> contentId)
   {
      return MANAGED_CONTENT_CONTEXT.create(ManagedContent.class, categoryId.toString(), name, contentId.toString());
   }

   public Id<Category> categoryId(String name)
   {
      return CATEGORY_CONTEXT.create(Category.class, name);
   }

   public <T extends Site> Id<T> siteId(Type<T> siteType, String siteName)
   {
      return SITE_CONTEXT.create(siteType.getValueType(), siteType.getName(), siteName);
   }

   public <T extends Site> Id<Page> pageId(Id<T> ownerSite, String pageName)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(ownerSite, "Owner Site Id");
      return ownerSite.getIdForChild(pageName);
   }

   public void start()
   {
      dataStorage = (ModelDataStorage)container.getComponentInstanceOfType(ModelDataStorage.class);
      registryService = (ApplicationRegistryService)container.getComponentInstanceOfType(ApplicationRegistryService.class);
      gadgetService = (GadgetRegistryService)container.getComponentInstanceOfType(GadgetRegistryService.class);
      sourceStorage = (SourceStorage)container.getComponentInstanceOfType(SourceStorage.class);
      configService = (UserPortalConfigService)container.getComponentInstanceOfType(UserPortalConfigService.class);
      portletInvoker = (FederatingPortletInvoker)container.getComponentInstanceOfType(FederatingPortletInvoker.class);
   }

   public void stop()
   {
      // nothing to do
   }

   public ModelDataStorage getDataStorage()
   {
      return dataStorage;
   }

   public NavigationService getNavigationService()
   {
      return configService.getNavigationService();
   }

   public ApplicationRegistryService getRegistryService()
   {
      return registryService;
   }

   public SourceStorage getSourceStorage()
   {
      return sourceStorage;
   }

   public FederatingPortletInvoker getPortletInvoker()
   {
      return portletInvoker;
   }

   private PortalData getPortalDataFor(Id<Portal> portalId)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(portalId, "Portal Id");
      try
      {
         begin();

         return dataStorage.getPortalConfig(PortalKey.create(portalId.toString()));
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
      finally
      {
         end();
      }
   }

   public void begin()
   {
      lcManager.begin();
   }

   public void end()
   {
      lcManager.end();
   }

   public GadgetRegistryService getGadgetService()
   {
      return gadgetService;
   }

   public OrganizationService getOrganizationService()
   {
      return configService.getOrganizationService();
   }

   public UserACL getUserACL()
   {
      return configService.getUserACL();
   }

   public DescriptionService getDescriptionService()
   {
      return configService.getDescriptionService();
   }
}
