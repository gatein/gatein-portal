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

import java.util.AbstractList;

/**
 *  Author : Nhu Dinh Thuan
 *          Email:nhudinhthuan@yahoo.com
 * Jul 30, 2006
 * 
 * backup
 * 
 */
public abstract class Holder<T> extends AbstractList<T>
{

   protected T[] array = null;

   protected ClassConfig config;

   public Holder()
   {
      config = Factory.createBean(getClass());
   }

   public T get(int index)
   {
      return array[index];
   }

   public int size()
   {
      return array.length;
   }

   public static class Factory
   {

      private static Class<?> cacheDefaultClass = null;

      private static ClassConfig cacheClassConfig = null;

      public static <T> ClassConfig createBean(Class<T> clazz)
      {
         if (cacheDefaultClass == clazz)
            return cacheClassConfig;
         cacheClassConfig = clazz.getAnnotation(ClassConfig.class);
         cacheDefaultClass = clazz;
         return cacheClassConfig;
      }

      public static <T> void class2Object(Class<T> defaultClass, T[] array)
      {
         for (int i = 0; i < cacheClassConfig.classes().length; i++)
         {
            try
            {
               array[i] = defaultClass.cast(cacheClassConfig.classes()[i].newInstance());
            }
            catch (Exception e)
            {
               e.printStackTrace();
            }
         }
      }

   }

}
