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

package org.exoplatform.webui.core;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.web.application.AbstractApplicationMessage;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.commons.serialization.api.annotations.Serialized;
import org.exoplatform.webui.exception.MessageException;

import java.io.Writer;

/**
 * Created by The eXo Platform SAS
 * May 8, 2006
 */
@Serialized
abstract public class UIApplication extends UIContainer
{

   protected static Log log = ExoLogger.getLogger("portal:UIApplication");

   private String owner;

   private long lastAccessApplication_;

   private UIPopupMessages uiPopupMessages_;
   private static final String UIAPPLICATION = "uiapplication";

   public UIApplication() throws Exception
   {      
   }

   //TODO this looks like not to be used anymore
   public String getOwner()
   {
      return owner;
   }

   //TODO this looks like not to be used anymore
   public void setOwner(String s)
   {
      owner = s;
   }

   /**
    * Return the common UIPopupMessages
    * @return UIPopupMessages
    */
   public UIPopupMessages getUIPopupMessages()
   {
      if (uiPopupMessages_ == null)
      {
         try
         {
            uiPopupMessages_ = createUIComponent(UIPopupMessages.class, null, null);
            uiPopupMessages_.setId("_" + uiPopupMessages_.hashCode());
         }
         catch (Exception e)
         {
            log.error(e.getMessage(), e);
         }
      }
      return uiPopupMessages_;
   }

   public void addMessage(AbstractApplicationMessage message)
   {
      getUIPopupMessages().addMessage(message);
   }

   public void clearMessages()
   {
      getUIPopupMessages().clearMessages();
   }

   public long getLastAccessApplication()
   {
      return lastAccessApplication_;
   }

   public void setLastAccessApplication(long time)
   {
      lastAccessApplication_ = time;
   }

   public String getUIComponentName()
   {
      return UIAPPLICATION;
   }

   @SuppressWarnings("unchecked")
   public <T extends UIComponent> T findComponentById(String lookupId)
   {
      if (getUIPopupMessages().getId().equals(lookupId))
         return (T)getUIPopupMessages();
      return (T)super.findComponentById(lookupId);
   }

   public void renderChildren() throws Exception
   {
      super.renderChildren();
      if (getUIPopupMessages() == null)
         return;
      WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
      getUIPopupMessages().processRender(context);
   }

   public void processAction(WebuiRequestContext context) throws Exception
   {
      try
      {
         super.processAction(context);
      }
      catch (MessageException ex)
      {
         addMessage(ex.getDetailMessage());
      }
      catch (Throwable t)
      {
         ApplicationMessage msg =
            new ApplicationMessage("UIApplication.msg.unknown-error", null, ApplicationMessage.ERROR);
         addMessage(msg);
         log.error("Error during the processAction phase", t);
      }
   }

   public void renderBlockToUpdate(UIComponent uicomponent, WebuiRequestContext context, Writer w) throws Exception
   {
      w.write("<div class=\"BlockToUpdate\">");
      w.append("<div class=\"BlockToUpdateId\">").append(uicomponent.getId()).append("</div>");
      w.write("<div class=\"BlockToUpdateData\">");
      uicomponent.processRender(context);
      w.write("</div>");
      w.write("</div>");
   }
}