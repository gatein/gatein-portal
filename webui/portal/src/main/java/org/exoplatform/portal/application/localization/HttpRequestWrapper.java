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

import org.exoplatform.portal.application.PortalRequestContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

/**
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class HttpRequestWrapper extends HttpServletRequestWrapper
{
   private static final List<Locale> EMPTY_LOCALE_LIST = Collections.emptyList();

   /**
    * Constructs a request object wrapping the given request.
    *
    * @throws IllegalArgumentException if the request is null
    */
   public HttpRequestWrapper(HttpServletRequest request)
   {
      super(request);
   }

   /**
    * Note: Keep the implementation here in sync with {@link org.exoplatform.portal.webui.application.ExoUserContext#getLocale}
    * @return
    */
   @Override
   public Locale getLocale()
   {
      if (PortalRequestContext.getCurrentInstance() != null)
         return getRequest().getLocale();
      
      Locale current = LocalizationFilter.getCurrentLocale();
      if (current != null)
         return current;

      return getRequest().getLocale();
   }

   @Override
   public Enumeration getLocales()
   {
      Locale current = LocalizationFilter.getCurrentLocale();
      if (PortalRequestContext.getCurrentInstance() != null || current == null)
         return getRequest().getLocales();

      Locale loc = getLocale();
      if (loc == null)
      {
         return Collections.enumeration(EMPTY_LOCALE_LIST);
      }
      else
      {
         LinkedList<Locale> locs = new LinkedList<Locale>();
         locs.add(loc);

         Enumeration<Locale> clientLocs = (Enumeration<Locale>) getRequest().getLocales();
         while (clientLocs.hasMoreElements())
         {
            current = clientLocs.nextElement();
            if (current.getLanguage().equals(loc.getLanguage()) && current.getCountry().equals(loc.getCountry()))
               continue;
            locs.add(current);
         }

         return Collections.enumeration(locs);
      }
   }
}
