package org.gatein.portal.web.layout;

/**
 * @author Julien Viet
 */
public class RenderingContext {

    /** . */
    public final String pageKey;

    public final String layoutId;

    /** . */
    public final String path;

    public RenderingContext(String path, String layoutId, String pageKey) {
        this.path = path;
        this.pageKey = pageKey;
        this.layoutId = layoutId;
    }
}
