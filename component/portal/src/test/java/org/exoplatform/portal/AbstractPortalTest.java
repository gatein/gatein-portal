/*
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
package org.exoplatform.portal;

import org.exoplatform.commons.chromattic.ChromatticManager;
import org.exoplatform.component.test.*;
import org.exoplatform.container.PortalContainer;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class AbstractPortalTest extends AbstractKernelTest
{

   public AbstractPortalTest()
   {
   }

   public AbstractPortalTest(String name)
   {
      super(name);
   }

   @Override
   protected void end()
   {
      end(false);
   }

   protected void end(boolean save)
   {
      PortalContainer container = getContainer();
      ChromatticManager manager = (ChromatticManager)container.getComponentInstanceOfType(ChromatticManager.class);
      manager.getSynchronization().setSaveOnClose(save);
      super.end();
   }

   protected final void sync()
   {
      end();
      begin();
   }

   protected final void sync(boolean save)
   {
      end(save);
      begin();
   }

   @Override
   protected void setUp() throws Exception
   {
   }

   @Override
   protected void tearDown() throws Exception
   {
      //
   }
}
