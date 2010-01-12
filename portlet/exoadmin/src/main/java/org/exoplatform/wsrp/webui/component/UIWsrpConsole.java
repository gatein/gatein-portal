/*
 * JBoss, a division of Red Hat
 * Copyright 2010, Red Hat Middleware, LLC, and individual
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
package org.exoplatform.wsrp.webui.component;

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UITabPane;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;

/** @author Wesley Hales */
@ComponentConfigs({
   @ComponentConfig(
      lifecycle = UIApplicationLifecycle.class,
      template = "app:/groovy/wsrp/webui/component/UIWsrpConsole.gtmpl"),
   @ComponentConfig(
      id = "UIWsrpConsoleTab",
      type = UITabPane.class,
      template = "app:/groovy/wsrp/webui/component/UIWsrpConsoleContent.gtmpl",
      events = {@EventConfig(listeners = UITabPane.SelectTabActionListener.class)})})
public class UIWsrpConsole extends UIContainer
{
   public UIWsrpConsole() throws Exception
   {
      UITabPane uiTabPane = addChild(UITabPane.class, "UIWsrpConsoleTab", null);
      uiTabPane.addChild(UIWsrpConsumerOverview.class, null, "Manage Consumers").setRendered(true);
      uiTabPane.addChild(UIWsrpProducerOverview.class, null, "Producer Configuration").setRendered(false);

      if (uiTabPane.getSelectedTabId().equals(""))
      {
         uiTabPane.setSelectedTab(1);
      }
   }
}
