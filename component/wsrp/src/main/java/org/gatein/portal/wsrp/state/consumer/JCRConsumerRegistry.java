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

package org.gatein.portal.wsrp.state.consumer;

import org.chromattic.api.ChromatticSession;
import org.exoplatform.container.ExoContainer;
import org.gatein.portal.wsrp.state.JCRPersister;
import org.gatein.portal.wsrp.state.StoresByPathManager;
import org.gatein.portal.wsrp.state.consumer.mapping.EndpointInfoMapping;
import org.gatein.portal.wsrp.state.consumer.mapping.ProducerInfoMapping;
import org.gatein.portal.wsrp.state.consumer.mapping.ProducerInfosMapping;
import org.gatein.portal.wsrp.state.consumer.mapping.RegistrationInfoMapping;
import org.gatein.portal.wsrp.state.consumer.mapping.RegistrationPropertyMapping;
import org.gatein.portal.wsrp.state.mapping.RegistrationPropertyDescriptionMapping;
import org.gatein.wsrp.WSRPConsumer;
import org.gatein.wsrp.consumer.ProducerInfo;
import org.gatein.wsrp.consumer.registry.AbstractConsumerRegistry;
import org.gatein.wsrp.consumer.registry.xml.XMLConsumerRegistry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class JCRConsumerRegistry extends AbstractConsumerRegistry implements StoresByPathManager<ProducerInfo>
{
   //   private NewJCRPersister persister;
   private JCRPersister persister;
   private static final String PRODUCER_INFOS_PATH = ProducerInfosMapping.NODE_NAME;

   public JCRConsumerRegistry(ExoContainer container) throws Exception
   {
      List<Class> mappingClasses = new ArrayList<Class>(6);
      Collections.addAll(mappingClasses, ProducerInfosMapping.class, ProducerInfoMapping.class,
         EndpointInfoMapping.class, RegistrationInfoMapping.class, RegistrationPropertyMapping.class,
         RegistrationPropertyDescriptionMapping.class);

      persister = new JCRPersister(container, JCRPersister.WSRP_WORKSPACE_NAME);
      persister.initializeBuilderFor(mappingClasses);
//      persister = NewJCRPersister.getInstance(container);
   }

   @Override
   protected void save(ProducerInfo info, String messageOnError)
   {

      try
      {
         ChromatticSession session = persister.getSession();

         ProducerInfosMapping pims = getProducerInfosMapping(session);
         ProducerInfoMapping pim = pims.createProducerInfo(info.getId());
         String key = session.persist(pims, pim, info.getId());
         info.setKey(key);
         pim.initFrom(info);

         persister.closeSession(true);
      }
      catch (Exception e)
      {
         e.printStackTrace();  // todo: fix me
         persister.closeSession(false);
      }
   }

   @Override
   protected void delete(ProducerInfo info)
   {
      persister.delete(info, this);
   }

   @Override
   protected String update(ProducerInfo producerInfo)
   {
      String key = producerInfo.getKey();
      if (key == null)
      {
         throw new IllegalArgumentException("ProducerInfo '" + producerInfo.getId()
            + "' hasn't been persisted and thus cannot be updated.");
      }

      String oldId;
      String newId;
      synchronized (this)
      {
         ChromatticSession session = persister.getSession();
         ProducerInfoMapping pim = session.findById(ProducerInfoMapping.class, key);
         if (pim == null)
         {
            throw new IllegalArgumentException("Couldn't find ProducerInfoMapping associated with key " + key);
         }
         oldId = pim.getId();
         newId = producerInfo.getId();
         pim.initFrom(producerInfo);

         persister.closeSession(true);
      }

      // if the consumer's id has changed, return the old one so that state can be updated
      return (oldId.equals(newId)) ? null : oldId;
   }

   @Override
   protected Iterator<ProducerInfo> getProducerInfosFromStorage()
   {
      ChromatticSession session = persister.getSession();
      ProducerInfosMapping producerInfosMapping = getProducerInfosMapping(session);

      List<ProducerInfoMapping> mappings = producerInfosMapping.getProducerInfos();

      persister.closeSession(true);

      return new MappingToProducerInfoIterator(mappings.iterator());
   }

   private ProducerInfosMapping getProducerInfosMapping(ChromatticSession session)
   {
      ProducerInfosMapping producerInfosMapping = session.findByPath(ProducerInfosMapping.class, PRODUCER_INFOS_PATH);

      // if we don't have info from JCR, load from XML and populate JCR
      if (producerInfosMapping == null)
      {
         producerInfosMapping = session.insert(ProducerInfosMapping.class, ProducerInfosMapping.NODE_NAME);

         List<ProducerInfoMapping> infos = producerInfosMapping.getProducerInfos();

         // Load from XML
         XMLConsumerRegistry fromXML = new XMLConsumerRegistry();
         fromXML.reloadConsumers();

         // Save to JCR
         List<WSRPConsumer> consumers = fromXML.getConfiguredConsumers();
         for (WSRPConsumer consumer : consumers)
         {
            ProducerInfo info = consumer.getProducerInfo();

            ProducerInfoMapping pim = producerInfosMapping.createProducerInfo(info.getId());

            // need to add to parent first to attach newly created ProducerInfoMapping
            infos.add(pim);

            // init it from ProducerInfo
            pim.initFrom(info);
         }
      }

      return producerInfosMapping;
   }

   public String getChildPath(ProducerInfo needsComputedPath)
   {
      return getPathFor(needsComputedPath);
   }

   private static String getPathFor(ProducerInfo info)
   {
      return PRODUCER_INFOS_PATH + "/" + info.getId();
   }

   private static ProducerInfoMapping toProducerInfoMapping(ProducerInfo producerInfo, ChromatticSession session)
   {
      ProducerInfoMapping pim = session.findById(ProducerInfoMapping.class, producerInfo.getKey());
      if (pim == null)
      {
         pim = session.insert(ProducerInfoMapping.class, getPathFor(producerInfo));
      }

      pim.initFrom(producerInfo);

      return pim;
   }

   private static class MappingToProducerInfoIterator implements Iterator<ProducerInfo>
   {
      private Iterator<ProducerInfoMapping> mappings;

      public MappingToProducerInfoIterator(Iterator<ProducerInfoMapping> infoMappingIterator)
      {
         this.mappings = infoMappingIterator;
      }

      public boolean hasNext()
      {
         return mappings.hasNext();
      }

      public ProducerInfo next()
      {
         return mappings.next().toModel(null);
      }

      public void remove()
      {
         throw new UnsupportedOperationException("Remove not supported!");
      }
   }
}
