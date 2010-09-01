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
package org.exoplatform.portal.resource.compressor;

import org.exoplatform.component.test.AbstractKernelTest;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.portal.resource.compressor.impl.JSMinCompressorPlugin;
import org.exoplatform.portal.resource.compressor.impl.ResourceCompressorService;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

/**
 * @author <a href="trong.tran@exoplatform.com">Trong Tran</a>
 * @version $Revision$
 */

@ConfiguredBy({@ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/resource-compressor-service-configuration.xml")})
public class TestResourceCompressorService extends AbstractKernelTest
{
   public void testInitializing()
   {
      ResourceCompressorService compressor =
         (ResourceCompressorService)getContainer().getComponentInstanceOfType(ResourceCompressor.class);
      assertNotNull(compressor);
      assertTrue(compressor instanceof ResourceCompressorService);

      assertNotNull(compressor.getCompressorPlugin(ResourceType.JAVASCRIPT, "MockCompressorPlugin"));

      assertNotNull(compressor.getCompressorPlugin(ResourceType.JAVASCRIPT, "JSMinCompressorPlugin"));
   }

   public void testPriority()
   {
      ResourceCompressorService compressor =
         (ResourceCompressorService)getContainer().getComponentInstanceOfType(ResourceCompressor.class);
      ResourceCompressorPlugin plugin = compressor.getHighestPriorityCompressorPlugin(ResourceType.JAVASCRIPT);
      assertTrue(plugin instanceof JSMinCompressorPlugin);
   }

   public void testJSMinCompressing() throws IOException
   {
      File jsFile = new File("src/test/resources/javascript.js");
      File jsCompressedFile = new File("target/jsmin-compressed-file.js");

      Reader reader = new FileReader(jsFile);
      Writer writer = new FileWriter(jsCompressedFile);

      ResourceCompressorService compressor =
         (ResourceCompressorService)getContainer().getComponentInstanceOfType(ResourceCompressor.class);
      try
      {
         compressor.compress(reader, writer, ResourceType.JAVASCRIPT);
      }
      catch (Exception e)
      {
         fail(e.getLocalizedMessage());
      }
      writer.close();
      assertTrue(jsCompressedFile.length() > 0);
      assertTrue(jsFile.length() > jsCompressedFile.length());
      log.info("The original javascript (" + getFileSize(jsFile) + ") is compressed by JSMIN into "
         + jsCompressedFile.getAbsolutePath() + " (" + getFileSize(jsCompressedFile) + ")");
   }

   public void testYUICSSCompressing() throws IOException
   {
      File cssFile = new File("src/test/resources/Stylesheet.css");
      File compressedCssFile = new File("target/yui-compressed-file.css");

      Reader reader = new FileReader(cssFile);
      Writer writer = new FileWriter(compressedCssFile);

      ResourceCompressorService compressor =
         (ResourceCompressorService)getContainer().getComponentInstanceOfType(ResourceCompressor.class);
      try
      {
         compressor.compress(reader, writer, ResourceType.STYLESHEET);
      }
      catch (Exception e)
      {
         fail(e.getLocalizedMessage());
      }
      writer.close();
      assertTrue(compressedCssFile.length() > 0);
      assertTrue(cssFile.length() > compressedCssFile.length());
      log.info("The original CSS (" + getFileSize(cssFile) + ") is compressed by YUI library into "
         + compressedCssFile.getAbsolutePath() + " (" + getFileSize(compressedCssFile) + ")");
   }

   private String getFileSize(File file)
   {
      return (file.length() / 1024) + " KB";
   }
}
