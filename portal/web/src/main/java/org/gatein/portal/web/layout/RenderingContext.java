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

    /** . */
    public final boolean editing;

    public RenderingContext(String path, String layoutId, String pageKey, boolean editing) {
        this.path = path;
        this.pageKey = pageKey;
        this.layoutId = layoutId;
        this.editing = editing;
    }
}
