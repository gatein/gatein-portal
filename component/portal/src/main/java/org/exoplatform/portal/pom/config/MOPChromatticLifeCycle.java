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
package org.exoplatform.portal.pom.config;

import org.exoplatform.commons.chromattic.ChromatticLifeCycle;
import org.exoplatform.commons.chromattic.SessionContext;
import org.exoplatform.container.xml.InitParams;

/**
 * Extends the chromattic life cycle to associate the mop session as an attachment of the chromattic session.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class MOPChromatticLifeCycle extends ChromatticLifeCycle
{

   /** . */
   POMSessionManager manager;

   public MOPChromatticLifeCycle(InitParams params)
   {
      super(params);
   }

   @Override
   protected void onOpenSession(SessionContext context)
   {
      POMSession session = new POMSession(manager, this, context);
      context.setAttachment("mopsession", session);
   }

   @Override
   protected void onCloseSession(SessionContext context)
   {
   }
}
