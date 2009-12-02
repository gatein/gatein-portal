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

import org.chromattic.api.Chromattic;
import org.exoplatform.portal.pom.spi.gadget.Gadget;
import org.exoplatform.portal.pom.spi.gadget.GadgetContentProvider;
import org.exoplatform.portal.pom.spi.portlet.Portlet;
import org.exoplatform.portal.pom.spi.portlet.PortletContentProvider;
import org.exoplatform.portal.pom.spi.wsrp.WSRP;
import org.exoplatform.portal.pom.spi.wsrp.WSRPContentProvider;
import org.gatein.mop.core.api.MOPService;
import org.gatein.mop.core.api.content.ContentManagerRegistry;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class PortalMOPService extends MOPService
{

   /** . */
   private final Chromattic chromattic;

   public PortalMOPService(Chromattic chromattic)
   {
      this.chromattic = chromattic;
   }

   @Override
   protected Chromattic getChromattic()
   {
      return chromattic;
   }

   @Override
   protected void configure(ContentManagerRegistry registry)
   {
      registry.register(Portlet.CONTENT_TYPE, new PortletContentProvider());
      registry.register(Gadget.CONTENT_TYPE, new GadgetContentProvider());
      registry.register(WSRP.CONTENT_TYPE, new WSRPContentProvider());
   }
}
