/*
 * Copyright (C) 2011 eXo Platform SAS.
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

package org.exoplatform.component.test.web;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpSession;

import org.gatein.common.NotYetImplemented;
import org.gatein.wci.WebApp;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class WebAppImpl implements WebApp {

    /** . */
    private final ServletContext servletContext;

    /** . */
    private final ClassLoader loader;

    public WebAppImpl(ServletContext servletContext, ClassLoader loader) throws NullPointerException {
        if (servletContext == null) {
            throw new NullPointerException("No null servlet context allowed");
        }
        if (loader == null) {
            throw new NullPointerException("No null loader accepted");
        }

        //
        this.servletContext = servletContext;
        this.loader = loader;
    }

    public WebAppImpl(Class<?> base, String path, String name) {
        this.servletContext = new ServletContextImpl(base, path, name);
        this.loader = base.getClassLoader();
    }

    public ServletContext getServletContext() {
        return servletContext;
    }

    public ClassLoader getClassLoader() {
        return loader;
    }

    public String getContextPath() {
        return servletContext.getContextPath();
    }

    public boolean importFile(String parentDirRelativePath, String name, InputStream source, boolean overwrite)
            throws IOException {
        throw new NotYetImplemented();
    }

    @Override
    public HttpSession getHttpSession(String sessId) {
        throw new NotYetImplemented();
    }

    @Override
    public void fireRequestDestroyed(ServletRequest servletRequest) {
        //Do Nothing
    }

    @Override
    public void fireRequestInitialized(ServletRequest servletRequest) {
        //Do Nothing
    }
}
