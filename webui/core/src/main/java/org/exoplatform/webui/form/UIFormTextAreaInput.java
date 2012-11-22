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

import org.exoplatform.commons.serialization.api.annotations.Serialized;
import org.exoplatform.commons.utils.HTMLEntityEncoder;
import org.exoplatform.webui.application.WebuiRequestContext;

/**
 * Represents a textarea element The value is stored in UIFormInputBase
 */
@Serialized
public class UIFormTextAreaInput extends UIFormInputBase<String> {
    /**
     * number of rows
     */
    private int rows = 10;

    /**
     * number of columns
     */
    private int columns = 30;

    public UIFormTextAreaInput() {
    }

    public UIFormTextAreaInput(String name, String bindingExpression, String value) {
        super(name, bindingExpression, String.class);
        this.value_ = value;
    }

    public void decode(Object input, WebuiRequestContext context) {
        String val = (String) input;
        value_ = val;
        if (value_ != null && value_.length() == 0)
            value_ = null;
    }

    public void processRender(WebuiRequestContext context) throws Exception {
        Writer w = context.getWriter();
        String value = getValue();
        if (value == null)
            value = getDefaultValue();
        w.append("<textarea class=\"textarea\" name=\"").append(getName()).append("\" id=\"").append(getId()).append("\"");
        if (readonly_)
            w.write(" readonly ");
        if (isDisabled())
            w.write(" disabled ");
        w.append(" rows=\"").append(String.valueOf(rows)).append("\"");
        w.append(" cols=\"").append(String.valueOf(columns)).append("\"");

        renderHTMLAttributes(w);

        w.write(">");
        if (value != null) {
            value = HTMLEntityEncoder.getInstance().encode(value);
            w.write(value);
        }
        w.write("</textarea>");
        if (this.isMandatory())
            w.write(" *");
    }

    public int getColumns() {
        return columns;
    }

    public void setColumns(int columns) {
        this.columns = columns;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

}
