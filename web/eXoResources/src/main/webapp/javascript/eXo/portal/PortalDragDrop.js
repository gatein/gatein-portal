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
eXo.portal.PortalDragDrop = { 
	
	/**
	 * This function inits the PortalDragDrop object
	 * It initializes a DragDrop object that will manage the drag events
	 */
	init : function(dragClasses) {
      if (!dragClasses || !dragClasses.length) {
		return;
	  }
		
	  var DOMUtil = eXo.core.DOMUtil;
	  var browser = eXo.core.Browser;
	  var DragDrop = eXo.core.DragDrop2;
	  var PortalDragDrop = eXo.portal.PortalDragDrop;
	  
	  var previewBlock = null;
		/**
		 * This function is called after the DragDrop object is initialized
		 */
	  var initCallback = function (x, y, mouseX, mouseY, e) {	  	
		if (eXo.portal.isInDragging) return;
		  
	    this.origDragObjectStyle = new eXo.core.HashMap();
	    var dragObject = this, jDragObj = xj(this);
	    var properties = ["top", eXo.core.I18n.isLT() ? "left" : "right", "zIndex", "opacity", "filter", "position", "width"];
	    this.origDragObjectStyle.copyProperties(properties, dragObject.style);
	    
	    var isAddingNewly = !DOMUtil.findFirstDescendantByClass(dragObject, "div", "UIComponentBlock");
		var position = jDragObj.position();
		var originalDragObjectTop = position.top;
		var originalDragObjectLeft = position.left;
			
	    //use this when press ESC with firefox (cancel dragdrop in column container)
	    PortalDragDrop.backupParentSibling = DOMUtil.findNextElementByTagName(dragObject.parentNode, "td");	  
	    var backupDragObjectWidth = dragObject.offsetWidth;
	        
	    var componentBlockWidth = 300;
	    if(isAddingNewly) {
	      var cloneObject = dragObject.cloneNode(true);
	      dragObject.parentNode.insertBefore(cloneObject, dragObject);
	      DragDrop.init(cloneObject, cloneObject);
	      cloneObject.onDragStart = initCallback;
	      cloneObject.onDrag = dragCallback;
	      cloneObject.onDragEnd = dropCallback;
	      dragObject.style.width = backupDragObjectWidth + "px" ;
	      xj(dragObject).fadeTo(0, 0.5);
	    } else {
	        previewBlock = PortalDragDrop.createPreview();
	    	dragObject.parentNode.insertBefore(previewBlock, dragObject);
	    	xj(dragObject).width(componentBlockWidth).find(".EDITION-BLOCK .NewLayer").each(function() {
	    		xj(this).width(componentBlockWidth);
	    	});	    	
	    }
	    
	    dragObject.isAddingNewly = isAddingNewly;	    
	    dragObject.style.position = "absolute";
	    dragObject.style.top = originalDragObjectTop + "px";
	    var dragObjectLeft = originalDragObjectLeft;
	    
	    var objAndMouse = browser.findMouseRelativeX(dragObject.offsetParent, e) - originalDragObjectLeft;
	    if (objAndMouse > componentBlockWidth/2) {
	    	var tmp = (objAndMouse*componentBlockWidth)/backupDragObjectWidth;
	    	dragObjectLeft = originalDragObjectLeft + objAndMouse - tmp;
	    }	       
	    
	    if (eXo.core.I18n.isRT() && !isAddingNewly && !jDragObj.hasClass("UIPageBody")) {
	    	dragObject.style.left = originalDragObjectLeft + backupDragObjectWidth - componentBlockWidth + "px";
	    } else {
	    	//rt and lt are the same for UIPageBody and newly added obj
	    	dragObject.style.left = dragObjectLeft + "px";
	    }
	    
	    PortalDragDrop.dropableTarget = PortalDragDrop.findDropableTargets(dragObject);
	    eXo.portal.isInDragging = true;
	  };
	  
	   var dragCallback = function(nx, ny, ex, ey, e) {
	     var dragObject = this;
	     /* Control Scroll */
	     eXo.portal.PortalDragDrop.scrollOnDrag(dragObject, e);
	    
	     var foundTarget = PortalDragDrop.findTarget(dragObject, PortalDragDrop.dropableTarget, ex, ey);	  
	     PortalDragDrop.lastFoundTargetObject = PortalDragDrop.foundTargetObject;
	     PortalDragDrop.foundTargetObject = foundTarget;
	     if(!foundTarget) {
	        if (!PortalDragDrop.lastFoundTargetObject) {
	          return;
	        } else {
	          PortalDragDrop.foundTargetObject = foundTarget = PortalDragDrop.lastFoundTargetObject;
	        }
	     }
	    
	     var uiComponentLayout ;
	     if(foundTarget.className == "UIPage") {
		   uiComponentLayout = DOMUtil.findFirstDescendantByClass(foundTarget, "div", "VIEW-PAGE");
	     } else if(foundTarget.className == "UIPortal") {
	       if(eXo.portal.portalMode % 2) uiComponentLayout = DOMUtil.findFirstDescendantByClass(foundTarget, "div", "LAYOUT-PORTAL") ;
	       else uiComponentLayout = DOMUtil.findFirstDescendantByClass(foundTarget, "div", "VIEW-PORTAL");
	     } else {
	       var foundUIComponent = new eXo.portal.UIPortalComponent(foundTarget) ;
	       if(eXo.portal.portalMode % 2) uiComponentLayout = foundUIComponent.getLayoutBlock() ;
	       else uiComponentLayout = foundUIComponent.getViewBlock();
	       uiComponentLayout.style.height = "auto";
	     }
	    
	     var componentIdElement = DOMUtil.getChildrenByTagName(uiComponentLayout, "div")[0] ;
	     var layoutTypeElement = DOMUtil.getChildrenByTagName(componentIdElement, "div")[0] ;
	     PortalDragDrop.layoutTypeElementNode = layoutTypeElement;
	    
	     if(previewBlock == null) previewBlock = PortalDragDrop.createPreview();
	    
	     if(layoutTypeElement != null && !DOMUtil.hasClass(layoutTypeElement, "UITableColumnContainer")) {
	      /* ===============================CASE ROW LAYOUT================================ */
	      var rowContainer = DOMUtil.findFirstDescendantByClass(uiComponentLayout, "div", "UIRowContainer");
	      var childRowContainer = DOMUtil.getChildrenByTagName(rowContainer, "div") ;
	      
	      var listComponent = new Array();
	      for(var i = 0; i < childRowContainer.length; i++) {
	        if((childRowContainer[i].className != "DragAndDropPreview") && (childRowContainer[i] != dragObject)) {
	          listComponent.push(childRowContainer[i]);
	        }
	      }
	      
	      foundTarget.listComponentInTarget = listComponent ;
	      var insertPosition = eXo.portal.PortalDragDrop.findInsertPosition(listComponent, "row", ey) ;
	      foundTarget.foundIndex = insertPosition ;
	      
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
	      
	      foundTarget.listComponentInTarget = listComponent;
	      var insertPosition = eXo.portal.PortalDragDrop.findInsertPosition(listComponent, "column", ex);
	      foundTarget.foundIndex = insertPosition;
	      
	      /* Insert preview block */
	      if(insertPosition >= 0) {
	          trContainer.insertBefore(dragObject.parentNode, listComponent[insertPosition]);
	       } else {
	          trContainer.appendChild(dragObject.parentNode);
	       }
	    }
	    var dragParent = dragObject.parentNode;
	    if (eXo.core.DOMUtil.getChildrenByTagName(dragParent, "div").length === 1 && !eXo.core.DOMUtil.hasClass(dragParent, "EmptyContainer")) {
	       eXo.core.DOMUtil.addClass(dragParent, "EmptyContainer");
	    }
	  } ;

	  var dropCallback = function(x, y, clientX, clientY, e) {
	    var hasChanged = true;
	    var dragObject = this;
		//When press esc key, we want to cancel the dragdrop
	  	if (e.keyCode === 27) {
	  		hasChanged = false;
	  	}
	  	//When dragObject is outside 
	  	if (!PortalDragDrop.foundTargetObject) {
	  		PortalDragDrop.foundTargetObject = PortalDragDrop.lastFoundTargetObject;
	  	}
	  	
	    var targetElement = PortalDragDrop.foundTargetObject;
	    if(!targetElement || targetElement.foundIndex == null) {
	       hasChanged = false;
	    }
//			 Case RowContainer : When dragobject is next to preview object (position is not changed)
//		    Case ColumnContainer : When dragObject.parent's lastSibling doesn't change
	    var DOMUtil = eXo.core.DOMUtil;
	  	if(!dragObject.isAddingNewly) {
	      if (dragObject.parentNode.tagName.toLowerCase() == "td") {
	        //Column Container
	        var backupParentSibling = eXo.portal.PortalDragDrop.backupParentSibling; 
	        if (DOMUtil.findNextElementByTagName(dragObject.parentNode, "td") == backupParentSibling) {
	          hasChanged = false;          
	        }
	      } else {
	        //RowContainer
	        var tempObj = DOMUtil.findNextElementByTagName(dragObject,  "div");
	        if (tempObj != null && tempObj.className == "DragAndDropPreview") {
	          hasChanged = false;
	        } else {
	          tempObj = DOMUtil.findPreviousElementByTagName(dragObject,  "div");
	          if (tempObj != null && tempObj.className == "DragAndDropPreview") {
	            hasChanged = false;
	          }
	        }
	      }
	  	}

	    if(e.keyCode !== 27) {
	    	eXo.portal.PortalDragDrop.doDropCallback(dragObject);
	    } else {
	      //When click ESC, restore dragObject's last position
	      if (dragObject.parentNode && dragObject.parentNode.tagName.toLowerCase() == "td") {
	        var tdNode = dragObject.parentNode ;
	        var lastSibling = eXo.portal.PortalDragDrop.backupParentSibling;
	        if (lastSibling == null) {
	          tdNode.parentNode.appendChild(tdNode);
	        } else {
	          tdNode.parentNode.insertBefore(tdNode, lastSibling);
	        }
	      }
	      
	      if(dragObject.isAddingNewly) {
			dragObject.parentNode.removeChild(dragObject);
		  }
	    }
	    
	    if(!dragObject.isAddingNewly) {
			var componentBlock = eXo.core.DOMUtil.findFirstDescendantByClass(dragObject, "div", "UIComponentBlock");
		  	var editBlock = eXo.core.DOMUtil.findFirstChildByClass(componentBlock, "div", "EDITION-BLOCK");
		    if(editBlock) editBlock.style.display = "none";
	    }
	    
	    if(previewBlock) previewBlock.parentNode.removeChild(previewBlock);
	    previewBlock = null;
	    
	    eXo.portal.isInDragging = false;
	    if (hasChanged) {
	    	eXo.portal.UIPortal.changeComposerSaveButton();
	    }

	    this.origDragObjectStyle.setProperties(dragObject.style, false);
	  };
	  
	  dragClasses = dragClasses.join(",.");
	  xj("." + dragClasses).each(function() {
		  var dragBlock = this;	  
		  var clickObject = xj(this).find(".DragControlArea").last();

		  if(clickObject.length != 0) {
			  //object existing in the current layout
			  clickObject = clickObject[0];
		  } else {
			  //from the popup composer to add newly
			  clickObject = dragBlock;
		  }
		  DragDrop.init(clickObject, dragBlock);
		  
		  dragBlock.onDragStart = initCallback;
		  dragBlock.onDrag = dragCallback;
		  dragBlock.onDragEnd = dropCallback;
	  });	 	 
	},

	/**
	 * Perform following works after dropping :
	 * 
	 * 1. Remove the dragging object if any
	 * 2. Send an request to server side to update the changes
	 */
	doDropCallback : function(dragObject) {
	  var srcElement = dragObject ;
	  var targetElement = eXo.portal.PortalDragDrop.foundTargetObject;
	  
	  if(!targetElement || targetElement.foundIndex == null) {
	  	if(dragObject.isAddingNewly) {
		    dragObject.parentNode.removeChild(dragObject);
	  	}
	  	dragObject.style.width = "auto";
	  	return;
	  }
	  
	  if(!srcElement.isAddingNewly && (targetElement.foundIndex != null)) {
	    if(eXo.portal.PortalDragDrop.layoutTypeElementNode != null) {
	      eXo.portal.PortalDragDrop.divRowContainerAddChild(srcElement, targetElement, targetElement.foundIndex);
	    }
	  }

	  if(srcElement.isAddingNewly) {
	    eXo.core.DOMUtil.removeElement(srcElement);
	  }
	  
	  var params = [
	    {name: "srcID", value: (srcElement.id.replace(/^UIPortlet-/, ""))},
	    {name: "targetID", value: targetElement.id.replace(/^.*-/, "")},
	    {name: "insertPosition", value: targetElement.foundIndex},
	    {name: "isAddingNewly", value: srcElement.isAddingNewly}
	  ] ;
	  
	  try {
		  eXo.portal.PortalDragDrop.lastFoundTargetObject.foundIndex = null;
	  } catch(err) {
	  	
	  }
		// Modified by Philippe : added callback function
	  ajaxGet(eXo.env.server.createPortalURL("UIPortal", "MoveChild", true, params));
	},

	/**
	 * Return an array of droppable target objects
	 * 
	 * @param the dragging object
	 */
	findDropableTargets : function(dragBlock) {
	  var DOMUtil = eXo.core.DOMUtil;
	  var dropableTargets = new Array();

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
	},

	scrollOnDrag : function(dragObject, e) {
	  var workspaceHeight = document.getElementById("UIWorkingWorkspace").offsetHeight;
	  var browserHeight = eXo.core.Browser.getBrowserHeight() ;
	  if(workspaceHeight <= browserHeight) return;
	  var mouseY = eXo.core.Browser.findMouseYInClient(e) ;
	  var deltaTop = mouseY - (Math.round(browserHeight * 5/6)) ;
	  var deltaBottom = mouseY - (Math.round(browserHeight/6)) ;
	  var currentDragObjPos = parseInt(dragObject.style.top);
	  if(deltaTop > 0) {
	    document.documentElement.scrollTop += deltaTop - 5;
	    currentDragObjPos += deltaTop - 5;
	  }
	  
	  if(deltaBottom < 0 && document.documentElement.scrollTop > 0) {
	    document.documentElement.scrollTop += deltaBottom ;
	    currentDragObjPos += deltaBottom;
	  }
	  
	  dragObject.style.top = currentDragObjPos + "px";
	},

	/**
	 * Return a most suiable position among the <code>components</code> objects
	 * that the dragging object should be at
	 * 
	 * @param layout {string} the layout type which is "row" or "column"
	 */
	findInsertPosition : function(components, layout, mousePos) {
	   var browser = eXo.core.Browser;
	   if (layout == "row") {
	      for (var i = 0; i < components.length; i++) {
	         var componentTop = browser.findPosY(components[i]);
	         var mouseYInPage = mousePos
	         var componentMIddle = componentTop + Math.round(components[i].offsetHeight / 2);
	         if (mouseYInPage > componentMIddle) continue;
	         else return i;
	      }
	      
	      return -1;
	   } else {
	      for (var i = 0; i < components.length; i++) {
	         var mouseXInPage = mousePos
	         var componentX = browser.findPosX(components[i], eXo.core.I18n.isRT());
	         if (eXo.core.I18n.isRT()) {
	        	 if (mouseXInPage < componentX) continue;        	 
	         } else if (mouseXInPage > componentX ) continue;
	                  
	         return i; 
	      }
	      
	      return -1;
	   }  
	},

	/**
	 * Create a div block which show the preview block
	 */
	createPreview : function(layoutType) {
		var previewBlock = document.createElement("div") ;
		previewBlock.className = "DragAndDropPreview" ;
		previewBlock.id = "DragAndDropPreview" ;
		return previewBlock;
	},

	findTarget : function(dragObject, dropableTargets, mousexInPage, mouseyInPage) {
	  if(dropableTargets == null) return null ;
	  
	  var foundTarget = null ;
	  var len = dropableTargets.length ;
	  for(var i = 0 ; i < len ; i++) {
	    var ele =  dropableTargets[i] ;

	    if(dragObject != ele && this.isIn(mousexInPage, mouseyInPage, ele)) {
	      if(foundTarget == null) {
	        foundTarget = ele ;
	      } else {
	        if(eXo.core.DOMUtil.hasAncestor(ele, foundTarget)) {
	          foundTarget = ele ;
	        }
	      } 
	    }
	  }
	 	
	  return foundTarget ;
	},
	  
	isIn : function(x, y, component) {
	  var browser = eXo.core.Browser;
	  var componentLeft = browser.findPosX(component);
	  var componentRight = componentLeft + component.offsetWidth ;
	  var componentTop = browser.findPosY(component) ;
	  var componentBottom = componentTop + component.offsetHeight ;
	  var isOver = false ;

	  if((componentLeft < x) && (x < componentRight)) {
	    if((componentTop < y) && (y < componentBottom)) {
	      isOver = true ;
	    }
	  }
	  return isOver ;
	},
	
	/**
	 * Add the <code>srcElement</code> dragging object to a container.
	 * If the dragging object is a column then let remove it from the table column container
	 */
	divRowContainerAddChild : function(srcElement, targetElement, insertPosition) {
	  var listComponent = targetElement.listComponentInTarget ;
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
	}
} ;
