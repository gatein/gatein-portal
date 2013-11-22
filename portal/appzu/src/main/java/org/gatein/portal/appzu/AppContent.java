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

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.xml.namespace.QName;

import juzu.Response;
import juzu.impl.asset.AssetServer;
import juzu.impl.bridge.Bridge;
import juzu.impl.bridge.BridgeConfig;
import juzu.impl.bridge.BridgeContext;
import juzu.impl.bridge.module.ApplicationBridge;
import juzu.impl.bridge.module.ModuleContextImpl;
import juzu.impl.common.Completion;
import juzu.impl.common.Content;
import juzu.impl.common.JUL;
import juzu.impl.common.Logger;
import juzu.impl.common.Name;
import juzu.impl.common.Tools;
import juzu.impl.fs.Visitor;
import juzu.impl.fs.spi.ReadFileSystem;
import juzu.impl.fs.spi.ram.RAMFileSystem;
import juzu.impl.inject.spi.Injector;
import juzu.impl.inject.spi.InjectorProvider;
import juzu.impl.resource.ClassLoaderResolver;
import juzu.impl.resource.ResourceResolver;
import juzu.impl.runtime.ApplicationRuntime;
import juzu.impl.runtime.ModuleRuntime;
import org.gatein.portal.content.RenderTask;
import org.gatein.portal.content.Result;
import org.gatein.portal.content.WindowContent;
import org.gatein.portal.content.WindowContentContext;

/**
 * @author Julien Viet
 */
public class AppContent extends WindowContent<App> {

    /** . */
    final Name name;

    /** . */
    final RAMFileSystem fs;

    /** . */
    Bridge bridge;

    public AppContent(Name name) throws IOException {

        //
        RAMFileSystem fs = new RAMFileSystem();
        String[] root = fs.makePath(name);
        fs.createDir(root);
        fs.setContent(Tools.appendTo(root, "package-info.java"), new Content("@Application\npackage " + name + ";\n\nimport juzu.Application;\n"));
        fs.setContent(Tools.appendTo(root, "Controller.java"), new Content(
                "package " + name + ";\n" +
                "import juzu.View;\n" +
                "import juzu.Response;\n" +
                "\n" +
                "public class Controller {\n" +
                "\n" +
                "public @View Response index() {\n" +
                "return Response.ok(\"Hello World\");\n" +
                "}\n" +
                "}\n"));

        //
        this.name = name;
        this.fs = fs;
    }

    public Name getName() {
        return name;
    }

    public void start() throws Exception {
        BridgeContext context = new BridgeContext() {
            @Override
            public Logger getLogger(String name) {
                return JUL.SYSTEM;
            }
            @Override
            public ReadFileSystem<?> getClassPath() {
                return null;
            }
            @Override
            public ReadFileSystem<?> getSourcePath() {
                return fs;
            }
            @Override
            public ReadFileSystem<?> getResourcePath() {
                return null;
            }
            @Override
            public ClassLoader getClassLoader() {
                return null;
            }
            @Override
            public String getInitParameter(String name) {
                if ("juzu.run_mode".equals(name)) {
                    return "live";
                } else {
                    return null;
                }
            }
            @Override
            public ResourceResolver getResolver() {
                return null;
            }
            @Override
            public Object getAttribute(String key) {
                return null;
            }
            @Override
            public void setAttribute(String key, Object value) {

            }
        };

        ResourceResolver resolver = new ClassLoaderResolver(Thread.currentThread().getContextClassLoader());
        Injector injector = InjectorProvider.GUICE.get();
        ModuleContextImpl module = new ModuleContextImpl(JUL.SYSTEM, context, resolver);
        HashMap<String, String> a = new HashMap<String, String>();
        a.put(BridgeConfig.APP_NAME, name.toString());
        a.put(BridgeConfig.INJECT, "guice");
        BridgeConfig config = new BridgeConfig(JUL.SYSTEM, a);
        Bridge bridge = new ApplicationBridge(module, context, config, new AssetServer(), resolver, injector);
        Completion<Boolean> completion = bridge.refresh();

        //
        if (completion.isFailed()) {
            completion.getCause().printStackTrace();
        }

        //
        this.bridge = bridge;
    }

    @Override
    public RenderTask createRender(WindowContentContext<App> window) {
        return new AppRenderTask(this);
    }

    @Override
    public Result processAction(WindowContentContext<App> window, String windowState, String mode, Map<String, String[]> interactionState) {
        return null;
    }

    @Override
    public Response serveResource(WindowContentContext<App> window, String id, Map<String, String[]> resourceState) {
        return null;
    }

    @Override
    public String resolveTitle(Locale locale) {
        return null;
    }

    @Override
    public String getParameters() {
        return null;
    }

    @Override
    public void setParameters(String s) {
      
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
        return null;
    }

    @Override
    public Iterable<Map.Entry<QName, String[]>> getPublicParametersChanges(Map<String, String[]> changes) {
        return null;
    }

    @Override
    public WindowContent<App> copy() {
        return null;
    }
}
