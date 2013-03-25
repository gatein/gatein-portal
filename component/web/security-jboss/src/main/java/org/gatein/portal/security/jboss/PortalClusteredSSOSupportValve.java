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

package org.gatein.portal.security.jboss;

import java.io.IOException;

import javax.servlet.ServletException;

import org.apache.catalina.Container;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Pipeline;
import org.apache.catalina.Realm;
import org.apache.catalina.Session;
import org.apache.catalina.Valve;
import org.apache.catalina.authenticator.Constants;
import org.apache.catalina.authenticator.SingleSignOn;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.util.LifecycleSupport;
import org.apache.catalina.valves.ValveBase;
import org.exoplatform.container.ExoContainer;
import org.jboss.logging.Logger;

/**
 * Helper valve for supporting JBoss clustered SSO Valve. Re-authentication is not initiated by JBoss as it is not aware that
 * the resources require authentication. This valve forces re-authentication when a single-sign-on identify is present in the
 * session and there is no principal authenticated for the request.
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class PortalClusteredSSOSupportValve extends ValveBase implements Lifecycle {

    private static final Logger log = Logger.getLogger(PortalClusteredSSOSupportValve.class);

    private final LifecycleSupport support = new LifecycleSupport(this);

    private SingleSignOn sso = null;

    @Override
    public void invoke(Request request, Response response) throws IOException, ServletException {
        if (sso != null) {
            Session session = request.getSessionInternal();
            String ssoId = (String) request.getNote(Constants.REQ_SSOID_NOTE);
            if (ssoId != null && request.getUserPrincipal() == null) {
                if (sso != null) {
                    Container parent = getContainer();
                    if (parent != null) {
                        Realm realm = parent.getRealm();
                        if (realm != null) {
                            if (sso.reauthenticate(ssoId, realm, request)) {
                                sso.associate(ssoId, session);

                                if (log.isDebugEnabled()) {
                                    log.debug(" Reauthenticated cached principal '" + request.getUserPrincipal().getName()
                                            + "' with auth type '" + request.getAuthType() + "'");
                                }
                            }
                        }
                    }
                }
            }
        }

        getNext().invoke(request, response);
    }

    private SingleSignOn findSSOValve() {
        if (!ExoContainer.getProfiles().contains("cluster")) {
            return null;
        }

        for (Container parent = container.getParent(); parent != null; parent = parent.getParent()) {
            if (parent instanceof Pipeline) {
                Valve[] valves = ((Pipeline) parent).getValves();
                for (int i = 0; i < valves.length; i++) {
                    if (valves[i] instanceof SingleSignOn) {
                        SingleSignOn sso = (SingleSignOn) valves[i];
                        log.debug("Found SingleSignOn Valve at " + sso);
                        return sso;
                    }
                }
            }
        }

        log.debug("No SingleSignOn Valve is present");
        return null;
    }

    public void start() throws LifecycleException {
        sso = findSSOValve();
        support.fireLifecycleEvent(START_EVENT, this);
    }

    public void stop() throws LifecycleException {
        support.fireLifecycleEvent(STOP_EVENT, this);
    }

    @Override
    public void addLifecycleListener(LifecycleListener listener) {
        support.addLifecycleListener(listener);
    }

    @Override
    public void removeLifecycleListener(LifecycleListener listener) {
        support.removeLifecycleListener(listener);
    }

    @Override
    public LifecycleListener[] findLifecycleListeners() {
        return support.findLifecycleListeners();
    }
}
