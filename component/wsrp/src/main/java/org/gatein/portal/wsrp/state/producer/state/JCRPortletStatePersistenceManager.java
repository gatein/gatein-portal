/*
 * JBoss, a division of Red Hat
 * Copyright 2010, Red Hat Middleware, LLC, and individual
 * contributors as indicated by the @authors tag. See the
 * copyright.txt in the distribution for a full listing of
 * individual contributors.
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

package org.gatein.portal.wsrp.state.producer.state;

import org.chromattic.api.ChromatticSession;
import org.exoplatform.container.ExoContainer;
import org.gatein.common.util.ParameterValidation;
import org.gatein.pc.api.state.PropertyMap;
import org.gatein.pc.portlet.state.InvalidStateIdException;
import org.gatein.pc.portlet.state.NoSuchStateException;
import org.gatein.pc.portlet.state.producer.AbstractPortletStatePersistenceManager;
import org.gatein.pc.portlet.state.producer.PortletStateContext;
import org.gatein.portal.wsrp.state.JCRPersister;
import org.gatein.portal.wsrp.state.producer.state.mapping.PortletStateContextMapping;
import org.gatein.portal.wsrp.state.producer.state.mapping.PortletStateContextsMapping;
import org.gatein.portal.wsrp.state.producer.state.mapping.PortletStateMapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class JCRPortletStatePersistenceManager extends AbstractPortletStatePersistenceManager
{
   private JCRPersister persister;
   private static final String PATH = PortletStateContextsMapping.NODE_NAME + "/";

   public JCRPortletStatePersistenceManager(ExoContainer container) throws Exception
   {
      persister = new JCRPersister(container, JCRPersister.PORTLET_STATES_WORKSPACE_NAME);

      List<Class> mappingClasses = new ArrayList<Class>(6);
      Collections.addAll(mappingClasses, PortletStateContextsMapping.class, PortletStateContextMapping.class, PortletStateMapping.class);

      persister.initializeBuilderFor(mappingClasses);

//      persister = NewJCRPersister.getInstance(container);
   }

   private PortletStateContextsMapping getContexts(ChromatticSession session)
   {
      PortletStateContextsMapping portletStateContexts = session.findByPath(PortletStateContextsMapping.class, PortletStateContextsMapping.NODE_NAME);
      if (portletStateContexts == null)
      {
         portletStateContexts = session.insert(PortletStateContextsMapping.class, PortletStateContextsMapping.NODE_NAME);
      }
      return portletStateContexts;
   }

   @Override
   public void updateState(String stateId, PropertyMap propertyMap) throws NoSuchStateException, InvalidStateIdException
   {
      // more optimized version of updateState
      ParameterValidation.throwIllegalArgExceptionIfNull(propertyMap, "property map");

      ChromatticSession session = persister.getSession();

      PortletStateContextMapping pscm = getPortletStateContextMapping(session, stateId);
      PortletStateMapping psm = pscm.getState();
      psm.setProperties(propertyMap);

      persister.closeSession(session, true);
   }


   @Override
   protected PortletStateContext getStateContext(String stateId)
   {
      ChromatticSession session = persister.getSession();

      PortletStateContextMapping pscm = getPortletStateContextMapping(session, stateId);
      PortletStateContext context;
      if (pscm == null)
      {
         context = null;
      }
      else
      {
         context = pscm.toPortletStateContext();
      }

      persister.closeSession(session, false);

      return context;
   }

   @Override
   protected String createStateContext(String portletId, PropertyMap propertyMap)
   {
      ChromatticSession session = persister.getSession();

      String encodedForPath = JCRPersister.PortletNameFormatter.encode(portletId);

      PortletStateContextMapping pscm = session.findByPath(PortletStateContextMapping.class, PATH + encodedForPath);
      if (pscm == null)
      {
         PortletStateContextsMapping portletStateContexts = getContexts(session);
         pscm = portletStateContexts.createPortletStateContext(portletId);
         portletStateContexts.getPortletStateContexts().add(pscm);
      }

      PortletStateMapping psm = pscm.getState();
      psm.setPortletID(pscm.getPortletId());
      psm.setProperties(propertyMap);

      persister.closeSession(session, true);

      return pscm.getPersistentKey();
   }

   @Override
   protected PortletStateContext destroyStateContext(String stateId)
   {
      ChromatticSession session = persister.getSession();

      PortletStateContextMapping pscm = getPortletStateContextMapping(session, stateId);
      PortletStateContext result;
      if (pscm == null)
      {
         result = null;
      }
      else
      {
         getContexts(session).getPortletStateContexts().remove(pscm);
         session.remove(pscm);
         result = pscm.toPortletStateContext();
      }

      persister.closeSession(session, true);
      return result;
   }

   @Override
   protected void updateStateContext(PortletStateContext stateContext)
   {
      throw new UnsupportedOperationException("Shouldn't be called as updateState method is overriden!");
   }

   private PortletStateContextMapping getPortletStateContextMapping(ChromatticSession session, String stateId)
   {
      return getContexts(session).findPortletStateContextById(stateId);
   }
}
