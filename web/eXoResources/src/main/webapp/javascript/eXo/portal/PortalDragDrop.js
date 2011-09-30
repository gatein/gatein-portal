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
	if (eXo.core.DragDrop.dndEvent){
		return;
	}
	
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
    var properties = ["top", eXo.core.I18n.isLT() ? "left" : "right", "zIndex", "opacity", "filter", "position", "width"] ;
    this.origDragObjectStyle.copyProperties(properties, dragObject.style) ;
    
    var isAddingNewly = !DOMUtil.findFirstDescendantByClass(dragObject, "div", "UIComponentBlock");
   	
   	var uiWorkingWS = document.getElementById("UIWorkingWorkspace");
		PortalDragDrop.positionRootObj = !isAddingNewly ? uiWorkingWS : 
					DOMUtil.findFirstDescendantByClass(uiWorkingWS, "div", "UIPortalComposer");
		
		var originalDragObjectTop = Browser.findPosYInContainer(dragObject, PortalDragDrop.positionRootObj);
		var originalDragObjectLeft = Browser.findPosXInContainer(dragObject, PortalDragDrop.positionRootObj);
		PortalDragDrop.deltaYDragObjectAndMouse = Browser.findMouseRelativeY(dragObject, e);
		PortalDragDrop.deltaXDragObjectAndMouse = Browser.findMouseRelativeX(dragObject, e);
    if(isAddingNewly) {
      var contentContainer = DOMUtil.findAncestorByClass(dragObject, "PopupContent");
      originalDragObjectTop -= contentContainer.scrollTop;
      PortalDragDrop.deltaYDragObjectAndMouse += contentContainer.scrollTop;
    } 
		
    PortalDragDrop.parentDragObject = dragObject.parentNode ;
    //use this when press ESC with firefox (cancel dragdrop in column container)
    PortalDragDrop.backupParentSibling = DOMUtil.findNextElementByTagName(dragObject.parentNode, "td");
    PortalDragDrop.backupDragObjectWidth = dragObject.offsetWidth ;
        
    /*Case: dragObject out of UIPortal*/
    
    if(isAddingNewly) {
      var cloneObject = dragObject.cloneNode(true) ;
      dragObject.parentNode.insertBefore(cloneObject, dragObject) ;
      cloneObject.style.opacity = 0.5 ;
      cloneObject.style.filter = "alpha(opacity=50)" ;
      cloneObject.style.width = PortalDragDrop.backupDragObjectWidth + "px" ;
      dndEvent.dragObject = cloneObject ;
      dndEvent.dragObject.isAddingNewly = isAddingNewly;
    } else {
       var componentBlockWidth = 300;
       
    	previewBlock = PortalDragDrop.createPreview();
    	previewBlock.style.height = dragObject.offsetHeight + "px";
    	dragObject.parentNode.insertBefore(previewBlock, dragObject);
    	dragObject.style.width = componentBlockWidth + "px";
    	var componentBlock = eXo.core.DOMUtil.findFirstDescendantByClass(dragObject, "div", "UIComponentBlock") ;
    	var editBlock = eXo.core.DOMUtil.findFirstChildByClass(componentBlock, "div", "EDITION-BLOCK");
	    if(editBlock) {
	    	var newLayer = eXo.core.DOMUtil.findFirstDescendantByClass(editBlock, "div", "NewLayer");
	    	if(newLayer) newLayer.style.width = "300px";
	    }
    }
    dragObject.isAddingNewly = isAddingNewly;
    dragObject = dndEvent.dragObject;
    dragObject.style.position = "absolute" ;
    dragObject.style.top = originalDragObjectTop + "px" ;
    var dragObjectLeft = originalDragObjectLeft;
    
    if (PortalDragDrop.deltaXDragObjectAndMouse > componentBlockWidth/2) {
       if ((PortalDragDrop.backupDragObjectWidth - PortalDragDrop.deltaXDragObjectAndMouse) > componentBlockWidth/2) {
          dragObjectLeft = originalDragObjectLeft + PortalDragDrop.deltaXDragObjectAndMouse - componentBlockWidth/2;
       } else {
          dragObjectLeft = originalDragObjectLeft + PortalDragDrop.backupDragObjectWidth - componentBlockWidth;
       }
    }
    
    if (eXo.core.I18n.isLT()) dragObject.style.left = dragObjectLeft + "px";
    else dragObject.style.right = PortalDragDrop.positionRootObj.offsetWidth - dragObject.offsetWidth - dragObjectLeft + "px";
    
    eXo.portal.isInDragging = true;
  }
  
  DragDrop.dragCallback = function(dndEvent) {
    var dragObject = dndEvent.dragObject ;
    /* Control Scroll */
    eXo.portal.PortalDragDrop.scrollOnDrag(dndEvent) ;
    if(!dndEvent.foundTargetObject) {
       if (!dndEvent.lastFoundTargetObject) {
          return;
       } else {
          dndEvent.foundTargetObject = dndEvent.lastFoundTargetObject;
       }
    }
    
    var uiComponentLayout ;
    if(dndEvent.foundTargetObject.className == "UIPage") {
			uiComponentLayout = DOMUtil.findFirstDescendantByClass(dndEvent.foundTargetObject, "div", "VIEW-PAGE");
    } else if(dndEvent.foundTargetObject.className == "UIPortal") {
      if(eXo.portal.portalMode % 2) uiComponentLayout = DOMUtil.findFirstDescendantByClass(dndEvent.foundTargetObject, "div", "LAYOUT-PORTAL") ;
      else uiComponentLayout = DOMUtil.findFirstDescendantByClass(dndEvent.foundTargetObject, "div", "VIEW-PORTAL");
    } else {
      var foundUIComponent = new eXo.portal.UIPortalComponent(dndEvent.foundTargetObject) ;
      if(eXo.portal.portalMode % 2) uiComponentLayout = foundUIComponent.getLayoutBlock() ;
      else uiComponentLayout = foundUIComponent.getViewBlock();
      uiComponentLayout.style.height = "auto";
    }
    
    dndEvent.foundTargetObject.uiComponentLayoutType = uiComponentLayout ;
    var componentIdElement = DOMUtil.getChildrenByTagName(uiComponentLayout, "div")[0] ;
    var layoutTypeElement = DOMUtil.getChildrenByTagName(componentIdElement, "div")[0] ;
    eXo.portal.PortalDragDrop.layoutTypeElementNode = layoutTypeElement ;
    
    if(previewBlock == null) previewBlock = eXo.portal.PortalDragDrop.createPreview();
    
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
      var insertPosition = eXo.portal.PortalDragDrop.findInsertPosition(listComponent, "row", dndEvent.backupMouseEvent) ;
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
          }
        } else {
          listComponent.push(tdElementList[i]) ;
        }          
      }
      
      dndEvent.foundTargetObject.listComponentInTarget = listComponent ;
      var insertPosition = eXo.portal.PortalDragDrop.findInsertPosition(listComponent, "column", dndEvent.backupMouseEvent) ;
      dndEvent.foundTargetObject.foundIndex = insertPosition ;
      
      /* Insert preview block */
      if(insertPosition >= 0) {
        trContainer.insertBefore(dragObject.parentNode, listComponent[insertPosition]) ;
      } else {
        trContainer.appendChild(dragObject.parentNode) ;
      }
    }
     var dragParent = dragObject.parentNode;
     if (eXo.core.DOMUtil.getChildrenByTagName(dragParent, "div").length === 1 && !eXo.core.DOMUtil.hasClass(dragParent, "EmptyContainer")) {
        eXo.core.DOMUtil.addClass(dragParent, "EmptyContainer");
     }
  } ;

  DragDrop.dropCallback = function(dndEvent) {
  	var hasChanged = true;
  	//When press esc key, we want to cancel the dragdrop
  	if (dndEvent.backupMouseEvent && dndEvent.backupMouseEvent.keyCode == 27) {
  		hasChanged = false;
  	}
  	//When dragObject is outside 
  	if (!dndEvent.foundTargetObject) {
  	   dndEvent.foundTargetObject = dndEvent.lastFoundTargetObject;
  	}
  	
    var targetElement = dndEvent.foundTargetObject;
    if(!targetElement || targetElement.foundIndex == null) {
       hasChanged = false;
    }
//	 Case RowContainer : When dragobject is next to preview object (position is not changed)
//    Case ColumnContainer : When dragObject.parent's lastSibling doesn't change
    var DOMUtil = eXo.core.DOMUtil;
  	if(!dndEvent.dragObject.isAddingNewly) {
      if (dndEvent.dragObject.parentNode.tagName.toLowerCase() == "td") {
        //Column Container
        var backupParentSibling = eXo.portal.PortalDragDrop.backupParentSibling; 
        if (DOMUtil.findNextElementByTagName(dndEvent.dragObject.parentNode, "td") == backupParentSibling) {
          hasChanged = false;          
        }
      } else {
        //RowContainer
        var tempObj = DOMUtil.findNextElementByTagName(dndEvent.dragObject,  "div");
        if (tempObj != null && tempObj.className == "DragAndDropPreview") {
          hasChanged = false;
        } else {
          tempObj = DOMUtil.findPreviousElementByTagName(dndEvent.dragObject,  "div");
          if (tempObj != null && tempObj.className == "DragAndDropPreview") {
            hasChanged = false;
          }
        }
      }
  	}

    if(dndEvent.backupMouseEvent && dndEvent.backupMouseEvent.keyCode != 27) {
    	eXo.portal.PortalDragDrop.doDropCallback(dndEvent) ;
    } else {
      //When click ESC, restore dragObject's last position
      if (dndEvent.dragObject.parentNode && dndEvent.dragObject.parentNode.tagName.toLowerCase() == "td") {
        var tdNode = dndEvent.dragObject.parentNode ;
        var lastSibling = eXo.portal.PortalDragDrop.backupParentSibling;
        if (lastSibling == null) {
          tdNode.parentNode.appendChild(tdNode);
        } else {
          tdNode.parentNode.insertBefore(tdNode, lastSibling);
        }
      }
      
      if(dndEvent.dragObject.isAddingNewly) {
				dndEvent.dragObject.parentNode.removeChild(dndEvent.dragObject) ;
			}
    }
    
    if(!dndEvent.dragObject.isAddingNewly) {
			var componentBlock = eXo.core.DOMUtil.findFirstDescendantByClass(dndEvent.dragObject, "div", "UIComponentBlock") ;
	  	var editBlock = eXo.core.DOMUtil.findFirstChildByClass(componentBlock, "div", "EDITION-BLOCK");
	    if(editBlock) editBlock.style.display = "none";
    }
    
    if(previewBlock) previewBlock.parentNode.removeChild(previewBlock);
    previewBlock = null;
    
    eXo.portal.isInDragging = false;
    if (hasChanged) {
    	eXo.portal.UIPortal.changeComposerSaveButton();
    }

    // fix bug WEBOS-196
//		dndEvent.dragObject.style.width = "auto" ;
    this.origDragObjectStyle.setProperties(dndEvent.dragObject.style, false) ;
  };
  
  var clickObject = this;
  var componentBlock = DOMUtil.findAncestorByClass(clickObject, "UIComponentBlock") ;

	//Check if it is dragging the object existing in the current layout or from the popup composer to add newly
  if(componentBlock != null) {
    var dragBlock = eXo.portal.UIPortal.findUIComponentOf(componentBlock) ;
    DragDrop.init(eXo.portal.PortalDragDrop.findDropableTargets(dragBlock), clickObject, dragBlock, e) ;
  } else {
  	var dragBlock = DOMUtil.findAncestorByClass(clickObject, "DragObjectPortlet") ;
  	//TODO: Seems the dragBlock is always null 
		if(dragBlock) {
			eXo.debug("The dragBlock is not null");
  		DragDrop.init(eXo.portal.PortalDragDrop.findDropableTargets(dragBlock), clickObject, dragBlock, e) ;
		} else {
    	DragDrop.init(eXo.portal.PortalDragDrop.findDropableTargets(dragBlock), clickObject, clickObject, e) ;
		}
  }
};

/**
 * Perform following works after dropping :
 * 
 * 1. Remove the dragging object if any
 * 2. Send an request to server side to update the changes
 */
PortalDragDrop.prototype.doDropCallback = function(dndEvent) {
	var srcElement = dndEvent.dragObject ;
  var targetElement = dndEvent.foundTargetObject;
  
  if(!targetElement || targetElement.foundIndex == null) {
  	if(dndEvent.dragObject.isAddingNewly) {
	    dndEvent.dragObject.parentNode.removeChild(dndEvent.dragObject) ;
  	}
  	dndEvent.dragObject.style.width = "auto";
  	return;
  }
  
  if(!srcElement.isAddingNewly && (targetElement.foundIndex != null)) {
    if(eXo.portal.PortalDragDrop.layoutTypeElementNode != null) {
      eXo.portal.PortalDragDrop.divRowContainerAddChild(srcElement, targetElement, targetElement.foundIndex) ;
    }
  }

  if(srcElement.isAddingNewly) {
    eXo.core.DOMUtil.removeElement(srcElement) ;
  }
  
  var params = [
    {name: "srcID", value: (srcElement.id.replace(/^UIPortlet-/, ""))},
    {name: "targetID", value: targetElement.id.replace(/^.*-/, "")},
    {name: "insertPosition", value: targetElement.foundIndex},
    {name: "isAddingNewly", value: srcElement.isAddingNewly}
  ] ;
  
  try {
    dndEvent.lastFoundTargetObject.foundIndex = null;
  } catch(err) {
  	
  }
	// Modified by Philippe : added callback function
  ajaxGet(eXo.env.server.createPortalURL("UIPortal", "MoveChild", true, params)) ;
};

/**
 * Return an array of droppable target objects
 * 
 * @param the dragging object
 */
PortalDragDrop.prototype.findDropableTargets = function(dragBlock) {
  var DOMUtil = eXo.core.DOMUtil;
  var dropableTargets = new Array() ;
  
  if (dragBlock && DOMUtil.hasClass(dragBlock, "UIColumnContainer")) {
    var uiTableContainer = eXo.core.DOMUtil.findAncestorByClass(dragBlock, "UITableColumnContainer");
    dropableTargets.push(uiTableContainer);
    return dropableTargets;
  }

  var uiWorkingWorkspace = document.getElementById("UIWorkingWorkspace") ;
  var pagebody = document.getElementById("UIPageBody");
  if(eXo.portal.portalMode && pagebody) {
	  var uiPortal = DOMUtil.findFirstDescendantByClass(uiWorkingWorkspace, "div", "UIPortal") ;
    dropableTargets.push(uiPortal) ;
  } else {
  	var uiPage = DOMUtil.findFirstDescendantByClass(uiWorkingWorkspace, "div", "UIPage") ;
    if(uiPage) dropableTargets.push(uiPage) ;
  }
  
  var uiContainers = DOMUtil.findDescendantsByClass(uiWorkingWorkspace, "div", "UIContainer") ;
  for(var i = 0; i < uiContainers.length; i++) {
  	if(DOMUtil.hasAncestor(uiContainers[i], dragBlock)) continue;
  	if(DOMUtil.hasClass(uiContainers[i], "ProtectedContainer")) continue;
  	if (DOMUtil.hasClass(uiContainers[i], "UITableColumnContainer")) continue;

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
  var currentDragObjPos = parseInt(dndEvent.dragObject.style.top);
  if(deltaTop > 0) {
    document.documentElement.scrollTop += deltaTop - 5;
    currentDragObjPos += deltaTop - 5;
  }
  
  if(deltaBottom < 0 && document.documentElement.scrollTop > 0) {
    document.documentElement.scrollTop += deltaBottom ;
    currentDragObjPos += deltaBottom;
  }
  
  dndEvent.dragObject.style.top = currentDragObjPos + "px";
};

/**
 * Return a most suiable position among the <code>components</code> objects
 * that the dragging object should be at
 * 
 * @param layout {string} the layout type which is "row" or "column"
 */
PortalDragDrop.prototype.findInsertPosition = function(components, layout, mouseEvent) {
	   var Browser = eXo.core.Browser;
	   if (layout == "row") {
	      for (var i = 0; i < components.length; i++) {
	         var componentTop = Browser.findPosY(components[i]);
	         var mouseYInPage = Browser.findMouseYInPage(mouseEvent);
	         var componentMIddle = componentTop + Math.round(components[i].offsetHeight / 2);
	         if (mouseYInPage > componentMIddle) continue;
	         else return i;
	      }
	      
	      return -1;
	   } else {
	      for (var i = 0; i < components.length; i++) {
	         var mouseXInPage = Browser.findMouseXInPage(mouseEvent);
	         var componentX = Browser.findPosX(components[i], eXo.core.I18n.isRT());
	         if (eXo.core.I18n.isRT()) {
	        	 if (mouseXInPage < componentX) continue;        	 
	         } else if (mouseXInPage > componentX ) continue;
	                  
	         return i; 
	      }
	      
	      return -1;
	   }  
	};

/**
 * Create a div block which show the preview block
 */
PortalDragDrop.prototype.createPreview = function(layoutType) {
	var previewBlock = document.createElement("div") ;
	previewBlock.className = "DragAndDropPreview" ;
	previewBlock.id = "DragAndDropPreview" ;
	return previewBlock;
};

/**
 * Add the <code>srcElement</code> dragging object to a container.
 * If the dragging object is a column then let remove it from the table column container
 */
PortalDragDrop.prototype.divRowContainerAddChild = function(srcElement, targetElement, insertPosition) {
  var listComponent = eXo.core.DragDrop.dndEvent.foundTargetObject.listComponentInTarget ;
  var uiRowContainer = eXo.core.DOMUtil.findFirstDescendantByClass(targetElement, "div", "UIRowContainer") ;
  srcElement.style.width = "auto" ;
  
	var parentNode = srcElement.parentNode;
  if(insertPosition >= 0) {
    uiRowContainer.insertBefore(srcElement, listComponent[insertPosition]) ;
  } else {
    uiRowContainer.appendChild(srcElement) ;
  }

   eXo.core.DOMUtil.removeClass(uiRowContainer, "EmptyContainer");
	
  if(parentNode.nodeName.toLowerCase() == "td") {
  	eXo.core.DOMUtil.removeElement(parentNode) ;
  }
};

eXo.portal.PortalDragDrop = new PortalDragDrop() ;