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

import java.nio.charset.Charset;

/**
 * A char encoder that encodes chars to a suite of bytes. No assumptions must be made about the
 * statefullness nature of an encoder as some encoder may be statefull and some encoder may be stateless.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public interface CharEncoder
{

   /**
    * Encodes a single char into an array of bytes. The returned array of bytes should not be modified.
    *
    * @param c the char to encode
    * @return the serie of bytes corresponding to the encoded char
    */
   byte[] encode(char c);

   /**
    * Returns the charset that will perform the encoding.
    *
    * @return the charset for encoding
    */
   Charset getCharset();
}
