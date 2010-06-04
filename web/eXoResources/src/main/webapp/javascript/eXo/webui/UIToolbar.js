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

function UIToolbar() {};
/**
 * Display clicked element on block state
 * @param {Object} clickedEle clicked object
 */
UIToolbar.prototype.displayBlockContent = function(clickedEle) {
	if(clickedEle == null) return;
  var uiToolbar = eXo.core.DOMUtil.findAncestorByClass(clickedEle, "UIToolbar");
  var parentUIToolbar = uiToolbar.parentNode;
  
  var buttons = eXo.core.DOMUtil.findDescendantsByClass(uiToolbar, "div", "Button");
  var buttonLabel = eXo.core.DOMUtil.findDescendantsByClass(uiToolbar, "div", "ButtonLabel");

  var uiToolbarContentContainer = eXo.core.DOMUtil.findFirstDescendantByClass(parentUIToolbar, "div", "UIToolbarContentContainer");
  var uiToolbarContentBlock = eXo.core.DOMUtil.getChildrenByTagName(uiToolbarContentContainer, "div");

  for(var i = 0; i < buttons.length; i++) {
    if(clickedEle == buttons[i]) {
      uiToolbarContentBlock[i].style.display = "block";
      buttonLabel[i].style.fontWeight = "bold";
    } else {
      uiToolbarContentBlock[i].style.display = "none";
      buttonLabel[i].style.fontWeight = "100";		
    }                                                                  
  }
  
};

eXo.webui.UIToolbar = new UIToolbar();
