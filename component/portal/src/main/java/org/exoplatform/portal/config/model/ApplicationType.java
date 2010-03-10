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

package org.exoplatform.portal.config.model;

import org.exoplatform.portal.pom.spi.gadget.Gadget;
import org.exoplatform.portal.pom.spi.portlet.Portlet;
import org.exoplatform.portal.pom.spi.wsrp.WSRP;
import org.gatein.mop.api.content.ContentType;

import java.io.ObjectStreamException;
import java.io.Serializable;

/**
 * The type of an application.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 * @param <S> the content state type of the application
 */
public class ApplicationType<S> implements Serializable
{

   public static ApplicationType<?> getType(String name)
   {
      if (PORTLET.getName().equals(name))
      {
         return ApplicationType.PORTLET;
      }
      else if (GADGET.getName().equals(name))
      {
         return ApplicationType.GADGET;
      }
      else if (WSRP_PORTLET.getName().equals(name))
      {
         return ApplicationType.WSRP_PORTLET;
      }
      else
      {
         return null;
      }
   }

   public static <S> ApplicationType<S> getType(ContentType<S> name)
   {
      if (PORTLET.getContentType().equals(name))
      {
         return (ApplicationType<S>)ApplicationType.PORTLET;
      }
      else if (GADGET.getContentType().equals(name))
      {
         return (ApplicationType<S>)ApplicationType.GADGET;
      }
      else if (WSRP_PORTLET.getContentType().equals(name))
      {
         return (ApplicationType<S>)ApplicationType.WSRP_PORTLET;
      }
      else
      {
         return null;
      }
   }

   /** . */
   public static final ApplicationType<Portlet> PORTLET = new ApplicationType<Portlet>(Portlet.CONTENT_TYPE, "portlet");

   /** . */
   public static final ApplicationType<Gadget> GADGET = new ApplicationType<Gadget>(Gadget.CONTENT_TYPE, "gadget");

   /** . */
   public static final ApplicationType<WSRP> WSRP_PORTLET = new ApplicationType<WSRP>(WSRP.CONTENT_TYPE, "wsrp");

   /** . */
   private transient final ContentType<S> contentType;

   /** . */
   private final String name;

   private ApplicationType(ContentType<S> contentType, String name)
   {
      this.contentType = contentType;
      this.name = name;
   }

   public ContentType<S> getContentType()
   {
      return contentType;
   }

   public String getName()
   {
      return name;
   }

   /**
    * This is needed to preserve the enumation aspect of this class.
    *
    * @return the correct instance
    * @throws ObjectStreamException never thrown
    */
   private Object readResolve() throws ObjectStreamException
   {
      return getType(name);
   }
}
