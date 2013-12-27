package org.gatein.portal.web.layout;

/**
 * @author Julien Viet
 */
public class RenderingContext {

    /** . */
    public final String pageKey;

    public final String layoutId;
    
    public final String factoryId;
    
    public final String pageDisplayName;
    
    public final String parentLink;
    
    public RenderingContext() {
        this(null, null, null, null, null);
    }

    public RenderingContext(String layoutId, String pageKey, String factoryId, String pageDisplayName, String parentLink) {
        this.pageKey = pageKey;
        this.layoutId = layoutId;
        this.factoryId = factoryId;
        this.pageDisplayName = pageDisplayName;
        this.parentLink = parentLink;
    }
}
