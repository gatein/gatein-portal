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

import org.exoplatform.management.invocation.MethodInvoker;
import org.exoplatform.management.invocation.SimpleMethodInvoker;
import org.exoplatform.management.spi.ManagedMethodMetaData;
import org.exoplatform.management.spi.ManagedMethodParameterMetaData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class RestResourceMethod
{

   /** . */
   final ManagedMethodMetaData metaData;

   /** . */
   final List<RestResourceMethodParameter> parameters;

   /** . */
   final Set<String> parameterNames;

   /** . */
   final MethodInvoker methodInvoker;

   public RestResourceMethod(ManagedMethodMetaData metaData)
   {
      List<RestResourceMethodParameter> parameters = new ArrayList<RestResourceMethodParameter>();
      Set<String> parameterNames = new HashSet<String>();
      for (ManagedMethodParameterMetaData parameterMD : metaData.getParameters())
      {
         parameters.add(new RestResourceMethodParameter(parameterMD));
         parameterNames.add(parameterMD.getName());
      }

      //
      this.metaData = metaData;
      this.parameterNames = Collections.unmodifiableSet(parameterNames);
      this.parameters = Collections.unmodifiableList(parameters);
      this.methodInvoker = new SimpleMethodInvoker(metaData.getMethod())
      {
         @Override
         protected String getArgumentName(int index)
         {
            RestResourceMethodParameter param = RestResourceMethod.this.parameters.get(index);
            return param != null ? param.getName() : null;
         }
      };
   }

   public String getName()
   {
      return metaData.getName();
   }

   public String getMethod()
   {
      switch (metaData.getImpact())
      {
         case READ:
            return "get";
         case WRITE:
            return "post";
         case IDEMPOTENT_WRITE:
            return "put";
         default:
            throw new AssertionError();
      }
   }

   public String getDescription()
   {
      return metaData.getDescription();
   }

   public List<RestResourceMethodParameter> getParameters()
   {
      return parameters;
   }

   // Internal *********************************************************************************************************

   MethodInvoker getMethodInvoker()
   {
      return methodInvoker;
   }
}
