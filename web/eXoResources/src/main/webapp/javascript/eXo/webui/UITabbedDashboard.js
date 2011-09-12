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

eXo.webui.UITabbedDashboard = {
	
	init : function(){eXo.webui.UITabbedDashboard.isInRequest = false;},
	
	renameTabLabel : function(e){
		if(!e){
			e = window.event;
		}
		var keyNum = e.keyCode;
		
		//If user presses on ENTER button, then rename the tab label
		if(keyNum == 13){
			var inputElement = eXo.core.Browser.getEventSource(e);
			var newTabLabel = inputElement.value;
			if(!newTabLabel || newTabLabel.length < 1){
				return;
			}
			var DOMUtil = eXo.core.DOMUtil;
			var portletFrag = DOMUtil.findAncestorByClass(inputElement, "PORTLET-FRAGMENT");
			var compId = portletFrag.parentNode.id;
			var nodeName = inputElement.id;
			
			//Send request to server to change node name
			var href = eXo.env.server.portalBaseURL + "?portal:componentId=" + compId;
			href += "&portal:type=action";
			href += "&portal:isSecure=false";
			href += "&uicomponent=UITabPaneDashboard";
			href += "&op=RenameTabLabel";
			href += "&objectId=" + nodeName;
			href += "&newTabLabel=" + encodeURIComponent(newTabLabel);
			window.location = href;
		}
		//If user presses on the ESCAPE key, then reset the span element on the tab
		else if(keyNum == 27){
			var inputElement = eXo.core.Browser.getEventSource(e);
			if(eXo.webui.UITabbedDashboard.backupElement) {
 				inputElement.parentNode.replaceChild(eXo.webui.UITabbedDashboard.backupElement, inputElement);
 				eXo.webui.UITabbedDashboard.backupElement = null;
			}
		}
	},

	showEditLabelInput : function(selectedElement, nodeName, currentContent){
		eXo.webui.UITabbedDashboard.backupElement = selectedElement;
		var prNode = selectedElement.parentNode;
		
		var inputElement = document.createElement("input");
		inputElement.type = "text";
		inputElement.id = nodeName;
		inputElement.name = currentContent; // To store old value
		inputElement.value = currentContent;
		inputElement.style.border = "1px solid #b7b7b7";
		inputElement.style.width = (selectedElement.offsetWidth - 2 ) + "px";
		inputElement.onkeypress = eXo.webui.UITabbedDashboard.renameTabLabel;
		inputElement.setAttribute('maxLength', 50);
		inputElement.onblur = function() {
			prNode.replaceChild(eXo.webui.UITabbedDashboard.backupElement, inputElement);
		};
		
		prNode.replaceChild(inputElement, selectedElement);
		inputElement.focus();
		
		var DOMUtil = eXo.core.DOMUtil;
		var uiTab = DOMUtil.findAncestorByClass(inputElement, "UITab");
		DOMUtil.addClass(uiTab, "EditTab");	
	},
	
	createDashboardPage : function(e){
		if(!e){
			e = window.event;
		}	
		if(eXo.webui.UITabbedDashboard.isInRequest) return;
		var keyNum = e.keyCode;
		
		//If user presses on ENTER button
		if(keyNum == 13){
			var inputElement = eXo.core.Browser.getEventSource(e);
			var newTabLabel = inputElement.value;
			if(!newTabLabel || newTabLabel.length < 1) return;
			
			//Send request to server to change node name
			var href = eXo.env.server.portalBaseURL + "?portal:componentId=" + inputElement.id;
			href += "&portal:type=action";
			href += "&portal:isSecure=false";
			href += "&uicomponent=UITabPaneDashboard";
			href += "&op=AddDashboard";
			href += "&objectId=" + encodeURIComponent(newTabLabel);
			eXo.webui.UITabbedDashboard.isInRequest = true;
			window.location = href;
		}
		//If user presses on ESCAPE button
		else if(keyNum == 27){
			var inputElement = eXo.core.Browser.getEventSource(e);
			var editingTabElement = eXo.core.DOMUtil.findAncestorByClass(inputElement, "UITab");
			
			//Remove the editing tab
			editingTabElement.parentNode.removeChild(editingTabElement);
		}
	},
	
	cancelTabDashboard : function(e){
		if(!e){
			e = window.event;
		}
		var inputElement = eXo.core.Browser.getEventSource(e);
		var editingTabElement = eXo.core.DOMUtil.findAncestorByClass(inputElement, "UITab");
		
		//Remove the editing tab
		editingTabElement.parentNode.removeChild(editingTabElement);
	},
	
	createTabDashboard : function(addTabElement){
		var DOMUtil = eXo.core.DOMUtil;
		var tabContainer = addTabElement.parentNode;
		var tabElements = DOMUtil.findChildrenByClass(tabContainer, "div", "UITab");
		var portletFrag = DOMUtil.findAncestorByClass(tabContainer, "PORTLET-FRAGMENT");
		var selectedTabElement = DOMUtil.findFirstDescendantByClass(tabContainer, "div", "SelectedTab");
		
		var newTabElement = selectedTabElement.cloneNode(true);
		tabContainer.insertBefore(newTabElement, addTabElement);
		
		var inputElement = document.createElement("input");
		inputElement.type = "text";
		inputElement.value = "Tab_" + tabElements.length;
		inputElement.style.border = "1px solid #b7b7b7";
		inputElement.style.width = "80px";
		inputElement.onkeypress = eXo.webui.UITabbedDashboard.createDashboardPage;
		inputElement.onblur = eXo.webui.UITabbedDashboard.cancelTabDashboard;
		inputElement.setAttribute('maxLength', 50);
		inputElement.id = portletFrag.parentNode.id; //Store the id of the portlet here
		
		var spanElement = DOMUtil.findDescendantsByTagName(newTabElement, "span")[0];
		spanElement.parentNode.replaceChild(inputElement, spanElement);
		
		DOMUtil.findNextElementByTagName(inputElement, "a").href = "#";
		inputElement.focus();	
		
		var DOMUtil = eXo.core.DOMUtil;
		var uiTab = DOMUtil.findAncestorByClass(inputElement, "UITab");
		DOMUtil.addClass(uiTab, "EditTab");	
	}
}