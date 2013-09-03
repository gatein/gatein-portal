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

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.commons.serialization.api.annotations.Serialized;
import org.exoplatform.commons.utils.HTMLEntityEncoder;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.form.validator.MandatoryValidator;
import org.exoplatform.webui.form.validator.Validator;

/**
 * Created by The eXo Platform SARL Author : Tuan Nguyen tuan08@users.sourceforge.net Jun 6, 2006
 *
 * The base class to create form elements. Extend it to create your own elements.
 */
@Serialized
public abstract class UIFormInputBase<T> extends UIContainer implements UIFormInput<T> {
    /**
     * @deprecated According to deprecation of the {@link #setEditable(boolean)} and {@link #setEnable(boolean)} methods
     */
    @Deprecated
    public static final boolean ENABLE = true, DISABLE = false;

    /**
     * The HTML 'name' attribute of this element
     */
    protected String name;

    /**
     * The HTML 'label' field of this element
     */
    private String label;

    /**
    *
    */
    protected String bindingField;

    /**
     * The list of validators for this form element
     */
    protected List<Validator> validators;

    /**
     * A default value for this field
     */
    protected T defaultValue_;

    /**
     * The current value of this field
     */
    protected T value_;

    /**
     * The type of value that is expected
     */
    protected Class<? extends T> typeValue_;

    /**
     * @deprecated According to the deprecation of the {@link #setEnable(boolean)} method
     *
     *             Whether this field is enabled
     */
    @Deprecated
    protected boolean enable_ = true;

    /**
     * Whether this field is disabled.
     */
    protected boolean disabled = false;

    /**
     * Whether this field is in read only mode
     */
    protected boolean readonly_ = false;

    /**
     * A map of HTML attribute
     */
    private Map<String, String> attributes;

    public UIFormInputBase(String name, String bindingField, Class<T> typeValue) {
        this.name = name;
        this.bindingField = bindingField;
        this.typeValue_ = typeValue;
        setId(name);
    }

    protected UIFormInputBase() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBindingField() {
        return bindingField;
    }

    public void setBindingField(String s) {
        this.bindingField = s;
    }

    public T getDefaultValue() {
        return defaultValue_;
    }

    public void setDefaultValue(T defaultValue) {
        defaultValue_ = defaultValue;
    }

    public T getValue() {
        return value_;
    }

    public UIFormInput<T> setValue(T value) {
        this.value_ = value;
        return this;
    }

    public Class<? extends T> getTypeValue() {
        return typeValue_;
    }

    public void reset() {
        value_ = defaultValue_;
    }

    /**
     * @deprecated Use {@link #isDisabled()} instead
     * @return
     */
    @Deprecated
    public boolean isEnable() {
        return !isDisabled();
    }

    /**
     * @deprecated Use {@link #setDisabled(boolean)} instead
     *
     * @param enable
     * @return
     */
    @Deprecated
    public UIFormInputBase<T> setEnable(boolean enable) {
        return setDisabled(!enable);
    }

    /**
     * Return <code>true</code> if this input field is disabled. Otherwise, return <code>false</code>.
     *
     * @return True if this input field is disabled. Otherwise, return false.
     */
    public boolean isDisabled() {
        return disabled;
    }

    /**
     * Specifies that this input field should be disabled OR NOT.
     *
     * @param disabled
     * @return
     */
    public UIFormInputBase<T> setDisabled(boolean disabled) {
        this.disabled = disabled;
        enable_ = !disabled; // for compatibility
        return this;
    }

    /**
     * @deprecated Use {@link #isReadOnly()} instead
     *
     * @return True if the input is read only. Otherwise, return false.
     */
    @Deprecated
    public boolean isEditable() {
        return !isReadOnly();
    }

    /**
     * @deprecated Use {@link #setReadOnly(boolean)} instead
     *
     * @param editable
     * @return
     */
    @Deprecated
    public UIFormInputBase<T> setEditable(boolean editable) {
        return setReadOnly(!editable);
    }

    /**
     * Return <code>true</code> if this input field is read only. Otherwise, return <code>false</code>.
     *
     * @return True if the input is read only. Otherwise, return false.
     */
    public boolean isReadOnly() {
        return readonly_;
    }

    /**
     * Specifies that this input field should be read-only OR NOT.
     *
     * @param readonly
     * @return
     */
    public UIFormInputBase<T> setReadOnly(boolean readonly) {
        readonly_ = readonly;
        return this;
    }

    public boolean isValid() {
        return (isRendered() && !isDisabled());
    }

    public <E extends Validator> UIFormInputBase<T> addValidator(Class<E> clazz, Object... params) throws Exception {
        if (validators == null)
            validators = new ArrayList<Validator>(3);
        if (params.length > 0) {
            Class<?>[] classes = new Class[params.length];
            for (int i = 0; i < params.length; i++) {
                classes[i] = params[i].getClass();
            }
            Constructor<E> constructor = clazz.getConstructor(classes);
            validators.add(constructor.newInstance(params));
            return this;
        }
        validators.add(clazz.newInstance());
        return this;
    }

    public List<Validator> getValidators() {
        return validators;
    }

    public final void processDecode(WebuiRequestContext context) throws Exception {
        UIForm uiForm = getAncestorOfType(UIForm.class);
        String action = uiForm.getSubmitAction(); // context.getRequestParameter(UIForm.ACTION) ;
        Event<UIComponent> event = createEvent(action, Event.Phase.DECODE, context);
        if (event != null)
            event.broadcast();
    }

    public boolean isMandatory() {
        if (validators == null)
            return false;
        for (Validator validator : validators) {
            if (validator instanceof MandatoryValidator)
                return true;
        }
        return false;
    }

    public abstract void decode(Object input, WebuiRequestContext context);

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getHTMLAttribute(String name) {
        if (attributes != null) {
            return attributes.get(name);
        }
        return null;
    }

    public void setHTMLAttribute(String name, String value) {
        if (attributes == null) {
            attributes = new HashMap<String, String>();
        }
        attributes.put(name, value);
    }

    protected void renderHTMLAttributes(Writer w) throws IOException {
        if (attributes != null) {
            w.append(" ");
            for (String name : attributes.keySet()) {
                String value = HTMLEntityEncoder.getInstance().encodeHTMLAttribute(attributes.get(name));
                w.append(name + "=\"" + value + "\"");
            }
            w.append(" ");
        }
    }
}
