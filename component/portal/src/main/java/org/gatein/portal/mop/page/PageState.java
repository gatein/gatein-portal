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
    final List<String> editPermissions;

    /** . */
    final boolean showMaxWindow;

    /** . */
    final String factoryId;

    /** . */
    final String displayName;

    /** . */
    final String description;

    /** . */
    final List<String> accessPermissions;

    public PageState(String displayName, String description, boolean showMaxWindow, String factoryId,
            List<String> accessPermissions, List<String> editPermissions) {
        this.editPermissions = editPermissions;
        this.showMaxWindow = showMaxWindow;
        this.factoryId = factoryId;
        this.displayName = displayName;
        this.description = description;
        this.accessPermissions = accessPermissions;
    }

    public List<String> getEditPermissions() {
        return editPermissions;
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

    public List<String> getAccessPermissions() {
        return accessPermissions;
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
        return Safe.equals(editPermissions, that.editPermissions) && showMaxWindow == that.showMaxWindow
                && Safe.equals(factoryId, that.factoryId) && Safe.equals(displayName, that.displayName)
                && Safe.equals(description, that.description) && Safe.equals(accessPermissions, that.accessPermissions);
    }

    @Override
    public int hashCode() {
        int result = editPermissions != null ? editPermissions.hashCode() : 0;
        result = 31 * result + (showMaxWindow ? 1 : 0);
        result = 31 * result + (factoryId != null ? factoryId.hashCode() : 0);
        result = 31 * result + (displayName != null ? displayName.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (accessPermissions != null ? accessPermissions.hashCode() : 0);
        return result;
    }

    public Builder builder() {
        return new Builder(editPermissions, showMaxWindow, factoryId, displayName, description, accessPermissions);
    }

    public static class Builder {

        /** . */
        private List<String> editPermissions;

        /** . */
        private boolean showMaxWindow;

        /** . */
        private String factoryId;

        /** . */
        private String displayName;

        /** . */
        private String description;

        /** . */
        private List<String> accessPermissions;

        public Builder() {
            this(null,
                    false,
                    null,
                    null,
                    null,
                    new ArrayList<String>());
        }

        private Builder(List<String> editPermissions, boolean showMaxWindow, String factoryId, String displayName, String description,
                List<String> accessPermissions) {
            this.editPermissions = editPermissions;
            this.showMaxWindow = showMaxWindow;
            this.factoryId = factoryId;
            this.displayName = displayName;
            this.description = description;
            this.accessPermissions = accessPermissions;
        }

        public Builder editPermissions(List<String> editPermissions) {
            this.editPermissions = editPermissions;
            return this;
        }

        public Builder accessPermissions(List<String> accessPermissions) {
            this.accessPermissions = accessPermissions;
            return this;
        }

        public Builder accessPermissions(String... accessPermissions) {
            this.accessPermissions = new ArrayList<String>(Arrays.asList(accessPermissions));
            return this;
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
            return new PageState(displayName, description, showMaxWindow, factoryId, accessPermissions, editPermissions);
        }
    }
}
