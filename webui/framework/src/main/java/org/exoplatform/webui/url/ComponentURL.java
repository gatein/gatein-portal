/*
 * Copyright (C) 2010 eXo Platform SAS.
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

package org.exoplatform.webui.url;

import org.exoplatform.web.controller.QualifiedName;
import org.exoplatform.web.url.PortalURL;
import org.exoplatform.web.url.ResourceType;
import org.exoplatform.web.url.URLContext;
import org.exoplatform.webui.core.UIComponent;
import org.gatein.common.util.Tools;

import java.util.Collections;
import java.util.Set;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ComponentURL extends PortalURL<UIComponent, ComponentURL>
{

   /** . */
   public static final ResourceType<UIComponent, ComponentURL> TYPE = new ResourceType<UIComponent, ComponentURL>() {};

   /** . */
   public static final QualifiedName COMPONENT = QualifiedName.create("gtn", "componentid");

   /** . */
   public static final QualifiedName ACTION = QualifiedName.create("gtn", "action");

   /** . */
   public static final QualifiedName TARGET = QualifiedName.create("gtn", "objectid");

   /** . */
   public static final QualifiedName PATH = QualifiedName.create("gtn", "path");

   /** . */
   private static final Set<QualifiedName> NAMES = Collections.unmodifiableSet(Tools.toSet(COMPONENT, ACTION, TARGET, PATH));

   /** . */
   private UIComponent resource;

   /** . */
   private String action;

   /** . */
   private String targetBeanId;

   /** . */
   private String path;

   public ComponentURL(URLContext context) throws NullPointerException
   {
      super(context);
   }

   public UIComponent getResource()
   {
      return resource;
   }

   public ComponentURL setResource(UIComponent resource)
   {
      this.resource = resource;
      return this;
   }

   public Set<QualifiedName> getParameterNames()
   {
      return NAMES;
   }

   public String getParameterValue(QualifiedName parameterName)
   {
      if (COMPONENT.equals(parameterName))
      {
         return resource != null ? resource.getId() : null;
      }
      else if (ACTION.equals(parameterName))
      {
         return action;
      }
      else if (TARGET.equals(parameterName))
      {
         return targetBeanId;
      }
      else if (PATH.equals(parameterName))
      {
         return path;
      }
      else
      {
         return null;
      }
   }

   public String getAction()
   {
      return action;
   }

   public void setAction(String action)
   {
      this.action = action;
   }

   public String getTargetBeanId()
   {
      return targetBeanId;
   }

   public void setTargetBeanId(String targetBeanId)
   {
      this.targetBeanId = targetBeanId;
   }

   public String getPath()
   {
      return path;
   }

   public void setPath(String path)
   {
      this.path = path;
   }
}
