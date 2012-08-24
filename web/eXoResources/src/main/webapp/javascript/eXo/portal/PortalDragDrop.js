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
		
      $(".UIPageBody .DragControlArea").off("mouseover").on("mouseover", function() {
        if(eXo.portal.portalMode == 1 || eXo.portal.portalMode == 2) {
          this.style.cursor ='move';
        } else {
          this.style.cursor ='default';    		  
        }
      });
      
	  var browser = base.Browser;
	  var DragDrop = common.DragDrop;
	  var PortalDragDrop = _module.PortalDragDrop;
	  
	  var previewBlock = null;
		/**
		 * This function is called after the DragDrop object is initialized
		 */
	  var initCallback = function (x, y, mouseX, mouseY, e) {	  	
		if (eXo.portal.isInDragging) return;		  
		var dragObject = this, jDragObj = $(this);
		
		var origDragObjectStyle = {};
	    var properties = ["top", eXo.core.I18n.isLT() ? "left" : "right", "zIndex", "opacity", "filter", "position", "width"];
	    $.each(properties, function(idx, elem) {
	    	origDragObjectStyle[elem] = dragObject.style[elem];
	    });
	    jDragObj.data("origDragObjectStyle", origDragObjectStyle);
	    
	    var isAddingNewly = jDragObj.children(".UIComponentBlock").length == 0;
		var originalDragObjectTop = y;
		var originalDragObjectLeft = x;
		if (!isAddingNewly && browser.isIE7()) {
			originalDragObjectLeft = browser.findPosXInContainer(dragObject, $("#UIWorkingWorkspace")[0]);
		}
			
	    //use this when press ESC with firefox (cancel dragdrop in column container)
	    jDragObj.data("backupParentSibling", jDragObj.parent().next("td"));	  
	    var backupDragObjectWidth = dragObject.offsetWidth;
	        
	    var componentBlockWidth = 300;
	    if(isAddingNewly) {
	      var cloneObject = jDragObj.clone(true, true);
	      jDragObj.before(cloneObject);
	      DragDrop.init(cloneObject[0], cloneObject[0]);
	      cloneObject[0].onDragStart = initCallback;
	      cloneObject[0].onDrag = dragCallback;
	      cloneObject[0].onDragEnd = dropCallback;
	      
	      jDragObj.width(backupDragObjectWidth);
	      jDragObj.fadeTo(0, 0.5);
	    } else {
	        previewBlock = PortalDragDrop.createPreview();
	    	dragObject.parentNode.insertBefore(previewBlock, dragObject);
	    	jDragObj.width(componentBlockWidth).find(".EDITION-BLOCK .NewLayer").each(function() {
	    		$(this).width(componentBlockWidth);
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
	    
	    jDragObj.data("dropableTargets", PortalDragDrop.findDropableTargets(dragObject));
	    eXo.portal.isInDragging = true;
	  };
	  
	   var dragCallback = function(nx, ny, ex, ey, e) {
	     var dragObject = this, jDragObj = $(this);
	     /* Control Scroll */
	     _module.PortalDragDrop.scrollOnDrag(dragObject, e);
	    
	     var foundTarget = PortalDragDrop.findTarget(dragObject, ex, ey) || jDragObj.data("lastFoundTargetObject");
	     if (!foundTarget) return;	     
	     jDragObj.data("lastFoundTargetObject", jDragObj.data("foundTargetObject"));
	     jDragObj.data("foundTargetObject", foundTarget);	     
	    
	     var jTarget = $(foundTarget);
	     var uiComponentLayout;
	     if(foundTarget.className == "UIPage") {
		   uiComponentLayout = jTarget.find(".VIEW-PAGE").first();
	     } else if(foundTarget.className == "UIPortal") {
	       if(eXo.portal.portalMode % 2) uiComponentLayout = jTarget.find(".LAYOUT-PORTAL").first();
	       else uiComponentLayout = jTarget.find(".VIEW-PORTAL").first();
	     } else {
	       var foundUIComponent = $(foundTarget);
	       if(eXo.portal.portalMode % 2) uiComponentLayout = foundUIComponent.find(".LAYOUT-BLOCK").first();
	       else uiComponentLayout = foundUIComponent.find(".VIEW-BLOCK").first();
	       uiComponentLayout.css("height", "auto");
	     }
	    
	     var componentIdElement = uiComponentLayout.children("div").first();
	     var layoutTypeElement = componentIdElement.children("div");
	     layoutTypeElement = layoutTypeElement.length == 0 ? null : layoutTypeElement[0];
	     PortalDragDrop.layoutTypeElementNode = layoutTypeElement;
	    
	     if(previewBlock == null) previewBlock = PortalDragDrop.createPreview();	    	     
	     if(layoutTypeElement != null && !$(layoutTypeElement).hasClass("UITableColumnContainer")) {
	      /* ===============================CASE ROW LAYOUT================================ */
	      var rowContainer = jTarget.find(".UIRowContainer").first();
	      var listComponent = rowContainer.children("div").filter(function() {
	    	  return this.className != "DragAndDropPreview" && this != dragObject;
	      });	      	      
	      
	      jDragObj.data("listComponentInTarget", listComponent);
	      var insertPosition = _module.PortalDragDrop.findInsertPosition(listComponent, "row", ey);
	      if (jDragObj.data("foundTargetObject") === jDragObj.data("lastFoundTargetObject") && 
	    		  insertPosition === jDragObj.data("foundIndex")) return;	      
	      jDragObj.data("foundIndex", insertPosition);
	      
	      /* Insert preview block */
	      if(insertPosition >= 0) {
	        rowContainer[0].insertBefore(previewBlock, listComponent[insertPosition]);
	      } else {
	        rowContainer[0].appendChild(previewBlock);
	      }
	    } else {
	      /* ===============================CASE COLUMN LAYOUT================================ */
	      var trContainer = jTarget.find(".TRContainer").first();
	      var listComponent = trContainer.children("td").filter(function() {
	    	 return this != previewBlock.parentNode && this != dragObject.parentNode; 
	      });
	      
	      var insertPosition = _module.PortalDragDrop.findInsertPosition(listComponent, "column", ex);
	      if (jDragObj.data("foundTargetObject") === jDragObj.data("lastFoundTargetObject") && 
	    		  insertPosition === jDragObj.data("foundIndex")) return;
	      jDragObj.data("foundIndex", insertPosition);
	      
	      /* Insert preview block */
	      if(insertPosition >= 0) {
	          trContainer[0].insertBefore(dragObject.parentNode, listComponent[insertPosition]);
	       } else {
	          trContainer[0].appendChild(dragObject.parentNode);
	       }
	    }

	    var dragParent = $(dragObject).parent();
	    if (dragParent.children("div").length == 1 && !dragParent.hasClass("EmptyContainer")) {
	    	dragParent.addClass("EmptyContainer");
	    }
	  };

	  var dropCallback = function(x, y, clientX, clientY, e) {
	    var hasChanged = true;
	    var dragObject = this, jDragObj = $(this);
		//When press esc key, we want to cancel the dragdrop
	  	if (e.which === 27) {
	  		hasChanged = false;
	  	}
	  	//When dragObject is outside 
	  	if (!jDragObj.data("foundTargetObject")) {
	  		jDragObj.data("foundTargetObject", jDragObj.data("lastFoundTargetObject"));
	  	}
	  	
	    var targetElement = jDragObj.data("foundTargetObject");
	    var foundIndex = jDragObj.data("foundIndex");
	    if(!targetElement || foundIndex == undefined) {
	       hasChanged = false;
	    }
//			 Case RowContainer : When dragobject is next to preview object (position is not changed)
//		    Case ColumnContainer : When dragObject.parent's lastSibling doesn't change
	  	if(!dragObject.isAddingNewly) {
	      if (dragObject.parentNode.tagName.toLowerCase() == "td") {
	        //Column Container
	        var backupParentSibling = jDragObj.data("backupParentSibling"); 
	        var currSibling = jDragObj.parent().next("td");
	        if ((backupParentSibling.length == 0 && currSibling.length == 0) || 
	        		currSibling[0] == backupParentSibling[0]) {
	          hasChanged = false;          
	        }
	      } else {
	        //RowContainer
	    	var next = jDragObj.next("div.DragAndDropPreview");
	    	var prev = jDragObj.prev("div.DragAndDropPreview");
	        if (next.length || prev.length) {
	          hasChanged = false;
	        }
	      }
	  	}

	    if(e.which !== 27) {
	    	_module.PortalDragDrop.doDropCallback(dragObject);
	    } else {
	      //When click ESC, restore dragObject's last position
	      if (dragObject.parentNode && dragObject.parentNode.tagName.toLowerCase() == "td") {
	        var tdNode = dragObject.parentNode;
	        var lastSibling = jDragObj.data("backupParentSibling");
	        if (lastSibling.length == 0) {
	          tdNode.parentNode.appendChild(tdNode);
	        } else {
	          tdNode.parentNode.insertBefore(tdNode, lastSibling[0]);
	        }
	      }
	      
	      if(dragObject.isAddingNewly) {
			dragObject.parentNode.removeChild(dragObject);
		  }
	    }
	    
	    if(!dragObject.isAddingNewly) {
	    	jDragObj.find(".EDITION-BLOCK").last().hide();
	    	$.extend(dragObject.style, jDragObj.data("origDragObjectStyle"));
	    }
	    
	    if(previewBlock) previewBlock.parentNode.removeChild(previewBlock);
	    previewBlock = null;
	    
	    eXo.portal.isInDragging = false;
	    if (hasChanged) {
	    	_module.PortalComposer.toggleSaveButton();
	    }
	    
	    jDragObj.removeData();
	  };
	  
	  dragClasses = dragClasses.join(",.");
	  $("." + dragClasses).each(function() {
		  var dragBlock = this;	  
		  var clickObject = $(this).find(".DragControlArea").last();

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
	  var srcElement = dragObject, jDragObj = $(dragObject);
	  var PortalDragDrop = _module.PortalDragDrop;
	  var targetElement = jDragObj.data("foundTargetObject");
	  var foundIndex = jDragObj.data("foundIndex")
	  
	  if(!targetElement || foundIndex == undefined) {
	  	if(dragObject.isAddingNewly) {
		    dragObject.parentNode.removeChild(dragObject);
	  	}
	  	dragObject.style.width = "auto";
	  	return;
	  }
	  
	  if(!srcElement.isAddingNewly && (foundIndex != undefined)) {
	    if(PortalDragDrop.layoutTypeElementNode != null) {
	      _module.PortalDragDrop.divRowContainerAddChild(srcElement, targetElement, foundIndex);
	    }
	  }

	  if(srcElement.isAddingNewly) {
	    jDragObj.remove();
	  }
	  
	  var params = [
	    {name: "srcID", value: (srcElement.id.replace(/^UIPortlet-/, ""))},
	    {name: "targetID", value: targetElement.id.replace(/^.*-/, "")},
	    {name: "insertPosition", value: foundIndex},
	    {name: "isAddingNewly", value: srcElement.isAddingNewly}
	  ] ;

	  // Modified by Philippe : added callback function
	  ajaxGet(eXo.env.server.createPortalURL("UIPortal", "MoveChild", true, params));
	},

	/**
	 * Return an array of droppable target objects
	 * 
	 * @param the dragging object
	 */
	findDropableTargets : function(dragBlock) {
	  var dropableTargets = new Array();
	  var jDragObj = $(dragBlock);

      if (jDragObj.hasClass("UIColumnContainer")) {
	    var uiTableContainer = jDragObj.closest(".UITableColumnContainer");
	    dropableTargets.push(uiTableContainer[0]);
	    return dropableTargets;
	  }

      var toolPanel = $("#UIPortalToolPanel");
	  var uiPortal = toolPanel.find(".UIPortal");
	  if(uiPortal.length) {
	    dropableTargets.push(uiPortal[0]);
	  } else {
		var uiPage = toolPanel.find(".UIPage");
	    dropableTargets.push(uiPage[0]);
	  }
	  
	  var uiContainers = toolPanel.find(".UIContainer") ;
	  uiContainers.each(function() {
		 var jCont = $(this);
		 if (!jCont.closest(jDragObj).length && 
				 !jCont.hasClass("ProtectedContainer") &&
				 !jCont.hasClass("UITableColumnContainer")) {
			 dropableTargets.push(this) ;
		 }  
	  });
	  return dropableTargets;
	},

	scrollOnDrag : function(dragObject, e) {
      var jWin = $(window);
	  var workspaceHeight = $("#UIWorkingWorkspace").height();
	  var browserHeight = jWin.height();
	  if(workspaceHeight <= browserHeight) return;
	  var mouseY = e.clientY;
	  var deltaTop = mouseY - (Math.round(browserHeight * 5/6));
	  var deltaBottom = mouseY - (Math.round(browserHeight/6));
	  
	  var scrollTop = jWin.scrollTop();
	  if(deltaTop > 0) {		  
		  jWin.scrollTop(scrollTop + deltaTop - 5);
	  }
	  
	  if(deltaBottom < 0 && scrollTop > 0) {
		  jWin.scrollTop(scrollTop + deltaBottom);
	  }	  
	},

	/**
	 * Return a most suiable position among the <code>components</code> objects
	 * that the dragging object should be at
	 * 
	 * @param layout {string} the layout type which is "row" or "column"
	 */
	findInsertPosition : function(components, layout, mousePos) {
	   var browser = base.Browser;
	   if (layout == "row") {
	      for (var i = 0; i < components.length; i++) {
	    	  var componentTop = $(components[i]).offset().top;
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
		var previewBlock = document.createElement("div");
		previewBlock.className = "DragAndDropPreview";
		previewBlock.id = "DragAndDropPreview";
		return previewBlock;
	},

	findTarget : function(dragObject, mousexInPage, mouseyInPage) {
	  var dropableTargets = $(dragObject).data("dropableTargets");
	  if(!dropableTargets) return null;
	  
	  var foundTarget = null;
	  var len = dropableTargets.length;
	  for(var i = 0 ; i < len ; i++) {
	    var ele =  dropableTargets[i];

	    if(dragObject != ele && this.isIn(mousexInPage, mouseyInPage, ele)) {
	      if(foundTarget == null) {
	        foundTarget = ele;
	      } else {
	        if($(ele).closest(foundTarget).length > 0) {
	          foundTarget = ele;
	        }
	      } 
	    }
	  }
	 	
	  return foundTarget;
	},
	  
	isIn : function(x, y, component) {
	  var browser = base.Browser;
	  var offset = $(component).offset();
	  var componentLeft = offset.left;
	  var componentRight = componentLeft + component.offsetWidth ;
	  var componentTop = offset.top;
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
	  var listComponent = $(srcElement).data("listComponentInTarget");
	  var uiRowContainer = $(targetElement).find(".UIRowContainer").first();
	  srcElement.style.width = "auto" ;
	  
	  var parentNode = srcElement.parentNode;
	  if(insertPosition >= 0) {
	    uiRowContainer[0].insertBefore(srcElement, listComponent[insertPosition]);
	  } else {
	    uiRowContainer[0].appendChild(srcElement);
	  }
		
	  uiRowContainer.removeClass("EmptyContainer");
	  
	  if(parentNode.nodeName.toLowerCase() == "td") {
	  	$(parentNode).remove();
	  }
	}
} ;

_module.PortalDragDrop = eXo.portal.PortalDragDrop;