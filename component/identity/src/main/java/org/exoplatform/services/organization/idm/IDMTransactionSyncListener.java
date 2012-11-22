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

import org.gatein.common.transaction.JTAUserTransactionLifecycleListener;

/**
 * Listener used for sync global JTA transaction and underlying IDM (Hibernate) transaction
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
class IDMTransactionSyncListener implements JTAUserTransactionLifecycleListener {
    private PicketLinkIDMService idmService;

    public IDMTransactionSyncListener(PicketLinkIDMService idmService) {
        this.idmService = idmService;
    }

    @Override
    public void beforeBegin() {
    }

    @Override
    public void afterBegin() {
        try {
            // We need this as from Hibernate4 and with JTATransactionFactory, there is need to separately start IDM (Hibernate)
            // transaction as well even if JTA transaction is started
            if (!idmService.getIdentitySession().getTransaction().isActive()) {
                idmService.getIdentitySession().beginTransaction();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
