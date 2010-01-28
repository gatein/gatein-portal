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

package org.exoplatform.commons.serialization.model;

import org.exoplatform.commons.serialization.model.metadata.DomainMetaData;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public final class TypeDomain
{

   /** . */
   private static final Logger log = LoggerFactory.getLogger(TypeDomain.class);

   /** . */
   private final DomainMetaData metaData;

   /** . */
   private final Map<String, TypeModel<?>> typeModelMap;

   /** . */
   private final Map<String, TypeModel<?>> immutableTypeModelMap;

   /** . */
   private final Collection<TypeModel<?>> immutableTypeModelSet;

   /** . */
   private final boolean buildIfAbsent;

   /** . */
   private final Object lock;

   public TypeDomain(boolean putIfAbsent)
   {
      this(new DomainMetaData(), putIfAbsent);
   }

   public TypeDomain()
   {
      this(new DomainMetaData(), false);
   }

   public TypeDomain(DomainMetaData metaData)
   {
      this(metaData, false);
   }

   public TypeDomain(DomainMetaData metaData, boolean buildIfAbsent)
   {
      ConcurrentHashMap<String, TypeModel<?>> typeModelMap = new ConcurrentHashMap<String, TypeModel<?>>();
      Map<String, TypeModel<?>> immutableTypeModelMap = Collections.unmodifiableMap(typeModelMap);
      Collection<TypeModel<?>> immutableTypeModelSet = Collections.unmodifiableCollection(typeModelMap.values());

      //
      this.typeModelMap = typeModelMap;
      this.immutableTypeModelMap = immutableTypeModelMap;
      this.immutableTypeModelSet = immutableTypeModelSet;
      this.buildIfAbsent = buildIfAbsent;
      this.metaData = new DomainMetaData(metaData);
      this.lock = new Object();
   }

   public Map<String, TypeModel<?>> getTypeModelMap()
   {
      return immutableTypeModelMap;
   }

   public boolean getBuildIfAbsent()
   {
      return buildIfAbsent;
   }

   public Collection<TypeModel<?>> getTypeModels()
   {
      return immutableTypeModelSet;
   }

   public TypeModel<?> getTypeModel(String typeName)
   {
      if (typeName == null)
      {
         throw new NullPointerException();
      }
      return typeModelMap.get(typeName);
   }

   public <O> TypeModel<O> getTypeModel(Class<O> javaType)
   {
      return get(javaType, buildIfAbsent);
   }

   public <O> TypeModel<O> addTypeModel(Class<O> javaType)
   {
      return get(javaType, true);
   }

   // For now that operation is synchronized
   private <O> TypeModel<O> get(Class<O> javaType, boolean create)
   {
      if (javaType == null)
      {
         throw new NullPointerException();
      }

      // Cast OK
      TypeModel<O> model = (TypeModel<O>)typeModelMap.get(javaType.getName());

      //
      if (model == null && create)
      {
         synchronized (lock)
         {

            TypeModelBuilder builder = new TypeModelBuilder(metaData, immutableTypeModelMap);

            //
            model = builder.build(javaType);

            // Perform merge
            typeModelMap.putAll(builder.getAddedTypeModels());
         }
      }

      //
      return model;
   }

   public int getSize()
   {
      return typeModelMap.size();
   }

}
