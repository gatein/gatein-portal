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
(function($, base, common, uiMaskLayer, uiPopup) {
	var popupWindow = {
	  superClass : uiPopup,
	
	  // TODO: manage zIndex properties
	  /**
	   * Shows the popup window passed in parameter gets the highest z-index
	   * property of the elements in the page : . gets the z-index of the maskLayer .
	   * gets all the other popup windows . gets the highest z-index from these, if
	   * it's still at 0, set an arbitrary value of 2000 sets the position of the
	   * popup on the page (top and left properties)
	   */
	  show : function(popupId, isShowMask, middleBrowser) {
	    var popup = document.getElementById(popupId);
	    if (popup == null) return;        
	
	    // TODO Lambkin: this statement create a bug in select box component in
	    // Firefox
	    // this.superClass.init(popup) ;    
	    var popupBar = $(popup).find("span.PopupTitle")[0];
	    this.initDND(popupBar, popup);
	    
	    var resizeBtn = $(popup).find("span.ResizeButton")[0];
	    if (resizeBtn) {
	    	resizeBtn.style.display = 'block';
	    	resizeBtn.onmousedown = this.startResizeEvt;
	    }    	
	
	    if (isShowMask)
	    	popupWindow.showMask(popup, true);
	    popup.style.visibility = "hidden";
	    this.superClass.show(popup);
	    
	    if ($(popup).find("iframe").length > 0) {
	    	setTimeout(function() {popupWindow.setupWindow(popup, middleBrowser);}, 500);
	    } else {
	    	this.setupWindow(popup, middleBrowser);
	    }
	  },
	  
	  setupWindow : function(popup, middleBrowser) {	    	
	    var contentBlock = $(popup).find("div.PopupContent")[0];
	    var browserHeight = $(window).height();
	    if (contentBlock && (browserHeight - 100 < contentBlock.offsetHeight)) {
	      contentBlock.style.height = (browserHeight - 100) + "px";
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
	      var middleWindow = $(offsetParent).is(".UIPopupWindow,.UIWindow");
	      if (middleWindow) {
	        popup.style.top = Math.ceil((offsetParent.offsetHeight - popup.offsetHeight) / 2) + "px";
	      }
	      if (middleBrowser || !middleWindow) {
	        popup.style.top = Math.ceil((browserHeight - popup.offsetHeight) / 2) + scrollY + "px";
	      }
	      // Todo: set popup of UIPopup always display in the center browsers in case UIMaskWorkspace
	      if ($(offsetParent).hasClass("UIMaskWorkspace")) {
	        popup.style.top = Math.ceil((offsetParent.offsetHeight - popup.offsetHeight) / 2) + "px";
	      }
	      
	      // hack for position popup alway top in IE6.
	      var checkHeight = popup.offsetHeight > 300;
	
	      if (document.getElementById("UIDockBar") && checkHeight) {
	        popup.style.top = "6px";
	      }
	      popup.style.left = Math.ceil((offsetParent.offsetWidth - popup.offsetWidth) / 2) + "px";
	    }
	    if ($(popup).offset().top < 0)
	      popup.style.top = scrollY + "px";
	        
	    popup.style.visibility = "visible";	  
	  },
	  
	  hide : function(popupId, isShowMask) {
		var popup = document.getElementById(popupId);
		if (popup == null) return;     
	    this.superClass.hide(popup);
	    if (isShowMask) popupWindow.showMask(popup, false);
	  },
	  
	  showMask : function(popup, isShowPopup) {
	    var mask = popup.previousSibling;
	    // Make sure mask is not TextNode because of previousSibling property
	    if (mask && mask.className != "MaskLayer") {
	      mask = null;
	    }
	    if (isShowPopup) {
	      // Modal if popup is portal component
	      if ($(popup).parents(".PORTLET-FRAGMENT").length < 1){
	        if (!mask)
	          uiMaskLayer.createMask(popup.parentNode, popup, 1);
	      } else {
	        // If popup is portlet's component, modal with just its parent
	        if (!mask)
	          uiMaskLayer.createMaskForFrame(popup.parentNode, popup, 1);
	      }
	    } else {
	      if (mask)
	        uiMaskLayer.removeMask(mask);
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
		popupWindow.backupEvent = null;
		if (navigator.userAgent.indexOf("MSIE") >= 0) {
			//Need to check if we have remove resizedPopup after last mouseUp
			//IE bug: not call endResizeEvt when mouse moved out of page
			if (!popupWindow.resizedPopup && document.onselectstart) {
				popupWindow.backupEvent = document.onselectstart;
			}
			document.onselectstart = function() {return false};		
		} else {		
			if (document.onmousedown) {
				popupWindow.backupEvent = document.onmousedown;
			}
			document.onmousedown = function() {return false};		
		}
		
		var targetPopup = $(this).parents(".UIPopupWindow")[0];
		popupWindow.resizedPopup = targetPopup;
		popupWindow.backupPointerY = base.Browser.findMouseRelativeY(targetPopup, evt) ;	
	
	    document.onmousemove = popupWindow.resize;
	    document.onmouseup = popupWindow.endResizeEvt;
	  },
	
	  /**
	   * Function called when the window is being resized . gets the position of the
	   * mouse . calculates the height and the width of the window from this
	   * position . sets these values to the window
	   */
	  resize : function(evt) {
		var targetPopup = popupWindow.resizedPopup ;
	    var content = $(targetPopup).find("div.PopupContent")[0];
	    var isRTL = eXo.core.I18n.isRT();
	    var pointerX = base.Browser.findMouseRelativeX(targetPopup, evt, isRTL);
	    var pointerY = base.Browser.findMouseRelativeY(targetPopup, evt);
	    var delta = pointerY - popupWindow.backupPointerY;  
	    if ((content.offsetHeight + delta) > 0) {
	    	popupWindow.backupPointerY = pointerY;              
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
		popupWindow.resizedPopup = null;
	    this.onmousemove = null;
	    this.onmouseup = null;
	    
	    //enable select text
		if (navigator.userAgent.indexOf("MSIE") >= 0) {
			document.onselectstart = popupWindow.backupEvent;
		} else {                
			document.onmousedown = popupWindow.backupEvent;
		}
		popupWindow.backupEvent = null;
	  },
	
	  /**
	   * Init the Drag&Drop infrastructure using DragDrop
	   *
	   * @param popupBar
	   * @param popup
	   */
	  initDND : function(popupBar, popup)
	  {
	    common.DragDrop.init(popupBar, popup);
	
	    popup.onDragStart = function(x, y, last_x, last_y, e)
	    {
	      if (base.Browser.isFF() && popup.uiWindowContent)
	      {
	        popup.uiWindowContent.style.overflow = "auto";
	        $(popup.uiWindowContent).find("ul.PopupMessageBox").css("overflow", "auto");
	      }
	    };
	
	    popup.onDrag = function(nx, ny, ex, ey, e)
	    {
	    };
	
	    popup.onDragEnd = function(x, y, clientX, clientY)
	    {
	      if (base.Browser.isFF() && popup.uiWindowContent)
	      {
	        popup.uiWindowContent.style.overflow = "auto";
	        $(popup.uiWindowContent).find("ul.PopupMessageBox").css("overflow", "auto");
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
	
	return popupWindow;
})($, base, common, uiMaskLayer, uiPopup);