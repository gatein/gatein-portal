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

package org.exoplatform.webui;

import org.exoplatform.webui.config.InitParams;

/**
 * Created by The eXo Platform SAS
 * May 10, 2006
 * 
 * A utility class that provides static methods to create new objects, of a given type
 */
public class Util
{
   /**
    * The default parameters given to the constructor
    */
   static Class<?>[] CONSTRUCTOR_PARAMS = {InitParams.class};

   /**
    * 
    * @param type The type of the object to create, given as a String
    * @param params The parameters to give to the constructor
    * @return A new object of the given type
    * @throws Exception
    */
   static public Object createObject(String type, InitParams params) throws Exception
   {
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      Class<?> clazz = cl.loadClass(type);
      return createObject(clazz, params);
   }

   /**
    * 
    * @param <T> The type of the object to create
    * @param type The type parameter given as a Class object
    * @param params The parameters to give to the constructor
    * @return The object of type T
    * @throws Exception
    */
   static public <T> T createObject(Class<T> type, InitParams params) throws Exception
   {
      if (params == null)
      {
         return type.getConstructor().newInstance();
      }
      Object[] args = {params};
      return type.getConstructor(CONSTRUCTOR_PARAMS).newInstance(args);
   }
}
