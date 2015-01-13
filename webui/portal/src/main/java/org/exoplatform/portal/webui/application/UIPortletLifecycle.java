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

package org.exoplatform.portal.webui.application;

import java.io.Serializable;
import java.util.Map;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;

import org.exoplatform.Constants;
import org.exoplatform.commons.utils.Text;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.config.NoSuchDataException;
import org.exoplatform.portal.portlet.PortletExceptionHandleService;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.portal.webui.workspace.UIPortalApplication.EditMode;
import org.exoplatform.resolver.ApplicationResourceResolver;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.portletcontainer.PortletContainerException;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiApplication;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.Lifecycle;
import org.exoplatform.webui.core.lifecycle.WebuiBindingContext;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.exception.MessageException;

import org.gatein.pc.api.NoSuchPortletException;
import org.gatein.pc.api.invocation.RenderInvocation;
import org.gatein.pc.api.invocation.response.PortletInvocationResponse;

/**
 * Created by The eXo Platform SAS May 8, 2006
 */
public class UIPortletLifecycle<S, C extends Serializable, I> extends Lifecycle<UIPortlet<S, C>> {

    protected static Log log = ExoLogger.getLogger("portal:UIPortletLifecycle");

    /**
     * This processAction method associated with the portlet UI component does the following work:
     *
     * 1) If the current request is one that target the portal than an event targeting a Portal level ActionListener is sent.
     * This case happen when the incoming request contains the parameter PortalRequestContext.UI_COMPONENT_ACTION
     * (portal:action). When the event is broadcasted the methods is over 2) In other cases, the request targets the portlet
     * either to a) change the portlet mode b) change the window state c) make a processAction() or render() call to the portlet
     * container (Portlet API methods here) In those 3 cases, dedicated events are created and broadcasted and the portlet is
     * added in the list of components to update within the AJAX call
     */
    public void processAction(UIPortlet<S, C> uicomponent, WebuiRequestContext context) throws Exception {
        try {
            // The PortletMode and WindowState can change during a portlet invocation, so we need
            // to be able to compare the results before and after invoking the portlet to know if
            // we need to broadcast a change event or not.
            PortletMode currentPortletMode = uicomponent.getCurrentPortletMode();
            WindowState currentWindowState = uicomponent.getCurrentWindowState();

            String action = context.getRequestParameter(PortalRequestContext.UI_COMPONENT_ACTION);
            if (action != null) {
                Event<UIComponent> event = uicomponent.createEvent(action, Event.Phase.PROCESS, context);
                if (event != null)
                    event.broadcast();
                return;
            }

            String portletMode = context.getRequestParameter(org.exoplatform.portal.Constants.PORTAL_PORTLET_MODE);
            if (portletMode != null) {
                Event<UIComponent> event = uicomponent.createEvent("ChangePortletMode", Event.Phase.PROCESS, context);
                if (event != null)
                    event.broadcast();
            }

            String windowState = context.getRequestParameter(org.exoplatform.portal.Constants.PORTAL_WINDOW_STATE);
            if (windowState != null) {
                Event<UIComponent> event = uicomponent.createEvent("ChangeWindowState", Event.Phase.PROCESS, context);
                if (event != null)
                    event.broadcast();
            }

            /*
             * Check the type of the incoming request, can be either an ActionURL or a RenderURL one
             *
             * In case of a RenderURL, the parameter state map must be invalidated and this is done in the associated
             * ActionListener
             *
             * If no action type is specified we assume the default, which is to render
             */
            String portletActionType = context.getRequestParameter(Constants.TYPE_PARAMETER);
            if (portletActionType != null) {
                if (portletActionType.equals(Constants.PORTAL_PROCESS_ACTION)) {
                    Event<UIComponent> event = uicomponent.createEvent("ProcessAction", Event.Phase.PROCESS, context);
                    if (event != null)
                        event.broadcast();
                } else if (portletActionType.equals(Constants.PORTAL_SERVE_RESOURCE)) {
                    Event<UIComponent> event = uicomponent.createEvent("ServeResource", Event.Phase.PROCESS, context);
                    if (event != null)
                        event.broadcast();
                }
            } else {
                Event<UIComponent> event = uicomponent.createEvent("Render", Event.Phase.PROCESS, context);
                if (event != null)
                    event.broadcast();
            }

            // These two checks needs to go after the ProcessAction, ServeResource or Render broadcast events.
            // The mode or state can change during the invocation and we need to be able to broadcast the change
            // event if this occurs.
            if (currentPortletMode != null && !currentPortletMode.equals(uicomponent.getCurrentPortletMode())) {
                context.setAttribute(UIPortletActionListener.CHANGE_PORTLET_MODE_EVENT, uicomponent.getCurrentPortletMode()
                        .toString());
                Event<UIComponent> event = uicomponent.createEvent("ChangePortletMode", Event.Phase.PROCESS, context);
                if (event != null)
                    event.broadcast();
                context.setAttribute(UIPortletActionListener.CHANGE_PORTLET_MODE_EVENT, null);
            }
            if (currentWindowState != null && !currentWindowState.equals(uicomponent.getCurrentWindowState())) {
                context.setAttribute(UIPortletActionListener.CHANGE_WINDOW_STATE_EVENT, uicomponent.getCurrentWindowState()
                        .toString());
                Event<UIComponent> event = uicomponent.createEvent("ChangeWindowState", Event.Phase.PROCESS, context);
                if (event != null)
                    event.broadcast();
                context.setAttribute(UIPortletActionListener.CHANGE_WINDOW_STATE_EVENT, null);
            }

            context.addUIComponentToUpdateByAjax(uicomponent);
        } catch (NoSuchDataException e) {
            UIPortalApplication uiApp = Util.getUIPortalApplication();
            uiApp.refreshCachedUI();
            ApplicationMessage msg = new ApplicationMessage("UIPortlet.message.staleData", null,
                    ApplicationMessage.WARNING);
            uiApp.addMessage(msg);
        } catch (Exception e) {
            String message = e.getLocalizedMessage();
            log.error("Error processing the action: " + message, e);
            Object[] args = { message };
            context.addUIComponentToUpdateByAjax(uicomponent);
            throw new MessageException(new ApplicationMessage("UIPortletLifecycle.msg.process-error", args,
                    ApplicationMessage.ERROR));
        }

    }

    /**
     * This methods of the Lifecycle writes into the output writer the content of the portlet
     *
     * 1) Create a RenderInput object and fill it with all the Request information 2) Call the portletContainer.render() method
     * of the Portlet Container to get the HTML generated fragment 3) Then if the current request is an AJAX one, just write in
     * the buffer the content returned by the portlet container 4) If not AJAX, then merge the content with the UIPortlet.gtmpl
     */
    public void processRender(UIPortlet<S, C> uicomponent, WebuiRequestContext context) throws Exception {
        PortalRequestContext prcontext = (PortalRequestContext) context;
        ExoContainer container = prcontext.getApplication().getApplicationServiceContainer();

        //
        Text markup = null;

        try {
            Map<String, String[]> paramMap = prcontext.getRequest().getParameterMap();
            if (paramMap.containsKey("removePP")) {
                UIPortal uiPortal = Util.getUIPortal();
                for (String publicParamName : paramMap.get("removePP")) {
                    uiPortal.getPublicParameters().remove(publicParamName);
                }
            }

            RenderInvocation renderInvocation = uicomponent.create(RenderInvocation.class, prcontext);

            String appStatus = uicomponent.getProperties().get("appStatus");
            if ("Window".equals(uicomponent.getPortletStyle()) && !("SHOW".equals(appStatus) || "HIDE".equals(appStatus))) {
                markup = Text.create("<span></span>");
            } else {
                // Check mode of portal, portlet and permission for viewable
                if ((Util.getUIPortalApplication().getEditMode() != EditMode.BLOCK || uicomponent.getCurrentPortletMode()
                        .equals(PortletMode.EDIT)) && uicomponent.hasAccessPermission()) {
                    PortletInvocationResponse response = uicomponent.invoke(renderInvocation);
                    markup = uicomponent.generateRenderMarkup(response, prcontext);
                } else {
                    uicomponent.setConfiguredTitle(null);
                }
            }
        } catch (Exception e) {
            PortletContainerException pcException = new PortletContainerException(e);
            PortletExceptionHandleService portletExceptionService = uicomponent
                    .getApplicationComponent(PortletExceptionHandleService.class);
            if (portletExceptionService != null) {
                portletExceptionService.handle(pcException);
            }

            if (e instanceof NoSuchPortletException) {
                markup = Text.create(context.getApplicationResourceBundle().getString("UIPortlet.message.unavailable"));
            } else if (e instanceof NoSuchDataException) {
                UIPortalApplication uiApp = Util.getUIPortalApplication();
                uiApp.refreshCachedUI();
                markup = Text.create(context.getApplicationResourceBundle().getString("UIPortlet.message.staleData"));
            } else {
                // Log the error
                log.error("Portlet render threw an exception", pcException);

                //
                markup = Text.create(context.getApplicationResourceBundle().getString("UIPortlet.message.RuntimeError"));
            }
        }

        //
        if (context.useAjax() && !prcontext.getFullRender()) {
            if (markup != null) {
                markup.writeTo(prcontext.getWriter());
            }
        } else {
            WebuiApplication app = (WebuiApplication) prcontext.getApplication();
            ApplicationResourceResolver resolver = app.getResourceResolver();
            WebuiBindingContext bcontext = new WebuiBindingContext(resolver, context.getWriter(), uicomponent, prcontext);
            bcontext.put(UIComponent.UICOMPONENT, uicomponent);
            bcontext.put("portletContent", markup);
            try {
                renderTemplate(uicomponent.getTemplate(), bcontext);
            } catch (Throwable e) {
                log.error(e.getMessage(), e);
            }
        }
    }
}
