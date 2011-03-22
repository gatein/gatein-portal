/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.gatein.htmlunit.jira;

import org.testng.annotations.Test;

/**
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class Test_GTNPORTAL_1823_FailToCreatePageNavNode extends Test_GTNPORTAL_1823_FailToCreatePage
{

   /**
    * This test method relies on TestNG parallel test execution facility
    * to perform two concurrent executions of this method on a single instance of the test class
    *
    * @throws Throwable if test fails
    */
   @Test(invocationCount = TCOUNT, threadPoolSize = TCOUNT, groups = {"GateIn", "jira", "htmlunit"})
   public void testMain() throws Throwable
   {
      try
      {
         test(true);
      }
      finally
      {
         // If exception occurs we don't want the other thread to lock up
         // - so we perform another countDown()
         sync.countDown();
      }
   }
}