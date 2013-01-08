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

package org.exoplatform.webui.form.validator;

import java.io.Serializable;

import org.exoplatform.web.application.CompoundApplicationMessage;
import org.exoplatform.webui.form.UIFormInput;

/**
 * Created by The eXo Platform SARL Author : Dang Van Minh minhdv81@yahoo.com Jun 7, 2006
 *
 * Validates whether this number is in a correct format
 */
public class NumberFormatValidator extends MultipleConditionsValidator implements Serializable {
    @Override
    protected void validate(String value, String label, CompoundApplicationMessage messages, UIFormInput uiInput) {
        validateInteger(value, label, messages);
    }

    protected Integer validateInteger(String value, String label, CompoundApplicationMessage messages) {
        Object[] args = { label };
        if (value.charAt(0) == '0' && value.length() > 1) {
            messages.addMessage("NumberFormatValidator.msg.Invalid-number", args);
        } else if (value.charAt(0) == '-' && value.length() > 1 && value.charAt(1) == '0') {
            messages.addMessage("NumberFormatValidator.msg.Invalid-number", args);
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            messages.addMessage("NumberFormatValidator.msg.Invalid-number", args);
            return null;
        }
    }

}
