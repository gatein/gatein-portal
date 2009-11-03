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

import org.chromattic.api.ChromatticSession;
import org.chromattic.api.UndeclaredRepositoryException;
import org.exoplatform.portal.application.PortletPreferences;
import org.exoplatform.portal.pom.data.Mapper;
import org.gatein.mop.api.Model;
import org.gatein.mop.api.content.Customization;
import org.gatein.mop.api.workspace.ObjectType;
import org.gatein.mop.api.workspace.Site;
import org.gatein.mop.api.workspace.Workspace;
import org.gatein.mop.api.workspace.WorkspaceObject;
import org.gatein.mop.core.api.MOPFormatter;
import org.gatein.mop.core.api.ModelImpl;
import org.gatein.mop.core.api.workspace.NavigationImpl;
import org.gatein.mop.core.api.workspace.PageImpl;

import javax.jcr.RepositoryException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class POMSession
{

   /** . */
   final POMSessionManager mgr;

   /** . */
   private ModelImpl model;

   /** . */
   private boolean isInTask;

   /** Hack. */
   private final Map<String, PortletPreferences> pendingPrefs = new HashMap<String, PortletPreferences>();

   /** . */
   private boolean markedForRollback;

   public POMSession(POMSessionManager mgr)
   {
      this.mgr = mgr;
      this.isInTask = false;
      this.markedForRollback = false;
   }

   private Model getModel()
   {
      if (model == null)
      {
         model = mgr.getPOMService().getModel();
      }
      return model;
   }

   // julien todo : investigate how expensive is the call to hasPendingChanges method
   public boolean isModified()
   {
      try
      {
         return getSession().getJCRSession().hasPendingChanges();
      }
      catch (RepositoryException e)
      {
         throw new UndeclaredRepositoryException(e);
      }
   }

  private ChromatticSession getSession() {
    try {
      Model model = getModel();
      Field f = model.getClass().getDeclaredField("session");
      f.setAccessible(true);
      return (ChromatticSession)f.get(model);
    }
    catch (Exception e) {
      throw new Error(e);
    }
  }

   public Workspace getWorkspace()
   {
      return getModel().getWorkspace();
   }

   public boolean isMarkedForRollback()
   {
      return markedForRollback;
   }

   public String pathOf(WorkspaceObject o)
   {
      return getModel().pathOf(o);
   }

   public <O extends WorkspaceObject> Iterator<O> findObject(ObjectType<O> ownerType, String statement)
   {
      this.save();
      return getModel().findObject(ownerType, statement);
   }

   public <O extends WorkspaceObject> O findObjectById(ObjectType<O> ownerType, String id)
   {
      return getModel().findObjectById(ownerType, id);
   }

   public WorkspaceObject findObjectById(String id)
   {
      return findObjectById(ObjectType.ANY, id);
   }

   public Customization<?> findCustomizationById(String id)
   {
      return getModel().findCustomizationById(id);
   }

   public void addPortletPreferences(PortletPreferences prefs)
   {
      pendingPrefs.put(prefs.getWindowId(), prefs);
   }

   public PortletPreferences getPortletPreferences(String id)
   {
      return pendingPrefs.get(id);
   }

   public Set<PortletPreferences> getPortletPreferences(Site site)
   {
      Set<PortletPreferences> prefs = new HashSet<PortletPreferences>();
      for (Iterator<Map.Entry<String, PortletPreferences>> i = pendingPrefs.entrySet().iterator(); i.hasNext();)
      {
         Map.Entry<String, PortletPreferences> entry = i.next();
         String prefix = Mapper.getOwnerType(site.getObjectType()) + "#" + site.getName() + ":/";
         if (entry.getKey().startsWith(prefix))
         {
            prefs.add(entry.getValue());
            i.remove();
         }
      }
      return prefs;
   }

   public <O extends WorkspaceObject> Iterator<O> findObjects(ObjectType<O> type, ObjectType<? extends Site> siteType,
      String ownerId, String title)
   {
      this.save();
      //
      String ownerIdChunk = ownerId != null ? new MOPFormatter().encodeNodeName(null, ownerId) : "%";

      //
      String ownerTypeChunk;
      if (siteType != null)
      {
         if (siteType == ObjectType.PORTAL_SITE)
         {
            ownerTypeChunk = "portalsites";
         }
         else if (siteType == ObjectType.GROUP_SITE)
         {
            ownerTypeChunk = "groupsites";
         }
         else
         {
            ownerTypeChunk = "usersites";
         }
      }
      else
      {
         ownerTypeChunk = "%";
      }

      //
      Workspace workspace = getWorkspace();
      String workspaceChunk = model.pathOf(workspace);

      //
      String statement;
      if (siteType != null)
      {
         try
         {
            if (type == ObjectType.PAGE)
            {
               statement =
                  "jcr:path LIKE '" + workspaceChunk + "/" + ownerTypeChunk + "/" + ownerIdChunk
                     + "/rootpage/children/pages/children/%'";
            }
            else
            {
               statement =
                  "jcr:path LIKE '" + workspaceChunk + "/" + ownerTypeChunk + "/" + ownerIdChunk
                     + "/rootnavigation/children/default'";
            }
         }
         catch (IllegalArgumentException e)
         {
            if (type == ObjectType.PAGE)
            {
               statement = "jcr:path LIKE ''";
            }
            else
            {
               statement = "jcr:path LIKE ''";
            }
         }
      }
      else
      {
         if (title != null)
         {
            if (type == ObjectType.PAGE)
            {
               statement =
                  "jcr:path LIKE '" + workspaceChunk + "/" + ownerTypeChunk + "/" + ownerIdChunk
                     + "/rootpage/children/pages/children/%' AND mop:title='" + title + "'";
            }
            else
            {
               throw new UnsupportedOperationException();
            }
         }
         else
         {
            if (type == ObjectType.PAGE)
            {
               statement =
                  "jcr:path LIKE '" + workspaceChunk + "/" + ownerTypeChunk + "/" + ownerIdChunk
                     + "/rootpage/children/pages/children/%'";
            }
            else
            {
               statement =
                  "jcr:path LIKE '" + workspaceChunk + "/" + ownerTypeChunk + "/" + ownerIdChunk
                     + "/rootnavigation/children/default'";
            }
         }
      }

      // Temporary work around, to fix in MOP and then remove
      ChromatticSession session;
      try
      {
         Field f = ModelImpl.class.getDeclaredField("session");
         f.setAccessible(true);
         session = (ChromatticSession)f.get(model);
      }
      catch (Exception e)
      {
         throw new Error(e);
      }
      Class<O> mappedClass = (Class<O>)mapping.get(type);
      return session.createQueryBuilder().from(mappedClass).<O> where(statement).get().iterator();
   }

   private static final Map<ObjectType<?>, Class> mapping = new HashMap<ObjectType<?>, Class>();

   static
   {
      mapping.put(ObjectType.PAGE, PageImpl.class);
      mapping.put(ObjectType.NAVIGATION, NavigationImpl.class);
   }

   public void execute(POMTask task) throws Exception
   {
      if (isInTask)
      {
         throw new IllegalStateException();
      }

      //
      boolean needRollback = true;
      try
      {
         isInTask = true;
         task.run(this);
         needRollback = false;
      }
      finally
      {
         isInTask = false;
         markedForRollback = needRollback;
      }
   }

   public void save()
   {
      if (model != null)
      {
         if (!markedForRollback)
         {
            model.save();
         }
         else
         {
            System.out.println("Will not save session that is marked for rollback");
         }
      }
   }

   void close()
   {
      if (model != null)
      {
         model.close();
      }
   }
}
