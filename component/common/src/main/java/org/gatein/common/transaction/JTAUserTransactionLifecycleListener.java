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

package org.gatein.common.transaction;

/**
 * Listener for perform some actions at the specified point of JTA transaction lifecycle.<br />
 * Transaction lifecycle needs to be managed through {@link JTAUserTransactionLifecycleService} to have listeners executed.<br />
 *
 * For now, we have shared instance of one registered listener for all transactions, so listener implementations need to be
 * thread-safe
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public interface JTAUserTransactionLifecycleListener {
    /**
     * Callback method to be executed before start of JTA transaction
     */
    void beforeBegin();

    /**
     * Callback method to be executed after start of JTA transaction
     */
    void afterBegin();
}
