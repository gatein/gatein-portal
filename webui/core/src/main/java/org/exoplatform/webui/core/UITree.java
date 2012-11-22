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
import java.util.Collection;
import java.util.List;

import org.exoplatform.commons.serialization.api.annotations.Serialized;
import org.exoplatform.commons.utils.HTMLEntityEncoder;
import org.exoplatform.util.ReflectionUtil;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;

/**
 * Author : Nhu Dinh Thuan nhudinhthuan@exoplatform.com Jul 7, 2006
 *
 * A component that represents a tree. Can contain a UIRightClickPopupMenu
 *
 * @see UIRightClickPopupMenu
 */
@ComponentConfig(template = "system:/groovy/webui/core/UITree.gtmpl", events = @EventConfig(listeners = UITree.ChangeNodeActionListener.class))
@Serialized
public class UITree extends UIComponent {
    /**
     * The css class name to show the expand icon
     */
    private String expandIcon = "ExpandIcon";

    /**
     * The css class name to show the collapse icon
     */
    private String colapseIcon = "CollapseIcon";

    /**
     * The css class name to show the null icon (item has no child)
     */
    private String nullItemIcon = "NullItemIcon";

    /**
     * The css class name to show the selected icon
     */
    private String selectedIcon = "";

    private String icon = "";

    /**
     * The bean field that holds the id of the bean
     */
    private String beanIdField_;

    /**
     * The bean field that holds the count number of the children This help to express the node have childs or not
     */
    private String beanChildCountField_;

    /**
     * The bean field that holds the label of the bean
     */
    private String beanLabelField_;

    /**
     * The bean field that holds the icon of the bean
     */
    private String beanIconField_ = "";

    /**
     * The field that holds max character's title of node
     */
    private Integer maxTitleCharacter_ = 0;

    /**
     * A list of sibling nodes
     */
    private Collection<?> sibbling;

    /**
     * A list of children nodes
     */
    private Collection<?> children;

    /**
     * The selected node
     */
    private Object selected;

    /**
     * The parent node of the selected node
     */
    private Object parentSelected;

    /**
     * A right click popup menu
     */
    private UIRightClickPopupMenu uiPopupMenu_;

    /**
     * Encode the value before rendering or not
     */
    private boolean escapeHTML_ = false;

    public Object getFieldValue(Object bean, String field) throws Exception {
        Method method = ReflectionUtil.getGetBindingMethod(bean, field);
        return method.invoke(bean, ReflectionUtil.EMPTY_ARGS);
    }

    public void setBeanIdField(String beanIdField_) {
        this.beanIdField_ = beanIdField_;
    }

    public void setBeanIconField(String beanIconField_) {
        this.beanIconField_ = beanIconField_;
    }

    public void setBeanLabelField(String beanLabelField_) {
        this.beanLabelField_ = beanLabelField_;
    }

    public void setBeanChildCountField(String beanChildCountField) {
        this.beanChildCountField_ = beanChildCountField;
    }

    public Object getId(Object object) throws Exception {
        return getFieldValue(object, beanIdField_);
    }

    public String getActionLink() throws Exception {
        if (selected == null)
            return "#";
        if (parentSelected == null)
            return event("ChangeNode");
        return event("ChangeNode", (String) getId(parentSelected));
    }

    // TODO review equals object with id
    public boolean isSelected(Object obj) throws Exception {
        if (selected == null)
            return false;
        return getId(obj).equals(getId(selected));
    }

    public String getColapseIcon() {
        return colapseIcon;
    }

    public void setCollapseIcon(String colapseIcon) {
        this.colapseIcon = colapseIcon;
    }

    public String getExpandIcon() {
        return expandIcon;
    }

    public void setExpandIcon(String expandIcon) {
        this.expandIcon = expandIcon;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getSelectedIcon() {
        return selectedIcon;
    }

    public void setSelectedIcon(String selectedIcon) {
        this.selectedIcon = selectedIcon;
    }

    public Collection<?> getChildren() {
        return children;
    }

    public void setChildren(Collection<?> children) {
        this.children = children;
    }

    @SuppressWarnings("unchecked")
    public <T> T getSelected() {
        return (T) selected;
    }

    public void setSelected(Object selectedObject) {
        this.selected = selectedObject;
    }

    @SuppressWarnings("unchecked")
    public <T> T getParentSelected() {
        return (T) parentSelected;
    }

    public void setParentSelected(Object parentSelected) {
        this.parentSelected = parentSelected;
    }

    public Collection<?> getSibbling() {
        return sibbling;
    }

    public void setSibbling(Collection<?> sibbling) {
        this.sibbling = sibbling;
    }

    public UIRightClickPopupMenu getUIRightClickPopupMenu() {
        return uiPopupMenu_;
    }

    public void setUIRightClickPopupMenu(UIRightClickPopupMenu uiPopupMenu) {
        uiPopupMenu_ = uiPopupMenu;
        if (uiPopupMenu_ != null)
            uiPopupMenu_.setParent(this);
    }

    public void setEscapeHTML(boolean escape) {
        escapeHTML_ = escape;
    }

    public boolean getEscapeHTML() {
        return escapeHTML_;
    }

    public String event(String name, String beanId) throws Exception {
        UIForm uiForm = getAncestorOfType(UIForm.class);
        if (uiForm != null)
            return uiForm.event(name, getId(), beanId);
        return super.event(name, beanId);
    }

    public String renderNode(Object obj) throws Exception {
        String nodeIcon = expandIcon;
        String iconGroup = icon;

        String note = "";
        if (isSelected(obj)) {
            nodeIcon = colapseIcon;
            iconGroup = selectedIcon;
            note = " NodeSelected";
        }

        if (getBeanChildCountField() != null) {
            Object childCount = getFieldValue(obj, getBeanChildCountField());
            if (childCount != null && childCount.getClass().isAssignableFrom(Integer.class) && (Integer) childCount == 0) {
                nodeIcon = nullItemIcon;
            }
        }

        if (beanIconField_ != null && beanIconField_.length() > 0) {
            if (getFieldValue(obj, beanIconField_) != null)
                iconGroup = (String) getFieldValue(obj, beanIconField_);
        }
        String objId = String.valueOf(getId(obj));
        String actionLink = event("ChangeNode", objId);
        String fieldValue = (String) getFieldValue(obj, beanLabelField_);
        StringBuilder builder = new StringBuilder();

        // if field's length > max field's length then cut field value
        if ((fieldValue.length() > getMaxTitleCharacter()) && (getMaxTitleCharacter() != 0)) {
            fieldValue = fieldValue.substring(0, getMaxTitleCharacter() - 3) + "...";
        }

        if (escapeHTML_) {
            fieldValue = fieldValue != null ? HTMLEntityEncoder.getInstance().encode(fieldValue) : fieldValue;
        }

        if (nodeIcon.equals(expandIcon)) {
            builder.append(" <div class=\"").append(nodeIcon).append("\" onclick=\"").append(actionLink).append("\">");
        } else if (nodeIcon.equals(colapseIcon)) {
            builder.append(" <div class=\"").append(nodeIcon).append("\">");
        } else {// Null item
            builder.append(" <div class=\"").append(nodeIcon).append("\" onclick=\"").append(actionLink).append("\">");
        }
        if (uiPopupMenu_ == null) {
            builder.append(" <a href=\"javascript:void(0);\" class=\"NodeIcon ").append(iconGroup).append(note).append("\"")
                    .append(" title=\"").append(getFieldValue(obj, beanLabelField_)).append("\"").append(">")
                    .append(fieldValue).append("</a>");
        } else {
            builder.append("<a href=\"javascript:void(0);\" class=\"NodeIcon ").append(iconGroup).append(note).append("\" ")
                    .append(uiPopupMenu_.getJSOnclickShowPopup(objId, null)).append(" title=\"")
                    .append(getFieldValue(obj, beanLabelField_)).append("\"").append(">").append(fieldValue).append("</a>");
        }
        builder.append(" </div>");
        return builder.toString();
    }

    public void renderUIComponent(UIComponent uicomponent) throws Exception {
        uicomponent.processRender((WebuiRequestContext) WebuiRequestContext.getCurrentInstance());
    }

    @SuppressWarnings("unchecked")
    public <T extends UIComponent> T findComponentById(String id) {
        if (getId().equals(id))
            return (T) this;
        if (uiPopupMenu_ == null)
            return null;
        return (T) uiPopupMenu_.findComponentById(id);
    }

    public <T extends UIComponent> T findFirstComponentOfType(Class<T> type) {
        if (type.isInstance(this))
            return type.cast(this);
        if (uiPopupMenu_ == null)
            return null;
        return uiPopupMenu_.findFirstComponentOfType(type);
    }

    public <T> void findComponentOfType(List<T> list, Class<T> type) {
        if (type.isInstance(this))
            list.add(type.cast(this));
        if (uiPopupMenu_ == null)
            return;
        uiPopupMenu_.findComponentOfType(list, type);
    }

    public static class ChangeNodeActionListener extends EventListener<UITree> {
        public void execute(Event<UITree> event) throws Exception {
            event.getSource().<UIComponent> getParent().broadcast(event, event.getExecutionPhase());
        }
    }

    public String getBeanIdField() {
        return beanIdField_;
    }

    public String getBeanChildCountField() {
        return beanChildCountField_;
    }

    public String getBeanLabelField() {
        return beanLabelField_;
    }

    public String getBeanIconField() {
        return beanIconField_;
    }

    public void setMaxTitleCharacter(Integer maxTitleCharacter_) {
        this.maxTitleCharacter_ = maxTitleCharacter_;
    }

    public Integer getMaxTitleCharacter() {
        return maxTitleCharacter_;
    }

    public UIRightClickPopupMenu getUiPopupMenu() {
        return uiPopupMenu_;
    }

    public void setUiPopupMenu(UIRightClickPopupMenu uiPopupMenu) {
        this.uiPopupMenu_ = uiPopupMenu;
    }

    public void setColapseIcon(String colapseIcon) {
        this.colapseIcon = colapseIcon;
    }
}
