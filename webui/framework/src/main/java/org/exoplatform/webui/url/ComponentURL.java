/*
 * Copyright (C) 2010 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.exoplatform.webui.url;

import java.util.Collections;
import java.util.Set;

import org.exoplatform.web.controller.QualifiedName;
import org.exoplatform.web.url.PortalURL;
import org.exoplatform.web.url.ResourceType;
import org.exoplatform.web.url.URLContext;
import org.exoplatform.webui.core.UIComponent;
import org.gatein.common.util.Tools;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ComponentURL extends PortalURL<UIComponent, ComponentURL> {
    public static final String PORTAL_COMPONENT_ID = "portal:componentId";

    public static final String PORTAL_COMPONENT_ACTION = "portal:action";

    /** . */
    public static final ResourceType<UIComponent, ComponentURL> TYPE = new ResourceType<UIComponent, ComponentURL>() {
    };

    /** . */
    public static final QualifiedName PATH = QualifiedName.create("gtn", "path");

    /** . */
    private static final Set<QualifiedName> NAMES = Collections.unmodifiableSet(Tools.toSet(PATH));

    /** . */
    private UIComponent resource;

    /** . */
    private String action;

    /** . */
    private String path;

    public ComponentURL(URLContext context) throws NullPointerException {
        super(context);
    }

    public UIComponent getResource() {
        return resource;
    }

    public ComponentURL setResource(UIComponent resource) {
        this.resource = resource;

        if (resource != null) {
            setQueryParameterValue(PORTAL_COMPONENT_ID, resource.getId());
        }

        return this;
    }

    public void reset() {
        super.reset();

        //
        if (resource != null) {
            setQueryParameterValue(PORTAL_COMPONENT_ID, resource.getId());
        }
        setQueryParameterValue(PORTAL_COMPONENT_ACTION, action);
    }

    public Set<QualifiedName> getParameterNames() {
        return NAMES;
    }

    public String getParameterValue(QualifiedName parameterName) {
        if (PATH.equals(parameterName)) {
            return path;
        } else {
            return null;
        }
    }

    public void setAction(String action) {
        this.action = action;
        setQueryParameterValue(PORTAL_COMPONENT_ACTION, action);
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
