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

package org.exoplatform.portal.gadget.core;

import org.apache.shindig.config.ContainerConfig;
import org.apache.shindig.gadgets.DefaultGuiceModule;

/**
 * The goal of the module is to bind the {@link org.apache.shindig.common.ContainerConfig} interface to the
 * {@link org.exoplatform.portal.gadget.core.ExoContainerConfig} implementation instead of the default
 * implementation annotated on the container config interface.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ExoModule extends DefaultGuiceModule
{

   @Override
   protected void configure()
   {
      //super.configure();

      //
      bind(ContainerConfig.class).to(ExoContainerConfig.class);
   }
}
