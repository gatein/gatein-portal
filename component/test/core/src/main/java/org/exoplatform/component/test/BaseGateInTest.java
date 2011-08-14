/*
 * Copyright (C) 2010 eXo Platform SAS.
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

package org.exoplatform.component.test;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class BaseGateInTest extends TestCase
{

   public BaseGateInTest()
   {
   }

   public BaseGateInTest(String name)
   {
      super(name);
   }

   public static <T> T assertInstanceOf(Object o, Class<T> expectedType)
   {
      if (expectedType != null)
      {
         if (expectedType.isInstance(o))
         {
            fail();
            return null;
         }
         else
         {
            return expectedType.cast(o);
         }
      }
      else
      {
         fail("Need an expected type");
         return null;
      }
   }

   public static <T> T fail(String msg, Throwable t)
   {
      AssertionFailedError afe = new AssertionFailedError(msg);
      afe.initCause(t);
      throw afe;
   }

   public static <T> T fail(Throwable t)
   {
      AssertionFailedError afe = new AssertionFailedError();
      afe.initCause(t);
      throw afe;
   }
}
