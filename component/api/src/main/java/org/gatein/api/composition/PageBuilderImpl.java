package org.gatein.api.composition;

import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.page.PageContext;
import org.exoplatform.portal.mop.page.PageKey;
import org.exoplatform.portal.mop.page.PageState;
import org.gatein.api.Util;
import org.gatein.api.page.Page;
import org.gatein.api.page.PageImpl;
import org.gatein.api.security.Permission;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;

import java.util.Arrays;

/**
 * Main point of contact between the consumer of the API and the builders. Provides methods to set all the possible
 * parameters that a persisted page might have, as well as ways to get access to the building blocks of a page, ie,
 * containers.
 *
 * @author <a href="mailto:jpkroehling+javadoc@redhat.com">Juraci Paixão Kröhling</a>
 */
public class PageBuilderImpl extends LayoutBuilderImpl<PageBuilder> implements PageBuilder {
    private static final Logger log = LoggerFactory.getLogger(PageBuilderImpl.class);

    // Page-related properties
    private String name;
    private String description;

    // SiteKey-related properties
    private String siteType;
    private String siteName;

    // PageState-related properties
    private String displayName;
    private boolean showMaxWindow;
    private Permission accessPermission = Container.DEFAULT_ACCESS_PERMISSION;
    private Permission editPermission = Page.DEFAULT_EDIT_PERMISSION;
    private Permission moveAppsPermission = Container.DEFAULT_MOVE_APPS_PERMISSION;
    private Permission moveContainersPermission = Container.DEFAULT_MOVE_CONTAINERS_PERMISSION;

    public PageBuilderImpl() {
        if (log.isTraceEnabled()) {
            log.trace("Created a new page builder: " + this);
        }
    }

    /**
     * @see LayoutBuilderImpl#newColumnsBuilder()
     */
    @Override
    public ContainerBuilder<PageBuilder> newColumnsBuilder() {
        return super.newColumnsBuilder();
    }

    @Override
    public PageBuilder name(String name) {
        this.name = name;
        return this;
    }

    @Override
    public PageBuilder description(String description) {
        this.description = description;
        return this;
    }

    @Override
    public PageBuilder siteName(String siteName) {
        this.siteName = siteName;
        return this;
    }

    @Override
    public PageBuilder displayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    @Override
    public PageBuilder showMaxWindow(boolean showMaxWindow) {
        this.showMaxWindow = showMaxWindow;
        return this;
    }

    @Override
    public PageBuilder accessPermission(Permission accessPermission) {
        this.accessPermission = accessPermission;
        return this;
    }

    @Override
    public PageBuilder editPermission(Permission editPermission) {
        this.editPermission = editPermission;
        return this;
    }

    @Override
    public PageBuilder moveAppsPermission(Permission moveAppsPermission) {
        this.moveAppsPermission = moveAppsPermission;
        return this;
    }

    @Override
    public PageBuilder moveContainersPermission(Permission moveContainersPermission) {
        this.moveContainersPermission = moveContainersPermission;
        return this;
    }

    @Override
    public PageBuilder siteType(String siteType) {

        if (!"portal".equalsIgnoreCase(siteType) && !"site".equalsIgnoreCase(siteType) && !"user".equalsIgnoreCase(siteType)) {
            throw new IllegalArgumentException("siteType must be one of the following: portal, site or user");
        }

        this.siteType = siteType;
        return this;
    }

    /**
     * Builds a new page based on the information provided via this builder.
     * @return the Page that best represents the information stored on this builder
     * @throws java.lang.IllegalStateException if mandatory information is not provided
     */
    @Override
    public Page build() {

        if (null == siteName || null == siteType) {
            throw new IllegalStateException("API usage error: either the SiteKey should be set or both site name and site type should be set.");
        }
        if (null == name) {
            throw new IllegalStateException("API usage error: either the PageKey should be set or both SiteKey and page name should be set.");
        }
        SiteKey siteKey = new SiteKey(siteType, siteName);

        PageKey pageKey = new PageKey(siteKey, name);

        PageState pageState = new PageState(displayName,
                description,
                showMaxWindow,
                null,
                Arrays.asList(Util.from(accessPermission)),
                Util.from(editPermission)[0], // this is the same as the createPage, but is it right?
                Arrays.asList(Util.from(moveAppsPermission)),
                Arrays.asList(Util.from(moveContainersPermission)));
        PageContext pageContext = new PageContext(pageKey, pageState);
        Page page = new PageImpl(pageContext);
        page.setChildren(this.children);

        if (log.isTraceEnabled()) {
            log.trace("Page finished: " + this);
        }

        return page;
    }

    @Override
    public String toString() {
        return "PageBuilderImpl{" +
                super.toString() +
                ", hashCode=" + hashCode() +
                '}';
    }

}
