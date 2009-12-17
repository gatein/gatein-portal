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
package org.exoplatform.portal.pom.config;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TaskExecutionDecorator implements TaskExecutor
{

   /** . */
   private final TaskExecutor next;

   public TaskExecutionDecorator(TaskExecutor next)
   {
      this.next = next;
   }

   public <V> V execute(POMSession session, POMTask<V> task) throws Exception
   {
      return next.execute(session, task);
   }

   public <E extends TaskExecutor> E getDecorator(Class<E> decoratorClass)
   {
      if (decoratorClass.isInstance(this))
      {
         return decoratorClass.cast(this);
      }
      else
      {
         if (next != null && next instanceof TaskExecutionDecorator)
         {
            return ((TaskExecutionDecorator)next).getDecorator(decoratorClass);
         }
         else
         {
            return null;
         }
      }
   }
}
