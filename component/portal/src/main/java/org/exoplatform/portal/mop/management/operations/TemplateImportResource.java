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

package org.exoplatform.portal.mop.management.operations;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.exoplatform.commons.chromattic.ChromatticManager;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.model.NavigationFragment;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PageNode;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.mop.description.DescriptionService;
import org.exoplatform.portal.mop.importer.ImportMode;
import org.exoplatform.portal.mop.management.exportimport.NavigationExportTask;
import org.exoplatform.portal.mop.management.exportimport.NavigationImportTask;
import org.exoplatform.portal.mop.management.exportimport.PageExportTask;
import org.exoplatform.portal.mop.management.exportimport.PageImportTask;
import org.exoplatform.portal.mop.management.exportimport.SiteLayoutExportTask;
import org.exoplatform.portal.mop.management.exportimport.SiteLayoutImportTask;
import org.exoplatform.portal.mop.management.operations.navigation.NavigationUtils;
import org.exoplatform.portal.mop.management.operations.page.PageUtils;
import org.exoplatform.portal.mop.navigation.NavigationContext;
import org.exoplatform.portal.mop.navigation.NavigationService;
import org.exoplatform.portal.mop.page.PageService;
import org.exoplatform.portal.pom.config.POMSession;
import org.exoplatform.portal.pom.config.POMSessionManager;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.Query;
import org.exoplatform.services.organization.User;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.management.api.ContentType;
import org.gatein.management.api.binding.Marshaller;
import org.gatein.management.api.exceptions.OperationException;
import org.gatein.management.api.exceptions.ResourceNotFoundException;
import org.gatein.management.api.operation.OperationAttachment;
import org.gatein.management.api.operation.OperationContext;
import org.gatein.management.api.operation.OperationHandler;
import org.gatein.management.api.operation.ResultHandler;
import org.gatein.management.api.operation.model.NoResultModel;
import org.gatein.mop.api.workspace.Site;
import org.gatein.mop.api.workspace.Workspace;

/**
 * @author <a href="mailto:lponce@redhat.com">Lucas Ponce</a>
 * @version $Revision$
 */
public class TemplateImportResource extends SecureOperationHandler implements OperationHandler {

    private static final Logger log = LoggerFactory.getLogger(TemplateImportResource.class);

    private static final Set<String> FILES;
    private static final Set<String> DIR;
    static {
        HashSet<String> files = new HashSet<String>(5);
        files.add("portal.xml");
        files.add("group.xml");
        files.add("user.xml");
        files.add("pages.xml");
        files.add("navigation.xml");
        FILES = files;
        HashSet<String> dir = new HashSet<String>(3);
        dir.add("portal");
        dir.add("group");
        dir.add("user");
        DIR = dir;
    }

    private final String OWNER = "@owner@";
    private final String CREATE = "create";

    @Override
    public void doExecute(final OperationContext operationContext, ResultHandler resultHandler)
            throws ResourceNotFoundException, OperationException {

        final String PORTAL = "portal";
        final String GROUP = "group";
        final String USER = "user";

        OperationAttributes attr = new OperationAttributes();

        initAttributes(operationContext, attr);

        List<MopTemplate> templates = readZip(operationContext, attr);

        BackendServices svc = new BackendServices();

        initBackendServices(operationContext, attr, svc);

        // Expands template with proper sites/groups/users and populates importMap to perform import operation
        Map<SiteKey, MopImport> importMap = null;
        if (PORTAL.equals(attr.importType)) {
            importMap = expandPortalTemplate(attr, svc, templates);
        } else if (GROUP.equals(attr.importType)) {
            importMap = expandGroupTemplate(attr, svc, templates);
        } else if (USER.equals(attr.importType)) {
            importMap = expandUserTemplate(attr, svc, templates);
        }

        validationRules(attr, importMap);

        OperationException importError = performImport(attr, importMap);

        endRequest(attr, svc, importError);

        resultHandler.completed(NoResultModel.INSTANCE);
    }

    private void initAttributes(OperationContext operationContext, OperationAttributes attr)
            throws OperationException {

        // Expected operationName == "import-resource"
        attr.operationName = operationContext.getOperationName();

        // Expected importType == {template-type: portal|group|user}
        attr.importType = operationContext.getAddress().resolvePathTemplate("template-type");

        // Expected mode == {merge (default), overwrite, conserve, insert}
        attr.mode = operationContext.getAttributes().getValue("importMode");
        if (attr.mode == null || "".equals(attr.mode))
            attr.mode = "merge";

        try {
            attr.importMode = ImportMode.valueOf(attr.mode.trim().toUpperCase());
        } catch (Exception e) {
            throw new OperationException(attr.operationName,
                                         "Unknown importMode " + attr.mode + " for " + attr.importType + " template import.");
        }

        // Expression for users. This option is valid when importType == user
        attr.targetExpr = operationContext.getAttributes().getValue("targetExpr");
        // List of users. Option valid when importType == user
        attr.targetUser = operationContext.getAttributes().getValues("targetUser");
        // Flag to create dashboard if user has not initialized it. Option valid when importType == user
        // dashboardMode == create means dashboard will be created if it doesn't exist on import
        attr.dashboardMode = operationContext.getAttributes().getValue("dashboardMode");
        // List of groups. Option valid when importType == group
        attr.targetGroup = operationContext.getAttributes().getValues("targetGroup");
        // List of sites. Option valid when importType == portal
        attr.targetSite = operationContext.getAttributes().getValues("targetSite");
    }

    private void initBackendServices(OperationContext operationContext, OperationAttributes attr, BackendServices svc)
            throws OperationException {

        svc.mgr = operationContext.getRuntimeContext().getRuntimeComponent(POMSessionManager.class);
        POMSession session = svc.mgr.getSession();
        if (session == null)
            throw new OperationException(attr.operationName, "MOP session was null");

        svc.workspace = session.getWorkspace();
        if (svc.workspace == null)
            throw new OperationException(attr.operationName, "MOP workspace was null");

        svc.dataStorage = operationContext.getRuntimeContext().getRuntimeComponent(DataStorage.class);
        if (svc.dataStorage == null)
            throw new OperationException(attr.operationName, "DataStorage was null");

        svc.pageService = operationContext.getRuntimeContext().getRuntimeComponent(PageService.class);
        if (svc.pageService == null)
            throw new OperationException(attr.operationName, "PageService was null");

        svc.navigationService = operationContext.getRuntimeContext().getRuntimeComponent(NavigationService.class);
        if (svc.navigationService == null)
            throw new OperationException(attr.operationName, "Navigation service was null");

        svc.descriptionService = operationContext.getRuntimeContext().getRuntimeComponent(
                DescriptionService.class);
        if (svc.descriptionService == null)
            throw new OperationException(attr.operationName, "Description service was null");

        svc.organizationService = operationContext.getRuntimeContext().getRuntimeComponent(OrganizationService.class);
        if (svc.organizationService == null)
            throw new OperationException(attr.operationName, "Organization service was null");

        svc.chromatticManager = operationContext.getRuntimeContext().getRuntimeComponent(ChromatticManager.class);
        if (svc.chromatticManager == null)
            throw new OperationException(attr.operationName, "Chromattic manager was null");

    }

    /**
     * It will have three possible layouts:
     *  a) portal templates:
     *      .zip/portal/template/
     *          <templateName1>/{portal,pages,navigation}.xml
     *          <templateName2>/{portal,pages,navigation}.xml
     *          ...
     *          <templateNameN>/{portal,pages,navigation}.xml
     *
     *      This layout allows to have 1..N templates
     *
     * b) group templates:
     *      .zip/group/template/{portal,pages,navigation}.xml
     *
     *      This layout only accepts 1 group template
     *
     * c) user templates:
     *      .zip/user/template/{portal,pages,navigation}.xml
     *
     *      This layout only accepts 1 group template
     *
     * Each template is mapped in a MopTemplate object containing unmarshalled representation of template.
     *
     * @param operationContext context used in doExecute()
     * @param attr attributes used for this operation
     * @return list of templates read from .zip
     * @throws OperationException
     */
    private List<MopTemplate> readZip(OperationContext operationContext, OperationAttributes attr)
            throws OperationException {

        ArrayList<MopTemplate> templates = new ArrayList<MopTemplate>();
        // Gets .zip attachment
        OperationAttachment attachment = operationContext.getAttachment(true);
        if (attachment == null)
            throw new OperationException(attr.operationName,
                                         "No attachment available for " + attr.importType + " template import.");

        InputStream inputStream = attachment.getStream();
        if (inputStream == null)
            throw new OperationException(attr.operationName,
                                         "No data stream available for " + attr.importType + " template import.");

        // Reads .zip file
        final NonCloseableZipInputStream zis = new NonCloseableZipInputStream(inputStream);
        ZipEntry entry;

        boolean portal = false, group = false, user = false, template = false;
        try {
            log.info("Preparing data for import.");
            while ((entry = zis.getNextEntry()) != null) {
                // Skip directories
                if (entry.isDirectory()) {
                    // Directory entries finish with "/"
                    String[] folders = entry.getName().split("/");
                    String name = folders[folders.length - 1];
                    // 1st directory entry
                    if (!(portal || group || user)) {
                        if (DIR.contains(name)) {
                            if ("portal".equals(name)) {
                                portal = true;
                                if (!"portal".equalsIgnoreCase(attr.importType))
                                    throw new ZipTemplateException(".zip contains a portal folder but not " +
                                                                   "/template/portal operation found, " +
                                                                   "instead /template/" + attr.importType + " .");
                            } else if ("group".equals(name)) {
                                group = true;
                                if (!"group".equalsIgnoreCase(attr.importType))
                                    throw new ZipTemplateException(".zip contains a group folder but not " +
                                                                   "/template/group operation found, " +
                                                                   "instead /template/" + attr.importType + " .");
                            } else if ("user".equals(name)) {
                                user = true;
                                if (!"user".equalsIgnoreCase(attr.importType))
                                    throw new ZipTemplateException(".zip contains a user folder but not " +
                                                                   "/template/user operation found, " +
                                                                   "instead /template/" + attr.importType + " .");
                            }
                        } else {
                            throw new ZipTemplateException(".zip contains a not valid folder: " + name + ". " +
                                                           "Expecting one of " + DIR);
                        }
                    } else {
                        // 2nd directory entry
                        if (!template) {
                            if ("template".equals(name)) {
                                template = true;
                                if (group) {
                                    MopTemplate groupTemplate = new MopTemplate();
                                    groupTemplate.templateType = SiteType.GROUP;
                                    templates.add(groupTemplate);
                                } else if (user) {
                                    MopTemplate userTemplate = new MopTemplate();
                                    userTemplate.templateType = SiteType.USER;
                                    templates.add(userTemplate);
                                }
                            } else
                                throw new ZipTemplateException(".zip does not contains a template folder under " +
                                                               "portal/group/user folder, instead " + name + " .");
                        } else {
                            // 3rd directory
                            if (portal) {
                                MopTemplate portalTemplate = new MopTemplate();
                                portalTemplate.templateName = name;
                                portalTemplate.templateType = SiteType.PORTAL;
                                templates.add(portalTemplate);
                            } else {
                                throw new ZipTemplateException(".zip contains a folder under {portal,group,user}/template/ " +
                                                               "layout. It is expected one of " + FILES);
                            }
                        }
                    }
                    continue;
                }

                // Parses zip entry
                String[] parts = parseEntry(entry, portal);
                // Expected filesystem as
                //      portal/template/<templateName>/{portal,pages,navigation}.xml
                //      group/template/{group,pages,navigation}.xml
                //      user/template/{user,pages.navigation}.xml
                String file;
                if (portal)
                    file = parts[3];
                else
                    file = parts[2];

                // Validates file name
                if (!FILES.contains(file)) {
                    log.warn(".zip contains a not valid template file: " + file + ". Skipping...");
                    continue;
                }

                // Validates that a portal.xml is used with /template/portal operation
                if ("portal.xml".equals(file) && !portal) {
                    log.warn("portal.xml in .zip but not /template/portal operation found, " +
                            "instead /template/" + attr.importType + " . Skipping... ");
                    continue;
                }

                // Validates that a group.xml is used with /template/group operation
                if ("group.xml".equals(file) && !group) {
                    log.warn("group.xml in .zip but not /template/group operation found, " +
                            "instead /template/" + attr.importType + " . Skipping...");
                    continue;
                }

                // Validates that a user.xml is used with /template/user operation
                if ("user.xml".equals(file) && !user) {
                    log.warn("user.xml in .zip but not /template/user operation found, " +
                            "instead /template/" + attr.importType + " . Skipping... ");
                    continue;
                }

                // Templates are unmarshalled in generic objects, pattern substitution will be performed later
                if (SiteLayoutExportTask.FILES.contains(file)) {
                    Marshaller<PortalConfig> marshaller = operationContext.getBindingProvider()
                            .getMarshaller(PortalConfig.class, ContentType.XML);
                    templates.get(templates.size() - 1).portalConfig = marshaller.unmarshal(zis);
                } else if (file.equals(PageExportTask.FILE)) {
                    Marshaller<Page.PageSet> marshaller = operationContext.getBindingProvider()
                            .getMarshaller(Page.PageSet.class, ContentType.XML);
                    templates.get(templates.size() - 1).pageSet = marshaller.unmarshal(zis);
                } else if (file.equals(NavigationExportTask.FILE)) {
                    Marshaller<PageNavigation> marshaller = operationContext.getBindingProvider()
                            .getMarshaller(PageNavigation.class, ContentType.XML);
                    templates.get(templates.size() - 1).pageNavigation = marshaller.unmarshal(zis);
                }
            }

        } catch (Throwable t) {
            throw new OperationException(operationContext.getOperationName(), "Exception reading data for import.", t);
        } finally {
            try {
                zis.reallyClose();
            } catch (IOException e) {
                log.warn("Exception closing underlying data stream from import.");
            }
        }

        return templates;
    }

    private Map<SiteKey, MopImport> expandPortalTemplate(OperationAttributes attr,
                                                         BackendServices svc,
                                                         List<MopTemplate> templates)
            throws OperationException {

        Map<SiteKey, MopImport> expanded = new HashMap<SiteKey, MopImport>();
        try {
            // Pairs of (portalName, MopTemplate) filtered by targetSite
            Map<String, MopTemplate> portalNamesTemplates = filterPortalNames(attr, svc, templates);

            for (Map.Entry<String, MopTemplate> portalNameTemplate : portalNamesTemplates.entrySet()) {
                String portalName = portalNameTemplate.getKey();
                MopTemplate mopTemplate = portalNameTemplate.getValue();
                SiteKey newSiteKey = new SiteKey(SiteType.PORTAL, portalName);
                MopImport newMopImport = new MopImport();

                resolveTemplate(svc, newSiteKey, newMopImport, mopTemplate, portalName);

                expanded.put(newSiteKey, newMopImport);
            }

        } catch (Throwable t) {
            throw new OperationException("Import portal template", t.getMessage(), t);
        }

        return expanded;
    }

    private Map<String, MopTemplate> filterPortalNames(OperationAttributes attr,
                                                       BackendServices svc,
                                                       List<MopTemplate> templates)
            throws Exception {

        Map<String, MopTemplate> portalTemplates = new HashMap<String, MopTemplate>();
        List<String> portalNames = svc.dataStorage.getAllPortalNames();

        // Filters portals defined in targetSite attributes
        if (attr.targetSite.size() > 0) {
            Iterator<String> iterPortalNames = portalNames.iterator();
            while (iterPortalNames.hasNext()) {
                String name = iterPortalNames.next();
                boolean found = false;
                for (String targetSite : attr.targetSite) {
                    if (targetSite.equals(name)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    iterPortalNames.remove();
                }
            }
        }

        // Attaches portal templates
        for (String portalName : portalNames) {
            PortalConfig pConfig = svc.dataStorage.getPortalConfig(portalName);
            if (pConfig != null) {
                String portalTemplate = pConfig.getProperty("template");
                if (portalTemplate == null) {
                    log.warn("Portal " + portalName + " has not template property. " +
                            "It will not used in template import operation");
                } else {
                    for (MopTemplate mopTemplate : templates) {
                        if (mopTemplate.templateName.equals(portalTemplate)) {
                            portalTemplates.put(portalName, mopTemplate);
                            break;
                        }
                    }
                }
            }
        }

        return portalTemplates;
    }

    private void resolveTemplate(final BackendServices svc,
                                 SiteKey siteKey,
                                 MopImport mopImport,
                                 MopTemplate mopTemplate,
                                 String owner)
            throws Throwable {

        if (mopTemplate.portalConfig != null) {
            PortalConfig portalConfig = applyPattern(mopTemplate.portalConfig, owner);
            portalConfig.setType(siteKey.getTypeName());
            mopImport.siteTask = new SiteLayoutImportTask(portalConfig, siteKey, svc.dataStorage);
        }

        if (mopTemplate.pageSet != null) {
            Page.PageSet pageSet = applyPattern(mopTemplate.pageSet, owner);
            for (Page page : pageSet.getPages()) {
                page.setOwnerType(siteKey.getTypeName());
                page.setOwnerId(siteKey.getName());
            }
            // Obtains the site from the session when it's needed.
            MOPSiteProvider siteProvider = new MOPSiteProvider() {
                @Override
                public Site getSite(SiteKey siteKey) {
                    return svc.mgr.getSession().getWorkspace()
                            .getSite(Utils.getObjectType(siteKey.getType()), siteKey.getName());
                }
            };
            mopImport.pageTask = new PageImportTask(pageSet, siteKey, svc.dataStorage, svc.pageService, siteProvider);
        }

        if (mopTemplate.pageNavigation != null) {
            PageNavigation pageNavigation = applyPattern(mopTemplate.pageNavigation, owner);
            pageNavigation.setOwnerType(siteKey.getTypeName());
            pageNavigation.setOwnerId(siteKey.getName());
            mopImport.navigationTask = new NavigationImportTask(pageNavigation, siteKey, svc.navigationService,
                    svc.descriptionService, svc.dataStorage);
        }

    }

    /**
     * Applies "@owner@" pattern on following PortalConfig fields:
     * - name
     * - accessPermissions
     * - editPermissions
     *
     * "@owner@" pattern is not expected outside these fields.
     *
     * Note:
     * If pattern can appear in more fields may be it is better to store template as xml representation
     * and unmarshall after pattern replace
     *
     * @param template PortalConfig created from a xml template
     * @param owner final user who replace "@owner@" string in xml template
     * @return new PortalConfig object with @owner@ replaced
     */
    private PortalConfig applyPattern(PortalConfig template, String owner) {

        PortalConfig portalConfig = PageUtils.copy(template);

        if (portalConfig.getName() != null) {
            portalConfig.setName( portalConfig.getName().replaceAll(OWNER, owner) );
        }

        if (portalConfig.getAccessPermissions() != null) {
            for (int i=0; i<portalConfig.getAccessPermissions().length; i++) {
                if (portalConfig.getAccessPermissions()[i] != null) {
                    portalConfig.getAccessPermissions()[i] = portalConfig.getAccessPermissions()[i].replaceAll(OWNER, owner);
                }
            }
        }

        if (portalConfig.getEditPermission() != null) {
            portalConfig.setEditPermission( portalConfig.getEditPermission().replaceAll(OWNER, owner) );
        }

        return portalConfig;
    }

    /**
     * Applies "@owner@" pattern on following PageSet fields:
     * - name
     * - title
     * - accessPermissions
     * - editPermissions
     *
     * "@owner@" pattern is not expected outside these fields.
     *
     * Note:
     * If pattern can appear in more fields may be it is better to store template as xml representation
     * and unmarshall after pattern replace
     *
     * @param template PageSet created from a xml template
     * @param owner final user who replace "@owner@" string in xml template
     * @return new PageSet object with @owner@ replaced
     */
    private Page.PageSet applyPattern(Page.PageSet template, String owner) {

        Page.PageSet pageSet = PageUtils.copy(template);

        for (Page page : pageSet.getPages()) {
            if (page.getName() != null) {
                page.setName( page.getName().replaceAll(OWNER, owner) );
            }

            if (page.getTitle() != null) {
                page.setTitle( page.getTitle().replaceAll(OWNER, owner) );
            }

            if (page.getAccessPermissions() != null) {
                for (int i=0; i<page.getAccessPermissions().length; i++) {
                    if (page.getAccessPermissions()[i] != null) {
                        page.getAccessPermissions()[i] = page.getAccessPermissions()[i].replaceAll(OWNER, owner);
                    }
                }
            }

            if (page.getEditPermission() != null) {
                page.setEditPermission( page.getEditPermission().replaceAll(OWNER, owner) );
            }
        }

        return pageSet;
    }

    /**
     * Applies "@owner@" pattern on following PageNavigation fields:
     * - pageReference of PageNode objects
     *
     * "@owner@" pattern is not expected outside these fields.
     *
     * Note:
     * If pattern can appear in more fields may be it is better to store template as xml representation
     * and unmarshall after pattern replace
     *
     * @param template PageNavigation created from a xml template
     * @param owner final user who replace "@owner@" string in xml template
     * @return new PageNavigation object with @owner@ replaced
     */
    private PageNavigation applyPattern(PageNavigation template, String owner) {

        PageNavigation pageNavigation = NavigationUtils.copy(template);

        for (NavigationFragment fragment : pageNavigation.getFragments()) {
            for (PageNode pageNode : fragment.getNodes()) {
                applyPattern(pageNode, owner);
            }
        }

        return pageNavigation;
    }

    private void applyPattern(PageNode pageNode, String owner) {

        if (pageNode.getPageReference() != null) {
            pageNode.setPageReference( pageNode.getPageReference().replace(OWNER, owner) );
        }

        if (pageNode.getChildren() != null) {
            for (PageNode child : pageNode.getChildren()) {
                applyPattern(child, owner);
            }
        }

    }

    private Map<SiteKey, MopImport> expandGroupTemplate(OperationAttributes attr,
                                                        BackendServices svc,
                                                        List<MopTemplate> templates) {

        Map<SiteKey, MopImport> expanded = new HashMap<SiteKey, MopImport>();
        try {
            // Pairs of (groupName, MopTemplate) filtered by targetGroup
            Map<String, MopTemplate> groupNamesTemplates = filterGroupNames(attr, svc, templates);

            for (Map.Entry<String, MopTemplate> groupNameTemplate : groupNamesTemplates.entrySet()) {
                String groupName = groupNameTemplate.getKey();
                MopTemplate mopTemplate = groupNameTemplate.getValue();
                SiteKey newSiteKey = new SiteKey(SiteType.GROUP, groupName);
                MopImport newMopImport = new MopImport();

                resolveTemplate(svc, newSiteKey, newMopImport, mopTemplate, groupName);

                expanded.put(newSiteKey, newMopImport);
            }

        } catch (Throwable t) {
            throw new OperationException("Import group template", t.getMessage(), t);
        }

        return expanded;
    }

    private Map<String, MopTemplate> filterGroupNames(OperationAttributes attr,
                                                      BackendServices svc,
                                                      List<MopTemplate> templates)
            throws Exception {

        Map<String, MopTemplate> groupTemplates = new HashMap<String, MopTemplate>();
        List<String> groupNames = svc.dataStorage.getAllGroupNames();

        // Filters groups defined in targetGroup attributes
        if (attr.targetGroup.size() > 0) {
            Iterator<String> iterGroupNames = groupNames.iterator();
            while (iterGroupNames.hasNext()) {
                String name = iterGroupNames.next();
                if (!attr.targetGroup.contains(name)) {
                    iterGroupNames.remove();
                }
            }
        }

        // Filters groups with Navigation activated
        Iterator<String> iterGroupNames = groupNames.iterator();
        while (iterGroupNames.hasNext()) {
            String name = iterGroupNames.next();
            NavigationContext navigation = svc.navigationService.loadNavigation(SiteKey.group(name));
            if (navigation == null || navigation.getState() == null) {
                iterGroupNames.remove();
            }
        }

        for (String groupName : groupNames) {
            for (MopTemplate mopTemplate : templates) {
                if (mopTemplate.templateType.equals(SiteType.GROUP)) {
                    groupTemplates.put(groupName, mopTemplate);
                }
            }
        }

        return groupTemplates;
    }

    private Map<SiteKey, MopImport> expandUserTemplate(OperationAttributes attr,
                                                       BackendServices svc,
                                                       List<MopTemplate> templates) {

        Map<SiteKey, MopImport> expanded = new HashMap<SiteKey, MopImport>();
        try {
            // Pairs of (userName, MopTemplate) filtered by groups defined in targetUser attribute
            Map<String, MopTemplate> userNamesTemplates = filterUserNames(attr, svc, templates);

            for (Map.Entry<String, MopTemplate> userNameTemplate : userNamesTemplates.entrySet()) {
                String userName = userNameTemplate.getKey();
                MopTemplate mopTemplate = userNameTemplate.getValue();
                SiteKey newSiteKey = new SiteKey(SiteType.USER, userName);
                MopImport newMopImport = new MopImport();

                resolveTemplate(svc, newSiteKey, newMopImport, mopTemplate, userName);

                expanded.put(newSiteKey, newMopImport);
            }

        } catch (Throwable t) {
            throw new OperationException("Import user template", t.getMessage(), t);
        }

        return expanded;
    }

    private Map<String, MopTemplate> filterUserNames(OperationAttributes attr,
                                                     BackendServices svc,
                                                     List<MopTemplate> templates)
            throws Throwable {

        Map<String, MopTemplate> userTemplates = new HashMap<String, MopTemplate>();
        List<String> userNames = new ArrayList<String>();

        ListAccess<User> lUsers;

        // targetExpr attribute has preference over targetUser attribute
        if (attr.targetExpr != null && !"".equals(attr.targetExpr)) {
            Query qUsers = new Query();
            qUsers.setUserName(attr.targetExpr);
            lUsers = svc.organizationService.getUserHandler().findUsersByQuery(qUsers);
            for (User u : lUsers.load(0, lUsers.getSize())) {
                userNames.add(u.getUserName());
            }

        } else if (attr.targetUser != null && attr.targetUser.size() > 0) {
            // Validates that a user exists
            for (String u : attr.targetUser) {
                User user = svc.organizationService.getUserHandler().findUserByName(u);
                if (user != null) {
                    userNames.add(user.getUserName());
                }
            }
        } else {
            lUsers = svc.organizationService.getUserHandler().findAllUsers();
            for (User u : lUsers.load(0, lUsers.getSize())) {
                userNames.add(u.getUserName());
            }
        }

        if (attr.dashboardMode == null || !CREATE.equalsIgnoreCase(attr.dashboardMode)) {
            // Filters users with dashboard created
            Iterator<String> iterNames = userNames.iterator();
            while (iterNames.hasNext()) {
                String userName = iterNames.next();
                PortalConfig portalConfig = svc.dataStorage.getPortalConfig("user", userName);
                if (portalConfig == null) {
                    iterNames.remove();
                }
            }
        }

        for (String userName : userNames) {
            for (MopTemplate mopTemplate : templates) {
                if (mopTemplate.templateType.equals(SiteType.USER)) {
                    userTemplates.put(userName, mopTemplate);
                }
            }
        }

        return userTemplates;
    }

    private void validationRules(OperationAttributes attr, Map<SiteKey, MopImport> importMap) {

        for (Map.Entry<SiteKey, MopImport> mopImportEntry : importMap.entrySet()) {
            SiteKey siteKey = mopImportEntry.getKey();
            MopImport mopImport = mopImportEntry.getValue();

            // Rule #1: Send a warning if navigation.xml alone
            if (mopImport.siteTask == null &&
                mopImport.pageTask == null &&
                mopImport.navigationTask != null) {
                log.warn("Importing a template with only navigation.xml file. " +
                        "You should validate <page-reference> points to valid pages, " +
                        "if not this can create unstable references.");
            }

            // Rule #2: Send a warning if navigation.xml <page-reference> doesn't match with siteKey type
            List<String> refPages = new ArrayList<String>();
            if (mopImport.navigationTask != null) {
                PageNavigation pageNavigation = mopImport.navigationTask.getData();
                SiteType siteType = siteKey.getType();
                for (NavigationFragment fragment : pageNavigation.getFragments()) {
                    validateNodeType(siteType, fragment.getNodes(), refPages);
                }
            }

            // Rule #3: Send a warning if navigation.xml has <page-reference> without pointing to pages.xml
            if (mopImport.pageTask != null &&
                mopImport.navigationTask != null) {
                Page.PageSet pageSet = mopImport.pageTask.getData();
                if (pageSet != null) {
                    for (Page page : pageSet.getPages()) {
                        String name = page.getName();
                        if (!refPages.contains(name)) {
                            log.warn("pages.xml contains <page> not referenced on navigation.xml");
                        }
                    }
                }
            }

            // Rule #4: Send a warning if dashboardMode == create and missing some {user,pages,navigation}.xml file
            if (siteKey.getType() == SiteType.USER &&
                CREATE.equalsIgnoreCase(attr.dashboardMode) &&
                ( mopImport.siteTask == null ||
                  mopImport.pageTask == null ||
                  mopImport.navigationTask == null )) {
                log.warn("dashboardMode == " + attr.dashboardMode +
                         " and missing some {user,pages,navigation}.xml in .zip file.");
            }
        }

    }

    private void validateNodeType(SiteType siteType, List<PageNode> nodes, List<String> refPages) {

        if (nodes != null) {
            for (PageNode node : nodes) {
                String pageReference = node.getPageReference();
                if (pageReference != null) {
                    if (pageReference.startsWith("portal") && siteType != SiteType.PORTAL) {
                        log.warn("Detected navigation.xml with <page-reference> pointing to a portal pages for a site type "
                                + siteType + ".");
                    } else if (pageReference.startsWith("group") && siteType != SiteType.GROUP) {
                        log.warn("Detected navigation.xml with <page-reference> pointing to a group pages for a site type "
                                + siteType + ".");
                    } else if (pageReference.startsWith("user") && siteType != SiteType.USER) {
                        log.warn("Detected navigation.xml with <page-reference> pointing to a user pages for a site type "
                                + siteType + ".");
                    }
                    // Save referenced page for other validations
                    String[] chunks = pageReference.split("::");
                    if (chunks.length == 3) {
                        refPages.add(chunks[2]);
                    }
                }
                if (node.getChildren() != null) {
                    validateNodeType(siteType, node.getChildren(), refPages);
                }
            }
        }

    }

    private OperationException performImport(OperationAttributes attr, Map<SiteKey, MopImport> importMap)
            throws OperationException {

        // Performs import
        Map<SiteKey, MopImport> importsRan = new HashMap<SiteKey, MopImport>();
        OperationException importError = null;
        try {
            log.info("Performing import using importMode '" + attr.importMode + "'");
            for (Map.Entry<SiteKey, MopImport> mopImportEntry : importMap.entrySet()) {
                SiteKey siteKey = mopImportEntry.getKey();
                MopImport mopImport = mopImportEntry.getValue();
                MopImport ran = new MopImport();

                if (importsRan.containsKey(siteKey)) {
                    throw new IllegalStateException("Multiple site imports for same operation.");
                }
                importsRan.put(siteKey, ran);

                log.debug("Importing data for site " + siteKey);

                // Site layout import
                if (mopImport.siteTask != null) {
                    log.debug("Importing site layout data.");
                    ran.siteTask = mopImport.siteTask;
                    mopImport.siteTask.importData(attr.importMode);
                }

                // Pages import
                if (mopImport.pageTask != null) {
                    log.debug("Importing page data.");
                    ran.pageTask = mopImport.pageTask;
                    mopImport.pageTask.importData(attr.importMode);
                }

                // Navigation import
                if (mopImport.navigationTask != null) {
                    log.debug("Importing navigation data.");
                    ran.navigationTask = mopImport.navigationTask;
                    mopImport.navigationTask.importData(attr.importMode);
                }
            }
            log.info("Import successful !");
        } catch (Throwable t) {
            boolean rollbackSuccess = true;
            log.error("Exception importing data.", t);
            log.info("Attempting to rollback data modified by import.");
            for (Map.Entry<SiteKey, MopImport> mopImportEntry : importsRan.entrySet()) {
                SiteKey siteKey = mopImportEntry.getKey();
                MopImport mopImport = mopImportEntry.getValue();

                log.debug("Rolling back imported data for site " + siteKey);
                if (mopImport.navigationTask != null) {
                    log.debug("Rolling back navigation modified during import...");
                    try {
                        mopImport.navigationTask.rollback();
                    } catch (Throwable t1) // Continue rolling back even though there are exceptions.
                    {
                        rollbackSuccess = false;
                        log.error("Error rolling back navigation data for site " + siteKey, t1);
                    }
                }
                if (mopImport.pageTask != null) {
                    log.debug("Rolling back pages modified during import...");
                    try {
                        mopImport.pageTask.rollback();
                    } catch (Throwable t1) // Continue rolling back even though there are exceptions.
                    {
                        rollbackSuccess = false;
                        log.error("Error rolling back page data for site " + siteKey, t1);
                    }
                }
                if (mopImport.siteTask != null) {
                    log.debug("Rolling back site layout modified during import...");
                    try {
                        mopImport.siteTask.rollback();
                    } catch (Throwable t1) // Continue rolling back even though there are exceptions.
                    {
                        rollbackSuccess = false;
                        log.error("Error rolling back site layout for site " + siteKey, t1);
                    }
                }
            }

            String message = (rollbackSuccess) ?
                    "Error during import. Tasks successfully rolled back. Portal should be back to consistent state."
                    : "Error during import. Errors in rollback as well. Portal may be in an inconsistent state.";

            importError = new OperationException(attr.operationName, message, t);
        } finally {
            importMap.clear();
            importsRan.clear();
        }

        return importError;
    }

    // See GTNPORTAL-3257
    private static void endRequest(OperationAttributes attr, BackendServices svc, OperationException importError) {

        OperationException error = importError;
        try {
            // End the request to flush out anything that might go wrong when finalizing the request.
            svc.chromatticManager.endRequest(true);
        } catch (Throwable t) {
            // This allows us to properly respond with an error (500 in REST scenario) if ChromatticManager.endRequest fails
            if (importError == null) {
                log.error("Exception occurred ending the request of ChromatticManager after a successful import.", t);
                error = new OperationException(attr.operationName,
                                               "An exception occurred after a successful import. " +
                                               "See server logs for more details");
            } else {
                log.error("Exception occurred ending the request of ChromatticManager after a failed import.", t);
            }
        } finally {
            // Start it again, as the calling container ends all ComponentRequestLifecycle's,
            // and we don't want the end to be called w/out it beginning again.
            svc.chromatticManager.beginRequest();
        }

        if (error != null) {
            throw error;
        }

    }

    private static String[] parseEntry(ZipEntry entry, boolean portal)
            throws IOException {

        String name = entry.getName();
        if (isSiteLayoutEntry(name) || name.endsWith(PageExportTask.FILE) || name.endsWith(NavigationExportTask.FILE)) {
            String[] parts;
            if (portal) {
                parts = new String[4];
                parts[0] = name.substring(0, name.indexOf("/"));
                parts[1] = name.substring(parts[0].length() + 1, name.indexOf("/", parts[0].length() + 1));
                parts[2] = name.substring(parts[0].length() + parts[1].length() + 2, name.indexOf("/", parts[0].length()
                        + parts[1].length() + 3));
                parts[3] = name.substring(name.lastIndexOf("/") + 1);
            } else {
                parts = new String[3];
                parts[0] = name.substring(0, name.indexOf("/"));
                parts[1] = name.substring(parts[0].length() + 1, name.lastIndexOf("/"));
                parts[2] = name.substring(name.lastIndexOf("/") + 1);
            }
            return parts;
        } else {
            throw new IOException("Unknown entry " + name + " in zip file.");
        }

    }

    private static boolean isSiteLayoutEntry(String zipEntryName) {

        for (String file : SiteLayoutExportTask.FILES) {
            if (zipEntryName.endsWith(file))
                return true;
        }

        return false;
    }

    // Bug in SUN's JDK XMLStreamReader implementation closes the underlying stream when
    // it finishes reading an XML document. This is no good when we are using a ZipInputStream.
    // See http://bugs.sun.com/view_bug.do?bug_id=6539065 for more information.
    private static class NonCloseableZipInputStream extends ZipInputStream {

        private NonCloseableZipInputStream(InputStream inputStream) {
            super(inputStream);
        }

        @Override
        public void close() throws IOException {
        }

        private void reallyClose() throws IOException {
            super.close();
        }

    }

    private static class MopImport {

        private SiteLayoutImportTask siteTask;
        private PageImportTask pageTask;
        private NavigationImportTask navigationTask;

    }

    private static class OperationAttributes {

        private String operationName;
        private String importType;
        private String mode;
        private ImportMode importMode;
        private String targetExpr;
        private List<String> targetUser;
        private String dashboardMode;
        private List<String> targetGroup;
        private List<String> targetSite;

    }

    private static class BackendServices {

        private Workspace workspace = null;
        private DataStorage dataStorage = null;
        private PageService pageService = null;
        private NavigationService navigationService = null;
        private DescriptionService descriptionService = null;
        private POMSessionManager mgr = null;
        private OrganizationService organizationService = null;
        private ChromatticManager chromatticManager = null;

    }

    private static class MopTemplate {

        private String templateName;
        private SiteType templateType;
        private PortalConfig portalConfig;
        private Page.PageSet pageSet;
        private PageNavigation pageNavigation;

    }

    private static class ZipTemplateException extends Exception {

        public ZipTemplateException(String message) {
            super(message);
        }
    }
}