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

function UIPortlet() {
	this.maxIndex = 0;
} ;
/**
 * Event when mouse focuses to element, this function is called when user minimizes or
 * maximized portlet window
 * @param {Object} element focusing element
 * @param {boolean} isOver know as mouse over or out
 */
UIPortlet.prototype.onControlOver = function(element, isOver) {
  var originalElementName = element.className ;
  if(isOver) {
    var overElementName = "ControlIcon Over" + originalElementName.substr(originalElementName.indexOf(" ") + 1, 30) ;
    element.className   = overElementName;
   	if(element.className == "ControlIcon OverRestoreIcon"){ 
   		var hiddenAttribute = eval('(' + eXo.core.DOMUtil.findFirstChildByClass(element, "div", "").innerHTML + ')');
   		element.title = hiddenAttribute.modeTitle ;
   	}
    if(element.className == "ControlIcon OverMaximizedIcon"){ 
    	var hiddenAttribute = eval('(' + eXo.core.DOMUtil.findFirstChildByClass(element, "div", "").innerHTML + ')');
    	element.title = hiddenAttribute.normalTitle ;
    }
  } else {
    var over = originalElementName.indexOf("Over") ;
    if(over >= 0) {
      var overElementName = "ControlIcon " + originalElementName.substr(originalElementName.indexOf(" ") + 5, 30) ;
      element.className   = overElementName ;
    }
  }
} ;

eXo.webui.UIPortlet = new UIPortlet() ;
