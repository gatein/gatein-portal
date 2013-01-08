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

package org.exoplatform.webui.form;

import java.io.Writer;

import org.exoplatform.commons.utils.HTMLEntityEncoder;
import org.exoplatform.webui.application.WebuiRequestContext;

/**
 * Created by The eXo Platform SARL Author : lxchiati lebienthuyt@gmail.com Jun 6, 2006
 *
 * Repesents a hidden input field
 */
public class UIFormHiddenInput extends UIFormInputBase<String> {

    public UIFormHiddenInput(String name, String bindingExpression, String value) {
        super(name, bindingExpression, String.class);
        this.value_ = value;
    }

    public UIFormHiddenInput(String name, String value) {
        this(name, null, value);
    }

    @SuppressWarnings("unused")
    public void decode(Object input, WebuiRequestContext context) {
        value_ = (String) input;
        if (value_ != null && value_.length() == 0)
            value_ = null;
    }

    public void processRender(WebuiRequestContext context) throws Exception {
        Writer w = context.getWriter();
        w.write("<input name='");
        w.write(getName());
        w.write("'  type='hidden'");
        w.write(" id='");
        w.write(getId());
        w.write("'");
        String value = getValue();
        if (value != null && value.length() > 0) {
            w.write(" value='");
            value = HTMLEntityEncoder.getInstance().encodeHTMLAttribute(value);
            w.write(value);
            w.write("'");
        }

        renderHTMLAttributes(w);

        w.write(" />");
    }
}
