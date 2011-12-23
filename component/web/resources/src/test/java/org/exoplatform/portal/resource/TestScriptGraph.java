/*
 * Copyright (C) 2011 eXo Platform SAS.
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

package org.exoplatform.portal.resource;

import org.exoplatform.component.test.AbstractGateInTest;
import org.exoplatform.portal.controller.resource.ResourceId;
import org.exoplatform.portal.controller.resource.ResourceScope;
import org.exoplatform.portal.controller.resource.script.ScriptGraph;
import org.exoplatform.portal.controller.resource.script.ScriptResource;
import org.gatein.common.util.Tools;

import java.util.Collections;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class TestScriptGraph extends AbstractGateInTest
{

   /** . */
   private static final ResourceId A = new ResourceId(ResourceScope.SHARED, "A");

   /** . */
   private static final ResourceId B = new ResourceId(ResourceScope.SHARED, "B");

   /** . */
   private static final ResourceId C = new ResourceId(ResourceScope.SHARED, "C");
   
   public void testDetectCycle1()
   {
      ScriptGraph graph = new ScriptGraph();
      ScriptResource a = graph.addResource(A);
      ScriptResource b = graph.addResource(B);
      a.addDependency(B);
      try
      {
         b.addDependency(A);
         fail();
      }
      catch (IllegalStateException ignore)
      {
      }
   }

   public void testDetectCycle2()
   {
      ScriptGraph graph = new ScriptGraph();
      ScriptResource a = graph.addResource(A);
      ScriptResource b = graph.addResource(B);
      ScriptResource c = graph.addResource(C);
      a.addDependency(B);
      b.addDependency(C);
      try
      {
         c.addDependency(A);
         fail();
      }
      catch (IllegalStateException ignore)
      {
      }
   }
   
   public void testClosure()
   {
      ScriptGraph graph = new ScriptGraph();
      ScriptResource a = graph.addResource(A);
      a.addDependency(B);
      ScriptResource b = graph.addResource(B);
      b.addDependency(C);
      ScriptResource c = graph.addResource(C);
      assertEquals(Tools.toSet(B, C), a.getClosure());
      assertEquals(Tools.toSet(C), b.getClosure());
      assertEquals(Collections.emptySet(), c.getClosure());
   }

   /**
    * Closure of any node depends on node relationships in graph but does not depend on
    * the order of building graph nodes
    */
   public void testBuildingOrder()
   {
      //We expect that closure won't depend on building order of nodes (ScriptResource) in graph
      ScriptGraph graph = new ScriptGraph();
      ScriptResource b = graph.addResource(B);
      b.addDependency(C);
      ScriptResource a = graph.addResource(A);
      a.addDependency(B);
      ScriptResource c = graph.addResource(C);

      assertEquals(Tools.toSet(C), b.getClosure());

      //Although C is added as dependency of b before a is created, C must appear in a 's closure
      assertEquals(Tools.toSet(B, C), a.getClosure());
   }

   /**
    * Update on closure of a node is propagated to its ancestor 's closure
    */
   public void testClosurePropagate()
   {
      ResourceId D = new ResourceId(ResourceScope.PORTAL, "D");
      ScriptGraph graph = new ScriptGraph();
      ScriptResource a = graph.addResource(A);
      ScriptResource b = graph.addResource(B);
      ScriptResource c = graph.addResource(C);
      ScriptResource d = graph.addResource(D);

      a.addDependency(B);
      b.addDependency(C);
      c.addDependency(D);

      assertEquals(Tools.toSet(B, C, D), a.getClosure());
   }
}
