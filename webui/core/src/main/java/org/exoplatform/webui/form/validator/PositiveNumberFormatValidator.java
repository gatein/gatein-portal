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

/**
 * Created by The eXo Platform SARL Author : Vu Duy Tu duytucntt@gmail.com Jun 22, 2007
 *
 * Validates whether this number is positive or 0
 */

public class PositiveNumberFormatValidator extends NumberFormatValidator implements Serializable {

    @Override
    protected Integer validateInteger(String value, String label, CompoundApplicationMessage messages) {
        Integer integer = super.validateInteger(value, label, messages);

        if (integer == null) {
            return null;
        } else if (integer < 0) {
            messages.addMessage("PositiveNumberFormatValidator.msg.Invalid-number", new Object[] { label });
            return null;
        } else {
            return integer;
        }
    }
}
