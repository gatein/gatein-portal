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

package org.exoplatform.commons;

import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.container.configuration.ConfigurationException;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.naming.BindReferencePlugin;
import org.exoplatform.services.naming.InitialContextInitializer;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.stream.XMLStreamException;
import java.io.FileNotFoundException;

/**
 * This code should be moved in the core, for now it is here as it is needed here.
 * It extends the {@link org.exoplatform.services.naming.InitialContextInitializer} to override the
 * {@link #addPlugin(org.exoplatform.container.component.ComponentPlugin)} method and perform no binding
 * if there is an existing binding before. 
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class InitialContextInitializer2 extends InitialContextInitializer
{

   public InitialContextInitializer2(InitParams params) throws NamingException, ConfigurationException, FileNotFoundException, XMLStreamException
   {
      super(params);
   }

   @Override
   public void addPlugin(ComponentPlugin plugin)
   {
      if (plugin instanceof BindReferencePlugin)
      {
         BindReferencePlugin brplugin = (BindReferencePlugin)plugin;
         InitialContext initialContext = getInitialContext();
         try
         {
            initialContext.lookup(brplugin.getBindName());
            // If we reach this step it means that something is already bound
         }
         catch (NamingException e)
         {
            super.addPlugin(plugin);
         }
      }
   }
}
