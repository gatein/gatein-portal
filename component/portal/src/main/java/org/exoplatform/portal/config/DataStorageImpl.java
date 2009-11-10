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
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.portal.application.PortletPreferences;
import org.exoplatform.portal.config.model.ApplicationState;
import org.exoplatform.portal.config.model.Container;
import org.exoplatform.portal.config.model.Dashboard;
import org.exoplatform.portal.config.model.ModelChange;
import org.exoplatform.portal.config.model.ModelObject;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.pom.config.ModelDemarcation;
import org.exoplatform.portal.pom.data.DashboardData;
import org.exoplatform.portal.pom.data.ModelData;
import org.exoplatform.portal.pom.data.ModelDataStorage;
import org.exoplatform.portal.pom.data.NavigationData;
import org.exoplatform.portal.pom.data.NavigationKey;
import org.exoplatform.portal.pom.data.PageData;
import org.exoplatform.portal.pom.data.PageKey;
import org.exoplatform.portal.pom.data.PortalData;
import org.exoplatform.portal.pom.data.PortalKey;

import java.lang.reflect.Array;
import java.util.Comparator;
import java.util.List;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class DataStorageImpl implements DataStorage, ModelDemarcation
{

   /** . */
   private ModelDataStorage delegate;

   public DataStorageImpl(ModelDataStorage delegate)
   {
      this.delegate = delegate;
   }

   public Page clonePage(String pageId, String clonedOwnerType, String clonedOwnerId, String clonedName) throws Exception
   {
      PageKey key = PageKey.create(pageId);
      PageKey cloneKey = new PageKey(clonedOwnerType, clonedOwnerId, clonedName);
      return new Page(delegate.clonePage(key, cloneKey));
   }

   public PageNavigation getPageNavigation(String ownerType, String id) throws Exception
   {
      NavigationData data = delegate.getPageNavigation(new NavigationKey(ownerType, id));
      return data != null ? new PageNavigation(data) : null;
   }

   public void remove(Page page) throws Exception
   {
      delegate.remove(page.build());
   }

   public <S> S load(ApplicationState<S> state) throws Exception
   {
      return delegate.load(state);
   }

   public void create(Page page) throws Exception
   {
      delegate.create(page.build());
   }

   public PortletPreferences getPortletPreferences(String windowID) throws Exception
   {
      return delegate.getPortletPreferences(windowID);
   }

   public <S> ApplicationState<S> save(ApplicationState<S> state, S preferences) throws Exception
   {
      return delegate.save(state, preferences);
   }

   public Container getSharedLayout() throws Exception
   {
      return delegate.getSharedLayout();
   }

   public void save(PortalConfig config) throws Exception
   {
      delegate.save(config.build());
   }

   public void create(PortalConfig config) throws Exception
   {
      delegate.create(config.build());
   }

   public PortalConfig getPortalConfig(String portalName) throws Exception
   {
      return getPortalConfig(PortalConfig.PORTAL_TYPE, portalName);
   }

   public void save(PageNavigation navigation) throws Exception
   {
      delegate.save(navigation.build());
   }

   public void remove(PortalConfig config) throws Exception
   {
      delegate.remove(config.build());
   }

   public PageNavigation getPageNavigation(String fullId) throws Exception
   {
      NavigationKey key = NavigationKey.create(fullId);
      NavigationData data = delegate.getPageNavigation(key);
      return data != null ? new PageNavigation(data) : null;
   }

   public Page getPage(String pageId) throws Exception
   {
      PageKey key = PageKey.create(pageId);
      PageData data = delegate.getPage(key);
      return data != null ? new Page(data) : null;
   }

   public List<ModelChange> save(Page page) throws Exception
   {
      return delegate.save(page.build());
   }

   public void create(PageNavigation navigation) throws Exception
   {
      delegate.save(navigation.build());
   }

   private abstract class Bilto<O extends ModelObject, D extends ModelData>
   {

      final Query<O> q;

      final Class<D> dataType;

      Bilto(Query<O> q, Class<D> dataType)
      {
         this.q = q;
         this.dataType = dataType;
      }

      protected abstract O create(D d);

      LazyPageList<O> execute() throws Exception
      {
         Query<D> delegateQ = new Query<D>(q, dataType);
         LazyPageList<D> r = delegate.find(delegateQ, null);
         final List<D> list = r.getAll();
         ListAccess<O> access = new ListAccess<O>()
         {
            public int getSize() throws Exception
            {
               return list.size();
            }
            public O[] load(int index, int length) throws Exception, IllegalArgumentException
            {
               O[] pages = (O[])Array.newInstance(q.getClassType(), length);
               int i = 0;
               for (D data : list.subList(index, index + length))
               {
                  pages[i++] = create(data);
               }
               return pages;
            }
         };
         return new LazyPageList<O>(access, r.getPageSize());
      }

   }

   public <T> LazyPageList<T> find(Query<T> q, Comparator<T> sortComparator) throws Exception
   {
      Class<T> type = q.getClassType();
      if (type == Page.class)
      {
         Bilto<Page, PageData> bilto = new Bilto<Page, PageData>((Query<Page>)q, PageData.class)
         {
            @Override
            protected Page create(PageData pageData)
            {
               return new Page(pageData);
            }
         };
         return (LazyPageList<T>)bilto.execute();
      }
      else if (type == PageNavigation.class)
      {
         Bilto<PageNavigation, NavigationData> bilto = new Bilto<PageNavigation, NavigationData>((Query<PageNavigation>)q, NavigationData.class)
         {
            @Override
            protected PageNavigation create(NavigationData page)
            {
               return new PageNavigation(page);
            }
         };
         return (LazyPageList<T>)bilto.execute();
      }
      else if (type == PortalConfig.class)
      {
         Bilto<PortalConfig, PortalData> bilto = new Bilto<PortalConfig, PortalData>((Query<PortalConfig>)q, PortalData.class)
         {
            @Override
            protected PortalConfig create(PortalData portalData)
            {
               return new PortalConfig(portalData);
            }
         };
         return (LazyPageList<T>)bilto.execute();
      }
      else
      {
         throw new UnsupportedOperationException("Cannot query type " + type);
      }
   }

   public <T> LazyPageList<T> find(Query<T> q) throws Exception
   {
      return find(q, null);
   }

   public <S> String getId(ApplicationState<S> state) throws Exception
   {
      return delegate.getId(state);
   }

   public void save(PortletPreferences portletPreferences) throws Exception
   {
      delegate.save(portletPreferences);
   }

   public PortalConfig getPortalConfig(String ownerType, String portalName) throws Exception
   {
      PortalKey key = new PortalKey(ownerType, portalName);
      PortalData data = delegate.getPortalConfig(key);
      return data != null ? new PortalConfig(data) : null;
   }

   public void remove(PageNavigation navigation) throws Exception
   {
      delegate.remove(navigation.build());
   }

   public Dashboard loadDashboard(String dashboardId) throws Exception
   {
      DashboardData data = delegate.loadDashboard(dashboardId);
      return data != null ? new Dashboard(data) : null;
   }

   public void saveDashboard(Dashboard dashboard) throws Exception
   {
      delegate.saveDashboard(dashboard.build());
   }

   public void begin()
   {
      if (delegate instanceof ModelDemarcation)
      {
         ((ModelDemarcation)delegate).begin();
      }
   }

   public void end(boolean save)
   {
      if (delegate instanceof ModelDemarcation)
      {
         ((ModelDemarcation)delegate).end(save);
      }
   }
}
