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

eXo.gadget.UIGadget = {
    /**
     * Create a new Gadget
     * @param {String} url local or remote path that contain gadget .xml file
     * @param {String} id id of object contains this gadget (parent id)
     * @param {String} metadata contain information of gadget
     * @param {Object} userPref
     * @param {String} view type of view (home, canvas, ...)
     * @param {boolean} isdev normal or development mode (0, 1)
     * @param {boolean} debug normal or debug mode (0, 1)
     * @param {String} nocache value indicate cache or nocache at shindig level (0, 1)
     */
	createGadget : function(url, id, metadata, userPref, view, hostName, isdev, debug, nocache) {
		//eXo = eXo || {};
		window.gadgets = window.gadgets || {};
		eXo.gadgets = window.gadgets;
		//window.gadgets = eXo.gadget.Gadgets;
		if (!eXo.gadgets || !eXo.gadgets.rpc) {
			eXo.core.Loader.register('rpc', '1.0.0',true, 0, hostName + '/js/rpc.js?c=1');
			eXo.core.Loader.register('eXo.gadgets.Gadgets', '/eXoResources/javascript/eXo/gadget/Gadgets.js');
			eXo.core.Loader.register('eXo.gadgets.ExoBasedUserPrefStore', '/eXoResources/javascript/eXo/gadget/ExoBasedUserPrefStore.js');
		}
		eXo.core.Loader.init("rpc","eXo.gadgets.Gadgets","eXo.gadgets.ExoBasedUserPrefStore", 
		eXo.gadget.UIGadget.createCallback, null, arguments);
	},
    
    createCallback : function(url, id, metadata, userPref, view, hostName, isdev, debug, nocache) {
        //TODO: dang.tung - set language for gadget
        //-----------------------------------------
        var language = eXo.core.I18n.getLanguage();
        gadgets.container.setLanguage(language);
        //-----------------------------------------
        var gadget;
        if (metadata != null) {
        	// Check if gadget's height is not set and current view is canvas. By default, gadget's height is 800px
        	if(metadata.modulePrefs.height == 0 && view == 'canvas') {
        		metadata.modulePrefs.height = "800px";
        	}
            gadget = gadgets.container.createGadget({specUrl: url,height: metadata.modulePrefs.height, secureToken: metadata.secureToken, view: view});
            gadget.metadata = metadata;
        } else {
            gadget = gadgets.container.createGadget({specUrl: url});
        }
        gadget.parentId = id;
        gadget.debug = debug;
        gadget.nocache = nocache;
        gadget.isdev = isdev;
        gadget.serverBase_ = hostName;
        
        gadgets.container.addGadget(gadget);
        // i use the internal var "gadget.userPrefs_" to not call the save on the server side
        if (userPref != null) gadget.userPrefs_ = userPref;
        var gadgetBlock = document.getElementById(id);
        gadgetBlock.innerHTML = "<div id='gadget_" + gadget.id + "' class='UIGadgetContent'> </div>";
        gadgets.container.renderGadgets();
        var uiGadget = eXo.core.DOMUtil.findAncestorByClass(gadgetBlock, "UIGadget");
        //TODO: dang.tung - isn't portlet
        if (uiGadget != null) {
            var isDesktop = false;
            if (uiGadget.parentNode.className == "UIPageDesktop") {
                uiGadget.style.position = "absolute";
                isDesktop = true;
            }
            else uiGadget.style.width = "auto";
            eXo.gadget.UIGadget.init(uiGadget, isDesktop, gadget.metadata);
        }

    },
    /**
     * Initialize data of gadget such as title, style, etc
     * @param {Object} uiGadget object need to init data
     * @param {boolean} inDesktop use to realize UIDesktopPage or no
     * @param {String} metadata metadata of gadget
     */
    init : function(uiGadget, inDesktop, metadata) {
        var portletFragment = eXo.core.DOMUtil.findAncestorByClass(uiGadget, "PORTLET-FRAGMENT");
        if (portletFragment == null) {
            uiGadget.onmouseover = eXo.gadget.UIGadget.showGadgetControl;
            uiGadget.onmouseout = eXo.gadget.UIGadget.hideGadgetControl;
        } else {
            var gadgetControl = eXo.core.DOMUtil.findFirstDescendantByClass(uiGadget, "div", "GadgetControl");
            gadgetControl.style.display = "block";
            var gadgetTitle = eXo.core.DOMUtil.findFirstDescendantByClass(gadgetControl, "div", "GadgetTitle") ;
            gadgetTitle.style.display = "block";
            if (metadata && metadata.modulePrefs.title != null && metadata.modulePrefs.title.length > 0) gadgetTitle.innerHTML = metadata.modulePrefs.title;
        }

        if (inDesktop) {
            var dragHandleArea = eXo.core.DOMUtil.findFirstDescendantByClass(uiGadget, "div", "GadgetDragHandleArea");

            if (uiGadget.style.zIndex < 0) uiGadget.style.zIndex = 0;
            eXo.core.DragDrop2.init(dragHandleArea, uiGadget);

            var uiPageDesktop = document.getElementById("UIPageDesktop");
            var offsetHeight = uiPageDesktop.offsetHeight - uiGadget.offsetHeight ;
            var offsetWidth = uiPageDesktop.offsetWidth - uiGadget.offsetWidth ;
            var dragPosX = uiGadget.offsetLeft;
            var dragPosY = uiGadget.offsetTop;

            if (dragPosX < 0) uiGadget.style.left = "0px";
            if (dragPosY < 0) uiGadget.style.top = "0px";
            if (dragPosY > offsetHeight) uiGadget.style.top = offsetHeight + "px";
            if (dragPosX > offsetWidth) uiGadget.style.left = offsetWidth + "px";

            // drag start callback
            uiGadget.onDragStart = function(x, y, lastMouseX, lastMouseY, e) {
                var uiPageDesktop = document.getElementById("UIPageDesktop");
                if (uiPageDesktop == null) return;
                var uiGadgets = eXo.core.DOMUtil.findChildrenByClass(uiPageDesktop, "div", "UIGadget");
                for (var i = 0; i < uiGadgets.length; i++) {
                    var uiMask = eXo.core.DOMUtil.findFirstDescendantByClass(uiGadgets[i], "div", "UIMask");
                    if (uiMask != null) {
                        var gadgetContent = eXo.core.DOMUtil.findFirstDescendantByClass(uiGadgets[i], "div", "gadgets-gadget-content");
                        uiMask.style.marginTop = - gadgetContent.offsetHeight + "px";
                        uiMask.style.height = gadgetContent.offsetHeight + "px";
                        uiMask.style.width = gadgetContent.offsetWidth + "px";
                        uiMask.style.backgroundColor = "white";
                        eXo.core.Browser.setOpacity(uiMask, 3);
                        uiMask.style.display = "block";
                    }
                }
            }

            //drag callback
            uiGadget.onDrag = function(nx, ny, ex, ey, e) {
                if (nx < 0) uiGadget.style.left = "0px";
                if (ny < 0) uiGadget.style.top = "0px";
            }

            //drop callback
            uiGadget.onDragEnd = function(x, y, clientX, clientY) {
                var uiPageDesktop = document.getElementById("UIPageDesktop");
                var uiGadgets = eXo.core.DOMUtil.findChildrenByClass(uiPageDesktop, "div", "UIGadget");
                for (var i = 0; i < uiGadgets.length; i++) {
                    var uiMask = eXo.core.DOMUtil.findFirstDescendantByClass(uiGadgets[i], "div", "UIMask");
                    if (uiMask) {
                        uiMask.style.display = "none";
                    }
                }

                var offsetHeight = uiPageDesktop.offsetHeight - uiGadget.offsetHeight ;
                var offsetWidth = uiPageDesktop.offsetWidth - uiGadget.offsetWidth ;
                var dragPosX = uiGadget.offsetLeft;
                var dragPosY = uiGadget.offsetTop;

                if (dragPosX < 0) uiGadget.style.left = "0px";
                if (dragPosY < 0) uiGadget.style.top = "0px";
                if (dragPosY > offsetHeight) uiGadget.style.top = offsetHeight + "px";
                if (dragPosX > offsetWidth) uiGadget.style.left = offsetWidth + "px";

                eXo.gadget.UIGadget.saveWindowProperties(uiGadget);
            }
        }

    },
    /**
     * Show gadget control
     * @param {Event} e
     */
    showGadgetControl : function(e) {
        if (!e) e = window.event;
        e.cancelBubble = true;
        var DOMUtil = eXo.core.DOMUtil;
        var uiGadget = this ;
        var gadgetControl = DOMUtil.findFirstDescendantByClass(uiGadget, "div", "GadgetControl");
        gadgetControl.style.visibility = "visible";

        var uiPageDesktop = DOMUtil.findAncestorByClass(uiGadget, "UIPageDesktop");
        if (uiPageDesktop) {
            var dragHandleArea = DOMUtil.findFirstDescendantByClass(gadgetControl, "div", "GadgetTitle");
        }
    },

    /**
     * Hide gadget control
     *@param {Event} e
     */
    hideGadgetControl : function(e) {
        if (!e) e = window.event;
        e.cancelBubble = true;
        var uiGadget = this ;
        var gadgetControl = eXo.core.DOMUtil.findFirstDescendantByClass(uiGadget, "div", "GadgetControl");
        gadgetControl.style.visibility = "hidden";
        uiGadget.style.border = "none";
    },
    /**
     * Use to edit some information of gadget such as nocache, debug, etc
     * @param {String} id identifier of gadget
     */
    editGadget : function(id) {
        var DOMUtil = eXo.core.DOMUtil ;
        var uiapp = document.getElementById(id) ;
        var id = eXo.core.DOMUtil.findFirstDescendantByClass(uiapp, "iframe", "gadgets-gadget") ;
        var tempId = id.id.split('_')[2] ;
        gadgets.container.getGadget(tempId).handleOpenUserPrefsDialog();
    },
    /**
     * Minimize a gadget
     * @param {Object} selectedElement object to minimize
     */
    minimizeGadget: function(selectedElement) {
        var DOMUtil = eXo.core.DOMUtil ;
        var uiGadget = DOMUtil.findAncestorByClass(selectedElement, "UIGadget") ;
        var portletFrag = DOMUtil.findAncestorByClass(uiGadget, "PORTLET-FRAGMENT") ;
        if (!portletFrag) return;

        var gadgetApp = DOMUtil.findFirstChildByClass(uiGadget, "div", "GadgetApplication") ;
        var minimized = false;
        if (gadgetApp.style.display != "none") {
            minimized = true;
            gadgetApp.style.display = "none";
            DOMUtil.replaceClass(selectedElement, "MinimizeGadget", "RestoreGadget");
            selectedElement.title = selectedElement.getAttribute("unminiTitle");
        } else {
            minimized = false;
            gadgetApp.style.display = "block";
            DOMUtil.replaceClass(selectedElement, "RestoreGadget", "MinimizeGadget");
            selectedElement.title = selectedElement.getAttribute("miniTitle");
        }

        var compId = portletFrag.parentNode.id;
        var uicomp = DOMUtil.findAncestorByClass(uiGadget, "UIDashboard") ;
        var href = eXo.env.server.portalBaseURL + "?portal:componentId=" + compId ;
        href += "&portal:type=action&uicomponent=" + uicomp.id;
        href += "&op=MinimizeGadget";
        href += "&minimized=" + minimized;
        href += "&objectId=" + uiGadget.id + "&ajaxRequest=true";
        ajaxGet(href);
        if (uiGadget.minimizeCallback) uiGadget.minimizeCallback(portletFrag.parentNode.id);
    },
    /**
     * Maximize a gadget
     * @param {Object} selectedElement object to maximize
     */
    maximizeGadget: function(selectedElement) {
        var DOMUtil = eXo.core.DOMUtil ;
        var uiGadget = DOMUtil.findAncestorByClass(selectedElement, "UIGadget") ;
        var portletFrag = DOMUtil.findAncestorByClass(uiGadget, "PORTLET-FRAGMENT") ;
        if (!portletFrag) return;
        var compId = portletFrag.parentNode.id;
        var uicomp = DOMUtil.findAncestorByClass(uiGadget, "UIDashboard");
        var compDisplay = DOMUtil.findAncestorByClass(uiGadget, "UIDashboardContainer");
        var maximize = compDisplay ? "maximize" : "unmaximize";
        var href = eXo.env.server.portalBaseURL + "?portal:componentId=" + compId ;
        href += "&portal:type=action&uicomponent=" + uicomp.id;
        href += "&op=MaximizeGadget";
        href += "&maximize=" + maximize;
        href += "&objectId=" + uiGadget.id + "&ajaxRequest=true";
        ajaxGet(href,true);
    },
    /**
     * Delete a gadget from UI and database
     * @param {Object} selectedElement object to delete
     */
    deleteGadget : function(selectedElement) {
        var DOMUtil = eXo.core.DOMUtil ;
        var uiPage = DOMUtil.findAncestorByClass(selectedElement, "UIPage") ;
        var uiGadget = DOMUtil.findAncestorByClass(selectedElement, "UIGadget") ;
        var containerBlockId ;

        var portletFragment = DOMUtil.findAncestorByClass(uiGadget, "PORTLET-FRAGMENT");

        if (portletFragment != null) {
            var compId = portletFragment.parentNode.id;
            var uicomp = DOMUtil.findAncestorByClass(uiGadget, "UIDashboard").id;
//            if (DOMUtil.findChildrenByClass(portletFragment, "div", "UIDashboard"))
//                uicomp = "UIDashboard";
//            else
//                uicomp = DOMUtil.getChildrenByTagName(portletFragment, "div")[0].className;
            if (confirm(this.confirmDeleteGadget)) {
                var href = eXo.env.server.portalBaseURL + "?portal:componentId=" + compId;
                href += "&portal:type=action&uicomponent=" + uicomp;
                href += "&op=DeleteGadget";
                href += "&objectId=" + uiGadget.id + "&ajaxRequest=true";
                
                var uiDashboardCont = DOMUtil.findAncestorByClass(uiGadget, "UIDashboardContainer"); 
                if(uiDashboardCont) {
                	ajaxGet(href);
	                DOMUtil.removeElement(uiGadget);
	                if(!DOMUtil.findFirstDescendantByClass(uiDashboardCont, "div", "UIGadget")) {
	                	DOMUtil.findFirstDescendantByClass(uiDashboardCont, "div", "NoGadget").style.display = "block";
	                }
                }else {
//                Case: delete gadget in dashboard when maximized gadget
                	ajaxGet(href);
                }
            }

        } else {
            var uiPageIdNode = DOMUtil.findFirstDescendantByClass(uiPage, "div", "id");
            containerBlockId = uiPageIdNode.innerHTML;
            if (confirm(this.confirmDeleteGadget)) {
                var params = [
                    {name: "objectId", value : uiGadget.id}
                ] ;
                var result = ajaxAsyncGetRequest(eXo.env.server.createPortalURL(containerBlockId, "DeleteGadget", true, params), false) ;
                if (result == "OK") {
                    DOMUtil.removeElement(uiGadget);
                }
            }
        }
    },
    /**
     * Save Window information of gadget instance (x, y, z axis, etc)
     * @param {Object} object Gadget object
     */
    saveWindowProperties : function(object) {
        var DOMUtil = eXo.core.DOMUtil ;
        var uiPage = DOMUtil.findAncestorByClass(object, "UIPage") ;
        var uiPageIdNode = DOMUtil.findFirstDescendantByClass(uiPage, "div", "id");
        containerBlockId = uiPageIdNode.innerHTML;

        var gadgetApp = DOMUtil.findFirstDescendantByClass(object, "div", "GadgetApplication");

        var params = [
            {name: "objectId", value : object.id},
            {name: "posX", value : object.offsetLeft},
            {name: "posY", value : object.offsetTop},
            {name: "zIndex", value : object.style.zIndex}
        ] ;

        ajaxAsyncGetRequest(eXo.env.server.createPortalURL(containerBlockId, "SaveGadgetProperties", true, params), false);
    }
}