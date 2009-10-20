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

package org.exoplatform.portal.pom.config;

import org.exoplatform.commons.utils.IOUtil;
import org.exoplatform.commons.utils.LazyPageList;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.portal.application.PortletPreferences;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.Query;
import org.exoplatform.portal.config.model.Application;
import org.exoplatform.portal.config.model.ApplicationState;
import org.exoplatform.portal.config.model.Container;
import org.exoplatform.portal.config.model.ModelChange;
import org.exoplatform.portal.config.model.ModelObject;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PersistentApplicationState;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.config.model.TransientApplicationState;
import org.exoplatform.portal.pom.config.tasks.PageNavigationTask;
import org.exoplatform.portal.pom.config.tasks.PageTask;
import org.exoplatform.portal.pom.config.tasks.PortalConfigTask;
import org.exoplatform.portal.pom.config.tasks.PortletPreferencesTask;
import org.exoplatform.portal.pom.config.tasks.PreferencesTask;
import org.exoplatform.portal.pom.config.tasks.SearchTask;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IUnmarshallingContext;

import java.io.ByteArrayInputStream;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class POMDataStorage implements DataStorage
{

   /** . */
   private final POMSessionManager pomMgr;

   private ConfigurationManager confManager_;

   public POMDataStorage(POMSessionManager pomMgr, ConfigurationManager confManager)
   {
      this.pomMgr = pomMgr;
      confManager_ = confManager;
   }

   public <T extends POMTask> T execute(T task) throws Exception
   {
      pomMgr.execute(task);
      return task;
   }

   public POMSessionManager getPOMSessionManager()
   {
      return pomMgr;
   }

   public PortalConfig getPortalConfig(String portalName) throws Exception
   {
      return execute(new PortalConfigTask.Load(PortalConfig.PORTAL_TYPE, portalName)).getConfig();
   }

   public PortalConfig getPortalConfig(String ownerType, String portalName) throws Exception
   {
      return execute(new PortalConfigTask.Load(ownerType, portalName)).getConfig();
   }

   public void create(PortalConfig config) throws Exception
   {
      execute(new PortalConfigTask.Save(config, true));
   }

   public void save(PortalConfig config) throws Exception
   {
      execute(new PortalConfigTask.Save(config, true));
   }

   public void remove(PortalConfig config) throws Exception
   {
      execute(new PortalConfigTask.Remove(config.getType(), config.getName()));
   }

   public Page getPage(String pageId) throws Exception
   {
      return execute(new PageTask.Load(pageId)).getPage();
   }

   public Page clonePage(String pageId, String clonedOwnerType, String clonedOwnerId, String clonedName)
      throws Exception
   {
      return execute(new PageTask.Clone(pageId, clonedOwnerType, clonedOwnerId, clonedName, true)).getPage();
   }

   public void remove(Page page) throws Exception
   {
      execute(new PageTask.Remove(page));
   }

   public void create(Page page) throws Exception
   {
      execute(new PageTask.Save(page));
   }

   public List<ModelChange> save(Page page) throws Exception
   {
      return execute(new PageTask.Save(page)).getChanges();
   }

   public PageNavigation getPageNavigation(String fullId) throws Exception
   {
      return execute(new PageNavigationTask.Load(fullId)).getPageNavigation();
   }

   public PageNavigation getPageNavigation(String ownerType, String id) throws Exception
   {
      return execute(new PageNavigationTask.Load(ownerType + "::" + id)).getPageNavigation();
   }

   public void save(PageNavigation navigation) throws Exception
   {
      execute(new PageNavigationTask.Save(navigation, true));
   }

   public void create(PageNavigation navigation) throws Exception
   {
      execute(new PageNavigationTask.Save(navigation, false));
   }

   public void remove(PageNavigation navigation) throws Exception
   {
      execute(new PageNavigationTask.Remove(navigation));
   }

   public void save(PortletPreferences portletPreferences) throws Exception
   {
      execute(new PortletPreferencesTask.Save(portletPreferences));
   }

   public <S> S load(ApplicationState<S> state) throws Exception
   {
      if (state instanceof TransientApplicationState)
      {
         TransientApplicationState<S> transientState = (TransientApplicationState<S>)state;
         S prefs = transientState.getContentState();
         return prefs != null ? prefs : null;
      }
      else
      {
         PreferencesTask.Load<S> load = new PreferencesTask.Load<S>((PersistentApplicationState<S>)state);
         execute(load);
         return load.getState();
      }
   }

   public <S> ApplicationState<S> save(ApplicationState<S> state, S preferences) throws Exception
   {
      if (state instanceof TransientApplicationState)
      {
         throw new AssertionError("Does not make sense");
      }
      else
      {
         PreferencesTask.Save<S> save = new PreferencesTask.Save<S>((PersistentApplicationState<S>)state, preferences);
         execute(save);
         return state;
      }
   }

   public PortletPreferences getPortletPreferences(String windowID) throws Exception
   {
      return execute(new PortletPreferencesTask.Load(windowID)).getPreferences();
   }

   public <T> LazyPageList<T> find(Query<T> q) throws Exception
   {
      return find(q, null);
   }

   public <T> LazyPageList<T> find(Query<T> q, Comparator<T> sortComparator) throws Exception
   {
      if (Page.class.equals(q.getClassType()))
      {
         return (LazyPageList<T>)execute(new SearchTask.FindPage((Query<Page>)q)).getResult();
      }
      else if (PageNavigation.class.equals(q.getClassType()))
      {
         return (LazyPageList<T>)execute(new SearchTask.FindNavigation((Query<PageNavigation>)q)).getResult();
      }
      else if (PortletPreferences.class.equals(q.getClassType()))
      {
         return (LazyPageList<T>)execute(new SearchTask.FindPortletPreferences((Query<PortletPreferences>)q))
            .getResult();
      }
      else if (PortalConfig.class.equals(q.getClassType()))
      {
         return (LazyPageList<T>)execute(new SearchTask.FindSite((Query<PortalConfig>)q)).getResult();
      }
      else
      {
         throw new UnsupportedOperationException();
      }
   }

   /**
    * This is a hack and should be removed, it is only used temporarily.
    * This is because the objects are loaded from files and don't have name.
    */
   private void generateStorageName(ModelObject obj)
   {
      if (obj instanceof Container)
      {
         for (ModelObject child : ((Container)obj).getChildren())
         {
            generateStorageName(child);
         }
      }
      else if (obj instanceof Application)
      {
         ((Application)obj).setStorageName(UUID.randomUUID().toString());
      }
   }

   public Container getSharedLayout() throws Exception
   {
      String path = "war:/conf/portal/portal/sharedlayout.xml";
      String out = IOUtil.getStreamContentAsString(confManager_.getInputStream(path));
      ByteArrayInputStream is = new ByteArrayInputStream(out.getBytes("UTF-8"));
      IBindingFactory bfact = BindingDirectory.getFactory(Container.class);
      IUnmarshallingContext uctx = bfact.createUnmarshallingContext();
      Container container = Container.class.cast(uctx.unmarshalDocument(is, null));
      generateStorageName(container);
      return container;
   }
}
