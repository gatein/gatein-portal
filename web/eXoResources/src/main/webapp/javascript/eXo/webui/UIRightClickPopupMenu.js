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

function UIRightClickPopupMenu() {};
/**
 * Initialize a UIRightClickPopupMenu object
 * @param contextMenuId identifier of a document object
 */
UIRightClickPopupMenu.prototype.init = function(contextMenuId) {
	var contextMenu = document.getElementById(contextMenuId) ;
	// TODO: Fix temporary for the problem Minimize window in Page Mode
	if(!contextMenu) return;
	
	contextMenu.onmousedown = function(e) {
		if(!e) e = window.event ;
		e.cancelBubble = true ;
	}

	var parentNode = contextMenu.parentNode ;
	this.disableContextMenu(parentNode) ;
}
/**
 * Hide and disable mouse down event of context menu object
 * @param contextId identifier of context menu
 */
UIRightClickPopupMenu.prototype.hideContextMenu = function(contextId) {
	if (document.getElementById(contextId)) {
		document.getElementById(contextId).style.display = 'none' ;
		eXo.core.MouseEventManager.onMouseDownHandlers = null ;
	}
}
/**
 * Disable default context menu of browser
 * @param comp identifier or document object
 */
UIRightClickPopupMenu.prototype.disableContextMenu = function(comp) {
	if(typeof(comp) == "string") comp = document.getElementById(comp) ;
	comp.onmouseover = function() {
		document.oncontextmenu = function() {return false} ;
	}
	comp.onmouseout = function() {
		document.oncontextmenu = function() {return true} ;
	}
};
/**
 * Prepare something for context menu
 * @param {Object} evt event
 * @param {Object} elemt document object that contains context menu
 */
UIRightClickPopupMenu.prototype.prepareObjectId = function(evt, elemt) {
	var contextMenu = eXo.core.DOMUtil.findAncestorByClass(elemt, "UIRightClickPopupMenu") ;
	contextMenu.style.display = "none" ;
	var href = elemt.getAttribute('href') ;
	if(href.indexOf("javascript") == 0) {
		eval(unescape(href).replace('_objectid_', encodeURI(contextMenu.objId.replace(/'/g, "\\'")))) ; 
		eXo.core.MouseEventManager.docMouseDownEvt(evt) ;
		return false;
	}
	elemt.setAttribute('href', href.replace('_objectid_', encodeURI(contextMenu.objId.replace(/'/g, "\\'")))) ;
	return true;
}
/**
 * Mouse click on element, If click is right-click, the context menu will be shown
 * @param {Object} event
 * @param {Object} elemt clicked element
 * @param {String} menuId identifier of context menu will be shown
 * @param {String} objId object identifier in tree
 * @param {Array} params
 * @param {Number} opt option
 */
UIRightClickPopupMenu.prototype.clickRightMouse = function(event, elemt, menuId, objId, params, opt) {
	if (!event) event = window.event;
	eXo.core.MouseEventManager.docMouseDownEvt(event) ;
	var contextMenu = document.getElementById(menuId) ;
	contextMenu.objId = objId ;
	if(!(((event.which) && (event.which == 2 || event.which == 3)) || ((event.button) && (event.button == 2))))	{
		contextMenu.style.display = 'none' ;
		return;
	}
	
	eXo.core.MouseEventManager.addMouseDownHandler("eXo.webui.UIRightClickPopupMenu.hideContextMenu('" + menuId + "');")

	if(params) {
		params = "," + params + "," ;
		var items = contextMenu.getElementsByTagName("a") ;
		for(var i = 0; i < items.length; i++) {
			if(params.indexOf(items[i].getAttribute("exo:attr")) > -1) {
				items[i].style.display = 'block' ;
			} else {
				items[i].style.display = 'none' ;
			}
		}
	}
	var customItem = eXo.core.DOMUtil.findFirstDescendantByClass(elemt, "div", "RightClickCustomItem") ;
	var tmpCustomItem = eXo.core.DOMUtil.findFirstDescendantByClass(contextMenu, "div", "RightClickCustomItem") ;
	if(tmpCustomItem) {
		if(customItem) {
			tmpCustomItem.innerHTML = customItem.innerHTML ;
			tmpCustomItem.style.display = "block" ;
		} else {
			tmpCustomItem.style.display = "none" ;
		}
	}
		/*
	 * fix bug right click in IE7.
	 */
	var fixWidthForIE7 = 0 ;
	var UIWorkingWorkspace = document.getElementById("UIWorkingWorkspace") ;
	if (eXo.core.Browser.isIE7() && document.getElementById("UIDockBar")) {
		  if (event.clientX > UIWorkingWorkspace.offsetLeft) fixWidthForIE7 = UIWorkingWorkspace.offsetLeft ;
	}
	
	eXo.core.Mouse.update(event);
	eXo.webui.UIPopup.show(contextMenu);
	
	var ctxMenuContainer = eXo.core.DOMUtil.findFirstChildByClass(contextMenu, "div", "UIContextMenuContainer") ;
	var intTop = eXo.core.Mouse.mouseyInPage - (eXo.core.Browser.findPosY(contextMenu) - contextMenu.offsetTop) ;
	var intLeft = eXo.core.Mouse.mousexInPage - (eXo.core.Browser.findPosX(contextMenu) - contextMenu.offsetLeft) + fixWidthForIE7 ;
	if(eXo.core.I18n.isRT()) {
		//scrollWidth is width of browser scrollbar
		var scrollWidth = 16 ;
		if(eXo.core.Browser.getBrowserType() == "mozilla") scrollWidth = 0 ;
		intLeft = contextMenu.offsetParent.offsetWidth - intLeft + fixWidthForIE7 + scrollWidth ;
		var clickCenter = eXo.core.DOMUtil.findFirstDescendantByClass(contextMenu, "div", "ClickCenterBottom") ;
		if(clickCenter) {
			var clickCenterWidth = clickCenter ? parseInt(eXo.core.DOMUtil.getStyle(clickCenter, "marginRight")) : 0 ;
			intLeft += (ctxMenuContainer.offsetWidth - 2*clickCenterWidth) ;
		}
	}

	switch (opt) {
		case 1:
			intTop -= ctxMenuContainer.offsetHeight ;
			break;
		case 2:
			break;
		case 3:
			break;
		case 4:
			break;
		default:
			if((eXo.core.Mouse.mouseyInClient + ctxMenuContainer.offsetHeight) > eXo.core.Browser.getBrowserHeight()) {
				intTop -= ctxMenuContainer.offsetHeight ;
			}
			break;
	}
	
	if(eXo.core.I18n.isLT()) {
		//move context menu to center of screen to fix width
		contextMenu.style.left = eXo.core.Browser.getBrowserWidth() * 0.5 + "px" ;
		ctxMenuContainer.style.width = "auto" ;
		ctxMenuContainer.style.width = ctxMenuContainer.offsetWidth + 2 + "px" ;
		//end fix width
		contextMenu.style.left = intLeft + "px";
	}	else {
		//move context menu to center of screen to fix width
		contextMenu.style.right = eXo.core.Browser.getBrowserWidth() * 0.5 + "px" ;
		ctxMenuContainer.style.width = "auto" ;
		ctxMenuContainer.style.width = ctxMenuContainer.offsetWidth + 2 + "px" ;
		//end fix width
		contextMenu.style.right = intLeft + "px" ;
	}
	ctxMenuContainer.style.width = ctxMenuContainer.offsetWidth + "px" ;
	contextMenu.style.top = intTop + 1 + "px";
};

eXo.webui.UIRightClickPopupMenu = new UIRightClickPopupMenu() ;