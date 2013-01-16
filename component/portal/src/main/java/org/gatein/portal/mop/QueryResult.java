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

package org.gatein.portal.mop;

import java.util.Iterator;


/**
 * A query result.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public final class QueryResult<T> implements Iterable<T> {

    /** The result first item offset. */
    private final int from;

    /** The result item size. */
    private final int size;

    /** The items. */
    private final Iterable<T> items;

    public QueryResult(int from, int size, Iterable<T> items) {
        this.from = from;
        this.items = items;
        this.size = size;
    }

    /**
     * Returns the index of the first item in the result.
     *
     * @return the first index
     */
    public int getFrom() {
        return from;
    }

    /**
     * Returns the number of items returned.
     *
     * @return the number of items
     */
    public int getSize() {
        return size;
    }

    /**
     * Returns the index of the item following the last item in the result.
     *
     * @return the last index
     */
    public int getTo() {
        return from + size;
    }

    @Override
    public Iterator<T> iterator() {
        return items.iterator();
    }
}
