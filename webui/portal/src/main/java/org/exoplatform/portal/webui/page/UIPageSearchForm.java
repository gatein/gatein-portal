/**
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
package org.exoplatform.portal.webui.page;

import java.util.List;

import org.exoplatform.commons.serialization.api.annotations.Serialized;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormInputSet;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.validator.ExpressionValidator;

/**
 * @author <a href="kienna@exoplatform.com">Kien Nguyen</a>
 * @version $Revision$
 */
@ComponentConfig(
        lifecycle = UIFormLifecycle.class,
        template = "system:/groovy/portal/webui/page/UIPageSearchForm.gtmpl",
        events = {
                @EventConfig(listeners = UIPageSearchForm.QuickSearchActionListener.class),
                @EventConfig(listeners = UIPageSearchForm.ClearSearchActionListener.class)
        }
)
@Serialized
public class UIPageSearchForm extends UIForm {
    /**
     * The name of the quick search set
     */
    public static final String QUICK_SEARCH_SET = "QuickSearchSet";

    public UIPageSearchForm() throws Exception {
        UIFormInputSet uiQuickSearchSet = new UIFormInputSet(QUICK_SEARCH_SET);
        uiQuickSearchSet.addUIFormInput(new UIFormStringInput("pageTitle", "pageTitle", null));
        uiQuickSearchSet.addUIFormInput(new UIFormStringInput("siteName", "siteName", null).addValidator(
                ExpressionValidator.class, "[^\\'\"]*", "UISearchForm.msg.empty"));
        uiQuickSearchSet.addUIFormInput(new UIFormSelectBox("searchOption", null, null));
        addChild(uiQuickSearchSet);
    }

    public void setOptions(List<SelectItemOption<String>> options) {
        UIFormSelectBox uiSelect = (UIFormSelectBox) getQuickSearchInputSet().getChild(2);
        uiSelect.setOptions(options);
    }

    public UIFormInputSet getQuickSearchInputSet() {
        return (UIFormInputSet) getChild(0);
    }

    public static class QuickSearchActionListener extends EventListener<UIPageSearchForm> {
        public void execute(Event<UIPageSearchForm> event) throws Exception {
            UIPageSearchForm uiForm = event.getSource();
            UIPageBrowser uiSearch = uiForm.getParent();
            uiSearch.quickSearch(uiForm.getQuickSearchInputSet());
            event.getRequestContext().addUIComponentToUpdateByAjax(uiSearch);
        }
    }

    public static class ClearSearchActionListener extends EventListener<UIPageSearchForm> {
        public void execute(Event<UIPageSearchForm> event) throws Exception {
            UIPageSearchForm uiForm = event.getSource();
            UIPageBrowser uiSearch = uiForm.getParent();
            uiForm.getQuickSearchInputSet().reset();
            uiSearch.quickSearch(uiForm.getQuickSearchInputSet());
            event.getRequestContext().addUIComponentToUpdateByAjax(uiSearch);
        }
    }
}
