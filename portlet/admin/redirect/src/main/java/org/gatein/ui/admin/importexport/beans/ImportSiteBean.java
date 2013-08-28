/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
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

package org.gatein.ui.admin.importexport.beans;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import org.exoplatform.container.PortalContainer;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.management.api.ContentType;
import org.gatein.management.api.PathAddress;
import org.gatein.management.api.controller.ManagedRequest;
import org.gatein.management.api.controller.ManagedResponse;
import org.gatein.management.api.controller.ManagementController;
import org.gatein.management.api.operation.OperationNames;
import org.richfaces.event.FileUploadEvent;
import org.richfaces.model.UploadedFile;

@ManagedBean(name = "importer")
@ViewScoped
public class ImportSiteBean implements Serializable {

    private UIComponent component;

    public UIComponent getComponent() {
        return component;
    }

    public void setComponent(UIComponent component) {
        this.component = component;
    }

    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggerFactory.getLogger(ImportSiteBean.class);

    private static Map<String,String> importModes;

    static {
        importModes = new LinkedHashMap<String,String>();
        importModes.put("conserve", "redirect.admin.import.item.conserve");
        importModes.put("insert", "redirect.admin.import.item.insert");
        importModes.put("merge", "redirect.admin.import.item.merge");
        importModes.put("overwrite", "redirect.admin.import.item.overwrite");
    }

    public Map<String,String> getImportModes() {
        return importModes;
    }

    private String importMode = "merge";

    public String getImportMode() {
        return importMode;
    }

    public void setImportMode(String importMode) {
        this.importMode = importMode;
    }

    public void importSite(FileUploadEvent event) throws Exception {
        UploadedFile item = event.getUploadedFile();

        ManagementController controller = (ManagementController) PortalContainer.getComponent(ManagementController.class);
        Map<String, List<String>> attributes = new HashMap<String, List<String>>(1);
        attributes.put("importMode", Collections.singletonList(importMode));
        ManagedRequest request = ManagedRequest.Factory.create(OperationNames.IMPORT_RESOURCE,
                PathAddress.pathAddress("mop"), attributes, item.getInputStream(), ContentType.ZIP);
        try {
            ManagedResponse response = controller.execute(request);
            if (!response.getOutcome().isSuccess()) {
                addMessage(item.getName());
                log.error(response.getOutcome().getFailureDescription());
            }
        } catch (Exception e) {
            addMessage(item.getName());
            log.error("Error while processing" + item.getName(), e);
        }
    }

    private void addMessage(String filename){
        final FacesContext context = FacesContext.getCurrentInstance();
        final FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, filename, null);
        context.addMessage(component.getClientId(), facesMsg);
    }
}
