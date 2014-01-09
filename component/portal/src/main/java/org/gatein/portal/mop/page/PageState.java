package org.gatein.portal.mop.page;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.exoplatform.commons.utils.Safe;

/**
 * An immutable page state class, modifying an existing state should use the {@link Builder} builder class to rebuild a new
 * immutable state object.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class PageState implements Serializable {

    /** . */
    final boolean showMaxWindow;

    /** . */
    final String factoryId;

    /** . */
    final String displayName;

    /** . */
    final String description;

    public PageState(String displayName, String description, boolean showMaxWindow, String factoryId) {
        this.showMaxWindow = showMaxWindow;
        this.factoryId = factoryId;
        this.displayName = displayName;
        this.description = description;
    }

    public boolean getShowMaxWindow() {
        return showMaxWindow;
    }

    public String getFactoryId() {
        return factoryId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PageState)) {
            return false;
        }
        PageState that = (PageState) o;
        return  showMaxWindow == that.showMaxWindow
                && Safe.equals(factoryId, that.factoryId) && Safe.equals(displayName, that.displayName)
                && Safe.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        int result = (showMaxWindow ? 1 : 0);
        result = 31 * result + (factoryId != null ? factoryId.hashCode() : 0);
        result = 31 * result + (displayName != null ? displayName.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        return result;
    }

    public Builder builder() {
        return new Builder(showMaxWindow, factoryId, displayName, description);
    }

    public static class Builder {

        /** . */
        private boolean showMaxWindow;

        /** . */
        private String factoryId;

        /** . */
        private String displayName;

        /** . */
        private String description;

        public Builder() {
            this(false, null, null, null);
        }

        private Builder(boolean showMaxWindow, String factoryId, String displayName, String description) {
            this.showMaxWindow = showMaxWindow;
            this.factoryId = factoryId;
            this.displayName = displayName;
            this.description = description;
        }

        public Builder showMaxWindow(boolean showMaxWindow) {
            this.showMaxWindow = showMaxWindow;
            return this;
        }

        public Builder displayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder factoryId(String factoryId) {
            this.factoryId = factoryId;
            return this;
        }

        public PageState build() {
            return new PageState(displayName, description, showMaxWindow, factoryId);
        }
    }
}
