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

import junit.framework.Assert;
import org.gatein.integration.jboss.as7.support.AbstractParsingTest;
import org.gatein.integration.jboss.as7.support.KernelServices;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.Property;
import org.junit.Test;

import java.util.List;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

/**
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class SubsystemParsingTestCase extends AbstractParsingTest
{
   static enum Archive
   {
      GATEIN("gatein.ear"),
      GATEIN_SAMPLE_EXTENSION("gatein-sample-extension.ear"),
      GATEIN_SAMPLE_PORTAL("gatein-sample-portal.ear"),
      GATEIN_SAMPLE_SKIN("gatein-sample-skin.war");

      private String archive;

      public String getLocalName()
      {
         return archive;
      }

      private Archive(String name)
      {
         archive = name;
      }
   }

   static enum Module
   {
      ORG_GATEIN_LIB("org.gatein.lib"),
      ORG_GATEIN_WCI("org.gatein.wci"),
      JAVAX_PORTLET_API("javax.portlet.api");

      private String module;

      public String getLocalName()
      {
         return module;
      }

      private Module(String name)
      {
         module = name;
      }
   }

   /**
    * Tests that the xml is parsed into the correct operations
    */
   @Test
   public void testParseSubsystem() throws Exception
   {
      //Parse the subsystem xml
      String subsystemXml =
            "<subsystem xmlns=\"" + GateInExtension.NAMESPACE + "\">\n" +
                  "   <deployment-archives>\n" +
                  "      <archive name=\"" + Archive.GATEIN.getLocalName() + "\" main=\"true\" />\n" +
                  "      <archive name=\"" + Archive.GATEIN_SAMPLE_EXTENSION.getLocalName() + "\" />\n" +
                  "      <archive name=\"" + Archive.GATEIN_SAMPLE_PORTAL.getLocalName() + "\" />\n" +
                  "      <archive name=\"" + Archive.GATEIN_SAMPLE_SKIN.getLocalName() + "\" />\n" +
                  "   </deployment-archives>\n" +
                  "   <portlet-war-dependencies>\n" +
                  "      <dependency name=\"" + Module.ORG_GATEIN_LIB.getLocalName() + "\" import-services=\"true\" />\n" +
                  "      <dependency name=\"" + Module.ORG_GATEIN_WCI.getLocalName() + "\" />\n" +
                  "      <dependency name=\"" + Module.JAVAX_PORTLET_API.getLocalName() + "\" />\n" +
                  "   </portlet-war-dependencies>\n" +
                  "</subsystem>";

      List<ModelNode> operations = super.parse(subsystemXml);

      ///Check that we have the expected number of operations
      Assert.assertEquals(1, operations.size());

      //Check that the operation has correct content
      //Add subsystem operation is what we expect
      ModelNode addSubsystem = operations.get(0);
      Assert.assertEquals(ADD, addSubsystem.get(OP).asString());
      PathAddress addr = PathAddress.pathAddress(addSubsystem.get(OP_ADDR));
      Assert.assertEquals(1, addr.size());
      PathElement element = addr.getElement(0);
      Assert.assertEquals(SUBSYSTEM, element.getKey());
      Assert.assertEquals(GateInExtension.SUBSYSTEM_NAME, element.getValue());

      //We expect to have deployment-archives node set
      ModelNode deploymentArchives = addSubsystem.get(Constants.DEPLOYMENT_ARCHIVES);
      Assert.assertNotNull(Constants.DEPLOYMENT_ARCHIVES, deploymentArchives);

      List<Property> archives = deploymentArchives.asPropertyList();
      Assert.assertEquals(Archive.GATEIN.getLocalName(), archives.get(0).getName());
      Assert.assertEquals(Constants.MAIN, archives.get(0).getValue().asPropertyList().get(0).getName());
      Assert.assertEquals(1, archives.get(0).getValue().asPropertyList().size());

      Assert.assertEquals(Archive.GATEIN_SAMPLE_EXTENSION.getLocalName(), archives.get(1).getName());
      Assert.assertFalse(archives.get(1).getValue().isDefined());

      Assert.assertEquals(Archive.GATEIN_SAMPLE_PORTAL.getLocalName(), archives.get(2).getName());
      Assert.assertFalse(archives.get(2).getValue().isDefined());

      Assert.assertEquals(Archive.GATEIN_SAMPLE_SKIN.getLocalName(), archives.get(3).getName());
      Assert.assertFalse(archives.get(3).getValue().isDefined());


      ModelNode portletWarDependencies = addSubsystem.get(Constants.PORTLET_WAR_DEPENDENCIES);
      Assert.assertNotNull(Constants.PORTLET_WAR_DEPENDENCIES, portletWarDependencies);

      List<Property> modules = portletWarDependencies.asPropertyList();
      Assert.assertEquals(Module.ORG_GATEIN_LIB.getLocalName(), modules.get(0).getName());
      Assert.assertEquals(Module.ORG_GATEIN_WCI.getLocalName(), modules.get(1).getName());
      Assert.assertEquals(Module.JAVAX_PORTLET_API.getLocalName(), modules.get(2).getName());
   }

   /**
    * Test that the model created from the xml looks as expected
    */

   @Test
   public void testInstallIntoController() throws Exception
   {
      //Parse the subsystem xml and install into the controller
      String subsystemXml =
            "<subsystem xmlns=\"" + GateInExtension.NAMESPACE + "\">\n" +
                  "   <deployment-archives>\n" +
                  "      <archive name=\"" + Archive.GATEIN.getLocalName() + "\" main=\"true\" />\n" +
                  "      <archive name=\"" + Archive.GATEIN_SAMPLE_EXTENSION.getLocalName() + "\" />\n" +
                  "      <archive name=\"" + Archive.GATEIN_SAMPLE_PORTAL.getLocalName() + "\" />\n" +
                  "      <archive name=\"" + Archive.GATEIN_SAMPLE_SKIN.getLocalName() + "\" />\n" +
                  "   </deployment-archives>\n" +
                  "   <portlet-war-dependencies>\n" +
                  "      <dependency name=\"" + Module.ORG_GATEIN_LIB.getLocalName() + "\" import-services=\"true\" />\n" +
                  "      <dependency name=\"" + Module.ORG_GATEIN_WCI.getLocalName() + "\" />\n" +
                  "      <dependency name=\"" + Module.JAVAX_PORTLET_API.getLocalName() + "\" />\n" +
                  "   </portlet-war-dependencies>\n" +
                  "</subsystem>";
      KernelServices services = super.installInController(subsystemXml);

      //Read the whole model and make sure it looks as expected
      ModelNode model = services.readWholeModel();
      //Useful for debugging :-)
      //System.out.println(model);

      Assert.assertTrue(model.get(SUBSYSTEM).hasDefined(GateInExtension.SUBSYSTEM_NAME));
      model = model.get(SUBSYSTEM, GateInExtension.SUBSYSTEM_NAME);

      //We expect to have deployment-archives node set
      ModelNode deploymentArchives = model.get(Constants.DEPLOYMENT_ARCHIVES);
      Assert.assertNotNull(Constants.DEPLOYMENT_ARCHIVES, deploymentArchives);

      List<Property> archives = deploymentArchives.asPropertyList();
      Assert.assertEquals(Archive.GATEIN.getLocalName(), archives.get(0).getName());
      Assert.assertEquals(Constants.MAIN, archives.get(0).getValue().asPropertyList().get(0).getName());
      Assert.assertEquals(1, archives.get(0).getValue().asPropertyList().size());

      Assert.assertEquals(Archive.GATEIN_SAMPLE_EXTENSION.getLocalName(), archives.get(1).getName());
      Assert.assertFalse(archives.get(1).getValue().isDefined());

      Assert.assertEquals(Archive.GATEIN_SAMPLE_PORTAL.getLocalName(), archives.get(2).getName());
      Assert.assertFalse(archives.get(2).getValue().isDefined());

      Assert.assertEquals(Archive.GATEIN_SAMPLE_SKIN.getLocalName(), archives.get(3).getName());
      Assert.assertFalse(archives.get(3).getValue().isDefined());


      ModelNode portletWarDependencies = model.get(Constants.PORTLET_WAR_DEPENDENCIES);
      Assert.assertNotNull(Constants.PORTLET_WAR_DEPENDENCIES, portletWarDependencies);

      List<Property> modules = portletWarDependencies.asPropertyList();
      Assert.assertEquals(Module.ORG_GATEIN_LIB.getLocalName(), modules.get(0).getName());
      Assert.assertEquals(Module.ORG_GATEIN_WCI.getLocalName(), modules.get(1).getName());
      Assert.assertEquals(Module.JAVAX_PORTLET_API.getLocalName(), modules.get(2).getName());
   }

   /**
    * Starts a controller with a given subsystem xml and then checks that a second
    * controller started with the xml marshalled from the first one results in the same model
    */

   @Test
   public void testParseAndMarshalModel() throws Exception
   {
      //Parse the subsystem xml and install into the first controller
      String subsystemXml =
            "<subsystem xmlns=\"" + GateInExtension.NAMESPACE + "\">\n" +
                  "   <deployment-archives>\n" +
                  "      <archive name=\"" + Archive.GATEIN.getLocalName() + "\" main=\"true\" />\n" +
                  "      <archive name=\"" + Archive.GATEIN_SAMPLE_EXTENSION.getLocalName() + "\" />\n" +
                  "      <archive name=\"" + Archive.GATEIN_SAMPLE_PORTAL.getLocalName() + "\" />\n" +
                  "      <archive name=\"" + Archive.GATEIN_SAMPLE_SKIN.getLocalName() + "\" />\n" +
                  "   </deployment-archives>\n" +
                  "   <portlet-war-dependencies>\n" +
                  "      <dependency name=\"" + Module.ORG_GATEIN_LIB.getLocalName() + "\" import-services=\"true\" />\n" +
                  "      <dependency name=\"" + Module.ORG_GATEIN_WCI.getLocalName() + "\" />\n" +
                  "      <dependency name=\"" + Module.JAVAX_PORTLET_API.getLocalName() + "\" />\n" +
                  "   </portlet-war-dependencies>\n" +
                  "</subsystem>";

      KernelServices servicesA = super.installInController(subsystemXml);
      //Get the model and the persisted xml from the first controller
      ModelNode modelA = servicesA.readWholeModel();
      String marshalled = servicesA.getPersistedSubsystemXml();

      //Install the persisted xml from the first controller into a second controller
      KernelServices servicesB = super.installInController(marshalled);
      ModelNode modelB = servicesB.readWholeModel();

      //Make sure the models from the two controllers are identical
      super.compare(modelA, modelB);
   }

   /**
    * Starts a controller with the given subsystem xml and then checks that a second
    * controller started with the operations from its describe action results in the same model
    */

   @Test
   public void testDescribeHandler() throws Exception
   {
      //Parse the subsystem xml and install into the first controller
      String subsystemXml =
            "<subsystem xmlns=\"" + GateInExtension.NAMESPACE + "\">" +
                  "</subsystem>";
      KernelServices servicesA = super.installInController(subsystemXml);
      //Get the model and the describe operations from the first controller
      ModelNode modelA = servicesA.readWholeModel();
      ModelNode describeOp = new ModelNode();
      describeOp.get(OP).set(DESCRIBE);
      describeOp.get(OP_ADDR).set(
            PathAddress.pathAddress(
                  PathElement.pathElement(SUBSYSTEM, GateInExtension.SUBSYSTEM_NAME)).toModelNode());
      List<ModelNode> operations = super.checkResultAndGetContents(servicesA.executeOperation(describeOp)).asList();


      //Install the describe options from the first controller into a second controller
      KernelServices servicesB = super.installInController(operations);
      ModelNode modelB = servicesB.readWholeModel();

      //Make sure the models from the two controllers are identical
      super.compare(modelA, modelB);
   }

   /* TODO: test invalid configuration detection */
}
