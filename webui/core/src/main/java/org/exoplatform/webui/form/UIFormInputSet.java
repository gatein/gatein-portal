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

import java.io.Writer;
import java.util.ResourceBundle;

import org.exoplatform.commons.serialization.api.annotations.Serialized;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.bean.BeanDataMapping;
import org.exoplatform.webui.bean.ReflectionDataMapping;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.input.UICheckBoxInput;

/**
 * Created by The eXo Platform SARL Author : Tuan Nguyen tuan08@users.sourceforge.net Jun 8, 2006
 *
 * Represents table containing several input fields
 */
@Serialized
public class UIFormInputSet extends UIContainer {

    private transient BeanDataMapping beanMapping = null;

    private static String selectedCompId = "";

    public UIFormInputSet() {
    }

    public UIFormInputSet(String name) {
        setId(name);
    }

    public UIFormInputSet addUIFormInput(UIFormInput input) {
        addChild((UIComponent) input);
        return this;
    }

    public UIFormInputSet addUIFormInput(UIFormInputSet input) {
        addChild(input);
        return this;
    }

    public String getName() {
        return getId();
    }

    @SuppressWarnings("unchecked")
    public <T extends UIFormInput> T getUIInput(String name) {
        return (T) findComponentById(name);
    }

    public UIFormStringInput getUIStringInput(String name) {
        return (UIFormStringInput) findComponentById(name);
    }

    public String getSelectedComponentId() {
        return selectedCompId;
    }

    public void setSelectedComponent(String renderCompId) {
        selectedCompId = renderCompId;
    }

    public void setSelectedComponent(int index) {
        selectedCompId = ((UIComponent) getChild(index - 1)).getId();
    }

    public UIFormCheckBoxInput getUIFormCheckBoxInput(String name) {
        return (UIFormCheckBoxInput) findComponentById(name);
    }

    public UICheckBoxInput getUICheckBoxInput(String name) {
        return (UICheckBoxInput) findComponentById(name);
    }

    public UIFormSelectBox getUIFormSelectBox(String name) {
        return (UIFormSelectBox) findComponentById(name);
    }

    public UIFormInputInfo getUIFormInputInfo(String name) {
        return (UIFormInputInfo) findComponentById(name);
    }

    public UIFormTextAreaInput getUIFormTextAreaInput(String name) {
        return (UIFormTextAreaInput) findComponentById(name);
    }

    public void reset() {
        for (UIComponent uiChild : getChildren()) {
            if (uiChild instanceof UIFormInput) {
                ((UIFormInput) uiChild).reset();
            }
        }
    }

    public void invokeGetBindingField(Object bean) throws Exception {
        if (beanMapping == null)
            beanMapping = ReflectionDataMapping.getInstance();
        beanMapping.mapField(this, bean);
    }

    public void invokeSetBindingField(Object bean) throws Exception {
        if (beanMapping == null)
            beanMapping = ReflectionDataMapping.getInstance();
        beanMapping.mapBean(bean, this);
    }

    public void processDecode(WebuiRequestContext context) throws Exception {
        for (UIComponent child : getChildren()) {
            child.processDecode(context);
        }
    }

    public void processRender(WebuiRequestContext context) throws Exception {
        if (getComponentConfig() != null) {
            super.processRender(context);
            return;
        }
        Writer w = context.getWriter();
        w.write("<div class=\"UIFormInputSet\">");
        w.write("<table class=\"UIFormGrid\" summary=\"" + getId() + "\">");
        ResourceBundle res = context.getApplicationResourceBundle();
        UIForm uiForm = getAncestorOfType(UIForm.class);
        boolean required = false;
        // Loop to print the (*) required flag in the top
        for (UIComponent inputEntry : getChildren()) {
          if (!required && inputEntry instanceof UIFormInputBase) {
            required = ((UIFormInputBase) inputEntry).isMandatory();
          }
        }
        if (required)
          w.write("<tr><td colspan=\"2\" class=\"require\">" + res.getString("legend.required_field") + " (*)</td></tr>");
        for (UIComponent inputEntry : getChildren()) {
            if (inputEntry.isRendered()) {
                String label = "";
                boolean hasLabel = false;
                if (inputEntry instanceof UIFormInputBase) {
                    UIFormInputBase formInputBase = (UIFormInputBase) inputEntry;
                    if (formInputBase.getLabel() != null) {
                        label = uiForm.getLabel(res, formInputBase.getLabel());
                    } else {
                        label = uiForm.getLabel(res, formInputBase.getId());
                    }
                    if (formInputBase.getLabel() != null || (label != formInputBase.getId())) {
                        hasLabel = true;
                    }
                }
                w.write("<tr>");
                w.write("<td class=\"FieldLabel\" scope=\"row\">");

                // if missing resource and the label hasn't been set before, don't print out the label.
                if (hasLabel) {
                    w.write("<label for=\"" + inputEntry.getId() + "\">" + label + "</label>");
                }
                w.write("</td>");
                w.write("<td class=\"FieldComponent\">");
                renderUIComponent(inputEntry);
                w.write("</td>");
                w.write("</tr>");
            }
        }
        w.write("</table>");
        w.write("</div>");
    }

    public static class SelectComponentActionListener extends EventListener<UIFormInputSet> {
        public void execute(Event<UIFormInputSet> event) throws Exception {
            WebuiRequestContext context = event.getRequestContext();
            String renderTab = context.getRequestParameter(UIComponent.OBJECTID);
            if (renderTab == null)
                return;
            selectedCompId = renderTab;
            context.setResponseComplete(true);
        }
    }

}
