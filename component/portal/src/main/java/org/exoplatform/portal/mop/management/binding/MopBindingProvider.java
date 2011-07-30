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

package org.exoplatform.portal.mop.management.binding;

import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.mop.management.binding.xml.NavigationMarshaller;
import org.exoplatform.portal.mop.management.binding.xml.PageMarshaller;
import org.exoplatform.portal.mop.management.binding.xml.SiteLayoutMarshaller;
import org.exoplatform.portal.pom.data.PortalData;
import org.gatein.management.api.ContentType;
import org.gatein.management.api.binding.BindingException;
import org.gatein.management.api.binding.BindingProvider;
import org.gatein.management.api.binding.Marshaller;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class MopBindingProvider implements BindingProvider
{
   public static final MopBindingProvider INSTANCE = new MopBindingProvider();

   private MopBindingProvider(){}

   @Override
   public <T> Marshaller<T> getMarshaller(Class<T> type, ContentType contentType) throws BindingException
   {
      switch (contentType)
      {
         case XML:
            return getXmlMarshaller(type);
         case JSON:
         case ZIP:
         default:
            return null;
      }
   }

   @SuppressWarnings("unchecked")
   private <T> Marshaller<T> getXmlMarshaller(Class<T> type)
   {
      if (Page.class.isAssignableFrom(type))
      {
         return (Marshaller<T>) XmlMarshallers.page_marshaller;
      }
      else if (Page.PageSet.class.isAssignableFrom(type))
      {
         return (Marshaller<T>) XmlMarshallers.pages_marshaller;
      }
      else if (PageNavigation.class.isAssignableFrom(type))
      {
         return (Marshaller<T>) XmlMarshallers.navigation_marshaller;
      }
      else if (PortalConfig.class.isAssignableFrom(type))
      {
         return (Marshaller<T>) XmlMarshallers.site_marshaller;
      }

      return null;
   }

   private static class XmlMarshallers
   {

      //------------------------------------ Page Marshallers ------------------------------------//
      private static Marshaller<Page.PageSet> pages_marshaller = new PageMarshaller();

      private static Marshaller<Page> page_marshaller = new Marshaller<Page>()
      {
         @Override
         public void marshal(Page page, OutputStream outputStream) throws BindingException
         {
            Page.PageSet pages = new Page.PageSet();
            pages.setPages(new ArrayList<Page>(1));
            pages.getPages().add(page);

            XmlMarshallers.pages_marshaller.marshal(pages, outputStream);
         }

         @Override
         public Page unmarshal(InputStream inputStream) throws BindingException
         {
            Page.PageSet pages = pages_marshaller.unmarshal(inputStream);

            if (pages.getPages().isEmpty()) throw new BindingException("No page was unmarshalled.");

            if (pages.getPages().size() != 1) throw new BindingException("Multiple pages found.");

            return pages.getPages().get(0);
         }
      };

      private static Marshaller<PageNavigation> navigation_marshaller = new NavigationMarshaller();

      private static Marshaller<PortalConfig> site_marshaller = new SiteLayoutMarshaller();
   }
}
