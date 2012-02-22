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

eXo.webui.UIDashboardUtil = {

	findPosX : function(obj) {
	  var curleft = 0, browser = eXo.core.Browser;
	  var uiWorkspaceContainer = document.getElementById("UIWorkspaceContainer");
	  var uiWorkingWorkspace = document.getElementById("UIWorkingWorkspace");	  
	  while (obj) {
	  	if(uiWorkspaceContainer!=null && uiWorkspaceContainer.style.display!="none"
	  					 && browser.getBrowserType()=="ie"){
	  		var uiPageDesktop = document.getElementById("UIPageDesktop");
        var jqObj = xj(obj);
	  		if( (uiPageDesktop!=null && jqObj.hasClass("UIPageDesktop") && browser.isIE7())
	  					|| (uiPageDesktop==null && jqObj.hasClass("PORTLET-FRAGMENT")) ){
	  			curleft += (obj.offsetLeft - uiWorkingWorkspace.offsetLeft);
	  			obj = obj.offsetParent ;
	  			continue;
	  		}
	  	}
  		curleft += obj.offsetLeft ;
	    obj = obj.offsetParent ;
	  }
	  return curleft ;
	},
	
	isIn : function(x, y, component) {
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
	},

  isInColumn : function(column, x, scrollLeft)
  {
    var left = this.findPosX(column[0]) - scrollLeft;
    return left <= x && x < left + column[0].offsetWidth;
  },
	
	findColIndexInDashboard : function(dragObj){
    var index = 0;
    xj(dragObj).parent().prevAll("div.UIColumn").each(function()
    {
      index++;
    });
    return index;
	},

  findContainingColumn : function(gadgetCont, x)
  {
    var column;
    var scrollLeft = gadgetCont.scrollLeft();
    gadgetCont.find("div.UIColumn").each(function()
    {
      var left = eXo.webui.UIDashboardUtil.findPosX(this) - scrollLeft;
      if(left < x && x < left + this.offsetWidth)
      {
        column = xj(this);
        return false;
      }
    });

    return column;
  },
	
	findRowIndexInDashboard : function(dragObj){
    var row = 0;
    xj(dragObj).prevAll("div").each(function()
    {
      if(this.id == dragObj.id)
      {
        return false;
      }
      else
      {
        row++;
      }
    });

    return row;
	},

	createRequest : function(componentId, action, params){
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
	}
};