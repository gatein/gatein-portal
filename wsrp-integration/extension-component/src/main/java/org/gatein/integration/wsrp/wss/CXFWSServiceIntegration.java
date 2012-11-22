/******************************************************************************
 * JBoss, a division of Red Hat                                               *
 * Copyright 2011, Red Hat Middleware, LLC, and individual                    *
 * contributors as indicated by the @authors tag. See the                     *
 * copyright.txt in the distribution for a full listing of                    *
 * individual contributors.                                                   *
 *                                                                            *
 * This is free software; you can redistribute it and/or modify it            *
 * under the terms of the GNU Lesser General Public License as                *
 * published by the Free Software Foundation; either version 2.1 of           *
 * the License, or (at your option) any later version.                        *
 *                                                                            *
 * This software is distributed in the hope that it will be useful,           *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU           *
 * Lesser General Public License for more details.                            *
 *                                                                            *
 * You should have received a copy of the GNU Lesser General Public           *
 * License along with this software; if not, write to the Free                *
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA         *
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.                   *
 ******************************************************************************/
package org.gatein.integration.wsrp.wss;

import org.gatein.wsrp.services.PortCustomizer;
import org.gatein.wsrp.services.PortCustomizerRegistry;
import org.gatein.wsrp.wss.CredentialsAccess;
import org.gatein.wsrp.wss.credentials.CredentialsAccessor;
import org.gatein.wsrp.wss.cxf.consumer.BEAPolicyIgnoringPortCustomizer;
import org.gatein.wsrp.wss.cxf.consumer.CXFPortCustomizer;
import org.picocontainer.Startable;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @version $Revision$
 */
public class CXFWSServiceIntegration implements Startable {
    private final PortCustomizer WSS4J_CUSTOMIZER = new CXFPortCustomizer();
    private final PortCustomizer BEA_POLICY_IGNORING_CUSTOMIZER = new BEAPolicyIgnoringPortCustomizer();

    public CXFWSServiceIntegration(CredentialsAccessor credentialsAccessor) {
        CredentialsAccess.getInstance().setCredentialsAccessor(credentialsAccessor);
    }

    public void start() {
        final PortCustomizerRegistry registry = PortCustomizerRegistry.getInstance();
        registry.register(WSS4J_CUSTOMIZER);
        registry.register(BEA_POLICY_IGNORING_CUSTOMIZER);
    }

    public void stop() {
        final PortCustomizerRegistry registry = PortCustomizerRegistry.getInstance();
        registry.unregister(WSS4J_CUSTOMIZER);
        registry.unregister(BEA_POLICY_IGNORING_CUSTOMIZER);
    }

}
