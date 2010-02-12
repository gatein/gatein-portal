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

package org.exoplatform.management.invocation;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class SimpleMethodInvoker implements MethodInvoker
{

   /** The method we invoke. */
   private final Method method;

   public SimpleMethodInvoker(Method method)
   {
      if (method == null)
      {
         throw new NullPointerException();
      }

      //
      this.method = method;
   }

   public Object invoke(Object o, Map<String, List<String>> argMap) throws IllegalAccessException, InvocationTargetException
   {
      Class[] paramTypes = method.getParameterTypes();
      Object[] args = new Object[paramTypes.length];
      for (int i = 0;i < paramTypes.length;i++)
      {
         String argName = getArgumentName(i);
         List<String> argValues = argMap.get(argName);
         Class paramType = paramTypes[i];
         Object arg;
         if (paramType.isPrimitive())
         {
            throw new UnsupportedOperationException("Todo " + paramType);
         }
         else if (paramType.isArray())
         {
            throw new UnsupportedOperationException("Todo " + paramType);
         }
         else if (paramType == String.class)
         {
            arg = (argValues != null && argValues.size() > 0) ? argValues.get(0) : null;
         }
         else
         {
            throw new UnsupportedOperationException("Todo " + paramType);
         }
         args[i] = arg;
      }

      //
      return method.invoke(o, args);
   }

   protected abstract String getArgumentName(int index);
}