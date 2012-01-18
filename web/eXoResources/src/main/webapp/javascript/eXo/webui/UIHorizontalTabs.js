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
    var DOMUtil = eXo.core.DOMUtil;
    var uiSelectTab = DOMUtil.findAncestorByClass(clickedEle, "UITab");

    var uiHorizontalTabs = DOMUtil.findAncestorByClass(clickedEle,
        "UIHorizontalTabs");
    var uiTabs = eXo.core.DOMUtil.findDescendantsByClass(uiHorizontalTabs,
        "div", "UITab");
    var parentdHorizontalTab = uiHorizontalTabs.parentNode;
    var contentTabContainer = DOMUtil.findFirstDescendantByClass(
        parentdHorizontalTab, "div", "UITabContentContainer");
    var uiTabContents = DOMUtil.findChildrenByClass(contentTabContainer, "div",
        "UITabContent");
    var form = DOMUtil.getChildrenByTagName(contentTabContainer, "form");
    if (form.length > 0) {
      var tmp = DOMUtil.findChildrenByClass(form[0], "div", "UITabContent");
      for ( var i = 0; i < tmp.length; i++) {
        uiTabContents.push(tmp[i]);
      }
    }
    var index = 0;
    for ( var i = 0; i < uiTabs.length; i++) {
      var styleTabDiv = DOMUtil.getChildrenByTagName(uiTabs[i], "div")[0];
      if (styleTabDiv.className == "DisabledTab")
        continue;
      if (uiSelectTab == uiTabs[i]) {
        styleTabDiv.className = "SelectedTab";
        index = i;
        continue;
      }
      styleTabDiv.className = "NormalTab";
      uiTabContents[i].style.display = "none";
    }
    uiTabContents[index].style.display = "block";
    // if(tabId !=null){
    // //TODO: modify: dang.tung
    // url = url+"&objectId="+tabId ;
    // ajaxAsyncGetRequest(url, false) ;
    // }

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
