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
package org.exoplatform.web.controller.performance;

import com.sun.japex.JapexDriverBase;
import com.sun.japex.TestCase;
import org.exoplatform.web.controller.QualifiedName;
import org.exoplatform.web.controller.metadata.ControllerDescriptor;
import org.exoplatform.web.controller.metadata.DescriptorBuilder;
import org.exoplatform.web.controller.router.JRegexFactory;
import org.exoplatform.web.controller.router.RegexFactory;
import org.exoplatform.web.controller.router.RenderContext;
import org.exoplatform.web.controller.router.Router;
import org.exoplatform.web.controller.router.URIWriter;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class ControllerRendererDriver extends JapexDriverBase
{

   /** . */
   private RegexFactory regexFactory;

   /** . */
   private Router router;

   /** . */
   private RenderContext input;

   /** . */
   private URIWriter writer;

   @Override
   public void initializeDriver()
   {
      String regexFactoryName = getParam("regex");
      if (regexFactoryName.equals("jregex")) {
         regexFactory = JRegexFactory.INSTANCE;
      } else if (regexFactoryName.equals("java")) {
         regexFactory = RegexFactory.JAVA;
      }

      //
      this.regexFactory = regexFactory;
   }

   @Override
   public void prepare(TestCase testCase)
   {
      try
      {
         URL url = ControllerRendererDriver.class.getResource("controller.xml");
         DescriptorBuilder builder = new DescriptorBuilder();
         ControllerDescriptor descriptor = builder.build(url.openStream());
         Router router = descriptor.build(regexFactory);

         //
         Map<QualifiedName, String> input = new HashMap<QualifiedName, String>();
         String parameters = testCase.getParam("parameters");
         JSONObject o = new JSONObject(parameters);
         for (Iterator<String> i = o.keys();i.hasNext();)
         {
            String key = i.next();
            String value = (String)o.get(key);
            input.put(QualifiedName.parse(key), value);
         }

         //
         if (router.render(input) == null)
         {
            throw new Exception("Could not render " + input);
         }

         //
         this.router = router;
         this.input = new RenderContext(input);
         this.writer = new URIWriter(NullAppendable.INSTANCE);
      }
      catch (Exception e)
      {
         AssertionError afe = new AssertionError("Could not load controller configuration");
         afe.initCause(e);
         throw afe;
      }
   }

   @Override
   public void run(TestCase testCase)
   {
      try
      {
         router.render(input, writer);
         writer.reset(NullAppendable.INSTANCE);
      }
      catch (IOException e)
      {
         AssertionError err = new AssertionError("Unexpected IOException");
         err.initCause(e);
         throw err;
      }
   }

   @Override
   public void finish(TestCase testCase)
   {
      this.router = null;
      this.input = null;
      this.writer = null;
   }

   @Override
   public void terminateDriver()
   {
   }
}
