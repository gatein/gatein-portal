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
import org.chromattic.api.UndeclaredRepositoryException;
import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.*;

/**
 * <p>The chromattic life cycle objets is a plugin that allow to bootstrap a chromattic builder and make
 * it managed either locally or globally.</p>
 *
 * <p>It is allowed to create subclasses of this class to override the methods {@link #onOpenSession(SessionContext)}
 * or {@link #onCloseSession(SessionContext)} to perform additional treatment on the session context at a precise
 * phase of its life cycle.</p>
 *
 * <p>The life cycle domain uniquely identifies the chromattic domain among all domain registered against the
 * {@link org.exoplatform.commons.chromattic.ChromatticManager} manager.</p>
 *
 * <p>The plugin takes an instance of {@link org.exoplatform.container.xml.InitParams} as parameter that contains
 * the following entries:
 *
 * <ul>
 * <li>The <code>domain-name</code> string that is the life cycle domain name</li>
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
   private static final Map<String, ChromatticBuilder.Option<?>> RECOGNIZED_OPTIONS;

   static
   {
      Map<String, ChromatticBuilder.Option<?>> options = new HashMap<String, ChromatticBuilder.Option<?>>();
      for (ChromatticBuilder.Option<?> option : new ChromatticBuilder.Option<?>[]{ChromatticBuilder.JCR_OPTIMIZE_ENABLED,
         ChromatticBuilder.JCR_OPTIMIZE_HAS_NODE_ENABLED,
         ChromatticBuilder.JCR_OPTIMIZE_HAS_PROPERTY_ENABLED,
         ChromatticBuilder.OBJECT_FORMATTER_CLASSNAME,
         ChromatticBuilder.ROOT_NODE_PATH,
         ChromatticBuilder.CREATE_ROOT_NODE})
      {
         options.put(option.getName(), option);
      }
      RECOGNIZED_OPTIONS = options;
   }

   /** . */
   private final String domainName;

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

   /** . */
   final Logger log = LoggerFactory.getLogger(ChromatticLifeCycle.class);

   /** . */
   private final Map<String, String> optionMap;

   public ChromatticLifeCycle(InitParams params)
   {
      Map<String, String> options = new HashMap<String, String>();
      PropertiesParam pp = params.getPropertiesParam("options");
      if (pp != null)
      {
         options.putAll(pp.getProperties());
      }

      this.domainName = params.getValueParam("domain-name").getValue();
      this.workspaceName = params.getValueParam("workspace-name").getValue();
      this.entityClassNames = params.getValuesParam("entities").getValues();
      this.optionMap = options;
   }

   public String getDomainName()
   {
      return domainName;
   }

   public final String getRepositoryName()
   {
      try
      {
         return manager.repositoryService.getCurrentRepository().getConfiguration().getName();
      }
      catch (RepositoryException e)
      {
         throw new UndeclaredRepositoryException("JCR exceptions are really bad", e);
      }
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

   AbstractContext getLoginContext()
   {
      Synchronization sync = manager.getSynchronization();

      //
      if (sync != null)
      {
         return sync.getContext(domainName);
      }

      //
      return currentContext.get();
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
      log.trace("Requesting context");
      Synchronization sync = manager.getSynchronization();

      //
      if (sync != null)
      {
         log.trace("Found synchronization about to get the current context for chromattic " + domainName);
         SynchronizedContext context = sync.getContext(domainName);

         //
         if (context == null && !peek)
         {
            log.trace("No current context found, about to open one");
            context = sync.openContext(this);
         }
         else
         {
            log.trace("Found a context and will return it");
         }

         //
         return context;
      }

      //
      log.trace("No active synchronization about to try the current local context");
      LocalContext localContext = currentContext.get();
      log.trace("Found local context " + localContext);

      //
      return localContext;
   }

   final SessionContext openSynchronizedContext()
   {
      log.trace("Opening a global context");
      AbstractContext context = (AbstractContext)getContext(true);

      //
      if (context != null)
      {
         String msg = "A global context is already opened";
         log.trace(msg);
         throw new IllegalStateException(msg);
      }

      // Attempt to get the synchronization
      log.trace("Ok, no global context found, asking current synchronization");
      Synchronization sync = manager.getSynchronization();

      //
      if (sync == null)
      {
         String msg = "Need global synchronization for opening a global context";
         log.trace(msg);
         throw new IllegalStateException(msg);
      }

      //
      log.trace("Opening a global context for the related sync");
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
      log.trace("Opening a context");
      AbstractContext context = (AbstractContext)getContext(true);

      //
      if (context != null)
      {
         String msg = "A context is already opened";
         log.trace(msg);
         throw new IllegalStateException(msg);
      }

      //
      log.trace("Ok, no context found, asking current synchronization");
      Synchronization sync = manager.getSynchronization();

      //
      if (sync != null)
      {
         log.trace("Found a synchronization, about to open a global context");
         context = sync.openContext(this);
      }
      else
      {
         log.trace("Not synchronization found, about to a local context");
         LocalContext localContext = new LocalContext(this);
         currentContext.set(localContext);
         onOpenSession(localContext);
         context = localContext;
      }

      //
      return context;
   }

   public final void closeContext(boolean save)
   {
      log.trace("Requesting for context close with save=" + save + " going to look for any context");
      AbstractContext context = (AbstractContext)getContext(true);

      //
      if (context == null)
      {
         String msg = "Cannot close non existing context";
         log.trace(msg);
         throw new IllegalStateException(msg);
      }

      //
      context.close(save);
   }

   protected void onOpenSession(SessionContext context)
   {
   }

   protected void onCloseSession(SessionContext context)
   {
   }

   Session doLogin() throws RepositoryException
   {
      AbstractContext loginContext = getLoginContext();

      //
      if (loginContext == null)
      {
         throw new IllegalStateException("Could not obtain a login context");
      }

      //
      return loginContext.doLogin();
   }

   public final void start() throws Exception
   {
      log.debug("About to setup Chromattic life cycle " + domainName);
      ChromatticBuilder builder = ChromatticBuilder.create();

      //
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      for (String className : entityClassNames)
      {
         String fqn = className.trim();
         log.debug("Adding class " + fqn + " to life cycle " + domainName);
         Class<?> entityClass = cl.loadClass(fqn);
         builder.add(entityClass);
      }

      // Set up boot context
      PortalSessionLifeCycle.bootContext.set(this);

      //
      try
      {
         for (Map.Entry<String, String> optionEntry : optionMap.entrySet())
         {
            ChromatticBuilder.Option<?> option = RECOGNIZED_OPTIONS.get(optionEntry.getKey());
            if (option != null)
            {
               log.debug("Setting Chromattic option " + optionEntry);
               setOption(builder, option, optionEntry.getValue());
            }
         }

         // Set it now, so we are sure that it will be the correct life cycle
         builder.setOptionValue(ChromatticBuilder.SESSION_LIFECYCLE_CLASSNAME, PortalSessionLifeCycle.class.getName());

         //
         log.debug("Building Chromattic " + domainName);
         realChromattic = builder.build();
         chromattic = new ChromatticImpl(this);
      }
      catch (Exception e)
      {
         log.error("Could not start Chromattic " + domainName, e);
      }
      finally
      {
         PortalSessionLifeCycle.bootContext.set(null);
      }
   }

   private <D> void setOption(ChromatticBuilder builder, ChromatticBuilder.Option<D> option, String value)
   {
      log.debug("Setting Chromattic option " + option.getDisplayName());
      builder.setOptionValue(option, option.getInstance(value).getValue());
   }

   public final void stop()
   {
      // Nothing to do for now
   }
}
