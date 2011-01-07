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

package org.exoplatform.portal.webui.util;

import com.ibm.icu.text.Transliterator;

import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.workspace.UIMaskWorkspace;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.portal.webui.workspace.UIWorkingWorkspace;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.IdentityRegistry;
import org.exoplatform.services.security.MembershipEntry;
import org.exoplatform.webui.application.portlet.PortletRequestContext;

/**
 * Created by The eXo Platform SAS Author : Hoa Pham hoa.phamvu@exoplatform.com
 * Oct 23, 2008
 */
public class ToolbarUtils
{

   public static final String TURN_ON_QUICK_EDIT = "turnOnQuickEdit";

   /**
    * Checks if is edits the portlet in create page wizard.
    * 
    * @return true, if is edits the portlet in create page wizard
    */
   public static boolean isEditPortletInCreatePageWizard()
   {
      UIPortal uiPortal = Util.getUIPortal();
      UIPortalApplication uiApp = uiPortal.getAncestorOfType(UIPortalApplication.class);
      UIMaskWorkspace uiMaskWS = uiApp.getChildById(UIPortalApplication.UI_MASK_WS_ID);
      // show maskworkpace is being in Portal page edit mode
      if (uiMaskWS.getWindowWidth() > 0 && uiMaskWS.getWindowHeight() < 0)
         return true;
      return false;
   }

   /**
    * Refresh browser.
    * 
    * @param context the context
    */
   public static void updatePortal(PortletRequestContext context)
   {
      UIPortalApplication portalApplication = Util.getUIPortalApplication();
      PortalRequestContext portalRequestContext = (PortalRequestContext)context.getParentAppRequestContext();
      UIWorkingWorkspace uiWorkingWS = portalApplication.getChildById(UIPortalApplication.UI_WORKING_WS_ID);
      portalRequestContext.addUIComponentToUpdateByAjax(uiWorkingWS);
      portalRequestContext.ignoreAJAXUpdateOnPortlets(true);
   }

   /**
    * Can edit current portal.
    * 
    * @param remoteUser the remote user
    * @return true, if successful
    * @throws Exception the exception
    */
   public static boolean canEditCurrentPortal(String remoteUser) throws Exception
   {
      if (remoteUser == null)
         return false;
      IdentityRegistry identityRegistry = Util.getUIPortalApplication().getApplicationComponent(IdentityRegistry.class);
      Identity identity = identityRegistry.getIdentity(remoteUser);
      if (identity == null)
         return false;
      UIPortal uiPortal = Util.getUIPortal();
      //TODO this code only work for single edit permission
      String editPermission = uiPortal.getEditPermission();
      MembershipEntry membershipEntry = MembershipEntry.parse(editPermission);
      return identity.isMemberOf(membershipEntry);
   }

   public static boolean turnOnQuickEditable(PortletRequestContext context, boolean showAblePref) throws Exception
   {
      Object obj =
         Util.getPortalRequestContext().getRequest().getSession().getAttribute(ToolbarUtils.TURN_ON_QUICK_EDIT);
      boolean turnOnFlag = false;
      if (obj != null)
      {
         turnOnFlag = Boolean.parseBoolean(obj.toString());
      }
      if (showAblePref && turnOnFlag)
      {
         return true;
      }
      return false;
   }

   public static boolean isLiveMode()
   {
      Object obj =
         Util.getPortalRequestContext().getRequest().getSession().getAttribute(ToolbarUtils.TURN_ON_QUICK_EDIT);
      if (obj == null)
         return true;
      return !Boolean.parseBoolean(obj.toString());
   }

   public static String cleanString(String str)
   {

      Transliterator accentsconverter = Transliterator.getInstance("Latin; NFD; [:Nonspacing Mark:] Remove; NFC;");

      str = accentsconverter.transliterate(str);

      //the character ? seems to not be changed to d by the transliterate function 

      StringBuffer cleanedStr = new StringBuffer(str.trim());
      // delete special character
      for (int i = 0; i < cleanedStr.length(); i++)
      {
         char c = cleanedStr.charAt(i);
         if (c == ' ')
         {
            if (i > 0 && cleanedStr.charAt(i - 1) == '_')
            {
               cleanedStr.deleteCharAt(i--);
            }
            else
            {
               c = '_';
               cleanedStr.setCharAt(i, c);
            }
            continue;
         }

         if (!(Character.isLetterOrDigit(c) || c == '_'))
         {
            cleanedStr.deleteCharAt(i--);
            continue;
         }

         if (i > 0 && c == '_' && cleanedStr.charAt(i - 1) == '_')
            cleanedStr.deleteCharAt(i--);
      }
      return cleanedStr.toString().toLowerCase();
   }

}
