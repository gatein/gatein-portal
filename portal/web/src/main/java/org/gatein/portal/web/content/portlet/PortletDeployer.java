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

package org.gatein.portal.web.content.portlet;

import java.util.Set;

import javax.servlet.ServletContext;

import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.pc.api.Portlet;
import org.gatein.pc.api.PortletInvoker;
import org.gatein.pc.api.PortletInvokerException;
import org.gatein.pc.portlet.PortletInvokerInterceptor;
import org.gatein.pc.portlet.aspects.ConsumerCacheInterceptor;
import org.gatein.pc.portlet.aspects.PortletCustomizationInterceptor;
import org.gatein.pc.portlet.aspects.RequestAttributeConversationInterceptor;
import org.gatein.pc.portlet.aspects.SecureTransportInterceptor;
import org.gatein.pc.portlet.aspects.ValveInterceptor;
import org.gatein.pc.portlet.container.ContainerPortletDispatcher;
import org.gatein.pc.portlet.container.ContainerPortletInvoker;
import org.gatein.pc.portlet.impl.deployment.DeploymentException;
import org.gatein.pc.portlet.impl.state.StateManagementPolicyService;
import org.gatein.pc.portlet.impl.state.producer.PortletStatePersistenceManagerService;
import org.gatein.pc.portlet.state.producer.ProducerPortletInvoker;
import org.gatein.wci.ServletContainerFactory;
import org.gatein.wci.WebApp;
import org.gatein.wci.WebAppEvent;
import org.gatein.wci.WebAppLifeCycleEvent;
import org.gatein.wci.WebAppListener;
import org.picocontainer.Startable;

/**
 * A bean that deploys portlets.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class PortletDeployer implements WebAppListener, Startable {

    /** . */
    private final Logger logger = LoggerFactory.getLogger(PortletDeployer.class);

    /** . */
    private final PortletInvoker invoker;

    /** . */
    private final org.gatein.pc.portlet.impl.deployment.PortletApplicationDeployer deployer;

    public PortletDeployer() {

        ContainerPortletInvoker containerPortletInvoker = new ContainerPortletInvoker();
        org.gatein.pc.portlet.impl.deployment.PortletApplicationDeployer deployer = new org.gatein.pc.portlet.impl.deployment.PortletApplicationDeployer(containerPortletInvoker);

        // Container stack
        PortletInvokerInterceptor consumerPortletInvoker = new PortletInvokerInterceptor();
        consumerPortletInvoker.
            append(new ConsumerCacheInterceptor()).
            append(new PortletCustomizationInterceptor()).
            append(new ProducerPortletInvoker(new PortletStatePersistenceManagerService(), new StateManagementPolicyService(false), new PortletStateConverter())).
            append(containerPortletInvoker).
            append(new ValveInterceptor(deployer)).
            append(new SecureTransportInterceptor()).
            append(new RequestAttributeConversationInterceptor()).
            append(new ContainerPortletDispatcher());

        //
        this.invoker = consumerPortletInvoker;
        this.deployer = deployer;
    }

    /**
     * Returns the portlet invoker for the portal.
     *
     * @return the portlet invoker
     */
    public PortletInvoker getInvoker() {
        return invoker;
    }

    /**
     * Returns all the known portlets.
     *
     * @return the portlets
     * @throws PortletInvokerException
     */
	public Set<Portlet> getAllPortlets() throws PortletInvokerException {
		return invoker.getPortlets();
	}

    @Override
    public void onEvent(WebAppEvent event) {
        if (event instanceof WebAppLifeCycleEvent) {
            WebAppLifeCycleEvent lifeCycleEvent = (WebAppLifeCycleEvent) event;
            WebApp webApp = lifeCycleEvent.getWebApp();
            ServletContext context = webApp.getServletContext();
            if  (lifeCycleEvent.getType() == WebAppLifeCycleEvent.ADDED) {
                try {
                    deployer.add(context);
                    logger.info("Deployed portlet application " + webApp.getContextPath());
                } catch (DeploymentException e) {
                    logger.error("Could not deploy portlet application " + webApp.getContextPath(), e);
                }
            } else if  (lifeCycleEvent.getType() == WebAppLifeCycleEvent.REMOVED) {
                deployer.remove(context);
            }
        }
    }

    @Override
    public void start() {
        ServletContainerFactory.getServletContainer().addWebAppListener(this);
    }

    @Override
    public void stop() {
        ServletContainerFactory.getServletContainer().removeWebAppListener(this);
    }
}
