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

package org.exoplatform.portal.application.state;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.portal.webui.application.UIPortlet;

import javax.xml.namespace.QName;
import java.util.Map;

/**
 * This plugins setup properties that are NOT publicly supported, however it is recommanded to not change anything
 * but there are no guarantees that it won't change.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class InternalPropertiesPlugin extends AbstractContextualPropertyProviderPlugin
{

   /** . */
   private final QName storageIdQName;

   /** . */
   private final QName storageNameQName;

   public InternalPropertiesPlugin(InitParams params)
   {
      super(params);

      //
      this.storageIdQName = new QName(namespaceURI, "storage_id");
      this.storageNameQName = new QName(namespaceURI, "storage_name");
   }

   @Override
   public void getProperties(UIPortlet portletWindow, Map<QName, String[]> properties)
   {
      addProperty(properties, storageIdQName, portletWindow.getStorageId());
      addProperty(properties, storageNameQName, portletWindow.getStorageName());
   }
}
