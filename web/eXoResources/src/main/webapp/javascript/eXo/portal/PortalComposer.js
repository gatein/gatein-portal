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

eXo.portal.PortalComposer = {

  contentModified : false,

  toggle : function(icon)
  {
    var jqIcon = xj(icon);
    var compWindow = jqIcon.parent().closest(".UIPortalComposer");
    var contWindow = compWindow.children("div.UIWindowContent").eq(0);
    if(contWindow.css("display") == "block")
    {
      contWindow.css("display", "none");
      jqIcon.attr("class", "CollapseIcon");
    }
    else
    {
      contWindow.css("display", "block");
      jqIcon.attr("class", "ExpandIcon");
    }

    ajaxAsyncGetRequest(eXo.env.server.createPortalURL(compWindow.attr("id"), "Toggle", true));
  },

  showTab : function(id)
  {
    var toolPanel = xj("#UIPortalToolPanel");
    if(id == "UIApplicationList")
    {
      toolPanel.attr("class", "ApplicationMode");
    }
    else if(id == "UIContainerList")
    {
      toolPanel.attr("class", "ContainerMode");
    }
  },

  /**
   * Invoked when content is modified (comparing to persisted one)
   *
   * The method toggles the floppy-disk icon to 'THERE IS SOME NEW STUFF' status.
   */
  toggleSaveButton : function()
  {
    //Avoid execute method body multiple times
    if(!this.contentModified)
    {
      this.contentModified = true;
      var compWindow = xj("#UIWorkingWorkspace").find("div.UIPortalComposer").eq(0);
      compWindow.find("a.SaveButton").attr("class", "EdittedSaveButton");

      ajaxAsyncGetRequest(eXo.env.server.createPortalURL(compWindow.attr("id"), "ChangeEdittedState", true));
    }
  }
};