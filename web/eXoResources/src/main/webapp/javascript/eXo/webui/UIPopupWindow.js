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
 * A class that manages a popup window
 */
eXo.webui.UIPopupWindow = {
  superClass : eXo.webui.UIPopup,
  
  /**
   * Inits a popup window, with these parameters : . sets the superClass as
   * eXo.webui.UIPopup . sets the popup hidden . inits the drag and drop . inits
   * the resize area if the window is resizable
   */
  init : function(popupId, isShow, isResizable, showCloseButton, isShowMask) {
    var DOMUtil = eXo.core.DOMUtil;
    this.superClass = eXo.webui.UIPopup;
    var popup = document.getElementById(popupId);
    if (popup == null)
      return;
    popup.style.visibility = "hidden";

    // TODO Lambkin: this statement create a bug in select box component in
    // Firefox
    // this.superClass.init(popup) ;
    if (isShow) {
      popup.style.display = "block";
    }
    var contentBlock = DOMUtil.findFirstDescendantByClass(popup, 'div',
        'PopupContent');
    if (contentBlock
        && (eXo.core.Browser.getBrowserHeight() - 100 < contentBlock.offsetHeight)) {
      contentBlock.style.height = (eXo.core.Browser.getBrowserHeight() - 100)
          + "px";
    }
    var popupBar = DOMUtil.findFirstDescendantByClass(popup, 'span',
        'PopupTitle');

    popupBar.onmousedown = this.initDND;

    if (isShow == false) {
      this.superClass.hide(popup);
      if (isShowMask)
        eXo.webui.UIPopupWindow.showMask(popup, false);
    }

    if (isResizable) {
      var resizeBtn = DOMUtil.findFirstDescendantByClass(popup, "span",
          "ResizeButton");
      resizeBtn.style.display = 'block';
      resizeBtn.onmousedown = this.startResizeEvt;
    }

    popup.style.visibility = "hidden";
    if (isShow == true) {
      var iframes = DOMUtil.findDescendantsByTagName(popup, "iframe");
      if (iframes.length > 0) {
        setTimeout("eXo.webui.UIPopupWindow.show('" + popupId + "',"
            + isShowMask + ")", 500);
      } else {
        this.show(popup, isShowMask);
      }
    }
  },

  showMask : function(popup, isShowPopup) {
    var mask = popup.previousSibling;
    // Make sure mask is not TextNode because of previousSibling property
    if (mask && mask.className != "MaskLayer") {
      mask = null;
    }
    if (isShowPopup) {
      // Modal if popup is portal component
      if (eXo.core.DOMUtil.findAncestorByClass(popup, "PORTLET-FRAGMENT") == null) {
        if (!mask)
          eXo.core.UIMaskLayer.createMask(popup.parentNode, popup, 1);
      } else {
        // If popup is portlet's component, modal with just its parent
        if (!mask)
          eXo.core.UIMaskLayer.createMaskForFrame(popup.parentNode, popup, 1);
      }
    } else {
      if (mask)
        eXo.core.UIMaskLayer.removeMask(mask);
    }
  },

  // TODO: manage zIndex properties
  /**
   * Shows the popup window passed in parameter gets the highest z-index
   * property of the elements in the page : . gets the z-index of the maskLayer .
   * gets all the other popup windows . gets the highest z-index from these, if
   * it's still at 0, set an arbitrary value of 2000 sets the position of the
   * popup on the page (top and left properties)
   */
  show : function(popupId, isShowMask, middleBrowser) {
    var DOMUtil = eXo.core.DOMUtil;
    var popup = document.getElementById(popupId);
    if (popup == null) return;        

    // TODO Lambkin: this statement create a bug in select box component in
    // Firefox
    // this.superClass.init(popup) ;    
    var popupBar = DOMUtil.findFirstDescendantByClass(popup, 'span', 'PopupTitle');
    this.initDND(popupBar, popup);
    
    var resizeBtn = DOMUtil.findFirstDescendantByClass(popup, "span", "ResizeButton");
    if (resizeBtn) {
    	resizeBtn.style.display = 'block';
    	resizeBtn.onmousedown = this.startResizeEvt;
    }    	

    if (isShowMask)
    	eXo.webui.UIPopupWindow.showMask(popup, true);
    popup.style.visibility = "hidden";
    this.superClass.show(popup);
    
    var iframes = DOMUtil.findDescendantsByTagName(popup, "iframe");
    if (iframes.length > 0) {
    	setTimeout(function() {eXo.webui.UIPopupWindow.setupWindow(popup, middleBrowser);}, 500);
    } else {
    	this.setupWindow(popup, middleBrowser);
    }
  },
  
  setupWindow : function(popup, middleBrowser) {	    	
	var DOMUtil = eXo.core.DOMUtil;
    var contentBlock = DOMUtil.findFirstDescendantByClass(popup, 'div', 'PopupContent');
    if (contentBlock && (eXo.core.Browser.getBrowserHeight() - 100 < contentBlock.offsetHeight)) {
      contentBlock.style.height = (eXo.core.Browser.getBrowserHeight() - 100) + "px";
    }
    
    var scrollY = 0, offsetParent = popup.offsetParent;
    if (window.pageYOffset != undefined)
      scrollY = window.pageYOffset;
    else if (document.documentElement && document.documentElement.scrollTop)
      scrollY = document.documentElement.scrollTop;
    else
      scrollY = document.body.scrollTop;
    // reference
    if (offsetParent) {
      var middleWindow = (DOMUtil.hasClass(offsetParent, "UIPopupWindow") || DOMUtil.hasClass(offsetParent, "UIWindow"));
      if (middleWindow) {
        popup.style.top = Math.ceil((offsetParent.offsetHeight - popup.offsetHeight) / 2) + "px";
      }
      if (middleBrowser || !middleWindow) {
        popup.style.top = Math.ceil((eXo.core.Browser.getBrowserHeight() - popup.offsetHeight) / 2) + scrollY + "px";
      }
      // Todo: set popup of UIPopup always display in the center browsers in case UIMaskWorkspace
      if (eXo.core.DOMUtil.hasClass(offsetParent, "UIMaskWorkspace")) {
        popup.style.top = Math.ceil((offsetParent.offsetHeight - popup.offsetHeight) / 2) + "px";
      }
      
      // hack for position popup alway top in IE6.
      var checkHeight = popup.offsetHeight > 300;

      if (document.getElementById("UIDockBar") && checkHeight) {
        popup.style.top = "6px";
      }
      if (eXo.core.I18n.lt)
        popup.style.left = Math.ceil((offsetParent.offsetWidth - popup.offsetWidth) / 2) + "px";
      else
        popup.style.right = Math.ceil((offsetParent.offsetWidth - popup.offsetWidth) / 2) + "px";
    }
    if (eXo.core.Browser.findPosY(popup) < 0)
      popup.style.top = scrollY + "px";
        
    popup.style.visibility = "visible";	  
  },
  
  hide : function(popupId, isShowMask) {
	var popup = document.getElementById(popupId);
	if (popup == null) return;     
    this.superClass.hide(popup);
    if (isShowMask) eXo.webui.UIPopupWindow.showMask(popup, false);
  },
  
  showMask : function(popup, isShowPopup) {
    var mask = popup.previousSibling;
    // Make sure mask is not TextNode because of previousSibling property
    if (mask && mask.className != "MaskLayer") {
      mask = null;
    }
    if (isShowPopup) {
      // Modal if popup is portal component
      if (eXo.core.DOMUtil.findAncestorByClass(popup, "PORTLET-FRAGMENT") == null) {
        if (!mask)
          eXo.core.UIMaskLayer.createMask(popup.parentNode, popup, 1);
      } else {
        // If popup is portlet's component, modal with just its parent
        if (!mask)
          eXo.core.UIMaskLayer.createMaskForFrame(popup.parentNode, popup, 1);
      }
    } else {
      if (mask)
        eXo.core.UIMaskLayer.removeMask(mask);
    }
  },  
  
  /**
   * Called when the window starts being resized sets the onmousemove and
   * onmouseup events on the portal application (not the popup) associates these
   * events with UIPopupWindow.resize and UIPopupWindow.endResizeEvt
   * respectively
   */
  startResizeEvt : function(evt) {
	//disable select text
	eXo.webui.UIPopupWindow.backupEvent = null;
	if (navigator.userAgent.indexOf("MSIE") >= 0) {
		//Need to check if we have remove resizedPopup after last mouseUp
		//IE bug: not call endResizeEvt when mouse moved out of page
		if (!eXo.webui.UIPopupWindow.resizedPopup && document.onselectstart) {
			eXo.webui.UIPopupWindow.backupEvent = document.onselectstart;
		}
		document.onselectstart = function() {return false};		
	} else {		
		if (document.onmousedown) {
			eXo.webui.UIPopupWindow.backupEvent = document.onmousedown;
		}
		document.onmousedown = function() {return false};		
	}
	
	var targetPopup = eXo.core.DOMUtil.findAncestorByClass(this, "UIPopupWindow");
	eXo.webui.UIPopupWindow.resizedPopup = targetPopup;
	eXo.webui.UIPopupWindow.backupPointerY = eXo.core.Browser.findMouseRelativeY(targetPopup, evt) ;	
	  
    document.onmousemove = eXo.webui.UIPopupWindow.resize;
    document.onmouseup = eXo.webui.UIPopupWindow.endResizeEvt;
  },

  /**
   * Function called when the window is being resized . gets the position of the
   * mouse . calculates the height and the width of the window from this
   * position . sets these values to the window
   */
  resize : function(evt) {
	var targetPopup = eXo.webui.UIPopupWindow.resizedPopup ;
    var content = eXo.core.DOMUtil.findFirstDescendantByClass(targetPopup,
        "div", "PopupContent");
    var isRTL = eXo.core.I18n.isRT();
    var pointerX = eXo.core.Browser.findMouseRelativeX(targetPopup, evt, isRTL);
    var pointerY = eXo.core.Browser.findMouseRelativeY(targetPopup, evt);
    var delta = pointerY - eXo.webui.UIPopupWindow.backupPointerY;  
    if ((content.offsetHeight + delta) > 0) {
    	eXo.webui.UIPopupWindow.backupPointerY = pointerY;              
    	content.style.height = content.offsetHeight + delta +"px" ;     
    }
    targetPopup.style.height = "auto";

    if (isRTL) {
      pointerX = (-1) * pointerX;
    }

    if (pointerX > 200)
      targetPopup.style.width = (pointerX + 10) + "px";
  },

  /**
   * Called when the window stops being resized cancels the mouse events on the
   * portal app inits the scroll managers active on this page (in case there is
   * one in the popup)
   */
  endResizeEvt : function(evt) {
	eXo.webui.UIPopupWindow.resizedPopup = null;
    this.onmousemove = null;
    this.onmouseup = null;
    
    //enable select text
	if (navigator.userAgent.indexOf("MSIE") >= 0) {
		document.onselectstart = eXo.webui.UIPopupWindow.backupEvent;
	} else {                
		document.onmousedown = eXo.webui.UIPopupWindow.backupEvent;
	}
	eXo.webui.UIPopupWindow.backupEvent = null;
  },

  /**
   * Init the Drag&Drop infrastructure using DragDrop2
   *
   * @param popupBar
   * @param popup
   */
  initDND : function(popupBar, popup)
  {
    eXo.core.DragDrop2.init(popupBar, popup);

    popup.onDragStart = function(x, y, last_x, last_y, e)
    {
      if (eXo.core.Browser.browserType == "mozilla" && popup.uiWindowContent)
      {
        popup.uiWindowContent.style.overflow = "auto";
        var elements = eXo.core.DOMUtil.findDescendantsByClass(popup.uiWindowContent, "ul", "PopupMessageBox");
        for (var i = 0; i < elements.length; i++)
        {
          elements[i].style.overflow = "auto";
        }
      }
    };

    popup.onDrag = function(nx, ny, ex, ey, e)
    {
    };

    popup.onDragEnd = function(x, y, clientX, clientY)
    {
      if (eXo.core.Browser.browserType == "mozilla" && popup.uiWindowContent)
      {
        popup.uiWindowContent.style.overflow = "auto";
        var elements = eXo.core.DOMUtil.findDescendantsByClass(popup.uiWindowContent, "ul", "PopupMessageBox");
        for (var i = 0; i < elements.length; i++)
        {
          elements[i].style.overflow = "auto";
        }
      }
      var offsetParent = popup.offsetParent;
      if (offsetParent)
      {
        if (clientY < 0)
        {
          popup.style.top = (0 - offsetParent.offsetTop) + "px";
        }
      }
      else
      {
        popup.style.top = "0px";
      }
    };

    popup.onCancel = function(e)
    {
    };
  }
};