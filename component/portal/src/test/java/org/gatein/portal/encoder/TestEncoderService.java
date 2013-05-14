/*
 * JBoss, a division of Red Hat
 * Copyright 2013, Red Hat Middleware, LLC, and individual
 * contributors as indicated by the @authors tag. See the
 * copyright.txt in the distribution for a full listing of
 * individual contributors.
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

package org.gatein.portal.encoder;

import org.exoplatform.component.test.AbstractKernelTest;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.container.PortalContainer;

/**
 * Test for {@link EncoderService}
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
@ConfiguredBy({
   @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.encoder-configuration.xml")})
public class TestEncoderService extends AbstractKernelTest {

    private EncoderService encoderService;

    @Override
    protected void setUp() throws Exception {
        PortalContainer portalContainer = PortalContainer.getInstance();
        this.encoderService = (EncoderService) portalContainer.getComponentInstanceOfType(EncoderService.class);
    }

    public void testEncoder() throws Exception {
        encodeDecodeTest("gtn", "6MSyXIj3kkQ=");
        encodeDecodeTest("blabla", "tstM3KRJOU4=");
        encodeDecodeTest("gogog", "zlGKEql9zxE=");
    }

    private void encodeDecodeTest(String plainText, String expectedEncoded) throws Exception {
        String encoded = encoderService.encode64(plainText);
        assertEquals(encoded, expectedEncoded);

        String decoded = encoderService.decode64(encoded);
        assertEquals(decoded, plainText);
    }

}
