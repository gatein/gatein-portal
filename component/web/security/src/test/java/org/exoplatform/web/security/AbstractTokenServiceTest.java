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

import java.io.File;
import java.net.URL;

import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.component.test.AbstractKernelTest;
import org.exoplatform.web.security.security.AbstractTokenService;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Hai Thanh Nguyen</a>
 * @version $Id$
 *
 */

public abstract class AbstractTokenServiceTest<S extends AbstractTokenService<?, ?>> extends AbstractKernelTest {
    protected S service;

    protected void beforeRunBare() {
        String foundGateInConfDir = PropertyManager.getProperty("gatein.conf.dir");
        if (foundGateInConfDir == null || foundGateInConfDir.length() == 0) {
            /* A way to get the conf directory path */
            URL tokenserviceConfUrl = Thread.currentThread().getContextClassLoader()
                    .getResource("conf/tokenservice-configuration.xml");
            File confDir = new File(tokenserviceConfUrl.getPath()).getParentFile();
            PropertyManager.setProperty("gatein.conf.dir", confDir.getAbsolutePath());
        }
        super.beforeRunBare();
    }

    public abstract void testGetToken() throws Exception;

    public abstract void testGetAllToken() throws Exception;

    public abstract void testSize() throws Exception;

    public abstract void testDeleteToken() throws Exception;

    public abstract void testCleanExpiredTokens() throws Exception;
}
