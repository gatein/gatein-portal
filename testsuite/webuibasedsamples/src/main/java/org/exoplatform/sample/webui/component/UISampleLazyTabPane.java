package org.exoplatform.sample.webui.component;

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UILazyTabPane;
import org.exoplatform.webui.core.lifecycle.UIContainerLifecycle;

@ComponentConfig(lifecycle=UIContainerLifecycle.class)
public class UISampleLazyTabPane extends UIContainer
{

   public UISampleLazyTabPane() throws Exception
   {
      UILazyTabPane uiLazyTabPane = addChild(UILazyTabPane.class, null, null);
      uiLazyTabPane.addChild(UISampleRightClickPopupMenu.class, null, null);
      uiLazyTabPane.addChild(UISampleRepeater.class, null, null);
      uiLazyTabPane.setSelectedTab(1);
   }
}
