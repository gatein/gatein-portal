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

package org.exoplatform.portal.webui.application;

import org.exoplatform.portal.config.model.ApplicationState;
import org.exoplatform.portal.config.model.ApplicationType;

/**
 * Group the application state and the application id to form the state of a portlet.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 * @param <S> the application content state type
 */
public class PortletState<S>
{

   /** The application type. */
   private final ApplicationType<S> applicationType;

   /** The application state. */
   private ApplicationState<S> applicationState;

   public PortletState(ApplicationState<S> applicationState, ApplicationType<S> applicationType)
   {
      this.applicationState = applicationState;
      this.applicationType = applicationType;
   }

   public ApplicationType<S> getApplicationType()
   {
      return applicationType;
   }

   public ApplicationState<S> getApplicationState()
   {
      return applicationState;
   }

   public void setApplicationState(ApplicationState<S> applicationState)
   {
      this.applicationState = applicationState;
   }
}
