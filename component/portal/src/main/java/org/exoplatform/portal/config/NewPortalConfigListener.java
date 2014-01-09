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

package org.exoplatform.portal.config;

import java.util.Date;
import java.util.Set;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.mop.importer.Importer;
import org.exoplatform.portal.mop.site.SimpleDataCache;
import org.gatein.portal.mop.layout.LayoutService;
import org.gatein.portal.mop.permission.SecurityService;
import org.gatein.portal.mop.site.SiteKey;
import org.gatein.portal.mop.description.DescriptionService;
import org.exoplatform.portal.mop.importer.Imported;
import org.exoplatform.portal.mop.importer.Imported.Status;
import org.gatein.portal.mop.navigation.NavigationService;
import org.gatein.portal.mop.page.PageService;
import org.gatein.portal.mop.site.SiteService;
import org.exoplatform.portal.pom.config.POMSession;
import org.exoplatform.portal.pom.config.POMSessionManager;
import org.gatein.mop.api.workspace.Workspace;

/**
 * Created by The eXo Platform SARL Author : Tuan Nguyen tuan08@users.sourceforge.net May 22, 2006
 */

public class NewPortalConfigListener extends BaseComponentPlugin {

    /** . */
    private final POMSessionManager pomMgr;

    /** . */
    private final SiteService siteService;

    /** . */
    final Importer importer;

    public NewPortalConfigListener(
            final POMSessionManager pomMgr,
            PageService pageService,
            ConfigurationManager cmanager,
            InitParams params,
            LayoutService layoutService,
            SiteService siteService,
            SecurityService securityService,
            NavigationService navigationService,
            DescriptionService descriptionService) throws Exception {


        //
        this.siteService = siteService;
        this.pomMgr = pomMgr;
        this.importer = new Importer(
                pageService,
                cmanager,
                params,
                navigationService,
                descriptionService,
                layoutService,
                siteService,
                securityService);
    }

    private void touchImport() {
        RequestLifeCycle.begin(PortalContainer.getInstance());
        try {
            POMSession session = pomMgr.getSession();
            Workspace workspace = session.getWorkspace();
            Imported imported = workspace.adapt(Imported.class);
            imported.setLastModificationDate(new Date());
            imported.setStatus(Status.DONE.status());
            session.save();
        } finally {
            RequestLifeCycle.end();
        }
    }

    private boolean performImport() throws Exception {
        RequestLifeCycle.begin(PortalContainer.getInstance());
        try {

            POMSession session = pomMgr.getSession();

            // Obtain the status
            Workspace workspace = session.getWorkspace();
            boolean perform = !workspace.isAdapted(Imported.class);

            // We mark it
            if (perform) {
                Imported imported = workspace.adapt(Imported.class);
                imported.setCreationDate(new Date());

                // for legacy checking
                if (siteService.loadSite(SiteKey.portal(importer.getDefaultPortal())) != null) {
                    perform = false;
                    imported.setStatus(Status.DONE.status());
                } else {
                    importer.setFirstStartup(true);
                }
                session.save();
            } else {
                Imported imported = workspace.adapt(Imported.class);
                Integer st = imported.getStatus();
                if (st != null) {
                    Status status = Status.getStatus(st);
                    perform = (Status.WANT_REIMPORT == status);
                }
            }

            if (importer.isOverrideExistingData()) {
                return true;
            }

            //
            return perform;
        } finally {
            RequestLifeCycle.end();
        }
    }

    public void run() throws Exception {
        if (!performImport()) {
            return;
        }

        //
        importer.run();

        //
        touchImport();
    }

    String getDefaultPortal() {
        return importer.getDefaultPortal();
    }

    /**
     * This is used to merge an other NewPortalConfigListener to this one
     *
     * @param other the other
     */
    public void mergePlugin(NewPortalConfigListener other) {
        importer.mergePlugin(other.importer);
    }

    /**
     * This is used to delete an already loaded NewPortalConfigListener(s)
     *
     * @param other the other
     */
    public void deleteListenerElements(NewPortalConfigListener other) {
        importer.deleteListenerElements(other.importer);
    }

    public boolean createPortalConfig(NewPortalConfig config, String owner) throws Exception {
        return importer.createPortalConfig(config, owner);
    }

    public void createPage(NewPortalConfig config, String owner) throws Exception {
        importer.createPage(config, owner);
    }

    public void createPageNavigation(NewPortalConfig config, String owner) throws Exception {
        importer.createPageNavigation(config, owner);
    }

    public Page createPageFromTemplate(String ownerType, String owner, String temp) throws Exception {
        return importer.createPageFromTemplate(ownerType, owner, temp);
    }

    public String getTemplateConfig(String type, String name) {
        return importer.getTemplateConfig(type, name);
    }

    /**
     * Get all template configurations
     *
     * @param siteType (portal, group, user)
     * @return set of template name
     */
    public Set<String> getTemplateConfigs(String siteType) {
        return importer.getTemplateConfigs(siteType);
    }

    /**
     * Get detail configuration from a template file
     *
     * @param siteType (portal, group, user)
     * @param templateName name of template
     * @return PortalConfig object
     */
    public PortalConfig getPortalConfigFromTemplate(String siteType, String templateName) {
        return importer.getPortalConfigFromTemplate(siteType, templateName);
    }
}
