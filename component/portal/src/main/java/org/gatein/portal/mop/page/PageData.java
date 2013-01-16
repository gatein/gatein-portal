package org.gatein.portal.mop.page;

import java.io.Serializable;

/**
 * An immutable page data class.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class PageData implements Serializable {

    /** Useful. */
    public static final PageData EMPTY = new PageData();

    /** . */
    public final PageKey key;

    /** . */
    public final String id;

    /** . */
    public final String layoutId;

    /** . */
    public final PageState state;

    private PageData() {
        this.key = null;
        this.id = null;
        this.state = null;
        this.layoutId = null;
    }

    public PageData(PageKey key, String id, String layoutId, PageState state) {
        this.key = key;
        this.id = id;
        this.layoutId = layoutId;
        this.state = state;
    }

    protected Object readResolve() {
        if (key == null && state == null && id == null) {
            return EMPTY;
        } else {
            return this;
        }
    }
}
