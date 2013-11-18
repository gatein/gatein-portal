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
package org.gatein.portal.web.page;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.ReentrantLock;

import juzu.PropertyType;
import juzu.Response;
import juzu.io.Chunk;
import juzu.io.ChunkBuffer;
import juzu.request.RequestContext;
import org.gatein.portal.web.layout.Layout;
import org.gatein.portal.web.layout.RenderingContext;
import org.gatein.portal.web.content.RenderTask;
import org.w3c.dom.Element;

/**
 * A reactive page.
 *
 * @author Julien Viet
 */
class ReactivePage {

    /** . */
    private final PageContext context;

    /** . */
    private final Locale locale;

    /** . */
    private final RenderingContext renderingContext;

    /** . */
    private final ArrayList<ReactiveWindow> windows;

    /** The collected fragments. */
    private Map<String, Result> results;

    /** The streamable. */
    private ChunkBuffer buffer;

    /** . */
    private Layout siteLayout;

    /** . */
    private Layout pageLayout;

    /** . */
    private final ReentrantLock lock;

    ReactivePage(PageContext context, Locale locale, RenderingContext renderingContext) {

        ArrayList<ReactiveWindow> windows = new ArrayList<ReactiveWindow>();
        for (Map.Entry<String, WindowContext> entry : context) {
            ReactiveWindow window = new ReactiveWindow(entry.getValue());
            windows.add(window);
        }

        //
        Thread.UncaughtExceptionHandler logger = new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                e.printStackTrace();
            }
        };

        //
        this.context = context;
        this.locale = locale;
        this.windows = windows;
        this.results = new HashMap<String, Result>();
        this.buffer = new ChunkBuffer(logger);
        this.lock = new ReentrantLock();
        this.renderingContext = renderingContext;
    }

    /**
     * Create a response that renders the page with the specified layout.
     *
     * @param siteLayout the site layout
     * @param pageLayout the page layout
     * @param context the request context
     * @return the response
     */
    Response execute(
            Layout siteLayout,
            Layout pageLayout,
            RequestContext context) {

        //
        this.pageLayout = pageLayout;
        this.siteLayout = siteLayout;

        //
        Executor executor = context.getExecutor();

        // Schedule
        for (ReactiveWindow window : windows) {
            executor.execute(window);
        }

        //
        return new Response.Content(200, buffer);
    }

    private void done(ReactiveWindow window, Result result) {

        //
        boolean send;
        lock.lock();
        try {
            results.put(window.context.name, result);
            send = results.size() == windows.size();
        } finally {
            lock.unlock();
        }

        //
        if (send) {
            send();
        }
    }

    private void send() {

        //
        buffer.append(new Chunk.Property<String>("text/html", PropertyType.MIME_TYPE));

        // Get all fragments
        HashMap<String, Result.Fragment> fragments = new HashMap<String, Result.Fragment>();
        for (Map.Entry<String, Result> entry : results.entrySet()) {
            Result result = entry.getValue();
            if (result instanceof Result.Fragment) {
                fragments.put(entry.getKey(), (Result.Fragment) result);
            }
        }

        // Send headers
        for (Result.Fragment fragment : fragments.values()) {
            for (Map.Entry<String, String> header : fragment.headers) {
                buffer.append(new Chunk.Property(header, PropertyType.HEADER));
            }
        }

        // Send header tags
        for (Result.Fragment fragment : fragments.values()) {
            for (Element headerTag : fragment.headerTags) {
                buffer.append(new Chunk.Property(headerTag, PropertyType.HEADER_TAG));
            }
        }

        // Compute and send page
        try {
            StringBuilder body = new StringBuilder();
            pageLayout.render(renderingContext, fragments, null, context, body);
            siteLayout.render(renderingContext, fragments, body.toString(), context, buffer);
        } catch (IOException e) {
            // Could not render page
            // find something useful to do :-)
            e.printStackTrace();
        } finally {
            buffer.close();
        }
    }

    /**
     * @author Julien Viet
     */
    class ReactiveWindow implements Runnable {

        /** . */
        private final WindowContext context;

        /** . */
        private final RenderTask task;

        ReactiveWindow(WindowContext context) {

            // Do this now because later it can throw exceptions (capture context)
            RenderTask task = context.createRenderTask();

            //
            this.context = context;
            this.task = task;
        }

        @Override
        public void run() {
            Result result = task.execute(locale);
            ReactivePage.this.done(ReactiveWindow.this, result);
        }
    }
}
