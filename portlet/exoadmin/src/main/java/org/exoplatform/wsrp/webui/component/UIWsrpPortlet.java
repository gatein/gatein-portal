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
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.UITabPane;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.exoplatform.webui.core.renderers.ValueRendererRegistry;
import org.exoplatform.wsrp.webui.component.consumer.UIWsrpConsumerOverview;
import org.exoplatform.wsrp.webui.component.producer.UIWsrpProducerEditor;
import org.exoplatform.wsrp.webui.component.renderers.LocalizedStringValueRenderer;
import org.exoplatform.wsrp.webui.component.renderers.RegistrationDescriptionValueRenderer;
import org.exoplatform.wsrp.webui.component.renderers.RegistrationPropertyStatusValueRenderer;
import org.gatein.wsrp.consumer.RegistrationProperty;
import org.gatein.wsrp.registration.LocalizedString;
import org.gatein.wsrp.registration.RegistrationPropertyDescription;

/** @author Wesley Hales */
@ComponentConfig(
   lifecycle = UIApplicationLifecycle.class
)
public class UIWsrpPortlet extends UIPortletApplication
{
   static
   {
      // register value renderers
      ValueRendererRegistry.registerDefaultRendererFor(new RegistrationDescriptionValueRenderer(), RegistrationPropertyDescription.class);
      ValueRendererRegistry.registerDefaultRendererFor(new LocalizedStringValueRenderer(), LocalizedString.class);
      ValueRendererRegistry.registerDefaultRendererFor(new RegistrationPropertyStatusValueRenderer(), RegistrationProperty.Status.class);
   }

   public UIWsrpPortlet() throws Exception
   {
      UITabPane uiTabPane = addChild(UITabPane.class, null, null);
      uiTabPane.addChild(UIWsrpConsumerOverview.class, null, "Consumers");
      uiTabPane.addChild(UIWsrpProducerEditor.class, null, "Producer");

      if (uiTabPane.getSelectedTabId().equals(""))
      {
         uiTabPane.setSelectedTab(1);
      }
   }


}
