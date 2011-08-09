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

package org.gatein.portal.api.impl.content;

import org.exoplatform.application.registry.Application;
import org.exoplatform.application.registry.ApplicationCategory;
import org.exoplatform.application.registry.ApplicationRegistryService;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.pom.spi.portlet.Portlet;
import org.exoplatform.portal.pom.spi.wsrp.WSRP;
import org.gatein.api.content.Category;
import org.gatein.api.content.Content;
import org.gatein.api.content.ContentRegistry;
import org.gatein.api.content.Gadget;
import org.gatein.api.content.ManagedContent;
import org.gatein.api.id.Id;
import org.gatein.api.util.IterableCollection;
import org.gatein.api.util.Type;
import org.gatein.common.util.ParameterValidation;
import org.gatein.mop.api.content.ContentType;
import org.gatein.portal.api.impl.GateInImpl;
import org.gatein.portal.api.impl.util.AdaptedIterableCollection;

import java.util.ArrayList;
import java.util.List;

/** @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a> */
public class CategoryImpl implements Category
{
   private final ApplicationCategory category;
   private final GateInImpl gateIn;
   private final ContentRegistry registry;

   public CategoryImpl(ApplicationCategory category, ContentRegistry registry, GateInImpl gateIn)
   {
      this.category = category;
      this.registry = registry;
      this.gateIn = gateIn;
   }

   public IterableCollection<ManagedContent> getManagedContents()
   {
      List<Application> applications = category.getApplications();
      return new AdaptedIterableCollection<Application, ManagedContent>(applications.size(), applications.iterator())
      {
         public ManagedContent adapt(Application old)
         {
            return new ManagedContentImpl(old.getApplicationName(), GateInImpl.getContentIdFrom(old), old, CategoryImpl.this);
         }

         public boolean contains(ManagedContent managedContent)
         {
            return getApplication(managedContent.getName()) != null;
         }
      };
   }

   @Override
   public String toString()
   {
      StringBuilder sb = new StringBuilder("Category '").append(getName()).append("':\n");

      for (ManagedContent content : getManagedContents())
      {
         sb.append('\t').append(content).append('\n');
      }

      return sb.toString();
   }

   ContentRegistry getRegistry()
   {
      return registry;
   }

   public boolean contains(String managedContentName)
   {
      return getApplication(managedContentName) != null;
   }

   private Application getApplication(String managedContentName)
   {
      try
      {
         gateIn.begin();
         return gateIn.getRegistryService().getApplication(category.getName(), managedContentName);
      }
      catch (Exception e)
      {
         return null;
      }
      finally
      {
         gateIn.end();
      }
   }

   public <T extends Content> ManagedContent<T> addContent(Id<T> contentId, String name)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(contentId, "Content Id");
      GateInImpl.MANAGED_CONTENT_CONTEXT.validateValueFor("name", name);

      final ContentType<?> contentType = getContentTypeFor(contentId);

      // default permissions by default
      ArrayList<String> permissions = new ArrayList<String>();
      permissions.add(UserACL.EVERYONE);

      try
      {
         gateIn.begin();

         final ApplicationRegistryService registryService = gateIn.getRegistryService();
         final Application application = registryService.createOrUpdateApplication(category.getName(), name, contentType, contentId.toString(), name, null, permissions);
         return new ManagedContentImpl<T>(name, contentId, application, this);
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
      finally
      {
         gateIn.end();
      }
   }

   private ContentType<?> getContentTypeFor(Type type)
   {
      if (Content.GADGET.equals(type))
      {
         return org.exoplatform.portal.pom.spi.gadget.Gadget.CONTENT_TYPE;
      }
      else if (Content.PORTLET.equals(type))
      {
         return Portlet.CONTENT_TYPE;
      }
      else if (Content.WSRP.equals(type))
      {
         return WSRP.CONTENT_TYPE;
      }
      else
      {
         throw new IllegalArgumentException("Unknown Content type: " + type);
      }
   }

   private ContentType<?> getContentTypeFor(Id id)
   {
      final Class type = id.getIdentifiableType();
      if (Gadget.class.equals(type))
      {
         return org.exoplatform.portal.pom.spi.gadget.Gadget.CONTENT_TYPE;
      }
      else if (org.gatein.api.content.Portlet.class.equals(type))
      {
         return Portlet.CONTENT_TYPE;
      }
      else if (org.gatein.api.content.WSRP.class.equals(type))
      {
         return WSRP.CONTENT_TYPE;
      }
      else
      {
         throw new IllegalArgumentException("Unknown Content type: " + type);
      }
   }

   public String getDescription()
   {
      return category.getDescription();
   }

   public void setDescription(String description)
   {
      category.setDescription(description);
      save();
   }

   private void save()
   {
      try
      {
         gateIn.begin();
         gateIn.getRegistryService().save(category);
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
      finally
      {
         gateIn.end();
      }
   }

   public void setDisplayName(String displayName)
   {
      category.setDisplayName(displayName);
      save();
   }

   public void removeContent(String managedContentName)
   {
      Application application = getApplication(managedContentName);

      gateIn.begin();
      try
      {
         gateIn.getRegistryService().remove(application);
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
      finally
      {
         gateIn.end();
      }
   }

   public ManagedContent getManagedContent(String name)
   {
      Application application = getApplication(name);

      if (application == null)
      {
         return null;
      }

      final Id<? extends Content> contentId = GateInImpl.getContentIdFrom(application);
      return new ManagedContentImpl(name, contentId, application, this);
   }

   public IterableCollection<String> getKnownManagedContentNames()
   {
      IterableCollection<ManagedContent> managedContents = getManagedContents();
      return new AdaptedIterableCollection<ManagedContent, String>(managedContents.size(), managedContents.iterator())
      {
         public boolean contains(String s)
         {
            return getApplication(s) != null;
         }

         public String adapt(ManagedContent old)
         {
            return old.getName();
         }
      };
   }

   public Id<Category> getId()
   {
      return gateIn.categoryId(getName());
   }

   public String getName()
   {
      return category.getName();
   }

   public String getDisplayName()
   {
      return category.getDisplayName();
   }

   public GateInImpl getGateIn()
   {
      return gateIn;
   }
}
