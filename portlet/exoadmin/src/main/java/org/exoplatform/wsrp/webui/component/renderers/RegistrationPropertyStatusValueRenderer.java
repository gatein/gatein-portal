/*
 * JBoss, a division of Red Hat
 * Copyright 2010, Red Hat Middleware, LLC, and individual
 * contributors as indicated by the @authors tag. See the
 * copyright.txt in the distribution for a full listing of
 * individual contributors.
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

package org.exoplatform.wsrp.webui.component.renderers;

import org.exoplatform.webui.core.lifecycle.WebuiBindingContext;
import org.exoplatform.webui.core.renderers.ValueRenderer;
import org.gatein.wsrp.consumer.RegistrationProperty;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class RegistrationPropertyStatusValueRenderer extends ValueRenderer<RegistrationProperty.Status>
{
   @Override
   public String render(RegistrationProperty.Status value, WebuiBindingContext context) throws Exception
   {
      return context.appRes(value.getLocalizationKey());
   }

   @Override
   public String getCSSClassFor(RegistrationProperty.Status value)
   {
      return value.name();
   }
}
