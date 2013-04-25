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
import java.lang.reflect.UndeclaredThrowableException;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.Map;

import juzu.impl.common.PercentCodec;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class Encoder {

    /** . */
    static final PercentCodec NAME_CODEC = PercentCodec.RFC3986_QUERY_PARAM_NAME;

    /** . */
    static final PercentCodec VALUE_CODEC = PercentCodec.create(
            PercentCodec.RFC3986_QUERY_PARAM_VALUE.
                    clearBit(':').
                    clearBit(',')
    );

    /** . */
    private final Iterable<Map.Entry<String, String[]>> parameters;

    public Encoder(Iterable<Map.Entry<String, String[]>> parameters) {
        this.parameters = parameters;
    }

    public Encoder(Map<String, String[]> parameters) {
        this.parameters = parameters.entrySet();
    }

    public String encode() {
        try {
            StringBuilder buffer = new StringBuilder();
            encode(buffer);
            return buffer.toString();
        } catch (IOException e) {
            throw new UndeclaredThrowableException(e);
        }
    }

    public void encode(Appendable appendable) throws IOException {
        boolean first = true;
        Iterator<Map.Entry<String, String[]>> i = parameters.iterator();
        if (i.hasNext()) {
            while (i.hasNext()) {
                Map.Entry<String, String[]> parameter = i.next();
                String[] value = parameter.getValue();
                if (value.length == 0) {
                    throw new AssertionError("wtf?");
                } else {
                    for (String s : value) {
                        if (!first) {
                            appendable.append(",");
                        } else {
                            first = false;
                        }
                        NAME_CODEC.encode(parameter.getKey(), appendable);
                        appendable.append(':');
                        VALUE_CODEC.encode(s, appendable);
                    }
                }
            }
        }
    }
}
