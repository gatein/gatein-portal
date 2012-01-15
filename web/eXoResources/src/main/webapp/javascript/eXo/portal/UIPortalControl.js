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

eXo.portal.UIPortalControl = {

  /**
   * Collapse tree, use for Navigation Tree
   * 
   * @param {Object}
   *          selectedElement first object of tree
   */
  collapseTree : function(selectedElement) {
    var DOMUtil = eXo.core.DOMUtil;

    var parentNode = DOMUtil.findAncestorByClass(selectedElement, "Node");
    var childrenContainer = DOMUtil.findFirstDescendantByClass(parentNode,
        "div", "ChildrenContainer");
    var newHTML = "<div onclick=\""
        + childrenContainer.getAttribute("actionLink")
        + "\" class=\"ExpandIcon\">" + selectedElement.innerHTML + "</div>";
    parentNode.innerHTML = newHTML;
  },

  /**
   * Process enter key press
   * 
   * @param {Event}
   *          e this event
   * @param {String}
   *          executeScript javascript command to execute if enter key was
   *          pressed
   */
  onEnterPress : function(e, executeScript) {
    var e = window.event || e;
    var code;
    if (!e)
      e = window.event;
    if (e.keyCode)
      code = e.keyCode;
    else if (e.which)
      code = e.which;
    if (code == 13) {
      if (window.event) {
        e.returnValue = false;
      } else {
        e.preventDefault();
      }
      //TODO: Move the code serving for login form to executeScript param in caller side
      var uiPortalLoginFormAction = document
          .getElementById("UIPortalLoginFormAction");
      if (uiPortalLoginFormAction) {
        uiPortalLoginFormAction.onclick();
      } else {
        if (executeScript)
          eval(executeScript);
      }
    }
  },

  onKeyPress : function(e, executeScript, expectedCode)
  {
    if(!e)
    {
      e = window.event;
    }

    if(e.keyCode && e.keyCode == expectedCode)
    {
      if(window.event)
      {
        e.cancelBubble = true;
      }
      else
      {
        e.preventDefault();
      }

      if(executeScript)
      {
        eval(executeScript);
      }
    }
  }
}

/** ********* Vertical Scroll Manager *********** */
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
/*********** End Of Vertical Scroll Manager ************/