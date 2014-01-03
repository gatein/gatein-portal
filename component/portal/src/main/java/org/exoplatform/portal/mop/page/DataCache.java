package org.exoplatform.portal.mop.page;

import java.util.Collections;
import java.util.List;

import org.exoplatform.portal.mop.Described;
import org.exoplatform.portal.mop.ProtectedResource;
import org.exoplatform.portal.mop.Utils;
import org.exoplatform.portal.pom.config.POMSession;
import org.exoplatform.portal.pom.data.MappedAttributes;
import org.gatein.mop.api.Attributes;
import org.gatein.mop.api.workspace.ObjectType;
import org.gatein.mop.api.workspace.Page;
import org.gatein.mop.api.workspace.Site;
import org.gatein.mop.api.workspace.Workspace;
import org.gatein.portal.mop.page.PageData;
import org.gatein.portal.mop.page.PageKey;
import org.gatein.portal.mop.page.PageState;
import org.gatein.portal.mop.site.SiteKey;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
abstract class DataCache {

    protected abstract PageData getPage(POMSession session, PageKey key);

    protected abstract void removePage(POMSession session, PageKey key);

    protected abstract void putPage(PageData data);

    protected abstract void clear();

    final PageData getPageData(POMSession session, PageKey key) {
        PageData data;
        if (session.isModified()) {
            data = loadPage(session, key);
        } else {
            data = getPage(session, key);
        }

        //
        return data;
    }

    protected final PageData loadPage(POMSession session, PageKey key) {
        Workspace workspace = session.getWorkspace();
        ObjectType<Site> objectType = Utils.objectType(key.getSite().getType());
        Site site = workspace.getSite(objectType, key.getSite().getName());
        if (site != null) {
            org.gatein.mop.api.workspace.Page root = site.getRootPage();
            org.gatein.mop.api.workspace.Page pages = root.getChild("pages");
            org.gatein.mop.api.workspace.Page page = pages.getChild(key.getName());
            if (page != null) {
                return create(page);
            } else {
                return PageData.EMPTY;
            }
        } else {
            return PageData.EMPTY;
        }
    }

    static PageData create(Page page) {
        Site site = page.getSite();

        Attributes attrs = page.getAttributes();
        Described described = page.adapt(Described.class);

        //
        List<String> accessPermissions = Collections.emptyList();
        List<String> editPermission = null;
        if (page.isAdapted(ProtectedResource.class)) {
            ProtectedResource pr = page.adapt(ProtectedResource.class);
            accessPermissions = pr.getAccessPermissions();
            editPermission = pr.getEditPermissions();
        }

        //
        PageState state = new PageState(
                described.getName(),
                described.getDescription(),
                attrs.getValue(MappedAttributes.SHOW_MAX_WINDOW, false),
                attrs.getValue(MappedAttributes.FACTORY_ID),
                org.exoplatform.portal.pom.config.Utils.safeImmutableList(accessPermissions),
                editPermission);

        //
        return new PageData(
                new SiteKey(Utils.siteType(site.getObjectType()), site.getName()).page(page.getName()),
                page.getObjectId(),
                page.getRootComponent().getObjectId(),
                state);
    }
}
