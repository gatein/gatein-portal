/*
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

package org.exoplatform.commons.cache.future;

/**
 * The loader interface is used by the future cache to retrieves the value from the key when it does not exist.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 * @param <K> the key type parameter
 * @param <V> the value type parameter
 * @param <C> the context type parameter
 */
public interface Loader<K, V, C>
{

   /**
    * Retrieves the value from the key within the specified context. If the resource is not found then the value
    * null must be returned.
    *
    * @param context the context
    * @param key the key
    * @return the value
    * @throws Exception any exception that would prevent the value to be loaded
    */
   V retrieve(C context, K key) throws Exception;

}
