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

import java.io.Serializable;

import org.exoplatform.web.application.CompoundApplicationMessage;
import org.exoplatform.webui.exception.MessageException;
import org.exoplatform.webui.form.UIFormInput;

/** @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a> */
public abstract class MultipleConditionsValidator extends AbstractValidator implements Serializable {
    public void validate(UIFormInput uiInput) throws Exception {
        String value = trimmedValueOrNullIfBypassed((String) uiInput.getValue(), uiInput, exceptionOnMissingMandatory,
                trimValue);
        if (value == null) {
            return;
        }

        String label = getLabelFor(uiInput);

        CompoundApplicationMessage messages = new CompoundApplicationMessage();

        validate(value, label, messages, uiInput);

        if (!messages.isEmpty()) {
            throw new MessageException(messages);
        }
    }

    protected abstract void validate(String value, String label, CompoundApplicationMessage messages, UIFormInput uiInput);

    @Override
    protected String getMessageLocalizationKey() {
        throw new UnsupportedOperationException("Unneeded by this implementation");
    }

    @Override
    protected boolean isValid(String value, UIFormInput uiInput) {
        throw new UnsupportedOperationException("Unneeded by this implementation");
    }
}
