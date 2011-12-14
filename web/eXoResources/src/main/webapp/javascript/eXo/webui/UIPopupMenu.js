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
 * Manages a popup menu
 */
eXo.webui.UIPopupMenu = {

  // Elements that must be hidden
  elementsToHide : [],
  // Elements that must be kept visible
  currentVisibleContainers : [],

  /**
   * initialize UIPopupMenu
   * 
   * @param {Object,
   *          String} popupMenu popup object
   * @param {Object}
   *          container
   * @param {Number}
   *          x
   * @param {Number}
   *          y
   */
  init : function(popupMenu, container, x, y) {
    this.superClass = eXo.webui.UIPopup;
    this.superClass.init(popupMenu, container.id);
  },
  /**
   * Set position to a popup
   * 
   * @param {Object}
   *          popupMenu
   * @param {Number}
   *          x x axis
   * @param {Number}
   *          y y axis
   * @param {boolean}
   *          isRTL right to left flag
   */
  setPosition : function(popupMenu, x, y, isRTL) {
    this.superClass.setPosition(popupMenu, x, y, isRTL);
  },
  /**
   * Set size to a popup
   * 
   * @param {Object}
   *          popupMenu
   * @param {Number}
   *          w width
   * @param {Number}
   *          h height
   */
  setSize : function(popup, w, h) {
    this.superClass.setSize(popupMenu, w, h);
  },

  pushVisibleContainer : function(containerId) {
    eXo.webui.UIPopupMenu.currentVisibleContainers.push(containerId);
  },

  popVisibleContainer : function() {
    eXo.webui.UIPopupMenu.currentVisibleContainers.pop();
  },

  pushHiddenContainer : function(containerId) {
    eXo.webui.UIPopupMenu.elementsToHide.push(containerId);
  },
  /**
   * Function called when an element (or more) must be hidden Sets a timeout to
   * time (or 100ms by default) after which the elements in elementsToHide will
   * be hidden
   */
  setCloseTimeout : function(time) {
    if (!time)
      time = 100;
    setTimeout("eXo.webui.UIPopupMenu.doOnMenuItemOut()", time);
  },
  /**
   * Adds an onCLick event to link elements If they are http links, changes the
   * url in the browser If they are javascript links, executes the javascript
   */
  createLink : function(menuItem, link) {
    if (link && link.href) {
      menuItem.onclick = function(e) {
        if (link.href.substr(0, 7) == "http://")
          window.location.href = link.href;
        else
          eval(link.href);
        if (!e)
          e = window.event;
        if (e.stopPropagation)
          e.stopPropagation();
        e.cancelBubble = true;
        return false;
      }
    }
  },

  /**
   * The callback function called when timeout is finished Hides the submenus
   * that are no longer pointed at
   */
  doOnMenuItemOut : function() {
    while (eXo.webui.UIPopupMenu.elementsToHide.length > 0) {
      var container = document
          .getElementById(eXo.webui.UIPopupMenu.elementsToHide.shift());
      if (container) {
        /*
         * It can happen that a submenu appears in both the "to-hide" list and
         * the "keep-visible" list This happens because when the mouse moves
         * from the border of an item to the content of this item, a mouseOut
         * Event is fired and the item submenu is added to the "to-hide" list
         * while it remains in the "keep-visible" list. Here, we check that the
         * item submenu doesn't appear in the "keep-visible" list before we hide
         * it
         */
        if (!eXo.webui.UIPopupMenu.currentVisibleContainers
            .contains(container.id)) {
          eXo.webui.UIPopupMenu.hide(container);
        }
      }
    }
  },

  showMenuItemContainer : function(menuItemContainer, x, y) {
    /*
     * menuItemContainer.style.display = "block" ; var x = menuItem.offsetWidth +
     * menuItem.offsetLeft; var y = menuItem.offsetTop; var rootX =
     * eXo.core.Browser.findPosX(menuItem); var rootY =
     * eXo.core.Browser.findPosY(menuItem); if (x +
     * menuItemContainer.offsetWidth + rootX >
     * eXo.core.Browser.getBrowserWidth()) { x -= (menuItemContainer.offsetWidth +
     * menuItem.offsetWidth); } if (y + menuItemContainer.offsetHeight + rootY >
     * eXo.core.Browser.getBrowserHeight()) { y -=
     * (menuItemContainer.offsetHeight - menuItem.offsetHeight); }
     */
    this.superClass.setPosition(menuItemContainer, x, y);
  },
  /**
   * Change object to hidden state
   * 
   * @param {Object}
   *          object to hide
   */
  hide : function(object) {
    if (typeof (object) == "string")
      object = document.getElementById(object);
    object.style.display = "none";
    object.style.visibility = "hidden";
  },
  /**
   * Change object to visibility state
   * 
   * @param {Object}
   *          object to hide
   */
  show : function(object) {
    if (typeof (object) == "string")
      object = document.getElementById(object);
    object.style.display = "block";
    object.style.visibility = "";
  }
}