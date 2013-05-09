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

import java.io.OutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.portlet.ResourceResponse;

import org.exoplatform.container.PortalContainer;
import org.gatein.management.api.ContentType;
import org.gatein.management.api.PathAddress;
import org.gatein.management.api.controller.ManagedRequest;
import org.gatein.management.api.controller.ManagedResponse;
import org.gatein.management.api.controller.ManagementController;
import org.gatein.management.api.operation.OperationNames;

@ManagedBean(name = "exporter")
@SessionScoped
public class ExportSiteBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");

    public void exportSite(OutputStream out, Object site) throws Exception {
        FacesContext fctx = FacesContext.getCurrentInstance();

        String type = (String) fctx.getExternalContext().getRequestParameterMap().get("type");
        String name = (String) fctx.getExternalContext().getRequestParameterMap().get("name");

        String safeName = name.replaceAll("/", "-");
        if (safeName.startsWith("-")) {
            safeName = safeName.substring(1);
        }

        String filename = new StringBuilder(type).append("_").append(safeName).append("_").append(getTimestamp()).append(".zip").toString();

        ResourceResponse response = (ResourceResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();

        response.reset();
        response.setContentType("application/zip");
        response.setProperty("Content-disposition", "attachment; filename=\"" + filename + "\"");

        ManagementController controller = (ManagementController) PortalContainer.getComponent(ManagementController.class);
        PathAddress address = PathAddress.pathAddress("mop", type + "sites", name);

        ManagedRequest request = ManagedRequest.Factory.create(OperationNames.EXPORT_RESOURCE, address, ContentType.ZIP);
        ManagedResponse expResponse = controller.execute(request);
        if (expResponse.getOutcome().isSuccess()) {
            expResponse.writeResult(out, true);
        } else {
            throw new Exception(expResponse.getOutcome().getFailureDescription());
        }

        fctx.responseComplete();
    }

    private String getTimestamp() {
        return SDF.format(new Date());
    }
}