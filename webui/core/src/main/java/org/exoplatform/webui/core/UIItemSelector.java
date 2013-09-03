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

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.model.SelectItemCategory;
import org.exoplatform.webui.core.model.SelectItemOption;

/**
 * Created by The eXo Platform SARL Author : Pham Thanh Tung Apr 16, 2007
 *
 * An item selector represented by a normal list
 */
@ComponentConfig(template = "system:/groovy/webui/core/UIItemSelector.gtmpl")
public class UIItemSelector<T> extends UIComponent {
    /**
     * The name of this selector
     */
    private String name_;

    /**
     * The item categories, each category contains items
     */
    private List<SelectItemCategory<T>> categories_;

    public UIItemSelector(String name) {
        name_ = name;
        setComponentConfig(getClass(), null);
        categories_ = new ArrayList<SelectItemCategory<T>>();
    }

    public String getName() {
        return name_;
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

    public SelectItemCategory<T> getSelectedItemCategory() {
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
        SelectItemCategory<T> selectedCategory = getSelectedItemCategory();
        if (selectedCategory != null)
            return selectedCategory.getSelectedItemOption();
        return null;
    }

}
