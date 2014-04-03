/*
 * Copyright (C) 2011 eXo Platform SAS.
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

package org.exoplatform.webui.form.input;

import java.io.Writer;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.exoplatform.commons.serialization.api.annotations.Serialized;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormInput;
import org.exoplatform.webui.form.UIFormInputBase;

/**
 * <p>
 * Represent an html checkbox input <br/>
 * This class is a replacement for {@link org.exoplatform.webui.form.UIFormCheckBoxInput} <br/>
 * Still support raising event when user click the checkbox, but now we only accept boolean value.
 * </p>
 *
 * @author <a href="mailto:phuong.vu@exoplatform.com">Vu Viet Phuong</a>
 * @version $Id$
 */
@Serialized
public class UICheckBoxInput extends UIFormInputBase<Boolean> {
    /**
     * Name of {@link org.exoplatform.webui.event.EventListener} that will be fired when checkbox state is changed
     */
    private String onchange_;

    /**
     * Id of {@link org.exoplatform.webui.core.UIComponent} that is configured with the fired event This component must be in
     * the same form with the checkbox If this field is null, event of the UIForm will be fire instead
     */
    private String componentEvent_ = null;

    public UICheckBoxInput() {
        this(null, null, false);
    }

    public UICheckBoxInput(String name, String bindingExpression, Boolean value) {
        super(name, bindingExpression, Boolean.class);
        setValue(value);
    }

    public UIFormInput<Boolean> setValue(Boolean value) {
        if (value == null) {
            value = false;
        }

        return super.setValue(value);
    }

    /**
     * This method is used to make the action more meaning in the context of a checkbox
     */
    public boolean isChecked() {
        return getValue() == null ? false : getValue();
    }

    /**
     * This method is used to make the action more meaning in the context of a checkbox
     */
    public UICheckBoxInput setChecked(boolean check) {
        return (UICheckBoxInput) setValue(check);
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

    public void decode(Object input, WebuiRequestContext context) {
        if (isDisabled())
            return;

        if (input == null || "false".equals(input.toString())) {
            setValue(false);
        } else {
            setValue(true);
        }
    }

    public void processRender(WebuiRequestContext context) throws Exception {
        ResourceBundle res = context.getApplicationResourceBundle();
        String label = getId() + ".label";
        try {
            label = res.getString(label);
        } catch (MissingResourceException e) {
            label = null;
        }

        Writer w = context.getWriter();
        if (label != null) {
            w.write("<label class=\"uiCheckbox\">");
        } else {
            w.write("<span class=\"uiCheckbox\">");
        }
        w.append("<input id=\"").append(getId()).append("\" type=\"checkbox\" class=\"checkbox\" name=\"");
        w.write(name);
        w.write("\"");
        if (onchange_ != null) {
            UIForm uiForm = getAncestorOfType(UIForm.class);
            w.append(" onclick=\"").append(renderOnChangeEvent(uiForm)).append("\"");
        }
        if (isChecked())
            w.write(" checked=\"checked\" ");
        if (isDisabled())
            w.write(" disabled=\"disabled\" ");

        renderHTMLAttributes(w);

        w.write("/><span>");
        if (label != null) {
            w.write(label);
            w.write("</span></label>");
        } else {
            w.write("</span></span>");
        }
        if (this.isMandatory())
            w.write(" *");
    }

}