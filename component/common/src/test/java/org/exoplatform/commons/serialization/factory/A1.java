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

package org.exoplatform.commons.serialization.factory;

import org.exoplatform.commons.serialization.api.factory.ObjectFactory;
import org.exoplatform.commons.serialization.api.factory.CreateException;
import org.exoplatform.commons.serialization.model.FieldModel;

import java.util.Map;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class A1 extends ObjectFactory<A2>
{

   static A2 instance = new A2();

   @Override
   public <S extends A2> S create(Class<S> type, Map<FieldModel<? super S, ?>, ?> state) throws CreateException
   {
      if (type == A2.class)
      {
         return type.cast(instance);
      }
      else
      {
         throw new CreateException();
      }
   }
}
