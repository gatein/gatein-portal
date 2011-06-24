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

package org.exoplatform.portal.config.serialize;

import static org.exoplatform.portal.pom.config.Utils.split;

import org.exoplatform.portal.application.Preference;
import org.exoplatform.portal.config.NewPortalConfigListener;
import org.exoplatform.portal.config.model.Application;
import org.exoplatform.portal.config.model.Properties;
import org.exoplatform.portal.config.model.TransientApplicationState;
import org.exoplatform.portal.pom.spi.gadget.Gadget;
import org.exoplatform.portal.pom.spi.portlet.Portlet;
import org.exoplatform.portal.pom.spi.portlet.PortletBuilder;
import org.jibx.runtime.IAliasable;
import org.jibx.runtime.IMarshaller;
import org.jibx.runtime.IMarshallingContext;
import org.jibx.runtime.IUnmarshaller;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;
import org.jibx.runtime.impl.UnmarshallingContext;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class AbstractApplicationHandler implements IMarshaller, IUnmarshaller, IAliasable
{

   private String m_uri;

   private int m_index;

   private String m_name;

   public AbstractApplicationHandler()
   {
   }

   public AbstractApplicationHandler(String m_uri, int m_index, String m_name)
   {
      this.m_uri = m_uri;
      this.m_index = m_index;
      this.m_name = m_name;
   }

   // IMarshaller implementation

   public boolean isExtension(String s)
   {
      throw new UnsupportedOperationException();
   }

   public void marshal(Object o, IMarshallingContext iMarshallingContext) throws JiBXException
   {
      throw new UnsupportedOperationException();
   }

   // IUnmarshaller implementation

   public boolean isPresent(IUnmarshallingContext ctx) throws JiBXException
   {
      return ctx.isAt(m_uri, m_name);
   }

   public Object unmarshal(Object obj, IUnmarshallingContext ictx) throws JiBXException
   {
      UnmarshallingContext ctx = (UnmarshallingContext)ictx;
      if (!ctx.isAt(m_uri, m_name))
      {
         ctx.throwStartTagNameError(m_uri, m_name);
      }

      //
      if (obj != null)
      {
         throw new AssertionError("That should not happen");
      }

      // Id
      String id = optionalAttribute(ctx, "id");

      //
      ctx.parsePastStartTag(m_uri, m_name);

      //
      Application<?> app;
      TransientApplicationState state;
      if ("application".equals(m_name))
      {
         String instanceId = ctx.parseElementText(m_uri, "instance-id");
         instanceId = NewPortalConfigListener.fixInstanceIdOwnerName(instanceId);
         int i0 = instanceId.indexOf("#");
         int i1 = instanceId.indexOf(":/", i0 + 1);
         String ownerType = instanceId.substring(0, i0);
         String ownerId = instanceId.substring(i0 + 1, i1);
         String persistenceid = instanceId.substring(i1 + 2);
         String[] persistenceChunks = split("/", persistenceid);
         if (persistenceChunks.length == 2)
         {
            state = new TransientApplicationState<Portlet>(
               persistenceChunks[0] + "/" +  persistenceChunks[1],
               null,
               ownerType,
               ownerId,
               null);
         }
         else
         {
            state = new TransientApplicationState<Portlet>(
               persistenceChunks[0] + "/" +  persistenceChunks[1],
               null,
               ownerType,
               ownerId,
               persistenceChunks[2]);
         }
         app = Application.createPortletApplication();
         app.setState(state);
      }
      // Since we don't support dashboard's here, this only works for gadgets using the gadget wrapper portlet.
      else if ("gadget-application".equals(m_name))
      {
         ctx.parsePastStartTag(m_uri, "gadget");
         String gadgetName = ctx.parseElementText(m_uri, "gadget-ref");
         Gadget gadget = null;
         // Once the gadget portlet wrapper is able to use gadget userPref's, include parsing logic here.
         // Gadget gadget = new Gadget();
         // gadget.setUserPref();
         state = new TransientApplicationState<Gadget>(gadgetName, gadget);
         app = Application.createGadgetApplication();
         app.setState(state);
         ctx.parsePastEndTag(m_uri, "gadget");
      }
      else
      {
         String contentId;
         boolean isWSRP = false;
         if(ctx.isAt(m_uri, "wsrp"))
         {
            contentId = ctx.parseElementText(m_uri, "wsrp");
            app = Application.createWSRPApplication();
            isWSRP = true;
         }
         else
         {

            ctx.parsePastStartTag(m_uri, "portlet");
            String applicationName = ctx.parseElementText(m_uri, "application-ref");
            String portletName = ctx.parseElementText(m_uri, "portlet-ref");
            contentId = applicationName + "/" + portletName;
            app = Application.createPortletApplication();
         }

         if (ctx.isAt(m_uri, "preferences"))
         {
            PortletBuilder builder = new PortletBuilder();
            ctx.parsePastStartTag(m_uri, "preferences");
            while (ctx.isAt(m_uri, "preference"))
            {
               Preference value = (Preference)ctx.unmarshalElement();
               builder.add(value.getName(), value.getValues(), value.isReadOnly());
            }
            ctx.parsePastEndTag(m_uri, "preferences");
            state = new TransientApplicationState(contentId, builder.build());
         }
         else
         {
            state = new TransientApplicationState(contentId, null);
         }

         if(!isWSRP)
         {
            ctx.parsePastEndTag(m_uri, "portlet");
         }

         app.setState(state);
      }

      //
      nextOptionalTag(ctx, "application-type");
      String theme = nextOptionalTag(ctx, "theme");
      String title = nextOptionalTag(ctx, "title");
      String accessPermissions = nextOptionalTag(ctx, "access-permissions");
      boolean showInfoBar = nextOptionalBooleanTag(ctx, "show-info-bar", true);
      boolean showApplicationState = nextOptionalBooleanTag(ctx, "show-application-state", true);
      boolean showApplicationMode = nextOptionalBooleanTag(ctx, "show-application-mode", true);
      String description = nextOptionalTag(ctx, "description");
      String icon = nextOptionalTag(ctx, "icon");
      String width = nextOptionalTag(ctx, "width");
      String height = nextOptionalTag(ctx, "height");

      //
      Properties properties = null;
      if (ctx.isAt(m_uri, "properties"))
      {
         properties = (Properties)ctx.unmarshalElement();
      }

      //
      ctx.parsePastEndTag(m_uri, m_name);

      //
      app.setId(id);
      app.setTheme(theme);
      app.setTitle(title);
      app.setAccessPermissions(JibxArraySerialize.deserializeStringArray(accessPermissions));
      app.setShowInfoBar(showInfoBar);
      app.setShowApplicationState(showApplicationState);
      app.setShowApplicationMode(showApplicationMode);
      app.setDescription(description);
      app.setIcon(icon);
      app.setWidth(width);
      app.setHeight(height);
      app.setProperties(properties);

      //
      return app;
   }

   private String optionalAttribute(UnmarshallingContext ctx, String attrName) throws JiBXException
   {
      String value = null;
      if (ctx.hasAttribute(m_uri, attrName))
      {
         value = ctx.attributeText(m_uri, attrName);
      }
      return value;
   }

   private String nextOptionalTag(UnmarshallingContext ctx, String tagName) throws JiBXException
   {
      String value = null;
      if (ctx.isAt(m_uri, tagName))
      {
         value = ctx.parseElementText(m_uri, tagName);
      }
      return value;
   }

   private boolean nextOptionalBooleanTag(UnmarshallingContext ctx, String tagName, boolean defaultValue)
      throws JiBXException
   {
      Boolean value = defaultValue;
      if (ctx.isAt(m_uri, tagName))
      {
         value = ctx.parseElementBoolean(m_uri, tagName);
      }
      return value;
   }
}
