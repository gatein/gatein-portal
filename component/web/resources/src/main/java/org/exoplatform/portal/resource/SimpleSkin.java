/**
 * Copyright (C) 2009 eXo Platform SAS.
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

package org.exoplatform.portal.resource;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.services.resources.Orientation;
import org.exoplatform.web.ControllerContext;
import org.exoplatform.web.WebAppController;
import org.exoplatform.web.controller.QualifiedName;
import org.exoplatform.web.controller.router.URIWriter;
import org.exoplatform.web.url.MimeType;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.portal.controller.resource.ResourceRequestHandler;

/**
 * An implementation of the skin config.
 *
 * Created by The eXo Platform SAS Jan 19, 2007
 */
class SimpleSkin implements SkinConfig {

    private final String module_;

    private final String name_;

    private final String cssPath_;

    private final String id_;

    private final int priority;

    public SimpleSkin(SkinService service, String module, String name, String cssPath) {
        this(service, module, name, cssPath, Integer.MAX_VALUE);
    }

    public SimpleSkin(SkinService service, String module, String name, String cssPath, int cssPriority) {
        module_ = module;
        name_ = name;
        cssPath_ = cssPath;
        id_ = module.replace('/', '_');
        priority = cssPriority;
    }

    public int getCSSPriority() {
        return priority;
    }

    public String getId() {
        return id_;
    }

    public String getModule() {
        return module_;
    }

    public String getCSSPath() {
        return cssPath_;
    }

    public String getName() {
        return name_;
    }

    public String toString() {
        return "SimpleSkin[id=" + id_ + ",module=" + module_ + ",name=" + name_ + ",cssPath=" + cssPath_ + ", priority="
                + priority + "]";
    }

    public SkinURL createURL(final ControllerContext context) {
        if (context == null) {
            throw new NullPointerException("No controller context provided");
        }
        return new SkinURL() {

            Orientation orientation = null;
            boolean compress = !PropertyManager.isDevelopping();

            public void setOrientation(Orientation orientation) {
                this.orientation = orientation;
            }

            @Override
            public String toString() {
                try {
                    String resource = cssPath_.substring(1, cssPath_.length() - ".css".length());

                    //
                    Map<QualifiedName, String> params = new HashMap<QualifiedName, String>();
                    params.put(ResourceRequestHandler.VERSION_QN, ResourceRequestHandler.VERSION);
                    params.put(ResourceRequestHandler.ORIENTATION_QN, orientation == Orientation.RT ? "rt" : "lt");
                    params.put(ResourceRequestHandler.COMPRESS_QN, compress ? "min" : "");
                    params.put(WebAppController.HANDLER_PARAM, "skin");
                    params.put(ResourceRequestHandler.RESOURCE_QN, resource);
                    StringBuilder url = new StringBuilder();
                    context.renderURL(params, new URIWriter(url, MimeType.PLAIN));

                    //
                    return url.toString();
                } catch (IOException e) {
                    LoggerFactory.getLogger(this.getClass()).error(e.getMessage(), e);
                    return null;
                }
            }
        };
    }
}
