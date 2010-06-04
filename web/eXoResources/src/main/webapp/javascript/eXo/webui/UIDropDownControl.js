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

function UIDropDownControl() {} ;

UIDropDownControl.prototype.init = function(id) {
	//var popup = document.getElementById(id) ;
	//return popup;
};

UIDropDownControl.prototype.selectItem = function(method, id, selectedIndex) {
	if(method)	method(id, selectedIndex) ;
} ;

/*.
 * minh.js.exo
 */
/**
 * show or hide drop down control
 * @param {Object} obj document object to use as Anchor for drop down
 * @param {Object} evet event object
 */
UIDropDownControl.prototype.show = function(obj, evt) {
	if(!evt) evt = window.event ;
	evt.cancelBubble = true ;
	
	var DOMUtil = eXo.core.DOMUtil ;
	var Browser = eXo.core.Browser ;
	var dropDownAnchor = DOMUtil.findNextElementByTagName(obj, 'div') ;	
	if (dropDownAnchor) {
		if (dropDownAnchor.style.display == "none") {
			dropDownAnchor.style.display = "block" ;
			dropDownAnchor.style.visibility = "visible" ;
			var middleCont = DOMUtil.findFirstDescendantByClass(dropDownAnchor, "div", "MiddleItemContainer") ;
			var topCont = DOMUtil.findPreviousElementByTagName(middleCont, "div") ;
			var bottomCont = DOMUtil.findNextElementByTagName(middleCont, "div") ;
			topCont.style.display = "block" ;
			bottomCont.style.display = "block" ;
			var visibleHeight = Browser.getBrowserHeight() - Browser.findPosY(middleCont) - 40 ;
			var scrollHeight = middleCont.scrollHeight ;
			if(scrollHeight > visibleHeight) {
				topCont.style.display = "block" ;
				bottomCont.style.display = "block" ;
				middleCont.style.height = visibleHeight - topCont.offsetHeight - bottomCont.offsetHeight + "px" ;
				topCont.onclick = function(event) {
					event = event || window.event;
					event.cancelBubble = true;
				};
				bottomCont.onclick = function(event){
					event = event || window.event;
					event.cancelBubble = true;
				}
			} else {
				topCont.style.display = "none" ;
				bottomCont.style.display = "none" ;
				middleCont.scrollTop = 0;
				middleCont.style.height = "auto";
			}
			DOMUtil.listHideElements(dropDownAnchor) ;
		}
		else {
			dropDownAnchor.style.display = "none" ;
			dropDownAnchor.style.visibility = "hidden" ;
		}
	}
	
} ;
/**
 * Hide an object
 * @param {Object, String} obj object to hide
 */
UIDropDownControl.prototype.hide = function(obj) {
	if (typeof(obj) == "string") obj = document.getElementById(obj) ;
	obj.style.display = "none" ;		
} ;
/**
 * Use as event when user selects a item in drop down list
 * Display content of selected item and hide drop down control
 * @param {Object} obj selected object
 * @param {Object} evt event
 */
UIDropDownControl.prototype.onclickEvt = function(obj, evt) {
	var DOMUtil = eXo.core.DOMUtil ;
	var uiDropDownAnchor = DOMUtil.findAncestorByClass(obj, 'UIDropDownAnchor') ;
	var uiDropDownTitle = DOMUtil.findPreviousElementByTagName(uiDropDownAnchor, 'div') ;
	var uiDropDownMiddleTitle = DOMUtil.findFirstDescendantByClass(uiDropDownTitle,'div','DropDownSelectLabel') ;
	uiDropDownMiddleTitle.innerHTML = obj.innerHTML ;
	uiDropDownAnchor.style.display = 'none' ;
} ;

eXo.webui.UIDropDownControl = new UIDropDownControl() ;
