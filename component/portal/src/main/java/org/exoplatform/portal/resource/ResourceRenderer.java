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

import org.exoplatform.commons.utils.BinaryOutput;

import java.io.IOException;

/**
 * An interface defining the renderer contract for a resource.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public interface ResourceRenderer
{

   /**
    * Returns an output stream for performing resource rendering.
    *
    * @return a stream
    * @throws IOException any io exception
    */
   BinaryOutput getOutput() throws IOException;

   /**
    * Instruct the renderer about the expiration time in seconds. A non positive value
    * means that no caching should be performed. The expiration value is relative to the
    * date of the request.
    *
    * @param seconds the value in seconds
    */
   void setExpiration(long seconds);

}
