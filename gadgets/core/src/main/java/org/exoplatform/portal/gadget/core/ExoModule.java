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

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

import org.apache.shindig.config.ContainerConfig;
import org.apache.shindig.gadgets.DefaultGuiceModule;
import org.apache.shindig.gadgets.http.HttpFetcher;
import org.apache.shindig.protocol.conversion.BeanAtomConverter;
import org.apache.shindig.protocol.conversion.BeanConverter;
import org.apache.shindig.protocol.conversion.BeanJsonConverter;
import org.apache.shindig.protocol.conversion.BeanXmlConverter;

/**
 * The goal of the module is to bind the {@link org.apache.shindig.common.ContainerConfig} interface to the
 * {@link org.exoplatform.portal.gadget.core.ExoContainerConfig} implementation instead of the default
 * implementation annotated on the container config interface.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ExoModule extends AbstractModule
{

   @Override
   protected void configure()
   {
      bind(ContainerConfig.class).to(ExoContainerConfig.class);
      bind(HttpFetcher.class).to(ExoHttpFetcher.class);
      
      bind(BeanConverter.class).annotatedWith(Names.named("shindig.bean.converter.xml")).to(BeanXmlConverter.class);
      bind(BeanConverter.class).annotatedWith(Names.named("shindig.bean.converter.json")).to(BeanJsonConverter.class);
      bind(BeanConverter.class).annotatedWith(Names.named("shindig.bean.converter.atom")).to(BeanAtomConverter.class);
   }
}
