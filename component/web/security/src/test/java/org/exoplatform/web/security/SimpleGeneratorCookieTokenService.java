/*
 * JBoss, a division of Red Hat
 * Copyright 2012, Red Hat Middleware, LLC, and individual
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

package org.exoplatform.web.security;

import org.exoplatform.commons.chromattic.ChromatticManager;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ObjectParameter;
import org.exoplatform.web.security.hash.NoSaltedHashService;
import org.exoplatform.web.security.security.TokenServiceInitializationException;
import org.exoplatform.web.security.security.CookieTokenService;

/**
 * CookieTokenService with changed mechanism for token generation (testing purposes)
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class SimpleGeneratorCookieTokenService extends CookieTokenService {

    private int counter = 0;
    private int noRandom = 0;

    public SimpleGeneratorCookieTokenService(InitParams initParams, ChromatticManager chromatticManager)
            throws TokenServiceInitializationException {
        super(replaceHashService(initParams), chromatticManager);
    }

    /**
     * @param initParams
     * @return
     */
    private static InitParams replaceHashService(InitParams initParams) {
        ObjectParameter hashParam = new ObjectParameter();
        hashParam.setName(CookieTokenService.HASH_SERVICE_INIT_PARAM);
        hashParam.setObject(new NoSaltedHashService());
        initParams.addParameter(hashParam );
        return initParams;
    }

    @Override
    protected String nextTokenId() {
        counter++;
        return "rememberme" + counter / 2;
    }



    /* (non-Javadoc)
     * @see org.exoplatform.web.security.security.AbstractTokenService#nextRandom()
     */
    @Override
    protected String nextRandom() {
        noRandom++;
        return "random"+ String.valueOf(noRandom/2);
    }

    int getCounter() {
        return counter;
    }
}
