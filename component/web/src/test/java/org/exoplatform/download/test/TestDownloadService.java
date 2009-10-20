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

package org.exoplatform.download.test;

import org.exoplatform.download.DownloadService;
import org.exoplatform.test.BasicTestCase;

/**
 * Created by The eXo Platform SARL
 * Author : Tuan Nguyen
 *          tuan08@users.sourceforge.net
 * Dec 26, 2005
 */
public class TestDownloadService extends BasicTestCase
{

   private DownloadService service_;

   public TestDownloadService(String name)
   {
      super(name);
   }

   public void setUp() throws Exception
   {
      if (service_ != null)
         return;
      //    PortalContainer manager = PortalContainer.getInstance() ;
      //    service_ = (DownloadService)manager.getComponentInstanceOfType(DownloadService.class) ;      
   }

   public void testDownloadService() throws Exception
   {
      assertTrue("expect service is inited", service_ == null);
   }

   protected String getDescription()
   {
      return "Test Download Service";
   }
}
