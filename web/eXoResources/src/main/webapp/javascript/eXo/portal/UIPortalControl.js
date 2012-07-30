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

var uiPortalControl = {

  /**
   * Collapse tree, use for Navigation Tree
   * 
   * @param {Object}
   *          selectedElement first object of tree
   */
  collapseTree : function(selectedElement) {

    var ancest = $(selectedElement).parent().closest(".Node");
    var childrenCont = ancest.find("div.ChildrenContainer").eq(0);
    var newHTML = "<div onclick=\""
        + childrenCont.attr("actionLink")
        + "\" class=\"ExpandIcon\">" + selectedElement.innerHTML + "</div>";
    ancest.html(newHTML);
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
};

_module.UIPortalControl = uiPortalControl;