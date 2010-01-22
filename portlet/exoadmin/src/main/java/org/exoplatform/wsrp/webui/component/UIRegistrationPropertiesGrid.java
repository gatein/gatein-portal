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

import org.exoplatform.commons.utils.LazyPageList;
import org.exoplatform.commons.utils.ListAccessImpl;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPageIterator;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIFormGrid;
import org.gatein.common.util.ParameterValidation;
import org.gatein.wsrp.consumer.RegistrationProperty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
@ComponentConfig(
   template = "system:/groovy/webui/core/UIGrid.gtmpl",
   events = {
      @EventConfig(listeners = UIRegistrationPropertiesGrid.EditPropertyActionListener.class)
   })
public class UIRegistrationPropertiesGrid extends UIFormGrid
{
   private static final String NAME = "name";
   static String[] FIELDS = {NAME, "description", "status", "value"};
   static String[] PROPERTIES_ACTIONS = {"EditProperty", "DeleteProperty"};
   private Map<String, RegistrationProperty> props;

   public UIRegistrationPropertiesGrid() throws Exception
   {
      super();

      //configure the edit and delete buttons based on an id from the data list - this will also be passed as param to listener
      configure(NAME, FIELDS, PROPERTIES_ACTIONS);
      UIPageIterator pageIterator = getUIPageIterator();
      pageIterator.setRendered(false);
   }

   @Override
   public String getName()
   {
      return getId();
   }

   public void resetProps(Map<String, RegistrationProperty> props)
   {

      ListAccessImpl<RegistrationProperty> listAccess;
      if (ParameterValidation.existsAndIsNotEmpty(props))
      {
         setRendered(true);
      }
      else
      {
         props = Collections.emptyMap();
         setRendered(false);
      }

      this.props = props;

      ArrayList<RegistrationProperty> propsList = new ArrayList<RegistrationProperty>(props.values());
      listAccess = new ListAccessImpl<RegistrationProperty>(RegistrationProperty.class, propsList);
      getUIPageIterator().setPageList(new LazyPageList<RegistrationProperty>(listAccess, 10));
   }

   public RegistrationProperty getProperty(String name)
   {
      return props.get(name);
   }

   static public class EditPropertyActionListener extends EventListener<UIRegistrationPropertiesGrid>
   {
      @Override
      public void execute(Event<UIRegistrationPropertiesGrid> event) throws Exception
      {
         String name = event.getRequestContext().getRequestParameter(OBJECTID);
         UIRegistrationPropertiesGrid registrationPropertiesGrid = event.getSource();
      }
   }
}
