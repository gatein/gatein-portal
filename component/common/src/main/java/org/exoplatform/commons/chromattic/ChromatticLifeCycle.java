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
package org.exoplatform.commons.chromattic;

import org.chromattic.api.Chromattic;
import org.chromattic.api.ChromatticBuilder;
import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;

import java.util.List;

/**
 * <p>The chromattic life cycle objets is a plugin that allow to bootstrap a chromattic builder and make
 * it managed either locally or globally.</p>
 *
 * <p>It is allowed to create subclasses of this class to override the methods {@link #onOpenSession(SessionContext)}
 * or {@link #onCloseSession(SessionContext)} to perform additional treatment on the session context at a precise
 * phase of its life cycle.</p>
 *
 * <p>The life cycle name uniquely identifies the chromattic domain among all domain registered against the
 * {@link org.exoplatform.commons.chromattic.ChromatticManager} manager.</p>
 *
 * <p>The plugin takes an instance of {@link org.exoplatform.container.xml.InitParams} as parameter that contains
 * the following entries:
 *
 * <ul>
 * <li>The <code>name</code> string that is the life cycle name</li>
 * <li>The <code>workspace-name</code> string that is the repository workspace name associated with this life cycle</li>
 * <li>The <code>entities</code> list value that contains the list of chromattic entities that will be registered
 * against the builder chromattic builder</li>
 * </ul>
 * </p>
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ChromatticLifeCycle extends BaseComponentPlugin
{

   /** . */
   private final String name;

   /** . */
   private final String workspaceName;

   /** . */
   Chromattic realChromattic;

   /** . */
   private ChromatticImpl chromattic;

   /** . */
   ChromatticManager manager;

   /** . */
   private final List<String> entityClassNames;

   /** . */
   final ThreadLocal<LocalContext> currentContext = new ThreadLocal<LocalContext>();

   public ChromatticLifeCycle(InitParams params)
   {
      this.name = params.getValueParam("name").getValue();
      this.workspaceName = params.getValueParam("workspace-name").getValue();
      this.entityClassNames = params.getValuesParam("entities").getValues();
   }

   public final String getName()
   {
      return name;
   }

   public final String getWorkspaceName()
   {
      return workspaceName;
   }

   public final Chromattic getChromattic()
   {
      return chromattic;
   }

   public final ChromatticManager getManager()
   {
      return manager;
   }

   /**
    * Returns <code>#getContext(false)</code>.
    *
    * @see #getContext(boolean)
    * @return a session context
    */
   public final SessionContext getContext()
   {
      return getContext(false);
   }

   /**
    * A best effort to return a session context whether it's local or global.
    *
    * @param peek true if no context should be automatically created
    * @return a session context
    */
   public final SessionContext getContext(boolean peek)
   {
      Synchronization sync = manager.getSynchronization();

      //
      if (sync != null)
      {
         GlobalContext context = sync.getContext(name);

         //
         if (context == null && !peek)
         {
            context = sync.openContext(this);
         }

         //
         return context;
      }

      //
      return currentContext.get();
   }

   LoginContext getLoginContext()
   {
      Synchronization sync = manager.getSynchronization();

      //
      if (sync != null)
      {
         return sync;
      }

      //
      return currentContext.get();
   }

   final SessionContext openGlobalContext()
   {
      AbstractContext context = (AbstractContext)getContext(true);

      //
      if (context != null)
      {
         throw new IllegalStateException("A session is already opened.");
      }

      //
      Synchronization sync = manager.getSynchronization();

      //
      if (sync == null)
      {
         throw new IllegalStateException("Need global synchronization");
      }

      //
      return sync.openContext(this);
   }

   /**
    * Opens a context and returns it. If there is a global ongoing synchronization then the context will be
    * scoped to that synchronization, otherwise it will be a local context.
    *
    * @return the session context
    */
   public final SessionContext openContext()
   {
      AbstractContext context = (AbstractContext)getContext(true);

      //
      if (context != null)
      {
         throw new IllegalStateException("A session is already opened.");
      }

      //
      Synchronization sync = manager.getSynchronization();

      //
      if (sync != null)
      {
         context = sync.openContext(this);
      }
      else
      {
         LocalContext localContext = new LocalContext(this);
         currentContext.set(localContext);
         onOpenSession(localContext);
         context = localContext;
      }

      //
      return context;
   }

   public final void closeContext(SessionContext context, boolean save)
   {
      AbstractContext abstractContext = (AbstractContext)context;

      //
      ((AbstractContext)context).close(save);
   }

   protected void onOpenSession(SessionContext context)
   {
   }

   protected void onCloseSession(SessionContext context)
   {
   }

   public final void start() throws Exception
   {
      ChromatticBuilder builder = ChromatticBuilder.create();

      //
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      for (String className : entityClassNames)
      {
         Class<?> entityClass = cl.loadClass(className);
         builder.add(entityClass);
      }

      // Set up boot context
      LifeCycleContext.bootContext.set(new LifeCycleContext(this, manager, workspaceName));

      //
      try
      {
         // Set it now, so we are sure that it will be the correct life cycle
         builder.setOption(ChromatticBuilder.SESSION_LIFECYCLE_CLASSNAME, PortalSessionLifeCycle.class.getName());

         //
         realChromattic = builder.build();
         chromattic = new ChromatticImpl(this);
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
      finally
      {
         LifeCycleContext.bootContext.set(null);
      }
   }

   public final void stop()
   {
      // Nothing to do for now
   }
}
