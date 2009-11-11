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

import org.chromattic.api.ChromatticBuilder;
import org.chromattic.api.ChromatticSession;
import org.chromattic.apt.InstrumentorImpl;
import org.exoplatform.portal.pom.registry.CategoryDefinition;
import org.exoplatform.portal.pom.registry.ContentDefinition;
import org.exoplatform.portal.pom.registry.ContentRegistry;
import org.exoplatform.portal.pom.spi.gadget.Gadget;
import org.exoplatform.portal.pom.spi.gadget.GadgetContentProvider;
import org.exoplatform.portal.pom.spi.gadget.GadgetState;
import org.exoplatform.portal.pom.spi.portlet.Portlet;
import org.exoplatform.portal.pom.spi.portlet.PortletContentProvider;
import org.exoplatform.portal.pom.spi.portlet.PreferenceState;
import org.exoplatform.portal.pom.spi.portlet.PortletState;
import org.exoplatform.portal.pom.spi.wsrp.WSRP;
import org.exoplatform.portal.pom.spi.wsrp.WSRPContentProvider;
import org.exoplatform.portal.pom.spi.wsrp.WSRPState;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.registry.RegistryService;
import org.gatein.mop.core.api.MOPService;
import org.gatein.mop.core.api.content.ContentManagerRegistry;
import org.gatein.mop.core.api.content.CustomizationContextProviderRegistry;
import org.gatein.mop.core.api.workspace.GroupSite;
import org.gatein.mop.core.api.workspace.GroupSiteContainer;
import org.gatein.mop.core.api.workspace.NavigationContainer;
import org.gatein.mop.core.api.workspace.NavigationImpl;
import org.gatein.mop.core.api.workspace.PageContainer;
import org.gatein.mop.core.api.workspace.PageImpl;
import org.gatein.mop.core.api.workspace.PageLinkImpl;
import org.gatein.mop.core.api.workspace.PortalSite;
import org.gatein.mop.core.api.workspace.PortalSiteContainer;
import org.gatein.mop.core.api.workspace.UIBodyImpl;
import org.gatein.mop.core.api.workspace.UIContainerImpl;
import org.gatein.mop.core.api.workspace.UIWindowImpl;
import org.gatein.mop.core.api.workspace.URLLinkImpl;
import org.gatein.mop.core.api.workspace.UserSite;
import org.gatein.mop.core.api.workspace.UserSiteContainer;
import org.gatein.mop.core.api.workspace.WorkspaceImpl;
import org.gatein.mop.core.api.workspace.content.ContextSpecialization;
import org.gatein.mop.core.api.workspace.content.ContextType;
import org.gatein.mop.core.api.workspace.content.ContextTypeContainer;
import org.gatein.mop.core.api.workspace.content.CustomizationContainer;
import org.gatein.mop.core.api.workspace.content.WorkspaceClone;
import org.gatein.mop.core.api.workspace.content.WorkspaceSpecialization;

import javax.jcr.Credentials;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.lang.reflect.Field;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Set;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class POMSessionManager
{

   /** . */
   private static final ThreadLocal<POMSession> current = new ThreadLocal<POMSession>();

   /** . */
   final RepositoryService repositoryService;

   /** . */
   private MOPService pomService;

   /** . */
   final String repositoryName = "repository";

   /** . */
   final String workspaceName = "portal-system";

   public POMSessionManager(RegistryService service) throws Exception
   {
      RepositoryService repositoryService = service.getRepositoryService();

      //
      this.repositoryService = repositoryService;
      this.pomService = null;
   }

   public Session login() throws RepositoryException
   {
      ManageableRepository repo = repositoryService.getCurrentRepository();
      return repo.login();
   }

   public Session login(String workspace) throws RepositoryException
   {
      Repository repo = repositoryService.getCurrentRepository();
      return repo.login(workspace);
   }

   public Session login(Credentials credentials, String workspace) throws RepositoryException
   {
      Repository repo = repositoryService.getCurrentRepository();
      return repo.login(credentials, workspace);
   }

   public Session login(Credentials credentials) throws RepositoryException
   {
      Repository repo = repositoryService.getCurrentRepository();
      return repo.login(credentials);
   }

   /*
    * todo : use better than the synchronized block  
    */
   public synchronized MOPService getPOMService()
   {
      if (pomService == null)
      {
         try
         {
            MOPService pomService = new MOPService();
            pomService.setOption(ChromatticBuilder.SESSION_LIFECYCLE_CLASSNAME, PortalSessionLifeCycle.class.getName());
            pomService.setOption(ChromatticBuilder.INSTRUMENTOR_CLASSNAME, InstrumentorImpl.class.getName());

            //
            Field chromeField = MOPService.class.getDeclaredField("chrome");
            Field builderField = MOPService.class.getDeclaredField("builder");
            Field contentManagerRegistryField = MOPService.class.getDeclaredField("contentManagerRegistry");
            Field customizationContextResolversField =
               MOPService.class.getDeclaredField("customizationContextResolvers");
            chromeField.setAccessible(true);
            builderField.setAccessible(true);
            contentManagerRegistryField.setAccessible(true);
            customizationContextResolversField.setAccessible(true);

            // Perform a manual start of the MOPService until the real .start() is cleaned in a new beta
            ChromatticBuilder builder = (ChromatticBuilder)builderField.get(pomService);

            //
            Field classesField = ChromatticBuilder.class.getDeclaredField("classes");
            classesField.setAccessible(true);
            Set<Class<?>> classes = (Set<Class<?>>)classesField.get(builder);
            classes.clear();

            //
            builder.add(WorkspaceImpl.class);
            builder.add(UIContainerImpl.class);
            builder.add(UIWindowImpl.class);
            builder.add(UIBodyImpl.class);
            builder.add(PageImpl.class);
            builder.add(PageContainer.class);
            builder.add(NavigationImpl.class);
            builder.add(NavigationContainer.class);
            builder.add(PageLinkImpl.class);
            builder.add(URLLinkImpl.class);
            builder.add(PortalSiteContainer.class);
            builder.add(PortalSite.class);
            builder.add(GroupSiteContainer.class);
            builder.add(GroupSite.class);
            builder.add(UserSiteContainer.class);
            builder.add(UserSite.class);

            //
            builder.add(CustomizationContainer.class);
            builder.add(ContextTypeContainer.class);
            builder.add(ContextType.class);
            builder.add(ContextSpecialization.class);
            builder.add(WorkspaceClone.class);
            builder.add(WorkspaceSpecialization.class);

            //
            builder.add(PortletState.class);
            builder.add(PreferenceState.class);
            builder.add(GadgetState.class);
            builder.add(WSRPState.class);

            //
            builder.add(ContentRegistry.class);
            builder.add(CategoryDefinition.class);
            builder.add(ContentDefinition.class);

            //
            CustomizationContextProviderRegistry customizationContextResolvers =
               new CustomizationContextProviderRegistry();

            //
            ContentManagerRegistry contentManagerRegistry = new ContentManagerRegistry();
            contentManagerRegistry.register(Portlet.CONTENT_TYPE, new PortletContentProvider());
            contentManagerRegistry.register(Gadget.CONTENT_TYPE, new GadgetContentProvider());
            contentManagerRegistry.register(WSRP.CONTENT_TYPE, new WSRPContentProvider());

            //
            chromeField.set(pomService, builder.build());
            contentManagerRegistryField.set(pomService, contentManagerRegistry);
            customizationContextResolversField.set(pomService, customizationContextResolvers);

            //
            this.pomService = pomService;
         }
         catch (Exception e)
         {
            throw new UndeclaredThrowableException(e);
         }
      }
      return pomService;
   }

   /**
    * <p>Returns the session currently associated with the current thread of execution.</p>
    *
    * @return the current session
    */
   public static POMSession getSession()
   {
      return current.get();
   }

   /**
    * <p>Open and returns a session to the model. When the current thread is already associated with a previously opened
    * session the method will throw an <tt>IllegalStateException</tt>.</p>
    *
    * @return a session to the model.
    */
   public POMSession openSession()
   {
      POMSession session = current.get();
      if (session == null)
      {
         session = new POMSession(this);
         current.set(session);

         //
         // A bit ugly but we will improve that later
         ChromatticSession csession = session.getSession();
         csession.addEventListener(new Injector(session));
      }
      else
      {
         throw new IllegalStateException("A session is already opened.");
      }
      return session;
   }

   /**
    * <p>Closes the current session and discard the changes done during the session.</p>
    *
    * @return a boolean indicating if the session was closed
    * @see #closeSession(boolean)
    */
   public boolean closeSession()
   {
      return closeSession(false);
   }

   /**
    * <p>Closes the current session and optionally saves its content. If no session is associated then this method has
    * no effects and returns false.</p>
    *
    * @param save if the session must be saved
    * @return a boolean indicating if the session was closed
    */
   public boolean closeSession(boolean save)
   {
      POMSession session = current.get();
      if (session == null)
      {
         // Should warn
         return false;
      }
      else
      {
         current.set(null);
         try
         {
            if (save)
            {
               session.save();
            }
         }
         finally
         {
            session.close();
         }
         return true;
      }
   }

   /**
    * <p>Execute the task with a session. The method attempts first to get a current session and if no such session is
    * found then a session will be created for the scope of the method.</p>
    *
    * @param task the task to execute
    * @throws Exception any exception thrown by the task
    */
   public void execute(POMTask task) throws Exception
   {
      POMSession session = getSession();
      if (session == null)
      {
         session = openSession();
         try
         {
            session.execute(task);
         }
         finally
         {
            closeSession(true);
         }
      }
      else
      {
         session.execute(task);
      }
   }
}
