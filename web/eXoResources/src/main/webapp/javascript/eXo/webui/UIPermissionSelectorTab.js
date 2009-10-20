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

function UIPermissionSelectorTab() {
  
};

UIPermissionSelectorTab.prototype.init = function() {
  
};

UIPermissionSelectorTab.prototype.displayBlockContent = function(clickedEle) {
	var permissionTypeBar = eXo.core.DOMUtil.findAncestorByClass(clickedEle, "PermissionTypeBar") ;
	var permissionButton = eXo.core.DOMUtil.findChildrenByClass(permissionTypeBar, "div", "PermissionButton") ;
	var selectedPermissionInfo = eXo.core.DOMUtil.findChildrenByClass(permissionTypeBar.parentNode, "div", "SelectedPermissionInfo") ;

	for(var i = 0; i < permissionButton.length; i++) {
		if(permissionButton[i] == clickedEle) {
			permissionButton[i].style.fontWeight = "bold" ;
			selectedPermissionInfo[i].style.display = "block" ;
		} else {
			permissionButton[i].style.fontWeight = "100" ;
			selectedPermissionInfo[i].style.display = "none" ;
		}
	}
};

eXo.webui.UIPermissionSelectorTab = new UIPermissionSelectorTab() ;