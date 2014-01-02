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

import java.io.Serializable;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.exoplatform.commons.serialization.api.annotations.Serialized;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.core.UIComponent;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;

/**
 * Created by The eXo Platform SARL Author : Dang Van Minh minh.dang@exoplatform.com Sep 20, 2006
 */
@Serialized
public class UIFormInputWithActions extends UIFormInputSet {

    /** . */
    private static final Logger log = LoggerFactory.getLogger(UIFormInputWithActions.class);

    Map<String, List<ActionData>> actionField = new HashMap<String, List<ActionData>>();

    public UIFormInputWithActions() {
    }

    public UIFormInputWithActions(String id) {
        super.setId(id);
    }

    public void setActionField(String fieldName, List<ActionData> actions) {
        actionField.put(fieldName, actions);
    }

    public void processRender(WebuiRequestContext context) throws Exception {
        if (getComponentConfig() != null) {
            super.processRender(context);
            return;
        }
        UIForm uiForm = getAncestorOfType(UIForm.class);
        Writer w = context.getWriter();
        w.write("<div id=\"" + getId() + "\" class=\"UIFormInputSet " + getId() + "\">");
        w.write("<table class=\"UIFormGrid\" summary=\"" + getId() + "\">");
        ResourceBundle res = context.getApplicationResourceBundle();

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
                String label;
                try {
                    label = uiForm.getLabel(res, inputEntry.getId());
                    if (inputEntry instanceof UIFormInputBase) {
                        ((UIFormInputBase) inputEntry).setLabel(label);
                    }
                } catch (MissingResourceException ex) {
                    label = inputEntry.getId();
                    log.error("\n " + uiForm.getId() + ".label." + inputEntry.getId() + " not found value");
                }
                w.write("<tr>");
                w.write("<td class=\"FieldLabel\" scope=\"row\">");
                w.write("<label for=\"" + inputEntry.getId() + "\">" + label + "</label>");
                w.write("</td>");
                w.write("<td class=\"FieldComponent\">");
                renderUIComponent(inputEntry);
                List<ActionData> actions = actionField.get(inputEntry.getName());
                if (actions != null) {
                    for (ActionData action : actions) {
                        String actionLabel;
                        try {
                            actionLabel = uiForm.getLabel(res, "action." + action.getActionName());
                        } catch (MissingResourceException ex) {
                            actionLabel = action.getActionName();
                            log.debug("Key: '" + uiForm.getId() + ".label.action." + action.getActionName() + "' not found");
                        }
                        String actionLink;
                        if (action.getActionParameter() != null) {
                            actionLink = getParent().event(action.getActionListener(), action.getActionParameter());
                        } else {
                            actionLink = getParent().event(action.getActionListener());
                        }

                        if (action.getActionType() == ActionData.TYPE_ICON) {
                            w.write("<img title=\"" + actionLabel + "\" onclick=\"" + actionLink + "\" "
                                    + "src=\"/eXoResources/skin/DefaultSkin/background/Blank.gif\" class=\""
                                    + action.getCssIconClass() + "\" alt=\"" + actionLabel + "\"/>");
                            if (action.isShowLabel)
                                w.write(actionLabel);
                        } else if (action.getActionType() == ActionData.TYPE_LINK) {
                            w.write("<a title=\"" + actionLabel + "\" href=\"" + actionLink + "\">" + actionLabel + "</a>");
                        }
                        w.write("&nbsp;");
                        if (action.isBreakLine())
                            w.write("<br/>");
                    }
                }
                w.write("</td>");
                w.write("</tr>");
            }
        }
        w.write("</table>");
        w.write("</div>");
    }

    public static class ActionData implements Serializable {
        public static final int TYPE_ICON = 0;

        public static final int TYPE_LINK = 1;

        private int actionType = 0;

        private String actionName;

        private String actionListener;

        private String actionParameter = null;

        private String cssIconClass = "AddNewNodeIcon";

        private boolean isShowLabel = false;

        private boolean isBreakLine = false;

        public void setActionType(int actionType) {
            this.actionType = actionType;
        }

        public int getActionType() {
            return actionType;
        }

        public void setActionName(String actionName) {
            this.actionName = actionName;
        }

        public String getActionName() {
            return actionName;
        }

        public void setActionListener(String actionListener) {
            this.actionListener = actionListener;
        }

        public String getActionListener() {
            return actionListener;
        }

        public void setActionParameter(String actionParameter) {
            this.actionParameter = actionParameter;
        }

        public String getActionParameter() {
            return actionParameter;
        }

        public void setCssIconClass(String cssIconClass) {
            this.cssIconClass = cssIconClass;
        }

        public String getCssIconClass() {
            return cssIconClass;
        }

        public void setShowLabel(boolean isShowLabel) {
            this.isShowLabel = isShowLabel;
        }

        public boolean isShowLabel() {
            return isShowLabel;
        }

        public void setBreakLine(boolean isBreakLine) {
            this.isBreakLine = isBreakLine;
        }

        public boolean isBreakLine() {
            return isBreakLine;
        }
    }
}
