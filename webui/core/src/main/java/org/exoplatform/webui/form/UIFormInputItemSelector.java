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

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.model.SelectItemCategory;
import org.exoplatform.webui.core.model.SelectItemOption;

/**
 * Created by The eXo Platform SARL Author : Tuan Nguyen tuan08@users.sourceforge.net Jun 26, 2006
 *
 * Represents any item selector, of a given type
 */
@ComponentConfig(template = "system:/groovy/webui/form/UIFormInputItemSelector.gtmpl")
public class UIFormInputItemSelector<T> extends UIFormInputBase<T> {
    /**
     * The type of item selectable
     */
    private Class<? extends T> type_;

    protected List<SelectItemCategory<T>> categories_ = new ArrayList<SelectItemCategory<T>>();

    public UIFormInputItemSelector(String name, String bindingField, Class<T> typeValue) {
        super(name, bindingField, typeValue);
        setComponentConfig(getClass(), null);
    }

    public List<SelectItemCategory<T>> getItemCategories() {
        return categories_;
    }

    public void setItemCategories(List<SelectItemCategory<T>> categories) {
        categories_ = categories;
        boolean selected = false;
        for (SelectItemCategory<T> ele : categories) {
            if (ele.isSelected()) {
                if (selected)
                    ele.setSelected(false);
                else
                    selected = true;
            }
        }
        if (!selected)
            categories_.get(0).setSelected(true);
    }

    public SelectItemCategory<T> getSelectedCategory() {
        for (SelectItemCategory<T> category : categories_) {
            if (category.isSelected())
                return category;
        }
        if (categories_.size() > 0) {
            SelectItemCategory<T> category = categories_.get(0);
            category.setSelected(true);
            category.getSelectItemOptions().get(0).setSelected(true);
            return category;
        }
        return null;
    }

    public SelectItemOption<T> getSelectedItemOption() {
        SelectItemCategory<T> selectedCategory = getSelectedCategory();
        if (selectedCategory == null)
            return null;
        return selectedCategory.getSelectedItemOption();
    }

    public T getValue() {
        SelectItemCategory<T> selectedCategory = getSelectedCategory();
        if (selectedCategory == null)
            return null;
        SelectItemOption<T> selectedItem = selectedCategory.getSelectedItemOption();
        if (selectedItem == null)
            return null;
        return selectedItem.getValue();
    }

    public UIFormInputItemSelector<T> setValue(Object input) {
        for (SelectItemCategory<T> category : categories_) {
            category.setSelected(isSelectItemCategory(category, input));
        }
        return this;
    }

    public Class<? extends T> getTypeValue() {
        if (type_ != null)
            return type_;
        if (getSelectedCategory() == null || getSelectedCategory().getSelectedItemOption() == null
                || getSelectedCategory().getSelectedItemOption().getValue() == null)
            return typeValue_;
        T val = getSelectedCategory().getSelectedItemOption().getValue();
        return (Class<? extends T>) val.getClass();
    }

    public void setTypeValue(Class<? extends T> type) {
        this.type_ = type;
    }

    private boolean isSelectItemCategory(SelectItemCategory<T> category, Object input) {
        List<SelectItemOption<T>> options = category.getSelectItemOptions();
        for (SelectItemOption<T> option : options) {
            if (option.getValue().equals(input)) {
                option.setSelected(true);
                return true;
            }
        }
        return category.getName().equals(input);
    }

    public void decode(Object input, WebuiRequestContext context) {
        // System.out.println("\n\n\n == > current input value is "+input+"\n\n");
        if (input == null || String.valueOf(input).length() < 1)
            return;
        setValue(input);
    }

}
