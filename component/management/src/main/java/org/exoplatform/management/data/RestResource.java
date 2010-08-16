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

package org.exoplatform.management.data;

import org.exoplatform.management.ValueWrapper;
import org.exoplatform.management.invocation.MethodInvoker;
import org.exoplatform.management.annotations.ImpactType;
import org.exoplatform.management.spi.ManagedMethodMetaData;
import org.exoplatform.management.spi.ManagedPropertyMetaData;
import org.exoplatform.management.spi.ManagedResource;
import org.exoplatform.management.spi.ManagedTypeMetaData;
import org.exoplatform.services.rest.impl.ApplicationContextImpl;
import org.exoplatform.services.rest.impl.MultivaluedMapImpl;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.MessageBodyReader;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class RestResource
{

   /** . */
   final Map<String, RestResourceProperty> properties;

   /** . */
   private final List<RestResourceMethod> methods;

   /** . */
   private final ManagedResource managedResource;

   /** . */
   private final String name;

   /** . */
   private final String description;

   public RestResource(String name, ManagedResource managedResource)
   {
      ManagedTypeMetaData managedType = managedResource.getMetaData();

      //
      HashMap<String, RestResourceProperty> properties = new HashMap<String, RestResourceProperty>();
      for (ManagedPropertyMetaData managedProperty : managedType.getProperties()) {
         RestResourceProperty resourceProperty = new RestResourceProperty(managedProperty);
         properties.put(resourceProperty.getName(), resourceProperty);
      }

      //
      List<RestResourceMethod> methods = new ArrayList<RestResourceMethod>();
      for (ManagedMethodMetaData managedMethod : managedType.getMethods()) {
         RestResourceMethod resourceMethod = new RestResourceMethod(managedMethod);
         methods.add(resourceMethod);
      }

      //
      this.name = name;
      this.description = managedType.getDescription();
      this.managedResource = managedResource;
      this.properties = Collections.unmodifiableMap(properties);
      this.methods = methods;
   }

   public String getName()
   {
      return name;
   }

   public String getDescription()
   {
      return description;
   }

   public Collection<RestResourceProperty> getProperties()
   {
      return properties.values();
   }

   public Collection<RestResourceMethod> getMethods()
   {
      return methods;
   }

   @GET
   @Produces(MediaType.APPLICATION_JSON)
   public RestResource get()
   {
      return this;
   }

   @GET
   @Path("{name}")
   @Produces(MediaType.APPLICATION_JSON)
   public Object get(@Context UriInfo info, @PathParam("name") String name)
   {
      MultivaluedMap<String, String> parameters = info.getQueryParameters();
      
      // Try first to get a property
      RestResourceProperty property = properties.get(name);
      if (property != null)
      {
         MethodInvoker getter = property.getGetterInvoker();
         if (getter != null)
         {
            return safeInvoke(getter, parameters);
         }
      }

      //
      return tryInvoke(name, parameters, ImpactType.READ);
   }

   @PUT
   @Path("{name}")
   @Produces(MediaType.APPLICATION_JSON)
   public Object put(@Context UriInfo info, @PathParam("name") String name)
   {
      MultivaluedMap<String, String> parameters = getParameters(info);
      // Try first to get a property
      RestResourceProperty property = properties.get(name);
      if (property != null)
      {
         MethodInvoker setter = property.getSetterInvoker();
         if (setter != null)
         {
            return safeInvoke(setter, parameters);
         }
      }

      //
      return tryInvoke(name, parameters, ImpactType.IDEMPOTENT_WRITE);
   }

   @POST
   @Path("{name}")
   @Produces(MediaType.APPLICATION_JSON)
   public Object post(@Context UriInfo info, @PathParam("name") String name)
   {
      return tryInvoke(name, getParameters(info), ImpactType.WRITE);
   }

   /**
    * Try to invoke a method with matching parameters from the query string
    *
    * @param methodName the method name to invoke
    * @param info the uri info
    * @param impact the expected impact
    * @return a suitable response
    */
   private Object tryInvoke(String methodName, MultivaluedMap<String, String> parameters, ImpactType impact)
   {
      //
      RestResourceMethod method = lookupMethod(methodName, parameters.keySet(), impact);

      //
      if (method != null)
      {
         MethodInvoker invoker = method.getMethodInvoker();
         return safeInvoke(invoker, parameters);
      }

      //
      return null;
   }

   private RestResourceMethod lookupMethod(String methodName, Set<String> argNames, ImpactType impact)
   {
      for (RestResourceMethod method : methods)
      {
         if (method.getName().equals(methodName) && method.metaData.getImpact() == impact && method.parameterNames.equals(argNames))
         {
            return method;
         }
      }
      return null;
   }

   /**
    * Invoke safely a method.
    *
    * @param invoker the method to invoke
    * @param argMap the arguments
    * @return the ok response or an object returned by the method wrapped by {@link ValueWrapper}
    */
   private Object safeInvoke(MethodInvoker invoker, Map<String, List<String>> argMap)
   {
      Object resource = managedResource.getResource();

      //
      managedResource.beforeInvoke(resource);

      //
      try
      {
         Object ret = invoker.invoke(resource, argMap);
         return ret == null ? Response.ok() : ValueWrapper.wrap(ret);
      }
      catch (Exception e)
      {
         e.printStackTrace();
         return Response.serverError();
      }
      finally
      {
         managedResource.afterInvoke(resource);
      }
   }
   
   @SuppressWarnings("unchecked")
   private MultivaluedMap<String, String> getParameters(UriInfo info)
   {
      MultivaluedMap<String, String> parameters = info.getQueryParameters();
      ApplicationContextImpl context = (ApplicationContextImpl)info;      
      
      Type formType = (ParameterizedType)MultivaluedMapImpl.class.getGenericInterfaces()[0];
      MediaType contentType = context.getHttpHeaders().getMediaType();
      if (contentType == null) {
         contentType = MediaType.APPLICATION_FORM_URLENCODED_TYPE;
      }      

      MultivaluedMap<String, String> form = new MultivaluedMapImpl();      
      try {
         MessageBodyReader reader =
            context.getProviders().getMessageBodyReader(MultivaluedMap.class, formType, null, contentType);
         if (reader != null) {
            form = (MultivaluedMap<String, String>)reader.readFrom(MultivaluedMap.class, formType, null, contentType, context
               .getHttpHeaders().getRequestHeaders(), context.getContainerRequest().getEntityStream());
         }
      } catch (Exception e) {         
      }
      
      parameters.putAll(form);
      return parameters;
   }
   
}
