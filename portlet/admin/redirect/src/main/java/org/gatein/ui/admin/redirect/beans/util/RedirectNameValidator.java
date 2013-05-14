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

package org.gatein.ui.admin.redirect.beans.util;

import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.config.model.PortalRedirect;

@FacesValidator("rdrNameValidator")
public class RedirectNameValidator implements Validator {

    private DataStorage ds;

    public RedirectNameValidator(){
        ds = (DataStorage) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(DataStorage.class);
    }

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        String newName = (String) value;
        // check for empty redirect name
        if (newName.equals("")) {
            ((UIInput) component).setValid(false);
            ((UIInput) component).setValidatorMessage("The redirect name cannot be empty.");
            return;
        }

        String oldName = (String) component.getAttributes().get("vOriginalRedirectName");
        // if the name hasn't changed, just return
        if(oldName != null && oldName.equals(newName)) {
            ((UIInput) component).setValid(true);
            ((UIInput) component).setValidatorMessage(null);
            return;
        }
        // get site name and compare the new name with existing ones
        String siteName = (String) component.getAttributes().get("vSiteName");

        ds = (DataStorage) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(DataStorage.class);

        try {
            PortalConfig cfg = ds.getPortalConfig(siteName);
            List<PortalRedirect> prs = cfg.getPortalRedirects();
            for (PortalRedirect pr : prs) {
                if (pr.getName().equals(newName)) {
                    ((UIInput) component).setValid(false);
                    ((UIInput) component).setValidatorMessage("The redirect name '" + newName + "' already exists.");
                }
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
