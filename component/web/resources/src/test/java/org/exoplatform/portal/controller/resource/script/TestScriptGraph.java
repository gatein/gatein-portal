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

package org.exoplatform.portal.controller.resource.script;

import org.exoplatform.component.test.AbstractGateInTest;
import org.exoplatform.portal.controller.resource.ResourceId;
import org.exoplatform.portal.controller.resource.ResourceScope;
import org.exoplatform.portal.controller.resource.script.FetchMode;
import org.exoplatform.portal.controller.resource.script.ScriptGraph;
import org.exoplatform.portal.controller.resource.script.ScriptResource;
import org.gatein.common.util.Tools;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

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

   /** . */
   private static final ResourceId D = new ResourceId(ResourceScope.PORTAL, "D");

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
      ScriptGraph graph = new ScriptGraph();
      
      // A -> B
      ScriptResource a = graph.addResource(A);
      a.addDependency(B);

      // C -> D
      ScriptResource c = graph.addResource(C);
      c.addDependency(D);
      
      // B -> C
      ScriptResource b = graph.addResource(B);
      b.addDependency(C);

      ScriptResource d = graph.addResource(D);

      assertEquals(Tools.toSet(D), c.getClosure());
      
      assertEquals(Tools.toSet(C, D), b.getClosure());
      
      assertEquals(Tools.toSet(B, C, D), a.getClosure());
   }

   public void testFetchMode()
   {
      // Resource fetch mode should affect the resource
      ScriptGraph graph = new ScriptGraph();
      ScriptResource a = graph.addResource(A, FetchMode.ON_LOAD);
      ScriptResource b = graph.addResource(B, FetchMode.IMMEDIATE);
      ScriptResource c = graph.addResource(C, FetchMode.IMMEDIATE);
      a.addDependency(C);
      b.addDependency(C);

      //
      Map<ScriptResource, FetchMode> resolution = graph.resolve(Collections.singleton(A));
      assertEquals(2, resolution.size());
      assertEquals(Tools.toSet(a, c), resolution.keySet());
      assertEquals(FetchMode.ON_LOAD, resolution.get(a));
      assertEquals(FetchMode.ON_LOAD, resolution.get(c));

      //
      resolution = graph.resolve(Collections.singleton(B));
      assertEquals(2, resolution.size());
      assertEquals(Tools.toSet(b, c), resolution.keySet());
      assertEquals(FetchMode.IMMEDIATE, resolution.get(b));
      assertEquals(FetchMode.IMMEDIATE, resolution.get(c));

      //
      resolution = graph.resolve(Arrays.asList(A, B));
      assertEquals(3, resolution.size());
      assertEquals(Tools.toSet(a, b, c), resolution.keySet());
      assertEquals(FetchMode.ON_LOAD, resolution.get(a));
      assertEquals(FetchMode.IMMEDIATE, resolution.get(b));
      assertEquals(FetchMode.IMMEDIATE, resolution.get(c));
   }
}
