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
import org.gatein.api.id.RenderingContext;
import org.gatein.portal.api.impl.GateInImpl;

/** @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a> */
public class ComplexApplicationId implements Id<Portlet>
{
   public static final String LOCAL = "local";
   public static final String START = LOCAL + ".";
   private final String category;
   private final String app;
   private final String portlet;
   private final Context context;
   private final String separator;

   public ComplexApplicationId(String category, String appName, String portletName, String separator, ComplexApplicationContext context)
   {
      this.category = category;
      this.app = appName;
      this.portlet = portletName;
      this.context = context;
      this.separator = separator;
   }

   @Override
   public String toString()
   {
      return toString(context);
   }

   public String toString(RenderingContext context)
   {
      if (this.context.equals(context))
      {
         if ("/".equals(separator))
         {
            // we're in the regular portletcontext case: local./applicationName.portletName
            return START + separator + app + '.' + portlet;
         }
         else
         {
            // we're in the application registry imported portlet: category/local._applicationName.portletName
            return category + '/' + START + separator + app + '.' + portlet;
         }
      }
      else
      {
         throw new IllegalArgumentException("ComplexApplicationIds can only be rendered by ComplexApplicationContext at the moment.");
      }
   }

   @Override
   public boolean equals(Object o)
   {
      if (this == o)
      {
         return true;
      }
      if (o == null || getClass() != o.getClass())
      {
         return false;
      }

      ComplexApplicationId that = (ComplexApplicationId)o;

      if (!app.equals(that.app))
      {
         return false;
      }
      if (category != null ? !category.equals(that.category) : that.category != null)
      {
         return false;
      }
      if (!portlet.equals(that.portlet))
      {
         return false;
      }

      return true;
   }

   @Override
   public int hashCode()
   {
      int result = category != null ? category.hashCode() : 0;
      result = 31 * result + app.hashCode();
      result = 31 * result + portlet.hashCode();
      return result;
   }

   public Class<Portlet> getIdentifiableType()
   {
      return Portlet.class;
   }

   public Id getIdForChild(String childId)
   {
      throw new IllegalArgumentException("ComplexApplicationId doesn't currently allow children Ids.");
   }

   public String getComponent(String component)
   {
      if (GateInImpl.APPLICATION_COMPONENT.equals(component))
      {
         return app;
      }
      else if (GateInImpl.PORTLET_COMPONENT.equals(component))
      {
         return portlet;
      }
      else if (GateInImpl.INVOKER_COMPONENT.equals(component))
      {
         return LOCAL;
      }
      else if (GateInImpl.CATEGORY_COMPONENT.equals(component))
      {
         return category;
      }
      else
      {
         throw new IllegalArgumentException("Unknown component '" + component + "' for ComplexApplicationId");
      }
   }

   public Context getOriginalContext()
   {
      return context;
   }

   public int getComponentNumber()
   {
      return 4;
   }

   public String getRootComponent()
   {
      return LOCAL;
   }

   public Id getParent()
   {
      return null;
   }

   public String[] getComponents()
   {
      return new String[]{category, LOCAL, app, portlet};
   }

   public void associateComponentWith(int componentIndex, String name)
   {
      // do nothing as it shouldn't be called
   }

   public boolean knowsComponent(String name)
   {
      return context.hasComponent(name);
   }

   public int compareTo(Id o)
   {
      return toString().compareTo(o.toString());
   }
}
