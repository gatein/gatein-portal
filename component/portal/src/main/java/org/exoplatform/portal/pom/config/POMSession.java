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
import org.exoplatform.commons.chromattic.SessionContext;
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
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
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

   /** . */
   private List<Serializable> staleKeys;

   /** . */
   private boolean modified;

   /** . */
   private SessionContext context;

   /** . */
   private MOPChromatticLifeCycle configurator;

   public POMSession(POMSessionManager mgr, MOPChromatticLifeCycle configurator, SessionContext context)
   {
      this.mgr = mgr;
      this.isInTask = false;
      this.markedForRollback = false;
      this.staleKeys = null;
      this.configurator = configurator;
      this.context = context;
   }

   public Object getFromCache(Serializable key)
   {
      if (isModified())
      {
         throw new IllegalStateException("Cannot read object in shared cache from a modified session");
      }
      return mgr.cache.get(key);
   }

   public void putInCache(Serializable key, Object value)
   {
      if (isModified())
      {
         throw new IllegalStateException("Cannot put object in shared cache from a modified session");
      }
      mgr.cache.put(key, value);
   }

   public void scheduleForEviction(Serializable key)
   {
      if (key == null)
      {
         throw new NullPointerException();
      }
      if (staleKeys == null)
      {
         staleKeys = new LinkedList<Serializable>();
      }
      staleKeys.add(key);
   }

   private Model getModel()
   {
      if (model == null)
      {
         model = mgr.getPOMService().getModel();
      }
      return model;
   }

   public boolean isModified()
   {
      if (modified)
      {
         return true;
      }
      try
      {
         modified = getSession().getJCRSession().hasPendingChanges();
      }
      catch (RepositoryException e)
      {
         throw new UndeclaredRepositoryException(e);
      }
      return modified;
   }

   protected ChromatticSession getSession()
   {
      return context.getSession();
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

   /**
    * <p>Closes the current session and discard the changes done during the session.</p>
    *
    * @see #close(boolean)
    */
   public void close()
   {
      close(false);
   }

   /**
    * <p>Closes the current session and optionally saves its content. If no session is associated then this method has
    * no effects and returns false.</p>
    *
    * @param save if the session must be saved
    */
   public void close(boolean save)
   {
      if (save)
      {
         save();
      }

      if (model != null)
      {
         model.close();
      }

      //
      configurator.closeContext(context, save & markedForRollback);

      //
      if (staleKeys != null)
      {
         for (Serializable key : staleKeys)
         {
            mgr.cache.remove(key);
         }
      }
   }
}
