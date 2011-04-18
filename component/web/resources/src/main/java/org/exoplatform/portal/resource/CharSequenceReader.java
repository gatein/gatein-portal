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
package org.exoplatform.portal.resource;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */

import java.io.Reader;
import java.io.Serializable;

/**
 * Fork of commons-io 1.4 org.apache.commons.io.input.CharSequenceReader
 *
 * {@link Reader} implementation that can read from String, StringBuffer,
 * StringBuilder or CharBuffer.
 * <p>
 * <strong>Note:</strong> Supports {@link #mark(int)} and {@link #reset()}.
 *
 * @version $Revision$ $Date$
 * @since Commons IO 1.4
 */
public class CharSequenceReader extends Reader implements Serializable
{

    private final CharSequence charSequence;
    private int idx;
    private int mark;

    /**
     * Construct a new instance with the specified character sequence.
     *
     * @param charSequence The character sequence, may be <code>null</code>
     */
    public CharSequenceReader(CharSequence charSequence) {
        this.charSequence = (charSequence != null ? charSequence : "");
    }

    /**
     * Close resets the file back to the start and removes any marked position.
     */
    public void close() {
        idx = 0;
        mark = 0;
    }

    /**
     * Mark the current position.
     *
     * @param readAheadLimit ignored
     */
    public void mark(int readAheadLimit) {
        mark = idx;
    }

    /**
     * Mark is supported (returns true).
     *
     * @return <code>true</code>
     */
    public boolean markSupported() {
        return true;
    }

    /**
     * Read a single character.
     *
     * @return the next character from the character sequence
     * or -1 if the end has been reached.
     */
    public int read() {
        if (idx >= charSequence.length()) {
            return -1;
        } else {
            return charSequence.charAt(idx++);
        }
    }

    /**
     * Read the sepcified number of characters into the array.
     *
     * @param array The array to store the characters in
     * @param offset The starting position in the array to store
     * @param length The maximum number of characters to read
     * @return The number of characters read or -1 if there are
     * no more
     */
    public int read(char[] array, int offset, int length) {
        if (idx >= charSequence.length()) {
            return -1;
        }
        if (array == null) {
            throw new NullPointerException("Character array is missing");
        }
        if (length < 0 || (offset + length) > array.length) {
            throw new IndexOutOfBoundsException("Array Size=" + array.length +
                    ", offset=" + offset + ", length=" + length);
        }
        int count = 0;
        for (int i = 0; i < length; i++) {
            int c = read();
            if (c == -1) {
                return count;
            }
            array[offset + i] = (char)c;
            count++;
        }
        return count;
    }

    /**
     * Reset the reader to the last marked position (or the beginning if
     * mark has not been called).
     */
    public void reset() {
        idx = mark;
    }

    /**
     * Skip the specified number of characters.
     *
     * @param n The number of characters to skip
     * @return The actual number of characters skipped
     */
    public long skip(long n) {
        if (n < 0) {
            throw new IllegalArgumentException(
                    "Number of characters to skip is less than zero: " + n);
        }
        if (idx >= charSequence.length()) {
            return -1;
        }
        int dest = (int)Math.min(charSequence.length(), (idx + n));
        int count = dest - idx;
        idx = dest;
        return count;
    }

    /**
     * Return a String representation of the underlying
     * character sequence.
     *
     * @return The contents of the character sequence
     */
    public String toString() {
        return charSequence.toString();
    }
}