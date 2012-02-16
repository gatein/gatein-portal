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

eXo.webui.UITabbedDashboard = {

  /**
   * A flag indicating that there is ongoing request to create new dashboard page. That helps
   * us to avoid concurrent dashboard creating requests coming from one user.
   *
   */
  inRequest : false,

  renameTabLabel : function(input)
  {
    var newLabel = input.val();
    if (newLabel && newLabel.length > 0)
    {
      var portletID = input.closest(".PORTLET-FRAGMENT").parent().attr("id");

      var href = eXo.env.server.portalBaseURL + "?portal:componentId=" + portletID;
      href += "&portal:type=action";
      href += "&portal:isSecure=false";
      href += "&uicomponent=UITabPaneDashboard";
      href += "&op=RenameTabLabel";
      href += "&objectId=" + input.attr("id");
      href += "&newTabLabel=" + encodeURIComponent(newLabel);
      window.location = href;
    }
  },

  showEditLabelInput : function(target, nodeName, currentLabel)
  {
    var jqObj = xj(target);

    var input = xj("<input>").attr({type : "text", id : nodeName, name : currentLabel, value : currentLabel, maxLength : 50});
    input.css("border", "1px solid #b7b7b7").css("width", (target.offsetWidth - 2) + "px");

    jqObj = jqObj.replaceWith(input);
    input.blur(function()
    {
      xj(this).replaceWith(jqObj);
    });

    input.keypress(function(e)
    {
      var keyNum = e.keyCode ? e.keyCode : e.which;
      if (keyNum == 13)
      {
        eXo.webui.UITabbedDashboard.renameTabLabel(xj(this));
      }
      else if (keyNum == 27)
      {
        xj(this).replaceWith(jqObj);
      }
    });

    input.closest(".UITab").addClass("EditTab");
    input.focus();
  },

  createTab : function(input)
  {
    if (this.inRequest)
    {
      return;
    }
    else
    {
      var label = input.val();
      if (label && label.length > 0)
      {
        var href = eXo.env.server.portalBaseURL + "?portal:componentId=" + input.attr("id");
        href += "&portal:type=action";
        href += "&portal:isSecure=false";
        href += "&uicomponent=UITabPaneDashboard";
        href += "&op=AddDashboard";
        href += "&objectId=" + encodeURIComponent(label);
        this.InRequest = true;
        window.location = href;
      }
    }
  },

  showAddTabInput : function(addButton)
  {
    var jqAddButton = xj(addButton);
    var tabs = jqAddButton.parent().children("div.UITab");

    var newTab = jqAddButton.prevAll("div.SelectedTab").eq(0).clone();
    newTab.insertBefore(jqAddButton);

    var portletID = jqAddButton.closest("div.PORTLET-FRAGMENT").parent().attr("id");

    var input = xj("<input>").attr({type : "text", id : portletID , maxlength : 50, value : "Tab_" + tabs.length});
    input.css({"border" : "1px solid #b7b7b7", "width" : "80px"});

    newTab.find("span").eq(0).replaceWith(input);
    input.next("a").attr("href", "#");

    input.blur(function()
    {
      xj(this).closest(".UITab").remove();
    });

    input.keypress(function(e)
    {
      var keyNum = e.keyCode ? e.keyCode : e.which;
      if (keyNum == 13)
      {
        eXo.webui.UITabbedDashboard.createTab(xj(this));
      }
      else if (keyNum == 27)
      {
        xj(this).closest(".UITab").remove();
      }
    });

    input.closest(".UITab").addClass("EditTab");
    input.focus();
  }
}