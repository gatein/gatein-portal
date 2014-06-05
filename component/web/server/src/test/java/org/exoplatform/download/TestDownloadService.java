/*
 * Copyright (C) 2012 eXo Platform SAS.
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
package org.exoplatform.download;

import org.exoplatform.component.test.AbstractKernelTest;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.download.DownloadResource;
import org.exoplatform.download.DownloadService;
import org.exoplatform.download.MockDownloadResource;
import org.exoplatform.services.cache.ExoCache;
import org.junit.Test;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author <a href="hoang281283@gmail.com">Minh Hoang TO</a>
 * @date 11/19/12
 */
@ConfiguredBy(
   {
      @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/services/download-service.xml")
   })
public class TestDownloadService extends AbstractKernelTest
{

   private DownloadService service;

   private ExoCache<String, DownloadResource> cache;

   @Override
   protected void beforeRunBare()
   {
      super.beforeRunBare();
      service = (DownloadService)getContainer().getComponentInstanceOfType(DownloadService.class);
      cache = service.getCache();
   }

   @Test
   public void testMaxSizeEviction()
   {
      for (int i = 0; i < 20; i++)
      {
         service.addDownloadResource(new MockDownloadResource("" + i));
      }
      assertTrue(cache.getCacheSize() <= 10);
      cache.clearCache();
   }

   @Test
   public void testCleanningOnGetting()
   {
      DownloadResource s = new MockDownloadResource("blah");
      service.addDownloadResource(s);
      assertNotNull(service.getDownloadResource("" + s.hashCode()));
      assertNull(cache.get("" + s.hashCode()));
      cache.clearCache();
   }
}
