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

package org.exoplatform.services.organization.idm;

import javax.transaction.Status;
import javax.transaction.UserTransaction;

import org.exoplatform.container.ExoContainerContext;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.common.transaction.JTAUserTransactionLifecycleService;
import org.picketlink.idm.api.IdentitySession;
import org.picketlink.idm.api.Transaction;

/**
 * Abstract superclass for other DAO classes
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class AbstractDAOImpl {
    protected final PicketLinkIDMService service_;

    protected final PicketLinkIDMOrganizationServiceImpl orgService;

    protected final Logger log = LoggerFactory.getLogger(getClass());

    public AbstractDAOImpl(PicketLinkIDMOrganizationServiceImpl orgService, PicketLinkIDMService idmService) {
        service_ = idmService;
        this.orgService = orgService;
    }

    public void handleException(String messageToLog, Exception e) {
        log.info(messageToLog, e);

        // Mark JTA transaction to rollback-only if JTA setup is enabled
        if (orgService.getConfiguration().isUseJTA()) {
            try {
                JTAUserTransactionLifecycleService transactionLfService = (JTAUserTransactionLifecycleService) ExoContainerContext
                        .getCurrentContainer().getComponentInstanceOfType(JTAUserTransactionLifecycleService.class);
                UserTransaction tx = transactionLfService.getUserTransaction();
                if (tx.getStatus() == Status.STATUS_ACTIVE) {
                    tx.setRollbackOnly();
                }
            } catch (Exception tre) {
                log.warn("Unable to set Transaction status to be rollback only", tre);
            }
        } else {
            orgService.recoverFromIDMError();
        }
    }

    protected IdentitySession getIdentitySession() throws Exception {
        return service_.getIdentitySession();
    }
}
