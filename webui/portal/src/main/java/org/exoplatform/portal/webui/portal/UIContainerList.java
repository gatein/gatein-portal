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

package org.exoplatform.portal.webui.portal;

import java.io.ByteArrayInputStream;
import java.util.List;

import org.exoplatform.portal.config.model.Container;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.InitParams;
import org.exoplatform.webui.config.Param;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.config.annotation.ParamConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.model.SelectItemCategory;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IUnmarshallingContext;

/**
 * @author <a href="trongtt@gmail.com">Trong Tran</a>
 * @version $Revision$
 */

@ComponentConfig(template = "system:/groovy/portal/webui/container/UIContainerList.gtmpl", events = { @EventConfig(listeners = UIContainerList.SelectCategoryActionListener.class) }, initParams = @ParamConfig(name = "ContainerConfigOption", value = "system:/WEB-INF/conf/uiconf/portal/webui/container/ContainerConfigOption.groovy"))
public class UIContainerList extends UIContainer {

    private List<SelectItemCategory> categories_;

    private SelectItemCategory selectedCategory_;

    public UIContainerList(InitParams initParams) throws Exception {
        // setComponentConfig(UIContainerConfigOptions.class, null);
        selectedCategory_ = null;
        if (initParams == null)
            return;
        WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
        Param param = initParams.getParam("ContainerConfigOption");
        categories_ = (List<SelectItemCategory>) param.getMapGroovyObject(context);
        if (categories_ == null)
            return;
        setSelectedCategory(categories_.get(0));
    }

    public void setSelectedCategory(SelectItemCategory selectedCategory) {
        selectedCategory_ = selectedCategory;
    }

    public void setSelectedCategory(String name) {
        for (SelectItemCategory itemCategory : categories_) {
            if (itemCategory.getName().equals(name)) {
                selectedCategory_ = itemCategory;
                return;
            }
        }
    }

    public SelectItemCategory getSelectedCategory() {
        return selectedCategory_;
    }

    public List<SelectItemCategory> getCategories() {
        return categories_;
    }

    public void setCategories(List<SelectItemCategory> categories) {
        categories_ = categories;
    }

    public Container getContainer(String id) throws Exception {
        for (SelectItemCategory category : categories_) {
            List<SelectItemOption> items = category.getSelectItemOptions();
            for (SelectItemOption item : items) {
                if (item.getLabel().equals(id))
                    return toContainer(item.getValue().toString());
            }
        }
        return null;
    }

    private Container toContainer(String xml) throws Exception {
        ByteArrayInputStream is = new ByteArrayInputStream(xml.getBytes());
        IBindingFactory bfact = BindingDirectory.getFactory(Container.class);
        IUnmarshallingContext uctx = bfact.createUnmarshallingContext();
        return (Container) uctx.unmarshalDocument(is, null);
    }

    public static class SelectCategoryActionListener extends EventListener<UIContainerList> {
        public void execute(Event<UIContainerList> event) throws Exception {
            String category = event.getRequestContext().getRequestParameter(OBJECTID);
            UIContainerList uiContainerList = event.getSource();
            uiContainerList.setSelectedCategory(category);
            event.getRequestContext().addUIComponentToUpdateByAjax(uiContainerList);
        }

    }

}
