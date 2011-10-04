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

import org.jboss.as.controller.persistence.SubsystemMarshallingContext;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.Property;
import org.jboss.staxmapper.XMLElementReader;
import org.jboss.staxmapper.XMLElementWriter;
import org.jboss.staxmapper.XMLExtendedStreamReader;
import org.jboss.staxmapper.XMLExtendedStreamWriter;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import java.util.List;

import static org.gatein.integration.jboss.as7.Constants.*;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;
import static org.jboss.as.controller.parsing.ParseUtils.*;

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
      if (node.hasDefined(DEPLOYMENT_ARCHIVES))
      {
         writeDeploymentArchives(writer, node.get(DEPLOYMENT_ARCHIVES));
      }
      if (node.hasDefined(PORTLET_WAR_DEPENDENCIES))
      {
         writePortletWarDependencies(writer, node.get(PORTLET_WAR_DEPENDENCIES));
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
            if (archive.getValue().isDefined() && archive.getValue().asInt() > 0)
            {
               for (Property property : archive.getValue().asPropertyList())
               {
                  if (MAIN.equals(property.getName()))
                  {
                     writer.writeAttribute(Attribute.MAIN.getLocalName(), TRUE);
                  }
                  else
                  {
                     throw new RuntimeException("Unexpected model property: " + property + " on " + archive);
                  }
               }
            }
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
            if (dependency.getValue().isDefined() && dependency.getValue().asInt() > 0)
            {
               for (Property property : dependency.getValue().asPropertyList())
               {
                  if (IMPORT_SERVICES.equals(property.getName()))
                  {
                     writer.writeAttribute(Attribute.IMPORT_SERVICES.getLocalName(), TRUE);
                  }
                  else
                  {
                     throw new RuntimeException("Unexpected model property: " + property + " on " + dependency);
                  }
               }
            }
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
                  final ModelNode deploymentArchives = parseDeploymentArchives(reader);
                  subsystem.get(DEPLOYMENT_ARCHIVES).set(deploymentArchives);
                  break;
               }
               case PORTLET_WAR_DEPENDENCIES:
               {
                  final ModelNode portletWarDeps = parsePortletWarDependencies(reader);
                  subsystem.get(PORTLET_WAR_DEPENDENCIES).set(portletWarDeps);
                  break;
               }
               default:
               {
                  throw unexpectedElement(reader);
               }
            }
         }
         else
         {
            throw unexpectedElement(reader);
         }
      }
   }

   static ModelNode parseDeploymentArchives(XMLExtendedStreamReader reader) throws XMLStreamException
   {
      final ModelNode model = new ModelNode();
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
               parseArchive(reader, model);
               break;
            }
            default:
               throw unexpectedElement(reader);
         }
      }

      // validate archives
      int mainCount = 0;
      for (Property p : model.asPropertyList())
      {
         if (p.getValue().isDefined())
         {
            List<Property> props = p.getValue().asPropertyList();
            if (props.size() > 0 && MAIN.equals(props.get(0).getName()))
            {
               mainCount++;
            }
         }
      }

      if (mainCount != 1)
      {
         throw new RuntimeException("Exactly one archive has to be marked as a main archive (found: " + mainCount + ")");
      }

      return model;
   }

   static void parseArchive(XMLExtendedStreamReader reader, ModelNode model) throws XMLStreamException
   {
      // attributes
      final int count = reader.getAttributeCount();
      boolean main = false;
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
               requireTrueOrFalse(value);
               main = Boolean.parseBoolean(value);
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

      ModelNode attrList = new ModelNode();
      if (main)
      {
         attrList.add(MAIN, new ModelNode());
      }
      model.add(name, attrList);

      requireNoContent(reader);
   }

   static ModelNode parsePortletWarDependencies(XMLExtendedStreamReader reader) throws XMLStreamException
   {
      final ModelNode model = new ModelNode();
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
               parseDependency(reader, model);
               break;
            }
            default:
               throw unexpectedElement(reader);
         }
      }

      return model;
   }

   static void parseDependency(XMLExtendedStreamReader reader, ModelNode model) throws XMLStreamException
   {
      // attributes
      final int count = reader.getAttributeCount();
      boolean importSvcs = false;
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
               requireTrueOrFalse(value);
               importSvcs = Boolean.parseBoolean(value);
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

      ModelNode attrList = new ModelNode();
      if (importSvcs)
      {
         attrList.add(IMPORT_SERVICES, new ModelNode());
      }
      model.add(name, attrList);

      requireNoContent(reader);
   }

   private static void requireTrueOrFalse(String value)
   {
      if (!"true".equals(value) && !"false".equals(value))
      {
         throw new RuntimeException("Invalid value for boolean attribute: " + value);
      }
   }
}
