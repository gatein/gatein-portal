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
package org.exoplatform.portal.application.concurrent;

import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.UserPortalConfig;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.webui.page.UISiteBody;
import org.exoplatform.portal.webui.portal.PageNodeEvent;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.PortalDataMapper;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIEditInlineWorkspace;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.portal.webui.workspace.UIWorkingWorkspace;
import org.exoplatform.web.application.Application;
import org.exoplatform.web.application.ApplicationLifecycle;
import org.exoplatform.web.application.JavascriptManager;
import org.exoplatform.web.application.RequestFailure;
import org.exoplatform.webui.event.Event;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;

/**
 * @author <a href="mailto:hoang281283@gmail.com">Minh Hoang TO</a>
 * @version $Id$
 *
 */
public class PortalConcurrencyMonitorLifecycle implements ApplicationLifecycle<PortalRequestContext>
{

   private final Logger log = LoggerFactory.getLogger(PortalConcurrencyMonitorLifecycle.class);
   
   /**
    * @see org.exoplatform.web.application.ApplicationLifecycle#onDestroy(org.exoplatform.web.application.Application)
    */
   public void onDestroy(Application app) throws Exception
   {
      // TODO Auto-generated method stub

   }

   /**
    * @see org.exoplatform.web.application.ApplicationLifecycle#onEndRequest(org.exoplatform.web.application.Application, org.exoplatform.web.application.RequestContext)
    */
   public void onEndRequest(Application app, PortalRequestContext context) throws Exception
   {
      // TODO Auto-generated method stub

   }

   /**
    * @see org.exoplatform.web.application.ApplicationLifecycle#onFailRequest(org.exoplatform.web.application.Application, org.exoplatform.web.application.RequestContext, org.exoplatform.web.application.RequestFailure)
    */
   public void onFailRequest(Application app, PortalRequestContext context, RequestFailure failureType) throws Exception
   {
      if(failureType == RequestFailure.CONCURRENCY_FAILURE)
      {
         //Concurrency-related handling should be put here in the future
         log.error("Error in editing resources concurrently:  " + context.getRequestURI().toString());
      }
   }

   /**
    * @see org.exoplatform.web.application.ApplicationLifecycle#onInit(org.exoplatform.web.application.Application)
    */
   public void onInit(Application app) throws Exception
   {
      // TODO Auto-generated method stub

   }

   /**
    * @see org.exoplatform.web.application.ApplicationLifecycle#onStartRequest(org.exoplatform.web.application.Application, org.exoplatform.web.application.RequestContext)
    */
   public void onStartRequest(Application app, PortalRequestContext context) throws Exception
   {
      // TODO Auto-generated method stub

   }

}
