package org.gatein.api.application;

import org.gatein.api.security.Permission;

/**
 * Represents an implementation of the public API contract representing an Application, which can be a Gadget, a Portlet
 * or a WSRP.
 *
 * Internally, it combines properties from the different sources of Application data, which means that this is a
 * best-effort into representing the persisted data.
 *
 * @see org.gatein.api.application.Application
 *
 * @author <a href="mailto:jpkroehling+javadoc@redhat.com">Juraci Paixão Kröhling</a>
 */
public class ApplicationImpl implements Application {

    private String id;
    private String applicationName;
    private String categoryName;
    private ApplicationType type;

    private String displayName;
    private String description;
    private String iconURL;
    private Permission accessPermission;

    public ApplicationImpl() {
    }

    public ApplicationImpl(String applicationName) {
        this.applicationName = applicationName;
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    @Override
    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    @Override
    public ApplicationType getType() {
        return type;
    }

    public void setType(ApplicationType type) {
        this.type = type;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String getIconURL() {
        return iconURL;
    }

    public void setIconURL(String iconURL) {
        this.iconURL = iconURL;
    }

    @Override
    public Permission getAccessPermission() {
        return accessPermission;
    }

    public void setAccessPermission(Permission accessPermission) {
        this.accessPermission = accessPermission;
    }

    @Override
    public String toString() {
        return "ApplicationImpl{" +
                "id='" + id + '\'' +
                ", applicationName='" + applicationName + '\'' +
                ", categoryName='" + categoryName + '\'' +
                ", type=" + type +
                ", displayName='" + displayName + '\'' +
                ", description='" + description + '\'' +
                ", iconURL='" + iconURL + '\'' +
                ", accessPermission=" + accessPermission +
                '}';
    }
}
