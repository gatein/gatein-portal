/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.sample.webui.component;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIConfirmation;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIConfirmation.ActionConfirm;
import org.exoplatform.webui.core.lifecycle.UIContainerLifecycle;

/**
 * Created by The eXo Platform SAS Author : Nguyen Duc Khoi
 * khoi.nguyen@exoplatform.com Apr 28, 2010
 */

@ComponentConfig(lifecycle = UIContainerLifecycle.class)
public class UISampleContent extends UIContainer
{
   private Log log_ = ExoLogger.getLogger(UISampleContent.class);

   public static final int NODE_UIFORM = 0;

   public static final int NODE_UIPOPUPMESSAGE = 1;

   public static final int NODE_UIPOPUPCONFIRM = 2;

   public static final int NODE_LAZYTABPANE = 3;

   public static final int NODE_DOWNLOADUPLOAD = 5;

   public static final int NODE_REPEATER = 6;
   
   public static final int NODE_VIRTUAL_LIST = 7;
   
   public static final int NODE_RIGHTCLICKPOPUP = 8;
   
   public static final int NODE_MULTIVALUE_INPUTSET = 9;

   public static final ApplicationMessage MSG =
      new ApplicationMessage("UISampleContent.UIPopupMessage.msg", new String[]{"World !"}, ApplicationMessage.INFO);

   public static final String MSG_CONFIRM = "Are you sure";  

   public UISampleContent() throws Exception
   {
      // other uicomponents are lazy initialized
      addChild(UISampleUIForm.class, null, null);
   }

   public void showUIComponent(int nodeType)
   {
      switch (nodeType)
      {
         case NODE_UIFORM :
            showUIForm();
            break;
         case NODE_UIPOPUPMESSAGE :
            showUIPopupMessage();
            break;
         case NODE_UIPOPUPCONFIRM :
            showUIConfirmation();
            break;
         case NODE_LAZYTABPANE :
            showUILazyTabPane();
            break;
         case NODE_DOWNLOADUPLOAD :
            showUIDownloadUpload();
            break;
         case NODE_REPEATER:
            showRepeater();
            break;
         case NODE_VIRTUAL_LIST:
            showVirtualList();
            break;
         case NODE_RIGHTCLICKPOPUP:
            showRightClickPopup();
            break;
         case NODE_MULTIVALUE_INPUTSET:
            showMultiValueInputSet();
            break;
         default :
            log_.error("not implement yet");
      }
   }

   private void showMultiValueInputSet()
   {
      UISampleMultiValueInputSet multiInputSet = getChild(UISampleMultiValueInputSet.class);
      setRenderedChild(multiInputSet.getId());
   }

   private void showRightClickPopup()
   {
      UISampleRightClickPopupMenu popup = getChild(UISampleRightClickPopupMenu.class);
      setRenderedChild(popup.getId());
   }

   private void showVirtualList()
   {
      UISampleVirtualList virtualList = getChild(UISampleVirtualList.class);
      setRenderedChild(virtualList.getId());
   }

   private void showRepeater()
   {
      UISampleRepeater repeater = getChild(UISampleRepeater.class);
      setRenderedChild(repeater.getId());
   }

   private void showUIDownloadUpload()
   {
      UISampleDownloadUpload uiDL = getChild(UISampleDownloadUpload.class);
      setRenderedChild(uiDL.getId());
   }

   private void showUILazyTabPane()
   {
      UISampleLazyTabPane uiTabPane = getChild(UISampleLazyTabPane.class);
      setRenderedChild(uiTabPane.getId());
   }

   private void showUIForm()
   {
      setRenderedChild(UISampleUIForm.class);
   }

   private void showUIConfirmation()
   {
      UIConfirmation uiConfirmation = getChild(UIConfirmation.class);
      uiConfirmation.setActions(makeActionConfirmList());
      uiConfirmation.setCaller(this);
      uiConfirmation.addMessage(MSG_CONFIRM);
      uiConfirmation.setShow(true);

      setRenderedChild(UIConfirmation.class);
   }

   private void showUIPopupMessage()
   {
      UISamplePopupMessage uiSamplePopupMessage = getChild(UISamplePopupMessage.class);
      setRenderedChild(uiSamplePopupMessage.getId());
   }

   @Override
   public <T extends UIComponent> T getChild(Class<T> clazz)
   {
      // TODO Auto-generated method stub
      T uiComp = super.getChild(clazz);
      if (uiComp == null)
      {
         try
         {
            uiComp = addChild(clazz, null, null);
         }
         catch (Exception e)
         {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
      }
      return uiComp;
   }

   private List<ActionConfirm> makeActionConfirmList()
   {
      List<ActionConfirm> actionConfirms = new ArrayList<ActionConfirm>();
      actionConfirms.add(new ActionConfirm("Yes", "Yes"));
      actionConfirms.add(new ActionConfirm("No", "No"));
      return actionConfirms;
   }
}
