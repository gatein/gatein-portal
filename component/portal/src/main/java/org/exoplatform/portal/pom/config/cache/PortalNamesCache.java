/*
 * Copyright (C) 2010 eXo Platform SAS.
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

package org.exoplatform.portal.pom.config.cache;

import org.exoplatform.commons.utils.LazyPageList;
import org.exoplatform.commons.utils.ListAccessImpl;
import org.exoplatform.portal.pom.config.POMSession;
import org.exoplatform.portal.pom.config.POMTask;
import org.exoplatform.portal.pom.config.TaskExecutionDecorator;
import org.exoplatform.portal.pom.config.TaskExecutor;
import org.exoplatform.portal.pom.config.tasks.PortalConfigTask;
import org.exoplatform.portal.pom.config.tasks.SearchTask;
import org.exoplatform.portal.pom.data.PortalKey;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class PortalNamesCache extends TaskExecutionDecorator
{

   public PortalNamesCache(TaskExecutor next)
   {
      super(next);
   }

   @Override
   public <V> V execute(POMSession session, POMTask<V> task) throws Exception
   {
      if (!session.isModified())
      {
         if (task instanceof SearchTask.FindSiteKey)
         {
            List<PortalKey> data = (List<PortalKey>)session.getFromCache(SearchTask.FindSiteKey.class);
            if (data == null)
            {
               V result = super.execute(session, task);
               LazyPageList<PortalKey> list = (LazyPageList<PortalKey>)result;
               session.putInCache(SearchTask.FindSiteKey.class, Collections.unmodifiableList(new ArrayList<PortalKey>(list.getAll())));
               return result;
            }
            else
            {
               return (V)new LazyPageList<PortalKey>(new ListAccessImpl<PortalKey>(PortalKey.class, data), 10);
            }
         }
         else if (task instanceof PortalConfigTask.Save || task instanceof PortalConfigTask.Remove)
         {
            V result = super.execute(session, task);
            session.scheduleForEviction(SearchTask.FindSiteKey.class);
            return result;
         }
      }

      //
      return super.execute(session, task);
   }
}
