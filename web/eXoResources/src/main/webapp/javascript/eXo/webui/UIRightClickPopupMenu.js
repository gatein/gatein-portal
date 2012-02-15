/**
 * Copyright (C) 2009 eXo Platform SAS.
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

eXo.webui.UIRightClickPopupMenu = {
  /**
   * Add mouse down event handler
   * 
   * @param method
   *          handler method
   */
  addMouseDownHandler : function(method) {
    document.onmousedown = this.docMouseDownEvt;
    this.onMouseDownHandlers = method;
  },

  /**
   * Document mouse down event, it will cancel default behavior of browser and
   * process behavior in handler chain
   * 
   * @param {Event}
   *          evt
   */
  docMouseDownEvt : function(evt) {
    if (!evt)
      evt = window.event;
    evt.cancelBubble = true;

    if (eXo.webui.UIRightClickPopupMenu.onMouseDownHandlers == null)
      return;
    if (typeof (eXo.webui.UIRightClickPopupMenu.onMouseDownHandlers) == "string")
      eval(eXo.webui.UIRightClickPopupMenu.onMouseDownHandlers);
    else
      eXo.webui.UIRightClickPopupMenu.onMouseDownHandlers(evt);
    document.onmousedown = null;
  },

  /**
   * Initialize a UIRightClickPopupMenu object
   * 
   * @param contextMenuId
   *          identifier of a document object
   */
  init : function(contextMenuId) {
    var contextMenu = document.getElementById(contextMenuId);
    // TODO: Fix temporary for the problem Minimize window in Page Mode
    if (!contextMenu)
      return;

    contextMenu.onmousedown = function(e) {
      if (!e)
        e = window.event;
      e.cancelBubble = true;
    }

    var parentNode = contextMenu.parentNode;
    this.disableContextMenu(parentNode);
  },
  /**
   * Hide and disable mouse down event of context menu object
   * 
   * @param contextId
   *          identifier of context menu
   */
  hideContextMenu : function(contextId) {
    if (document.getElementById(contextId)) {
      document.getElementById(contextId).style.display = 'none';
      eXo.webui.UIRightClickPopupMenu.onMouseDownHandlers = null;
    }
  },
  /**
   * Disable default context menu of browser
   * 
   * @param comp
   *          identifier or document object
   */
  disableContextMenu : function(comp) {
    if (typeof (comp) == "string")
      comp = document.getElementById(comp);
    comp.onmouseover = function() {
      document.oncontextmenu = function() {
        return false
      };
    }
    comp.onmouseout = function() {
      document.oncontextmenu = function() {
        return true
      };
    }
  },

  /**
   * Prepare objectId for context menu Make ajaxPost request if needed
   * 
   * @param {Object}
   *          evt event
   * @param {Object}
   *          elemt document object that contains context menu
   */
  prepareObjectId : function(evt, elemt) {
    eXo.webui.UIRightClickPopupMenu.docMouseDownEvt(evt);
    var contextMenu = eXo.core.DOMUtil.findAncestorByClass(elemt,
        "UIRightClickPopupMenu");
    contextMenu.style.display = "none";
    var href = elemt.getAttribute('href');
    if (!href) {
      return;
    }
    if (href.indexOf("ajaxGet") != -1) {
      href = href.replace("ajaxGet", "ajaxPost");
      elemt.setAttribute('href', href);
    }
    if (href.indexOf("objectId") != -1 || !contextMenu.objId) {
      return;
    }
    var objId = encodeURIComponent(contextMenu.objId.replace(/'/g, "\\'"));

    if (href.indexOf("javascript") == -1) {
      elemt.setAttribute('href', href + "&objectId=" + objId);
      return;
    } else if (href.indexOf("window.location") != -1) {
      href = href.substr(0, href.length - 1) + "&objectId=" + objId + "'";
    } else if (href.indexOf("ajaxPost") != -1) {
      href = href.substr(0, href.length - 2) + "', 'objectId=" + objId + "')";
    } else {
      href = href.substr(0, href.length - 2) + "&objectId=" + objId + "')";
    }

    eval(href);
    if (evt && evt.preventDefault)
      evt.preventDefault();
    else
      window.event.returnValue = false;
    return false;
  },

  /**
   * Mouse click on element, If click is right-click, the context menu will be
   * shown
   * 
   * @param {Object}
   *          event
   * @param {Object}
   *          elemt clicked element
   * @param {String}
   *          menuId identifier of context menu will be shown
   * @param {String}
   *          objId object identifier in tree
   * @param {Array}
   *          params
   * @param {Number}
   *          opt option
   */
  clickRightMouse : function(event, elemt, menuId, objId, params, opt) {
    if (!event)
      event = window.event;
    eXo.webui.UIRightClickPopupMenu.docMouseDownEvt(event);
    var contextMenu = document.getElementById(menuId);
    contextMenu.objId = objId;
    if (!(((event.which) && (event.which == 2 || event.which == 3)) || ((event.button) && (event.button == 2)))) {
      contextMenu.style.display = 'none';
      return;
    }

    eXo.webui.UIRightClickPopupMenu
        .addMouseDownHandler("eXo.webui.UIRightClickPopupMenu.hideContextMenu('"
            + menuId + "');")

    if (params) {
      params = "," + params + ",";
      var items = contextMenu.getElementsByTagName("a");
      for ( var i = 0; i < items.length; i++) {
        if (params.indexOf(items[i].getAttribute("exo:attr")) > -1) {
          items[i].style.display = 'block';
        } else {
          items[i].style.display = 'none';
        }
      }
    }
    var customItem = eXo.core.DOMUtil.findFirstDescendantByClass(elemt, "div",
        "RightClickCustomItem");
    var tmpCustomItem = eXo.core.DOMUtil.findFirstDescendantByClass(
        contextMenu, "div", "RightClickCustomItem");
    if (tmpCustomItem) {
      if (customItem) {
        tmpCustomItem.innerHTML = customItem.innerHTML;
        tmpCustomItem.style.display = "inline";
      } else {
        tmpCustomItem.style.display = "none";
      }
    }
    /*
     * fix bug right click in IE7.
     */
    var fixWidthForIE7 = 0;
    var UIWorkingWorkspace = document.getElementById("UIWorkingWorkspace");
    if (eXo.core.Browser.isIE7() && document.getElementById("UIDockBar")) {
      if (event.clientX > UIWorkingWorkspace.offsetLeft)
        fixWidthForIE7 = UIWorkingWorkspace.offsetLeft;
    }

    eXo.core.Mouse.update(event);
    eXo.webui.UIPopup.show(contextMenu);

    var ctxMenuContainer = eXo.core.DOMUtil.findFirstChildByClass(contextMenu,
        "div", "UIContextMenuContainer");
    var intTop = eXo.core.Mouse.mouseyInPage
        - (eXo.core.Browser.findPosY(contextMenu) - contextMenu.offsetTop);
    var intLeft = eXo.core.Mouse.mousexInPage
        - (eXo.core.Browser.findPosX(contextMenu) - contextMenu.offsetLeft)
        + fixWidthForIE7;
    if (eXo.core.I18n.isRT()) {
      // scrollWidth is width of browser scrollbar
      var scrollWidth = 16;
      if (eXo.core.Browser.getBrowserType() == "mozilla")
        scrollWidth = 0;
      intLeft = contextMenu.offsetParent.offsetWidth - intLeft + fixWidthForIE7
          + scrollWidth;
      var clickCenter = eXo.core.DOMUtil.findFirstDescendantByClass(
          contextMenu, "div", "ClickCenterBottom");
      if (clickCenter) {
        var clickCenterWidth = clickCenter ? parseInt(xj(clickCenter).css("marginRight")) : 0;
        intLeft += (ctxMenuContainer.offsetWidth - 2 * clickCenterWidth);
      }
    }

    switch (opt) {
    case 1:
      intTop -= ctxMenuContainer.offsetHeight;
      break;
    case 2:
      break;
    case 3:
      break;
    case 4:
      break;
    default:
      // if it isn't fit to be showed down BUT is fit to to be showed up
      if ((eXo.core.Mouse.mouseyInClient + ctxMenuContainer.offsetHeight) > eXo.core.Browser
          .getBrowserHeight()
          && (intTop > ctxMenuContainer.offsetHeight)) {
        intTop -= ctxMenuContainer.offsetHeight;
      }
      break;
    }

    if (eXo.core.I18n.isLT()) {
      // move context menu to center of screen to fix width
      contextMenu.style.left = eXo.core.Browser.getBrowserWidth() * 0.5 + "px";
      ctxMenuContainer.style.width = "auto";
      ctxMenuContainer.style.width = ctxMenuContainer.offsetWidth + 2 + "px";
      // end fix width
      // need to add 1 more pixel because IE8 will dispatch onmouseout event to
      // contextMenu.parent
      contextMenu.style.left = (intLeft + 1) + "px";
    } else {
      // move context menu to center of screen to fix width
      contextMenu.style.right = eXo.core.Browser.getBrowserWidth() * 0.5 + "px";
      ctxMenuContainer.style.width = "auto";
      ctxMenuContainer.style.width = ctxMenuContainer.offsetWidth + 2 + "px";
      // end fix width
      contextMenu.style.right = intLeft + "px";
    }
    ctxMenuContainer.style.width = ctxMenuContainer.offsetWidth + "px";
    // need to add 1 more pixel because IE8 will dispatch onmouseout event to
    // contextMenu.parent
    if ((eXo.core.Mouse.mouseyInClient + ctxMenuContainer.offsetHeight) <= eXo.core.Browser
        .getBrowserHeight()) {
      intTop += 1
    }
    contextMenu.style.top = intTop + "px";
  }
};

eXo.core.Mouse = {
  init : function (mouseEvent) {
    this.mousexInPage = null ;
    this.mouseyInPage = null ;

    this.mousexInClient = null ;
    this.mouseyInClient = null ;

    this.lastMousexInClient = null ;
    this.lastMouseyInClient = null ;

    this.deltax = null ;
    this.deltay = null ;
    if(mouseEvent != null) this.update(mouseEvent) ;
  },

  update : function(mouseEvent) {
    browser = eXo.core.Browser;

    this.mousexInPage = browser.findMouseXInPage(mouseEvent);
    this.mouseyInPage = browser.findMouseYInPage(mouseEvent);

    var x  =  browser.findMouseXInClient(mouseEvent) ;
    var y  =  browser.findMouseYInClient(mouseEvent) ;

    this.lastMousexInClient =  this.mousexInClient != null ? this.mousexInClient : x ;
    this.lastMouseyInClient =  this.mouseyInClient != null ? this.mouseyInClient : y ;

    this.mousexInClient = x ;
    this.mouseyInClient = y ;

    this.deltax = this.mousexInClient - this.lastMousexInClient ;
    this.deltay = this.mouseyInClient - this.lastMouseyInClient ;
  }
};