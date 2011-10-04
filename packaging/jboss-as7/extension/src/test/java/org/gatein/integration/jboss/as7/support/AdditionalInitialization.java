/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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
package org.gatein.integration.jboss.as7.support;

import org.jboss.as.controller.ExtensionContext;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.as.controller.registry.Resource;
import org.jboss.msc.service.ServiceTarget;

/**
 * Allows you to additionally initialize the service container and the model controller
 * <p/>
 * beyond the subsystem being tested
 *
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 */
public interface AdditionalInitialization extends AdditionalParsers
{

   /**
    * Adds extra services to the service controller
    *
    * @param target the service controller target
    */
   void addExtraServices(ServiceTarget target);


   /**
    * Allows extra initialization of the model and addition of extra subsystems
    *
    * @param context          allows installation of extra subsystem extensions, call {@code extension.initialize(context)} for each extra extension you have
    * @param rootResource     the root model resource which allows you to for example add child elements to the model
    * @param rootRegistration the root resource registration which allows you to for example add additional operations to the model
    */
   void initializeExtraSubystemsAndModel(ExtensionContext context, Resource rootResource, ManagementResourceRegistration rootRegistration);

}
