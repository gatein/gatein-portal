/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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
package org.gatein.integration.jboss.as7;

import org.jboss.as.controller.parsing.ParseUtils;
import org.jboss.as.controller.persistence.SubsystemMarshallingContext;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.Property;
import org.jboss.staxmapper.XMLElementReader;
import org.jboss.staxmapper.XMLElementWriter;
import org.jboss.staxmapper.XMLExtendedStreamReader;
import org.jboss.staxmapper.XMLExtendedStreamWriter;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import java.util.EnumSet;
import java.util.List;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;
import static org.jboss.as.controller.parsing.ParseUtils.requireNoAttributes;
import static org.jboss.as.controller.parsing.ParseUtils.requireNoContent;
import static org.jboss.as.controller.parsing.ParseUtils.requireNoNamespaceAttribute;
import static org.jboss.as.controller.parsing.ParseUtils.unexpectedAttribute;
import static org.jboss.as.controller.parsing.ParseUtils.unexpectedElement;

/**
 * GateIn subsystem parser.
 *
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class GateInSubsystemParser implements XMLStreamConstants, XMLElementReader<List<ModelNode>>, XMLElementWriter<SubsystemMarshallingContext>
{

   private static final GateInSubsystemParser INSTANCE = new GateInSubsystemParser();

   static GateInSubsystemParser getInstance()
   {
      return INSTANCE;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void writeContent(XMLExtendedStreamWriter writer, SubsystemMarshallingContext context) throws XMLStreamException
   {
      context.startSubsystemElement(GateInExtension.NAMESPACE, false);

      ModelNode node = context.getModelNode();
      if (node.hasDefined(Constants.DEPLOYMENT_ARCHIVE))
      {
         writeDeploymentArchives(writer, node.get(Constants.DEPLOYMENT_ARCHIVE));
      }
      if (node.hasDefined(Constants.PORTLET_WAR_DEPENDENCY))
      {
         writePortletWarDependencies(writer, node.get(Constants.PORTLET_WAR_DEPENDENCY));
      }
      writer.writeEndElement();
   }

   private void writeDeploymentArchives(XMLExtendedStreamWriter writer, ModelNode deployArchives) throws XMLStreamException
   {
      if (deployArchives.isDefined() && deployArchives.asInt() > 0)
      {
         writer.writeStartElement(Element.DEPLOYMENT_ARCHIVES.getLocalName());
         for (Property archive : deployArchives.asPropertyList())
         {
            writer.writeStartElement(Element.ARCHIVE.getLocalName());
            writer.writeAttribute(Attribute.NAME.getLocalName(), archive.getName());
            ModelNode model = archive.getValue();
            DeploymentArchiveDefinition.MAIN.marshallAsAttribute(model, false, writer);
            writer.writeEndElement();
         }
         writer.writeEndElement();
      }
   }


   private void writePortletWarDependencies(XMLExtendedStreamWriter writer, ModelNode warDependencies) throws XMLStreamException
   {
      if (warDependencies.isDefined() && warDependencies.asInt() > 0)
      {
         writer.writeStartElement(Element.PORTLET_WAR_DEPENDENCIES.getLocalName());
         for (Property dependency : warDependencies.asPropertyList())
         {
            writer.writeStartElement(Element.DEPENDENCY.getLocalName());
            writer.writeAttribute(Attribute.NAME.getLocalName(), dependency.getName());
            ModelNode model = dependency.getValue();
            PortletWarDependancyDefinition.IMPORT_SERVICES.marshallAsAttribute(model, false, writer);
            writer.writeEndElement();
         }
         writer.writeEndElement();
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void readElement(XMLExtendedStreamReader reader, List<ModelNode> list) throws XMLStreamException
   {
      // no attributes
      if (reader.getAttributeCount() > 0)
      {
         throw unexpectedAttribute(reader, 0);
      }

      final ModelNode address = new ModelNode();
      address.add(SUBSYSTEM, GateInExtension.SUBSYSTEM_NAME);
      address.protect();

      final ModelNode subsystem = new ModelNode();
      subsystem.get(OP).set(ADD);
      subsystem.get(OP_ADDR).set(address);
      final int count = reader.getAttributeCount();
      for (int i = 0; i < count; i++)
      {
         requireNoNamespaceAttribute(reader, i);
         requireNoAttributes(reader);
      }
      list.add(subsystem);

      // elements
      while (reader.hasNext() && reader.nextTag() != END_ELEMENT)
      {
         if (GateInExtension.NAMESPACE.equals(reader.getNamespaceURI()))
         {
            final Element element = Element.forName(reader.getLocalName());
            switch (element)
            {
               case DEPLOYMENT_ARCHIVES:
               {
                  parseDeploymentArchives(reader, address, list);
                  break;
               }
               case PORTLET_WAR_DEPENDENCIES:
               {
                  parsePortletWarDependencies(reader, address, list);
                  break;
               }
               default:
               {
                  throw unexpectedElement(reader);
               }
            }
         } else
         {
            throw unexpectedElement(reader);
         }
      }
   }

   static void parseDeploymentArchives(XMLExtendedStreamReader reader, ModelNode parent, List<ModelNode> operations) throws XMLStreamException
   {

      // no attributes
      requireNoAttributes(reader);
      // elements
      while (reader.hasNext() && reader.nextTag() != END_ELEMENT)
      {
         final Element element = Element.forName(reader.getLocalName());
         switch (element)
         {
            case ARCHIVE:
            {
               parseArchive(reader, parent, operations);
               break;
            }
            default:
               throw unexpectedElement(reader);
         }
      }

      // validate archives
      //TODO: this must go in DeploymentArchiveAdd
      /*int mainCount = 0;
     for (Property p : model.asPropertyList()) {
         if (p.getValue().isDefined()) {
             List<Property> props = p.getValue().asPropertyList();
             if (props.size() > 0 && MAIN.equals(props.get(0).getName())) {
                 mainCount++;
             }
         }
     }

     if (mainCount != 1) {
         throw new RuntimeException("Exactly one archive has to be marked as a main archive (found: " + mainCount + ")");
     } */

   }

   static void parseArchive(XMLExtendedStreamReader reader, ModelNode parent, List<ModelNode> operations) throws XMLStreamException
   {
      ModelNode model = new ModelNode();

      final int count = reader.getAttributeCount();

      String name = null;
      for (int i = 0; i < count; i++)
      {
         requireNoNamespaceAttribute(reader, i);
         final String value = reader.getAttributeValue(i);
         final Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
         switch (attribute)
         {
            case NAME:
            {
               name = value;
               break;
            }
            case MAIN:
            {
               DeploymentArchiveDefinition.MAIN.parseAndSetParameter(value, model, reader);
               break;
            }
            default:
               throw unexpectedAttribute(reader, i);
         }
      }

      if (name == null)
      {
         throw new RuntimeException("Attribute '" + Attribute.NAME.getLocalName()
            + "' of '" + Element.ARCHIVE.getLocalName() + "' element can not be null!");
      }

      ModelNode address = parent.clone();
      address.add(Constants.DEPLOYMENT_ARCHIVE, name);

      model.get(OP).set(ADD);
      model.get(OP_ADDR).set(address);
      operations.add(model);

      requireNoContent(reader);
   }

   static void parsePortletWarDependencies(XMLExtendedStreamReader reader, ModelNode parent, List<ModelNode> operations) throws XMLStreamException
   {
      // no attributes
      requireNoAttributes(reader);
      // elements
      while (reader.hasNext() && reader.nextTag() != END_ELEMENT)
      {
         final Element element = Element.forName(reader.getLocalName());
         switch (element)
         {
            case DEPENDENCY:
            {
               parseDependency(reader, parent, operations);
               break;
            }
            default:
               throw unexpectedElement(reader);
         }
      }
   }

   static void parseDependency(XMLExtendedStreamReader reader, ModelNode parent, List<ModelNode> operations) throws XMLStreamException
   {

      final ModelNode model = new ModelNode();
      // attributes
      final int count = reader.getAttributeCount();
      String name = null;
      for (int i = 0; i < count; i++)
      {
         requireNoNamespaceAttribute(reader, i);
         final String value = reader.getAttributeValue(i);
         final Attribute attribute = Attribute.forName(reader.getAttributeLocalName(i));
         switch (attribute)
         {
            case NAME:
            {
               name = value;
               break;
            }
            case IMPORT_SERVICES:
            {
               PortletWarDependancyDefinition.IMPORT_SERVICES.parseAndSetParameter(value, model, reader);
               /*requireTrueOrFalse(value);
             importSvcs = Boolean.parseBoolean(value);*/
               break;
            }
            default:
               throw unexpectedAttribute(reader, i);
         }
      }

      if (name == null)
      {
         throw ParseUtils.missingRequired(reader, EnumSet.of(Attribute.NAME));
      }

      ModelNode address = parent.clone();
      address.add(Constants.PORTLET_WAR_DEPENDENCY, name);

      model.get(OP).set(ADD);
      model.get(OP_ADDR).set(address);
      operations.add(model);

      requireNoContent(reader);
   }
}
