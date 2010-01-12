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
 * This class manages the drag and drop of components on the page.
 * It uses a DradDrop object to manage the events, sets some callback functions
 * and some parameters to initialize the DragDrop object.
 */
function PortalDragDrop() { 
	this.count = 0 ;
} ;

/**
 * This function inits the PortalDragDrop object
 * It initializes a DragDrop object that will manage the drag events
 */

PortalDragDrop.prototype.init = function(e) {
	if (!e) e = window.event;
	if(((e.which) && (e.which == 2 || e.which == 3)) || ((e.button) && (e.button == 2)))	return;
	
	var DOMUtil = eXo.core.DOMUtil ;
	var Browser = eXo.core.Browser ;
  var DragDrop = eXo.core.DragDrop ;
  var Mouse = eXo.core.Mouse;
  
  var previewBlock = null;
  var previewTD = null;
	/**
	 * This function is called after the DragDrop object is initialized
	 */
  DragDrop.initCallback = function (dndEvent) {
  	var PortalDragDrop = eXo.portal.PortalDragDrop ;
    this.origDragObjectStyle = new eXo.core.HashMap() ;
    var dragObject = dndEvent.dragObject ;
    var properties = ["top", eXo.core.I18n.isLT() ? "left" : "right", "zIndex", "opacity", "filter", "position"] ;
    this.origDragObjectStyle.copyProperties(properties, dragObject.style) ;
    
    var isComponent = !!DOMUtil.findFirstDescendantByClass(dragObject, "div", "UIComponentBlock");
   	
   	var uiWorkingWS = document.getElementById("UIWorkingWorkspace");
		PortalDragDrop.positionRootObj = isComponent ? uiWorkingWS : 
					DOMUtil.findFirstDescendantByClass(uiWorkingWS, "div", "UIPortalComposer");
		
		var originalDragObjectTop = Browser.findPosYInContainer(dragObject, PortalDragDrop.positionRootObj);
		var originalDragObjectLeft = Browser.findPosXInContainer(dragObject, PortalDragDrop.positionRootObj);
		PortalDragDrop.deltaYDragObjectAndMouse = Browser.findMouseRelativeY(dragObject, e);
		PortalDragDrop.deltaXDragObjectAndMouse = Browser.findMouseRelativeX(dragObject, e);
    if(!isComponent) {
      var contentContainer = DOMUtil.findAncestorByClass(dragObject, "PopupContent");
      originalDragObjectTop -= contentContainer.scrollTop;
      PortalDragDrop.deltaYDragObjectAndMouse += contentContainer.scrollTop;
    } 
		
    PortalDragDrop.parentDragObject = dragObject.parentNode ;
    PortalDragDrop.backupDragObjectWidth = dragObject.offsetWidth ;
        
    /*Case: dragObject out of UIPortal*/
    if(!isComponent) {
      var cloneObject = dragObject.cloneNode(true) ;
      dragObject.parentNode.insertBefore(cloneObject, dragObject) ;
      cloneObject.style.opacity = 0.5 ;
      cloneObject.style.filter = "alpha(opacity=50)" ;
      cloneObject.style.width = PortalDragDrop.backupDragObjectWidth + "px" ;
      dndEvent.dragObject = cloneObject ;
    } else {
    	previewBlock = PortalDragDrop.createPreview();
    	dragObject.parentNode.insertBefore(previewBlock, dragObject);
    	dragObject.style.width = "300px";
    	var componentBlock = eXo.core.DOMUtil.findFirstDescendantByClass(dragObject, "div", "UIComponentBlock") ;
    	var editBlock = eXo.core.DOMUtil.findFirstChildByClass(componentBlock, "div", "EDITION-BLOCK");
	    if(editBlock) {
	    	var newLayer = eXo.core.DOMUtil.findFirstDescendantByClass(editBlock, "div", "NewLayer");
	    	if(newLayer) newLayer.style.width = "300px";
	    }
    }
    dragObject.isComponent = isComponent;
    
    dragObject = dndEvent.dragObject;
    dragObject.style.position = "absolute" ;
    if(eXo.core.I18n.isLT()) dragObject.style.left = originalDragObjectLeft + "px" ;
    else dragObject.style.right = (PortalDragDrop.positionRootObj.offsetWidth - originalDragObjectLeft - dragObject.offsetWidth) + "px" ;
    dragObject.style.top = originalDragObjectTop + "px" ;
    
    eXo.portal.isInDragging = true;
  }
  
  DragDrop.dragCallback = function(dndEvent) {
    var dragObject = dndEvent.dragObject ;
    /* Control Scroll */
    eXo.portal.PortalDragDrop.scrollOnDrag(dndEvent) ;
    if(!dndEvent.foundTargetObject) return;
    var uiComponentLayout ;
    if(dndEvent.foundTargetObject.className == "UIPage") {
			uiComponentLayout = DOMUtil.findFirstDescendantByClass(dndEvent.foundTargetObject, "div", "VIEW-PAGE");
    } else if(dndEvent.foundTargetObject.className == "UIPortal") {
      if(eXo.portal.portalMode%2) uiComponentLayout = DOMUtil.findFirstDescendantByClass(dndEvent.foundTargetObject, "div", "LAYOUT-PORTAL") ;
      else uiComponentLayout = DOMUtil.findFirstDescendantByClass(dndEvent.foundTargetObject, "div", "VIEW-PORTAL");
    } else {
      var foundUIComponent = new eXo.portal.UIPortalComponent(dndEvent.foundTargetObject) ;
      if(eXo.portal.portalMode%2) uiComponentLayout = foundUIComponent.getLayoutBlock() ;
      else uiComponentLayout = foundUIComponent.getViewBlock();
      uiComponentLayout.style.height = "auto";
    }
    
    dndEvent.foundTargetObject.uiComponentLayoutType = uiComponentLayout ;
    var componentIdElement = DOMUtil.getChildrenByTagName(uiComponentLayout, "div")[0] ;
    var layoutTypeElement = DOMUtil.getChildrenByTagName(componentIdElement, "div")[0] ;
    eXo.portal.PortalDragDrop.layoutTypeElementNode = layoutTypeElement ;
    
    if(previewBlock == null) previewBlock = eXo.portal.PortalDragDrop.createPreview();
    if(previewTD || document.getElementById("PreviewTDBlock")) {
    	if(!previewTD) previewTD = document.getElementById("PreviewTDBlock");
    	if(previewTD.parentNode) previewTD.parentNode.removeChild(previewTD);
    	previewTD = null;
    }
    
    if(layoutTypeElement != null && !DOMUtil.hasClass(layoutTypeElement, "UITableColumnContainer")) {
      /* ===============================CASE ROW LAYOUT================================ */
      var rowContainer = DOMUtil.findFirstDescendantByClass(uiComponentLayout, "div", "UIRowContainer") ;
      var childRowContainer = DOMUtil.getChildrenByTagName(rowContainer, "div") ;
      
      var listComponent = new Array() ;
      for(var i = 0; i < childRowContainer.length; i++) {
        if((childRowContainer[i].className != "DragAndDropPreview") && (childRowContainer[i] != dragObject)) {
          listComponent.push(childRowContainer[i]) ;
        }
      }
      
      dndEvent.foundTargetObject.listComponentInTarget = listComponent ;
      var insertPosition = eXo.portal.PortalDragDrop.findInsertPosition(listComponent, dragObject, "row") ;
      dndEvent.foundTargetObject.foundIndex = insertPosition ;
      
      /* Insert preview block */
      if(insertPosition >= 0) {
        rowContainer.insertBefore(previewBlock, listComponent[insertPosition]) ;
      } else {
        rowContainer.appendChild(previewBlock) ;
      }
    } else {
      /* ===============================CASE COLUMN LAYOUT================================ */
    	var columnContainer = DOMUtil.findFirstDescendantByClass(uiComponentLayout, "table", "UITableColumnContainer") ;
      var trContainer = DOMUtil.findFirstDescendantByClass(uiComponentLayout, "tr", "TRContainer") ;
      var tdElementList = DOMUtil.getChildrenByTagName(trContainer, "td") ;
      
      var listComponent = new Array() ;
      for(var i = 0; i < tdElementList.length; i++) {
        if(DOMUtil.hasAncestor(previewBlock, uiComponentLayout)) {
        	var td = tdElementList[i];
          if((td != previewBlock.parentNode) && (td != dragObject.parentNode)) {
            listComponent.push(td) ;
          } else if(td == dragObject.parentNode) {
          	td.style.width = "0px";
          }
        } else {
          listComponent.push(tdElementList[i]) ;
        }          
      }
      
      dndEvent.foundTargetObject.listComponentInTarget = listComponent ;
      var insertPosition = eXo.portal.PortalDragDrop.findInsertPosition(listComponent, dragObject, "column") ;
      dndEvent.foundTargetObject.foundIndex = insertPosition ;
      
      /* Insert preview block */
      var tdBlock = document.createElement("td");
      tdBlock.id = tdBlock.className = "PreviewTDBlock";
      tdBlock.appendChild(previewBlock);
      previewTD = tdBlock;
      if(insertPosition >= 0) {
        trContainer.insertBefore(previewTD, listComponent[insertPosition]) ;
      } else {
        trContainer.appendChild(previewTD) ;
      }
    }
  } ;

  DragDrop.dropCallback = function(dndEvent) {
  	this.origDragObjectStyle.setProperties(dndEvent.dragObject.style, false) ;

    if(dndEvent.foundTargetObject != null || (dndEvent.backupMouseEvent && dndEvent.backupMouseEvent.keyCode != 27)) {
      eXo.portal.PortalDragDrop.doDropCallback(dndEvent) ;
    } else {
      if(!dndEvent.dragObject.isComponent) {
				dndEvent.dragObject.parentNode.removeChild(dndEvent.dragObject) ;
			}
			if(dndEvent.dragObject.parentNode.tagName.toLowerCase() == "td") {
				dndEvent.dragObject.parentNode.style.width = "auto";
			}
			// fix bug WEBOS-196	
			dndEvent.dragObject.style.width = "auto" ; 
    }
    
    if(dndEvent.dragObject.isComponent) {
			var componentBlock = eXo.core.DOMUtil.findFirstDescendantByClass(dndEvent.dragObject, "div", "UIComponentBlock") ;
	  	var editBlock = eXo.core.DOMUtil.findFirstChildByClass(componentBlock, "div", "EDITION-BLOCK");
	    if(editBlock) editBlock.style.display = "none";
    }
    
    if(previewBlock) previewBlock.parentNode.removeChild(previewBlock);
    if(previewTD) previewTD.parentNode.removeChild(previewTD);
    previewBlock = previewTD = null;
    
    eXo.portal.isInDragging = false;
  	eXo.portal.UIPortal.changeComposerSaveButton();
  };
  
  DragDrop.cancelCallback = function(dndEvent) {
  	if(Browser.browserType == "ie" && Browser.findMouseYInClient(dndEvent.backupMouseEvent) < 0) {
  		DragDrop.onDrop(dndEvent.backupMouseEvent);
  	}
  };
  
  var clickObject = this;
  var componentBlock = DOMUtil.findAncestorByClass(clickObject, "UIComponentBlock") ;

  if(componentBlock != null) {
    var dragBlock = eXo.portal.UIPortal.findUIComponentOf(componentBlock) ;
    DragDrop.init(eXo.portal.PortalDragDrop.findDropableTargets(dragBlock), clickObject, dragBlock, e) ;
  } else {
  	var dragBlock = DOMUtil.findAncestorByClass(clickObject, "DragObjectPortlet") ;
		if(dragBlock) {
  		DragDrop.init(eXo.portal.PortalDragDrop.findDropableTargets(dragBlock), clickObject, dragBlock, e) ;
		} else {
    	DragDrop.init(eXo.portal.PortalDragDrop.findDropableTargets(dragBlock), clickObject, clickObject, e) ;
		}
  }
};

PortalDragDrop.prototype.doDropCallback = function(dndEvent) {
	var srcElement = dndEvent.dragObject ;
  var targetElement = dndEvent.foundTargetObject;
  
  var newComponent = false;
  if(srcElement.isComponent && (targetElement.foundIndex != null)) {
    if(eXo.portal.PortalDragDrop.layoutTypeElementNode != null) {
      eXo.portal.PortalDragDrop.divRowContainerAddChild(srcElement, targetElement, targetElement.foundIndex) ;
    } else {
    	eXo.portal.PortalDragDrop.parentDragObject.style.width = "auto";
      eXo.portal.PortalDragDrop.tableColumnContainerAddChild(srcElement, targetElement, targetElement.foundIndex) ;
    }
  }
  
  if(!dndEvent.dragObject.isComponent) {
    dndEvent.dragObject.parentNode.removeChild(dndEvent.dragObject) ;
    newComponent = true;
  }
  
  if(!targetElement) return;
  
  var params = [
    {name: "srcID", value: (srcElement.id.replace(/^UIPortlet-/, "")).replace(/^UIContainer-/,"")},
    {name: "targetID", value: targetElement.id.replace(/^.*-/, "")},
    {name: "insertPosition", value: targetElement.foundIndex},
    {name: "newComponent", value: newComponent}
  ] ;
  
  try {
    dndEvent.lastFoundTargetObject.foundIndex = -1;
  } catch(err) {
  	
  }
	// Modified by Philippe : added callback function
  ajaxGet(eXo.env.server.createPortalURL("UIPortal", "MoveChild", true, params)) ;
};

/* Find components in dropable target */
PortalDragDrop.prototype.findDropableTargets = function(dragBlock) {
  var dropableTargets = new Array() ;
  var uiWorkingWorkspace = document.getElementById("UIWorkingWorkspace") ;
  var uiPortal = eXo.core.DOMUtil.findFirstDescendantByClass(uiWorkingWorkspace, "div", "UIPortal") ;
  var pagebody = document.getElementById("UIPageBody");
  var uiContainers = eXo.core.DOMUtil.findDescendantsByClass(uiWorkingWorkspace, "div", "UIContainer") ;
  if(eXo.portal.portalMode && pagebody) {
    dropableTargets.push(uiPortal) ;
  } else {
  	var uiPage = eXo.core.DOMUtil.findFirstDescendantByClass(uiWorkingWorkspace, "div", "UIPage") ;
    if(uiPage) dropableTargets.push(uiPage) ;
  }
  for(var i = 0; i < uiContainers.length; i++) {
  	if (eXo.core.DOMUtil.hasAncestor(uiContainers[i], dragBlock)) continue;
    dropableTargets.push(uiContainers[i]) ;
  }
  return dropableTargets ;
};

PortalDragDrop.prototype.scrollOnDrag = function(dndEvent) {
	var workspaceHeight = document.getElementById("UIWorkingWorkspace").offsetHeight;
  var browserHeight = eXo.core.Browser.getBrowserHeight() ;
  if(workspaceHeight <= browserHeight) return;
  var mouseY = eXo.core.Browser.findMouseYInClient(dndEvent.backupMouseEvent) ;
  var deltaTop = mouseY - (Math.round(browserHeight * 5/6)) ;
  var deltaBottom = mouseY - (Math.round(browserHeight/6)) ;
  if(deltaTop > 0) {
    document.documentElement.scrollTop += deltaTop - 5 ;
  }
  
  if(deltaBottom < 0 && document.documentElement.scrollTop > 0) {
    document.documentElement.scrollTop += deltaBottom ;
  }
};

PortalDragDrop.prototype.findInsertPosition = function(components, dragObject, layout) {
  if(layout == "row") {
    for(var i = 0; i < components.length; i++) {
      var componentTop = eXo.core.Browser.findPosY(components[i]) ;
      var dragObjectTop = eXo.core.Browser.findPosY(dragObject) ;
      var componentMiddle = componentTop + Math.round(components[i].offsetHeight / 2) ;
            
      if(dragObjectTop > componentMiddle) continue ;
      else return i;
    }
    return -1 ;
    
  } else {
	  var dragObjectX = eXo.core.Browser.findPosX(dragObject) ;
    for(var i = 0; i < components.length; i++) {
      var componentInTD = eXo.core.DOMUtil.getChildrenByTagName(components[i] ,"div")[0] ;    	
      var componentX = eXo.core.Browser.findPosX(components[i]) ;
      
      if(dragObjectX > componentX) continue ;
      else return i ;
    }
    return -1 ;
  }  
};

PortalDragDrop.prototype.createPreview = function(layoutType) {
	var previewBlock = document.createElement("div") ;
	previewBlock.className = "DragAndDropPreview" ;
	previewBlock.id = "DragAndDropPreview" ;
	return previewBlock;
};

PortalDragDrop.prototype.divRowContainerAddChild = function(insertBlock, targetElement, insertPosition) {
  var listComponent = eXo.core.DragDrop.dndEvent.foundTargetObject.listComponentInTarget ;
  var uiRowContainer = eXo.core.DOMUtil.findFirstDescendantByClass(targetElement, "div", "UIRowContainer") ;
  insertBlock.style.width = "auto" ;
  
  if(insertPosition >= 0) {
    uiRowContainer.insertBefore(insertBlock, listComponent[insertPosition]) ;
  } else {
    uiRowContainer.appendChild(insertBlock) ;
  }
};

PortalDragDrop.prototype.tableColumnContainerAddChild = function(insertBlock, targetElement, insertPosition) {
  var listComponent = eXo.core.DragDrop.dndEvent.foundTargetObject.listComponentInTarget ;
  var DOMUtil = eXo.core.DOMUtil ;
  var trContainer = DOMUtil.findFirstDescendantByClass(targetElement, "tr", "TRContainer") ;
  
  var tdInserted = document.createElement('td') ;
  tdInserted.appendChild(insertBlock) ;
  
  if(insertPosition >= 0) {
    trContainer.insertBefore(tdInserted, listComponent[insertPosition]) ;
  } else {
    trContainer.appendChild(tdInserted) ;
  }

  insertBlock.style.width = "auto" ;
  
	if(eXo.portal.PortalDragDrop.parentDragObject.tagName.toLowerCase() == "td") {
    DOMUtil.removeElement(eXo.portal.PortalDragDrop.parentDragObject) ;
  }
};

eXo.portal.PortalDragDrop = new PortalDragDrop() ;