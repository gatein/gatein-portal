/**
 * Copyright (C) 2014 eXo Platform SAS.
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

package org.exoplatform.portal.webui.container;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.webui.application.WebuiRequestContext;

public class DefaultUIContainerFactory extends UIComponentFactory<UIContainer> {

  private static Log log = ExoLogger.getLogger(DefaultUIContainerFactory.class);

  @Override
  public UIContainer createUIComponent(String factoryID, WebuiRequestContext context) {
    if (context == null) {
      log.warn("WebuiRequestContext is null. Can't create UIComponent");
      return null;
    }

    Class<? extends UIContainer> containerType = null;
    if (factoryID == null || factoryID.isEmpty()
        || UIContainer.TABLE_COLUMN_CONTAINER.equals(factoryID)) {
      containerType = UIContainer.class;
    } else if (UITabContainer.TAB_CONTAINER.equals(factoryID)) {
      containerType = UITabContainer.class;
    } else if (UIColumnContainer.COLUMN_CONTAINER.equals(factoryID)) {
      containerType = UIColumnContainer.class;
    }

    if (containerType != null) {
      return create(containerType, context);
    } else {
      return null;
    }
  }
}
