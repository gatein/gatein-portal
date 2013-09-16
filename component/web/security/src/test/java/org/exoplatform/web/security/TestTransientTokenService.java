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
package org.exoplatform.web.security;

import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.web.security.security.TransientTokenService;
import org.gatein.wci.security.Credentials;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Hai Thanh Nguyen</a>
 * @version $Id$
 *
 */

@ConfiguredBy({ @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/tokenservice-configuration.xml"),
        @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.test.jcr-configuration.xml"),
        @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/jcr-configuration.xml") })
public class TestTransientTokenService extends AbstractTokenServiceTest<TransientTokenService> {

    protected void setUp() throws Exception {
        PortalContainer container = getContainer();
        service = (TransientTokenService) container.getComponentInstanceOfType(TransientTokenService.class);
    }

    @Override
    public void testGetToken() throws Exception {
        String tokenId = service.createToken(new Credentials("root", "gtn"));
        assertEquals(service.getValidityTime(), 2);

        GateInToken token = service.getToken(tokenId);
        assertEquals(token.getPayload().getUsername(), "root");
        assertEquals(token.getPayload().getPassword(), "gtn");
        service.deleteToken(tokenId);
    }

    @Override
    public void testGetAllToken() throws Exception {
        String tokenId1 = service.createToken(new Credentials("root1", "gtn1"));
        String tokenId2 = service.createToken(new Credentials("root2", "gtn2"));
        String[] tokens = service.getAllTokens();
        assertEquals(tokens.length, 2);

        GateInToken token1 = service.getToken(tokenId1);
        assertEquals(token1.getPayload().getUsername(), "root1");
        assertEquals(token1.getPayload().getPassword(), "gtn1");

        GateInToken token2 = service.getToken(tokenId2);
        assertEquals(token2.getPayload().getUsername(), "root2");
        assertEquals(token2.getPayload().getPassword(), "gtn2");
        service.deleteToken(tokenId1);
        service.deleteToken(tokenId2);
    }

    @Override
    public void testSize() throws Exception {
        String tokenId1 = service.createToken(new Credentials("root1", "gtn1"));
        assertEquals(service.size(), 1);
        service.deleteToken(tokenId1);
    }

    @Override
    public void testDeleteToken() throws Exception {
        String tokenId = service.createToken(new Credentials("root", "gtn"));
        service.deleteToken(tokenId);
        assertNull(service.getToken(tokenId));
    }

    public void testCleanExpiredTokens() throws Exception {
        assertEquals(2, service.getValidityTime());
        service.createToken(new Credentials("user1", "gtn"));
        assertEquals(1, service.size());

        Thread.sleep(2100);
        service.cleanExpiredTokens();
        assertEquals(0, service.size());
    }

}
