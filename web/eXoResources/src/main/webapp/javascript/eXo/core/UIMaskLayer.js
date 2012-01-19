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
eXo.core.UIMaskLayer = {

  /**
   * Creates a transparent mask with "wait" cursor type
   */
  createTransparentMask : function(position) {
    var browser = eXo.core.Browser;
    var ajaxLoading = document.getElementById("AjaxLoadingMask");
    var maskLayer = eXo.core.UIMaskLayer.createMask("UIPortalApplication",
        ajaxLoading, 0, position);
    browser.addOnScrollCallback("5439383", eXo.core.UIMaskLayer.setPosition);
    ajaxLoading.style.display = "none";
    browser.setOpacity(maskLayer, 0);
    maskLayer.style.backgroundColor = "white";
    maskLayer.style.cursor = "wait";
    return maskLayer;
  },

  /*
   * Display ajax loading and set opacity for mask layer
   */
  showAjaxLoading : function(mask) {
    var ajaxLoading = document.getElementById("AjaxLoadingMask");
    ajaxLoading.style.display = "block";
    eXo.core.Browser.setOpacity(mask,30);
    mask.style.backgroundColor = "black";
  },

  /**
   * Hides the transparent mask To avoid some bugs doesn't "really" hides it,
   * only reduces its size to 0x0 px
   */
  removeTransparentMask : function() {
    var mask = document.getElementById("TransparentMaskLayer");
    if (mask) {
      mask.style.height = "0px";
      mask.style.width = "0px";
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
      var browser = eXo.core.Browser;
      if (typeof (blockContainerId) == "string")
        blockContainerId = document.getElementById(blockContainerId);
      var blockContainer = blockContainerId;
      var maskLayer = document.createElement("div");

      this.object = object;
      this.blockContainer = blockContainer;
      this.position = position;

      if (document.getElementById("MaskLayer")) {
        /*
         * minh.js.exo fix for double id : MaskLayer reference with method
         * eXo.core.UIMaskLayer.doScroll()
         */
        document.getElementById("MaskLayer").id = "subMaskLayer";
      }
      blockContainer.appendChild(maskLayer);
      maskLayer.className = "MaskLayer";
      maskLayer.id = "MaskLayer";
      maskLayer.maxZIndex = eXo.webui.UIPopup.zIndex + 1; // 3 ;

      var offsetParent = maskLayer.offsetParent;
      if (offsetParent && browser.findPosX(offsetParent) != 0
          && browser.findPosY(offsetParent) != 0) {
        maskLayer.style.width = offsetParent.offsetWidth + "px";
        maskLayer.style.height = offsetParent.offsetHeight + "px";
      } else {
        maskLayer.style.width = browser.getBrowserWidth() + "px";
        maskLayer.style.height = browser.getBrowserHeight() + "px";
      }

      maskLayer.style.top = "0px";
      maskLayer.style.left = "0px";
      maskLayer.style.zIndex = maskLayer.maxZIndex;

      if (opacity) {
        browser.setOpacity(maskLayer, opacity);
      }

      if (object != null) {
        var tempNextSibling = document.createElement("span");
        if (object.nextSibling) {
          object.parentNode.insertBefore(tempNextSibling, object.nextSibling);
        } else {
          object.parentNode.appendChild(tempNextSibling);
        }
        maskLayer.nextSiblingOfObject = tempNextSibling;

        // object.style.zIndex = maskLayer.maxZIndex + 1 ;
        object.style.zIndex = maskLayer.maxZIndex;
        object.style.display = "block";

        blockContainer.appendChild(object);

        eXo.core.UIMaskLayer.setPosition();
        if (eXo.core.I18n.isLT()) {
          if ((blockContainer.offsetWidth > object.offsetLeft
              + object.offsetWidth)
              && (position == "TOP-RIGHT") || (position == "BOTTOM-RIGHT")) {
            object.style.left = blockContainer.offsetWidth - object.offsetWidth
                + "px";
          }
        }
        eXo.core.UIMaskLayer.doScroll();
      }
      if (maskLayer.parentNode.id == "UIPage") {
        eXo.core.UIMaskLayer.enablePageDesktop(false);
      }
    } catch (err) {
      alert(err);
    }
    browser.addOnResizeCallback(maskLayer.id,
        eXo.core.UIMaskLayer.resizeMaskLayer);
    return maskLayer;
  },

  /*
   * Tung.Pham added
   */
  // TODO: Temporary use
  createMaskForFrame : function(blockContainerId, object, opacity) {
    try {
      var browser = eXo.core.Browser;
      if (typeof (blockContainerId) == "string")
        blockContainerId = document.getElementById(blockContainerId);
      var blockContainer = blockContainerId;
      var maskLayer = document.createElement("div");
      blockContainer.appendChild(maskLayer);
      maskLayer.className = "MaskLayer";
      maskLayer.id = object.id + "MaskLayer";
      maskLayer.maxZIndex = 3;
      maskLayer.style.width = blockContainer.offsetWidth + "px";
      maskLayer.style.height = blockContainer.offsetHeight + "px";

      window
          .setTimeout(
              function() {
                var temp = blockContainer.parentNode;
                var parentOfBlockContainer;
                do {
                  parentOfBlockContainer = temp;
                  temp = temp.parentNode;
                } while (temp
                    && eXo.core.DOMUtil.getStyle(parentOfBlockContainer,
                        "position") === "static");

                maskLayer.style.top = browser.findPosYInContainer(
                    blockContainer, parentOfBlockContainer)
                    + "px";
                maskLayer.style.left = browser.findPosXInContainer(
                    blockContainer, parentOfBlockContainer)
                    + "px";
              }, 200);

      maskLayer.style.zIndex = maskLayer.maxZIndex;
      if (opacity) {
        browser.setOpacity(maskLayer, opacity);
      }

      if (object != null) {
        var tempNextSibling = document.createElement("span");
        if (object.nextSibling) {
          object.parentNode.insertBefore(tempNextSibling, object.nextSibling);
        } else {
          object.parentNode.appendChild(tempNextSibling);
        }
        maskLayer.nextSiblingOfObject = tempNextSibling;

        object.style.zIndex = maskLayer.maxZIndex + 1;
        object.style.display = "block";

        blockContainer.appendChild(object);
      }

    } catch (err) {
    }
    return maskLayer;
  },

  /**
   * Moves the position of the mask layer to follow a scroll
   */

  doScroll : function() {
    var maskLayer = document.getElementById("MaskLayer");
    if (maskLayer) {
      var offsetParent = maskLayer.offsetParent, browser = eXo.core.Browser;
      if (offsetParent && browser.findPosX(offsetParent) != 0
          || browser.findPosY(offsetParent) != 0) {
        maskLayer = document.getElementById("subMaskLayer");
        if (!maskLayer)
          return;
      }
      if (document.documentElement && document.documentElement.scrollTop) {
        maskLayer.style.top = document.documentElement.scrollTop + "px";
      } else {
        maskLayer.style.top = document.body.scrollTop + "px";
      }
      setTimeout("eXo.core.UIMaskLayer.doScroll()", 1);
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
    var browser = eXo.core.Browser;
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
      top = browser.getBrowserHeight() - object.offsetHeight + topPos;
      break;
    case "BOTTOM-CENTER":
      left = (blockContainer.offsetWidth - object.offsetWidth) / 2;
      top = browser.getBrowserHeight() - object.offsetHeight + topPos;
      break;
    case "BOTTOM-RIGHT":
      left = blockContainer.offsetWidth - object.offsetWidth;
      top = browser.getBrowserHeight() - object.offsetHeight + topPos;
      break;
    default:
      // By default, the mask layer always displays at the center
      left = (blockContainer.offsetWidth - object.offsetWidth) / 2;
      top = (browser.getBrowserHeight() - object.offsetHeight) / 2 + topPos;
    }

    if ((top + object.offsetHeight) > topPos + browser.getBrowserHeight()) {
      top = topPos + browser.getBrowserHeight() - object.offsetHeight;
    }

    object.style.left = left + "px";
    object.style.top = top + "px";
  },
  /**
   * Removes the mask layer from the DOM
   */
  removeMask : function(maskLayer) {
    if (maskLayer) {
      var parentNode = maskLayer.parentNode;
      maskLayer.nextSibling.style.display = "none";

      maskLayer.nextSiblingOfObject.parentNode.insertBefore(
          maskLayer.nextSibling, maskLayer.nextSiblingOfObject);
      maskLayer.nextSiblingOfObject.parentNode
          .removeChild(maskLayer.nextSiblingOfObject);

      maskLayer.nextSiblingOfObject = null;
      parentNode.removeChild(maskLayer);
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
      var offsetParent = maskLayer.offsetParent, browser = eXo.core.Browser;
      if (offsetParent
          && (browser.findPosX(offsetParent) != 0 || browser
              .findPosY(offsetParent) != 0)) {
        maskLayer = document.getElementById("subMaskLayer");
        if (!maskLayer)
          return;
        offsetParent = maskLayer.offsetParent;
      }
    }

    if (maskLayer && offsetParent && browser.findPosX(offsetParent) == 0
        && browser.findPosY(offsetParent) == 0) {
      maskLayer.style.width = browser.getBrowserWidth() + "px";
      maskLayer.style.height = browser.getBrowserHeight() + "px";
    }
  }
};
