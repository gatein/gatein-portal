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

import java.io.StringWriter;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;

import juzu.impl.bridge.spi.RequestBridge;
import juzu.impl.common.JUL;
import juzu.impl.common.Tools;
import juzu.impl.plugin.application.Application;
import juzu.impl.plugin.controller.ControllerPlugin;
import juzu.io.OutputStream;
import juzu.request.Phase;
import org.gatein.portal.appzu.bridge.RequestBridgeImpl;
import org.gatein.portal.content.RenderTask;
import org.gatein.portal.content.Result;
import org.w3c.dom.Element;

/**
 * @author Julien Viet
 */
public class AppRenderTask extends RenderTask {

    /** . */
    final AppContent content;

    public AppRenderTask(AppContent content) {
        this.content = content;
    }

    @Override
    public Result execute(Locale locale) {
        try {
            Application app = content.bridge.getApplication();
            ControllerPlugin controller = app.resolveBean(ControllerPlugin.class);
            RequestBridgeImpl requestBridge = new RequestBridgeImpl(content.bridge, Phase.VIEW, JUL.SYSTEM, Collections.<String, String[]>emptyMap());
            controller.invoke(requestBridge);
            juzu.request.Result result = requestBridge.getResult();
            if (result instanceof juzu.request.Result.Status) {
                juzu.request.Result.Status status = (juzu.request.Result.Status) result;
                if (status.streamable != null) {
                    StringWriter buffer = new StringWriter();
                    OutputStream stream = OutputStream.create(Tools.UTF_8, buffer);
                    status.streamable.send(stream);
                    return new Result.Fragment(Collections.<Map.Entry<String, String>>emptyList(), Collections.<Element>emptyList(), "The app", buffer.toString());
                }
            }
            return new Result.Fragment(Collections.<Map.Entry<String, String>>emptyList(), Collections.<Element>emptyList(), "The app", "The content of " + app);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result.Error(true, e);
        }
    }
}
