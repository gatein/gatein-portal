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

package org.gatein.portal.impl.mop.ram;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class Tx {

    /** . */
    private static final ThreadLocal<Tx> current = new ThreadLocal<Tx>();

    static Tx associate(Store context) {
        Tx tx = current.get();
        if (tx != null) {
            if (tx.context != null) {
                if (tx.context.origin != context) {
                    throw new AssertionError();
                }
            } else {
                tx.context = context.open();
            }
            return tx;
        } else {
            throw new IllegalStateException("No tx");
        }
    }

    /** . */
    private Store context;

    Store getContext() {
        return context;
    }

    private Tx() {
        this.context = null;
    }

    public static void begin() {
        if (current.get() != null) {
            throw new IllegalStateException("Already in tx");
        }
        current.set(new Tx());
    }

    public static void end(boolean commit) {
        Tx tx = current.get();
        if (tx == null) {
            throw new IllegalStateException("Not in tx");
        }
        if (tx.context != null) {
            if (commit) {
                tx.context.merge();
            }
        }
        current.set(null);
    }

    public static void run(Runnable runnable) {
        if (current.get() != null) {
            throw new IllegalStateException("Already in tx");
        }
        Tx tx = new Tx();
        current.set(tx);
        boolean commit = false;
        try {
            runnable.run();
            commit = true;
        } finally {
            if (commit) {
                if (tx.context != null) {
                    tx.context.merge();
                }
            }
            current.set(null);
        }
    }

}
