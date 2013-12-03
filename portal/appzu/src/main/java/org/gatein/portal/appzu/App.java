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
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import juzu.impl.asset.AssetServer;
import juzu.impl.bridge.Bridge;
import juzu.impl.bridge.BridgeConfig;
import juzu.impl.bridge.BridgeContext;
import juzu.impl.bridge.module.ApplicationBridge;
import juzu.impl.bridge.module.ModuleContextImpl;
import juzu.impl.common.Completion;
import juzu.impl.common.JUL;
import juzu.impl.common.Logger;
import juzu.impl.common.Name;
import juzu.impl.fs.spi.ReadFileSystem;
import juzu.impl.inject.spi.Injector;
import juzu.impl.inject.spi.InjectorProvider;
import juzu.impl.resource.ClassLoaderResolver;
import juzu.impl.resource.ResourceResolver;
import org.gatein.portal.common.kernel.ThreadContext;

/**
 * @author Julien Viet
 */
public class App {

    /** . */
    final Name name;

    /** . */
    Bridge bridge;

    /**
     * The current app status:
     * - null : unitilizaled
     * - true : running
     * - false : compilation failed
     */
    private Completion<Boolean> status;

    public App(Name name, final ReadFileSystem<?> fs) throws Exception {


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
                return Thread.currentThread().getContextClassLoader();
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

        //
        this.bridge = new ApplicationBridge(module, context, config, new AssetServer(), resolver, injector);
        this.name = name;
    }

    public Name getName() {
        return name;
    }

    public String getWebdavURL() {
        HttpServletRequest req = ThreadContext.getCurentHttpServletRequest();
        return req.getScheme() + "://" + req.getServerName() + ":" + req.getServerPort() +
                req.getContextPath() + "/repository" + "/" + name;
    }

    public Completion<Boolean> getStatus() {
        return status;
    }

    public String getStatusMessage() {
        if (status == null) {
            return "stopped";
        } else if (status.isFailed()) {
            return "failed";
        } else {
            return "started";
        }
    }

    public Completion<Boolean> refresh() throws Exception {
        status = bridge.refresh();
        if (status.isFailed()) {
            status.getCause().printStackTrace();
        }
        return status;
    }
}
