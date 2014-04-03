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

(function() {	
var uiPortalControl = {

  /**
   * Collapse tree, use for Navigation Tree
   * 
   * @param {Object}
   *          selectedElement first object of tree
   */
  collapseTree : function(selectedElement) {
    var ancest = $(selectedElement).parent().closest(".Node");
    var childrenCont = ancest.find("div.ChildrenContainer").eq(0);
    var newHTML = "<div onclick=\""
        + childrenCont.closest("actionLink").val()
        + "\" class=\"ExpandIcon\">" + selectedElement.innerHTML + "</div>";
    ancest.html(newHTML);
  },

  /**
   * Process enter key press
   * 
   * @param {Event}
   *          e this event
   * @param {String}
   *          executeScript javascript command to execute if enter key was
   *          pressed
   */
  onEnterPress : function(e, executeScript) {
    var e = window.event || e;
    var code;
    if (!e)
      e = window.event;
    if (e.keyCode)
      code = e.keyCode;
    else if (e.which)
      code = e.which;
    if (code == 13) {
      if (window.event) {
        e.returnValue = false;
      } else {
        e.preventDefault();
      }
      //TODO: Move the code serving for login form to executeScript param in caller side
      var uiPortalLoginFormAction = document
          .getElementById("UIPortalLoginFormAction");
      if (uiPortalLoginFormAction) {
        uiPortalLoginFormAction.onclick();
      } else {
        if (executeScript)
          eval(executeScript);
      }
    }
  },

  onKeyPress : function(e, executeScript, expectedCode)
  {
    if(!e)
    {
      e = window.event;
    }

    if(e.keyCode && e.keyCode == expectedCode)
    {
      if(window.event)
      {
        e.cancelBubble = true;
      }
      else
      {
        e.preventDefault();
      }

      if(executeScript)
      {
        eval(executeScript);
      }
    }
  }
};

eXo.portal.VerticalScrollManager = {
  repeat : null,

  initScroll : function(clickedEle, isUp, step) {
    var verticalScroll = eXo.portal.VerticalScrollManager;
    var container = $(clickedEle).closest(".ItemContainer");
    var middleCont = container.find(".MiddleItemContainer").first()[0];
    if (!middleCont.id)
      middleCont.id = "IC" + new Date().getTime()
          + Math.random().toString().substring(2);
    verticalScroll.scrollComponent(middleCont.id, isUp, step);
    document.onmouseup = verticalScroll.cancelScroll;
  },

  scrollComponent : function(id, isUp, step) {
    var verticalScroll = eXo.portal.VerticalScrollManager;
    var scrollComp = document.getElementById(id);
    if (isUp) {
      scrollComp.scrollTop -= step;
    } else {
      scrollComp.scrollTop += step;
    }
    if (verticalScroll.repeat) {
      verticalScroll.cancelScroll();
    }
    verticalScroll.repeat = setTimeout(function() {
    	eXo.portal.VerticalScrollManager.scrollComponent(id, isUp, step);
    }, 100);
  },

  cancelScroll : function() {
    clearTimeout(eXo.portal.VerticalScrollManager.repeat);
    eXo.portal.VerticalScrollManager.repeat = null;
  }
};

eXo.webui.UIHorizontalTabs = {

  init : function(id) {
    if(id)
    {
      $("#" + id).find("div.TabsContainer").find("div.UITab").find("div.MiddleTab").not(".LockedTab").on("click", function()
      {
        eXo.webui.UIHorizontalTabs.displayTabContent(this);
      });
    }
  },

  /**
   * Calls changeTabForUITabPane to display tab content
   */
  displayTabContent : function(clickedEle) {
    this.changeTabForUITabPane(clickedEle, null, null);
  },
  /**
   * Gets the tab element and the tab content associated and displays them .
   * changes the style of the tab . displays the tab content of the selected tab
   * (display: block) if tabId are provided, can get the tab content by Ajax
   */
  changeTabForUITabPane : function(clickedEle, tabId, url) {
    var uiSelectTab = $(clickedEle).parents(".UITab").eq(0);
    var uiHorizontalTabs = $(clickedEle).parents(".UIHorizontalTabs");
    var uiTabs = uiHorizontalTabs.find("div.UITab");
    var parentdHorizontabTab = uiHorizontalTabs.parent();
    var contentTabContainer = parentdHorizontabTab.find("div.UITabContentContainer");
    var uiTabContents = contentTabContainer.children("div.UITabContent");

    //TODO: Remove this! A generic method should not contain code handling specially tabs in form
    var form = contentTabContainer.children("form").eq(0);
    if(form)
    {
      //Note that the method add() in jQuery creates a completely new set and does not modify original object
      uiTabContents = uiTabContents.add("div.UITabContent", form);
    }

    uiTabs.each(function(index)
    {
      var styleTabDiv = $(this).children("div").eq(0);
      if(styleTabDiv.attr("class") == "DisabledTab")
      {
        return;
      }
      if ($(this)[0] == uiSelectTab[0])
      {
        styleTabDiv.removeAttr("class").attr("class", "SelectedTab");
        uiTabContents.eq(index).css("display", "block");
      }
      else
      {
        styleTabDiv.removeAttr("class").attr("class", "NormalTab");
        uiTabContents.eq(index).css("display", "none");
      }
    });
  },

  checkContentAvailable : function(id) {
    var tabContent = document.getElementById(id).parentNode;
    // var textTrimmed = tabContent.innerHTML.replace(/\n/g, '')
    if (!tabContent.isLoaded) {
      tabContent.isLoaded = true;
      return false;
    }

    tabContent.style.display = 'block';
    return true;
  },

  /**
   * 
   */
  changeTabForUIFormTabpane : function(clickedElemt, formId, hiddenValue) {
    this.displayTabContent(clickedElemt);
    uiForm.setHiddenValue(formId, 'currentSelectedTab', hiddenValue);
  }
};

return {
	UIPortalControl : uiPortalControl,
	UIHorizontalTabs : eXo.webui.UIHorizontalTabs,
	VerticalScrollManager : eXo.portal.VerticalScrollManager
};
})($, uiForm);