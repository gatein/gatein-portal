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

package org.exoplatform.services.organization;

import org.exoplatform.services.organization.idm.UserTransactionJtaPlatform;

import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import java.lang.reflect.Method;

/**
 * JTA platform implementation used in standalone JTS environment without JNDI
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class StandaloneTestJtaPlatform extends UserTransactionJtaPlatform
{
   private Method manager, user;

   public StandaloneTestJtaPlatform()
   {
      try
      {
         manager = Class.forName("com.arjuna.ats.jta.TransactionManager").getMethod("transactionManager");
         user = Class.forName("com.arjuna.ats.jta.UserTransaction").getMethod("userTransaction");
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   @Override
   protected TransactionManager locateTransactionManager()
   {
      try
      {
         return (TransactionManager) manager.invoke(null);
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   @Override
   protected UserTransaction locateUserTransaction()
   {
      try
      {
         return (UserTransaction) user.invoke(null);
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }
}
