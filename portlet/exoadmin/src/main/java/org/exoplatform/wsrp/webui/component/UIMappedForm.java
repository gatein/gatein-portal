/*
 * JBoss, a division of Red Hat
 * Copyright 2010, Red Hat Middleware, LLC, and individual
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

package org.exoplatform.wsrp.webui.component;

import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormInputSet;
import org.exoplatform.webui.form.UIFormStringInput;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class UIMappedForm extends UIForm
{
   private static final Class<?>[] EMPTY_ARGS = new Class<?>[]{};

   public void setBackingBean(Object bean) throws Exception
   {
      introspectAndBuild(bean, this);
      invokeSetBindingBean(bean);
   }

   private void introspectAndBuild(Object bean, UIContainer container) throws IllegalAccessException, InvocationTargetException
   {
      Class<? extends Object> beanClass = bean.getClass();
      Method[] methods = beanClass.getDeclaredMethods();
      for (Method method : methods)
      {
         Class<?> type = method.getReturnType();
         String name = method.getName();
         String fieldName = getFieldNameOrNullFrom(name, method, beanClass);
         if (fieldName != null)
         {
            Object beanValue = method.invoke(bean, null);
            if (String.class.isAssignableFrom(type))
            {
               container.addChild(new UIFormStringInput(name, name, beanValue == null ? null : beanValue.toString()));
            }
            else if (Boolean.class.isAssignableFrom(type))
            {
               container.addChild(new UIFormCheckBoxInput(name, name, beanValue));
            }
            else
            {
               UIFormInputSet input = new UIFormInputSet(name);
               container.addChild(input);
               introspectAndBuild(beanValue, input);
            }
         }
      }
   }

   private String getFieldNameOrNullFrom(String name, Method method, Class<?> beanClass)
   {
      int index = name.indexOf("get");
      int endIndex = 3;
      if (index != 0)
      {
         index = name.indexOf("is");
         endIndex = 2;
      }

      if (index == 0 && method.getParameterTypes().length == 0)
      {
         name = name.substring(endIndex);
         Class<?> type = method.getReturnType();

         try
         {
            // check if there's a get method associated to the setter
            beanClass.getDeclaredMethod("set" + name, type);
         }
         catch (NoSuchMethodException e)
         {
            return null;
         }
         return name;
      }
      else
      {
         return null;
      }
   }
}
