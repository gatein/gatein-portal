/*
 * Copyright (C) 2011 eXo Platform SAS.
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

package org.exoplatform.portal.mop.importer;

import org.exoplatform.portal.config.model.PortalConfig;
import org.gatein.portal.mop.site.SiteKey;
import org.gatein.portal.mop.hierarchy.NodeContext;
import org.exoplatform.portal.mop.layout.ElementState;
import org.exoplatform.portal.mop.layout.LayoutService;
import org.gatein.portal.mop.site.SiteContext;
import org.gatein.portal.mop.site.SiteService;
import org.exoplatform.portal.pom.data.ComponentData;
import org.exoplatform.portal.pom.data.ContainerAdapter;
import org.exoplatform.portal.pom.data.ContainerData;

/**
 * @author <a href="trongtt@gmail.com">Trong Tran</a>
 * @version $Revision$
 */
public class SiteImporter {

    /** . */
    private final PortalConfig src;

    /** . */
    private final SiteService siteService;

    /** . */
    private final LayoutService layoutService;

    /** . */
    private final ImportMode mode;

    public SiteImporter(ImportMode importMode, PortalConfig portal, SiteService siteService, LayoutService layoutService) {
        this.mode = importMode;
        this.src = portal;
        this.siteService = siteService;
        this.layoutService = layoutService;
    }

    public void perform() throws Exception {

        //
        SiteKey key = new SiteKey(src.getType(), src.getName());

        //
        SiteContext existing = siteService.loadSite(key);

        //
        SiteContext imported;
        switch (mode) {
            case CONSERVE:
                imported = null;
                break;
            case INSERT:
                if (existing == null) {
                    imported = new SiteContext(key, src.build().toState());
                } else {
                    imported = null;
                }
                break;
            case MERGE:
            case OVERWRITE:
                imported = new SiteContext(key, src.build().toState());
                break;
            default:
                throw new AssertionError();
        }

        //
        if (imported != null) {

            // Import site
            siteService.saveSite(imported);

            // Import layout
            ContainerData container = src.build().getPortalLayout();
            container = new ContainerData(
                    imported.getLayoutId(),
                    container.getId(),
                    container.getName(),
                    container.getIcon(),
                    container.getTemplate(),
                    container.getFactoryId(),
                    container.getTitle(),
                    container.getDescription(),
                    container.getWidth(),
                    container.getHeight(),
                    container.getAccessPermissions(),
                    container.getChildren()
            );

            // We cheat a bit with this cast
            // but well it's easier to do this way
            NodeContext<ComponentData, ElementState> ret = (NodeContext<ComponentData, ElementState>) layoutService.loadLayout(ElementState.model(), container.getStorageId(), null);

            // Save element
            layoutService.saveLayout(new ContainerAdapter(container), container, ret, null);
        }
    }
}
