/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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

package org.exoplatform.portal.mop.management;

import org.exoplatform.portal.mop.management.binding.MopBindingProvider;
import org.exoplatform.portal.mop.management.operations.MopImportResource;
import org.exoplatform.portal.mop.management.operations.MopReadResource;
import org.exoplatform.portal.mop.management.operations.TemplateImportResource;
import org.exoplatform.portal.mop.management.operations.navigation.NavigationExportResource;
import org.exoplatform.portal.mop.management.operations.navigation.NavigationReadConfig;
import org.exoplatform.portal.mop.management.operations.navigation.NavigationReadResource;
import org.exoplatform.portal.mop.management.operations.page.PageExportResource;
import org.exoplatform.portal.mop.management.operations.page.PageReadConfig;
import org.exoplatform.portal.mop.management.operations.page.PageReadResource;
import org.exoplatform.portal.mop.management.operations.site.SiteLayoutExportResource;
import org.exoplatform.portal.mop.management.operations.site.SiteLayoutReadConfig;
import org.exoplatform.portal.mop.management.operations.site.SiteLayoutReadResource;
import org.exoplatform.portal.mop.management.operations.site.SiteReadResource;
import org.exoplatform.portal.mop.management.operations.site.SiteTypeReadResource;
import org.gatein.management.api.ComponentRegistration;
import org.gatein.management.api.ManagedDescription;
import org.gatein.management.api.ManagedResource;
import org.gatein.management.api.operation.OperationNames;
import org.gatein.management.spi.ExtensionContext;
import org.gatein.management.spi.ManagementExtension;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class MopManagementExtension implements ManagementExtension {
    @Override
    public void initialize(ExtensionContext context) {

        initializeMop(context);
        initializeTemplate(context);

    }

    private void initializeMop(ExtensionContext context) {
        ComponentRegistration registration = context.registerManagedComponent("mop");
        registration.registerBindingProvider(MopBindingProvider.INSTANCE);

        ManagedResource.Registration mop = registration
                .registerManagedResource(description("MOP (Model Object for Portal) Managed Resource, responsible for handling management operations on navigation, pages, and sites."));
        mop.registerOperationHandler(OperationNames.IMPORT_RESOURCE, new MopImportResource(),
                description("Imports mop data from an exported zip file."));

        mop.registerOperationHandler(OperationNames.READ_RESOURCE, new MopReadResource(),
                description("Lists available site types for a portal"));

        ManagedResource.Registration sitetypes = mop
                .registerSubResource(
                        "{site-type}sites",
                        description("Management resource responsible for handling management operations on a specific site type for a portal."));
        sitetypes.registerOperationHandler(OperationNames.READ_RESOURCE, new SiteTypeReadResource(),
                description("Lists available sites for a given site type."));

        ManagedResource.Registration sites = sitetypes.registerSubResource("{site-name: .*}",
                description("Management resource responsible for handling management operations on a specific site."));
        sites.registerOperationHandler(OperationNames.READ_RESOURCE, new SiteReadResource(),
                description("Lists available resources for a given site (ie pages, navigation, site layout)"));

        // Site Layout management
        siteLayoutManagementRegistration(sites);

        // Page management
        pageManagementRegistration(sites);

        // Navigation management
        navigationManagementRegistration(sites);
    }

    private void siteLayoutManagementRegistration(ManagedResource.Registration sites) {
        // This allows us to filter based on path template site-layout.
        ManagedResource.Registration siteLayout = sites.registerSubResource("{site-layout: portal|group|user}",
                description("Management resource responsible for handling management operations for a site's layout."));
        siteLayout.registerOperationHandler(OperationNames.READ_RESOURCE, new SiteLayoutReadResource(),
                description("The site layout resource."));
        siteLayout.registerOperationHandler(OperationNames.READ_CONFIG, new SiteLayoutReadConfig(),
                description("Reads site layout data for a specific site as configuration xml."));
        siteLayout.registerOperationHandler(OperationNames.EXPORT_RESOURCE, new SiteLayoutExportResource(),
                description("Exports site layout configuration xml as a zip file."));
    }

    private void pageManagementRegistration(ManagedResource.Registration sites) {
        // Pages management resource registration
        ManagedResource.Registration pages = sites.registerSubResource("pages",
                description("Management resource responsible for handling management operations for pages of a site."));

        // Pages management operations
        pages.registerOperationHandler(OperationNames.READ_RESOURCE, new PageReadResource(),
                description("Lists available pages at a specified address."), true);
        pages.registerOperationHandler(OperationNames.READ_CONFIG, new PageReadConfig(),
                description("Reads pages as configuration xml at a specified address."), true);
        pages.registerOperationHandler(OperationNames.EXPORT_RESOURCE, new PageExportResource(),
                description("Exports pages configuration xml as a zip file."), true);

        // Page name management resource registration
        pages.registerSubResource("{page-name}", description("Page resource representing an individual page of a site."));
    }

    private void navigationManagementRegistration(ManagedResource.Registration sites) {
        // Navigation management resource registration
        ManagedResource.Registration navigation = sites.registerSubResource("navigation",
                description("Management resource responsible for handling management operations on a sites navigation."));

        // Navigation management operations
        navigation.registerOperationHandler(OperationNames.READ_RESOURCE, new NavigationReadResource(),
                description("Available navigation nodes at the specified address."), true);
        navigation.registerOperationHandler(OperationNames.READ_CONFIG, new NavigationReadConfig(),
                description("Reads navigation as configuration xml at a specified address."), true);
        navigation.registerOperationHandler(OperationNames.EXPORT_RESOURCE, new NavigationExportResource(),
                description("Exports navigation configuration xml as a zip file."), true);

        // Navigation node management resource registration
        navigation
                .registerSubResource(
                        "{nav-uri: .*}",
                        description("Management resource responsible for handling management operations on specific navigation nodes."));
    }

    private void initializeTemplate(ExtensionContext context) {
        ComponentRegistration registration = context.registerManagedComponent("template");
        registration.registerBindingProvider(MopBindingProvider.INSTANCE);

        ManagedResource.Registration template = registration
                .registerManagedResource(description("Template Managed Resource, responsible for handling imports operations of templates sites."));

        ManagedResource.Registration templateType = template.registerSubResource("{template-type: portal|group|user}",
                description("Management resource responsible for handling management operations for a template's import."));

        templateType.registerOperationHandler(OperationNames.IMPORT_RESOURCE, new TemplateImportResource(),
                description("Imports template data from an zip file."));

    }

    @Override
    public void destroy() {
    }

    private static ManagedDescription description(final String description) {
        return new ManagedDescription() {
            @Override
            public String getDescription() {
                return description;
            }
        };
    }
}
