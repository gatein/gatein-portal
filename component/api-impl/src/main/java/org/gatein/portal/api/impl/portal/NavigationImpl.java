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

package org.gatein.portal.api.impl.portal;

import org.exoplatform.commons.utils.ExpressionUtil;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.mop.Described;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.mop.description.DescriptionService;
import org.exoplatform.portal.mop.navigation.NavigationService;
import org.exoplatform.portal.mop.navigation.NodeContext;
import org.exoplatform.portal.mop.navigation.NodeModel;
import org.exoplatform.portal.mop.navigation.NodeState;
import org.exoplatform.portal.mop.navigation.Scope;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.resources.ResourceBundleManager;
import org.exoplatform.web.url.navigation.NavigationResource;
import org.exoplatform.web.url.navigation.NodeURL;
import org.gatein.api.GateIn;
import org.gatein.api.id.Id;
import org.gatein.api.id.Identifiable;
import org.gatein.api.portal.Navigation;
import org.gatein.api.portal.Page;
import org.gatein.api.portal.Site;
import org.gatein.api.util.IterableIdentifiableCollection;
import org.gatein.common.NotYetImplemented;
import org.gatein.common.text.EntityEncoder;
import org.gatein.common.util.EmptyResourceBundle;
import org.gatein.portal.api.impl.GateInImpl;
import org.gatein.portal.api.impl.util.AdaptedIterableIdentifiableCollection;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Locale;
import java.util.ResourceBundle;

/** @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a> */
public class NavigationImpl implements Navigation, Identifiable<Navigation>
{
   private final NodeContext<NavigationImpl> context;
   private Id<? extends Site> site;
   private Id<Navigation> id;
   private final GateInImpl gateIn;
   private URI uri;
   private String displayName;
   private ResourceBundle bundle;

   public NavigationImpl(Id<Site> siteId, NodeContext<NavigationImpl> context, GateInImpl gateIn)
   {
      this.context = context;
      this.site = siteId;
      this.gateIn = gateIn;
   }

   @Override
   public String toString()
   {
      String pageRef = context.getState().getPageRef();
      StringBuilder s = new StringBuilder("Navigation@").append(getId()).append(" URI: ").append(getURI());

      if (pageRef != null)
      {
         s.append("-target->").append(pageRef);
      }

      if (size() != 0)
      {
         loadChildrenIfNeeded();
         s.append("\n|");
         Iterator<NavigationImpl> children = context.iterator();
         while (children.hasNext())
         {
            s.append("\n+--").append(children.next());
         }
         s.append("\n");
      }

      return s.toString();
   }

   public NodeContext<NavigationImpl> getContext()
   {
      return context;
   }

   public URI getURI()
   {
      if (uri != null)
      {
         return uri;
      }
      else
      {
         try
         {
            PortalRequestContext requestContext = Util.getPortalRequestContext();
            SiteType siteType = SiteType.valueOf(site.getComponents()[0].toUpperCase());
            String siteName = site.getComponents()[1];

            NavigationResource navResource = new NavigationResource(siteType, siteName, buildURI().toString());            
            NodeURL nodeURL = requestContext.createURL(NodeURL.TYPE, navResource);
            nodeURL.setSchemeUse(true);

            uri = new URI(nodeURL.toString());
         }
         catch (URISyntaxException e)
         {
            throw new RuntimeException(e);
         }
         return uri;
      }
   }


   private StringBuilder buildURI()
   {
      NavigationImpl parent = context.getParentNode();
      if (parent != null)
      {
         StringBuilder builder = parent.buildURI();
         if (builder.length() > 0)
         {
            builder.append('/');
         }
         return builder.append(context.getName());
      }
      else
      {
         return new StringBuilder();
      }
   }


   public Page getTargetPage()
   {
      String pageRef = context.getState().getPageRef();
      if (pageRef != null)
      {
         return gateIn.get(gateIn.pageId(site, pageRef));
      }
      else
      {
         return null;
      }
   }

   public void setTargetPage(Page target)
   {
      setTargetPage(target.getId());
   }

   public void setTargetPage(Id<Page> targetId)
   {
      context.setState(new NodeState.Builder(context.getState()).pageRef(targetId.toString()).build());
   }

   public Site getSite()
   {
      return getGateIn().get(site);
   }

   public IterableIdentifiableCollection<Navigation> getAll()
   {
      loadChildrenIfNeeded();

      return new AdaptedIterableIdentifiableCollection<NavigationImpl, Navigation>(size(), context.iterator())
      {
         public Navigation adapt(NavigationImpl old)
         {
            return old;
         }

         public boolean contains(Id<Navigation> id)
         {
            return NavigationImpl.this.contains(id);
         }
      };
   }

   private void loadChildrenIfNeeded()
   {
      if (!context.isExpanded())
      {
         NavigationService service = gateIn.getNavigationService();
         try
         {
            gateIn.begin();
            service.rebaseNode(context, Scope.CHILDREN, null);
         }
         finally
         {
            gateIn.end();
         }
      }
   }

   public int size()
   {
      return context.getNodeSize();
   }

   public boolean contains(String key)
   {
      return contains(getIdForChild(key));
   }

   public <U extends Navigation> boolean contains(Id<U> navigationId)
   {
      loadChildrenIfNeeded();

      return getChild(navigationId) != null;
   }

   private <U extends Navigation> NodeContext<NavigationImpl> getChild(Id<U> navigationId)
   {
      return context.get(navigationId.getComponent(GateInImpl.NAVIGATION_COMPONENT));
   }

   public Navigation createAndAdd(String key)
   {
      return createAndAdd(getIdForChild(key));
   }

   public <U extends Navigation> U createAndAdd(Id<U> navigationId)
   {
      throw new NotYetImplemented(); // TODO
   }

   public Navigation get(String key)
   {
      return get(getIdForChild(key));
   }

   public <U extends Navigation> U get(Id<U> navigationId)
   {
      if (navigationId == null)
      {
         return null;
      }
      else
      {
         loadChildrenIfNeeded();
         final Class<U> type = navigationId.getIdentifiableType();
         final NodeContext<NavigationImpl> child = getChild(navigationId);
         return child != null ? type.cast(child.getNode()) : null;
      }
   }

   public Id<Navigation> getIdForChild(String key)
   {
      return site.getIdForChild(key);
   }

   public Id<Navigation> getId()
   {
      if (id == null)
      {
         id = GateInImpl.NAVIGATION_CONTEXT.create(Navigation.class, site.toString(), context.getId());
      }

      return id;
   }

   public String getName()
   {
      return context.getName();
   }

   public String getDisplayName()
   {
      // basically duplicating code from UserNode and PortalRequestContext because we can't use it as is
      if (displayName == null)
      {
         String resolvedLabel = null;

         String id = context.getId();

         if (context.getState().getLabel() != null)
         {
            resolvedLabel = ExpressionUtil.getExpressionValue(getBundle(), context.getState().getLabel());
         }
         else if (id != null)
         {
            Locale userLocale = getUserLocale();
            DescriptionService descriptionService = gateIn.getDescriptionService();
            Described.State description = descriptionService.resolveDescription(id, userLocale);
            if (description != null)
            {
               resolvedLabel = description.getName();
            }
         }

         //
         if (resolvedLabel == null)
         {
            resolvedLabel = getName();
         }

         //
         this.displayName = EntityEncoder.FULL.encode(resolvedLabel);
      }
      return displayName;
   }

   public Locale getUserLocale()
   {
      return Util.getPortalRequestContext().getLocale();
   }


   public ResourceBundle getBundle()
   {
      if (bundle == null)
      {
         ExoContainer container = ExoContainerContext.getCurrentContainer();
         ResourceBundleManager rbMgr = (ResourceBundleManager)container.getComponentInstanceOfType(ResourceBundleManager.class);
         Locale locale = Util.getPortalRequestContext().getLocale();
         bundle = rbMgr.getNavigationResourceBundle(
            locale.getLanguage(),
            site.getComponent(GateInImpl.SITE_TYPE_COMPONENT),
            site.getComponent(GateInImpl.SITE_NAME_COMPONENT));

         if (bundle == null)
         {
            bundle = EmptyResourceBundle.INSTANCE;
         }
      }
      return bundle;
   }


   public GateIn getGateIn()
   {
      return gateIn;
   }

   static class NavigationNodeModel implements NodeModel<NavigationImpl>
   {
      private final Id<Site> siteId;
      private final GateInImpl gateIn;

      NavigationNodeModel(Id<Site> siteId, GateInImpl gateIn)
      {
         this.siteId = siteId;
         this.gateIn = gateIn;
      }

      public NodeContext<NavigationImpl> getContext(NavigationImpl node)
      {
         return node.context;
      }

      public NavigationImpl create(NodeContext<NavigationImpl> context)
      {
         return new NavigationImpl(siteId, context, gateIn);
      }
   }
}
