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

function UIComponent(node) {
	if(!node) return null;
  this.node = node ;
  this.type = node.className ;
  var DOMUtil = eXo.core.DOMUtil;
  var componentBlock = DOMUtil.findFirstDescendantByClass(node, "div", "UIComponentBlock");
  var children =  DOMUtil.getChildrenByTagName(componentBlock, "div") ;
  
  for(var i=0; i<children.length; i++) {
  	if(DOMUtil.hasClass(children[i], "LAYOUT-BLOCK")) this.layout = children[i];
  	else if(DOMUtil.hasClass(children[i], "VIEW-BLOCK")) this.view = children[i];
  	else if(DOMUtil.hasClass(children[i], "EDITION-BLOCK")) this.control = children[i];
  }
	
  this.component = "";
  
  if(DOMUtil.hasClass(node, "UIPortal")) this.id = node.id.replace("UIPortal-", "");
  else if(DOMUtil.hasClass(node, "UIPortlet")) this.id = node.id.replace("UIPortlet-", "");
  else if(DOMUtil.hasClass(node, "UIContainer")) this.id = node.id.replace("UIContainer-", "");
  else this.id = node.id;
  
};

UIComponent.prototype.getId = function() { return this.id ; };
UIComponent.prototype.getElement = function() { return this.node ; };
UIComponent.prototype.getUIComponentType = function() { return this.type ; };

UIComponent.prototype.getUIComponentBlock = function() { return this.node ; };
UIComponent.prototype.getControlBlock = function() { return this.control ; };
UIComponent.prototype.getLayoutBlock = function() { return this.layout ; };
UIComponent.prototype.getViewBlock = function() { return this.view ; };

/*******************************************************************************/

function UIPortal() {
  this.portalUIComponentDragDrop = false;
};

UIPortal.prototype.blockOnMouseOver = function(event, portlet, isOver) {
  var DOMUtil = eXo.core.DOMUtil;
  if(!eXo.portal.portalMode || eXo.portal.isInDragging) return;
	if(eXo.portal.portalMode <= 2 && DOMUtil.hasClass(portlet, "UIContainer")) return;
	if(eXo.portal.portalMode > 2 && eXo.portal.portalMode != 4 && DOMUtil.hasClass(portlet, "UIPortlet")) return;
	
	if(!event) event = window.event;
	event.cancelBubble = true;
	
  var component = DOMUtil.findFirstDescendantByClass(portlet, "div", "UIComponentBlock");
  var children = DOMUtil.getChildrenByTagName(component, "div");
  var layoutBlock;
  var viewBlock;
  var editBlock;
  
  for(var i=0; i<children.length; i++) {
  	if(DOMUtil.hasClass(children[i], "LAYOUT-BLOCK")) layoutBlock = children[i];
  	else if(DOMUtil.hasClass(children[i], "VIEW-BLOCK")) viewBlock = children[i];
  	else if(DOMUtil.hasClass(children[i], "EDITION-BLOCK")) editBlock = children[i];
  }
  
  if(!editBlock) return;
	if(isOver) {
		var newLayer = DOMUtil.findFirstDescendantByClass(editBlock, "div", "NewLayer");
		var height = 0; var width = 0;
		if(layoutBlock && layoutBlock.style.display != "none") {
			height = layoutBlock.offsetHeight;
			width = layoutBlock.offsetWidth;
		} else if(viewBlock && viewBlock.style.display != "none") {
			height = viewBlock.offsetHeight;
      width = viewBlock.offsetWidth;
		}
		
		if(DOMUtil.hasClass(portlet, "UIPortlet")) {
			newLayer.style.width = width + "px";
			newLayer.style.height = height + "px";
		} else {
			newLayer.parentNode.style.width = width + "px";
			var normalBlock = DOMUtil.findFirstChildByClass(portlet, "div", "NormalContainerBlock");
			if(normalBlock) DOMUtil.replaceClass(normalBlock, "NormalContainerBlock", "OverContainerBlock");
		}
		newLayer.parentNode.style.top = -height + "px";
		editBlock.style.display = "block";		

		//resize width of portlet/container control if IE + LTR align BEGIN

		var uiInfoBar = DOMUtil.findFirstDescendantByClass(editBlock, "div", "UIInfoBar");

		if( uiInfoBar && (eXo.core.Browser.isIE6() || (eXo.core.Browser.isIE7() && eXo.core.I18n.isRT()))){
			//resize width of portlet/container only one time
			if(uiInfoBar.style.width == ""){
				var dragControlArea = DOMUtil.findFirstDescendantByClass(uiInfoBar, "div", "DragControlArea");
				
				var portletIcon = DOMUtil.findFirstDescendantByClass(uiInfoBar, "div", "PortletIcon");
				var editPortletPropertiesIcon = DOMUtil.findFirstDescendantByClass(uiInfoBar, "a", "EditPortletPropertiesIcon");
				var deletePortletIcon = DOMUtil.findFirstDescendantByClass(uiInfoBar, "a", "DeletePortletIcon");
				
				var contarnerIcon = DOMUtil.findFirstDescendantByClass(uiInfoBar, "div", "ContainerIcon");
				var editContainerIcon = DOMUtil.findFirstDescendantByClass(uiInfoBar, "a", "EditContainerIcon");
				var deleteContainerIcon = DOMUtil.findFirstDescendantByClass(uiInfoBar, "a", "DeleteContainerIcon");
				
				var uiInfoBarWidth =  dragControlArea.offsetWidth;
				
				if(DOMUtil.hasClass(portlet, "UIPortlet")){
					uiInfoBarWidth += portletIcon.offsetWidth;
					
					if(editPortletPropertiesIcon){
						uiInfoBarWidth += editPortletPropertiesIcon.offsetWidth;
					}
					
					if(deletePortletIcon){
						uiInfoBarWidth += deletePortletIcon.offsetWidth;
					}
				}
				
				if(DOMUtil.hasClass(portlet, "UIContainer")){
					uiInfoBarWidth += contarnerIcon.offsetWidth
					
					if(editContainerIcon){
						uiInfoBarWidth += editContainerIcon.offsetWidth;
					}
					
					if(deleteContainerIcon){
						uiInfoBarWidth += deleteContainerIcon.offsetWidth;
					}
				}
	
				uiInfoBar.style.width= uiInfoBarWidth + 35 + "px";
			}

		}
		//resize width of portlet/container control if IE + LTR align END
		
	}	else {
		editBlock.style.display = "none";
		if(!DOMUtil.hasClass(portlet, "UIPortlet")) {
			var normalBlock = DOMUtil.findFirstChildByClass(portlet, "div", "OverContainerBlock");
			if(normalBlock) DOMUtil.replaceClass(normalBlock, "OverContainerBlock", "NormalContainerBlock");
		}
	}
	
	// Don't display portlet control when View Container
	var controlPortlet =	DOMUtil.findFirstDescendantByClass(editBlock, "div", "CONTROL-PORTLET");
	if (controlPortlet) {
		controlPortlet.style.display = eXo.portal.portalMode == 4 ? "none" : "block";
	}
};
/**
 * Get all UIPortlets of current UIWorkingWorkspace
 * @return {Array} Array of UIComponents
 */
UIPortal.prototype.getUIPortlets = function() {
  var uiWorkingWorkspace = document.getElementById("UIWorkingWorkspace") ;
  var founds =  eXo.core.DOMUtil.findDescendantsByClass(uiWorkingWorkspace, "div", "UIPortlet") ;
  var components =  new Array() ;
  for(j = 0; j < founds.length; j++) {
    components[components.length] = new UIComponent(founds[j]) ;
  }
  return components ;
} ;
/**
 * Get all UIPortlets is children of UIWorkingWorkspace
 * @return {Array} Array of UIComponents
 */
UIPortal.prototype.getUIPortletsInUIPortal = function() {
  var uiWorkingWorkspace = document.getElementById("UIWorkingWorkspace") ;
  var founds =  eXo.core.DOMUtil.findDescendantsByClass(uiWorkingWorkspace, "div", "UIPortlet") ;
  var components =  new Array() ;
  for(var j = 0; j < founds.length; j++) {
    if(eXo.core.DOMUtil.findAncestorByClass(founds[j], 'UIPage') == null) {
      components[components.length] = new UIComponent(founds[j]) ;
    }
  }
  return components ;
} ;
/**
 * Get all UIPortlets in UIPage
 * @return {Array} components array of UIComponent objects
 */
UIPortal.prototype.getUIPortletsInUIPage = function() {
  var uiPage = document.getElementById("UIPage") ;
  var founds =  eXo.core.DOMUtil.findDescendantsByClass(uiPage, "div", "UIPortlet");
  var components =  new Array() ;
  for(var j = 0; j < founds.length; j++) {
    components[components.length] = new UIComponent(founds[j]) ;
  }
  return components ;
} ;
/**
 * Get All UIContainers in current UIWorkingWorkspace
 * @return {Array} components array of UIComponent objects
 */
UIPortal.prototype.getUIContainers = function() {
  var uiWorkingWorkspace = document.getElementById("UIWorkingWorkspace") ;
  var  founds = eXo.core.DOMUtil.findDescendantsByClass(uiWorkingWorkspace, "div", "UIContainer");
  var components =  new Array() ;
  for(var j = 0; j < founds.length; j++) {
    components[j] = new UIComponent(founds[j]) ;
  }
  return components ;
};
/**
 * Get current UIPageBody
 * @return {Object} UIPageBody object of this document
 */
UIPortal.prototype.getUIPageBody = function() {
//  var uiPortal = document.getElementById("UIPortal") ;
//  return new UIComponent(eXo.core.DOMUtil.findFirstDescendantByClass(uiPortal, "div", "UIPage")) ;
	return new UIComponent(document.getElementById("UIPageBody")) ;
};
/**
 * Get current UIPortal
 * @return {Object} UIComponent object that contains UIPortal object of this component
 */
UIPortal.prototype.getUIPortal = function() {
  var uiWorkingWorkspace = document.getElementById("UIWorkingWorkspace") ;
  return new UIComponent(eXo.core.DOMUtil.findFirstDescendantByClass(uiWorkingWorkspace, "div", "UIPortal"));
};

 /**Repaired: by Vu Duy Tu 25/04/07**/
UIPortal.prototype.showLayoutModeForPage = function() {
	var uiPage = eXo.core.DOMUtil.findFirstDescendantByClass(document.body, "div", "UIPage") ;
	if(uiPage == null) return;
	var viewPage = eXo.core.DOMUtil.findFirstDescendantByClass(uiPage, "div", "VIEW-PAGE") ;
	var uiPortalApplication = document.getElementById("UIPortalApplication");
	if(uiPortalApplication.className != "Vista") {
	 viewPage.style.border = "solid 3px #dadada" ;
	}
	
	viewPage.style.paddingTop = "50px" ;
	viewPage.style.paddingRight = "0px";
	viewPage.style.paddingBottom = "50px";
	viewPage.style.paddingLeft = "0px";

    var uiContainer = eXo.core.DOMUtil.findFirstDescendantByClass(viewPage, "div", "UIContainer") ;
    var uiPortlet = eXo.core.DOMUtil.findFirstDescendantByClass(viewPage, "div", "UIPortlet") ;
    if(uiContainer != null || uiPortlet != null) {
      viewPage.style.border = "none" ;
      viewPage.style.paddingTop = "5px" ;
      viewPage.style.paddingRight = "5px";
      viewPage.style.paddingBottom = "5px";
      viewPage.style.paddingLeft = "5px";
    }
};

UIPortal.prototype.showViewMode = function() {  
	var pageBody = this.getUIPageBody() ;
	var container = this.getUIContainers() ;
	var portlet = this.getUIPortlets() ;

	if(container.length == 0 && portlet.length == 0) {
  		var pageIdElemt = document.getElementById("UIPage");
  		var viewPage = eXo.core.DOMUtil.findAncestorByClass(pageIdElemt, "VIEW-PAGE");
  		viewPage.style.paddingTop = "50px" ;
		viewPage.style.paddingRight = "0px";
		viewPage.style.paddingBottom = "50px";
		viewPage.style.paddingLeft = "0px";
  	}
  	var pageBodyBlock = pageBody.getUIComponentBlock();
  	var mask = eXo.core.DOMUtil.findFirstDescendantByClass(pageBodyBlock, "div", "UIPageBodyMask");
  	if(mask) {
  		mask.style.top = - pageBodyBlock.offsetHeight + "px";
		mask.style.height = pageBodyBlock.offsetHeight + "px";
		mask.style.width = pageBodyBlock.offsetWidth + "px";
  	}
};

/**
 * Return the closest container of the element.
 * It might be one of these : UIPortlet, UIContainer, UIPageBody, UIPortal
 */
UIPortal.prototype.findUIComponentOf = function(element) {
  var DOMUtil = eXo.core.DOMUtil;
  var parent;
  if (parent = DOMUtil.findAncestorByClass(element, "UIPortlet")) {
    return parent;
  } else if (parent = DOMUtil.findAncestorByClass(element, "UIPageBody")) {
     return parent;
  } else if (parent = DOMUtil.findAncestorByClass(element, "UIContainer")) {
    return parent;
  } else if (parent = DOMUtil.findAncestorByClass(element, "UIPortal")) {
    return parent;
  }
  
  return null ;
};

/**
 * Change skin of Portal
 * @param url
 */
UIPortal.prototype.changeSkin = function(url) {
 var skin = '';
 if(eXo.webui.UIItemSelector.SelectedItem != undefined) {
   skin = eXo.webui.UIItemSelector.SelectedItem.option;
 }
 if(skin == undefined) skin = '';
  //ajaxAsyncGetRequest(url + '&skin='+skin, false);
  window.location = url + '&skin='+skin;
} ;
/**
 * Change language of Portal
 * @param url
 */
UIPortal.prototype.changeLanguage = function(url) {
	var language = '';
	if(eXo.webui.UIItemSelector.SelectedItem != undefined) {
  	language = eXo.webui.UIItemSelector.SelectedItem.option;
	}
	if(language == undefined) language = '';  
  //ajaxAsyncGetRequest(url + '&language='+language, false);
  window.location = url + '&language='+language;
} ;
/**
 * Change current portal
 */
UIPortal.prototype.changePortal = function(accessPath, portal) {
  window.location = eXo.env.server.context + "/" + accessPath + "/" + portal+"/";
} ;

/** Created: by Lxchiati **/
UIPortal.prototype.popupButton = function(url, action) {
	if(action == undefined) action = '';  
  window.location = url + '&action='+ action ;
} ;
/**
 * Remove a component of portal
 * @param {String} componentId identifier of component
 */
UIPortal.prototype.removeComponent = function(componentId) {
		var comp = document.getElementById(componentId);
		var viewPage = eXo.core.DOMUtil.findAncestorByClass(comp, "VIEW-PAGE");

		var parent = comp.parentNode;
		if (eXo.core.DOMUtil.getChildrenByTagName(parent, "div").length === 1 && !eXo.core.DOMUtil.hasClass(parent, "EmptyContainer")) {
			eXo.core.DOMUtil.addClass(parent, "EmptyContainer");
		}

		//Check if the removing component is a column
		if (comp.parentNode.nodeName.toUpperCase() == "TD") eXo.core.DOMUtil.removeElement(comp.parentNode);
		else eXo.core.DOMUtil.removeElement(comp);
		
		if(viewPage && eXo.portal.UIPortal.getUIContainers().length == 0 
				&& eXo.portal.UIPortal.getUIPortlets().length == 0) {
			viewPage.style.paddingTop = "50px" ;
			viewPage.style.paddingRight = "0px";
			viewPage.style.paddingBottom = "50px";
			viewPage.style.paddingLeft = "0px";
		}
};

/**
 * Change Save button to editing state
 */
UIPortal.prototype.changeComposerSaveButton = function() {
	if(eXo.portal.hasEditted == false) {
		var uiWorkingWS = document.getElementById("UIWorkingWorkspace");
		var portalComposer = eXo.core.DOMUtil.findFirstDescendantByClass(uiWorkingWS, "div", "UIPortalComposer");
		var saveButton = eXo.core.DOMUtil.findFirstDescendantByClass(portalComposer, "a", "SaveButton");
		if(saveButton) eXo.core.DOMUtil.replaceClass(saveButton, "SaveButton", "EdittedSaveButton");
		ajaxAsyncGetRequest(eXo.env.server.createPortalURL(portalComposer.id, "ChangeEdittedState", true));
  }
};

UIPortal.prototype.toggleComposer = function(clickedEle) {
	var portalComposer = eXo.core.DOMUtil.findAncestorByClass(clickedEle, "UIPortalComposer");
	var content = eXo.core.DOMUtil.findFirstChildByClass(portalComposer, "div", "UIWindowContent");
	if(content && content.style.display != "none") {
	content.style.display = "none";
	eXo.core.DOMUtil.replaceClass(clickedEle, "ExpandIcon", "CollapseIcon");
	} else {
	content.style.display = "block";
	eXo.core.DOMUtil.replaceClass(clickedEle, "CollapseIcon", "ExpandIcon");
	}
	var requestStr = eXo.env.server.createPortalURL(portalComposer.id, "Toggle", true);
	ajaxAsyncGetRequest(requestStr);
};

UIPortal.prototype.composerTabChanged = function(tabId) {
	var toolPanel = document.getElementById("UIPortalToolPanel");
	if (!tabId || !toolPanel) return;
	
	var removeCls, addCls;
	if (tabId === "UIApplicationList") {
		addCls = "ApplicationMode";
		removeCls = "ContainerMode";
	} else {
		addCls = "ContainerMode"; 
		removeCls = "ApplicationMode";
	}
	eXo.core.DOMUtil.removeClass(toolPanel, removeCls);
	eXo.core.DOMUtil.addClass(toolPanel, addCls);
}

/**
 * Clollapse or expand an element (all its children) of tree
 * @param {Object} element object to collapse or expand
 */
UIPortal.prototype.collapseExpand = function(element) {
	var subGroup = eXo.core.DOMUtil.findFirstChildByClass(element.parentNode, "div", "ChildrenContainer") ;
	var className = element.className;
	if(!subGroup) return;
	if(subGroup.style.display == "none") {
		if (className.indexOf("ExpandIcon") == 0) element.className = "CollapseIcon ClearFix" ;
		subGroup.style.display = "block" ;
	} else {
		if (className.indexOf("CollapseIcon") == 0) element.className = "ExpandIcon ClearFix" ;
		subGroup.style.display = "none" ;
	}
};

eXo.portal.UIPortalComponent = UIComponent.prototype.constructor ;
eXo.portal.UIPortal = new UIPortal() ;
eXo.portal.UIComponent = UIPortal.prototype.constructor ;
