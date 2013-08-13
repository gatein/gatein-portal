package org.gatein.portal.mop.page;

import org.gatein.portal.mop.site.SiteKey;

/**
 * Error codes for page service.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public enum PageError {

    NO_SITE,

    CLONE_NO_SRC_SITE,

    CLONE_NO_SRC_PAGE,

    CLONE_NO_DST_SITE,

    CLONE_DST_ALREADY_EXIST

    ;

    public static PageServiceException noSite(SiteKey key) {
        throw new PageServiceException(PageError.NO_SITE, "No site " + key.getType() + " with name " + key.getName());
    }

    public static PageServiceException cloneNoSrcSite(PageKey key) {
        throw new PageServiceException(PageError.CLONE_NO_SRC_SITE, "Could not clone page " + key.getName()
                + "from non existing site of type " + key.site.getType() + " with name " + key.site.getName());
    }

    public static PageServiceException cloneNoSrcPage(PageKey key) {
        throw new PageServiceException(PageError.CLONE_NO_SRC_PAGE, "Could not clone non existing page " + key.getName()
                + " from site of type " + key.site.getType() + " with name " + key.site.getName());
    }

    public static PageServiceException cloneNoDstSite(PageKey key) {
        throw new PageServiceException(PageError.CLONE_NO_DST_SITE, "Could not clone page " + key.name
                + "to non existing site of type " + key.site.getType() + " with name " + key.site.getName());
    }

    public static PageServiceException cloneDstAlreadyExists(PageKey key) {
        throw new PageServiceException(PageError.CLONE_DST_ALREADY_EXIST, "Could not clone page " + key.name
                + " to existing page " + key.site.getType() + " with name " + key.site.getName());
    }
}
