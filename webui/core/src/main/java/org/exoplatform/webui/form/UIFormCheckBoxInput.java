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
import org.exoplatform.webui.application.WebuiRequestContext;

/**
 * Represents a checkbox field.
 *
 * @param <T> The type of value that is expected
 * @deprecated use {@link org.exoplatform.webui.form.input.UICheckBoxInput} instead
 */
@SuppressWarnings("hiding")
@Serialized
@Deprecated
public class UIFormCheckBoxInput<T> extends UIFormInputBase<T> {
    /**
     * Whether this checkbox is checked
     */
    private boolean checked = false;

    /**
     * A javascript expression that will be fired when the value changes (JS onChange event)
     */
    private String onchange_;

    private String componentEvent_ = null;

    public UIFormCheckBoxInput() {
    }

    @SuppressWarnings("unchecked")
    public UIFormCheckBoxInput(String name, String bindingExpression, T value) {
        super(name, bindingExpression, null);
        if (value != null)
            typeValue_ = (Class<T>) value.getClass();
        setValue(value);
        setId(name);
    }

    @SuppressWarnings("unchecked")
    public UIFormInput setValue(T value) {
        if (value == null)
            return super.setValue(value);
        if (value instanceof Boolean) {
            checked = ((Boolean) value).booleanValue();
        } else if (boolean.class.isInstance(value)) {
            checked = boolean.class.cast(value);
        } else if (value instanceof String) {
            checked = Boolean.parseBoolean((String) value);
        }
        typeValue_ = (Class<T>) value.getClass();
        return super.setValue(value);
    }

    public void setOnChange(String onchange) {
        onchange_ = onchange;
    }

    public void setComponentEvent(String com) {
        componentEvent_ = com;
    }

    public void setOnChange(String event, String com) {
        this.onchange_ = event;
        this.componentEvent_ = com;
    }

    public String renderOnChangeEvent(UIForm uiForm) throws Exception {
        if (componentEvent_ == null)
            return uiForm.event(onchange_, null);
        return uiForm.event(onchange_, componentEvent_, (String) null);
    }

    public final boolean isChecked() {
        return checked;
    }

    public final UIFormCheckBoxInput setChecked(boolean check) {
        checked = check;
        return this;
    }

    @SuppressWarnings("unused")
    public void decode(Object input, WebuiRequestContext context) {
        if (isDisabled())
            return;
        // This help our code compatible with old serialize style : input is alway "true" or "false"
        checked = input != null && !"false".equals(input.toString());
        if (typeValue_ == Boolean.class || typeValue_ == boolean.class) {
            value_ = typeValue_.cast(checked);
        }
    }

    public void processRender(WebuiRequestContext context) throws Exception {
        Writer w = context.getWriter();
        w.write("<input type='checkbox' name='");
        w.write(name);
        w.write("'");
        w.write(" id='");
        w.write(name);
        w.write("'");
        if (onchange_ != null) {
            UIForm uiForm = getAncestorOfType(UIForm.class);
            w.append(" onclick=\"").append(renderOnChangeEvent(uiForm)).append("\"");
        }
        if (checked)
            w.write(" checked=\"checked\" ");
        if (isDisabled())
            w.write(" disabled=\"disabled\" ");

        renderHTMLAttributes(w);

        w.write(" class='checkbox'/>");
        if (this.isMandatory())
            w.write(" *");
    }

}
