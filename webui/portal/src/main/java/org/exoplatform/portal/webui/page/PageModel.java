/*
 * Copyright (C) 2012 eXo Platform SAS.
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
package org.exoplatform.portal.webui.page;

import org.exoplatform.portal.mop.page.PageContext;

import java.util.List;

/**
 * @author <a href="mailto:haithanh0809@gmail.com">Hai Thanh Nguyen</a>
 * @version $Id$
 *
 */
public class PageModel
{
   private final String pageId;
   
   private final String title;
   
   private final String[] accessPermissions;
   
   private final String editPermission;
   
   PageModel(PageContext context)
   {
      pageId = context.getKey().format();
      title = context.getState().getName();
      List<String> list = context.getState().getAccessPermissions();
      accessPermissions = list.toArray(new String[list.size()]);
      editPermission = context.getState().getEditPermission();
   }

   public String getPageId()
   {
      return pageId;
   }

   public String getTitle()
   {
      return title;
   }

   public String[] getAccessPermissions()
   {
      return accessPermissions;
   }

   public String getEditPermission()
   {
      return editPermission;
   }
}
