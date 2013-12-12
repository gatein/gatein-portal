package org.exoplatform.portal.mop.management.operations.page;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.config.DataStorage;
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
import org.exoplatform.portal.mop.page.PageContext;
import org.exoplatform.portal.mop.page.PageKey;
import org.exoplatform.portal.mop.page.PageService;
import org.exoplatform.portal.mop.page.PageServiceImpl;
import org.exoplatform.portal.mop.page.PageServiceWrapper;
import org.exoplatform.portal.mop.page.PageState;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class PageUtils {
    private PageUtils() {
    }

    public static Page getPage(DataStorage dataStorage, PageService pageService, PageKey pageKey) throws Exception {
        PageContext pageContext = pageService.loadPage(pageKey);
        if (pageContext == null)
            return null;

        // PageService does not support the entire page at the moment, so we must grab the page from legacy service
        // and update it with data page service does support.
        Page page = dataStorage.getPage(pageKey.format());
        pageContext.update(page);

        return page;
    }

    public static Page.PageSet getAllPages(DataStorage dataStorage, PageService pageService, SiteKey siteKey) throws Exception {
        Page.PageSet pages = new Page.PageSet();
        List<PageContext> pageContextList;

        // If the PageService interface ever supports a loadPages method, remove casting.
        if (pageService instanceof PageServiceWrapper) {
            pageContextList = ((PageServiceWrapper) pageService).loadPages(siteKey);
        } else if (pageService instanceof PageServiceImpl) {
            pageContextList = ((PageServiceImpl) pageService).loadPages(siteKey);
        } else {
            throw new IllegalArgumentException("Unknown page service implementation " + pageService.getClass());
        }

        ArrayList<Page> pageList = new ArrayList<Page>(pageContextList.size());
        for (PageContext pageContext : pageContextList) {
            Page page = dataStorage.getPage(pageContext.getKey().format());
            pageContext.update(page);
            pageList.add(page);
        }

        pages.setPages(pageList);

        return pages;
    }

    public static PageState toPageState(Page page) {
        return new PageState(page.getTitle(), page.getDescription(), page.isShowMaxWindow(), page.getFactoryId(),
                page.getAccessPermissions() != null ? Arrays.asList(page.getAccessPermissions()) : null,
                page.getEditPermission(), page.getMoveAppsPermissions() != null ? Arrays.asList(page.getMoveAppsPermissions()) : null,
                page.getMoveContainersPermissions() != null ? Arrays.asList(page.getMoveContainersPermissions()) : null);
    }

    public static <S> Application<S> copy(Application<S> existing) {
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

    public static <S> ApplicationState<S> copy(ApplicationType<S> type, ApplicationState<S> existing) {
        if (existing instanceof TransientApplicationState) {
            TransientApplicationState<S> state = (TransientApplicationState<S>) existing;
            return new TransientApplicationState<S>(state.getContentId(), state.getContentState(), state.getOwnerType(),
                    state.getOwnerId());
        } else {
            // Hate doing this, but it's the only way to deal with persistent application state...
            ExoContainer container = ExoContainerContext.getCurrentContainer();
            if (container instanceof PortalContainer) {
                DataStorage ds = (DataStorage) container.getComponentInstanceOfType(DataStorage.class);
                try {
                    S s = ds.load(existing, type);
                    String contentId = ds.getId(existing);

                    return new TransientApplicationState<S>(contentId, s);
                } catch (Exception e) {
                    throw new RuntimeException("Exception copying persistent application state.", e);
                }
            } else {
                throw new RuntimeException("Unable to copy persistent application state with kernel container " + container);
            }
        }
    }

    public static Container copy(Container existing) {
        Container container = new Container();
        copyFields(existing, container);

        return container;
    }

    public static Dashboard copy(Dashboard existing) {
        Dashboard dashboard = new Dashboard();
        copyFields(existing, dashboard);

        return dashboard;
    }

    public static Page copy(Page existing) {
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

    public static Page.PageSet copy(Page.PageSet existingPageSet) {
        Page.PageSet pageSet = new Page.PageSet();
        ArrayList<Page> pages = new ArrayList<Page>(existingPageSet.getPages().size());
        pageSet.setPages(pages);

        for (Page existingPage : existingPageSet.getPages()) {
            pages.add(copy(existingPage));
        }

        return pageSet;
    }

    public static PageBody copy(PageBody existing) {
        return new PageBody();
    }

    public static PortalConfig copy(PortalConfig existing) {
        PortalConfig portalConfig = new PortalConfig(existing.getType(), existing.getName());
        portalConfig.setAccessPermissions(copy(existing.getAccessPermissions()));
        portalConfig.setDescription(existing.getDescription());
        portalConfig.setEditPermission(existing.getEditPermission());
        portalConfig.setLabel(existing.getLabel());
        portalConfig.setSkin(existing.getSkin());
        portalConfig.setLocale(existing.getLocale());
        portalConfig.setModifiable(existing.isModifiable());
        portalConfig.setPortalLayout(copy(existing.getPortalLayout()));
        portalConfig.setProperties(new Properties(existing.getProperties()));

        return portalConfig;
    }

    private static void copyFields(Container existing, Container container) {
        container.setAccessPermissions(copy(existing.getAccessPermissions()));
        container.setMoveAppsPermissions(copy(existing.getMoveAppsPermissions()));
        container.setMoveContainersPermissions(copy(existing.getMoveContainersPermissions()));
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

    private static ArrayList<ModelObject> copyChildren(ArrayList<ModelObject> existing) {
        if (existing == null)
            return null;
        ArrayList<ModelObject> children = new ArrayList<ModelObject>(existing.size());

        for (ModelObject object : existing) {
            if (object instanceof Application) {
                Application<?> app = copy((Application<?>) object);

                children.add(app);
            }
            if (object instanceof Dashboard) {
                children.add(copy((Dashboard) object));
            }
            if (object instanceof Container) {
                children.add(copy((Container) object));
            }
            if (object instanceof PageBody) {
                children.add(copy((PageBody) object));
            }
        }

        return children;
    }

    private static String[] copy(String[] existing) {
        if (existing == null)
            return null;

        String[] array = new String[existing.length];
        System.arraycopy(existing, 0, array, 0, existing.length);

        return array;
    }
}
