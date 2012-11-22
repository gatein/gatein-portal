/*
 * JBoss, a division of Red Hat
 * Copyright 2012, Red Hat Middleware, LLC, and individual
 * contributors as indicated by the @authors tag. See the
 * copyright.txt in the distribution for a full listing of
 * individual contributors.
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

import org.exoplatform.commons.serialization.api.annotations.Serialized;
import org.exoplatform.webui.form.UIFormInput;

/**
 * @author <a href="mailto:vrockai@redhat.com">Viliam Rockai</a>
 * @datOct 11, 2011
 *
 *         Validates whether this value is a personal name. It's adding two more special characters beside letters and spaces.
 *         Those are "-" (Jean-Claude) and "'" (O'Connor). See GTNPORTAL-2560.
 */
@Serialized
public class PersonalNameValidator extends AbstractValidator {

    @Override
    protected String getMessageLocalizationKey() {
        return "PersonalNameValidator.msg.Invalid-char";
    }

    @Override
    protected boolean isValid(String value, UIFormInput uiInput) {
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (Character.isLetter(c) || Character.isSpaceChar(c) || c == '\'' || c == '-') {
                continue;
            }
            return false;
        }
        return true;
    }
}
