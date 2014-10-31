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

package org.gatein.security.oauth.registry;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.gatein.common.classloader.DelegatingClassLoader;
import org.gatein.security.oauth.principal.DefaultPrincipalProcessor;
import org.gatein.security.oauth.spi.AccessTokenContext;
import org.gatein.security.oauth.spi.OAuthPrincipalProcessor;
import org.gatein.security.oauth.spi.OAuthProviderProcessor;
import org.gatein.security.oauth.spi.OAuthProviderType;

/**
 * Kernel plugin wrapping data about single {@link org.gatein.security.oauth.spi.OAuthProviderType}
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class OauthProviderTypeRegistryPlugin<T extends AccessTokenContext> extends BaseComponentPlugin {

    private final OAuthProviderType oauthPrType;

    public OauthProviderTypeRegistryPlugin(InitParams params, ExoContainerContext containerContext) throws Exception {
        String key = getParam(params, "key");
        String enabledPar = getParam(params, "enabled");
        String usernameAttributeName = getParam(params, "userNameAttributeName");
        String oauthProviderProcessorClass = getParam(params, "oauthProviderProcessorClass");
        String principalProcessorClassName = null;

        ValueParam param = params.getValueParam("principalProcessorClass");
        if(param != null) {
            principalProcessorClassName = param.getValue();
        }

        String initOAuthURL = getParam(params, "initOAuthURL");
        String friendlyName = getParam(params, "friendlyName");

        boolean enabled = Boolean.parseBoolean(enabledPar);

        if (enabled) {
            ClassLoader tccl = Thread.currentThread().getContextClassLoader();
            ClassLoader oauth = OAuthProviderType.class.getClassLoader();
            ClassLoader delegating = new DelegatingClassLoader(tccl, oauth);
            Class<OAuthProviderProcessor<T>> processorClass = (Class<OAuthProviderProcessor<T>>)delegating.loadClass(oauthProviderProcessorClass);
            OAuthProviderProcessor<T> oauthProviderProcessor = (OAuthProviderProcessor<T>) containerContext.getContainer().getComponentInstanceOfType(processorClass);

            OAuthPrincipalProcessor principalProcessor = null;
            Class<OAuthPrincipalProcessor> principalProcessorClass = (Class<OAuthPrincipalProcessor>) (principalProcessorClassName != null ? delegating
                    .loadClass(principalProcessorClassName) : DefaultPrincipalProcessor.class);
            principalProcessor = (OAuthPrincipalProcessor) containerContext.getContainer().getComponentInstanceOfType(principalProcessorClass);
            if (principalProcessor == null) {
                principalProcessor = principalProcessorClass.newInstance();
            }

            oauthPrType = new OAuthProviderType<T>(key, enabled, usernameAttributeName, oauthProviderProcessor, principalProcessor, initOAuthURL, friendlyName);
        } else {
            oauthPrType = null;
        }
    }

    OAuthProviderType getOAuthProviderType() {
        return oauthPrType;
    }

    private String getParam(InitParams params, String paramName) {
        ValueParam param = params.getValueParam(paramName);
        if (param == null) {
            throw new IllegalArgumentException("Parameter '" + paramName + "' needs to be provided");
        }

        return param.getValue();
    }
}
