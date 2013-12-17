package org.gatein.portal.web.layout;

/**
 * @author Julien Viet
 */
public class RenderingContext {

    /** . */
    public final String pageKey;

    public final String layoutId;

    public RenderingContext(String layoutId, String pageKey) {
        this.pageKey = pageKey;
        this.layoutId = layoutId;
    }
}
