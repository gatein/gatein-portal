/*
* JBoss, a division of Red Hat
* Copyright 2008, Red Hat Middleware, LLC, and individual contributors as indicated
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
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

package org.gatein.portal.wsrp.state;

import org.chromattic.api.ChromatticSession;
import org.chromattic.api.format.FormatterContext;
import org.chromattic.api.format.ObjectFormatter;
import org.exoplatform.commons.chromattic.ChromatticLifeCycle;
import org.exoplatform.commons.chromattic.ChromatticManager;
import org.exoplatform.commons.chromattic.SessionContext;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.component.RequestLifeCycle;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class NewJCRPersister
{
   private AtomicBoolean initialized = new AtomicBoolean(false);
   private ChromatticManager manager;
   private ChromatticLifeCycle lifeCycle;
   private ExoContainer container;

   /** Same as defined in wsrp-configuration.xml */
   private static final String LIFECYCLE_NAME = "wsrp";

   private static class InstanceHolder
   {
      static final NewJCRPersister instance = new NewJCRPersister();
   }

   private NewJCRPersister()
   {
   }

   public ChromatticSession getSession()
   {
      manager.beginRequest();
//      lifeCycle.openContext();
      return lifeCycle.getChromattic().openSession();
   }

   public void closeSession(ChromatticSession session, boolean save)
   {
      session.save();
      session.close();
//      lifeCycle.closeContext(save);
      manager.endRequest(save);
   }

   public static NewJCRPersister getInstance(ExoContainer container)
   {
      // get the singleton
      NewJCRPersister persister = InstanceHolder.instance;

      // if it hasn't been initialized, do it
      if (!persister.initialized.get())
      {
         persister.initWith(container);
      }

      return persister;
   }

   private synchronized void initWith(ExoContainer container)
   {
      this.container = container;
      manager = (ChromatticManager) container.getComponentInstanceOfType(ChromatticManager.class);
      lifeCycle = manager.getLifeCycle(LIFECYCLE_NAME);
      initialized.set(true);
   }

   public static class QNameFormatter implements ObjectFormatter
   {
      private static final String OPEN_BRACE_REPLACEMENT = "-__";
      private static final String CLOSE_BRACE_REPLACEMENT = "__-";
      private static final String COLON_REPLACEMENT = "_-_";

      public String decodeNodeName(FormatterContext formatterContext, String s)
      {
         return decode(s);
      }

      public String encodeNodeName(FormatterContext formatterContext, String s)
      {
         return encode(s);
      }

      public String decodePropertyName(FormatterContext formatterContext, String s)
      {
         return decode(s);
      }

      public String encodePropertyName(FormatterContext formatterContext, String s)
      {
         return encode(s);
      }

      private String decode(String s)
      {
         return s.replace(CLOSE_BRACE_REPLACEMENT, "}").replace(OPEN_BRACE_REPLACEMENT, "{").replace(COLON_REPLACEMENT, ":");
      }

      private String encode(String s)
      {
         return s.replace("{", OPEN_BRACE_REPLACEMENT).replace("}", CLOSE_BRACE_REPLACEMENT).replace(":", COLON_REPLACEMENT);
      }
   }
}