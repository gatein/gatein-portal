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

package org.exoplatform.portal.mop.importer;

import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.model.PortalConfig;

/**
 * @author <a href="trongtt@gmail.com">Trong Tran</a>
 * @version $Revision$
 */
public class PortalConfigImporter
{
   /** . */
   private final PortalConfig src;

   /** . */
   private final DataStorage service;

   /** . */
   private final ImportMode mode;

   public PortalConfigImporter(ImportMode importMode, PortalConfig portal, DataStorage dataStorage_)
   {
      this.mode = importMode;
      this.src = portal;
      this.service = dataStorage_;
   }

   public void perform() throws Exception
   {
      PortalConfig existingPortalConfig = service.getPortalConfig(src.getType(), src.getName());
      PortalConfig dst = null;

      //
      switch (mode)
      {
         case CONSERVE:
            dst = null;
            break;
         case INSERT:
            if (existingPortalConfig == null)
            {
               dst = src;
            }
            else
            {
               dst = null;
            }
            break;
         case MERGE:
         case OVERWRITE:
            dst = src;
            break;
         default:
            throw new AssertionError();
      }
      
      if (dst != null)
      {
         if (existingPortalConfig == null)
         {
            service.create(dst);
         }
         else
         {
            service.save(dst);
         }
      }
   }
}
