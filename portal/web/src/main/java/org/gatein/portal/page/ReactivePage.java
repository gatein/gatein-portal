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
package org.gatein.portal.page;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.ReentrantLock;

import juzu.PropertyMap;
import juzu.Response;
import juzu.io.AsyncStreamable;
import org.gatein.pc.api.invocation.response.FragmentResponse;
import org.gatein.pc.api.invocation.response.PortletInvocationResponse;
import org.gatein.portal.layout.Layout;

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
    private final ArrayList<ReactiveWindow> windows;

    /** The collected fragments. */
    private Map<String, Result> results;

    /** The streamable. */
    private AsyncStreamable streamable;

    /** . */
    private PropertyMap properties;

    /** . */
    private Layout siteLayout;

    /** . */
    private Layout pageLayout;

    /** . */
    private final ReentrantLock lock;

    ReactivePage(PageContext context, Locale locale) {

        ArrayList<ReactiveWindow> windows = new ArrayList<ReactiveWindow>();
        for (Map.Entry<String, WindowContext> entry : context) {
            ReactiveWindow window = new ReactiveWindow(entry.getValue(), results);
            windows.add(window);
        }

        //
        this.context = context;
        this.locale = locale;
        this.windows = windows;
        this.results = new HashMap<String, Result>();
        this.streamable = new AsyncStreamable();
        this.lock = new ReentrantLock();
    }

    /**
     * Create a response that renders the page with the specified layout.
     *
     * @param siteLayout the site layout
     * @param pageLayout the page layout
     * @param executor the executor
     * @return the response
     */
    Response execute(Layout siteLayout, Layout pageLayout, Executor executor) {

        //
        this.pageLayout = pageLayout;
        this.siteLayout = siteLayout;

        // Schedule
        for (ReactiveWindow window : windows) {
            executor.execute(window);
        }

        //
        return new Response.Status(200, properties).content(streamable);
    }

    private void done(ReactiveWindow window, Result result) {

        //
        boolean send;
        lock.lock();
        try {
            results.put(window.context.state.name, result);
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
        // Get all fragments
        HashMap<String, Result.Fragment> fragments = new HashMap<String, Result.Fragment>();
        for (Map.Entry<String, Result> entry : results.entrySet()) {
            Result result = entry.getValue();
            if (result instanceof Result.Fragment) {
                fragments.put(entry.getKey(), (Result.Fragment) result);
            }
        }
        try {
            StringBuilder body = new StringBuilder();
            pageLayout.render(fragments, null, context, properties, body);
            siteLayout.render(fragments, body.toString(), context, properties, streamable);
        } catch (IOException e) {
            // Could not render page
            // find something useful to do :-)
            e.printStackTrace();
        } finally {
            streamable.close();
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

        /** . */
        private final Map<String, Result> fragments;

        ReactiveWindow(WindowContext context, Map<String, Result> fragments) {

            // Do this now because later it can throw exceptions (capture context)
            RenderTask task = context.createRenderTask();

            //
            this.context = context;
            this.fragments = fragments;
            this.task = task;
        }

        @Override
        public void run() {
            Result result;
            task.run();
            if (task.failure != null) {
                task.failure.printStackTrace();
                result = new Result.Error(true, task.failure);
            } else {
                PortletInvocationResponse response = task.response;
                if (response instanceof FragmentResponse) {
                    FragmentResponse fragment = (FragmentResponse)response;
                    String title = fragment.getTitle();
                    if (title == null) {
                        title = context.resolveTitle(locale);
                    }
                    result = new Result.Fragment(title, fragment.getContent());
                } else {
                    throw new UnsupportedOperationException("Not yet handled " + response);
                }
            }
            done(this, result);
        }
    }
}
