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

package org.gatein.portal.common.kernel;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.RootContainer;
import org.exoplatform.container.component.RequestLifeCycle;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class KernelLifeCycle implements Filter {

    /** . */
    private final AtomicReference<PortalContainer> container = new AtomicReference<PortalContainer>();

    /** . */
    private static final ThreadLocal<PortalContainer> current = new ThreadLocal<PortalContainer>();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        RootContainer.PortalContainerPostCreateTask task = new RootContainer.PortalContainerPostCreateTask() {
            @Override
            public void execute(ServletContext context, PortalContainer portalContainer) {
                KernelLifeCycle.this.container.set(portalContainer);
            }
        };

        // Init portal container
        RootContainer root = RootContainer.getInstance();
        ServletContext context = filterConfig.getServletContext();
        root.addInitTask(context, task);
        root.registerPortalContainer(context);

        //
        PortalContainer container = this.container.get();
        if (container == null) {
            throw new ServletException("Container could not be created");
        } else if (!container.isStarted()) {
            throw new ServletException("Container could not be started");
        }

    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
        PortalContainer c = container.get();
        try {
            before(c);
            chain.doFilter(req,  resp);
        } finally {
            after();
        }
    }

    private static void before(PortalContainer c) {
        current.set(c);
        RequestLifeCycle.begin(c);
    }

    private static void after() {
        try {
            RequestLifeCycle.end();
        } finally {
            // Do it anyway
            current.set(null);
        }
    }

    @Override
    public void destroy() {
        container.get().dispose();
    }

    public static Runnable wrap(final Runnable runnable) {
        final PortalContainer c = current.get();
        return new Runnable() {
            @Override
            public void run() {
                try {
                    before(c);
                    runnable.run();
                } finally {
                    after();
                }
            }
        };
    }
}
