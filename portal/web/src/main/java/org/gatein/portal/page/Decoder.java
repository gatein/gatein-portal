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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import juzu.impl.common.Tools;

/**
 * Decode parameters.
 *
 * T -> EMPTYABLE_LIST
 * EMPTYABLE_LIST ->
 * EMPTYABLE_LIST -> LIST
 * LIST -> PARAM
 * LIST -> PARAM "," LIST
 * PARAM -> QUOTED_KEY
 * PARAM -> QUOTED_KEY ":"
 * PARAM -> QUOTED_KEY ":" VALUE
 * QUOTED_KEY -> KEY
 * QUOTED_KEY -> "'" KEY "'"
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class Decoder {

    /** . */
    static final String MISSING_KEY_CODE = "Missing key";

    /** . */
    private final String s;

    /** . */
    private int length;

    /** . */
    private int ptr;

    /** . */
    private String error;

    /** . */
    private Map<String, String[]> parameters;

    public Decoder(String s) {
        this.s = s;
        this.length = s.length();
        this.ptr = 0;
        this.parameters = null;
        this.error = null;
    }

    public Decoder decode() {
        if (parameters == null) {
            try {
                decodeParameters();
            } catch (IllegalStateException e) {
                error = e.getMessage();
            }
            if (parameters == null) {
                parameters = Collections.emptyMap();
            }
        }
        return this;
    }

    public Map<String, String[]> getParameters() {
        return parameters;
    }

    public String getError() {
        return error;
    }

    private void decodeParameters() {
        while (ptr < length) {
            decodeParameter();
        }
    }

    /**
     * Decode a single parameter, pre condition there is one char read.
     */
    private void decodeParameter() {
        for (final int from = ptr;true;) {
            char c = s.charAt(ptr);
            if (c == ':') {
                if (from == ptr) {
                    throw new IllegalStateException(MISSING_KEY_CODE);
                } else {
                    put(s.substring(from, ptr++), parseValue());
                    break;
                }
            } else if (c == ',') {
                if (from == ptr) {
                    ptr++;
                } else {
                    put(s.substring(from, ptr++), "");
                }
                break;
            } else {
                if (++ptr >= length) {
                    put(s.substring(from, ptr), parseValue());
                    break;
                }
            }
        }
    }

    private String parseValue() {
        if (ptr < length) {
            String value;
            for (final int from = ptr;true;) {
                char c = s.charAt(ptr);
                if (c == ',') {
                    if (from < ptr) {
                        value = s.substring(from, ptr);
                    } else {
                        value = "";
                    }
                    ptr++;
                    break;
                } else {
                    if (++ptr < length) {
                        // Continue
                    } else {
                        value = s.substring(from, ptr);
                        break;
                    }
                }
            }
            return value;
        } else {
            return "";
        }
    }

    private void put(String rawName, String rawValue) {
        if (parameters == null) {
            parameters = new HashMap<String, String[]>();
        }
        String name = Encoder.NAME_CODEC.decode(rawName);
        String value = Encoder.VALUE_CODEC.decode(rawValue);
        String[] parameter = parameters.get(rawName);
        if (parameter != null) {
            parameter = Tools.appendTo(parameter, value);
        } else {
            parameter = new String[]{value};
        }
        parameters.put(name, parameter);
    }


}
