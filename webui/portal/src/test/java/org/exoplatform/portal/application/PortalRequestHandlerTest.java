/*
 * JBoss, a division of Red Hat
 * Copyright 2014, Red Hat Middleware, LLC, and individual
 * contributors as indicated by the @authors tag. See the
 * copyright.txt in the distribution for a full listing of
 * individual contributors.
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
package org.exoplatform.portal.application;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author Juraci Paixão Kröhling <juraci at kroehling.de>
 */
public class PortalRequestHandlerTest {

    @Test
    public void testDoNotUrlEncodeCacheControl() {
        PortalRequestHandler handler = new PortalRequestHandler();
        String cacheControl = "no-cache, max-age=0, must-revalidate, no-store";
        assertEquals(cacheControl, handler.getSanitizedCacheControl(cacheControl));
    }

    @Test
    public void testPreventResponseSplitting() {
        // reference: https://www.owasp.org/index.php/HTTP_Response_Splitting
        PortalRequestHandler handler = new PortalRequestHandler();
        String expected = "Wiley HackerHTTP/1.1 200 OK";

        String cacheControl = "Wiley Hacker\r\n\r\nHTTP/1.1 200 OK\r\n";
        assertEquals(expected, handler.getSanitizedCacheControl(cacheControl));

        cacheControl = "Wiley Hacker\r\rHTTP/1.1 200 OK\r";
        assertEquals(expected, handler.getSanitizedCacheControl(cacheControl));

        cacheControl = "Wiley Hacker\rHTTP/1.1 200 OK\r";
        assertEquals(expected, handler.getSanitizedCacheControl(cacheControl));

        cacheControl = "Wiley Hacker\nHTTP/1.1 200 OK\n";
        assertEquals(expected, handler.getSanitizedCacheControl(cacheControl));
    }
}
