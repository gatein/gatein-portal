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
import org.exoplatform.webui.core.model.SelectItemOption;

/**
 * Created by The eXo Platform SARL Author : Tran The Trong trongtt@gmail.com July 12, 2007
 *
 * A drop down selector
 */
@ComponentConfig(template = "system:/groovy/webui/core/UIDropDownControl.gtmpl")
public class UIDropDownControl extends UIComponent {
    /**
     * The action to perform when an item is selected
     */
    private String action_;

    /**
     * The list of items
     */
    private List<SelectItemOption<String>> options_;

    /**
     * The index in the list of the selected item
     */
    private int selectedItemIndex_ = 0;

    public UIDropDownControl() {
        options_ = new ArrayList<SelectItemOption<String>>();
    }

    public void setValue(String value) {
        int ln = options_.size();
        for (int i = 0; i < ln; i++) {
            if (options_.get(i).getValue().equals(value)) {
                selectedItemIndex_ = i;
                return;
            }
        }
    }

    public void setValue(int i) {
        selectedItemIndex_ = i;
        return;
    }

    public void cleanItem() {
        options_.clear();
    }

    public void setOptions(List<SelectItemOption<String>> options) {
        options_ = options;
        if (options == null)
            return;
        if (options_.size() < 1)
            return;
    }

    public int getSelectedIndex() {
        if (options_.size() < 1)
            return -1;
        return selectedItemIndex_;
    }

    public String getLabel() {
        return options_.get(selectedItemIndex_).getLabel();
    }

    public String getValue() {
        return options_.get(selectedItemIndex_).getValue();
    }

    public void setAction(String act) {
        action_ = act;
    }

    public String getAction() {
        return action_;
    }

    public List<SelectItemOption<String>> getOptions() {
        return options_;
    }

    public void addItem(SelectItemOption<String> opt) {
        options_.add(opt);
    }

    public void addItem(String value) {
        options_.add(new SelectItemOption<String>(value));
    }

    public void addItem(String label, String value) {
        options_.add(new SelectItemOption<String>(label, value));
    }

    public String event(int selectedIndex) throws Exception {
        if (action_ == null) {
            return super.event(config.getEvents().get(0).getName(), options_.get(selectedIndex).getValue());
        }
        StringBuilder evt = new StringBuilder("javascript:eXo.webui.UIDropDownControl.selectItem(");
        evt.append(action_).append(",'").append(this.getId()).append("','").append(selectedIndex).append("')");
        return evt.toString();
    }
}
