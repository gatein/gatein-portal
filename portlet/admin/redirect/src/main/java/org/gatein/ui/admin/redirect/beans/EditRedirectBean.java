/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
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

package org.gatein.ui.admin.redirect.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.model.DevicePropertyCondition;
import org.exoplatform.portal.config.model.NodeMap;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.config.model.PortalRedirect;
import org.exoplatform.portal.config.model.RedirectCondition;
import org.exoplatform.portal.config.model.RedirectMappings;
import org.exoplatform.portal.config.model.UserAgentConditions;
import org.gatein.api.PortalRequest;
import org.gatein.api.navigation.Node;
import org.gatein.api.navigation.Nodes;
import org.gatein.api.site.SiteId;

@ManagedBean(name = "rdrEdit")
@ViewScoped
public class EditRedirectBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean isEdit = false;

    public boolean getIsEdit() {
        return isEdit;
    }

    public void setIsEdit(boolean isEdit) {
        this.isEdit = isEdit;
    }

    DataStorage ds = null;
    PortalConfig cfg = null;

    protected PortalRedirect pr;

    // So we keep a reference to redirects with updated name
    protected String originalName;

    protected String redirectName;
    protected boolean enabled;
    protected String redirectSite;

    protected RedirectMappings mappings;

    private boolean isNewRedirect;

    /**
     * Sets the name of the Redirect to be edited. It's _NOT_ used to change the current redirect name!
     */
    public void configRedirect() {
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        redirectName = params.get("rname");
    }

    public void addRedirect(String site) {
        this.siteName = site;
        this.pr = new PortalRedirect();
        this.pr.setConditions(new ArrayList<RedirectCondition>());
        RedirectMappings rm = new RedirectMappings();
        rm.setMappings(new ArrayList<NodeMap>());
        this.pr.setMappings(rm);
        this.mappings = pr.getMappings();
        isNewRedirect = true;
    }

    /**
     * Returns the name of the redirect being edited.
     *
     * @return
     */
    public String getName() {
        return this.pr != null ? this.pr.getName() : redirectName;
    }

    /**
     * Sets the Redirect Name.
     *
     * @param name
     */
    public void setName(String name) {
        if (this.pr != null) {
            this.pr.setName(name);
        }
        this.redirectName = name;
    }

    /**
     * Toggles redirect enabled/disabled state. Persisted immediately, as it's used for the redirects listing.
     *
     * @param site the site the affected redirect belongs to
     * @param name the name of the redirect to be enabled/disabled
     */
    public void toggleEnabled(String site, String name) {
        try {
            // FIXME: Use webui Util.getUIPortal();
            if (ds == null) {
                ds = (DataStorage) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(DataStorage.class);
            }

            cfg = ds.getPortalConfig(site);
            for (PortalRedirect pr : cfg.getPortalRedirects()) {
                if (pr.getName().equals(name)) {
                    pr.setEnabled(!pr.isEnabled());
                    ds.save(cfg);
                    return;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void toggleEnabled() {
        toggleEnabled(siteName, redirectName);
    }

    public String deleteRedirect(String site, String name) {
        try {
            // FIXME: Use webui Util.getUIPortal();
            if (ds == null) {
                ds = (DataStorage) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(DataStorage.class);
            }

            cfg = ds.getPortalConfig(site);
            ArrayList<PortalRedirect> redirects = cfg.getPortalRedirects();

            int index = -1;
            for (int i = 0; i < redirects.size(); i++) {
                if (redirects.get(i).getName().equals(name)) {
                    index = i;
                    break;
                }
            }

            if (index != -1) {
                redirects.remove(index);
                cfg.setPortalRedirects(redirects);
                ds.save(cfg);
            } else {
                // redirect was not found or deleted...
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    /**
     * After editing a redirect, save/persist it.
     *
     * @return
     */
    public String saveRedirect() {
        try {
            // FIXME: Use webui Util.getUIPortal();
            if (ds == null) {
                ds = (DataStorage) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(DataStorage.class);
            }

            cfg = ds.getPortalConfig(siteName);
            ArrayList<PortalRedirect> redirects = cfg.getPortalRedirects();
            // FIXME: getPortalRedirects() should return empty list instead of null
            if (redirects == null) {
                redirects = new ArrayList<PortalRedirect>();
            }
            boolean save = false;
            if (isNewRedirect) {
                redirects.add(pr);
                save = true;
            } else {
                int index = -1;
                for (int i = 0; i < redirects.size(); i++) {
                    if (redirects.get(i).getName().equals(originalName)) {
                        index = i;
                        break;
                    }
                }
                if (index != -1) {
                    redirects.set(index, this.pr);
                    save = true;
                }
            }

            if (save) {
                cfg.setPortalRedirects(redirects);
                ds.save(cfg);
                isNewRedirect = false;
            } else {
                // redirect was not found or saved...
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    /**
     * After editing a redirect, cancel it's changes. Method restores the redirect state, name, conditions, mappings, etc to
     * it's backup state. TODO: Try to clone the entire redirect for backup.
     */
    public void rollbackRedirect() {
        isEdit = false;
    }

    // ----- REDIRECT ENABLED, NAME & SITE -----

    /**
     * Sets the enabled/disabled state of a redirect, in edit mode. As it is in edit mode, it is not persisted as in {
     * {@link #toggleEnabled(String, String)}, since it can be canceled.
     *
     * @param enabled the value to set the enabled property
     */
    public void setEnabled(boolean enabled) {
        pr.setEnabled(enabled);
    }

    /**
     * Getter for the redirect enabled property, indicating if the redirect is active or not.
     *
     * @return a boolean indicating the redirect enabled property
     */
    public boolean getEnabled() {
        return pr != null ? pr.isEnabled() : false;
    }

    public String getRedirectSite() {
        return this.pr != null ? this.pr.getRedirectSite() : null;
    }

    public void setRedirectSite(String redirectSite) {
        this.pr.setRedirectSite(redirectSite);
    }

    // ----- CONDITIONS -----

    public ArrayList<RedirectCondition> getConditions() {
        return pr != null ? pr.getConditions() : new ArrayList<RedirectCondition>();
    }

    public void setConditions(ArrayList<RedirectCondition> conditions) {
        pr.setConditions(conditions);
    }

    // current condition being edited
    private int currentConditionIndex;
    private RedirectCondition editedCondition;

    private ArrayList<String> backupContains;
    private ArrayList<String> backupDoesNotContain;
    private ArrayList<DevicePropertyCondition> backupDeviceProperties;

    private boolean conditionsChanged = false;
    private boolean isNewCondition = false;

    private String siteName;

    // holds selected node mapping site option
    private String nodesSiteName;
    // holds origin node names
    private List<String> originNodeNames = new ArrayList<String>();
    // holds redirect node names
    private List<String> redirectNodeNames = new ArrayList<String>();
    // copy of either origin or redirect node names, depending on selection
    private List<String> currentNodeNames = new ArrayList<String>();

    public int getCurrentConditionIndex() {
        return currentConditionIndex;
    }

    public void setCurrentConditionIndex(int currentConditionIndex) {
        this.currentConditionIndex = currentConditionIndex;
    }

    public RedirectCondition getEditedCondition() {
        return editedCondition;
    }

    public void setEditedCondition(RedirectCondition editedCondition) {
        this.editedCondition = editedCondition;
        this.backupContains = new ArrayList<String>(editedCondition.getUserAgentConditions().getContains());
        this.backupDoesNotContain = new ArrayList<String>(editedCondition.getUserAgentConditions().getDoesNotContain());
        this.backupDeviceProperties = editedCondition.getDeviceProperties() == null ? new ArrayList<DevicePropertyCondition>()
                : new ArrayList<DevicePropertyCondition>(editedCondition.getDeviceProperties());
        this.conditionsChanged = false;
    }

    public ArrayList<String> getContains(String condition) {
        return editedCondition.getUserAgentConditions().getContains();
    }

    public void addCondition() {
        this.editedCondition = createNewCondition();
        isNewCondition = true;
    }

    /**
     * Removes a Redirect Condition entry from the edited redirect.
     *
     * @param index the index of the entry to remove
     */
    public void removeCondition(Integer index) {
        RedirectCondition rc = pr.getConditions().remove((int) index);
    }

    /**
     * Creates a new redirect condition, sanitizing the initial values, as they are set to null instead of empty ArrayLists,
     * etc.
     *
     * @return
     */
    private RedirectCondition createNewCondition() {
        RedirectCondition newRC = new RedirectCondition();
        newRC.setName("");
        newRC.setDeviceProperties(new ArrayList<DevicePropertyCondition>());
        UserAgentConditions newUAC = new UserAgentConditions();
        newUAC.setContains(new ArrayList<String>());
        newUAC.setDoesNotContain(new ArrayList<String>());
        newRC.setUserAgentConditions(newUAC);

        return newRC;
    }

    /**
     * Adds a new "CONTAINS" entry to the edited condition.
     */
    public void addContains() {
        editedCondition.getUserAgentConditions().getContains().add("");
        conditionsChanged = true;
    }

    /**
     * Removes a "CONTAINS" entry from the edited condition.
     *
     * @param index the index of the entry to remove
     */
    public void removeContains(Integer index) {
        String rc = editedCondition.getUserAgentConditions().getContains().remove((int) index);
        conditionsChanged = true;
    }

    /**
     * Adds a new "DOES NOT CONTAIN" entry to the edited condition.
     */
    public void addDoesNotContain() {
        editedCondition.getUserAgentConditions().getDoesNotContain().add("");
        conditionsChanged = true;
    }

    /**
     * Removes a "DOES NOT CONTAIN" entry from the edited condition.
     *
     * @param index the index of the entry to remove
     */
    public void removeDoesNotContain(Integer index) {
        String rc = editedCondition.getUserAgentConditions().getDoesNotContain().remove((int) index);
        conditionsChanged = true;
    }

    public boolean getConditionsChanged() {
        return conditionsChanged;
    }

    /**
     * After editing a condition, save it. Method does nothing, as all changes should be already present in the object.
     *
     * @return
     */
    public String saveCondition() {
        if (isNewCondition) {
            this.pr.getConditions().add(editedCondition);
            isNewCondition = false;
            conditionsChanged = false;
        }
        return null;
    }

    /**
     * After editing a condition, cancel it's changes. Method restores the conditions, properties, etc to it's backup state.
     * TODO: Try to clone the entire condition for backup.
     */
    public void rollbackCondition() {
        if (!isNewCondition) {
            this.editedCondition.getUserAgentConditions().setContains(backupContains);
            this.editedCondition.getUserAgentConditions().setDoesNotContain(backupDoesNotContain);
            this.editedCondition.setDeviceProperties(backupDeviceProperties);
        }
    }

    // ----- PROPERTIES -----

    /**
     * Adds a new Device Property to the edited condition.
     */
    public void addProperty() {
        if (editedCondition.getDeviceProperties() == null) {
            editedCondition.setDeviceProperties(new ArrayList<DevicePropertyCondition>());
        }

        editedCondition.getDeviceProperties().add(new DevicePropertyCondition());
    }

    /**
     * Removes a Device Property entry from the edited condition.
     *
     * @param index the index of the entry to remove
     */
    public void removeProperty(Integer index) {
        DevicePropertyCondition rc = editedCondition.getDeviceProperties().remove((int) index);
        conditionsChanged = true;
    }

    /**
     * Gets the proper operator to be shown at property editor, mapping it to the &lt;select&gt; element.
     *
     * @param index the index of the entry to get the operator from
     * @return a string value representing the operator (mt for matches, bt for between, gt for greater-than, lt for less-than
     *         and eq for equals)
     */
    public String getPropertyOperator(int index) {
        DevicePropertyCondition dp = editedCondition.getDeviceProperties().get(index);
        if (dp.getMatches() != null && !dp.getMatches().trim().isEmpty()) {
            return "mt";
        } else if (dp.getGreaterThan() != null && dp.getLessThan() != null && dp.getGreaterThan() != 0.0
                && dp.getLessThan() != 0.0) {
            return "bt";
        } else if (dp.getGreaterThan() != null || dp.getGreaterThan() != 0.0) {
            return "gt";
        } else if (dp.getLessThan() != null || dp.getLessThan() != 0.0) {
            return "lt";
        } else {
            return "eq";
        }
    }

    // ----- MAPPINGS -----

    public void addNodeMapping() {
        this.pr.getMappings().getMappings().add(0, new NodeMap());
    }

    public void removeNodeMapping(int index) {
        ArrayList<NodeMap> mappings = this.pr.getMappings().getMappings();
        mappings.remove(index);
        this.pr.getMappings().setMappings(mappings);
    }

    public boolean getUseNodeNameMatching() {
        return (this.pr != null && this.pr.getMappings() != null) ? this.pr.getMappings().isUseNodeNameMatching() : false;
    }

    public void setUseNodeNameMatching(boolean useNodeNameMatching) {
        this.pr.getMappings().setUseNodeNameMatching(useNodeNameMatching);
    }

    public RedirectMappings getMappings() {
        return mappings;
    }

    public void setMappings(RedirectMappings mappings) {
        this.mappings = mappings;
    }

    /**
     * Returns the list of Portal nodes for the origin.
     *
     * @return a List of Strings representing node paths
     */
    public List<String> getOriginNodeNames() {
        return this.originNodeNames;
    }

    /**
     * Returns the list of Portal nodes for the redirect.
     *
     * @return a List of Strings representing node paths
     */
    public List<String> getRedirectNodeNames() {
        return this.redirectNodeNames;
    }

    /**
     * Returns the name of the Portal site for the selected option (origin or redirect) by {@link #setCurrentNodeNames(boolean)}
     * .
     *
     * @return
     */
    public String getCurrentNodesSiteName() {
        return nodesSiteName;
    }

    /**
     * Returns the list of Portal nodes for the selected option (origin or redirect) by {@link #setCurrentNodeNames(boolean)}.
     *
     * @return
     */
    public List<String> getCurrentNodeNames() {
        return currentNodeNames;
    }

    public void setCurrentNodeNames(boolean isOrigin) {
        if (isOrigin) {
            this.currentNodeNames = originNodeNames;
            this.nodesSiteName = siteName;
        } else {
            this.currentNodeNames = redirectNodeNames;
            this.nodesSiteName = redirectSite;
        }
    }

    // --- Utilities ----------------------------------------------------------

    public void load(String site, String redirect) {
        isNewRedirect = false;

        this.siteName = site;

        // FIXME: Use webui Util.getUIPortal();
        if (ds == null) {
            ds = (DataStorage) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(DataStorage.class);
        }

        try {
            cfg = ds.getPortalConfig(site);
            for (PortalRedirect pr : cfg.getPortalRedirects()) {
                if (pr.getName().equals(redirect)) {
                    this.pr = pr;
                    this.redirectName = pr.getName();
                    this.originalName = pr.getName();
                    this.enabled = pr.isEnabled();
                    this.redirectSite = pr.getRedirectSite();
                    this.mappings = pr.getMappings();
                    this.originNodeNames = loadOriginNodes(site);
                    this.redirectNodeNames = loadRedirectNodes();
                    isEdit = true;
                    return;
                }
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    // TODO: move this to a different bean
    public List<String> loadOriginNodes(String siteName) {
        try {
            ArrayList<String> nodes = new ArrayList<String>();
            if (siteName != null) {
                Node n = PortalRequest.getInstance().getPortal().getNavigation(new SiteId(siteName))
                        .getRootNode(Nodes.visitAll());
                for (Node node : Nodes.asList(n)) {
                    nodes.add(node.getNodePath().toString());
                }
            }

            return nodes;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<String> loadRedirectNodes() {
        try {
            ArrayList<String> nodes = new ArrayList<String>();
            if (redirectSite != null) {
                Node n = PortalRequest.getInstance().getPortal().getNavigation(new SiteId(redirectSite))
                        .getRootNode(Nodes.visitAll());
                for (Node node : Nodes.asList(n)) {
                    nodes.add(node.getNodePath().toString());
                }
            }

            this.redirectNodeNames = nodes;
            return nodes;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
