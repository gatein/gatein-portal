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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;
import org.exoplatform.commons.utils.IOUtil;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.portal.config.model.Container;
import org.exoplatform.portal.config.model.ModelUnmarshaller;
import org.exoplatform.portal.config.model.NavigationFragment;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.Page.PageSet;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PageNode;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.config.model.UnmarshalledObject;
import org.exoplatform.portal.mop.description.DescriptionService;
import org.exoplatform.portal.mop.importer.ImportMode;
import org.exoplatform.portal.mop.importer.Imported;
import org.exoplatform.portal.mop.importer.Imported.Status;
import org.exoplatform.portal.mop.importer.NavigationImporter;
import org.exoplatform.portal.mop.importer.PageImporter;
import org.exoplatform.portal.mop.importer.PortalConfigImporter;
import org.exoplatform.portal.mop.navigation.NavigationService;
import org.exoplatform.portal.mop.page.PageService;
import org.exoplatform.portal.pom.config.POMSession;
import org.exoplatform.portal.pom.config.POMSessionManager;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.mop.api.workspace.Workspace;
import org.jibx.runtime.JiBXException;

/**
 * Created by The eXo Platform SARL Author : Tuan Nguyen tuan08@users.sourceforge.net May 22, 2006
 */

public class NewPortalConfigListener extends BaseComponentPlugin {

    /** . */
    private final UserPortalConfigService owner_;

    /** . */
    private ConfigurationManager cmanager_;

    /** . */
    private DataStorage dataStorage_;

    /** . */
    private PageService pageService_;

    /** . */
    private volatile List<NewPortalConfig> configs;

    /** . */
    private List<SiteConfigTemplates> templateConfigs;

    /** . */
    private String pageTemplatesLocation_;

    /** . */
    private String defaultPortal;

    /**
     * If true the default portal name has been explicitly set. If false the name has not been set and we are using the default.
     */
    private boolean defaultPortalSpecified = false;

    /** . */
    private String defaultPortalTemplate;

    /** . */
    private boolean isUseTryCatch;

    /**
     * If true the portal clear portal metadata from data storage and replace it with new data created from .xml files.
     */
    private boolean overrideExistingData;

    /** . */
    private Logger log = LoggerFactory.getLogger(getClass());

    /** . */
    private final POMSessionManager pomMgr;

    /** . */
    private NavigationService navigationService_;

    /** . */
    private DescriptionService descriptionService_;

    final Set<String> createdOwners = new HashSet<String>();

    private boolean isFirstStartup = false;

    public NewPortalConfigListener(UserPortalConfigService owner, POMSessionManager pomMgr, DataStorage dataStorage,
            PageService pageService, ConfigurationManager cmanager, InitParams params, NavigationService navigationService,
            DescriptionService descriptionService) throws Exception {
        owner_ = owner;
        cmanager_ = cmanager;
        dataStorage_ = dataStorage;
        pageService_ = pageService;
        navigationService_ = navigationService;
        descriptionService_ = descriptionService;

        ValueParam valueParam = params.getValueParam("page.templates.location");
        if (valueParam != null)
            pageTemplatesLocation_ = valueParam.getValue();

        valueParam = params.getValueParam("default.portal");
        if (valueParam != null) {
            defaultPortal = valueParam.getValue();
        }

        if (defaultPortal == null || defaultPortal.trim().length() == 0) {
            defaultPortal = "classic";
        } else {
            defaultPortalSpecified = true;
        }

        // I guess we'll use the term 'portal' to mean site as to be consistent with defaultPortal
        valueParam = params.getValueParam("default.portal.template");
        if (valueParam != null) {
            defaultPortalTemplate = valueParam.getValue().trim();
        }

        configs = params.getObjectParamValues(NewPortalConfig.class);

        templateConfigs = params.getObjectParamValues(SiteConfigTemplates.class);

        // get parameter
        valueParam = params.getValueParam("initializing.failure.ignore");
        // determine in the run function, is use try catch or not
        if (valueParam != null) {
            isUseTryCatch = (valueParam.getValue().toLowerCase().equals("true"));
        } else {
            isUseTryCatch = true;
        }

        valueParam = params.getValueParam("override");
        if (valueParam != null) {
            overrideExistingData = "true".equals(valueParam.getValue());
        } else {
            overrideExistingData = false;
        }
        for (NewPortalConfig ele : configs) {
          ele.setOverrideMode(overrideExistingData);
        }
        this.pomMgr = pomMgr;
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
                if (dataStorage_.getPortalConfig(defaultPortal) != null) {
                    perform = false;
                    imported.setStatus(Status.DONE.status());
                } else {
                    isFirstStartup = true;
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
            return perform;
        } finally {
            RequestLifeCycle.end();
        }
    }

    public void run() throws Exception {
        boolean prepareImport = performImport();
        if (isUseTryCatch) {
            RequestLifeCycle.begin(PortalContainer.getInstance());
            try {
                for (NewPortalConfig ele : configs) {
                    try {
                        if(ele.getOverrideMode() || prepareImport) {
                            initPortalConfigDB(ele);
                        }
                    } catch (Exception e) {
                        log.error("NewPortalConfig error: " + e.getMessage(), e);
                    }
                }
            } finally {
                RequestLifeCycle.end();
            }
            for (NewPortalConfig ele : configs) {
                try {
                    if(ele.getOverrideMode() || prepareImport) {
                        initPageDB(ele);
                    }
                } catch (Exception e) {
                    log.error("NewPortalConfig error: " + e.getMessage(), e);
                }
            }
            RequestLifeCycle.begin(PortalContainer.getInstance());
            try {
                for (NewPortalConfig ele : configs) {
                    try {
                        if(ele.getOverrideMode() || prepareImport) {
                            initPageNavigationDB(ele);
                        }
                    } catch (Exception e) {
                        log.error("NewPortalConfig error: " + e.getMessage(), e);
                    }
                }
            } finally {
                RequestLifeCycle.end();
            }
        } else {
            RequestLifeCycle.begin(PortalContainer.getInstance());
            try {
                for (NewPortalConfig ele : configs) {
                    if(ele.getOverrideMode() || prepareImport) {
                        initPortalConfigDB(ele);
                    }
                }
            } finally {
                RequestLifeCycle.end();
            }
            for (NewPortalConfig ele : configs) {
                if(ele.getOverrideMode() || prepareImport) {
                    initPageDB(ele);
                }
            }
            RequestLifeCycle.begin(PortalContainer.getInstance());
            try {
                for (NewPortalConfig ele : configs) {
                    if(ele.getOverrideMode() || prepareImport) {
                        initPageNavigationDB(ele);
                    }
                }
            } finally {
                RequestLifeCycle.end();
            }
        }

        //
        touchImport();
    }

    String getDefaultPortal() {
        return defaultPortal;
    }

    String getDefaultPortalTemplate() {
        return defaultPortalTemplate;
    }

    /**
     * Returns a specified new portal config. The returned object can be safely modified by as it is a copy of the original
     * object.
     *
     * @param ownerType the owner type
     * @param template
     * @return the specified new portal config
     */
    NewPortalConfig getPortalConfig(String ownerType, String template) {
        for (NewPortalConfig portalConfig : configs) {
            if (portalConfig.getOwnerType().equals(ownerType)) {
                // We are defensive, we make a deep copy
                return new NewPortalConfig(portalConfig);
            }
        }
        return null;
    }

    /**
     * This is used to merge an other NewPortalConfigListener to this one
     *
     * @param other
     */
    public void mergePlugin(NewPortalConfigListener other) {
        // if other didn't actually set anything for the default portal name
        // then we should continue to use the current value. This way if an extension
        // doesn't set it, it wont override the parent's set value.
        if (other.defaultPortalSpecified) {
            this.defaultPortal = other.defaultPortal;
        }

        if (other.defaultPortalTemplate != null && other.defaultPortalTemplate.length() > 0) {
            this.defaultPortalTemplate = other.defaultPortalTemplate;
        }

        if (configs == null) {
            this.configs = other.configs;
        } else if (other.configs != null && !other.configs.isEmpty()) {
            List<NewPortalConfig> result = new ArrayList<NewPortalConfig>(configs);
            result.addAll(other.configs);
            this.configs = Collections.unmodifiableList(result);
        }

        if (templateConfigs == null) {
            this.templateConfigs = other.templateConfigs;
        } else if (other.templateConfigs != null && !other.templateConfigs.isEmpty()) {
            List<SiteConfigTemplates> result = new ArrayList<SiteConfigTemplates>(templateConfigs);
            result.addAll(other.templateConfigs);
            this.templateConfigs = Collections.unmodifiableList(result);
        }
    }

    /**
     * This is used to delete an already loaded NewPortalConfigListener(s)
     *
     * @param other
     */
    public void deleteListenerElements(NewPortalConfigListener other) {
        if (configs == null) {
            log.warn("No Portal configurations was loaded, nothing to delete !");
        } else if (other.configs != null && !other.configs.isEmpty()) {
            List<NewPortalConfig> result = new ArrayList<NewPortalConfig>(configs);
            for (NewPortalConfig newPortalConfigToDelete : other.configs) {
                int i = 0;
                while (i < result.size()) {
                    NewPortalConfig newPortalConfig = result.get(i);
                    if (newPortalConfigToDelete.getOwnerType().equals(newPortalConfig.getOwnerType())) {
                        for (String owner : newPortalConfigToDelete.getPredefinedOwner()) {
                            newPortalConfig.getPredefinedOwner().remove(owner);
                        }
                    }
                    // if the configuration has no owner definitions, then delete it
                    if (newPortalConfig.getPredefinedOwner().size() == 0) {
                        result.remove(newPortalConfig);
                    } else {
                        i++;
                    }
                }
            }
            this.configs = Collections.unmodifiableList(result);
        }

        if (templateConfigs == null) {
            log.warn("No Portal templates configurations was loaded, nothing to delete !");
        } else if (other.templateConfigs != null && !other.templateConfigs.isEmpty()) {
            List<SiteConfigTemplates> result = new ArrayList<SiteConfigTemplates>(templateConfigs);
            deleteSiteConfigTemplates(other, result, PortalConfig.PORTAL_TYPE);
            deleteSiteConfigTemplates(other, result, PortalConfig.GROUP_TYPE);
            deleteSiteConfigTemplates(other, result, PortalConfig.USER_TYPE);
            this.templateConfigs = Collections.unmodifiableList(result);
        }
    }

    private void deleteSiteConfigTemplates(NewPortalConfigListener other, List<SiteConfigTemplates> result, String templateType) {
        for (SiteConfigTemplates siteConfigTemplatesToDelete : other.templateConfigs) {
            Set<String> portalTemplatesToDelete = siteConfigTemplatesToDelete.getTemplates(templateType);
            if (portalTemplatesToDelete != null && portalTemplatesToDelete.size() > 0) {
                int i = 0;
                while (i < result.size()) {
                    SiteConfigTemplates siteConfigTemplates = result.get(i);
                    Set<String> portalTemplates = siteConfigTemplates.getTemplates(templateType);
                    if (portalTemplatesToDelete != null && portalTemplatesToDelete.size() > 0) {
                        portalTemplates.removeAll(portalTemplatesToDelete);
                    }
                    if ((siteConfigTemplates.getTemplates(PortalConfig.PORTAL_TYPE) == null || siteConfigTemplates
                            .getTemplates(PortalConfig.PORTAL_TYPE).size() == 0)
                            && (siteConfigTemplates.getTemplates(PortalConfig.GROUP_TYPE) == null || siteConfigTemplates
                                    .getTemplates(PortalConfig.GROUP_TYPE).size() == 0)
                            && (siteConfigTemplates.getTemplates(PortalConfig.USER_TYPE) == null || siteConfigTemplates
                                    .getTemplates(PortalConfig.USER_TYPE).size() == 0)) {
                        result.remove(siteConfigTemplates);
                    } else {
                        i++;
                    }
                }
            }
        }
    }

    public void initPortalConfigDB(NewPortalConfig config) throws Exception {
        for (String owner : config.getPredefinedOwner()) {
            if (createPortalConfig(config, owner)) {
                this.createdOwners.add(owner);
            }
        }
    }

    public void initPageDB(NewPortalConfig config) throws Exception {
        for (String owner : config.getPredefinedOwner()) {
            if (this.createdOwners.contains(owner)) {
                createPage(config, owner);
            }
        }
    }

    public void initPageNavigationDB(NewPortalConfig config) throws Exception {
        for (String owner : config.getPredefinedOwner()) {
            createPageNavigation(config, owner);
        }
    }

    public boolean createPortalConfig(NewPortalConfig config, String owner) throws Exception {
        String type = config.getOwnerType();
        UnmarshalledObject<PortalConfig> obj = getConfig(config, owner, type, PortalConfig.class);

        PortalConfig pConfig;
        if (obj == null) {
            String fixedName = fixOwnerName(type, owner);
            if (dataStorage_.getPortalConfig(type, fixedName) != null) {
                return true;
            } else {
                pConfig = new PortalConfig(type, fixedName);
            }
        } else {
            pConfig = obj.getObject();
        }

        ImportMode importMode = getRightMode(config.getImportMode());

        PortalConfigImporter portalImporter = new PortalConfigImporter(importMode, pConfig, dataStorage_);
        try {
            portalImporter.perform();
            return true;
        } catch (Exception ex) {
            log.error("An Exception occured when creating the Portal Configuration. Exception message: " + ex.getMessage(), ex);
            return false;
        }
    }

    public void createPage(NewPortalConfig config, String owner) throws Exception {
        UnmarshalledObject<PageSet> pageSet = getConfig(config, owner, "pages", PageSet.class);
        if (pageSet == null) {
            return;
        }
        ArrayList<Page> list = pageSet.getObject().getPages();
        for (Page page : list) {
            RequestLifeCycle.begin(PortalContainer.getInstance());
            try { //
                ImportMode importMode = getRightMode(config.getImportMode());

                PageImporter importer = new PageImporter(importMode, page, dataStorage_, pageService_);
                importer.perform();
            } finally {
                RequestLifeCycle.end();
            }
        }
    }

    public void createPageNavigation(NewPortalConfig config, String owner) throws Exception {
        UnmarshalledObject<PageNavigation> obj = getConfig(config, owner, "navigation", PageNavigation.class);
        if (obj == null) {
            return;
        }

        //
        PageNavigation navigation = obj.getObject();

        //
        ImportMode importMode = getRightMode(config.getImportMode());

        //
        Locale locale;
        PortalConfig portalConfig = dataStorage_.getPortalConfig(config.getOwnerType(), owner);
        if (portalConfig != null && portalConfig.getLocale() != null) {
            locale = new Locale(portalConfig.getLocale());
        } else {
            locale = Locale.ENGLISH;
        }

        //
        NavigationImporter merge = new NavigationImporter(locale, importMode, navigation, navigationService_,
                descriptionService_);

        //
        merge.perform();
    }

    private final Pattern OWNER_PATTERN = Pattern.compile("@owner@");

    /**
     * Best effort to load and unmarshall a configuration.
     *
     * @param config the config object
     * @param owner the owner
     * @param fileName the file name
     * @param type the type to unmarshall to
     * @return the xml of the config or null
     * @throws Exception any exception
     * @param <T> the generic type to unmarshall to
     */
    private <T> UnmarshalledObject<T> getConfig(NewPortalConfig config, String owner, String fileName, Class<T> type)
            throws Exception {
        log.debug("About to load config=" + config + " owner=" + owner + " fileName=" + fileName);

        //
        String ownerType = config.getOwnerType();

        // Get XML
        String path = "/" + ownerType + "/" + owner + "/" + fileName + ".xml";
        String xml = getDefaultConfig(config.getTemplateLocation(), path);

        //
        if (xml == null) {
            String templateName = config.getTemplateName() != null ? config.getTemplateName() : fileName;
            path = "/" + ownerType + "/template/" + templateName + "/" + fileName + ".xml";
            xml = getDefaultConfig(config.getTemplateLocation(), path);
            if (xml != null) {
                xml = OWNER_PATTERN.matcher(xml).replaceAll(StringEscapeUtils.escapeXml(owner));
            }
        }

        //
        if (xml != null) {
            boolean ok = false;
            try {
                final UnmarshalledObject<T> o = fromXML(config.getOwnerType(), owner, xml, type);
                ok = true;
                return o;
            } catch (JiBXException e) {
                log.error(e.getMessage() + " file: " + path, e);
                throw e;
            } finally {
                if (!ok) {
                    log.error("Could not load file: " + path);
                }
            }
        }

        //
        return null;
    }

    private String getDefaultConfig(String location, String path) {
        String s = location + path;
        String content = null;
        try {
            log.debug("Attempt to load file " + s);
            content = IOUtil.getStreamContentAsString(cmanager_.getInputStream(s));
            log.debug("Loaded file from path " + s + " with content " + content);
        } catch (Exception ignore) {
            log.debug("Could not get file " + s + " will return null instead");
        }
        return content;
    }

    public Page createPageFromTemplate(String ownerType, String owner, String temp) throws Exception {
        String path = pageTemplatesLocation_ + "/" + temp + "/page.xml";
        InputStream is = cmanager_.getInputStream(path);
        String xml = IOUtil.getStreamContentAsString(is);
        return fromXML(ownerType, owner, xml, Page.class).getObject();
    }

    public String getTemplateConfig(String type, String name) {
        for (SiteConfigTemplates tempConfig : templateConfigs) {
            Set<String> templates = tempConfig.getTemplates(type);
            if (templates != null && templates.contains(name))
                return tempConfig.getLocation();
        }
        return null;
    }

    /**
     * Get all template configurations
     *
     * @param siteType (portal, group, user)
     * @return set of template name
     */
    public Set<String> getTemplateConfigs(String siteType) {
        Set<String> result = new HashSet<String>();
        for (SiteConfigTemplates tempConfig : templateConfigs) {
            Set<String> templates = tempConfig.getTemplates(siteType);
            if (templates != null && templates.size() > 0) {
                result.addAll(templates);
            }
        }
        return result;
    }

    /**
     * Get detail configuration from a template file
     *
     * @param siteType (portal, group, user)
     * @param templateName name of template
     * @return PortalConfig object
     */
    public PortalConfig getPortalConfigFromTemplate(String siteType, String templateName) {
        String templatePath = getTemplateConfig(siteType, templateName);
        NewPortalConfig config = new NewPortalConfig(templatePath);
        config.setTemplateName(templateName);
        config.setOwnerType(siteType);
        UnmarshalledObject<PortalConfig> result = null;
        try {
            result = getConfig(config, templateName, siteType, PortalConfig.class);
            if (result != null) {
                return result.getObject();
            }
        } catch (Exception e) {
            log.warn("Cannot find configuration of template: " + templateName);
        }
        return null;
    }

    // Deserializing code

    private <T> UnmarshalledObject<T> fromXML(String ownerType, String owner, String xml, Class<T> clazz) throws Exception {
        UnmarshalledObject<T> obj = ModelUnmarshaller.unmarshall(clazz, xml.getBytes("UTF-8"));
        T o = obj.getObject();
        if (o instanceof PageNavigation) {
            PageNavigation nav = (PageNavigation) o;
            nav.setOwnerType(ownerType);
            nav.setOwnerId(owner);
            if (nav.getPriority() < 1) {
                nav.setPriority(PageNavigation.UNDEFINED_PRIORITY);
            }
            fixOwnerName((PageNavigation) o);
        } else if (o instanceof PortalConfig) {
            PortalConfig portalConfig = (PortalConfig) o;
            portalConfig.setType(ownerType);
            portalConfig.setName(owner);
            fixOwnerName(portalConfig);
        } else if (o instanceof PageSet) {
            for (Page page : ((PageSet) o).getPages()) {
                page.setOwnerType(ownerType);
                page.setOwnerId(owner);
                fixOwnerName(page);
                // The page will be created in the calling method
                // pdcService_.create(page);
            }
        }
        return obj;
    }

    private static String fixOwnerName(String type, String owner) {
        if (type.equals(PortalConfig.GROUP_TYPE) && !owner.startsWith("/")) {
            return "/" + owner;
        } else {
            return owner;
        }
    }

    private static void fixOwnerName(PortalConfig config) {
        config.setName(fixOwnerName(config.getType(), config.getName()));
        fixOwnerName(config.getPortalLayout());
    }

    private static void fixOwnerName(Container container) {
        for (Object o : container.getChildren()) {
            if (o instanceof Container) {
                fixOwnerName((Container) o);
            }
        }
    }

    private static void fixOwnerName(PageNavigation pageNav) {
        pageNav.setOwnerId(fixOwnerName(pageNav.getOwnerType(), pageNav.getOwnerId()));
        ArrayList<NavigationFragment> fragments = pageNav.getFragments();
        if (fragments != null) {
            for (NavigationFragment fragment : fragments) {
                fixOwnerName(fragment);
            }
        }
    }

    private static void fixOwnerName(NavigationFragment fragment) {
        ArrayList<PageNode> nodes = fragment.getNodes();
        if (nodes != null) {
            for (PageNode pageNode : nodes) {
                fixOwnerName(pageNode);
            }
        }
    }

    private static void fixOwnerName(PageNode pageNode) {
        if (pageNode.getPageReference() != null) {
            String pageRef = pageNode.getPageReference();
            int pos1 = pageRef.indexOf("::");
            int pos2 = pageRef.indexOf("::", pos1 + 2);
            String type = pageRef.substring(0, pos1);
            String owner = pageRef.substring(pos1 + 2, pos2);
            String name = pageRef.substring(pos2 + 2);
            owner = fixOwnerName(type, owner);
            pageRef = type + "::" + owner + "::" + name;
            pageNode.setPageReference(pageRef);
        }
        if (pageNode.getNodes() != null) {
            for (PageNode childPageNode : pageNode.getNodes()) {
                fixOwnerName(childPageNode);
            }
        }
    }

    private static void fixOwnerName(Page page) {
        page.setOwnerId(fixOwnerName(page.getOwnerType(), page.getOwnerId()));
        fixOwnerName((Container) page);
    }

    private ImportMode getRightMode(String mode) {
        ImportMode importMode;
        if (mode != null) {
            importMode = ImportMode.valueOf(mode.trim().toUpperCase());
        } else {
            importMode = owner_.getDefaultImportMode();
        }

        if (isFirstStartup && (importMode == ImportMode.CONSERVE || importMode == ImportMode.INSERT)) {
            return ImportMode.MERGE;
        }

        return importMode;
    }
}
