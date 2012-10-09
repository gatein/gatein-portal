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
 * Manages the mask layer component
 */

(function($, base, uiPopup) {
	eXo.core.UIMaskLayer = {
	
	  /**
	   * Creates a transparent mask with "wait" cursor type
	   */
	  createTransparentMask : function(position) {
	    var browser = base.Browser;
	    var ajaxLoading = $("#AjaxLoadingMask");
	    var maskLayer = $(eXo.core.UIMaskLayer.createMask("UIPortalApplication",
	        ajaxLoading[0], 0, position));
	    browser.addOnScrollCallback("5439383", eXo.core.UIMaskLayer.setPosition);
	    ajaxLoading.hide();
	    maskLayer.fadeTo(0, 0);
	    maskLayer.css("backgroundColor", "white");
	    maskLayer.css("cursor", "wait");
	    return maskLayer[0];
	  },
	
	  /*
	   * Display ajax loading and set opacity for mask layer
	   */
	  showAjaxLoading : function(mask) {
	    var ajaxLoading = document.getElementById("AjaxLoadingMask");
	    ajaxLoading.style.display = "block";
	    $(mask).fadeTo(0, 0.3);
	    mask.style.backgroundColor = "black";
	  },
	
	  /**
	   * Hides the transparent mask To avoid some bugs doesn't "really" hides it,
	   * only reduces its size to 0x0 px
	   */
	  removeTransparentMask : function() {
	    var mask = $("#TransparentMaskLayer");
	    if (mask.length) {
	      mask.height(0);
	      mask.width(0);
	    }
	  },
	  /**
	   * Removes both transparent and loading masks
	   */
	  removeMasks : function(maskLayer) {
	    eXo.core.UIMaskLayer.removeTransparentMask();
	    eXo.core.UIMaskLayer.removeMask(maskLayer);
	  },
	
	  /**
	   * Creates and returns the dom element that contains the mask layer, with
	   * these parameters . the mask layer is a child of blockContainerId . object .
	   * the opacity in % . the position between : TOP-LEFT, TOP-RIGHT, BOTTOM-LEFT,
	   * BOTTOM-RIGHT, other value will position to center The returned element has
	   * the following html attributes : . className = "MaskLayer" ; . id =
	   * "MaskLayer" ; . style.display = "block" ; . maxZIndex = 2 ; . style.zIndex =
	   * maskLayer.maxZIndex ; . style.top = "0px" ; . style.left = "0px" ;
	   */
	  createMask : function(blockContainerId, object, opacity, position) {
	    try {
	      var browser = base.Browser;
	      if (typeof (blockContainerId) == "string")
	        blockContainerId = $("#" + blockContainerId);
	      var blockContainer = $(blockContainerId);
	      var maskLayer = $(document.createElement("div"));
	
	      this.object = object;
	      this.blockContainer = blockContainer[0];
	      this.position = position;
	
	      var subMask = document.getElementById("MaskLayer"); 
	      if (subMask) {
	        /*
	         * minh.js.exo fix for double id : MaskLayer reference with method
	         * eXo.core.UIMaskLayer.doScroll()
	         */
	        subMask.id = "subMaskLayer";
	      }
	      blockContainer.append(maskLayer);
	      maskLayer.addClass("MaskLayer").attr("id", "MaskLayer");
	      maskLayer.data("maxZIndex", uiPopup.zIndex + 1); // 3 ;
	
	      var offsetParent = maskLayer.offsetParent();
	      var offset = offsetParent.offset();
	      if (offsetParent.length && offset.left != 0
	          && offsetParent.top != 0) {
	        maskLayer.width(offsetParent[0].offsetWidth);
	        maskLayer.height(offsetParent[0].offsetHeight);
	      } else {
	        maskLayer.width($(window).width());
	        maskLayer.height($(window).height());
	      }
	
	      maskLayer.css("top", "0px");
	      maskLayer.css("left", "0px");
	      maskLayer.css("zIndex", maskLayer.data("maxZIndex"));
	
	      if (opacity) {
	        maskLayer.fadeTo(0, opacity/100);
	      }
	
	      if (object != null) {
	        var tempNextSibling = document.createElement("span");
	        if (object.nextSibling) {
	          object.parentNode.insertBefore(tempNextSibling, object.nextSibling);
	        } else {
	          object.parentNode.appendChild(tempNextSibling);
	        }
	        maskLayer.data("nextSiblingOfObject", tempNextSibling);
	
	        object.style.zIndex = maskLayer.data("maxZIndex");
	        object.style.display = "block";
	
	        blockContainer.append(object);
	
	        eXo.core.UIMaskLayer.setPosition();
	        if (eXo.core.I18n.isLT()) {
	          if ((blockContainer[0].offsetWidth > object.offsetLeft
	              + object.offsetWidth)
	              && (position == "TOP-RIGHT") || (position == "BOTTOM-RIGHT")) {
	            object.style.left = blockContainer[0].offsetWidth - object.offsetWidth
	                + "px";
	          }
	        }
	        eXo.core.UIMaskLayer.doScroll();
	      }
	      if (maskLayer[0].parentNode.id == "UIPage") {
	        eXo.core.UIMaskLayer.enablePageDesktop(false);
	      }
	    } catch (err) {
	      alert(err);
	    }
	    browser.addOnResizeCallback(maskLayer[0].id,
	        eXo.core.UIMaskLayer.resizeMaskLayer);
	    return maskLayer[0];
	  },
	
	  /*
	   * Tung.Pham added
	   */
	  // TODO: Temporary use
	  createMaskForFrame : function(blockContainerId, object, opacity) {
	    try {
	      var browser = base.Browser;
	      if (typeof (blockContainerId) == "string")
	        blockContainerId = $("#" + blockContainerId);
	      var blockContainer = $(blockContainerId);
	      var maskLayer = $(document.createElement("div"));
	      blockContainer.append(maskLayer);
	      maskLayer.addClass("MaskLayer");
	      maskLayer.attr("id", object.id + "MaskLayer");
	      maskLayer.data("maxZIndex", 3);
	      maskLayer.width(blockContainer[0].offsetWidth);
	      maskLayer.height(blockContainer[0].offsetHeight);
	
	      window
	          .setTimeout(
	              function() {
	                var temp = blockContainer[0].parentNode;
	                var parentOfBlockContainer;
	                do {
	                  parentOfBlockContainer = temp;
	                  temp = temp.parentNode;
	                } while (temp
	                    && $(parentOfBlockContainer).css("position") === "static");
	
	                maskLayer.css("top", browser.findPosYInContainer(
	                    blockContainer[0], parentOfBlockContainer)
	                    + "px");
	                maskLayer.css("left", browser.findPosXInContainer(
	                    blockContainer[0], parentOfBlockContainer)
	                    + "px");
	              }, 200);
	
	      maskLayer.css("zIndex", maskLayer.data("maxZIndex"));
	      if (opacity) {
	    	  maskLayer.fadeTo(0, opacity/100);
	      }
	
	      if (object != null) {
	        var tempNextSibling = document.createElement("span");
	        if (object.nextSibling) {
	          object.parentNode.insertBefore(tempNextSibling, object.nextSibling);
	        } else {
	          object.parentNode.appendChild(tempNextSibling);
	        }
	        maskLayer.data("nextSiblingOfObject", tempNextSibling);
	
	        object.style.zIndex = maskLayer.data("maxZIndex") + 1;
	        object.style.display = "block";
	
	        blockContainer.append(object);
	      }
	
	    } catch (err) {
	    }
	    return maskLayer[0];
	  },
	
	  /**
	   * Moves the position of the mask layer to follow a scroll
	   */
	
	  doScroll : function() {
	    var maskLayer = document.getElementById("MaskLayer");
	    if (maskLayer) {
	      var offsetParent = maskLayer.offsetParent;
	      var offset = $(offsetParent).offset();
	      if (offsetParent && offset.left != 0 || offset.top != 0) {
	        maskLayer = document.getElementById("subMaskLayer");
	        if (!maskLayer)
	          return;
	      }
	      if (document.documentElement && document.documentElement.scrollTop) {
	        maskLayer.style.top = document.documentElement.scrollTop + "px";
	      } else {
	        maskLayer.style.top = document.body.scrollTop + "px";
	      }
	      setTimeout(function() {eXo.core.UIMaskLayer.doScroll();}, 1);
	    } else if (document.getElementById("subMaskLayer")) {
	      var subMaskLayer = document.getElementById("subMaskLayer");
	      subMaskLayer.id = "MaskLayer";
	      eXo.core.UIMaskLayer.doScroll();
	    }
	  },
	
	  /**
	   * Set the position of the mask layer, depending on the position attribute of
	   * UIMaskLayer position is between : TOP-LEFT, TOP-RIGHT, BOTTOM-LEFT,
	   * BOTTOM-RIGHT, other value will position to center
	   */
	  setPosition : function() {
	    var UIMaskLayer = eXo.core.UIMaskLayer;
	    var object = UIMaskLayer.object;
	    var blockContainer = UIMaskLayer.blockContainer;
	    var position = UIMaskLayer.position;
	    object.style.position = "absolute";
	
	    var left;
	    var top;
	    var topPos;
	    if (document.documentElement && document.documentElement.scrollTop) {
	      topPos = document.documentElement.scrollTop;
	    } else {
	      topPos = document.body.scrollTop;
	    }
	
	    var browserHeight = $(window).height();
	    switch (position) {
	    case "TOP-LEFT":
	      top = topPos;
	      left = 0;
	      break;
	    case "TOP-RIGHT":
	      top = topPos;
	      left = blockContainer.offsetWidth - object.offsetWidth;
	      break;
	    case "TOP-CENTER":
	      top = topPos;
	      left = (blockContainer.offsetWidth - object.offsetWidth) / 2;
	      break;
	    case "BOTTOM-LEFT":
	      left = 0;
	      top = browserHeight - object.offsetHeight + topPos;
	      break;
	    case "BOTTOM-CENTER":
	      left = (blockContainer.offsetWidth - object.offsetWidth) / 2;
	      top = browserHeight - object.offsetHeight + topPos;
	      break;
	    case "BOTTOM-RIGHT":
	      left = blockContainer.offsetWidth - object.offsetWidth;
	      top = browserHeight - object.offsetHeight + topPos;
	      break;
	    default:
	      // By default, the mask layer always displays at the center
	      left = (blockContainer.offsetWidth - object.offsetWidth) / 2;
	      top = (browserHeight - object.offsetHeight) / 2 + topPos;
	    }
	
	    if ((top + object.offsetHeight) > topPos + $(window).height()) {
	      top = topPos + browserHeight - object.offsetHeight;
	    }
	
	    object.style.left = left + "px";
	    object.style.top = top + "px";
	  },
	  /**
	   * Removes the mask layer from the DOM
	   */
	  removeMask : function(maskLayer) {
	    if (maskLayer) {
	      maskLayer.nextSibling.style.display = "none";
	
	      var nextSiblingOfObject = $(maskLayer).data("nextSiblingOfObject");
	      nextSiblingOfObject.parentNode.insertBefore(
	          maskLayer.nextSibling, nextSiblingOfObject);
	      nextSiblingOfObject.parentNode
	          .removeChild(nextSiblingOfObject);
	
	      $(maskLayer).remove();
	    }
	  },
	
	  /*
	   * Added by Tan Pham Fix for bug : In FF3, can action on dockbar when edit
	   * node
	   */
	  enablePageDesktop : function(enabled) {
	    var pageDesktop = document.getElementById("UIPageDesktop");
	    if (pageDesktop) {
	      if (enabled) {
	        pageDesktop.style.zIndex = "";
	      } else {
	        pageDesktop.style.zIndex = "-1";
	      }
	    }
	  },
	
	  resizeMaskLayer : function() {
	    var maskLayer = document.getElementById("MaskLayer");
	    if (maskLayer) {
	      var offsetParent = maskLayer.offsetParent;
	      if (offsetParent) {
	    	var offset = $(offsetParent).offset();
	    	if (offset.top != 0 || offset.left != 0) {
	    		maskLayer = document.getElementById("subMaskLayer");
	    		if (!maskLayer)
	    			return;
	    		offsetParent = maskLayer.offsetParent;    		
	    	}
	      }
	    }
	
	    var jWin = $(window);
	    if (maskLayer && offsetParent) {
	      var offset = $(offsetParent).offset();
	      if (offset.left == 0 && offset.top == 0) {
	    	  maskLayer.style.width = jWin.width() + "px";
	    	  maskLayer.style.height = jWin.height() + "px";    	  
	      }
	    }
	  }
	};
	
	return  eXo.core.UIMaskLayer;
})($, base, uiPopup);