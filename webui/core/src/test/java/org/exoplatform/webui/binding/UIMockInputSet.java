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
package org.exoplatform.webui.binding;

import java.io.Serializable;

import org.exoplatform.webui.form.UIFormInputSet;
import org.exoplatform.webui.form.UIFormStringInput;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Hai Thanh Nguyen</a>
 * @version $Id$
 *
 */
public class UIMockInputSet extends UIFormInputSet {
    public UIMockInputSet() {
        super("UIMockInputSet");
        addUIFormInput(new UIFormStringInput("value1", "value1", "value1"));
        addUIFormInput(new UIFormStringInput("value2", "value2", "value2"));
        addUIFormInput(new UIFormStringInput("value3", "value3", "value3"));
    }

    public void setFieldValue(String fieldName, String value) {
        ((UIFormStringInput) getChildById(fieldName)).setValue(value);
    }

    public void setReadonlyForField(String fieldName, boolean readonly) {
        ((UIFormStringInput) getChildById(fieldName)).setReadOnly(readonly);
    }

    public void setDisableForField(String fieldName, boolean disabled) {
        ((UIFormStringInput) getChildById(fieldName)).setDisabled(disabled);
    }

    public void binding(MockModel obj) throws Exception {
        if (obj == null) {
            return;
        }
        invokeSetBindingField(obj);
    }

    public static class MockModel implements Serializable {

        private static final long serialVersionUID = 1L;

        private String value1;

        private String value2;

        private String value3;

        public String getValue1() {
            return value1;
        }

        public void setValue1(String value1) {
            this.value1 = value1;
        }

        public String getValue2() {
            return value2;
        }

        public void setValue2(String value2) {
            this.value2 = value2;
        }

        public String getValue3() {
            return value3;
        }

        public void setValue3(String value3) {
            this.value3 = value3;
        }
    }
}
