/*
 * JBoss, a division of Red Hat
 * Copyright 2011, Red Hat Middleware, LLC, and individual
 * contributors as indicated by the @authors tag. See the
 * copyright.txt in the distribution for a full listing of
 * individual contributors.
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
import org.gatein.api.content.Content;
import org.gatein.api.content.ManagedContent;
import org.gatein.api.id.Id;
import org.gatein.portal.api.impl.GateInImpl;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class ManagedContentImpl<T extends Content> implements ManagedContent<T>
{
   private T content;
   private final Id<T> contentId;
   private final Id<ManagedContent<T>> id;
   private final CategoryImpl category;
   private final Application application;

   ManagedContentImpl(String name, Id contentId, Application application, CategoryImpl category)
   {
      this.contentId = contentId;
      this.id = category.getGateIn().managedContentId(category.getId(), name, contentId);
      this.category = category;
      this.application = application;
   }

   @Override
   public String toString()
   {
      return "ManagedContent '" + getName() + "' =>" + getContent();
   }

   public Id<ManagedContent<T>> getId()
   {
      return id;
   }

   public String getName()
   {
      return application.getApplicationName();
   }

   public String getDisplayName()
   {
      return application.getDisplayName();
   }

   public void setDisplayName(String displayName)
   {
      application.setDisplayName(displayName);
      save();
   }

   public String getDescription()
   {
      return application.getDescription();
   }

   public void setDescription(String description)
   {
      application.setDescription(description);
      save();
   }

   public GateInImpl getGateIn()
   {
      return category.getGateIn();
   }

   public T getContent()
   {
      if (content == null)
      {
         content = category.getRegistry().get(contentId);
      }

      return content;
   }

   private void save()
   {
      try
      {
         getGateIn().begin();
         getGateIn().getRegistryService().update(application);
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
      finally
      {
         getGateIn().end();
      }
   }
}
