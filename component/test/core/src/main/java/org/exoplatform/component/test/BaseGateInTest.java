/*
 * Copyright (C) 2010 eXo Platform SAS.
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

package org.exoplatform.component.test;

import java.util.concurrent.Callable;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.RootContainer;
import org.exoplatform.container.component.RequestLifeCycle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class BaseGateInTest extends TestCase {

    public BaseGateInTest() {
    }

    public BaseGateInTest(String name) {
        super(name);
    }

    public static <T> T assertInstanceOf(Object o, Class<T> expectedType) {
        if (expectedType != null) {
            if (expectedType.isInstance(o)) {
                return expectedType.cast(o);
            } else {
                fail("Was expecting " + o + " to be instanceof of " + expectedType);
                return null;
            }
        } else {
            fail("Need an expected type");
            return null;
        }
    }

    public static void fail(String msg, Throwable t) {
        throw failure(msg, t);
    }

    public static void fail(Throwable t) {
        AssertionFailedError afe = new AssertionFailedError();
        afe.initCause(t);
        throw afe;
    }

    public static Error failure(String msg) {
        throw new AssertionFailedError(msg);
    }

    public static Error failure(Throwable t) {
        AssertionFailedError afe = new AssertionFailedError();
        afe.initCause(t);
        return afe;
    }

    public static Error failure(String msg, Throwable t) {
        AssertionFailedError afe = new AssertionFailedError(msg);
        afe.initCause(t);
        return afe;
    }

    /**
     * Execute the <code>callback</code> argument within the context of the portal container.
     *
     * @param callback the callback to execute
     */
    public static void inPortalContainer(final Runnable callback) {
        inPortalContainer(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                callback.run();
                return null;
            }
        });
    }

    /**
     * Execute the <code>callback</code> argument within the context of the portal container and return the callback value.
     *
     * @param callback the callback to execute
     */
    public static <V> V inPortalContainer(Callable<V> callback) {
        PortalContainer portalContainer = PortalContainer.getInstanceIfPresent();
        boolean remove;
        if (portalContainer == null) {
            remove = true;
            RootContainer rootContainer = RootContainer.getInstance();
            portalContainer = (PortalContainer) rootContainer.getComponentInstanceOfType(PortalContainer.class);
            if (portalContainer == null) {
                throw failure("Could not obtain a valid portal container");
            } else {
                PortalContainer.setInstance(portalContainer);
            }
        } else {
            remove = false;
        }
        RequestLifeCycle.begin(portalContainer);
        try {
            return callback.call();
        } catch (Exception e) {
            throw failure(e);
        } finally {
            RequestLifeCycle.end();
            if (remove) {
                PortalContainer.setInstance(null);
            }
        }
    }
}
