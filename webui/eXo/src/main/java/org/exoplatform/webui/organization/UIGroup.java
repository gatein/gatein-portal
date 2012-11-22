package org.exoplatform.webui.organization;

import java.io.Serializable;

import org.exoplatform.commons.utils.HTMLEntityEncoder;
import org.exoplatform.services.organization.Group;

public class UIGroup implements Serializable {

    private Group group;

    public UIGroup(Group group) {
        this.group = group;
    }

    public String getEncodedLabel() {
        return HTMLEntityEncoder.getInstance().encode(getLabel());
    }

    public String getLabel() {
        return group.getLabel();
    }

    public String getId() {
        return group.getId();
    }
}
