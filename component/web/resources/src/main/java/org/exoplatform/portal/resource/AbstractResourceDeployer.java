/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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

package org.exoplatform.portal.resource;

import java.net.MalformedURLException;
import java.net.URL;

import org.gatein.wci.WebApp;
import org.gatein.wci.WebAppEvent;
import org.gatein.wci.WebAppLifeCycleEvent;
import org.gatein.wci.WebAppListener;

/**
 * @author <a href="mailto:ppalaga@redhat.com">Peter Palaga</a>
 *
 */
public abstract class AbstractResourceDeployer implements WebAppListener {

    public static final String GATEIN_CONFIG_RESOURCE = "/WEB-INF/gatein-resources.xml";

    /**
     * @see org.gatein.wci.WebAppListener#onEvent(org.gatein.wci.WebAppEvent)
     */
    public void onEvent(WebAppEvent event) {
        if (event instanceof WebAppLifeCycleEvent) {
            WebAppLifeCycleEvent lifeCycleEvent = (WebAppLifeCycleEvent) event;
            WebApp webApp = null;
            URL url = null;
            switch (lifeCycleEvent.getType()) {
                case WebAppLifeCycleEvent.ADDED:
                    webApp = event.getWebApp();
                    url = getGateinResourcesXml(webApp);
                    if (url != null) {
                        add(webApp, url);
                    }
                    break;
                case WebAppLifeCycleEvent.REMOVED:
                    webApp = event.getWebApp();
                    url = getGateinResourcesXml(webApp);
                    if (url != null) {
                        remove(event.getWebApp());
                    }
                    break;
            }
        }
    }

    /**
     * Called on web application add event if the application contains {@value #GATEIN_CONFIG_RESOURCE} file.
     *
     * @param webApp
     * @param url
     */
    protected abstract void add(WebApp webApp, URL url);

    /**
     * Called on web application remove event if the application contains {@value #GATEIN_CONFIG_RESOURCE} file.
     *
     * @param webApp
     * @param url
     */
    protected abstract void remove(WebApp webApp);

    protected URL getGateinResourcesXml(final WebApp webApp) {
        try {
            return webApp.getServletContext().getResource(GATEIN_CONFIG_RESOURCE);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

}
