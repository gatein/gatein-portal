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
package org.exoplatform.commons.chromattic;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 * @param <V> the return type value
 */
public abstract class ContextualTask<V>
{

   /**
    * Executes a task within a context from the specified life cycle. If an existing context already exists
    * then this context is used otherwise a context is managed for the duration of the {@link #execute(SessionContext)}
    * method.
    *
    * @param lifeCycle the life cycle
    * @return a value
    */
   public final V executeWith(ChromatticLifeCycle lifeCycle)
   {
      SessionContext context = lifeCycle.getContext(true);
      if (context == null)
      {
         context = lifeCycle.openContext();
         try
         {
            return execute(context);
         }
         finally
         {
            lifeCycle.closeContext(true);
         }
      }
      else
      {
         return execute(context);
      }
   }

   /**
    * Implementor must provide the task logic here.
    *
    * @param context the context
    * @return a value
    */
   protected abstract V execute(SessionContext context);

}
