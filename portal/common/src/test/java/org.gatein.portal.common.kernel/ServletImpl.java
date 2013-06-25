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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.exoplatform.container.PortalContainer;

/**
 * @author Julien Viet
 */
public class ServletImpl extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        KernelLifeCycleTestCase.container1 = PortalContainer.getInstance();
        final CountDownLatch latch = new CountDownLatch(1);
        Runnable task = KernelLifeCycle.wrap(new Runnable() {
            @Override
            public void run() {
                KernelLifeCycleTestCase.container2 = PortalContainer.getInstance();
                latch.countDown();
            }
        });
        new Thread(task).start();
        try {
            latch.await(10, TimeUnit.SECONDS);
            resp.setStatus(200);
            resp.setContentType("text/plain");
            resp.getWriter().append("done").close();
        } catch (InterruptedException e) {
            resp.setStatus(500);
        }
    }
}
