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

package org.exoplatform.portal.webui.workspace;

import java.io.IOException;
import java.io.OutputStream;

import org.exoplatform.commons.utils.PortalPrinter;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.resolver.ResourceResolver;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.Lifecycle;
import org.exoplatform.webui.core.lifecycle.WebuiBindingContext;

/**
 * Created by The eXo Platform SAS May 8, 2006
 */
public class UIPortalApplicationLifecycle extends Lifecycle<UIPortalApplication> {

    public void processDecode(UIPortalApplication uicomponent, WebuiRequestContext context) throws Exception {
        String componentId = context.getRequestParameter(context.getUIComponentIdParameterName());
        if (componentId == null)
            return;
        UIComponent uiTarget = uicomponent.findComponentById(componentId);
        if (uiTarget == null) {
            context.addUIComponentToUpdateByAjax(uicomponent.<UIComponent> getChildById(UIPortalApplication.UI_WORKING_WS_ID));
            context.addUIComponentToUpdateByAjax(uicomponent.getChild(UIMaskWorkspace.class));
            ((PortalRequestContext) context).ignoreAJAXUpdateOnPortlets(true);
            return;
        }
        if (uiTarget == uicomponent) {
            super.processDecode(uicomponent, context);
        } else {
            uiTarget.processDecode(context);
        }
    }

    /**
     * The processAction() method of the UIPortalApplication is called, as there is no method in the object itself it will call
     * the processAction() of the UIPortalApplicationLifecycle bound to the UI component
     *
     * If no uicomponent object is targeted, which is the case the first time (unless a bookmarked link is used) then nothing is
     * done. Otherwise, the targeted component is extracted and a call of its processAction() method is executed.
     *
     */
    public void processAction(UIPortalApplication uicomponent, WebuiRequestContext context) throws Exception {
        String componentId = context.getRequestParameter(context.getUIComponentIdParameterName());
        if (componentId == null)
            return;
        UIComponent uiTarget = uicomponent.findComponentById(componentId);
        if (uiTarget == null)
            return;
        if (uiTarget == uicomponent)
            super.processAction(uicomponent, context);
        uiTarget.processAction(context);
    }

    public void processRender(UIPortalApplication uicomponent, WebuiRequestContext context) throws Exception {
        /*
         * We need to render the child elements of the portal first since portlets can set the markup headers during there
         * render call. We cannot render the page in the order of the UIPortalApplication since this will create the headers
         * before the portlets are rendered. To get around this we need to render the portlets and UIPortalApplication childrens
         * to a separate writer first, then render the UIPortalApplication page as normal, outputting the contents of the
         * separate writer where the child elements should be rendered. Its messy but required for portlet markup header
         * setting.
         */

        PortalRequestContext prc = (PortalRequestContext) context;
        OutputStream responseOutputStream = prc.getResponse().getOutputStream();

        PortalPrinter parentWriter = new PortalPrinter(responseOutputStream, true, 5000);
        PortalPrinter childWriter = new PortalPrinter(responseOutputStream, true, 25000, true);

        context.setWriter(childWriter);
        processRender(uicomponent, context, "system:/groovy/portal/webui/workspace/UIPortalApplicationChildren.gtmpl");

        context.setWriter(parentWriter);
        processRender(uicomponent, context, "system:/groovy/portal/webui/workspace/UIPortalApplication.gtmpl");

        try {
            // flush the parent writer to the output stream so that we are really to accept the child content
            parentWriter.flushOutputStream();
            // now that the parent has been flushed, we can flush the contents of the child to the output
            childWriter.flushOutputStream();
        } catch (IOException ioe) {
            // We want to ignore the ClientAbortException since this is caused by the users
            // browser closing the connection and is not something we should be logging.
            if (!ioe.getClass().toString().contains("ClientAbortException")) {
                throw ioe;
            }

        }
    }

    public void processRender(UIPortalApplication uicomponent, WebuiRequestContext context, String template) throws Exception {
        // Fail if we have no template
        if (template == null) {
            throw new IllegalStateException("uicomponent " + uicomponent + " with class " + uicomponent.getClass().getName()
                    + " has no template for rendering");
        }

        //
        ResourceResolver resolver = uicomponent.getTemplateResourceResolver(context, template);
        WebuiBindingContext bcontext = new WebuiBindingContext(resolver, context.getWriter(), uicomponent, context);
        bcontext.put(UIComponent.UICOMPONENT, uicomponent);
        renderTemplate(template, bcontext);
    }

}
