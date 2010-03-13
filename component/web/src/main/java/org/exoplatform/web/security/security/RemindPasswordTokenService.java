/******************************************************************************
 * JBoss, a division of Red Hat                                               *
 * Copyright 2010, Red Hat Middleware, LLC, and individual                    *
 * contributors as indicated by the @authors tag. See the                     *
 * copyright.txt in the distribution for a full listing of                    *
 * individual contributors.                                                   *
 *                                                                            *
 * This is free software; you can redistribute it and/or modify it            *
 * under the terms of the GNU Lesser General Public License as                *
 * published by the Free Software Foundation; either version 2.1 of           *
 * the License, or (at your option) any later version.                        *
 *                                                                            *
 * This software is distributed in the hope that it will be useful,           *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU           *
 * Lesser General Public License for more details.                            *
 *                                                                            *
 * You should have received a copy of the GNU Lesser General Public           *
 * License along with this software; if not, write to the Free                *
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA         *
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.                   *
 ******************************************************************************/
package org.exoplatform.web.security.security;

import org.exoplatform.commons.chromattic.ChromatticManager;
import org.exoplatform.container.xml.InitParams;

/**
 * @author <a href="mailto:theute@redhat.com">Thomas Heute</a>
 * @version $Revision$
 */
public class RemindPasswordTokenService extends CookieTokenService {

   public RemindPasswordTokenService(InitParams initParams,
      ChromatticManager chromatticManager)
   {
      super(initParams, chromatticManager);
   }
	
   @Override
   protected String nextTokenId()
   {
      return "" + random.nextInt();
   }
}
