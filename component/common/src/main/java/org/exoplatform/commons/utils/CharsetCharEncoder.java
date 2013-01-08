/**
 * Copyright (C) 2009 eXo Platform SAS.
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

package org.exoplatform.commons.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;


/**
 * An implementation of a char encoder that is stateless and is based on a specified charset.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class CharsetCharEncoder implements CharEncoder {

    private static final CharEncoder UTF8 = new CharsetCharEncoder(Charset.forName("UTF8"));

    private static final byte[] EMPTY = new byte[0];

    public static CharEncoder getUTF8() {
        return UTF8;
    }

    private final Charset charset;

    private final java.nio.CharBuffer in;

    private final ByteBuffer out;

    private final byte[][] arrays;

    public CharsetCharEncoder(Charset charset) {
        this.charset = charset;
        this.in = CharBuffer.allocate(1);
        this.out = ByteBuffer.allocate(100);
        this.arrays = new byte[][] { new byte[0], new byte[1], new byte[2], new byte[3], new byte[4], new byte[5] };
    }

    public Charset getCharset() {
        return charset;
    }

    public byte[] encode(char c) {
        /*
         * switch (Character.getType(c)) { case Character.SURROGATE: case Character.PRIVATE_USE: return EMPTY; default: if
         * (encoder.canEncode(c)) { in.rewind(); out.rewind(); in.put(0, c); encoder.reset(); encoder.encode(in, out, true);
         * encoder.flush(out); int length = out.position(); byte[] bytes = arrays[length]; System.arraycopy(out.array(), 0,
         * bytes, 0, length); return bytes; } else { return EMPTY; } }
         */
        try {
            CharsetEncoder encoder = charset.newEncoder();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            OutputStreamWriter writer = new OutputStreamWriter(baos, encoder);
            writer.write(c);
            writer.close();
            return baos.toByteArray();
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

}
