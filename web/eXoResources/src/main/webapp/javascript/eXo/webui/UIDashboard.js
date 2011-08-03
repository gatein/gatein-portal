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

function UIDashboard() {
	
	var currCol = null;	
	var targetObj = null;
	
	UIDashboard.prototype.init = function (dragItem, dragObj) {
		
		var DOMUtil = eXo.core.DOMUtil;
		eXo.core.DragDrop2.init(dragItem, dragObj);

		dragObj.onDragStart = function(x, y, lastMouseX, lastMouseY, e) {
			var uiDashboard = eXo.webui.UIDashboard ;
			var portletFragment = DOMUtil.findAncestorByClass(dragObj, "PORTLET-FRAGMENT");
			if(!portletFragment) return;
			
			var uiWorkingWS = document.getElementById("UIWorkingWorkspace");
			var gadgetContainer = DOMUtil.findFirstDescendantByClass(portletFragment, "div", "GadgetContainer");

			var ggwidth = dragObj.offsetWidth;
			var ggheight = dragObj.offsetHeight;
			
			//find position to put drag object in
			var mx = eXo.webui.UIDashboardUtil.findMouseRelativeX(uiWorkingWS, e);
			var ox = eXo.webui.UIDashboardUtil.findMouseRelativeX(dragObj, e);
			var x = mx-ox;
				
			var my = eXo.webui.UIDashboardUtil.findMouseRelativeY(uiWorkingWS, e);
			var oy = eXo.webui.UIDashboardUtil.findMouseRelativeY(dragObj, e);
			var y = my-oy;

			var temp = dragObj;
			while(temp.parentNode && DOMUtil.hasDescendant(portletFragment, temp)) {
				if(temp.scrollLeft>0) 
					x -= temp.scrollLeft;
				if(temp.scrollTop>0)
					y -= temp.scrollTop;
				temp = temp.parentNode;
			}
			
			var uiTarget = null;
			if(!DOMUtil.hasClass(dragObj, "SelectItem")) {
				uiTarget = uiDashboard.createTarget(ggwidth, 0);
				dragObj.parentNode.insertBefore(uiTarget, dragObj.nextSibling);
				currCol = eXo.webui.UIDashboardUtil.findColIndexInDashboard(dragObj);
			}else{
				var dragCopyObj = dragObj.cloneNode(true);
				DOMUtil.addClass(dragCopyObj, "CopyObj");
				dragObj.parentNode.insertBefore(dragCopyObj, dragObj);
				targetObj = null;
			}
			dragObj.style.width = ggwidth +"px";

			//increase speed of mouse when over iframe by create div layer above it
			var uiGadgets = DOMUtil.findDescendantsByClass(gadgetContainer, "div", "UIGadget");
			for(var i=0; i<uiGadgets.length; i++) {
				var uiMask = DOMUtil.findFirstDescendantByClass(uiGadgets[i], "div", "UIMask");
				if(uiMask!=null) {
					var gadgetApp = DOMUtil.findFirstDescendantByClass(uiGadgets[i], "div", "GadgetApplication");
					uiMask.style.marginTop = - gadgetApp.offsetHeight + "px";
					uiMask.style.height = gadgetApp.offsetHeight + "px";
					uiMask.style.width = gadgetApp.offsetWidth + "px";
					uiMask.style.display = "block";
					uiMask.style.backgroundColor = "white";
					eXo.core.Browser.setOpacity(uiMask, 3);
				}
			}
			
			if(!DOMUtil.hasClass(dragObj, "Dragging"))
				DOMUtil.addClass(dragObj, "Dragging");
				
			//set position of drag object
			dragObj.style.position = "absolute";
			eXo.webui.UIDashboardUtil.setPositionInContainer(uiWorkingWS, dragObj, x, y);
			if(uiTarget!=null) {
				uiTarget.style.height = ggheight +"px";
				targetObj = uiTarget;
			}
		}
		
		
		
		dragObj.onDrag = function(nx, ny, ex, ey, e) {	
			var uiTarget = targetObj;
			var portletFragment = DOMUtil.findAncestorByClass(dragObj, "PORTLET-FRAGMENT");

			if(!portletFragment) return;
			
			var dashboardCont = DOMUtil.findFirstDescendantByClass(portletFragment, "div", "GadgetContainer");
			var cols = null;
			
			eXo.webui.UIDashboard.scrollOnDrag(dragObj);
			if(eXo.webui.UIDashboardUtil.isIn(ex, ey, dashboardCont)) {
				if(!uiTarget) {
					uiTarget = eXo.webui.UIDashboard.createTargetOfAnObject(dragObj);
					targetObj = uiTarget;
				}
				
				var uiCol = currCol ;
				
				if(!uiCol) {
					if(!cols) cols = DOMUtil.findDescendantsByClass(dashboardCont, "div", "UIColumn");
					for(var i=0; i<cols.length; i++) {
						var uiColLeft = eXo.webui.UIDashboardUtil.findPosX(cols[i]) - dashboardCont.scrollLeft;
						if(uiColLeft<ex  &&  ex<uiColLeft+cols[i].offsetWidth) {
							currCol = uiCol = cols[i];
							break;
						}
					}
					
				}
				
				if(!uiCol) return;

				var uiColLeft = eXo.webui.UIDashboardUtil.findPosX(uiCol) - dashboardCont.scrollLeft;
				if(uiColLeft<ex  &&  ex<uiColLeft+uiCol.offsetWidth ) {
					var gadgets = DOMUtil.findDescendantsByClass(uiCol, "div", "UIGadget");
					//remove drag object from dropable target
					for(var i=0; i<gadgets.length; i++) {
						if(dragObj.id==gadgets[i].id) {
							gadgets.splice(i,1);
							break;
						}
					}

					if(gadgets.length == 0) {
						uiCol.appendChild(uiTarget);
						return;
					}

					//find position and add uiTarget into column				
					for(var i=0; i<gadgets.length; i++) {
						var oy = eXo.webui.UIDashboardUtil.findPosY(gadgets[i]) + (gadgets[i].offsetHeight/3) - dashboardCont.scrollTop;
						
						if(ey<=oy) {
							uiCol.insertBefore(uiTarget, gadgets[i]);
							break;
						}
						if(i==gadgets.length-1 && ey>oy) uiCol.appendChild(uiTarget);
					}
					
				}	else {

					//find column which draggin in					
					if(cols == null) cols = DOMUtil.findDescendantsByClass(dashboardCont, "div", "UIColumn");
					for(var i=0; i<cols.length; i++) {
						var uiColLeft = eXo.webui.UIDashboardUtil.findPosX(cols[i]) - dashboardCont.scrollLeft;
						if(uiColLeft<ex  &&  ex<uiColLeft+cols[i].offsetWidth) {
							currCol = cols[i];
							break;
						}
					}
				}
			} else {
				//prevent dragging gadget object out of DashboardContainer
				if(uiTarget!=null && DOMUtil.hasClass(dragObj, "SelectItem")) {
					uiTarget.parentNode.removeChild(uiTarget);					
					targetObj = uiTarget = null;
				}
			}
		}


	
		dragObj.onDragEnd = function(x, y, clientX, clientY) {
			var uiDashboardUtil = eXo.webui.UIDashboardUtil;
			var portletFragment = DOMUtil.findAncestorByClass(dragObj, "PORTLET-FRAGMENT");
			
			if(!portletFragment) return;
			
			var masks = DOMUtil.findDescendantsByClass(portletFragment, "div", "UIMask");
			for(var i=0; i<masks.length; i++) {
				eXo.core.Browser.setOpacity(masks[i], 100);
				masks[i].style.display = "none";
			}
			
			var uiTarget = targetObj;
			if(uiTarget && !uiTarget.parentNode) { 
				uiTarget = null; 
			}
			dragObj.style.position = "static";
			DOMUtil.removeClass(dragObj,"Dragging");

			var dragCopyObj = DOMUtil.findFirstDescendantByClass(portletFragment, "div", "CopyObj");
			if(dragCopyObj) {
				dragCopyObj.parentNode.replaceChild(dragObj, dragCopyObj);
				dragObj.style.width = "auto";
			}
			
			if(uiTarget) {	
				//if drag object is not gadget module, create an module
				var col = uiDashboardUtil.findColIndexInDashboard(uiTarget);
				var row = uiDashboardUtil.findRowIndexInDashboard(uiTarget);
				var compId = portletFragment.parentNode.id;
				
				if(DOMUtil.hasClass(dragObj, "SelectItem")) {
					var params = [
						{name: "colIndex", value: col},
						{name: "rowIndex", value: row},
						{name: "objectId", value: dragObj.id}
					];
					var url = uiDashboardUtil.createRequest(compId, 'AddNewGadget', params);
					ajaxGet(url);
				} else {
					//in case: drop to old position
					if(uiDashboardUtil.findColIndexInDashboard(dragObj) == col 
								&& uiDashboardUtil.findRowIndexInDashboard(dragObj) == (row-1)) {
						uiTarget.parentNode.removeChild(uiTarget);
					} else {					
						uiTarget.parentNode.replaceChild(dragObj, uiTarget);
						row = uiDashboardUtil.findRowIndexInDashboard(dragObj);
						var params = [
							{name: "colIndex", value: col},
							{name: "rowIndex", value: row},
							{name: "objectId", value: dragObj.id}
						];
						var url = uiDashboardUtil.createRequest(compId, 'MoveGadget', params);
						ajaxGet(url);
					}
				}
			}

			uiTarget = DOMUtil.findFirstDescendantByClass(portletFragment, "div", "UITarget");
			while (uiTarget) {
				DOMUtil.removeElement(uiTarget);
				uiTarget = eXo.core.DOMUtil.findFirstDescendantByClass(portletFragment, "div", "UITarget");
			}
			targetObj = currCol = null;
		}
		
		
		dragObj.onCancel = function(e){
			if(eXo.core.Browser.browserType == "ie" && eXo.core.Browser.findMouseYInClient() < 0) {
				eXo.core.DragDrop2.end(e);
			}
		}
	};
	
	UIDashboard.prototype.onLoad = function(windowId, canEdit) {		
		var portletWindow = document.getElementById(windowId);
		if(!portletWindow) {
			windowId = "UIPortlet-" + windowId;
			portletWindow = document.getElementById(windowId);
		}		
		
		var DOMUtil = eXo.core.DOMUtil;
		var uiDashboard = DOMUtil.findFirstDescendantByClass(portletWindow, "div", "UIDashboard");
		var portletFragment = DOMUtil.findAncestorByClass(uiDashboard, "PORTLET-FRAGMENT") ;
		var uiContainer = DOMUtil.findFirstDescendantByClass(uiDashboard, "div", "UIDashboardContainer");
		if(!uiContainer) return;
		
		var uiWindow = DOMUtil.findAncestorByClass(portletWindow, "UIWindow") ;
		if(uiWindow) {
			if(!uiWindow.resizeCallback) uiWindow.resizeCallback = new eXo.core.HashMap() ;
			uiWindow.resizeCallback.put(DOMUtil.generateId(windowId), eXo.webui.UIDashboard.initHeight) ;
		}
		
		var gadgetContainer = DOMUtil.findFirstChildByClass(uiContainer, "div", "GadgetContainer");
		uiDashboard.style.overflow = "hidden";
		portletFragment.style.overflow = "hidden" ;
		if(eXo.core.Browser.isIE6()) gadgetContainer.style.width = "99.5%";
		
		var selectPopup = DOMUtil.findPreviousElementByTagName(uiContainer, "div");
		var closeButton = DOMUtil.findFirstDescendantByClass(selectPopup, "div", "CloseButton");
		closeButton.onclick = eXo.webui.UIDashboard.showHideSelectContainer;
		
		var colsContainer = DOMUtil.findFirstChildByClass(gadgetContainer, "div", "UIColumns");
		var columns = DOMUtil.findChildrenByClass(colsContainer, "div", "UIColumn");
		var colsSize = 0;
		for(var i=0; i<columns.length; i++) {
			if(columns[i].style.display != "none") colsSize++;
		}
		//if(colsSize*320 + 20> uiContainer.offsetWidth)	colsContainer.style.width = colsSize*320 + "px";
		//else colsContainer.style.width = "100%" ;
		colsContainer.style.width = "100%" ;

		eXo.webui.UIDashboard.initHeight(windowId) ;
		
		//Todo: nguyenanhkien2a@gmail.com
		//We set and increase waiting time for initDragDrop function to make sure all UI (tag, div, iframe, etc) 
		//was loaded and to avoid some potential bugs (ex: GTNPORTAL-1068)
		setTimeout("eXo.webui.UIDashboard.initDragDrop('" + windowId + "'," + canEdit + ");", 400) ;
	};
	
	UIDashboard.prototype.initDragDrop = function(windowId, canEdit) {
		var DOMUtil = eXo.core.DOMUtil ;
		var portletWindow = document.getElementById(windowId) ;
		var gadgetControls = DOMUtil.findDescendantsByClass(portletWindow, "div", "GadgetControl");
		for(var j=0; j<gadgetControls.length; j++) {
			var uiGadget = DOMUtil.findAncestorByClass(gadgetControls[j],"UIGadget");
			var iframe = DOMUtil.findFirstDescendantByClass(uiGadget, "iframe", "gadgets-gadget") ;
			if (iframe) {
				iframe.style.width = "99.9%" ;
			}
			var minimizeButton = DOMUtil.findFirstDescendantByClass(gadgetControls[j], "div", "MinimizeAction") ;
			if(canEdit) {
				eXo.webui.UIDashboard.init(gadgetControls[j], uiGadget);
				
				if(minimizeButton) minimizeButton.style.display = "block" ;
				uiGadget.minimizeCallback = eXo.webui.UIDashboard.initHeight ;
			} else{
				if(minimizeButton) {
					minimizeButton.style.display = "none" ;
					var controlBar = minimizeButton.parentNode ;
					var closeButton = DOMUtil.findFirstChildByClass(controlBar, "div", "CloseGadget") ;
					var editButton = DOMUtil.findFirstChildByClass(controlBar, "div", "EditGadget") ;
					closeButton.style.display = "none" ;
					editButton.style.display = "none" ;
				}
			}
		}
	};
	
	UIDashboard.prototype.initHeight = function(windowId) {
		var DOMUtil = eXo.core.DOMUtil;
		var portletWindow, uiWindow ;
		if(typeof(windowId) != "string") {
			uiWindow = eXo.desktop.UIWindow.portletWindow ;
			portletWindow = document.getElementById(uiWindow.id.replace(/^UIWindow-/, "")) ;
		} else {
			portletWindow = document.getElementById(windowId) ;
			uiWindow = DOMUtil.findAncestorByClass("UIWindow") ;
		}
		var uiDashboard = DOMUtil.findFirstDescendantByClass(portletWindow, "div", "UIDashboard") ;
		var uiSelect = DOMUtil.findFirstDescendantByClass(uiDashboard, "div", "UIDashboardSelectContainer");
		
		if(uiSelect && document.getElementById("UIPageDesktop")) {
			var itemCont = DOMUtil.findFirstChildByClass(uiSelect, "div", "DashboardItemContainer");
			var middleItemCont = DOMUtil.findFirstDescendantByClass(uiSelect, "div", "MiddleItemContainer");
			var topItemCont = DOMUtil.findNextElementByTagName(middleItemCont, "div");
			var bottomItemCont = DOMUtil.findPreviousElementByTagName(middleItemCont, "div");
			
			var uiContainer = DOMUtil.findFirstDescendantByClass(uiDashboard, "div", "UIDashboardContainer");
			
			var minusHeight = 0 ;
			var minusHeightEle = DOMUtil.findPreviousElementByTagName(middleItemCont.parentNode, "div") ; 
			while(minusHeightEle) {
				minusHeight += minusHeightEle.offsetHeight ;
				minusHeightEle = DOMUtil.findPreviousElementByTagName(minusHeightEle, "div") ;
			}
			minusHeightEle = DOMUtil.findPreviousElementByTagName(itemCont, "div") ;
			while(minusHeightEle) {
				minusHeight += minusHeightEle.offsetHeight ;
				minusHeightEle = DOMUtil.findPreviousElementByTagName(minusHeightEle, "div") ;
			}
			minusHeightEle = null;
			var windowHeight = portletWindow.offsetHeight ; 
			if(uiWindow && uiWindow.style.display == "none") {
				windowHeight = parseInt(DOMUtil.getStyle(portletFragment, "height")) ;
			}
			var middleItemContHeight = windowHeight - minusHeight
																- parseInt(DOMUtil.getStyle(itemCont,"paddingTop"))
																- parseInt(DOMUtil.getStyle(itemCont,"paddingBottom"))
																- 5 ;
	    // fix bug IE 6
		  if (middleItemContHeight < 0) {
		  	middleItemContHeight = 0;
		  }
			middleItemCont.style.height = middleItemContHeight + "px";
			
			//TODO: tan.pham: fix bug WEBOS-272: Ie7 can get positive scrollHeight value althrought portlet doesn't display
			if(middleItemCont.offsetHeight > 0) {
				if(middleItemCont.scrollHeight > middleItemCont.offsetHeight) {
					topItemCont.style.display = "block";
					bottomItemCont.style.display = "block";
					middleItemCont.style.height = middleItemCont.offsetHeight - topItemCont.offsetHeight - bottomItemCont.offsetHeight + "px";
				} else {
					topItemCont.style.display = "none";
					bottomItemCont.style.display = "none";
					middleItemCont.scrollTop = 0 ;
				}
			}
		}
	};
	
	UIDashboard.prototype.initPopup = function(popup) {
		if(typeof(popup) == "string") popup = document.getElementById(popup);
		if(!popup || popup.style.display == "none") return;
		var uiDashboard = eXo.core.DOMUtil.findAncestorByClass(popup, "UIDashboard");
		var deltaY = Math.ceil((uiDashboard.offsetHeight - popup.offsetHeight) / 2);
		if (deltaY < 0) {
			deltaY = 0;
		}
		popup.style.top = eXo.core.Browser.findPosY(uiDashboard) + deltaY + "px";
	};
	/**
	 * Build a UITarget element (div element) with properties in parameters
	 * @param {Number} width
	 * @param {Number} height
	 */
	UIDashboard.prototype.createTarget = function(width, height) {
		var uiTarget = document.createElement("div");
		uiTarget.id = "UITarget";
		uiTarget.className = "UITarget";
		uiTarget.style.width = width + "px";
		uiTarget.style.height = height + "px";
		return uiTarget;
	};
	 /**
   * Build a UITarget element (div element) with properties equal to object's properties in parameter
   * @param {Object} obj object
   */
	UIDashboard.prototype.createTargetOfAnObject = function(obj) {
		var uiTarget = document.createElement("div");
		uiTarget.id = "UITarget";
		uiTarget.className = "UITarget";
		uiTarget.style.height = obj.offsetHeight + "px";
		return uiTarget;
	};
	 /**
   * Show and hide gadget list for selecting gadget in dashboard
   * @param {Object} comp indicate action show and hide, if it is close button, action is hide
   */
	UIDashboard.prototype.showHideSelectContainer = function(event) {
		if(!event) event = window.event;
		var DOMUtil = eXo.core.DOMUtil;
		var comp = eXo.core.Browser.getEventSource(event);
		var uiDashboardPortlet = DOMUtil.findAncestorByClass(comp, "UIDashboard");
		var portletFragment = DOMUtil.findAncestorByClass(uiDashboardPortlet, "PORTLET-FRAGMENT");
		var uiContainer = DOMUtil.findFirstDescendantByClass(uiDashboardPortlet, "div", "UIDashboardContainer");
		var uiSelectPopup = DOMUtil.findPreviousElementByTagName(uiContainer, "div");
		var addButton = DOMUtil.findFirstDescendantByClass(uiContainer, "div", "ContainerControlBarL");

		var params;
		if(uiSelectPopup.style.display != "none") {
			uiSelectPopup.style.visibility = "hidden";
			uiSelectPopup.style.display = "none";
			addButton.style.visibility = "visible";
			params = [{name: "isShow", value: false}];
			var url = eXo.webui.UIDashboardUtil.createRequest(portletFragment.parentNode.id, "SetShowSelectContainer", params);
			ajaxAsyncGetRequest(url, false);
		} else {
			addButton.style.visibility = "hidden";
			params = [{name: "isShow", value: true}];
			var url = eXo.webui.UIDashboardUtil.createRequest(portletFragment.parentNode.id, "SetShowSelectContainer", params);
			ajaxGet(url);
		}
	};
	 /**
   * Using when click event happens on a dashboard tab
   * @param {Object} clickElement
   * @param {String} normalStyle a css style
   * @param {String} selectedType a css style
   */
	UIDashboard.prototype.onTabClick = function(clickElement, normalStyle, selectedType) {
		var DOMUtil = eXo.core.DOMUtil;
		var category = DOMUtil.findAncestorByClass(clickElement, "GadgetCategory");
		var categoryContent = DOMUtil.findFirstChildByClass(category, "div", "ItemsContainer");
		var categoriesContainer = DOMUtil.findAncestorByClass(category, "GadgetItemsContainer");
		var categories = DOMUtil.findChildrenByClass(categoriesContainer, "div", "GadgetCategory");
		var gadgetTab = DOMUtil.findFirstChildByClass(category, "div", "GadgetTab");
		
		if(DOMUtil.hasClass(gadgetTab, normalStyle)) {
			for(var i=0; i<categories.length; i++) {
				DOMUtil.findFirstChildByClass(categories[i], "div", "GadgetTab").className = "GadgetTab " + normalStyle;
				DOMUtil.findFirstChildByClass(categories[i], "div", "ItemsContainer").style.display = "none";
			}
			DOMUtil.findFirstChildByClass(category, "div", "GadgetTab").className = "GadgetTab " + selectedType;
			categoryContent.style.display = "block";
		} else {
			DOMUtil.findFirstChildByClass(category, "div", "GadgetTab").className = "GadgetTab " + normalStyle;
			categoryContent.style.display = "none";
		}
	};
	/**
	 * Change disabled object to enable state
	 * @param {Object} elemt object to enable
	 */
	UIDashboard.prototype.enableContainer = function(elemt) {
		var DOMUtil = eXo.core.DOMUtil;
		if(DOMUtil.hasClass(elemt, "DisableContainer")) {
			DOMUtil.replaceClass(elemt, " DisableContainer", "");
		}
		var arrow = DOMUtil.findFirstChildByClass(elemt, "div", "Arrow");
		if(DOMUtil.hasClass(arrow, "DisableArrowIcon")) DOMUtil.replaceClass(arrow," DisableArrowIcon", "");
	};
	 /**
   * Change object to disable state
   * @param {Object} elemt object to enable
   */
	UIDashboard.prototype.disableContainer = function(elemt) {
		var DOMUtil = eXo.core.DOMUtil;
		if(!DOMUtil.hasClass(elemt, "DisableContainer")) {
			DOMUtil.addClass(elemt, "DisableContainer");
		}
		var arrow = DOMUtil.findFirstChildByClass(elemt, "div", "Arrow");
		if(!DOMUtil.hasClass(arrow, "DisableArrowIcon")) DOMUtil.addClass(arrow," DisableArrowIcon");
	};
	
	UIDashboard.prototype.scrollOnDrag = function(dragObj) {
		var DOMUtil = eXo.core.DOMUtil;
		var dashboardUtil = eXo.webui.UIDashboardUtil;
		var uiDashboard = DOMUtil.findAncestorByClass(dragObj, "UIDashboard");
		var gadgetContainer = DOMUtil.findFirstDescendantByClass(uiDashboard, "div", "GadgetContainer");
		var colCont = DOMUtil.findFirstChildByClass(gadgetContainer, "div", "UIColumns");
		
		if(!DOMUtil.findFirstDescendantByClass(colCont, "div", "UITarget")) return;
		
		var visibleWidth = gadgetContainer.offsetWidth;
		var visibleHeight = gadgetContainer.offsetHeight;
		var trueWidth = colCont.offsetWidth;
		var trueHeight = colCont.offsetHeight;
		
		var objLeft = dashboardUtil.findPosXInContainer(dragObj, gadgetContainer);
		var objRight = objLeft + dragObj.offsetWidth;
		var objTop = dashboardUtil.findPosYInContainer(dragObj, gadgetContainer);
		var objBottom = objTop + dragObj.offsetHeight;
		
		//controls horizontal scroll
		var deltaX = gadgetContainer.scrollLeft;
		if((trueWidth - (visibleWidth + deltaX) > 0) && objRight > visibleWidth) {
			gadgetContainer.scrollLeft += 5;
		} else {
			if(objLeft < 0 && deltaX > 0) gadgetContainer.scrollLeft -= 5;
		}
		
		//controls vertical scroll
		var controlBar = DOMUtil.findFirstChildByClass(gadgetContainer, "div", "ContainerControlBarL");
		var buttonHeight = 0 ;
		if(controlBar) buttonHeight = controlBar.offsetHeight;
		var deltaY = gadgetContainer.scrollTop;
		if((trueHeight - (visibleHeight -10 - buttonHeight + deltaY) > 0) && objBottom > visibleHeight) {
			gadgetContainer.scrollTop += 5;
		}	else {
			if(objTop < 0 && deltaY > 0) gadgetContainer.scrollTop -= 5;
		}
	};
};

eXo.webui.UIDashboard = new UIDashboard();
