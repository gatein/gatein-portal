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

package org.exoplatform.portal.webui.page;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.InitParams;
import org.exoplatform.webui.config.Param;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ParamConfig;
import org.exoplatform.webui.core.UIDropDownControl;
import org.exoplatform.webui.core.model.SelectItemCategory;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.form.UIFormInputItemSelector;

/**
 * Created by The eXo Platform SARL Author : Nguyen Viet Chung nguyenchung136@yahoo.com Aug 10, 2006
 */
@ComponentConfig(template = "system:/groovy/portal/webui/page/UIPageTemplateOptions.gtmpl", initParams = @ParamConfig(name = "PageLayout", value = "system:/WEB-INF/conf/uiconf/portal/webui/page/PageConfigOptions.groovy"))
public class UIPageTemplateOptions extends UIFormInputItemSelector<String> {

    private SelectItemOption<String> selectedItemOption_ = null;

    public UIPageTemplateOptions(InitParams initParams) throws Exception {
        super("UIPageTemplateOptions", null, String.class);
        if (initParams == null)
            return;
        WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
        Param param = initParams.getParam("PageLayout");
        categories_ = (List<SelectItemCategory<String>>) param.getFreshObject(context);
        selectedItemOption_ = getDefaultItemOption();
        List<SelectItemOption<String>> itemOptions = new ArrayList<SelectItemOption<String>>();

        for (SelectItemCategory<String> itemCategory : categories_) {
            itemOptions.add(new SelectItemOption<String>(itemCategory.getName()));
        }

        // modify: Dang.Tung
        UIDropDownControl uiItemSelector = addChild(UIDropDownControl.class, null, "UIDropDownPageTemp");
        uiItemSelector.setOptions(itemOptions);
        uiItemSelector.setAction("eXo.webui.UIPageTemplateOptions.selectPageLayout");
        // end modify
    }

    public SelectItemOption<String> getDefaultItemOption() {
        SelectItemCategory<String> category = getSelectedCategory();
        if (category == null)
            return null;
        SelectItemOption<String> itemOption = category.getSelectedItemOption();
        if (itemOption == null)
            return null;
        return itemOption;
    }

    public void setSelectOptionItem(String value) {
        boolean found = false;
        for (SelectItemCategory<String> category : categories_) {
            category.setSelected(false);
            if (!found) {
                for (SelectItemOption<String> itemOption : category.getSelectItemOptions()) {
                    if (itemOption.getLabel().equals(value)) {

                        UIDropDownControl uiItemSelector = findFirstComponentOfType(UIDropDownControl.class);
                        uiItemSelector.setValue(category.getName());

                        category.setSelected(true);
                        selectedItemOption_ = itemOption;
                        for (SelectItemOption<String> item : category.getSelectItemOptions()) {
                            item.setSelected(false);
                        }
                        itemOption.setSelected(true);
                        found = true;
                    }
                }
            }
        }
        if (!found) {
            selectedItemOption_ = null;
        }
    }

    public SelectItemOption<String> getSelectedItemOption() {
        return selectedItemOption_;
    }

    public void decode(Object input, WebuiRequestContext context) {
        if (input == null || String.valueOf(input).length() < 1)
            return;
        setSelectOptionItem((String) input);
    }

    public Page createPageFromSelectedOption(String ownerType, String ownerId) throws Exception {
        if (selectedItemOption_ == null)
            selectedItemOption_ = getDefaultItemOption();
        if (selectedItemOption_ == null)
            return null;
        String temp = selectedItemOption_.getValue();
        if (temp == null)
            return null;
        UserPortalConfigService configService = getApplicationComponent(UserPortalConfigService.class);
        return configService.createPageTemplate(temp, ownerType, ownerId);
    }
}
