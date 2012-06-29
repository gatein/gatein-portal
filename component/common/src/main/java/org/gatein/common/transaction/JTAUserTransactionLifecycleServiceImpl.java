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

import org.exoplatform.services.transaction.TransactionService;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.Status;
import javax.transaction.UserTransaction;

/**
 * Base implementation of {@link JTAUserTransactionLifecycleService} .
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class JTAUserTransactionLifecycleServiceImpl implements JTAUserTransactionLifecycleService
{
   private static final Logger log = LoggerFactory.getLogger( JTAUserTransactionLifecycleServiceImpl.class);

   private UserTransaction userTransaction;

   private TransactionService transactionService;

   public JTAUserTransactionLifecycleServiceImpl(TransactionService transactionService)
   {
      this.transactionService = transactionService;
   }

   /**
    * {@inheritDoc}
    */
   public void beginJTATransaction()
   {
      UserTransaction tx = getUserTransaction();

      try
      {
         if (tx.getStatus() == Status.STATUS_NO_TRANSACTION)
         {
            tx.begin();
         }
         else
         {
            log.warn("UserTransaction not started as it's in state " + tx.getStatus());
         }
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }


   /**
    * {@inheritDoc}
    */
   public void finishJTATransaction()
   {
      UserTransaction tx = getUserTransaction();

      try
      {
         int txStatus = tx.getStatus();
         if (txStatus == Status.STATUS_NO_TRANSACTION)
         {
            log.warn("UserTransaction can't be finished as it wasn't started");
         }
         else if (txStatus == Status.STATUS_MARKED_ROLLBACK || txStatus == Status.STATUS_ROLLEDBACK || txStatus == Status.STATUS_ROLLING_BACK)
         {
            log.warn("Going to rollback UserTransaction as it's status is " + txStatus);
            tx.rollback();
         }
         else
         {
            tx.commit();
         }
      }
      catch (Exception se)
      {
         throw new RuntimeException(se);
      }
   }


   /**
    * Obtain {@link UserTransaction} via JNDI call
    *
    * @return transaction
    */
   public UserTransaction getUserTransaction()
   {
      // It's fine to reuse same instance of UserTransaction as UserTransaction is singleton in JBoss and most other AS.
      // And new InitialContext().lookup("java:comp/UserTransaction") is quite expensive operation
      if (userTransaction == null)
      {
         synchronized (this)
         {
            if (userTransaction == null)
            {
               try
               {
                  userTransaction = (UserTransaction)new InitialContext().lookup("java:comp/UserTransaction");
               }
               catch (NamingException ne)
               {
                  log.debug("UserTransaction not found via JNDI. Trying TransactionService");
                  userTransaction = transactionService.getUserTransaction();
               }
            }
         }
      }
      return userTransaction;
   }

}
