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

package org.exoplatform.portal.webui.application;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.gatein.pc.portlet.impl.spi.AbstractServerContext;
import org.gatein.wci.RequestDispatchCallback;
import org.gatein.wci.ServletContainer;
import org.gatein.wci.ServletContainerFactory;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ExoServerContext extends AbstractServerContext {

    public ExoServerContext(HttpServletRequest clientRequest, HttpServletResponse clientResponse) {
        super(clientRequest, clientResponse);
    }

    @Override
    public void dispatch(ServletContext target, HttpServletRequest request, HttpServletResponse response,
            final Callable callable) throws Exception {
        ServletContainer container = ServletContainerFactory.getServletContainer();
        container.include(target, request, response, new RequestDispatchCallback() {
            @Override
            public Object doCallback(ServletContext dispatchedServletContext, HttpServletRequest dispatchedRequest,
                    HttpServletResponse dispatchedResponse, Object handback) throws ServletException, IOException {
                callable.call(dispatchedServletContext, dispatchedRequest, dispatchedResponse);

                // We don't use return value anymore
                return null;
            }
        }, null);
    }
}
