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

package org.exoplatform.portal.webui.application;

import org.exoplatform.application.gadget.GadgetRegistryService;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.model.ApplicationState;
import org.exoplatform.portal.config.model.ApplicationType;
import org.exoplatform.portal.config.model.PersistentApplicationState;
import org.exoplatform.portal.config.model.TransientApplicationState;
import org.exoplatform.portal.pc.ExoPortletState;
import org.exoplatform.portal.pc.ExoPortletStateType;
import org.exoplatform.portal.pom.config.Utils;
import org.exoplatform.portal.pom.spi.gadget.Gadget;
import org.exoplatform.portal.pom.spi.portlet.Preference;
import org.exoplatform.portal.pom.spi.portlet.Preferences;
import org.exoplatform.portal.pom.spi.portlet.PreferencesBuilder;
import org.exoplatform.portal.pom.spi.wsrp.WSRP;
import org.exoplatform.portal.pom.spi.wsrp.WSRPPortletStateType;
import org.exoplatform.web.application.gadget.GadgetApplication;
import org.gatein.pc.api.PortletContext;
import org.gatein.pc.api.PortletInvoker;
import org.gatein.pc.api.StatefulPortletContext;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class ModelAdapter<S, C extends Serializable>
{

   public static <S, C extends Serializable, I> ModelAdapter<S, C> getAdapter(ApplicationType<S> type)
   {
      if (type == ApplicationType.PORTLET)
      {
         @SuppressWarnings("unchecked") ModelAdapter<S, C> adapter = (ModelAdapter<S, C>)PORTLET;
         return adapter;
      }
      else if (type == ApplicationType.GADGET)
      {
         @SuppressWarnings("unchecked") ModelAdapter<S, C> adapter = (ModelAdapter<S, C>)GADGET;
         return adapter;
      }
      else if (type == ApplicationType.WSRP_PORTLET)
      {
         @SuppressWarnings("unchecked") ModelAdapter<S, C> adapter = (ModelAdapter<S, C>)WSRP;
         return adapter;
      }
      else
      {
         throw new AssertionError();
      }
   }

   /** . */
   private static final ModelAdapter<Preferences, ExoPortletState> PORTLET = new ModelAdapter<Preferences, ExoPortletState>()
   {

      @Override
      public StatefulPortletContext<ExoPortletState> getPortletContext(ExoContainer container, String applicationId, ApplicationState<Preferences> applicationState) throws Exception
      {
         DataStorage dataStorage = (DataStorage)container.getComponentInstanceOfType(DataStorage.class);
         Preferences preferences = dataStorage.load(applicationState);
         PortletContext producerOfferedPortletContext = getProducerOfferedPortletContext(applicationId);
         ExoPortletState map = new ExoPortletState(producerOfferedPortletContext.getId());
         if (preferences != null)
         {
            for (Preference pref : preferences)
            {
               map.getState().put(pref.getName(), pref.getValues());
            }
         }
         return StatefulPortletContext.create("local._dumbvalue", ExoPortletStateType.getInstance(), map);
      }

      @Override
      public ApplicationState<Preferences> update(ExoContainer container, ExoPortletState updateState, ApplicationState<Preferences> applicationState) throws Exception
      {
         // Compute new preferences
         PreferencesBuilder builder = new PreferencesBuilder();
         for (Map.Entry<String, List<String>> entry : updateState.getState().entrySet())
         {
            builder.add(entry.getKey(), entry.getValue());
         }

         if (applicationState instanceof TransientApplicationState)
         {
            TransientApplicationState<Preferences> transientState = (TransientApplicationState<Preferences>)applicationState;
            transientState.setContentState(builder.build());
            return transientState;
         }
         else
         {
            PersistentApplicationState<Preferences> persistentState = (PersistentApplicationState<Preferences>)applicationState;
            DataStorage dataStorage = (DataStorage)container.getComponentInstanceOfType(DataStorage.class);
            return dataStorage.save(persistentState, builder.build());
         }
      }

      @Override
      public PortletContext getProducerOfferedPortletContext(String applicationState)
      {
         String[] chunks = Utils.split("/", applicationState);
         String appName = chunks[0];
         String portletName = chunks[1];
         return PortletContext.createPortletContext(PortletInvoker.LOCAL_PORTLET_INVOKER_ID + "./" + appName + "." + portletName);
      }

      @Override
      public Preferences getState(ExoContainer container, ApplicationState<Preferences> applicationState) throws Exception
      {
         if (applicationState instanceof TransientApplicationState)
         {
            TransientApplicationState<Preferences> transientState = (TransientApplicationState<Preferences>)applicationState;
            Preferences pref = transientState.getContentState();
            if(pref == null) pref = new Preferences();
            return pref;
         }
         else
         {
            PersistentApplicationState<Preferences> persistentState = (PersistentApplicationState<Preferences>)applicationState;
            DataStorage dataStorage = (DataStorage)container.getComponentInstanceOfType(DataStorage.class);
            return dataStorage.load(persistentState);
         }
      }
   };

   private static final ModelAdapter<Gadget, ExoPortletState> GADGET = new ModelAdapter<Gadget, ExoPortletState>()
   {

      /** . */
      private final String WRAPPER_ID = "local./" + "dashboard" + "." + "GadgetPortlet";

      /** . */
      private final PortletContext WRAPPER_CONTEXT = PortletContext.createPortletContext(WRAPPER_ID);

      @Override
      public StatefulPortletContext<ExoPortletState> getPortletContext(ExoContainer container, String applicationId, ApplicationState<Gadget> applicationState) throws Exception
      {
         GadgetRegistryService gadgetService = (GadgetRegistryService)container.getComponentInstanceOfType(GadgetRegistryService.class);
         org.exoplatform.application.gadget.Gadget model = gadgetService.getGadget(applicationId);
         GadgetApplication application = new GadgetApplication(model.getName(), model.getUrl(), model.isLocal());
         String url = GadgetUtil.reproduceUrl(application.getUrl(), application.isLocal());
         ExoPortletState prefs = new ExoPortletState(WRAPPER_ID);
         prefs.getState().put("url", Arrays.asList(url));
         return StatefulPortletContext.create("local._dumbvalue", ExoPortletStateType.getInstance(), prefs);
      }

      @Override
      public ApplicationState<Gadget> update(ExoContainer container, ExoPortletState updateState, ApplicationState<Gadget> gadgetApplicationState) throws Exception
      {
         throw new UnsupportedOperationException("todo / julien");
      }

      @Override
      public PortletContext getProducerOfferedPortletContext(String applicationState)
      {
         return WRAPPER_CONTEXT;
      }

      @Override
      public Preferences getState(ExoContainer container, ApplicationState<Gadget> applicationState) throws Exception
      {
         // For now we return null as it does not make sense to edit the gadget preferences
         return null;
      }
   };


   private static final ModelAdapter<WSRP, WSRP> WSRP = new ModelAdapter<WSRP, WSRP>()
   {
      @Override
      public Preferences getState(ExoContainer container, ApplicationState<WSRP> state) throws Exception
      {
         return null;  // return null for now
      }

      @Override
      public PortletContext getProducerOfferedPortletContext(String applicationId)
      {
         return PortletContext.createPortletContext(applicationId);
      }

      @Override
      public StatefulPortletContext<WSRP> getPortletContext(ExoContainer container, String applicationId, ApplicationState<WSRP> state) throws Exception
      {
         DataStorage dataStorage = (DataStorage)container.getComponentInstanceOfType(DataStorage.class);
         WSRP wsrp = dataStorage.load(state);
         if (wsrp == null)
         {
            wsrp = new WSRP();
            wsrp.setPortletId(applicationId);
         }
         return StatefulPortletContext.create(wsrp.getPortletId(), WSRPPortletStateType.instance, wsrp);
      }

      @Override
      public ApplicationState<WSRP> update(ExoContainer container, WSRP updateState, ApplicationState<WSRP> state) throws Exception
      {
         if (state instanceof TransientApplicationState)
         {
            TransientApplicationState<WSRP> transientState = (TransientApplicationState<WSRP>)state;
            transientState.setContentState(updateState);
            return transientState;
         }
         else
         {
            PersistentApplicationState<WSRP> persistentState = (PersistentApplicationState<WSRP>)state;
            DataStorage dataStorage = (DataStorage)container.getComponentInstanceOfType(DataStorage.class);
            return dataStorage.save(persistentState, updateState);
         }
      }
   };

   public abstract PortletContext getProducerOfferedPortletContext(String applicationId);

   public abstract StatefulPortletContext<C> getPortletContext(ExoContainer container, String applicationId, ApplicationState<S> applicationState) throws Exception;

   public abstract ApplicationState<S> update(ExoContainer container, C updateState, ApplicationState<S> applicationState) throws Exception;

   /**
    * Returns the state of the gadget as preferences or null if the preferences cannot be edited as such.
    *
    * @param container        the container
    * @param applicationState the application state
    * @return the preferences
    * @throws Exception any exception
    */
   public abstract Preferences getState(ExoContainer container, ApplicationState<S> applicationState) throws Exception;

}
