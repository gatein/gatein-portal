/*
 * Copyright (C) 2012 eXo Platform SAS.
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

import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.portal.mop.importer.Importer;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.portal.mop.description.DescriptionService;
import org.gatein.portal.mop.layout.LayoutService;
import org.gatein.portal.mop.navigation.NavigationService;
import org.gatein.portal.mop.page.PageService;
import org.gatein.portal.mop.permission.SecurityService;
import org.gatein.portal.mop.site.SiteService;
import org.picocontainer.Startable;

/**
 * For now basic import (no override done).
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class ModelImporter implements Startable {

    /** . */
    private final ConfigurationManager configManager;

    /** . */
    private final InitParams initParams;

    /** . */
    private final Logger logger = LoggerFactory.getLogger(ModelImporter.class);

    /** . */
    private PageService pageService;

    /** . */
    private LayoutService layoutService;

    /** . */
    private DescriptionService descriptionService;

    /** . */
    private NavigationService navigationService;

    /** . */
    private SiteService siteService;

    private Importer importer;

    public ModelImporter(
            ConfigurationManager configManager,
            InitParams initParams,
            PageService pageService,
            LayoutService layoutService,
            DescriptionService descriptionService,
            NavigationService navigationService,
            SiteService siteService,
            SecurityService securityService) {

        //
        this.configManager = configManager;
        this.initParams = initParams;
        this.pageService = pageService;
        this.layoutService = layoutService;
        this.descriptionService = descriptionService;
        this.navigationService = navigationService;
        this.siteService = siteService;
        if (initParams != null) {
            try {
                importer = new Importer(pageService,
                        configManager,
                        initParams,
                        navigationService,
                        descriptionService,
                        layoutService,
                        siteService,
                        securityService);
            } catch (Exception ex) {
                logger.error("Could not instantiate importer", ex);
            }
        }
    }

    @Override
    public void start() {

        if (initParams != null) {
            try {
                importer.run();
            } catch (Exception e) {
                logger.error("Could not import model", e);
            }
        } else {
            logger.debug("No init params found");
        }

    }

    @Override
    public void stop() {
    }

    public Importer getImporter() {
        return importer;
    }
}
