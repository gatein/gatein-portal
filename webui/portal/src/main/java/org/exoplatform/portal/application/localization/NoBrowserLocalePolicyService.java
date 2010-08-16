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
package org.exoplatform.portal.application.localization;

import org.exoplatform.services.resources.LocaleContextInfo;

import java.util.Locale;

/**
 * This implementation of {@link org.exoplatform.services.resources.LocalePolicy} disregards client browser language preference.
 * Localization will therefore not be affected by different OS or browser language settings. 
 *
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class NoBrowserLocalePolicyService extends DefaultLocalePolicyService
{
   /**
    * Override super method with no-op.
    *
    * @param context locale context info available to implementations in order to determine appropriate Locale
    * @return null
    */
   @Override
   protected Locale getLocaleConfigFromBrowser(LocaleContextInfo context)
   {
      return null;
   }
}
