/*
 * JBoss, a division of Red Hat
 * Copyright 2009, Red Hat Middleware, LLC, and individual
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

import org.chromattic.api.Chromattic;
import org.chromattic.api.ChromatticBuilder;
import org.chromattic.api.ChromatticSession;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.jcr.ext.registry.RegistryService;
import org.gatein.portal.wsrp.state.consumer.mapping.EndpointInfoMapping;
import org.gatein.portal.wsrp.state.consumer.mapping.ProducerInfoMapping;
import org.gatein.portal.wsrp.state.consumer.mapping.ProducerInfosMapping;
import org.gatein.portal.wsrp.state.consumer.mapping.RegistrationInfoMapping;
import org.gatein.portal.wsrp.state.consumer.mapping.RegistrationPropertyDescriptionMapping;
import org.gatein.portal.wsrp.state.consumer.mapping.RegistrationPropertyMapping;
import org.gatein.wsrp.WSRPConsumer;
import org.gatein.wsrp.consumer.ProducerInfo;
import org.gatein.wsrp.consumer.registry.AbstractConsumerRegistry;
import org.gatein.wsrp.consumer.registry.xml.XMLConsumerRegistry;

import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class ConsumerRegistry extends AbstractConsumerRegistry
{
   private Chromattic chrome;
   private static final String WSRP_SERVICES_PATH = RegistryService.EXO_SERVICES + "/wsrp/";
   private static final String PRODUCER_INFOS_PATH = WSRP_SERVICES_PATH + "producerinfos";

   public ConsumerRegistry(InitParams params, RegistryService jcrRegistry) throws Exception
   {

      ChromatticBuilder builder = ChromatticBuilder.create();
      builder.setOption(ChromatticBuilder.INSTRUMENTOR_CLASSNAME, "org.chromattic.apt.InstrumentorImpl");

      builder.add(ProducerInfosMapping.class);
      builder.add(ProducerInfoMapping.class);
      builder.add(EndpointInfoMapping.class);
      builder.add(RegistrationInfoMapping.class);
      builder.add(RegistrationPropertyMapping.class);
      builder.add(RegistrationPropertyDescriptionMapping.class);

      chrome = builder.build();
   }

   @Override
   protected void save(ProducerInfo info, String messageOnError)
   {
      String path = getPathFor(info);
      ChromatticSession session = chrome.openSession();

      session.persist(toProducerInfoMapping(info), path);

      // todo: find a way to inject JCR Id into ProducerInfo

      session.close();
   }

   @Override
   protected void delete(ProducerInfo info)
   {
      ChromatticSession session = chrome.openSession();
      delete(session, getPathFor(info));

      session.close();
   }

   @Override
   protected String update(ProducerInfo producerInfo)
   {
      // todo: need JCR id to be able to find the previous ProducerInfo in case the id has been modified
      return null;
   }

   @Override
   protected Iterator getAllProducerInfos()
   {
      ChromatticSession session = chrome.openSession();
      ProducerInfosMapping producerInfosMapping = session.findByPath(ProducerInfosMapping.class, PRODUCER_INFOS_PATH);

      Iterator<ProducerInfo> producerInfos;

      // if we don't have info from JCR, load from XML and populate JCR
      if (producerInfosMapping == null)
      {
         producerInfosMapping = session.insert(ProducerInfosMapping.class, PRODUCER_INFOS_PATH);

         List<ProducerInfoMapping> infos = producerInfosMapping.getProducerInfos();

         // Load from XML
         XMLConsumerRegistry fromXML = new XMLConsumerRegistry();
         fromXML.reloadConsumers();

         // Save to JCR
         List<WSRPConsumer> consumers = fromXML.getConfiguredConsumers();
         for (WSRPConsumer consumer : consumers)
         {
            infos.add(toProducerInfoMapping(consumer.getProducerInfo()));
         }
         session.persist(infos); // todo: is that sufficient?

         producerInfos = new ConsumerToProducerInfoIterator(consumers.iterator());
      }
      else
      {
         List<ProducerInfoMapping> mappings = producerInfosMapping.getProducerInfos();
         producerInfos = new MappingToProducerInfoIterator(mappings.iterator());
      }

      session.save();
      session.close();
      return producerInfos;
   }

   private void delete(ChromatticSession session, String path)
   {
      ProducerInfoMapping old = session.findByPath(ProducerInfoMapping.class, path);

      if (old != null)
      {
         session.remove(old);
      }
   }

   private String getPathFor(ProducerInfo info)
   {
      return PRODUCER_INFOS_PATH + "/" + info.getId();
   }

   private static ProducerInfoMapping toProducerInfoMapping(ProducerInfo producerInfo)
   {
      return null;  // todo: implement
   }

   private static ProducerInfo toProducerInfo(ProducerInfoMapping mapping)
   {
      return null; // todo: implement
   }

   private static class ConsumerToProducerInfoIterator implements Iterator<ProducerInfo>
   {

      private final Iterator<WSRPConsumer> consumers;

      public ConsumerToProducerInfoIterator(Iterator<WSRPConsumer> consumers)
      {
         this.consumers = consumers;
      }

      @Override
      public boolean hasNext()
      {
         return consumers.hasNext();
      }

      @Override
      public ProducerInfo next()
      {
         return consumers.next().getProducerInfo();
      }

      @Override
      public void remove()
      {
         throw new UnsupportedOperationException("Remove not supported!");
      }
   }

   private static class MappingToProducerInfoIterator implements Iterator<ProducerInfo>
   {
      private Iterator<ProducerInfoMapping> mappings;

      public MappingToProducerInfoIterator(Iterator<ProducerInfoMapping> infoMappingIterator)
      {
         this.mappings = infoMappingIterator;
      }

      @Override
      public boolean hasNext()
      {
         return mappings.hasNext();
      }

      @Override
      public ProducerInfo next()
      {
         return toProducerInfo(mappings.next());
      }

      @Override
      public void remove()
      {
         throw new UnsupportedOperationException("Remove not supported!");
      }
   }
}
