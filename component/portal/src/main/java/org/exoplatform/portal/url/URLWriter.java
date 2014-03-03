/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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
package org.exoplatform.portal.url;

import java.io.IOException;

import javax.portlet.BaseURL;

import org.exoplatform.commons.utils.PropertyManager;
import org.apache.commons.io.output.StringBuilderWriter;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * @author <a href="mailto:lponce@redhat.com">Lucas Ponce</a>
 */
public class URLWriter {

    protected static Log log = ExoLogger.getLogger("portal:URLWriter");

    public static final String W3C_URL_ENCODED = "gatein.w3c.url.encoded";

    private static Boolean urlEncoded = null;
    private static final boolean ESCAPE_XML = true;

    public static boolean isUrlEncoded() {
        if (urlEncoded == null) {
            urlEncoded = Boolean.valueOf(PropertyManager.getProperty(W3C_URL_ENCODED));
        }
        return urlEncoded.booleanValue();
    }

    public static String toString(BaseURL url) {
        if (url == null) return null;
        if (isUrlEncoded()) {
            StringBuilderWriter urlXhtml = new StringBuilderWriter();
            try {
                url.write(urlXhtml, ESCAPE_XML);
                return urlXhtml.toString();
            } catch (IOException e) {
                StringBuilder msg = new StringBuilder().append("Error trying to escape BaseURL: ").append(url.toString())
                        .append(". Msg: ").append(e.toString());
                log.error(msg.toString(), e);
                // In case of error it returns a non escaped URL
                return url.toString();
            }
        } else {
            return url.toString();
        }
    }

}
