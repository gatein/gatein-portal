/*
 * Copyright (C) 2012 eXo Platform SAS.
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
package org.gatein.portal.appzu;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.xml.namespace.QName;

import juzu.Response;
import juzu.impl.common.JUL;
import juzu.impl.plugin.application.Application;
import juzu.impl.plugin.controller.ControllerPlugin;
import juzu.impl.request.ContextLifeCycle;
import juzu.impl.request.Method;
import juzu.impl.request.Request;
import juzu.request.Phase;
import juzu.request.ResponseParameter;
import org.gatein.portal.content.RenderTask;
import org.gatein.portal.content.Result;
import org.gatein.portal.content.WindowContent;
import org.gatein.portal.content.WindowContentContext;
import org.gatein.portal.web.page.Encoder;

/**
 * @author Julien Viet
 */
public class AppContent extends WindowContent<AppState> {

    /** . */
    final App app;

    /** . */
    private String parameters;

    public AppContent(App app) {
        this.app = app;
        this.parameters = null;
    }

    public AppContent(AppContent content) {
        this.app = content.app;
        this.parameters = content.parameters;
    }

    juzu.request.Result invoke(
            WindowContentContext<AppState> context,
            Phase phase,
            Map<String, String[]> parameters) throws Exception {
        ContextLifeCycle lifeCycle = Request.getCurrent().suspend();
        try {
            Application app = this.app.bridge.getApplication();
            ControllerPlugin controller = app.resolveBean(ControllerPlugin.class);
            RequestBridgeImpl requestBridge = new RequestBridgeImpl(
                    this,
                    context,
                    lifeCycle,
                    phase,
                    JUL.SYSTEM,
                    parameters);
            controller.invoke(requestBridge);
            return requestBridge.getResult();
        } finally {
            lifeCycle.resume();
        }
    }

    @Override
    public RenderTask createRender(WindowContentContext<AppState> window) {
        return new AppRenderTask(this, window);
    }

    @Override
    public Result.Action processAction(WindowContentContext<AppState> window, String windowState, String mode, Map<String, String[]> interactionState) {
        try {
            String parameters;
            juzu.request.Result result = invoke(window, Phase.ACTION, interactionState);
            if (result instanceof juzu.request.Result.View) {
                juzu.request.Result.View view = (juzu.request.Result.View) result;
                Phase.View.Dispatch update = (Phase.View.Dispatch)view.dispatch;
                HashMap<String, String[]> tmp;
                if (update.getParameters().size() > 0) {
                    tmp = new HashMap<String, String[]>();
                    for (ResponseParameter entry : update.getParameters().values()) {
                        tmp.put(entry.getName(), entry.toArray());
                    }
                } else {
                    tmp = null;
                }
                if (update.getTarget() != null) {
                    if (tmp == null) {
                        tmp = new HashMap<String, String[]>();
                    }
                    Method method = app.bridge.getApplication().resolveBean(ControllerPlugin.class).getDescriptor().getMethodByHandle(update.getTarget());
                    tmp.put("juzu.op", new String[]{method.getId()});
                }
                if (tmp != null) {
                    parameters = new Encoder(tmp).encode();
                } else {
                    parameters = null;
                }
            } else {
                // Not yet handled
                parameters = null;
            }
            return new Result.Update<AppState>(parameters, null, null, Collections.<String, String[]>emptyMap(), null);

        } catch (Exception e) {
            e.printStackTrace();
            return new Result.Error(true, e);
        }
    }

    @Override
    public Response serveResource(WindowContentContext<AppState> window, String id, Map<String, String[]> resourceState) {
        return null;
    }

    @Override
    public String resolveTitle(Locale locale) {
        return app.displayName;
    }

    @Override
    public String getParameters() {
        return parameters;
    }

    @Override
    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    @Override
    public boolean isSupportedWindowState(String ws) {
        return false;
    }

    @Override
    public String getWindowState() {
        return null;
    }

    @Override
    public void setWindowState(String ws) {
    }

    @Override
    public boolean isSupportedMode(String mode) {
        return false;
    }

    @Override
    public String getMode() {
        return null;
    }

    @Override
    public void setMode(String m) {
    }

    @Override
    public Map<String, String[]> computePublicParameters(Map<QName, String[]> parameters) {
        return Collections.emptyMap();
    }

    @Override
    public Iterable<Map.Entry<QName, String[]>> getPublicParametersChanges(Map<String, String[]> changes) {
        return Collections.<QName, String[]>emptyMap().entrySet();
    }

    @Override
    public WindowContent<AppState> copy() {
        return new AppContent(this);
    }
}
