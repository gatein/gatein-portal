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

package org.exoplatform.portal.application.replication;

import org.exoplatform.commons.utils.LazyList;
import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.services.organization.Query;
import org.exoplatform.commons.serialization.SerializationContext;
import org.exoplatform.commons.serialization.model.TypeDomain;
import org.exoplatform.commons.serialization.model.metadata.DomainMetaData;
import org.exoplatform.services.organization.impl.UserImpl;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
class SerializationContextSingleton
{

   /** . */
   private static SerializationContext instance = createInstance();

   public static SerializationContext getInstance()
   {
      return instance;
   }

   private static SerializationContext createInstance()
   {
      DomainMetaData domainMetaData = new DomainMetaData();

      // For now we need to mark the Query class as serialized
      domainMetaData.addClassType(Query.class, true);

      // Some other that need to be serialized
      domainMetaData.addClassType(ObjectPageList.class, true);
      domainMetaData.addClassType(UserImpl.class, true);
      domainMetaData.addClassType(LazyList.class, true);

      // Build domain
      TypeDomain domain = new TypeDomain(domainMetaData, true);

      // Build serialization context
      SerializationContext serializationContext = new SerializationContext(domain);
      serializationContext.addFactory(new UIComponentFactory());

      //
      return serializationContext;
   }
}
