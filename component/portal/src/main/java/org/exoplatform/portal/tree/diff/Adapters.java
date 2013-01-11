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

package org.exoplatform.portal.tree.diff;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;


/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class Adapters {

    /** . */
    private static final ArrayAdapter ARRAY_INSTANCE = new ArrayAdapter();

    /** . */
    private static final JavaUtilListAdapter LIST_INSTANCE = new JavaUtilListAdapter();

    public static <E> ListAdapter<E[], E> array() {
        @SuppressWarnings("unchecked")
        ListAdapter<E[], E> adapter = (ListAdapter<E[], E>) ARRAY_INSTANCE;
        return adapter;
    }

    public static <E> ListAdapter<List<E>, E> list() {
        @SuppressWarnings("unchecked")
        ListAdapter<List<E>, E> adapter = (ListAdapter<List<E>, E>) LIST_INSTANCE;
        return adapter;
    }

    private static class ArrayAdapter<E> implements ListAdapter<E[], E> {
        public int size(E[] list) {
            return list.length;
        }

        public Iterator<E> iterator(final E[] list, final boolean reverse) {
            return new Iterator<E>() {
                /** . */
                int count = 0;

                public boolean hasNext() {
                    return count < list.length;
                }

                public E next() {
                    if (!hasNext()) {
                        throw new NoSuchElementException();
                    }
                    int index = count++;
                    if (reverse) {
                        index = list.length - index - 1;
                    }
                    return list[index];
                }

                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }

    private static class JavaUtilListAdapter<E> implements ListAdapter<List<E>, E> {
        @Override
        public int size(List<E> list) {
            return list.size();
        }

        @Override
        public Iterator<E> iterator(List<E> list, boolean reverse) {
            if (reverse) {
                final ListIterator<E> i = list.listIterator(list.size());
                return new Iterator<E>() {
                    @Override
                    public boolean hasNext() {
                        return i.hasPrevious();
                    }
                    @Override
                    public E next() {
                        return i.previous();
                    }
                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            } else {
                return list.iterator();
            }
        }
    }
}
