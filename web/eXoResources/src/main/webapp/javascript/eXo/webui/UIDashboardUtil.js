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

function UIDashboardUtil() {

	UIDashboardUtil.prototype.findPosX = function(obj) {
	  var curleft = 0, browser = eXo.core.Browser;
	  var uiWorkspaceContainer = document.getElementById("UIWorkspaceContainer");
	  var uiWorkingWorkspace = document.getElementById("UIWorkingWorkspace");	  
	  while (obj) {
	  	if(uiWorkspaceContainer!=null && uiWorkspaceContainer.style.display!="none"
	  					 && browser.getBrowserType()=="ie"){
	  		var uiPageDesktop = document.getElementById("UIPageDesktop");
	  		if( (uiPageDesktop!=null && eXo.core.DOMUtil.hasClass(obj,"UIPageDesktop") && browser.isIE7()) 
	  					|| (uiPageDesktop==null && eXo.core.DOMUtil.hasClass(obj,"PORTLET-FRAGMENT")) ){
	  			curleft += (obj.offsetLeft - uiWorkingWorkspace.offsetLeft);
	  			obj = obj.offsetParent ;
	  			continue;
	  		}
	  	}
  		curleft += obj.offsetLeft ;
	    obj = obj.offsetParent ;
	  }
	  return curleft ;
	};
	
	UIDashboardUtil.prototype.isIn = function(x, y, component) {
	  var componentLeft = eXo.webui.UIDashboardUtil.findPosX(component);
	  var componentRight = componentLeft + component.offsetWidth ;
	  var componentTop = eXo.core.Browser.findPosY(component) ;
	  var componentBottom = componentTop + component.offsetHeight ;
	  var isOver = false ;

	  if((componentLeft < x) && (x < componentRight)) {
	    if((componentTop < y) && (y < componentBottom)) {
	      isOver = true ;
	    }
	  }
	  
	  return isOver ;
	};
	
	UIDashboardUtil.prototype.findColIndexInDashboard = function(dragObj){
		var col = dragObj.parentNode;
		if(col==null) return null;
		var dashboardContainer = eXo.core.DOMUtil.findAncestorByClass(col, "DashboardContainer");
		var columns = eXo.core.DOMUtil.findDescendantsByClass(dashboardContainer, "div", "UIColumn");
		for(var i=0; i<columns.length; i++){
			if(col.id == columns[i].id){
				return i;
			}
		}
	};
	
	UIDashboardUtil.prototype.findRowIndexInDashboard = function(dragObj){
		var modules = eXo.core.DOMUtil.getChildrenByTagName(dragObj.parentNode, "div");
		for(var i=0; i<modules.length; i++){
			if(modules[i].id == dragObj.id) return i;
		}
	};
	
	UIDashboardUtil.prototype.createRequest = function(componentId, action, params){
		var url = eXo.env.server.portalBaseURL;
		url += '?portal:componentId=' + componentId +
							'&portal:type=action&uicomponent=UIDashboard&op=' + action ;
		url += '&ajaxRequest=true';
		if(params != null) {
	  	var len = params.length ;
	    for(var i = 0 ; i < len ; i++) {
	      url += "&" +  params[i].name + "=" + params[i].value ;
	    }
	  }
		return url;
	};
};

eXo.webui.UIDashboardUtil = new UIDashboardUtil();