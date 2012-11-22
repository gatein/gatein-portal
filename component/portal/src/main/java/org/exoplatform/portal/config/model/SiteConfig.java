package org.exoplatform.portal.config.model;

import org.exoplatform.portal.pom.data.ModelData;

/**
 *
 * @author <a href="mailto:hoang281283@gmail.com">Minh Hoang TO</a>
 * @version $Id$
 *
 */
public class SiteConfig extends ModelObject {

    public static final String USER_TYPE = "user";

    public static final String GROUP_TYPE = "group";

    public static final String PORTAL_TYPE = "portal";

    private String ownerType;

    private String ownerId;

    /** Access permissions on UI */
    private String[] accessPermissions;

    /** Edit permissions on UI */
    private String editPermission;

    /** Layout of the site */
    private Container siteLayout;

    private String siteSkin;

    public SiteConfig(String _ownerType, String _ownerId, String storageId) {
        super(storageId);
        this.ownerType = _ownerType;
        this.ownerId = _ownerId;
    }

    @Override
    public ModelData build() {
        // TODO Auto-generated method stub
        return null;
    }

    public void setSiteLayout(Container _siteLayout) {
        this.siteLayout = _siteLayout;
    }

    public Container getSiteLayout() {
        return this.siteLayout;
    }

    public String getSiteSkin() {
        return this.siteSkin;
    }

    public void setSiteSkin(String _siteSkin) {
        this.siteSkin = _siteSkin;
    }
}
