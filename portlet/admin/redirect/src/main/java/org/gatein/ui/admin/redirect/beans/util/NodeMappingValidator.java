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

package org.gatein.ui.admin.redirect.beans.util;

import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import java.io.Serializable;

@FacesValidator("rdrNodeMappingValidator")
public class NodeMappingValidator implements Serializable, Validator {

    @Override
    public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        String nodeValue = (String) value;
        // check for empty redirect name
        if (nodeValue == null || nodeValue.equals("")) {
            ((UIInput) component).setValid(false);
            ((UIInput) component).setValidatorMessage("The node cannot be empty.");
        } else if (!nodeValue.startsWith("/")) {
            ((UIInput) component).setValid(false);
            ((UIInput) component).setValidatorMessage("The node must start with a '/'.");
        } else {
            ((UIInput) component).setValid(true);
            ((UIInput) component).setValidatorMessage("OK!");
        }

        // TODO: Check if node exists in portal, if not, show warning (maybe at client side)

        // TODO: Check if node is already defined in existing node mappings (maybe at client side)
    }
}