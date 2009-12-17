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

package org.exoplatform.portal.pom.config.tasks;

import org.exoplatform.portal.pom.config.POMTask;
import org.exoplatform.portal.pom.data.DashboardData;
import org.exoplatform.portal.pom.data.Mapper;
import org.exoplatform.portal.pom.config.POMSession;
import org.gatein.mop.api.workspace.ObjectType;
import org.gatein.mop.api.workspace.ui.UIContainer;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class DashboardTask
{

   public static class Load implements POMTask<DashboardData>
   {

      /** . */
      protected final String storageId;

      public Load(String storageId)
      {
         this.storageId = storageId;
      }

      public DashboardData run(POMSession session) throws Exception
      {
         UIContainer container = session.findObjectById(ObjectType.CONTAINER, storageId);

         //
         if (container != null)
         {
            return new Mapper(session).loadDashboard(container);
         }

         //
         return null;
      }

      @Override
      public String toString()
      {
         return "DashboardTask.Load[id=" + storageId + "]";
      }
   }

   public static class Save implements POMTask<Void>
   {

      /** The dashboard object. */
      protected final DashboardData dashboard;

      public Save(DashboardData dashboard)
      {
         if (dashboard == null)
         {
            throw new NullPointerException("No null dashboard accepted");
         }
         if (dashboard.getStorageId() == null)
         {
            throw new IllegalArgumentException("No dashboard with null storage id accepted");
         }
         this.dashboard = dashboard;
      }

      public Void run(POMSession session) throws Exception
      {
         String id = dashboard.getStorageId();
         if (id == null)
         {
            throw new IllegalArgumentException();
         }

         //
         UIContainer container = session.findObjectById(ObjectType.CONTAINER, id);
         if (container == null)
         {
            throw new IllegalArgumentException();
         }

         //
         Mapper mapper = new Mapper(session);

         //
         mapper.saveDashboard(dashboard, container);

         //
         return null;
      }

      @Override
      public String toString()
      {
         return "DashboardTask.Save[id=" + dashboard.getStorageId() + "]";
      }
   }
}
