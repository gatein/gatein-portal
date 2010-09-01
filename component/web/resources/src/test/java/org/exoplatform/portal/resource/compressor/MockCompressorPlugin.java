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

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.portal.resource.compressor.BaseResourceCompressorPlugin;
import org.exoplatform.portal.resource.compressor.ResourceCompressorException;
import org.exoplatform.portal.resource.compressor.ResourceType;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

/**
 * A mock compressor plugin that just copy all characters from input to output
 * 
 * @author <a href="trong.tran@exoplatform.com">Trong Tran</a>
 * @version $Revision$
 */

public class MockCompressorPlugin extends BaseResourceCompressorPlugin
{
   public MockCompressorPlugin(InitParams params)
   {
      super(params);
   }

   public ResourceType getResourceType()
   {
      return ResourceType.JAVASCRIPT;
   }

   public void compress(Reader input, Writer output) throws ResourceCompressorException
   {
      int c;
      try
      {
         c = input.read();
         while (c != -1)
         {
            output.write(c);
            c = input.read();
         }
      }
      catch (IOException e)
      {
         throw new ResourceCompressorException(e);
      }
   }
}
