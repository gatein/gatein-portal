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

import org.exoplatform.portal.pom.config.Utils;
import org.exoplatform.portal.pom.data.ComponentData;
import org.exoplatform.portal.pom.data.ContainerData;
import org.exoplatform.portal.pom.data.ModelData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * May 13, 2004
 * @author: Tuan Nguyen
 * @email:   tuan08@users.sourceforge.net
 * @version: $Id: Container.java,v 1.8 2004/11/03 01:23:55 tuan08 Exp $
 **/
public class Container extends ModelObject
{

   protected String id;

   protected String name;

   protected String icon;

   protected String decorator;

   protected String template;

   protected String factoryId;

   protected String title;

   protected String description;

   protected String width;

   protected String height;

   protected String[] accessPermissions;

   protected ArrayList<ModelObject> children;

   public Container()
   {
      children = new ArrayList<ModelObject>();
   }

   public Container(String storageId)
   {
      super(storageId);

      //
      this.children = new ArrayList<ModelObject>();
   }

   public Container(ContainerData data)
   {
      super(data.getStorageId());

      //
      ArrayList<ModelObject> children = new ArrayList<ModelObject>();
      for (ComponentData child : data.getChildren())
      {
         children.add(ModelObject.build(child));
      }

      //
      this.id = data.getId();
      this.name = data.getName();
      this.icon = data.getIcon();
      this.decorator = data.getDecorator();
      this.template = data.getTemplate();
      this.factoryId = data.getFactoryId();
      this.title = data.getTitle();
      this.description = data.getDescription();
      this.width = data.getWidth();
      this.height = data.getHeight();
      this.accessPermissions = data.getAccessPermissions().toArray(new String[data.getAccessPermissions().size()]);
      this.children = children;
   }

   public String getId()
   {
      return id;
   }

   public void setId(String s)
   {
      id = s;
   }

   public String getName()
   {
      return name;
   }

   public void setName(String s)
   {
      name = s;
   }

   public String getIcon()
   {
      return icon;
   }

   public void setIcon(String icon)
   {
      this.icon = icon;
   }

   public ArrayList<ModelObject> getChildren()
   {
      return children;
   }

   public void setChildren(ArrayList<ModelObject> children)
   {
      this.children = children;
   }

   public String getHeight()
   {
      return height;
   }

   public void setHeight(String height)
   {
      this.height = height;
   }

   public String getWidth()
   {
      return width;
   }

   public void setWidth(String width)
   {
      this.width = width;
   }

   public String getDecorator()
   {
      return decorator;
   }

   public void setDecorator(String s)
   {
      decorator = s;
   }

   public String getDescription()
   {
      return description;
   }

   public void setDescription(String des)
   {
      description = des;
   }

   public String getTitle()
   {
      return title;
   }

   public void setTitle(String title)
   {
      this.title = title;
   }

   public String getFactoryId()
   {
      return factoryId;
   }

   public void setFactoryId(String factoryId)
   {
      this.factoryId = factoryId;
   }

   public String getTemplate()
   {
      return template;
   }

   public void setTemplate(String template)
   {
      this.template = template;
   }

   public String[] getAccessPermissions()
   {
      return accessPermissions;
   }

   public void setAccessPermissions(String[] accessPermissions)
   {
      this.accessPermissions = accessPermissions;
   }

   @Override
   public ContainerData build()
   {
      List<ComponentData> children = buildChildren();
      return new ContainerData(
         storageId,
         id,
         name,
         icon,
         decorator,
         template,
         factoryId,
         title,
         description,
         width,
         height,
         Utils.safeImmutableList(accessPermissions),
         children
      );
   }

   protected List<ComponentData> buildChildren()
   {
      if (children != null && children.size() > 0)
      {
         ArrayList<ComponentData> dataChildren = new ArrayList<ComponentData>(children.size());
         for (int i = 0;i < children.size();i++)
         {
            ModelObject node = children.get(i);
            ModelData data = node.build();
            dataChildren.add((ComponentData)data);
         }
         return Collections.unmodifiableList(dataChildren);
      }
      else
      {
         return Collections.emptyList();
      }
   }
}