/*
    * JBoss, Home of Professional Open Source.
    * Copyright 2012, Red Hat, Inc., and individual contributors
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

package org.exoplatform.portal.mop.management.binding.xml;

import static org.junit.Assert.assertArrayEquals;
import junit.framework.TestCase;

/**
 * @author <a href="mailto:ppalaga@redhat.com">Peter Palaga</a>
 *
 */
public class UtilsTest extends TestCase {
    public void testTidyUp() {
        assertArrayEquals(null, Utils.tidyUp(null));
        assertArrayEquals(new String[] {}, Utils.tidyUp(new String[] {}));

        String[] array = new String[] {"one", "two", "three"};
        assertSame(array, Utils.tidyUp(array));

        assertArrayEquals(new String[] {"one", "three"}, Utils.tidyUp(new String[] {"one", "", "three"}));
        assertArrayEquals(new String[] {"one",}, Utils.tidyUp(new String[] {"one", "", ""}));
        assertArrayEquals(new String[] {"three"}, Utils.tidyUp(new String[] {"", "", "three"}));
        assertArrayEquals(new String[] {}, Utils.tidyUp(new String[] {"", "", ""}));
        assertArrayEquals(new String[] {"one", "three"}, Utils.tidyUp(new String[] {"one", null, "three"}));
        assertArrayEquals(new String[] {"one"}, Utils.tidyUp(new String[] {"one", null, null}));
        assertArrayEquals(new String[] {"three"}, Utils.tidyUp(new String[] {null, null, "three"}));
        assertArrayEquals(new String[] {}, Utils.tidyUp(new String[] {null, null, null}));
    }

}
