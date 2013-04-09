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

package org.gatein.portal.portlet;

import java.io.IOException;
import java.io.Writer;

import org.gatein.common.net.media.MediaType;
import org.gatein.pc.api.ContainerURL;
import org.gatein.pc.api.URLFormat;
import org.gatein.pc.api.spi.PortletInvocationContext;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
class GateInPortletInvocationContext implements PortletInvocationContext {

    @Override
    public MediaType getResponseContentType() {
        return MediaType.TEXT_HTML;
    }

    @Override
    public String encodeResourceURL(String url) throws IllegalArgumentException {
        throw new UnsupportedOperationException("todo");
    }

    @Override
    public String renderURL(ContainerURL containerURL, URLFormat format) {
        throw new UnsupportedOperationException("todo");
    }

    @Override
    public void renderURL(Writer writer, ContainerURL containerURL, URLFormat format) throws IOException {
        throw new UnsupportedOperationException("todo");
    }
}
