/*
 * Copyright (C) 2013 eXo Platform SAS.
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
package org.gatein.portal.importer;

import java.lang.reflect.Field;
import java.util.Date;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.portal.mop.PersistenceContext;
import org.exoplatform.portal.mop.importer.Imported;
import org.exoplatform.portal.pom.config.POMSession;
import org.exoplatform.portal.pom.config.POMSessionManager;
import org.gatein.mop.api.workspace.Workspace;
import org.gatein.portal.mop.description.DescriptionService;
import org.gatein.portal.mop.layout.LayoutService;
import org.gatein.portal.mop.navigation.NavigationService;
import org.gatein.portal.mop.page.PageService;
import org.gatein.portal.mop.permission.SecurityService;
import org.gatein.portal.mop.site.SiteKey;
import org.gatein.portal.mop.site.SiteService;
import org.gatein.portal.mop.site.SiteServiceImpl;

/**
 * ModelImporterDelegator delegates work to ModelImporter and enable us to use InitParams parsed by kernel
 *
 * @author <a href="hoang281283@gmail.com">Minh Hoang TO</a>
 * @date 1/25/13
 */
public class MopModelImporter {

    private final ConfigurationManager configurationManager;

    private final InitParams initParams;

    private SiteService siteService;

    private DescriptionService descriptionService;

    private PageService pageService;

    private NavigationService navigationService;

    private LayoutService layoutService;

    private SecurityService securityService;

    private ModelImporter modelImporter;

    private POMSessionManager pomMgr;

    private volatile boolean wired = false;

    public MopModelImporter(ConfigurationManager configMgr, InitParams initParams) {
        this.configurationManager = configMgr;
        this.initParams = initParams;
    }

    public void wireServices(PersistenceContext context) {
        if (!wired) {
            synchronized (this) {
                if (!wired) {
                    siteService = new SiteServiceImpl(context.getSiteStore());
                    descriptionService = context.getDescriptionService();
                    pageService = context.getPageService();
                    layoutService = context.getLayoutService();
                    navigationService = context.getNavigationService();
                    pomMgr = getPomMgr((PersistenceContext.JCR) context);
                    securityService = context.getSecurityService();

                    modelImporter = new ModelImporter(configurationManager, initParams, pageService, layoutService, descriptionService, navigationService, siteService, securityService);
                    wired = true;
                }
            }
        }
    }

    public void doImport() {
        if (!performImport()) {
            return;
        }

        modelImporter.start();
        touchImport();
    }

    private static POMSessionManager getPomMgr(PersistenceContext.JCR jcrContext) {
        try {
            Field f = jcrContext.getClass().getDeclaredField("mgr");
            f.setAccessible(true);
            return (POMSessionManager) f.get(jcrContext);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private boolean performImport() {
        try {
            PortalContainer pc = PortalContainer.getInstance();
            RequestLifeCycle.begin(pc);

            POMSession session = pomMgr.getSession();
            // Obtain the status
            Workspace workspace = session.getWorkspace();
            boolean perform = !workspace.isAdapted(Imported.class);

            // We mark it
            if (perform) {
                Imported imported = workspace.adapt(Imported.class);
                imported.setCreationDate(new Date());

                // for legacy checking
                if (siteService.loadSite(SiteKey.portal(modelImporter.getImporter().getDefaultPortal())) != null) {
                    perform = false;
                    imported.setStatus(Imported.Status.DONE.status());
                } else {
                    modelImporter.getImporter().setFirstStartup(true);
                }
                session.save();
            } else {
                Imported imported = workspace.adapt(Imported.class);
                Integer st = imported.getStatus();
                if (st != null) {
                    Imported.Status status = Imported.Status.getStatus(st);
                    perform = (Imported.Status.WANT_REIMPORT == status);
                }
            }

            if (modelImporter.getImporter().isOverrideExistingData()) {
                return true;
            }
            return perform;
        } catch (Exception ex) {
            return false;
        } finally {
            RequestLifeCycle.end();
        }
    }

    private void touchImport() {
        RequestLifeCycle.begin(PortalContainer.getInstance());
        try {
            POMSession session = pomMgr.getSession();
            Workspace workspace = session.getWorkspace();
            Imported imported = workspace.adapt(Imported.class);
            imported.setLastModificationDate(new Date());
            imported.setStatus(Imported.Status.DONE.status());
            session.save();
        } finally {
            RequestLifeCycle.end();
        }
    }

    public POMSessionManager getPOMSessionManager()
    {
        return pomMgr;
    }
}
