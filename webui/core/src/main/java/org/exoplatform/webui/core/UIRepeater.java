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

package org.exoplatform.webui.core;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.exoplatform.commons.serialization.api.annotations.Serialized;
import org.exoplatform.util.ReflectionUtil;
import org.exoplatform.webui.config.annotation.ComponentConfig;

/**
 * Created by The eXo Platform SARL * A grid element (represented by an HTML table) that can be paginated with a UIPageIterator
 *
 * @see UIPageIterator
 */
@ComponentConfig(template = "system:/groovy/webui/core/UIRepeater.gtmpl")
@Serialized
public class UIRepeater extends UIComponent {

    /**
     * The bean field that holds the id of this bean
     */
    protected String beanIdField_;

    /**
     * An array of String representing the fields in each bean
     */
    protected String[] beanField_;

    /**
     * An array of String representing the actions on each bean
     */
    protected String[] action_;

    protected String label_;

    protected Iterator<List<?>> source;

    private List<?> fetchedBeans = Collections.emptyList();

    public UIRepeater configure(String beanIdField, String[] beanField, String[] action) {
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

    public List<?> getBeans() {
        return this.fetchedBeans;
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

    public void feedNext() {
        try {
            this.fetchedBeans = source.next();
        } catch (NoSuchElementException e) {
            this.fetchedBeans = Collections.emptyList();
        }
    }

    public boolean hasNext() {
        return source.hasNext();
    }

    public void setSource(Iterator<List<?>> source) {
        this.source = source;
    }

    public Iterator<List<?>> getSource() {
        return this.source;
    }
}
