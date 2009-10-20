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

package org.exoplatform.services.common;

import java.lang.ref.SoftReference;
import java.lang.reflect.Constructor;

/**
 *  Author : Nhu Dinh Thuan
 *          Email:nhudinhthuan@yahoo.com
 * Sep 19, 2006
 */
public class ThreadSoftRef<T> extends ThreadLocal<SoftReference<T>>
{

   private Class<T> clazz;

   @SuppressWarnings("unchecked")
   public ThreadSoftRef(Class<?> clazz)
   {
      this.clazz = (Class<T>)clazz;
   }

   public T getRef()
   {
      SoftReference<T> sr = get();
      if (sr == null || sr.get() == null)
      {
         try
         {
            Constructor<T> constructor = clazz.getDeclaredConstructor(new Class[]{});
            constructor.setAccessible(true);
            sr = new SoftReference<T>(constructor.newInstance(new Object[]{}));
         }
         catch (Exception exp)
         {
            exp.printStackTrace();
         }
         set(sr);
      }
      return sr.get();
   }

}
