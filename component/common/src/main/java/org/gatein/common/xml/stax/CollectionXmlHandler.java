/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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

package org.gatein.common.xml.stax;

import java.util.Collection;

import org.gatein.common.xml.stax.writer.StaxWriter;
import org.staxnav.StaxNavigator;

import static org.gatein.common.xml.stax.navigator.Exceptions.*;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public abstract class CollectionXmlHandler<T, N> implements XmlHandler<Collection<T>, N> {

    private final N collectionName;
    private final N itemName;

    public CollectionXmlHandler(N collectionName, N itemName) {
        this.collectionName = collectionName;
        this.itemName = itemName;
    }

    @Override
    public Collection<T> read(StaxNavigator<N> navigator) {
        if (!navigator.getName().equals(collectionName)) {
            throw expectedElement(navigator, collectionName);
        }
        Collection<T> collection = createCollection();

        N element = navigator.child();
        while (element != null) {
            if (!element.equals(itemName)) {
                throw expectedElement(navigator, itemName);
            }
            collection.add(readElement(navigator.fork()));
            element = navigator.sibling();
        }

        return collection;
    }

    @Override
    public void write(StaxWriter<N> writer, Collection<T> collection) {
        if (collection == null || collection.isEmpty()) return;

        writer.writeStartElement(collectionName);
        for (T element : collection) {
            writeElement(writer, element);
        }
        writer.writeEndElement();
    }

    protected abstract T readElement(StaxNavigator<N> navigator);

    protected abstract void writeElement(StaxWriter<N> writer, T object);

    protected abstract Collection<T> createCollection();
}
