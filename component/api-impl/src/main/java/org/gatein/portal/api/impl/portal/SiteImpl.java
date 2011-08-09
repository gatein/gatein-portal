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

import org.exoplatform.portal.config.Query;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.navigation.NavigationContext;
import org.exoplatform.portal.mop.navigation.NavigationService;
import org.exoplatform.portal.mop.navigation.NavigationState;
import org.exoplatform.portal.mop.navigation.NodeModel;
import org.exoplatform.portal.mop.navigation.Scope;
import org.exoplatform.portal.pom.data.OwnerKey;
import org.exoplatform.portal.pom.data.PageData;
import org.exoplatform.portal.pom.data.PageKey;
import org.gatein.api.id.Id;
import org.gatein.api.portal.Navigation;
import org.gatein.api.portal.Page;
import org.gatein.api.portal.Site;
import org.gatein.api.util.GateInTypesResolver;
import org.gatein.api.util.HierarchicalContainer;
import org.gatein.api.util.IterableIdentifiableCollection;
import org.gatein.api.util.Type;
import org.gatein.common.NotYetImplemented;
import org.gatein.portal.api.impl.GateInImpl;
import org.gatein.portal.api.impl.IdentifiableImpl;
import org.gatein.portal.api.impl.util.AdaptedIterableIdentifiableCollection;

import java.util.List;

/** @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a> */
public abstract class SiteImpl extends IdentifiableImpl implements Site
{
   private PageContainer pageRegistry;

   public SiteImpl(Id<? extends Site> siteId, String name, GateInImpl gateIn)
   {
      super(siteId, name, gateIn);
      pageRegistry = new PageContainer(gateIn, this);
   }

   @Override
   public String toString()
   {
      return getType().getName() + "\n" + getNavigation().toString();
   }

   static Type getAPITypeFrom(OwnerKey key)
   {
      return GateInTypesResolver.forName(key.getType(), Site.class);
   }

   public HierarchicalContainer<String, Page> getPageRegistry()
   {
      return pageRegistry;
   }

   public Navigation getNavigation()
   {
      GateInImpl gateIn = getGateInImpl();
      NavigationService service = gateIn.getNavigationService();

      try
      {
         gateIn.begin();
         NavigationContext navigation = service.loadNavigation(getSiteKey());

         if (navigation != null)
         {
            NodeModel<NavigationImpl> nodeModel = new NavigationImpl.NavigationNodeModel(getId(), gateIn);

            return service.loadNode(nodeModel, navigation, Scope.CHILDREN, null).getNode();
         }
         else
         {
            return null;
         }
      }
      finally
      {
         gateIn.end();
      }
   }

   public int getPriority()
   {
      GateInImpl gateIn = getGateInImpl();
      NavigationService service = gateIn.getNavigationService();

      try
      {
         gateIn.begin();
         NavigationContext navigation = service.loadNavigation(getSiteKey());

         NavigationState state = navigation.getState();
         return state != null ? state.getPriority() : 1;
      }
      finally
      {
         gateIn.end();
      }
   }

   public Navigation createNavigationTo(Page node, Navigation parent)
   {
      throw new NotYetImplemented(); // todo
   }

   protected abstract SiteKey getSiteKey();

   static class PageContainer implements HierarchicalContainer<String, Page>
   {
      private final GateInImpl gateIn;
      private final Site site;
      private final Query<PageData> pageDataQuery;

      PageContainer(GateInImpl gateIn, SiteImpl site)
      {
         this.gateIn = gateIn;
         this.site = site;
         pageDataQuery = new Query<PageData>(site.getType().getName(), null, PageData.class);
      }

      public boolean contains(String key)
      {
         return getPageData(key) != null;
      }

      private PageData getPageData(String key)
      {
         try
         {
            gateIn.begin();
            return gateIn.getDataStorage().getPage(PageKey.create(key));
         }
         catch (Exception e)
         {
            return null;
         }
         finally
         {
            gateIn.end();
         }
      }

      public Page createAndAdd(String key)
      {
         throw new NotYetImplemented(); // todo
      }

      public Page get(String key)
      {
         final PageData pageData = getPageData(key);
         return pageData != null ? new PageImpl(pageData, site.getId(), gateIn) : null;
      }

      public Id<Page> getIdForChild(String key)
      {
         return site.getId().getIdForChild(key);
      }

      public <U extends Page> U createAndAdd(Id<U> id)
      {
         return (U)createAndAdd(id.toString());
      }

      public IterableIdentifiableCollection<Page> getAll()
      {
         final List<PageData> pageList = getAllPageData();
         return new AdaptedIterableIdentifiableCollection<PageData, Page>(pageList.size(), pageList.iterator())
         {
            public Page adapt(PageData old)
            {
               return new PageImpl(old, site.getId(), gateIn);
            }

            public boolean contains(Id<Page> t)
            {
               return getPageData(t.toString()) != null;
            }
         };
      }

      private List<PageData> getAllPageData()
      {
         try
         {
            gateIn.begin();

            return gateIn.getDataStorage().find(pageDataQuery).getAll();
         }
         catch (Exception e)
         {
            throw new RuntimeException(e);
         }
         finally
         {
            gateIn.end();
         }
      }

      public int size()
      {
         // todo: optimize
         return getAllPageData().size();
      }

      public <U extends Page> U get(Id<U> id)
      {
         return (U)get(id.toString());
      }

      public <U extends Page> boolean contains(Id<U> id)
      {
         return contains(id.toString());
      }
   }
}
