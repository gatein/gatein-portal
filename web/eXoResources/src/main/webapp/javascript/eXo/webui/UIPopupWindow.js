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
function UIPopupWindow() {} ;
/**
 * Inits a popup window, with these parameters :
 *  . sets the superClass as eXo.webui.UIPopup
 *  . sets the popup hidden
 *  . inits the drag and drop
 *  . inits the resize area if the window is resizable
 */
UIPopupWindow.prototype.init = function(popupId, isShow, isResizable, showCloseButton, isShowMask) {
	var DOMUtil = eXo.core.DOMUtil ;
	this.superClass = eXo.webui.UIPopup ;
	var popup = document.getElementById(popupId) ;
	if(popup == null) return;
	popup.style.visibility = "hidden" ;
	
	//TODO Lambkin: this statement create a bug in select box component in Firefox
	//this.superClass.init(popup) ;
	if (isShow) {
		popup.style.display = "block";
	}
	var contentBlock = DOMUtil.findFirstDescendantByClass(popup, 'div' ,'PopupContent');
	if(contentBlock && (eXo.core.Browser.getBrowserHeight() - 100 < contentBlock.offsetHeight)) {
		contentBlock.style.height = (eXo.core.Browser.getBrowserHeight() - 100) + "px";
	}
	var popupBar = DOMUtil.findFirstDescendantByClass(popup, 'span' ,'PopupTitle') ;

	popupBar.onmousedown = this.initDND ;
	
	if(isShow == false) {
		this.superClass.hide(popup) ;
		if(isShowMask) eXo.webui.UIPopupWindow.showMask(popup, false) ;
	} 
	
	if(isResizable) {
		var resizeBtn = DOMUtil.findFirstDescendantByClass(popup, "span", "ResizeButton");
		resizeBtn.style.display = 'block' ;
		resizeBtn.onmousedown = this.startResizeEvt ;
	}
	
	popup.style.visibility = "hidden" ;
	if(isShow == true) {
		var iframes = DOMUtil.findDescendantsByTagName(popup, "iframe") ;
		if(iframes.length > 0) {
			setTimeout("eXo.webui.UIPopupWindow.show('" + popupId + "'," + isShowMask + ")", 500) ;
		} else {
			this.show(popup, isShowMask) ;
		}
	}
} ;

UIPopupWindow.prototype.showMask = function(popup, isShowPopup) {
	var mask = popup.previousSibling;
  //Make sure mask is not TextNode because of previousSibling property
	if (mask && mask.className != "MaskLayer") {
		mask = null;
	}
	if(isShowPopup) {
		//Modal if popup is portal component
		if (eXo.core.DOMUtil.findAncestorByClass(popup, "PORTLET-FRAGMENT") == null) {
			if(!mask) eXo.core.UIMaskLayer.createMask(popup.parentNode, popup, 1) ;
		} else {
			//If popup is portlet's component, modal with just its parent
			if(!mask) eXo.core.UIMaskLayer.createMaskForFrame(popup.parentNode, popup, 1) ;				
		}
	} else {		
		if(mask) eXo.core.UIMaskLayer.removeMask(mask) ;			
	}
} ;

//TODO: manage zIndex properties
/**
 * Shows the popup window passed in parameter
 * gets the highest z-index property of the elements in the page :
 *  . gets the z-index of the maskLayer
 *  . gets all the other popup windows
 *  . gets the highest z-index from these, if it's still at 0, set an arbitrary value of 2000
 * sets the position of the popup on the page (top and left properties)
 */
UIPopupWindow.prototype.show = function(popup, isShowMask, middleBrowser) {
	var DOMUtil = eXo.core.DOMUtil ;
	if(typeof(popup) == "string") popup = document.getElementById(popup) ;
	var portalApp = document.getElementById("UIPortalApplication") ;

	var maskLayer = DOMUtil.findFirstDescendantByClass(portalApp, "div", "UIMaskWorkspace") ;
	var zIndex = 0 ;
	var currZIndex = 0 ;
	if (maskLayer != null) {
		currZIndex = DOMUtil.getStyle(maskLayer, "zIndex") ;
		if (!isNaN(currZIndex) && currZIndex > zIndex) zIndex = currZIndex ;
	}
	var popupWindows = DOMUtil.findDescendantsByClass(portalApp, "div", "UIPopupWindow") ;
	var len = popupWindows.length ;
	for (var i = 0 ; i < len ; i++) {
		currZIndex = DOMUtil.getStyle(popupWindows[i], "zIndex") ;
		if (!isNaN(currZIndex) && currZIndex > zIndex) zIndex = currZIndex ;
	}
	if (zIndex == 0) zIndex = 2000 ;
	// We don't increment zIndex here because it is done in the superClass.show function
	if(isShowMask) eXo.webui.UIPopupWindow.showMask(popup, true) ;
	popup.style.visibility = "hidden" ;
	this.superClass.show(popup) ;
 	var offsetParent = popup.offsetParent ;
 	var scrollY = 0;
	if (window.pageYOffset != undefined) scrollY = window.pageYOffset;
	else if (document.documentElement && document.documentElement.scrollTop) scrollY = document.documentElement.scrollTop;
	else	scrollY = document.body.scrollTop;
	//reference
	if(offsetParent) {
		var middleWindow = (eXo.core.DOMUtil.hasClass(offsetParent, "UIPopupWindow") || eXo.core.DOMUtil.hasClass(offsetParent, "UIWindow"));
		if (middleWindow) {			
			popup.style.top = Math.ceil((offsetParent.offsetHeight - popup.offsetHeight) / 2) + "px" ;
		} 
		if (middleBrowser || !middleWindow) {
			popup.style.top = Math.ceil((eXo.core.Browser.getBrowserHeight() - popup.offsetHeight ) / 2) + scrollY + "px";
		}
		//Todo: set popup of UIPopup always display in the center browsers in case UIMaskWorkspace
		if(eXo.core.DOMUtil.hasClass(offsetParent, "UIMaskWorkspace")) {
			//if(eXo.core.Browser.browserType=='ie') offsetParent.style.position = "relative";
			popup.style.top = Math.ceil((offsetParent.offsetHeight - popup.offsetHeight) / 2) + "px" ;
		}
		
		// hack for position popup alway top in IE6.
		var checkHeight = popup.offsetHeight > 300; 

		if (document.getElementById("UIDockBar") && checkHeight) {
			popup.style.top = "6px";
		}
		if(eXo.core.I18n.lt) popup.style.left = Math.ceil((offsetParent.offsetWidth - popup.offsetWidth) / 2) + "px" ;
		else popup.style.right = Math.ceil((offsetParent.offsetWidth - popup.offsetWidth) / 2) + "px" ;
		
	}
	if (eXo.core.Browser.findPosY(popup) < 0) popup.style.top = scrollY + "px" ;
  popup.style.visibility = "visible" ;
} ;
/**
 * @param {Object} evt
 */
UIPopupWindow.prototype.increasezIndex = function(popup) {
  var DOMUtil = eXo.core.DOMUtil ;
	if(typeof(popup) == "string") popup = document.getElementById(popup) ;
	var portalApp = document.getElementById("UIPortalApplication") ;
  var uiLogin = DOMUtil.findFirstDescendantByClass(portalApp, "div", "UILoginForm"); 
  if(uiLogin) {
    var curMaskzIndex = parseInt(DOMUtil.getStyle(document.getElementById('UIMaskWorkspace'), "zIndex"));
    popup.style.zIndex = ++curMaskzIndex +"";
  }
}

/**
 * Hides (display: none) the popup window when the close button is clicked
 */
UIPopupWindow.prototype.closePopupEvt = function(evt) {
	eXo.core.DOMUtil.findAncestorByClass(this, "UIDragObject").style.display = "none" ;
}
/**
 * Called when the window starts being resized
 * sets the onmousemove and onmouseup events on the portal application (not the popup)
 * associates these events with UIPopupWindow.resize and UIPopupWindow.endResizeEvt respectively
 */
UIPopupWindow.prototype.startResizeEvt = function(evt) {		
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
	document.onmouseup = eXo.webui.UIPopupWindow.endResizeEvt ;
}

/**
 * Function called when the window is being resized
 *  . gets the position of the mouse
 *  . calculates the height and the width of the window from this position
 *  . sets these values to the window
 */
UIPopupWindow.prototype.resize = function(evt) {
	var targetPopup = eXo.webui.UIPopupWindow.resizedPopup ;
	var content = eXo.core.DOMUtil.findFirstDescendantByClass(targetPopup, "div", "PopupContent") ;
	var isRTL = eXo.core.I18n.isRT();
	var pointerX = eXo.core.Browser.findMouseRelativeX(targetPopup, evt, isRTL) ;
	var pointerY = eXo.core.Browser.findMouseRelativeY(targetPopup, evt) ;
	
	var delta = pointerY - eXo.webui.UIPopupWindow.backupPointerY;	
	if ((content.offsetHeight + delta) > 0) {
		eXo.webui.UIPopupWindow.backupPointerY = pointerY;		
		content.style.height = content.offsetHeight + delta +"px" ;	
	}
	targetPopup.style.height = "auto";
	
	if(isRTL){
	 pointerX = (-1) * pointerX;
	}	
	if(pointerX > 200) targetPopup.style.width = (pointerX+10) + "px" ;
} ;

/**
 * Called when the window stops being resized
 * cancels the mouse events on the portal app
 * inits the scroll managers active on this page (in case there is one in the popup)
 */
UIPopupWindow.prototype.endResizeEvt = function(evt) {
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
	
	// Added by Philippe
	// inits all the scroll managers, in case there is one in the popup that needs to be recalculated
	eXo.portal.UIPortalControl.initAllManagers();
	// other solutions :
	// - add a callback property that points to the init function of the concerned scroll manager. call it here
	// - add a boolean to each scroll manager that specifies if it's in a popup. re init only those that have this property true
}
/**
 * Inits the drag and drop
 * configures the DragDrop callback functions
 *  . initCallback : sets overflow: hidden to elements in the popup if browser is mozilla
 *  . dragCallback : empty
 *  . dropCallback : sets overflow: auto to elements in the popup if browser is mozilla
 */
UIPopupWindow.prototype.initDND = function(evt) {
  var DragDrop = eXo.core.DragDrop ;
  var DOMUtil = eXo.core.DOMUtil ;

	DragDrop.initCallback = function (dndEvent) {
		var dragObject = dndEvent.dragObject ;
		dragObject.uiWindowContent = DOMUtil.findFirstDescendantByClass(dragObject, "div", "PopupContent") ;
		if(!dragObject.uiWindowContent) return;
		if(eXo.core.Browser.browserType == "mozilla") {
			dragObject.uiWindowContent.style.overflow = "hidden" ;
			var elements = eXo.core.DOMUtil.findDescendantsByClass(dragObject.uiWindowContent,  "ul" ,"PopupMessageBox") ;
  		for(var i = 0; i < elements.length; i++) {
     	  elements[i].style.overflow  = "hidden" ;
			}
		}
  }

  DragDrop.dragCallback = function (dndEvent) {
  }

  DragDrop.dropCallback = function (dndEvent) {
  	var dragObject = dndEvent.dragObject ;
		if(eXo.core.Browser.browserType == "mozilla" && dragObject.uiWindowContent) {
			dragObject.uiWindowContent.style.overflow = "auto" ;
   		var elements = eXo.core.DOMUtil.findDescendantsByClass(dragObject.uiWindowContent,  "ul" ,"PopupMessageBox") ;
  		for(var i = 0; i < elements.length; i++) {
     	  elements[i].style.overflow  = "auto" ;
			}
		}
  	
  	var offsetParent = dragObject.offsetParent ;
  	if (offsetParent) {
  		if (eXo.core.Browser.findPosY(dragObject) < 0)  dragObject.style.top = (0 - offsetParent.offsetTop) + "px" ;
  	} else {
  		dragObject.style.top = "0px" ;
  	}
  }
  var clickBlock = this ;
  var dragBlock = eXo.core.DOMUtil.findAncestorByClass(this, "UIDragObject") ;
  DragDrop.init(null, clickBlock, dragBlock, evt) ;
} ;

eXo.webui.UIPopupWindow = new UIPopupWindow();
