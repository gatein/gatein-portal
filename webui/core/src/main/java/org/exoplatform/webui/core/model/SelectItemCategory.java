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

package org.exoplatform.webui.core.model;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by The eXo Platform SARL Author : Nguyen Thi Hoa hoa.nguyen@exoplatform.com Aug 10, 2006
 *
 * Represents a group of SelectItemOption, held in a UIFormInputItemSelector
 *
 * @see org.exoplatform.webui.form.UIFormInputItemSelector
 * @see SelectItemOption
 */
public class SelectItemCategory<T> {
    /**
     * The name of the category
     */
    private String name_;

    /**
     * The label of the category
     */
    private String label_;

    /**
     * The list of SelectItemOption that this category contains
     */
    private List<SelectItemOption<T>> options_;

    /**
     * Whether this category is selected
     */
    protected boolean selected_ = false;

    public SelectItemCategory(String name) {
        this.name_ = name;
    }

    public SelectItemCategory(String name, boolean selected) {
        this.name_ = name;
        label_ = name;
        this.selected_ = selected;
    }

    public void setLabel(String label) {
        label_ = label;
    }

    public String getLabel() {
        return label_;
    }

    public String getName() {
        return name_;
    }

    public void setName(String name) {
        name_ = name;
    }

    public boolean isSelected() {
        return selected_;
    }

    public void setSelected(boolean b) {
        selected_ = b;
    }

    public List<SelectItemOption<T>> getSelectItemOptions() {
        return options_;
    }

    public void setSelectItemOptions(List<SelectItemOption<T>> options) {
        this.options_ = options;
    }

    public SelectItemCategory<T> addSelectItemOption(SelectItemOption<T> option) {
        if (options_ == null) {
            options_ = new ArrayList<SelectItemOption<T>>();
        }
        options_.add(option);
        return this;
    }

    public SelectItemOption<T> getSelectedItemOption() {
        if (options_ == null)
            return null;
        for (SelectItemOption<T> item : options_) {
            if (item.isSelected())
                return item;
        }
        return options_.get(0);
    }
}
