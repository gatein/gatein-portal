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
package org.exoplatform.portal.resource;

import org.gatein.wci.WebAppEvent;
import org.gatein.wci.WebAppLifeCycleEvent;

/**
 * @author <a href="mailto:hoang281283@gmail.com">Minh Hoang TO</a>
 * @version $Id$
 *
 */
public class GateinSkinConfigRemoval extends AbstractResourceHandler
{
   
   private SkinService service;
   
   private String portalContainerName;
   
   public GateinSkinConfigRemoval(String _portalContainerName, SkinService _service){
      this.portalContainerName = _portalContainerName;
      this.service = _service;
   }
   
   /**
    * @see org.exoplatform.portal.resource.AbstractResourceHandler#onEvent(org.gatein.wci.WebAppEvent)
    */
   @Override
   public void onEvent(WebAppEvent event)
   {
      // TODO Auto-generated method stub
      if(event instanceof WebAppLifeCycleEvent){
         WebAppLifeCycleEvent waEvent = (WebAppLifeCycleEvent)event;
         if(waEvent.getType() == WebAppLifeCycleEvent.REMOVED){
            
         }
      }
   }

}
