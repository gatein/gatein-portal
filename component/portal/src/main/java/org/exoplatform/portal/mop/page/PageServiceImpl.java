package org.exoplatform.portal.mop.page;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.exoplatform.portal.mop.Described;
import org.exoplatform.portal.mop.ProtectedContainer;
import org.exoplatform.portal.mop.ProtectedResource;
import org.exoplatform.portal.mop.QueryResult;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.mop.Utils;
import org.exoplatform.portal.pom.config.POMSession;
import org.exoplatform.portal.pom.config.POMSessionManager;
import org.exoplatform.portal.pom.data.MappedAttributes;
import org.gatein.mop.api.Attributes;
import org.gatein.mop.api.content.ContentType;
import org.gatein.mop.api.content.Customization;
import org.gatein.mop.api.workspace.ObjectType;
import org.gatein.mop.api.workspace.Page;
import org.gatein.mop.api.workspace.Site;
import org.gatein.mop.api.workspace.Workspace;
import org.gatein.mop.api.workspace.WorkspaceCustomizationContext;
import org.gatein.mop.api.workspace.ui.UIComponent;
import org.gatein.mop.api.workspace.ui.UIContainer;
import org.gatein.mop.api.workspace.ui.UIWindow;

/**
 * This class implements the {@link PageService} business methods.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class PageServiceImpl implements PageService {

    /** . */
    final POMSessionManager manager;

    /** . */
    private final DataCache dataCache;

    /**
     * Create an instance that uses a simple data cache, such instance should be used for testing purposes.
     *
     * @param manager the mop session manager
     * @throws NullPointerException if the manager argument is null
     */
    public PageServiceImpl(POMSessionManager manager) throws NullPointerException {
        this(manager, new SimpleDataCache());
    }

    /**
     * Create an instance that will use a specified data cache instance.
     *
     * @param manager the mop session manager
     * @param dataCache the data cache
     * @throws NullPointerException if any argument is null
     */
    public PageServiceImpl(POMSessionManager manager, DataCache dataCache) throws NullPointerException {
        if (manager == null) {
            throw new NullPointerException("No null pom session manager allowed");
        }
        if (dataCache == null) {
            throw new NullPointerException("No null data cache allowed");
        }
        this.manager = manager;
        this.dataCache = dataCache;
    }

    @Override
    public PageContext loadPage(PageKey key) {
        if (key == null) {
            throw new NullPointerException();
        }

        //
        POMSession session = manager.getSession();
        PageData data = dataCache.getPageData(session, key);
        return data != null && data != PageData.EMPTY ? new PageContext(data) : null;
    }

    /**
     * <p>
     * Load all the pages of a specific site. Note that this method can potentially raise performance issues if the number of
     * pages is very large and should be used with cautions. That's the motiviation for not having this method on the
     * {@link PageService} interface.
     * </p>
     *
     * @param siteKey the site key
     * @return the list of pages
     * @throws NullPointerException if the site key argument is null
     * @throws PageServiceException anything that would prevent the operation to succeed
     */
    public List<PageContext> loadPages(SiteKey siteKey) throws NullPointerException, PageServiceException {
        if (siteKey == null) {
            throw new NullPointerException("No null site key accepted");
        }

        //
        POMSession session = manager.getSession();
        ObjectType<Site> objectType = Utils.objectType(siteKey.getType());
        Workspace workspace = session.getWorkspace();
        Site site = workspace.getSite(objectType, siteKey.getName());

        //
        if (site == null) {
            throw new PageServiceException(PageError.NO_SITE);
        }

        //
        org.gatein.mop.api.workspace.Page root = site.getRootPage();
        Collection<org.gatein.mop.api.workspace.Page> pages = root.getChild("pages").getChildren();
        List<PageContext> list = new ArrayList<PageContext>(pages.size());
        for (Page page : pages) {
            list.add(loadPage(new PageKey(siteKey, page.getName())));
        }

        return list;
    }

    @Override
    public boolean savePage(PageContext page) {
        if (page == null) {
            throw new NullPointerException();
        }

        //
        POMSession session = manager.getSession();
        ObjectType<Site> objectType = Utils.objectType(page.key.getSite().getType());
        Workspace workspace = session.getWorkspace();
        Site site = workspace.getSite(objectType, page.key.getSite().getName());

        //
        if (site == null) {
            throw new PageServiceException(PageError.NO_SITE);
        }

        //
        org.gatein.mop.api.workspace.Page root = site.getRootPage();
        org.gatein.mop.api.workspace.Page pages = root.getChild("pages");
        org.gatein.mop.api.workspace.Page dst = pages.getChild(page.key.getName());

        //
        boolean created;
        if (dst == null) {
            dst = pages.addChild(page.key.getName());
            created = true;
        } else {
            created = false;
        }

        //
        PageState state = page.state;
        if (state != null) {
            ProtectedResource pr = dst.adapt(ProtectedResource.class);
            pr.setAccessPermissions(page.state.accessPermissions);
            pr.setEditPermission(page.state.editPermission);

            ProtectedContainer dstPc = dst.adapt(ProtectedContainer.class);
            dstPc.setMoveAppsPermissions(page.state.moveAppsPermissions);
            dstPc.setMoveContainersPermissions(page.state.moveContainersPermissions);

            //
            Described described = dst.adapt(Described.class);
            described.setName(page.state.displayName);
            described.setDescription(page.state.description);

            //
            Attributes attrs = dst.getAttributes();
            attrs.setValue(MappedAttributes.FACTORY_ID, page.state.factoryId);
            attrs.setValue(MappedAttributes.SHOW_MAX_WINDOW, page.state.showMaxWindow);
        }

        //
        dataCache.removePage(session, page.key);

        // Update state
        page.data = dataCache.getPageData(session, page.key);
        page.state = null;

        //
        return created;
    }

    @Override
    public boolean destroyPage(PageKey key) {
        if (key == null) {
            throw new NullPointerException("No null page argument");
        }

        //
        POMSession session = manager.getSession();
        ObjectType<Site> objectType = Utils.objectType(key.getSite().getType());
        Workspace workspace = session.getWorkspace();
        Site site = workspace.getSite(objectType, key.getSite().getName());

        //
        if (site == null) {
            throw new PageServiceException(PageError.NO_SITE);
        }

        //
        org.gatein.mop.api.workspace.Page root = site.getRootPage();
        org.gatein.mop.api.workspace.Page pages = root.getChild("pages");
        org.gatein.mop.api.workspace.Page page = pages.getChild(key.getName());
        if (page != null) {
            page.destroy();
            dataCache.removePage(session, key);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public PageContext clone(PageKey src, PageKey dst) {
        POMSession session = manager.getSession();
        Workspace workspace = session.getWorkspace();

        //
        org.gatein.mop.api.workspace.Page srcPage;
        ObjectType<Site> srcType = Utils.objectType(src.site.getType());
        Site srcSite = workspace.getSite(srcType, src.site.getName());
        if (srcSite == null) {
            throw new PageServiceException(PageError.CLONE_NO_SRC_SITE, "Could not clone page " + src.getName()
                    + "from non existing site of type " + src.site.getType() + " with id " + src.site.getName());
        } else {
            org.gatein.mop.api.workspace.Page root = srcSite.getRootPage();
            org.gatein.mop.api.workspace.Page pages = root.getChild("pages");
            srcPage = pages.getChild(src.getName());
        }

        //
        if (srcPage == null) {
            throw new PageServiceException(PageError.CLONE_NO_SRC_PAGE, "Could not clone non existing page " + src.getName()
                    + " from site of type " + src.site.getType() + " with id " + src.site.getName());
        }

        //
        ObjectType<Site> dstType = Utils.objectType(dst.getSite().getType());
        Site dstSite = workspace.getSite(dstType, dst.getSite().getName());
        if (dstSite == null) {
            throw new PageServiceException(PageError.CLONE_NO_DST_SITE, "Could not clone page " + dst.name
                    + "to non existing site of type " + dst.site.getType() + " with id " + dst.site.getName());
        }

        //
        org.gatein.mop.api.workspace.Page dstRoot = dstSite.getRootPage();
        org.gatein.mop.api.workspace.Page dstPages = dstRoot.getChild("pages");
        if (dstPages.getChild(dst.getName()) != null) {
            throw new PageServiceException(PageError.CLONE_DST_ALREADY_EXIST, "Could not clone page " + dst.name
                    + "to existing page " + dst.site.getType() + " with id " + dst.site.getName());
        }

        //
        org.gatein.mop.api.workspace.Page dstPage = dstPages.addChild(dst.getName());

        // Copy all attributes
        Attributes srcAttrs = srcPage.getAttributes();
        Attributes dstAttrs = dstPage.getAttributes();
        for (String key : srcAttrs.getKeys()) {
            Object value = srcAttrs.getObject(key);
            dstAttrs.setObject(key, value);
        }

        // Copy described
        Described srcDescribed = srcPage.adapt(Described.class);
        Described dstDescribed = dstPage.adapt(Described.class);
        dstDescribed.setName(srcDescribed.getName());
        dstDescribed.setDescription(srcDescribed.getDescription());

        // Copy src permissions to dst permission
        ProtectedResource srcPR = srcPage.adapt(ProtectedResource.class);
        ProtectedResource dstPR = dstPage.adapt(ProtectedResource.class);
        dstPR.setAccessPermissions(srcPR.getAccessPermissions());
        dstPR.setEditPermission(srcPR.getEditPermission());

        if (srcPage.isAdapted(ProtectedContainer.class)) {
            ProtectedContainer srcPc = srcPage.adapt(ProtectedContainer.class);
            ProtectedContainer dstPc = dstPage.adapt(ProtectedContainer.class);
            dstPc.setMoveAppsPermissions(srcPc.getMoveAppsPermissions());
            dstPc.setMoveContainersPermissions(srcPc.getMoveContainersPermissions());
        }

        // Need to clone page data structure as well
        copy(srcPage, dstPage, srcPage.getRootComponent(), dstPage.getRootComponent());

        // Remove
        dataCache.removePage(session, dst);

        //
        return new PageContext(new PageData(dstPage));
    }

    private void copy(Page srcPage, Page dstPage, UIContainer src, UIContainer dst) {
        for (UIComponent srcChild : src.getComponents()) {
            UIComponent dstChild = dst.add(srcChild.getObjectType(), srcChild.getObjectId());

            //
            if (srcChild.isAdapted(Described.class)) {
                Described srcDescribed = srcChild.adapt(Described.class);
                Described dstDescribed = dstChild.adapt(Described.class);
                dstDescribed.setName(srcDescribed.getName());
                dstDescribed.setDescription(srcDescribed.getDescription());
            }

            //
            if (srcChild.isAdapted(ProtectedResource.class)) {
                ProtectedResource srcPR = srcChild.adapt(ProtectedResource.class);
                ProtectedResource dstPR = dstChild.adapt(ProtectedResource.class);
                dstPR.setAccessPermissions(srcPR.getAccessPermissions());
                dstPR.setEditPermission(srcPR.getEditPermission());
            }

            if (srcChild.isAdapted(ProtectedContainer.class)) {
                ProtectedContainer srcPc = srcPage.adapt(ProtectedContainer.class);
                ProtectedContainer dstPc = dstPage.adapt(ProtectedContainer.class);
                dstPc.setMoveAppsPermissions(srcPc.getMoveAppsPermissions());
                dstPc.setMoveContainersPermissions(srcPc.getMoveContainersPermissions());
            }


            //
            Attributes srcAttrs = srcChild.getAttributes();
            Attributes dstAttrs = dstChild.getAttributes();
            for (String key : srcAttrs.getKeys()) {
                Object value = srcAttrs.getObject(key);
                dstAttrs.setObject(key, value);
            }

            //
            if (srcChild instanceof UIWindow) {
                UIWindow srcWindow = (UIWindow) srcChild;
                UIWindow dstWindow = (UIWindow) dstChild;
                Customization<?> customization = srcWindow.getCustomization();
                ContentType contentType = customization.getType();
                String contentId = customization.getContentId();
                Customization parent = customization.getParent();
                Customization dstParent = null;
                if (parent != null) {
                    WorkspaceCustomizationContext parentCtx = (WorkspaceCustomizationContext) parent.getContext();
                    String name = parentCtx.nameOf(parent);
                    if (parentCtx == srcPage) {
                        dstParent = dstPage.getCustomizationContext().getCustomization(name);
                        if (dstParent == null) {
                            Object state = parent.getVirtualState();
                            dstParent = dstPage.getCustomizationContext().customize(name, contentType, contentId, state);
                        }
                    }
                    if (dstParent != null) {
                        Object state = customization.getState();
                        Customization dstCustomization = dstWindow.customize(dstParent);
                        dstCustomization.setState(state);
                    } else {
                        Object state = customization.getVirtualState();
                        dstWindow.customize(contentType, contentId, state);
                    }
                } else {
                    Object state = customization.getVirtualState();
                    dstWindow.customize(contentType, contentId, state);
                }
            } else if (srcChild instanceof UIContainer) {
                UIContainer srcContainer = (UIContainer) srcChild;
                UIContainer dstContainer = (UIContainer) dstChild;
                copy(srcPage, dstPage, srcContainer, dstContainer);
            }
        }
    }

    @Override
    public QueryResult<PageContext> findPages(int from, int to, SiteType siteType, String siteName, String pageName,
            String pageTitle) {
        POMSession session = manager.getSession();
        org.chromattic.api.query.QueryResult<Page> a = session.findObjects(ObjectType.PAGE, Utils.objectType(siteType),
                siteName, pageTitle, from, to);
        int size = a.size();
        PageContext[] array = new PageContext[size];
        int ptr = 0;
        while (a.hasNext()) {
            Page page = a.next();
            PageData data = new PageData(page);
            dataCache.putPage(data);
            array[ptr++] = new PageContext(data);
        }
        return new QueryResult<PageContext>(from, size, Arrays.asList(array));
    }

    public void clearCache() {
        dataCache.clear();
    }
}
