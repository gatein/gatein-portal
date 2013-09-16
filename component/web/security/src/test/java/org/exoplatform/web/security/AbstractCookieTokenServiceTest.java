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
import org.exoplatform.web.security.security.CookieTokenService;
import org.gatein.wci.security.Credentials;

/**
 *
 * @author <a href="mailto:haithanh0809@gmail.com">Hai Thanh Nguyen</a>
 *
 */

@ConfiguredBy({ @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/tokenservice-configuration.xml"),
        @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.test.jcr-configuration.xml"),
        @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/jcr-configuration.xml") })
public abstract class AbstractCookieTokenServiceTest extends AbstractTokenServiceTest<CookieTokenService> {

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
        /* Do nothing there is no CookieTokenService.getAllTokens(); */
    }

    @Override
    public void testSize() throws Exception {
        String token = service.createToken(new Credentials("root", "gtn"));
        assertEquals(service.size(), 1);
        service.deleteToken(token);
    }

    @Override
    public void testDeleteToken() throws Exception {
        String tokenId = service.createToken(new Credentials("root", "gtn"));
        assertNotSame(service.getToken(tokenId), service.deleteToken(tokenId));
        assertNull(service.getToken(tokenId));
        assertEquals(service.size(), 0);
        service.deleteToken(tokenId);
    }

    @Override
    public void testCleanExpiredTokens() throws Exception {
        assertEquals(2, service.getValidityTime());
        String tokenId1 = service.createToken(new Credentials("user1", "gtn"));
        assertEquals(1, service.size());

        Thread.sleep(2100);
        service.cleanExpiredTokens();
        assertEquals(0, service.size());

        service.deleteToken(tokenId1);

    }

}
