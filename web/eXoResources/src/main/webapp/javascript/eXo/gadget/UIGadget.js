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

(function($, base, common) {	
	var eXoGadget = {
	
	  /**
	   * Create a new Gadget
	   * @param {String} url local or remote path that contain gadget .xml file
	   * @param {String} id id of object contains this gadget (parent id)
	   * @param {String} metadata contain information of gadget
	   * @param {Object} userPref
	   * @param {String} view type of view (home, canvas, ...)
	   * @param {boolean} debug normal or debug mode (0, 1)
	   * @param {String} nocache value indicate cache or nocache at shindig level (0, 1)
	   */
	  createGadget : function(url, id, metadata, userPref, view, hostName, debug, nocache)
	  {
        var ampPlain = "&";
        var ampXhtml = "&amp;";
        // var amp = eXo.env.portal.urlEncoded ? ampXhtml : ampPlain;
        // pubsubURL will be always in plain text even for js require loader
        var amp = ampPlain;
	    window.gadgets = window.gadgets || {};
	    eXo.gadgets = window.gadgets;
	    gadgets.pubsubURL = hostName + '/js/gatein-container.js?c=1' + (debug ? amp + "debug=1": "") + (nocache ? amp +"nocache=1" : amp + "nocache=0");
	    var args = arguments;
		  window.require([gadgets.pubsubURL], function() {

        //Make sure that 2 modules of gadget container have been loaded already
        window.require(["eXo.gadget.Gadgets", "eXo.gadget.ExoBasedUserPrefStore"], function() {
          gadgets.container.setName(eXo.env.portal.containerName);
          gadgets.container.setLanguage(eXo.core.I18n.getLanguage());
          eXoGadget.createCallback.apply(window, args);
        });
		  });
	  },
	
	  createCallback : function(url, id, metadata, userPref, view, hostName, debug, nocache)
	  {
	    var gadget;
	    if (metadata != null)
	    {
	      // Check if gadget's height is not set and current view is canvas. By default, gadget's height is 800px
	      if (metadata.modulePrefs.height == 0 && view == 'canvas')
	      {
	        metadata.modulePrefs.height = "800px";
	      }
	      gadget = gadgets.container.createGadget({specUrl: url,height: metadata.modulePrefs.height, secureToken: metadata.secureToken, view: view});
	      gadget.metadata = metadata;
	    }
	    else
	    {
	      gadget = gadgets.container.createGadget({specUrl: url});
	    }
	    gadget.parentId = id;
	    gadget.debug = debug;
	    gadget.nocache = nocache;
	    gadget.serverBase_ = hostName;
	    
	    gadgets.container.addGadget(gadget);
	    // i use the internal var "gadget.userPrefs_" to not call the save on the server side
	    if (userPref != null)
	    {
	      gadget.userPrefs_ = userPref;
	    }
	    var gadgetBlock = document.getElementById(id);
	    gadgetBlock.innerHTML = "<div id='gadget_" + gadget.id + "' class='UIGadgetContent'> </div>";
	    gadgets.container.renderGadget(gadget);
	    var uiGadget = $(gadgetBlock).closest(".UIGadget");
	    if (uiGadget.length > 0)
	    {      
	      if (metadata && metadata.modulePrefs.title != null && metadata.modulePrefs.title.length > 0)
	      {
	    	  uiGadget.find(".GadgetTitle").html(metadata.modulePrefs.title);
	      }
	    }
	    //setup for pubsub mechanism
	    gadgets.pubsubrouter.init(function(id) {return url;}, {});
	  },
	
	  /**
	   * Initialize data of gadget such as title, style, etc
	   * @param {Object} uiGadget object need to init data
	   * @param {boolean} inDesktop use to realize UIDesktopPage or no
	   * @param {String} metadata metadata of gadget
	   */
	  init : function(uiGadget)
	  {
	    if (typeof (uiGadget) == "string") uiGadget = document.getElementById(uiGadget);
	    var gadget = $(uiGadget);
	    var portletFrag = gadget.closest(".PORTLET-FRAGMENT");
	
	    if (portletFrag.length == 0)
	    {
	      gadget.mouseover(function()
	      {
	        $(this).find("div.GadgetControl").css("visibility", "visible");
	        return false;
	      });
	
	      gadget.mouseout(function()
	      {
	        $(this).find("div.GadgetControl").css("visibility", "hidden").css("border", "none");
	        return false;
	      });
	    }
	    else
	    {
	      var gadgetControl = gadget.find("div.GadgetControl").eq(0);
	      var gadgetTitle = gadgetControl.find("span.GadgetTitle").eq(0);
	      gadgetControl.css("display", "block");
	      gadgetTitle.css("display", "block");      
	    }
	
            gadget.find(".CloseGadget, .MaximizeAction, .MinimizeAction, .EditGadget").on("mousedown touchstart", false);
	
	    gadget.find(".CloseGadget").on("click touchstart", function() {
	    	eXoGadget.deleteGadget(this);
	    });
	    gadget.find(".MaximizeAction").on("click touchstart", function() {
	    	eXoGadget.maximizeGadget(this);
	    });
	    gadget.find(".MinimizeAction").on("click touchstart", function() {
	    	eXoGadget.minimizeGadget(this);
	    });
	    gadget.find(".EditGadget").on("click touchstart", function() {
	    	eXoGadget.editGadget(gadget.attr("id"));
	    });
	    
	    if (!gadget.parent().hasClass("UIPageDesktop")) {
	      gadget.css("width", "auto");    	
	    } else {
	      gadget.css("position", "absolute");
	      var dragArea = gadget.find("div.GadgetDragHandleArea")[0];
	      if (gadget.css("z-index") < 0)
	      {
	        gadget.css("z-index", "0");
	      }
	      common.DragDrop.init(dragArea, uiGadget);
	
	      var desktopPage = $("#UIPageDesktop");
	      var offsetHeight = desktopPage.offsetHeight - uiGadget.offsetHeight;
	      var offsetWidth = desktopPage.offsetWidth - uiGadget.offsetWidth;
	      var dragPosX = uiGadget.offsetLeft;
	      var dragPosY = uiGadget.offsetTop;
	
	      if (dragPosX < 0)
	      {
	        uiGadget.style.left = "0px";
	      }
	      if (dragPosY < 0)
	      {
	        uiGadget.style.top = "0px";
	      }
	      if (dragPosY > offsetHeight)
	      {
	        uiGadget.style.top = offsetHeight + "px";
	      }
	      if (dragPosX > offsetWidth)
	      {
	        uiGadget.style.left = offsetWidth + "px";
	      }
	
	      // drag start callback
	      uiGadget.onDragStart = function(x, y, lastMouseX, lastMouseY, e)
	      {
	        desktopPage.children("div.UIGadget").each(function()
	        {
	          var mask = $(this).find("div.UIMask").eq(0);
	          if (mask)
	          {
	            var c = $(this).find("div.gadgets-gadget-content")[0];
	            mask.css({"marginTop" : -c.offsetHeight + "px", "height" : c.offsetHeight + "px", "width" : c.offsetWidth + "px", "backgroundColor" : "white", "display" : "block"});
	            mask.fadeTo(0, 0.03);
	          }
	        });
	
	      };
	
	      //drag callback
	      uiGadget.onDrag = function(nx, ny, ex, ey, e)
	      {
	        if (nx < 0)
	        {
	          uiGadget.style.left = "0px";
	        }
	        if (ny < 0)
	        {
	          uiGadget.style.top = "0px";
	        }
	      };
	
	      //drop callback
	      uiGadget.onDragEnd = function(x, y, clientX, clientY)
	      {
	        desktopPage.children("div.UIGadget").each(function()
	        {
	          var mask = $(this).find("div.UIMask").eq(0);
	          if (mask)
	          {
	            mask.css("display", "none");
	          }
	        });
	
	        var offsetHeight = desktopPage[0].offsetHeight - uiGadget.offsetHeight;
	        var offsetWidth = desktopPage[0].offsetWidth - uiGadget.offsetWidth;
	        var dragPosX = uiGadget.offsetLeft;
	        var dragPosY = uiGadget.offsetTop;
	
	        if (dragPosX < 0)
	        {
	          uiGadget.style.left = "0px";
	        }
	        if (dragPosY < 0)
	        {
	          uiGadget.style.top = "0px";
	        }
	        if (dragPosY > offsetHeight)
	        {
	          uiGadget.style.top = offsetHeight + "px";
	        }
	        if (dragPosX > offsetWidth)
	        {
	          uiGadget.style.left = offsetWidth + "px";
	        }
	        eXoGadget.saveWindowProperties(uiGadget);
	      };
	    }
	  },
	
	  /**
	   * Use to edit some information of gadget such as nocache, debug, etc
	   * @param {String} id identifier of gadget
	   */
	  editGadget : function(id)
	  {
	    var tempId = $("#" + id).find("iframe.gadgets-gadget").attr("id").split('_')[2];
	    gadgets.container.getGadget(tempId).handleOpenUserPrefsDialog();
	  },
	
	  /**
	   * Minimize a gadget
	   * @param {Object} icon object to minimize
	   */
	  minimizeGadget: function(icon)
	  {
	    var minIcon = $(icon);
	    var gadget = minIcon.closest(".UIGadget");
	    var portletFrag = gadget.closest(".PORTLET-FRAGMENT");
	    if (portletFrag.length == 0)
	    {
	      return;
	    }
	
	    var gadgetApp = gadget.find("div.GadgetApplication");
	    var minimized = gadgetApp.css("display") != "none";
	    if (minimized)
	    {
	      gadgetApp.css("display", "none");
	      minIcon.removeClass("MinimizeGadget").addClass("RestoreGadget");
	      minIcon.attr("title", minIcon.attr("unminiTitle"));
	    }
	    else
	    {
	      gadgetApp.css("display", "block");
	      minIcon.removeClass("RestoreGadget").addClass("MinimizeGadget");
	      minIcon.attr("title", minIcon.attr("miniTitle"));
	    }
        var ampPlain = "&";
        var ampXhtml = "&amp;";
        var amp = eXo.env.portal.urlEncoded ? ampXhtml : ampPlain;
	    var portletID = portletFrag.parent().attr("id");
	    var dashboardID = gadget.closest(".UIDashboard").attr("id");
	    var href = eXo.env.server.portalBaseURL + "?portal:componentId=" + portletID;
	    href += amp + "portal:type=action&uicomponent=" + dashboardID;
	    href += amp + "op=MinimizeGadget";
	    href += amp + "minimized=" + minimized;
	    href += amp + "objectId=" + gadget.attr("id") + amp + "ajaxRequest=true";
	    ajaxGet(href);
	
	    //TODO: Examine if this is really useful
	    if (gadget[0].minimizeCallback)
	    {
	      gadget[0].minimizeCallback(portletID);
	    }
	  },
	
	  /**
	   * Maximize a gadget
	   * @param {Object} icon object to maximize
	   */
	  maximizeGadget: function(icon)
	  {
	    var maxIcon = $(icon);
	    var gadget = maxIcon.closest(".UIGadget");
	    var portletFrag = gadget.closest(".PORTLET-FRAGMENT");
	    if (portletFrag.length == 0)
	    {
	      return;
	    }
        var ampPlain = "&";
        var ampXhtml = "&amp;";
        var amp = eXo.env.portal.urlEncoded ? ampXhtml : ampPlain;
        var portletID = portletFrag.parent().attr("id");
	    var dashboardID = gadget.closest(".UIDashboard").attr("id");
	    var maximizeParam = gadget.closest(".UIDashboardContainer").length > 0 ? "maximize" : "unmaximize";
	    var href = eXo.env.server.portalBaseURL + "?portal:componentId=" + portletID;
	    href += amp + "portal:type=action" + amp + "uicomponent=" + dashboardID;
	    href += amp + "op=MaximizeGadget";
	    href += amp + "maximize=" + maximizeParam;
	    href += amp + "objectId=" + gadget.attr("id") + amp + "ajaxRequest=true";
	    ajaxGet(href, true);
	  },
	
	  /**
	   * Delete a gadget from UI and database
	   * @param {Object} selectedElement object to delete
	   */
	  deleteGadget : function(icon)
	  {
	    var closeIcon = $(icon);
	    var gadget = closeIcon.closest(".UIGadget");
	    var portletFrag = gadget.closest(".PORTLET-FRAGMENT");
	
	    if (portletFrag.length > 0)
	    {
	      var portletID = portletFrag.parent().attr("id");
	      var dashboardID = gadget.closest(".UIDashboard").attr("id");
	
	      if (confirm("${GadgetDeletionConfirmation}"))
	      {
            var ampPlain = "&";
            var ampXhtml = "&amp;";
            var amp = eXo.env.portal.urlEncoded ? ampXhtml : ampPlain;
	        var href = eXo.env.server.portalBaseURL + "?portal:componentId=" + portletID;
	        href += amp + "portal:type=action&uicomponent=" + dashboardID;
	        href += amp + "op=DeleteGadget";
	        href += amp + "objectId=" + gadget.attr("id") + amp + "ajaxRequest=true";
	
	        var dashboardCont = gadget.closest(".UIDashboardContainer");
	        if (dashboardCont.length > 0)
	        {
	          ajaxGet(href);
	          gadget.remove();
	          if(dashboardCont.find("div.UIGadget").length == 0)
	          {
	            dashboardCont.find("div.NoGadget").css("display", "block");
	          }
	        }
	        else
	        {
	          //Delete maximized gadget
	          ajaxGet(href);
	        }
	      }
	    }
	    else
	    {
	      //Code used for desktop page
	      var blockID = closeIcon.closest(".UIPage").find("div.id").html();
	      if (confirm("${GadgetDeletionConfirmation}"))
	      {
	        var params = [
	          {name: "objectId", value : gadget.attr("id")}
	        ];
	        var result = ajaxAsyncGetRequest(eXo.env.server.createPortalURL(blockID, "DeleteGadget", true, params), false);
	        if (result == "OK")
	        {
	          gadget.remove();
	        }
	      }
	    }
	  },
	
	  /**
	   * Save Window information of gadget instance (x, y, z axis, etc)
	   * @param {Object} object Gadget object
	   */
	  saveWindowProperties : function(object)
	  {
	    var gadget = $(object);
	    var blockID = gadget.closest(".UIPage").find("div.id").html();
	    var params = [
	      {name: "objectId", value : object.id},
	      {name: "posX", value : object.offsetLeft},
	      {name: "posY", value : object.offsetTop},
	      {name: "zIndex", value : object.style.zIndex}
	    ];
	    ajaxAsyncGetRequest(eXo.env.server.createPortalURL(blockID, "SaveGadgetProperties", true, params), false);
	  },
	
	  /**
	   * Resize height of parent portlet to full height of browser
	   * @param componentId a component is child of portlet
	   */
	  resizeFullHeight : function(componentId)
	  {
	    var portletFrag = $("#" + componentId).closest(".PORTLET-FRAGMENT");
	    base.Browser.fillUpFreeSpace(portletFrag[0]);
	  }
	};
	
	return {UIGadget : eXoGadget};
})($, base, common);
