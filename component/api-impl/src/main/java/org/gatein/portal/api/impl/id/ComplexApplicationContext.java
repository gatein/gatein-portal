/*
* JBoss, a division of Red Hat
* Copyright 2008, Red Hat Middleware, LLC, and individual contributors as indicated
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

package org.gatein.portal.api.impl.id;

import org.gatein.api.content.Portlet;
import org.gatein.api.id.Context;
import org.gatein.api.id.Id;
import org.gatein.api.id.Identifiable;
import org.gatein.portal.api.impl.GateInImpl;

/** @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a> */
public class ComplexApplicationContext implements Context
{
   public static final Context INSTANCE = new ComplexApplicationContext();

   public String getName()
   {
      return "Application with prefixed invoker id";
   }

   public Id create(String rootComponent, String... additionalComponent)
   {
      return create(Identifiable.class, rootComponent, additionalComponent);
   }

   public <T extends Identifiable> Id<T> create(Class<T> type, String rootComponent, String... additionalComponents)
   {
      if (!type.isAssignableFrom(Portlet.class))
      {
         throw new IllegalArgumentException("ComplexApplicationContext can only create Id<Portlet>. Was asked to create Id<" + type.getSimpleName() + ">");
      }

      if (additionalComponents != null && additionalComponents.length == 3)
      {
         return (Id<T>)new ComplexApplicationId(rootComponent, additionalComponents[0], additionalComponents[1], additionalComponents[2], this);
      }
      else
      {
         throw new IllegalArgumentException("ComplexApplicationContext can only create Ids with 3 components: category name, application name and portlet name");
      }
   }

   public Id parse(String idAsString)
   {
      return parse(idAsString, Identifiable.class);
   }

   public <U extends Identifiable<U>> Id<U> parse(String idAsString, Class<U> expectedType)
   {
      final int index = idAsString.indexOf(ComplexApplicationId.START);
      if (index < 0)
      {
         throw new IllegalArgumentException("'" + idAsString + "' cannot be understood by ComplexApplicationContext. Understood format is '{category/}local.(_|/)applicationName.portletName'");
      }

      String category;
      if (index == 0)
      {
         // we're in the regular portletcontext case: local./applicationName.portletName
         category = null;
      }
      else
      {
         // we're in the application registry imported portlet: category/local._applicationName.portletName
         category = idAsString.substring(0, idAsString.indexOf('/'));
      }

      final int length = ComplexApplicationId.START.length();
      final int sepIndex = idAsString.indexOf('.', index + length);
      String separator = idAsString.substring(index + length, index + length + 1);
      String app = idAsString.substring(index + length + 1, sepIndex);
      String portlet = idAsString.substring(sepIndex + 1);

      return create(expectedType, category, app, portlet, separator);
   }

   public String toString(Id id)
   {
      if (id instanceof ComplexApplicationId)
      {
         return id.toString();
      }
      else
      {
         throw new IllegalArgumentException("ComplexApplicationContext cannot handle Ids that are not ComplexApplicationIds");
      }
   }

   public boolean isComponentRequired(String component)
   {
      return !GateInImpl.CATEGORY_COMPONENT.equals(component);
   }

   public boolean isComponentUnboundedHierarchical(String component)
   {
      return false;  // no components are hierarchical
   }

   public boolean hasComponent(String component)
   {
      return GateInImpl.APPLICATION_COMPONENT.equals(component) || GateInImpl.PORTLET_COMPONENT.equals(component) || GateInImpl.CATEGORY_COMPONENT.equals(component) || GateInImpl.INVOKER_COMPONENT.equals(component);
   }

   public void validateValueFor(String component, String value) throws IllegalArgumentException
   {
      // todo
   }
}
