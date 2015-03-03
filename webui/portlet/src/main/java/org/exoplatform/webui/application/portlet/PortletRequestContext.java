/*
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

package org.exoplatform.webui.application.portlet;

import java.io.Writer;

import javax.portlet.ActionResponse;
import javax.portlet.MimeResponse;
import javax.portlet.PortletMode;
import javax.portlet.PortletModeException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.StateAwareResponse;

import org.exoplatform.commons.utils.WriterPrinter;
import org.exoplatform.portal.mop.user.UserPortal;
import org.exoplatform.services.resources.Orientation;
import org.exoplatform.web.application.RequestContext;
import org.exoplatform.web.application.URLBuilder;
import org.exoplatform.web.url.PortalURL;
import org.exoplatform.web.url.ResourceType;
import org.exoplatform.web.url.URLFactory;
import org.exoplatform.webui.application.WebuiApplication;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;

/**
 * todo (julien) : there is an issue here (small) as the PRC seems to be stored in http session and keep a pointer on request
 * and response object.
 *
 * The request context of a portlet
 *
 */
public class PortletRequestContext extends WebuiRequestContext {
    /**
     * Portlet Window ID
     */
    private String windowId_;

    /**
     * The request
     */
    private PortletRequest request_;

    /**
     * The response
     */
    private PortletResponse response_;

    private Writer writer_;

    private boolean isAppLifecycleStarted = false;

    /** . */
    private PortletURLBuilder urlBuilder;

    public PortletRequestContext(RequestContext parentAppRequestContext, WebuiApplication app, Writer writer,
            PortletRequest req, PortletResponse res) {
        super(parentAppRequestContext, app);
        init(writer, req, res);
        setSessionId(req.getPortletSession(true).getId());
    }

    @Override
    public <R, U extends PortalURL<R, U>> U newURL(ResourceType<R, U> resourceType, URLFactory urlFactory) {
        return parentAppRequestContext_.newURL(resourceType, urlFactory);
    }

    public void init(Writer writer, PortletRequest req, PortletResponse res) {
        request_ = req;
        response_ = res;
        writer_ = new WriterPrinter(writer);
        windowId_ = req.getWindowID();

        //
        if (res instanceof MimeResponse) {
            this.urlBuilder = new PortletURLBuilder(((MimeResponse) res).createActionURL());
        } else {
            this.urlBuilder = null;
        }
    }

    public void setUIApplication(UIApplication uiApplication) throws Exception {
        uiApplication_ = uiApplication;
        appRes_ = getApplication().getResourceBundle(getParentAppRequestContext().getLocale());
    }

    public final String getRequestParameter(String name) {
        return request_.getParameter(name);
    }

    public final String[] getRequestParameterValues(String name) {
        return request_.getParameterValues(name);
    }

    public Orientation getOrientation() {
        return parentAppRequestContext_.getOrientation();
    }

    public String getRequestContextPath() {
        return request_.getContextPath();
    }

    @Override
    public String getPortalContextPath() {
        if (parentAppRequestContext_ instanceof WebuiRequestContext) {
            return ((WebuiRequestContext) parentAppRequestContext_).getPortalContextPath();
        } else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public PortletRequest getRequest() {
        return request_;
    }

    @SuppressWarnings("unchecked")
    public PortletResponse getResponse() {
        return response_;
    }

    @Override
    public URLFactory getURLFactory() {
        return parentAppRequestContext_.getURLFactory();
    }

    public String getRemoteUser() {
        return parentAppRequestContext_.getRemoteUser();
    }

    public final boolean isUserInRole(String roleUser) {
        return request_.isUserInRole(roleUser);
    }

    public PortletMode getApplicationMode() {
        return request_.getPortletMode();
    }

    public void setApplicationMode(PortletMode mode) throws PortletModeException {
        if (response_ instanceof StateAwareResponse) {
            StateAwareResponse res = (StateAwareResponse) response_;
            res.setPortletMode(mode);
        } else {
            throw new PortletModeException("The portlet don't support to set a portlet mode by current runtime environment",
                    mode);
        }
    }

    public Writer getWriter() throws Exception {
        return writer_;
    }

    public void setWriter(Writer writer) {
        this.writer_ = writer;
    }

    public final boolean useAjax() {
        return getParentAppRequestContext().useAjax();
    }

    public void sendRedirect(String url) throws Exception {
        setResponseComplete(true);
        if (response_ instanceof ActionResponse)
            ((ActionResponse) response_).sendRedirect(url);
    }

    @Override
    public UserPortal getUserPortal() {
        return getParentAppRequestContext().getUserPortal();
    }

    public boolean isAppLifecycleStarted() {
        return isAppLifecycleStarted;
    }

    public void setAppLifecycleStarted(boolean b) {
        isAppLifecycleStarted = b;
    }

    public URLBuilder<UIComponent> getURLBuilder() {
        if (urlBuilder == null) {
            throw new IllegalStateException("Cannot create portlet URL during action/event phase");
        }
        return urlBuilder;
    }

    /**
     * Puts the component to update inside the parent request context
     *
     * Here it will be the PortalRequestHandler and hence it will be responsible of making the distinction between 3rd parties
     * portlets (that need a full portlet fragment refresh) and our portlets that also allow some UIComponent within the portlet
     * to be refreshed
     */
    // public void addUIComponentToUpdateByAjax(UIComponent uicomponent) {
    // ((WebuiRequestContext)getParentAppRequestContext()).addUIComponentToUpdateByAjax(uicomponent);
    // }
    //
    // public List<UIComponent> getUIComponentToUpdateByAjax() {
    // return ((WebuiRequestContext)getParentAppRequestContext()).getUIComponentToUpdateByAjax() ;
    // }

    public String getWindowId() {
        return windowId_;
    }
}
