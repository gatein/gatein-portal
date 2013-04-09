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

package org.gatein.portal;

import org.exoplatform.component.test.AbstractGateInTest;
import org.gatein.common.io.IOTools;
import org.gatein.portal.mop.PropertyType;
import org.gatein.portal.mop.layout.ElementState;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class PropertyTypeTestCase extends AbstractGateInTest {

    public void testSimple() {
        assertEquals("description", ElementState.Window.DESCRIPTION.getName());
        assertEquals(String.class, ElementState.Window.DESCRIPTION.getType());
    }

    public void testSerialization() throws Exception {
        PropertyType<String> type = ElementState.Window.DESCRIPTION;
        PropertyType clone = IOTools.clone(type);
        assertSame(clone, type);
    }
}
