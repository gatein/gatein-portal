/*
 * Copyright (C) 2011 eXo Platform SAS.
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

package org.exoplatform.web.controller.router;

import org.exoplatform.commons.utils.CharEncoder;
import org.exoplatform.commons.utils.CharsetCharEncoder;
import org.gatein.common.io.UndeclaredIOException;

import java.io.IOException;
import java.util.BitSet;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public final class PercentEncoding {

    /** . */
    private static final CharEncoder encoder = CharsetCharEncoder.getUTF8();

    /** . */
    private static final char[] ALPHABET = "0123456789ABCDEF".toCharArray();

    /** Path segment. */
    public static final PercentEncoding PATH_SEGMENT;

    static {
        BitSet allowed = new BitSet();

        // Unreserved
        for (char c = 'A'; c <= 'Z'; c++) {
            allowed.set(c);
        }
        for (char c = 'a'; c <= 'z'; c++) {
            allowed.set(c);
        }
        for (char c = '0'; c <= '9'; c++) {
            allowed.set(c);
        }
        allowed.set('_');
        allowed.set('.');
        allowed.set('-');
        allowed.set('~');

        // sub-delims
        allowed.set('!');
        allowed.set('$');
        allowed.set('&');
        allowed.set('\'');
        allowed.set('(');
        allowed.set(')');
        allowed.set('*');
        allowed.set('+');
        allowed.set(',');
        allowed.set(';');
        allowed.set('=');

        // ':' | '@'
        allowed.set(':');
        allowed.set('@');

        //
        PATH_SEGMENT = new PercentEncoding(allowed);
    }

    /** Query params name or value. */
    public static final PercentEncoding QUERY_PARAM;

    static {
        BitSet allowed = new BitSet(128);
        for (char c = 'A'; c <= 'Z'; c++) {
            allowed.set(c);
        }
        for (char c = 'a'; c <= 'z'; c++) {
            allowed.set(c);
        }
        for (char c = '0'; c <= '9'; c++) {
            allowed.set(c);
        }
        allowed.set('_');
        allowed.set('.');
        allowed.set('-');
        allowed.set('~');

        // sub-delims without ( '&' | '=' )
        allowed.set('!');
        allowed.set('$');
        allowed.set('\'');
        allowed.set('(');
        allowed.set(')');
        allowed.set('*');
        allowed.set('+');
        allowed.set(',');
        allowed.set(';');

        // ':' | '@'
        allowed.set(':');
        allowed.set('@');

        // '?' | '/'
        allowed.set('?');
        allowed.set('/');

        //
        QUERY_PARAM = new PercentEncoding(allowed);
    }

    /** . */
    private final BitSet allowed;
    private final int length;

    private PercentEncoding(BitSet allowed) {
        this.allowed = allowed;
        this.length = allowed.length();
    }

    boolean accept(char c) {
        return c < length && allowed.get(c);
    }

    public void encode(CharSequence s, Appendable appendable) throws IOException {
        for (int len = s.length(), i = 0; i < len; i++) {
            char c = s.charAt(i);
            encode(c, appendable);
        }
    }

    public String encode(CharSequence s) {
        try {
            StringBuilder sb = new StringBuilder(s.length());
            encode(s, sb);
            return sb.toString();
        } catch (IOException e) {
            throw new UndeclaredIOException(e);
        }
    }

    public void encode(char c, Appendable appendable) throws IOException {
        if (accept(c)) {
            appendable.append(c);
        } else {
            byte[] bytes = encoder.encode(c);
            for (byte b : bytes) {
                appendable.append('%');
                appendable.append(ALPHABET[(b & 0xF0) >> 4]);
                appendable.append(ALPHABET[b & 0xF]);
            }
        }
    }
}
