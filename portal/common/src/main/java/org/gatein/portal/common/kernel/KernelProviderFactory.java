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

package org.gatein.portal.common.kernel;

import javax.inject.Provider;

import juzu.inject.ProviderFactory;
import org.exoplatform.container.PortalContainer;
import org.picocontainer.ComponentAdapter;

/**
 * Provides kernel services in Juzu application.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class KernelProviderFactory implements ProviderFactory {

    @Override
    public <T> Provider<? extends T> getProvider(final Class<T> implementationType) throws Exception {
        final PortalContainer container = PortalContainer.getInstance();
        if (container == null) {
            throw new IllegalStateException("Not running in the context of a portal container");
        }
        final ComponentAdapter adapter = container.getComponentAdapterOfType(implementationType);
        if (adapter != null) {
            return new Provider<T>() {
                @Override
                public T get() {
                    Object service = adapter.getComponentInstance(container);
                    if (service == null) {
                        throw new RuntimeException("Could not obtain service " + implementationType + " from container " + container);
                    }
                    return implementationType.cast(service);
                }
            };
        } else {
            return null;
        }
    }
}
