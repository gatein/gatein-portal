package org.exoplatform.portal.mop.management.operations.page;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.Query;
import org.exoplatform.portal.config.model.Application;
import org.exoplatform.portal.config.model.ApplicationState;
import org.exoplatform.portal.config.model.ApplicationType;
import org.exoplatform.portal.config.model.Container;
import org.exoplatform.portal.config.model.Dashboard;
import org.exoplatform.portal.config.model.ModelObject;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PageBody;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.config.model.Properties;
import org.exoplatform.portal.config.model.TransientApplicationState;
import org.exoplatform.portal.mop.SiteKey;
import org.gatein.management.api.exceptions.OperationException;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class PageUtils
{
   private PageUtils(){}

   public static Page getPage(DataStorage dataStorage, PageKey pageKey, String operationName)
   {
      try
      {
         return dataStorage.getPage(pageKey.getPageId());
      }
      catch (Exception e)
      {
         throw new OperationException(operationName, "Operation failed getting page for " + pageKey, e);
      }
   }

   public static Page.PageSet getAllPages(DataStorage dataStorage, SiteKey siteKey, String operationName)
   {
      Query<Page> query = new Query<Page>(siteKey.getTypeName(), siteKey.getName(), Page.class);
      try
      {
         List<Page> pageList = dataStorage.find(query).getAll();
         Page.PageSet pages = new Page.PageSet();
         pages.setPages(new ArrayList<Page>(pageList));

         return pages;
      }
      catch (Exception e)
      {
         throw new OperationException(operationName, "Could not retrieve pages for site " + siteKey);
      }
   }

   public static <S> Application<S> copy(Application<S> existing)
   {
      Application<S> application = new Application<S>(existing.getType());
      application.setAccessPermissions(copy(existing.getAccessPermissions()));
      application.setDescription(existing.getDescription());
      application.setHeight(existing.getHeight());
      application.setIcon(existing.getIcon());
      application.setId(existing.getId());
      application.setModifiable(existing.isModifiable());
      application.setProperties(new Properties(existing.getProperties()));
      application.setShowApplicationMode(existing.getShowApplicationMode());
      application.setShowApplicationState(existing.getShowApplicationState());
      application.setShowInfoBar(existing.getShowInfoBar());
      application.setState(copy(existing.getType(), existing.getState()));
      application.setTheme(existing.getTheme());
      application.setTitle(existing.getTitle());
      application.setWidth(existing.getWidth());

      return application;
   }

   public static <S> ApplicationState<S> copy(ApplicationType<S> type, ApplicationState<S> existing)
   {
      if (existing instanceof TransientApplicationState)
      {
         TransientApplicationState<S> state = (TransientApplicationState<S>) existing;
         return new TransientApplicationState<S>(state.getContentId(), state.getContentState(), state.getOwnerType(), state.getOwnerId(), state.getUniqueId());
      }
      else
      {
         // Hate doing this, but it's the only way to deal with persistent application state...
         ExoContainer container = ExoContainerContext.getCurrentContainer();
         if (container instanceof PortalContainer)
         {
            DataStorage ds = (DataStorage) container.getComponentInstanceOfType(DataStorage.class);
            try
            {
               S s = ds.load(existing, type);
               String contentId = ds.getId(existing);

               return new TransientApplicationState<S>(contentId, s);
            }
            catch (Exception e)
            {
               throw new RuntimeException("Exception copying persistent application state.", e);
            }
         }
         else
         {
            throw new RuntimeException("Unable to copy persistent application state with kernel container " + container);
         }
      }
   }

   public static Container copy(Container existing)
   {
      Container container = new Container();
      copyFields(existing, container);

      return container;
   }

   public static Dashboard copy(Dashboard existing)
   {
      Dashboard dashboard = new Dashboard();
      copyFields(existing, dashboard);

      return dashboard;
   }

   public static Page copy(Page existing)
   {
      Page page = new Page();

      // Copy page specific data
      page.setEditPermission(existing.getEditPermission());
      page.setModifiable(existing.isModifiable());
      page.setOwnerId(existing.getOwnerId());
      page.setOwnerType(existing.getOwnerType());
      page.setPageId(existing.getPageId());
      page.setShowMaxWindow(existing.isShowMaxWindow());

      // Copy container specific data.
      copyFields(existing, page);

      return page;
   }

   public static Page.PageSet copy(Page.PageSet existingPageSet)
   {
      Page.PageSet pageSet = new Page.PageSet();
      ArrayList<Page> pages = new ArrayList<Page>(existingPageSet.getPages().size());
      pageSet.setPages(pages);

      for (Page existingPage : existingPageSet.getPages())
      {
         pages.add(copy(existingPage));
      }

      return pageSet;
   }

   @SuppressWarnings("unused")
   public static PageBody copy(PageBody existing)
   {
      return new PageBody();
   }

   public static PortalConfig copy(PortalConfig existing)
   {
      PortalConfig portalConfig = new PortalConfig(existing.getType(), existing.getName());
      portalConfig.setAccessPermissions(copy(existing.getAccessPermissions()));
      portalConfig.setDescription(existing.getDescription());
      portalConfig.setEditPermission(existing.getEditPermission());
      portalConfig.setLabel(existing.getLabel());
      portalConfig.setLocale(existing.getLocale());
      portalConfig.setModifiable(existing.isModifiable());
      portalConfig.setPortalLayout(copy(existing.getPortalLayout()));
      portalConfig.setProperties(new Properties(existing.getProperties()));

      return portalConfig;
   }

   private static void copyFields(Container existing, Container container)
   {
      container.setAccessPermissions(copy(existing.getAccessPermissions()));
      container.setChildren(copyChildren(existing.getChildren()));
      container.setDecorator(existing.getDecorator());
      container.setDescription(existing.getDescription());
      container.setFactoryId(existing.getFactoryId());
      container.setHeight(existing.getHeight());
      container.setIcon(existing.getIcon());
      container.setId(existing.getId());
      container.setName(existing.getName());
      container.setTemplate(existing.getTemplate());
      container.setTitle(existing.getTitle());
      container.setWidth(existing.getWidth());
   }

   private static ArrayList<ModelObject> copyChildren(ArrayList<ModelObject> existing)
   {
      if (existing == null) return null;
      ArrayList<ModelObject> children = new ArrayList<ModelObject>(existing.size());

      for (ModelObject object : existing)
      {
         if (object instanceof Application)
         {
            @SuppressWarnings("unchecked")
            Application app = copy((Application) object);

            children.add(app);
         }
         if (object instanceof Dashboard)
         {
            children.add(copy((Dashboard) object));
         }
         if (object instanceof Container)
         {
            children.add(copy((Container) object));
         }
      }

      return children;
   }

   private static String[] copy(String[] existing)
   {
      if (existing == null) return null;

      String[] array = new String[existing.length];
      System.arraycopy(existing, 0, array, 0, existing.length);

      return array;
   }
}
