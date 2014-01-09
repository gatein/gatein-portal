/*
 * Copyright (C) 2012 eXo Platform SAS.
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

package org.exoplatform.portal.mop.page;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.exoplatform.portal.mop.Described;
import org.exoplatform.portal.mop.ProtectedResource;
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
import org.gatein.portal.mop.page.PageData;
import org.gatein.portal.mop.page.PageError;
import org.gatein.portal.mop.page.PageKey;
import org.gatein.portal.mop.page.PageStore;
import org.gatein.portal.mop.page.PageState;
import org.gatein.portal.mop.site.SiteKey;
import org.gatein.portal.mop.site.SiteType;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class MopStore implements PageStore {

    /** . */
    final POMSessionManager manager;

    /** . */
    private final DataCache dataCache;

    public MopStore(POMSessionManager manager, DataCache dataCache) {
        this.manager = manager;
        this.dataCache = dataCache;
    }

    @Override
    public PageData loadPage(PageKey key) {
        POMSession session = manager.getSession();
        PageData data = dataCache.getPageData(session, key);
        return data == PageData.EMPTY ? null : data;
    }

    @Override
    public boolean savePage(PageKey key, PageState state) {
        POMSession session = manager.getSession();
        SiteKey siteKey = key.getSite();
        ObjectType<Site> objectType = Utils.objectType(siteKey.getType());
        Workspace workspace = session.getWorkspace();
        Site site = workspace.getSite(objectType, siteKey.getName());
        if (site == null) {
            throw PageError.noSite(key.site);
        }
        org.gatein.mop.api.workspace.Page root = site.getRootPage();
        org.gatein.mop.api.workspace.Page pages = root.getChild("pages");
        org.gatein.mop.api.workspace.Page dst = pages.getChild(key.getName());
        boolean created;
        if (dst == null) {
            dst = pages.addChild(key.getName());
            created = true;
        } else {
            created = false;
        }
        if (state != null) {

            //
            Described described = dst.adapt(Described.class);
            described.setName(state.getDisplayName());
            described.setDescription(state.getDescription());

            //
            Attributes attrs = dst.getAttributes();
            attrs.setValue(MappedAttributes.FACTORY_ID, state.getFactoryId());
            attrs.setValue(MappedAttributes.SHOW_MAX_WINDOW, state.getShowMaxWindow());
        }
        dataCache.removePage(session, key);
        return created;
    }

    @Override
    public boolean destroyPage(PageKey key) {
        POMSession session = manager.getSession();
        ObjectType<Site> objectType = Utils.objectType(key.getSite().getType());
        Workspace workspace = session.getWorkspace();
        Site site = workspace.getSite(objectType, key.getSite().getName());
        if (site == null) {
            throw PageError.noSite(key.site);
        }
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
    public PageData clonePage(PageKey src, PageKey dst) {
        POMSession session = manager.getSession();
        Workspace workspace = session.getWorkspace();

        //
        org.gatein.mop.api.workspace.Page srcPage;
        ObjectType<Site> srcType = Utils.objectType(src.site.getType());
        Site srcSite = workspace.getSite(srcType, src.site.getName());
        if (srcSite == null) {
            throw PageError.cloneNoSrcSite(src);
        } else {
            org.gatein.mop.api.workspace.Page root = srcSite.getRootPage();
            org.gatein.mop.api.workspace.Page pages = root.getChild("pages");
            srcPage = pages.getChild(src.getName());
        }

        //
        if (srcPage == null) {
            throw PageError.cloneNoSrcPage(src);
        }

        //
        ObjectType<Site> dstType = Utils.objectType(dst.getSite().getType());
        Site dstSite = workspace.getSite(dstType, dst.getSite().getName());
        if (dstSite == null) {
            throw PageError.cloneNoDstSite(src);
        }

        //
        org.gatein.mop.api.workspace.Page dstRoot = srcSite.getRootPage();
        org.gatein.mop.api.workspace.Page dstPages = dstRoot.getChild("pages");
        if (dstPages.getChild(dst.getName()) != null) {
            throw PageError.cloneDstAlreadyExists(src);
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
        dstPR.setEditPermissions(srcPR.getEditPermissions());

        // Need to clone page data structure as well
        copy(srcPage, dstPage, srcPage.getRootComponent(), dstPage.getRootComponent());

        // Remove
        dataCache.removePage(session, dst);

        //
        return DataCache.create(dstPage);
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
                dstPR.setEditPermissions(srcPR.getEditPermissions());
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
                            Object state = parent.getState();
                            dstParent = dstPage.getCustomizationContext().customize(name, contentType, contentId, state);
                        }
                    }
                    if (dstParent != null) {
                        Object state = customization.getState();
                        Customization dstCustomization = dstWindow.customize(dstParent);
                        dstCustomization.setState(state);
                    } else {
                        Object state = customization.getState();
                        dstWindow.customize(contentType, contentId, state);
                    }
                } else {
                    Object state = customization.getState();
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
    public List<PageKey> findPageKeys(SiteKey siteKey) {
        POMSession session = manager.getSession();
        ObjectType<Site> objectType = Utils.objectType(siteKey.getType());
        Workspace workspace = session.getWorkspace();
        Site site = workspace.getSite(objectType, siteKey.getName());
        if (site == null) {
            throw PageError.noSite(siteKey);
        }
        org.gatein.mop.api.workspace.Page root = site.getRootPage();
        Collection<Page> pages = root.getChild("pages").getChildren();
        List<PageKey> list = new ArrayList<PageKey>(pages.size());
        for (Page page : pages) {
            list.add(new PageKey(siteKey, page.getName()));
        }
        return list;
    }

    @Override
    public Collection<PageData> findPages(
            int from,
            int to,
            SiteType siteType,
            String siteName,
            String pageName,
            String pageTitle) {
        POMSession session = manager.getSession();
        org.chromattic.api.query.QueryResult<Page> a = session.findObjects(
                ObjectType.PAGE,
                Utils.objectType(siteType),
                siteName,
                pageTitle,
                from,
                to);
        int size = a.size();
        PageData[] array = new PageData[size];
        int ptr = 0;
        while (a.hasNext()) {
            Page page = a.next();
            PageData data = DataCache.create(page);
            dataCache.putPage(data);
            array[ptr++] = data;
        }
        return Arrays.asList(array);
    }

    @Override
    public void clear() {
        dataCache.clear();
    }
}
