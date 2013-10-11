package org.gatein.portal.web.layout;

/**
 * @author Julien Viet
 */
public class RenderingContext {

    /** . */
    public final String path;

    /** . */
    public final boolean editing;

    public RenderingContext(String path, boolean editing) {
        this.path = path;
        this.editing = editing;
    }
}
