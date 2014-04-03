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

(function($, base, msg) {


var uiFormInputThemeSelector = {

  initForm : function() {
    $(".UIFormInputThemeSelector").find(".SetDefault").on("click", function() {
      uiFormInputThemeSelector.setDefaultTheme(this,'DefaultTheme');
    });
  },

  initSelector : function() {
    $(".UIFormInputThemeSelector").find(".UIThemeSelector").parent().on("click", function() {
      var theme = $(this).children("div").attr("class").replace("UIThemeSelector ", "");
      uiFormInputThemeSelector.showThemeSelected(this, theme);
    });
  },

  showThemeSelected : function(obj, param) {
    var jqObj = $(obj);
    var itemListContainer = jqObj.parent().closest(".ItemListContainer");
    var detailList = itemListContainer.next("div").find("div.UIThemeSelector").eq(0);
    detailList.next("div").html(jqObj.find("div.NameStyle").eq(0).html());
    detailList.attr("class", "UIThemeSelector " + param);
    //jqObj.parent().prev("input")[0].value = param;//This does not work as 'prev' in jQuery does not return hidden input
    jqObj.parent().parent().children("input").eq(0).val(param);
  },

  setDefaultTheme : function(obj, param) {
    var itemDetailList = $(obj).parent().closest(".ItemDetailList");
    var detailList = itemDetailList.find("div.UIThemeSelector").eq(0);
    detailList.attr("class", "UIThemeSelector " + param);

    detailList.next("div").html(msg.getMessage("DefaultTheme"));
    itemDetailList.prev("div").find("div.ItemList").eq(0).parent().children("input").eq(0).val(param);
  }
};

eXo.webui.UIPageTemplateOptions = {

  /**
   * @author dang.tung
   *
   * TODO To change the template layout in page config Called by
   * UIPageTemplateOptions.java Review UIDropDownControl.java: set javascrip
   * action UIDropDownControl.js : set this method to do
   */
  selectPageLayout : function(id, selectedIndex) {
    var dropDownControl = $("#" + id);
    var itemSelectorAncest = dropDownControl.closest(".ItemSelectorAncestor");
    var itemList = itemSelectorAncest.find("div.ItemList");
    var itemSelectorLabel = itemSelectorAncest.find("a.OptionItem");
    var itemSelector = dropDownControl.closest("div.UIItemSelector");
    var itemDetailList = itemSelector.find("div.ItemDetailList");
    if (itemList.length == 0)
      return;
    for (i = 0; i < itemSelectorLabel.length; ++i) {
      if (i >= itemList.length)
        continue;
      if (i == selectedIndex) {
        itemList[i].style.display = "block";
        if (itemDetailList.length < 1)
          continue;
        itemDetailList[i].style.display = "block";
        var selectedItem = $(itemList[i]).find("div.SelectedItem").eq(0);
        if (!selectedItem || selectedItem == null)
          continue;
        var setValue = selectedItem.closest(".SetValue");
        if (setValue == null)
          continue;
        eval(setValue.innerHTML);
      } else {
        itemList[i].style.display = "none";
        if (itemDetailList.length > 0)
          itemDetailList[i].style.display = "none";
      }
    }
  }
};

eXo.portal.UIPortal = {
  portalUIComponentDragDrop : false,

  initMouseHover : function(id) {
    var comp = $("#" + id);
    if (!comp.length) return;
    comp[0].onmouseover = function(event) {eXo.portal.UIPortal.blockOnMouseOver(event, this, true);};
    comp[0].onmouseout = function(event) {eXo.portal.UIPortal.blockOnMouseOver(event, this, false);};
    if (eXo.portal.UIPortal.isFullPreviewInPageEditor()) {
      /* When in full page preview edit mode,
       * disable all components in site header and footer areas. */
      if (comp.closest(".UIPageBody").length == 0) {
        /* Header and Footer is everything not under UIPageBody */
        eXo.portal.UIPortal.maskWithNewLayer(comp).addClass("Disabled")
            .closest(".EDITION-BLOCK").find(".CONTROL-BLOCK:first").remove();
      }
    }
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

    var editBlock = jqBlock.find("div.UIComponentBlock").eq(0).children(".EDITION-BLOCK");
    if (editBlock.length == 0 || editBlock.find(".CONTROL-BLOCK").length == 0)
    {
      return;
    }

    if (isOver)
    {
      var parent = this.findParentContainer(block);
      if (!this.canMoveComponent(block, parent)) {
        /* Nothing to do if the removal is not allowed. */
        return;
      }
      eXo.portal.UIPortal.maskWithNewLayer(jqBlock);

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
  showComponentEditInBlockMode : function() {
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

  showComponentEditInViewMode : function() {
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
  },
  findParentContainer : function(componentElement) {
    var jqStart = $(componentElement).parent();
    var result = jqStart.closest(".UIContainer");
    if (!result.length) {
      result = jqStart.closest(".UIPage");
    }
    if (!result.length) {
      result = jqStart.closest(".UIPortal");
    }
    return result.length ? result[0] : null;
  },
  canMoveComponent : function(componentElement, parentContainerElement) {
    if (parentContainerElement) {
      var jqComponentElement = $(componentElement);
      var jqParentContainerElement = $(parentContainerElement);
      /* the second disjunct is for the case when componentElement is newly added from the palette */
      var jqElementIsContainer = jqComponentElement.hasClass("UIContainer")
              || !!jqComponentElement.closest("#UIContainerList").length;
      return jqComponentElement.hasClass("UIPageBody") // Everybody can move UIPageBody
          || (jqElementIsContainer && !jqParentContainerElement.hasClass("CannotMoveContainers"))
          || (!jqElementIsContainer && !jqParentContainerElement.hasClass("CannotMoveApps"));
    }
    else {
      return false;
    }
  },
  /**
   * See org.exoplatform.portal.webui.page.UIPage.isFullPreviewInPageEditor() in Java.
   * */
  isFullPreviewInPageEditor : function() {
    /* APP_VIEW_EDIT_MODE || CONTAINER_VIEW_EDIT_MODE */
    return (eXo.portal.portalMode == 2 || eXo.portal.portalMode == 4)
        && eXo.portal.portalEditLevel == "EDIT_PAGE"
        && eXo.portal.fullPreview
  },

  maskWithNewLayer : function(componentBlock) {
    var uiComponentBlock =  componentBlock.find("div.UIComponentBlock").eq(0);
    var viewBlock = uiComponentBlock.children(".VIEW-BLOCK");
    var layoutBlock = uiComponentBlock.children(".LAYOUT-BLOCK");
    var editBlock = uiComponentBlock.children(".EDITION-BLOCK");
    var newLayer = editBlock.find("div.NewLayer").eq(0);

    editBlock.css("display", "block");
    var height = 0;
    var width = 0;
    if (layoutBlock.length && layoutBlock.css("display") != "none")
    {
      height = editBlock.offset().top - layoutBlock.offset().top;
      width = layoutBlock[0].offsetWidth;
    }
    else if (viewBlock.length && viewBlock.css("display") != "none")
    {
      height = editBlock.offset().top - viewBlock.offset().top;
      width = viewBlock[0].offsetWidth;
    }

    if (componentBlock.hasClass("UIPortlet"))
    {
      newLayer.css("width", width + "px");
      newLayer.css("height", height + "px");
    }
    else
    {
      newLayer.parent().css("width", width + "px");
      var normalBlock = componentBlock.children("div.NormalContainerBlock");
      if (normalBlock.length > 0)
      {
        normalBlock.eq(0).removeClass("NormalContainerBlock").addClass("OverContainerBlock");
      }
    }
    newLayer.parent().css("top", -height + "px");
    return newLayer;
  }
};

return {UIPortal : eXo.portal.UIPortal,
        UIPageTemplateOptions : eXo.webui.UIPageTemplateOptions,
        UIFormInputThemeSelector : uiFormInputThemeSelector};
})($, base, msg);
