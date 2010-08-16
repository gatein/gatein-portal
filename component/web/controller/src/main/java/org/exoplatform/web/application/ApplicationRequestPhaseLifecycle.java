/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.exoplatform.web.application;

/**
 * Interface that extends {@link ApplicationLifecycle} with request phase methods that allow interception of
 * before/after ACTION phase, and before/after RENDER phase of request processing.
 *
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public interface ApplicationRequestPhaseLifecycle<E extends RequestContext> extends ApplicationLifecycle<E>
{
   /**
    * Perform any processing required at the beginning of {@link Phase#ACTION} or {@link Phase#RENDER} phase.
    * @param app Application
    * @param context current RequestContext
    * @param phase starting phase
    */
   public void onStartRequestPhase(Application app, E context, Phase phase);

   /**
    * Perform any processing required at the end of {@link Phase#ACTION} or {@link Phase#RENDER} phase.
    * @param app Application
    * @param context current RequestContext
    * @param phase ending phase
    */
   public void onEndRequestPhase(Application app, E context, Phase phase);
}
