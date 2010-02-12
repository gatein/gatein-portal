/*
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

package org.exoplatform.management.data;

import org.exoplatform.management.invocation.GetterInvoker;
import org.exoplatform.management.invocation.MethodInvoker;
import org.exoplatform.management.invocation.NoSuchMethodInvoker;
import org.exoplatform.management.invocation.SetterInvoker;
import org.exoplatform.management.spi.ManagedPropertyMetaData;

import java.lang.reflect.Method;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class RestResourceProperty
{

   /** . */
   final ManagedPropertyMetaData metaData;

   /** . */
   private final MethodInvoker setterInvoker;

   /** . */
   private final MethodInvoker getterInvoker;

   public RestResourceProperty(ManagedPropertyMetaData metaData)
   {
      Method getter = metaData.getGetter();
      MethodInvoker getterInvoker = getter != null ? new GetterInvoker(getter) : new NoSuchMethodInvoker();

      //
      Method setter = metaData.getSetter();
      MethodInvoker setterInvoker = setter != null ? new SetterInvoker(setter) : new NoSuchMethodInvoker();

      //
      this.metaData = metaData;
      this.setterInvoker = setterInvoker;
      this.getterInvoker = getterInvoker;
   }

   public String getName()
   {
      return metaData.getName();
   }

   public String getDescription()
   {
      return metaData.getDescription();
   }

   // Internal *********************************************************************************************************

   MethodInvoker getSetterInvoker()
   {
      return setterInvoker;
   }

   MethodInvoker getGetterInvoker()
   {
      return getterInvoker;
   }
}
