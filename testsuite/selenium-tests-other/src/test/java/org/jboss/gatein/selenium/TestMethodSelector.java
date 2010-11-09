/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.gatein.selenium;


import org.apache.commons.lang.StringUtils;
import org.testng.IAnnotationTransformer;
import org.testng.annotations.ITestAnnotation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class TestMethodSelector implements IAnnotationTransformer
{

   /**
    * Disables the test methods which doesn't match the given method name.
    */
   @SuppressWarnings("unchecked")
   public void transform(ITestAnnotation annotation, Class testClass, Constructor testConstructor, Method testMethod)
   {
      String[] selectedMethods = System.getProperty("method", "*").split(",");

      String methodName = testMethod.getDeclaringClass().getCanonicalName() + "." + testMethod.getName();
      boolean match = false;

      for (String selectedMethod : selectedMethods)
      {

         selectedMethod = StringUtils.replace(selectedMethod, "*", ".*");

         if (methodName.matches(selectedMethod))
         {
            match = true;
            break;
         }
      }

      annotation.setEnabled(match);

   }
}
