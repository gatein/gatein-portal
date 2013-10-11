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
package org.gatein.portal.web.servlet;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Julien Viet
 */
class Wrapper implements Runnable {

    /** . */
    final AsyncContext context;

    /** . */
    final Runnable delegate;

    Wrapper(AsyncContext context, Runnable delegate) {
        this.context = context;
        this.delegate = delegate;
    }

    @Override
    public void run() {

        // Those should be null
        HttpServletRequest previousReq = Context.currentRequest.get();
        HttpServletResponse previousResp = Context.currentResponse.get();
        HttpServletRequest nextReq = (HttpServletRequest) context.getRequest();
        HttpServletResponse nextResp = (HttpServletResponse) context.getResponse();
        Context.currentRequest.set(nextReq);
        Context.currentResponse.set(nextResp);

        //
        try {
            delegate.run();
        } finally {
            Context.currentRequest.set(previousReq);
            Context.currentResponse.set(previousResp);
        }
    }
}
