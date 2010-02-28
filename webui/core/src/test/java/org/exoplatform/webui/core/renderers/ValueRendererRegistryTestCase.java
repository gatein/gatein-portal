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

package org.exoplatform.webui.core.renderers;

import org.exoplatform.component.test.AbstractGateInTest;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class ValueRendererRegistryTestCase extends AbstractGateInTest
{
   private ValueRendererRegistry registry;

   @Override
   protected void setUp() throws Exception
   {
      registry = new ValueRendererRegistry();
   }

   public void testGetDefaultRenderer()
   {
      ValueRenderer<? super Object> renderer = registry.getRendererFor(Object.class);
      assertEquals(ValueRenderer.DEFAULT_RENDERER, renderer);
      assertEquals(ValueRenderer.DEFAULT_CSS_CLASS, renderer.getCSSClassFor(new Object()));
   }

   public void testRegisterLocalRenderer()
   {
      SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
      String css = "testDate";
      registry.registerRendererFor(new FormattableValueRenderer<Date>(dateFormat, css), TestDate.class);

      TestDate date = new TestDate(2010, 1, 12, 13, 37, 0);
      ValueRenderer<? super TestDate> renderer = registry.getRendererFor(date);
      assertNotNull(renderer);
      assertEquals(dateFormat.format(date), renderer.render(date));
      assertEquals(css, renderer.getCSSClassFor(date));
   }

   public void testRenderNullValue()
   {
      ValueRenderer<? super Object> renderer = registry.getRendererFor(null);
      assertNotNull(renderer);
      assertEquals(ValueRenderer.EMPTY, renderer.render(null));
      assertEquals(ValueRenderer.EMPTY, renderer.getCSSClassFor(null));
   }

   public void testSupportsPreviousUIGridScenario()
   {
      ValueRenderer<? super Integer> renderer = registry.getRendererFor(Integer.class);
      assertNotNull(renderer);
      assertEquals("100", renderer.render(100));
      assertEquals("number", renderer.getCSSClassFor(100));

      SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss yyyy-MM-dd");
      TestDate date = new TestDate(2010, 1, 12, 13, 37, 0);
      ValueRenderer<? super TestDate> renderer2 = registry.getRendererFor(date);
      assertNotNull(renderer);
      assertEquals(dateFormat.format(date), renderer2.render(date));
      assertEquals("Datetime", renderer2.getCSSClassFor(date));
   }

   private static class TestDate extends Date
   {
      private TestDate(int year, int month, int date, int hrs, int min, int sec)
      {
         super(year, month, date, hrs, min, sec);
      }
   }
}
