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
package org.gatein.api;

import java.util.Arrays;
import java.util.Comparator;

import org.gatein.api.security.Membership;
import org.gatein.api.security.Permission;

public class Assert extends org.junit.Assert {

    public static void assertEquals(Permission expected, Permission actual) {
        Membership[] e = expected.getMemberships().toArray(new Membership[expected.getMemberships().size()]);
        Membership[] a = actual.getMemberships().toArray(new Membership[expected.getMemberships().size()]);

        Comparator<Membership> m = new Comparator<Membership>() {
            @Override
            public int compare(Membership o1, Membership o2) {
                int i = o1.getGroup().getId().compareTo(o2.getGroup().getId());
                if (i != 0) {
                    return i;
                }

                return o1.getMembershipType().compareTo(o2.getMembershipType());
            }
        };

        Arrays.sort(e, m);
        Arrays.sort(a, m);

        assertArrayEquals(e, a, m);
    }
    
    public static <T> void assertArrayEquals(T[] expected, T[] actual, Comparator<T> comparator) {
        assertEquals("Arrays length differ", expected.length, actual.length);
        
        for (int i = 0; i < expected.length; i++) {
            assertEquals("Arrays differ at " + i, 0, comparator.compare(expected[i], actual[i]));
        }
    }
}
