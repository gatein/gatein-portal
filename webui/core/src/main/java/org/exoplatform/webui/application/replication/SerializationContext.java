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

package org.exoplatform.webui.application.replication;

import org.exoplatform.webui.application.replication.model.TypeDomain;
import org.exoplatform.webui.application.replication.serial.ObjectReader;
import org.exoplatform.webui.application.replication.serial.ObjectWriter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class SerializationContext
{

   /** . */
   private final TypeDomain typeDomain;

   /** . */
   private final Map<Class<?>, Object> creationContexts;

   public SerializationContext(TypeDomain typeDomain)
   {
      this.typeDomain = typeDomain;
      this.creationContexts = new HashMap<Class<?>, Object>();
   }

   public void addCreationContext(Object o)
   {
      creationContexts.put(o.getClass(), o);
   }

   public TypeDomain getTypeDomain()
   {
      return typeDomain;
   }

   public <C> C getContext(Class<C> contextType)
   {
      // This is ok
      return (C)creationContexts.get(contextType);
   }

   public <O> O clone(O o) throws IOException, ClassNotFoundException
   {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ObjectWriter writer = new ObjectWriter(this, baos);
      writer.writeObject(o);
      writer.close();
      ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
      ObjectReader in = new ObjectReader(this, bais);
      return (O)in.readObject();
   }
}
