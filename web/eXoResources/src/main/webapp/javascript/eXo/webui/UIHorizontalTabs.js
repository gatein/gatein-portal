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

/**
 * A class to manage horizontal tabs TODO : could be a good thing to implement a
 * scroll manager directly in this class
 */
eXo.webui.UIHorizontalTabs = {

  init : function() {
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
    var uiSelectTab = xj(clickedEle).parents(".UITab").eq(0);
    var uiHorizontalTabs = xj(clickedEle).parents(".UIHorizontalTabs");
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
      var styleTabDiv = xj(this).children("div").eq(0);
      if(styleTabDiv.attr("class") == "DisabledTab")
      {
        return;
      }
      if (xj(this)[0] == uiSelectTab[0])
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
    eXo.webui.UIForm.setHiddenValue(formId, 'currentSelectedTab', hiddenValue);
  }
}
