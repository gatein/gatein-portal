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

eXo.portal.VerticalScrollManager = {
  repeat : null,

  initScroll : function(clickedEle, isUp, step) {
    var DOMUtil = eXo.core.DOMUtil;
    var verticalScroll = eXo.portal.VerticalScrollManager;
    var container = DOMUtil.findAncestorByClass(clickedEle, "ItemContainer");
    var middleCont = DOMUtil.findFirstDescendantByClass(container, "div",
        "MiddleItemContainer");
    if (!middleCont.id)
      middleCont.id = "IC" + new Date().getTime()
          + Math.random().toString().substring(2);
    verticalScroll.scrollComponent(middleCont.id, isUp, step);
    document.onmouseup = verticalScroll.cancelScroll;
  },

  scrollComponent : function(id, isUp, step) {
    var verticalScroll = eXo.portal.VerticalScrollManager;
    var scrollComp = document.getElementById(id);
    if (isUp) {
      scrollComp.scrollTop -= step;
    } else {
      scrollComp.scrollTop += step;
    }
    if (verticalScroll.repeat) {
      verticalScroll.cancelScroll();
    }
    verticalScroll.repeat = setTimeout(
        "eXo.portal.VerticalScrollManager.scrollComponent('" + id + "'," + isUp
            + "," + step + ")", 100);
  },

  cancelScroll : function() {
    clearTimeout(eXo.portal.VerticalScrollManager.repeat);
    eXo.portal.VerticalScrollManager.repeat = null;
  }
}