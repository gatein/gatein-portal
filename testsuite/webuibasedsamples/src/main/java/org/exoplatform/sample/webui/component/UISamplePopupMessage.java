package org.exoplatform.sample.webui.component;

import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPopupMessages;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

@ComponentConfig(template = "app:/groovy/webui/component/UISamplePopupMessage.gtmpl", events = {@EventConfig(listeners = UISamplePopupMessage.ShowPopupMessageActionListener.class)})
public class UISamplePopupMessage extends UIContainer
{
   static public class ShowPopupMessageActionListener extends EventListener<UISamplePopupMessage>
   {

      @Override
      public void execute(Event<UISamplePopupMessage> event) throws Exception
      {
         int popupType = Integer.parseInt(event.getRequestContext().getRequestParameter(OBJECTID));

         UIPopupMessages uiPopupMessages =
            ((PortletRequestContext)WebuiRequestContext.getCurrentInstance()).getUIApplication().getUIPopupMessages();
         uiPopupMessages.addMessage(new ApplicationMessage("Test Message", null, popupType));
         uiPopupMessages.setShow(true);
      }

   }
}
