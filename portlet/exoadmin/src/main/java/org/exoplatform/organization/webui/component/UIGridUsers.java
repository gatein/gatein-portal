/*
 * Copyright (C) 2012 eXo Platform SAS.
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
package org.exoplatform.organization.webui.component;

import java.lang.reflect.Method;
import java.util.List;

import org.exoplatform.commons.serialization.api.annotations.Serialized;
import org.exoplatform.util.ReflectionUtil;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIPageIterator;

@ComponentConfig(template = "app:/groovy/organization/webui/component/UIGridUsers.gtmpl")
@Serialized
public class UIGridUsers extends UIComponent {
    /** The page iterator */
    protected UIPageIterator uiIterator_;

    /** The bean field that holds the id of this bean */
    protected String beanIdField_;

    /** An array of String representing the fields in each bean */
    protected String[] beanField_;

    /** An array of String representing the actions on each bean */
    protected String[] action_;

    protected String label_;

    protected boolean useAjax = true;

    protected int displayedChars_ = 30;

    public UIGridUsers() throws Exception {
        uiIterator_ = createUIComponent(UIPageIterator.class, null, null);
        uiIterator_.setParent(this);
    }

    public UIPageIterator getUIPageIterator() {
        return uiIterator_;
    }

    public UIGridUsers configure(String beanIdField, String[] beanField, String[] action) {
        this.beanIdField_ = beanIdField;
        this.beanField_ = beanField;
        this.action_ = action;
        return this;
    }

    public String getBeanIdField() {
        return beanIdField_;
    }

    public String[] getBeanFields() {
        return beanField_;
    }

    public String[] getBeanActions() {
        return action_;
    }

    public List<?> getBeans() throws Exception {
        return uiIterator_.getCurrentPageData();
    }

    public String getLabel() {
        return label_;
    }

    public void setLabel(String label) {
        label_ = label;
    }

    public Object getFieldValue(Object bean, String field) throws Exception {
        Method method = ReflectionUtil.getGetBindingMethod(bean, field);
        return method.invoke(bean, ReflectionUtil.EMPTY_ARGS);
    }

    public String getBeanIdFor(Object bean) throws Exception {
        return getFieldValue(bean, beanIdField_).toString();
    }

    @SuppressWarnings("unchecked")
    public UIComponent findComponentById(String lookupId) {
        if (uiIterator_.getId().equals(lookupId)) {
            return uiIterator_;
        }
        return super.findComponentById(lookupId);
    }

    public boolean isUseAjax() {
        return useAjax;
    }

    public void setUseAjax(boolean value) {
        useAjax = value;
    }

    public int getDisplayedChars() {
        return displayedChars_;
    }

    public void setDisplayedChars(int displayedChars) {
        this.displayedChars_ = displayedChars;
    }
}
