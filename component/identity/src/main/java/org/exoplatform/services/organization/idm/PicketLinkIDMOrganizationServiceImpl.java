/**
 * Copyright (C) 2009 eXo Platform SAS.
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

package org.exoplatform.services.organization.idm;

import javax.transaction.Status;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.component.ComponentRequestLifecycle;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ObjectParameter;
import org.exoplatform.services.organization.BaseOrganizationService;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.common.transaction.JTAUserTransactionLifecycleService;
import org.picocontainer.Startable;

/*
 * @author <a href="mailto:boleslaw.dawidowicz at redhat.com">Boleslaw Dawidowicz</a>
 */
public class PicketLinkIDMOrganizationServiceImpl extends BaseOrganizationService implements Startable,
        ComponentRequestLifecycle {

    // We may have several portal containers thus we need one PicketLinkIDMService per portal container
    // private static PicketLinkIDMService jbidmService_;
    private PicketLinkIDMServiceImpl idmService_;

    public static final String CONFIGURATION_OPTION = "configuration";

    private Config configuration = new Config();

    private JTAUserTransactionLifecycleService jtaTransactionLifecycleService;

    private static final Logger log = LoggerFactory.getLogger(PicketLinkIDMOrganizationServiceImpl.class);
    private static final boolean traceLoggingEnabled = log.isTraceEnabled();

    // Indicates whether any call to startRequest and endRequest is accepted
    private volatile boolean acceptComponentRequestCall;

    public PicketLinkIDMOrganizationServiceImpl(InitParams params, PicketLinkIDMService idmService,
            JTAUserTransactionLifecycleService jtaTransactionLifecycleService) throws Exception {
        groupDAO_ = new GroupDAOImpl(this, idmService);
        userDAO_ = new UserDAOImpl(this, idmService);
        userProfileDAO_ = new UserProfileDAOImpl(this, idmService);
        membershipDAO_ = new MembershipDAOImpl(this, idmService);
        membershipTypeDAO_ = new MembershipTypeDAOImpl(this, idmService);

        idmService_ = (PicketLinkIDMServiceImpl) idmService;

        this.jtaTransactionLifecycleService = jtaTransactionLifecycleService;

        if (params != null) {
            // Options
            ObjectParameter configurationParam = params.getObjectParam(CONFIGURATION_OPTION);

            if (configurationParam != null) {
                this.configuration = (Config) configurationParam.getObject();
            }

        }

    }

    public final org.picketlink.idm.api.Group getJBIDMGroup(String groupId) throws Exception {
        String[] ids = groupId.split("/");
        String name = ids[ids.length - 1];
        String parentId = null;
        if (groupId.contains("/")) {
            parentId = groupId.substring(0, groupId.lastIndexOf("/"));
        }

        String plGroupName = configuration.getPLIDMGroupName(name);

        return idmService_.getIdentitySession().getPersistenceManager()
                .findGroup(plGroupName, getConfiguration().getGroupType(parentId));
    }

    @Override
    public void start() {
        try {
            if (configuration.isUseJTA()) {
                jtaTransactionLifecycleService.registerListener(new IDMTransactionSyncListener(idmService_));
            }
            acceptComponentRequestCall = true;

            RequestLifeCycle.begin(this);

            super.start();

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            RequestLifeCycle.end();
        }

    }

    @Override
    public void stop() {
        // toto
    }

    /*
   */
    /**
     * Used to allow nested requests (as done by the authenticator during unit tests) and avoid to commit two times the same
     * transaction.
     */
    /*
     *
     * private ThreadLocal<AtomicInteger> currentRequestCount = new ThreadLocal<AtomicInteger>() {
     *
     * @Override protected AtomicInteger initialValue() { return new AtomicInteger(); } };
     */

    public void startRequest(ExoContainer container) {
        if (!acceptComponentRequestCall)
            return;
        try {
            if (configuration.isUseJTA()) {
                if (traceLoggingEnabled) {
                    log.trace("Starting UserTransaction in method startRequest");
                }
                jtaTransactionLifecycleService.beginJTATransaction();
            } else {

                if (!idmService_.getIdentitySession().getTransaction().isActive()) {
                    idmService_.getIdentitySession().beginTransaction();
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public void flush() {
        try {

            if (configuration.isUseJTA()) {
                if (traceLoggingEnabled) {
                    log.trace("Flushing UserTransaction in method flush");
                }
                // Complete restart of JTA transaction don't have good performance. So we will only sync identitySession (same
                // as for non-jta environment)
                // finishJTATransaction();
                // beginJTATransaction();
                if (jtaTransactionLifecycleService.getUserTransaction().getStatus() == Status.STATUS_ACTIVE) {
                    idmService_.getIdentitySession().save();
                }
            } else {
                if (idmService_.getIdentitySession().getTransaction().isActive()) {
                    idmService_.getIdentitySession().save();
                }
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public void endRequest(ExoContainer container) {
        if (!acceptComponentRequestCall)
            return;
        try {
            if (configuration.isUseJTA()) {
                if (traceLoggingEnabled) {
                    log.trace("Finishing UserTransaction in method endRequest");
                }
                jtaTransactionLifecycleService.finishJTATransaction();
            } else {
                if (idmService_.getIdentitySession().getTransaction().isActive()) {
                    idmService_.getIdentitySession().getTransaction().commit();
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public Config getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Config configuration) {
        this.configuration = configuration;
    }

}
