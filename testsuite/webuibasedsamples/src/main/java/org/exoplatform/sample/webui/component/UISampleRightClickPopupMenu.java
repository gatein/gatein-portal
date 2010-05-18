package org.exoplatform.sample.webui.component;

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIRightClickPopupMenu;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

@ComponentConfigs({
   @ComponentConfig(template = "app:/groovy/webui/component/UISampleRightClickPopupMenu.gtmpl"),
   @ComponentConfig(id = "UISamplePopupMenu", type = UIRightClickPopupMenu.class, template = "system:/groovy/webui/core/UIRightClickPopupMenu.gtmpl", events = {
      @EventConfig(listeners = UISampleRightClickPopupMenu.SayHelloActionListener.class),
      @EventConfig(listeners = UISampleRightClickPopupMenu.SayGoodByeActionListener.class)})})
public class UISampleRightClickPopupMenu extends UIContainer
{

   public UISampleRightClickPopupMenu() throws Exception
   {
      UIRightClickPopupMenu popup = addChild(UIRightClickPopupMenu.class, "UISamplePopupMenu", null).setRendered(true);
      popup.setActions(new String[]{"SayHello", "SayGoodBye"});
   }

   static public class SayHelloActionListener extends EventListener<UISampleRightClickPopupMenu>
   {

      @Override
      public void execute(Event<UISampleRightClickPopupMenu> event) throws Exception
      {
         System.out.println("Hello");
      }
   }

   static public class SayGoodByeActionListener extends EventListener<UISampleRightClickPopupMenu>
   {

      @Override
      public void execute(Event<UISampleRightClickPopupMenu> event) throws Exception
      {
         System.out.println("GoodBye");
      }
   }
}
