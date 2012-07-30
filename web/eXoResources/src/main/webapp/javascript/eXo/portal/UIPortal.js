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

eXo.portal.UIPortal = {
  portalUIComponentDragDrop : false,

  initMouseHover : function(id) {
	  var comp = $("#" + id);
	  if (!comp.length) return;
	  comp[0].onmouseover = function(event) {_module.UIPortal.blockOnMouseOver(event, this, true);};
	  comp[0].onmouseout = function(event) {_module.UIPortal.blockOnMouseOver(event, this, false);};
  },
  
  blockOnMouseOver : function(event, block, isOver) {
    var jqBlock = $(block);
    if (!eXo.portal.portalMode || eXo.portal.isInDragging)
      return;
    if (eXo.portal.portalMode <= 2 && jqBlock.hasClass("UIContainer"))
      return;
    if (eXo.portal.portalMode > 2 && eXo.portal.portalMode != 4
        && jqBlock.hasClass("UIPortlet"))
      return;

    if (!event)
      event = window.event;
    event.cancelBubble = true;

    var viewBlock, layoutBlock, editBlock;
    jqBlock.find("div.UIComponentBlock").eq(0).children("div").each(function()
    {
      var child = $(this);
      if (child.hasClass("VIEW-BLOCK"))
      {
        viewBlock = child;
      }
      else if (child.hasClass("LAYOUT-BLOCK"))
      {
        layoutBlock = child;
      }
      else if (child.hasClass("EDITION-BLOCK"))
      {
        editBlock = child;
      }
    });

    if (!editBlock)
    {
      return;
    }

    if (isOver)
    {
      var newLayer = editBlock.find("div.NewLayer").eq(0);
      var height = 0;
      var width = 0;

      if (layoutBlock && layoutBlock.css("display") != "none")
      {
        height = layoutBlock[0].offsetHeight;
        width = layoutBlock[0].offsetWidth;
      }
      else if (viewBlock && viewBlock.css("display") != "none")
      {
        height = viewBlock[0].offsetHeight;
        width = viewBlock[0].offsetWidth;
      }

      if (jqBlock.hasClass("UIPortlet"))
      {
        newLayer.css("width", width + "px");
        newLayer.css("height", height + "px");
      }
      else
      {
        newLayer.parent().css("width", width + "px");
        var normalBlock = jqBlock.children("div.NormalContainerBlock");
        if (normalBlock.length > 0)
        {
          normalBlock.eq(0).removeClass("NormalContainerBlock").addClass("OverContainerBlock");
        }
      }

      newLayer.parent().css("top", -height + "px");
      editBlock.css("display", "block");

      var infBar = editBlock.find("div.UIInfoBar").eq(0);
      if (infBar && (base.Browser.isIE6() || (base.Browser.isIE7() && eXo.core.I18n.isRT())))
      {
        // Avoid resizing width of portlet/container block multiple times
        if (infBar.css("width") == "")
        {
          var blockIcon, editIcon, delIcon;
          if (jqBlock.hasClass("UIPortlet"))
          {
            blockIcon = infBar.find("div.PortletIcon");
            editIcon = infBar.find("a.EditPortletPropertiesIcon");
            delIcon = infBar.find("a.DeletePortletIcon");
          }
          else
          {
            blockIcon = infBar.find("div.ContainerIcon");
            editIcon = infBar.find("a.EditContainerIcon");
            delIcon = infBar.find("a.DeleteContainerIcon");
          }

          var infBarWidth = infBar.find("div.DragControlArea")[0].offsetWidth;
          infBarWidth += blockIcon[0].offsetWidth;
          if (editIcon.length > 0)
          {
            infBarWidth += editIcon[0].offsetWidth;
          }
          if (delIcon.length > 0)
          {
            infBarWidth += delIcon[0].offsetWidth;
          }

          infBar.css("width", infBarWidth + 35 + "px");
        }
      }
    }
    else
    {
      editBlock.css("display", "none");
      if (jqBlock.hasClass("UIContainer"))
      {
        var normalBlock = jqBlock.find("div.OverContainerBlock");
        if (normalBlock.length > 0)
        {
          normalBlock.eq(0).removeClass("OverContainerBlock").addClass("NormalContainerBlock");
        }
      }
    }

    // Don't display portlet control when View Container
    var controlPortlet = editBlock.find("div.CONTROL-PORTLET");
    if(controlPortlet.length > 0)
    {
      controlPortlet.eq(0).css("display", eXo.portal.portalMode == 4 ? "none" : "block");
    }
  },

  /** Repaired: by Vu Duy Tu 25/04/07* */
  showLayoutModeForPage : function() {
    var uiPage = $(document.body).find("div.UIPage");
    if (uiPage.length == 0)
      return;
    var viewPage = uiPage.find("div.VIEW-PAGE").eq(0);
    if($("#UIPortalApplication").attr("class") != "Vista")
    {
      viewPage.css("border", "solid 3px #dadada");
    }

    if(viewPage.find("div.UIContainer,div.UIPortlet").length > 0)
    {
      viewPage.css({"border" : "none", "paddingTop" : "5px", "paddingRight" : "5px", "paddingBottom" : "5px", "paddingLeft" : "5px"});
    }
    else
    {
      viewPage.css({"paddingTop" : "50px", "paddingRight" : "0px", "paddingBottom" : "50px", "paddingLeft" : "0px"});
    }
  },

  showViewMode : function() {
    var wkWs = $("#UIWorkingWorkspace");
    if (wkWs.find("div.UIPortlet").length == 0 && wkWs.find("div.UIContainer").length == 0)
    {
      $("#UIPage").parents(".VIEW-PAGE").css({"paddingTop" : "50px", "paddingRight" : "0px", "paddingBottom" : "50px", "paddingLeft" : "0px"});
    }
    var pageBodyBlock = $("#UIPageBody");
    var mask = pageBodyBlock.find("div.UIPageBodyMask");
    if(mask.length > 0)
    {
      mask.css("top", -pageBodyBlock[0].offsetHeight + "px").css("height", pageBodyBlock[0].offsetHeight + "px").css("width", pageBodyBlock[0].offsetWidth + "px");
    }
  },

  /**
   * Change current portal
   */
  changePortal : function(accessPath, portal) {
    window.location = eXo.env.server.context + "/" + accessPath + "/" + portal
        + "/";
  },

  /** Created: by Lxchiati * */
  popupButton : function(url, action) {
    if (action == undefined)
      action = '';
    window.location = url + '&action=' + action;
  },
  /**
   * Remove a component of portal
   * 
   * @param {String}
   *          componentId identifier of component
   */
  removeComponent : function(id) {
    var comp = $("#" + id);
    var parent = comp.parent();
    var viewPage = parent.closest(".VIEW-PAGE");

    // Check if the removing component is a column
    if (parent[0].nodeName.toUpperCase() == "TD")
    {
      parent.remove();
    }
    else
    {
      comp.remove();
    }

    var wkWs = $("#UIWorkingWorkspace");
    if (viewPage.length > 0 && wkWs.find("div.UIContainer").length == 0 && wkWs.find("div.UIPortlet").length == 0)
    {
      viewPage.css({"paddingTop" : "50px", "paddingRight" : "0px", "paddingBottom" : "50px", "paddingLeft" : "0px"});
    }
  }  
};

_module.UIPortal = eXo.portal.UIPortal;