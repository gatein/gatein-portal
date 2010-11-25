/*
 * Copyright (C) 2010 eXo Platform SAS.
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

package org.exoplatform.application.gadget;

import org.exoplatform.application.gadget.impl.GadgetDefinition;
import org.exoplatform.application.gadget.impl.RemoteGadgetData;
import org.gatein.common.net.URLTools;

import java.io.IOException;
import java.net.URL;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class RemoteImporter extends GadgetImporter
{

   public RemoteImporter(String name, String gadgetPath)
   {
      super(name, gadgetPath);
   }

   @Override
   protected byte[] getGadgetBytes(String gadgetURI) throws IOException
   {
      URL url = new URL(gadgetURI);
      return URLTools.getContent(url, 5000, 5000);
   }

   @Override
   protected String getGadgetURL(String gadgetURI) throws Exception
   {
      return "http://www.gatein.org";
   }

   @Override
   protected void process(String gadgetURI, GadgetDefinition def) throws Exception
   {
      def.setLocal(false);

      //
      RemoteGadgetData data = (RemoteGadgetData)def.getData();

      // Set remote URL
      data.setURL(gadgetURI);
   }
}
