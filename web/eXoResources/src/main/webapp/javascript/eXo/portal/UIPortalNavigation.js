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
 * Manages the main navigation menu on the portal
 */
eXo.portal.UIPortalNavigation = {
  hideMenuTimeoutIds : new HashMap(),
  scrollMgr : null,
  scrollManagerLoaded : false,
  
  /**
   * Sets some parameters :
   *  . the superClass to eXo.webui.UIPopupMenu
   *  . the css style classes
   * and calls the buildMenu function
   */
  init : function(popupMenu, container) {
    this.superClass = eXo.webui.UIPopupMenu;
    this.superClass.init(popupMenu, container);
    
    this.containerStyleClass = "MenuItemContainer";
    this.tabStyleClass = "MenuItem";

    this.buildMenu(popupMenu);
  },
  
  /**
   * Calls the init function when the page loads
   */
  onLoad : function(baseId) {    
	var uiNavPortlet = $("#" + baseId);
	if(uiNavPortlet.hasClass("UIHorizontalTabs")) eXo.portal.UIPortalNavigation.init(uiNavPortlet[0], uiNavPortlet[0]);
	  
	if (baseId === "UIHorizontalNavigation") {
		$(".UIHorizontalNavigation").slice(1).each(function() {$(this).hide();});
	}
  },
  
  /**
   * Builds the menu and the submenus
   * Configures each menu item :
   *  . sets onmouseover and onmouseout to call setTabStyle
   *  . sets the width of the item
   * Checks if a submenu exists, if yes, set some parameters :
   *  . sets onclick on the item to call toggleSubMenu
   *  . sets the width and min-width of the sub menu container
   * For each sub menu item :
   *  . set onmouseover to onMenuItemOver and onmouseout to onMenuItemOut
   *  . adds onclick event if the item contains a link, so a click on this item will call the link
   */
  buildMenu : function(popupMenu) {
    var DOMUtil = eXo.core.DOMUtil;
    var portalNav = eXo.portal.UIPortalNavigation;
    var topContainer = $(popupMenu);
    topContainer.attr("id", "PortalNavigationTopContainer");

    // Top menu items
    topContainer.children(".UITab").each(function()
    {
      var tab = $(this);

      var highlightClass = "UITab HighlightNavigationTab";
      tab.mouseenter(function()
      {
        portalNav.mouseEnterTab($(this), highlightClass);
      });

      var actualClass = tab.attr("class");
      tab.mouseleave(function()
      {
        portalNav.mouseLeaveTab($(this), actualClass);
      });

      tab.find("." + portalNav.containerStyleClass).first().css("minWidth", tab.width());
    });

    var itemConts = topContainer.find("." + this.containerStyleClass);
    itemConts.each(function()
    {
      if (!this.id)
      {
        this.id = DOMUtil.generateId("PortalNavigationContainer");
      }
      this.resized = false;

      var jObj = $(this);
      var items = jObj.find("." + portalNav.tabStyleClass);
      if (items.length == 0)
      {
        jObj.remove();
      }
      else
      {
        jObj.on({"mouseenter" : portalNav.onMenuItemOver, "mouseleave" : portalNav.onMenuItemOut,
          "click" : function() {portalNav.hideMenu(jObj.attr("id"));}}, "." + portalNav.tabStyleClass);
      }
    });
  },

  /**
   * Method triggered as mouse cursor enter a navigation node showed on navigation tab.
   *
   * @param tab
   * @param newClass
   */
  mouseEnterTab : function(tab, newClass)
  {
    var portalNav = eXo.portal.UIPortalNavigation;

    var getNodeURL = tab.attr("exo:getNodeURL");
    var menuItemContainer = tab.find("." + portalNav.containerStyleClass).first();
    if (getNodeURL && !menuItemContainer.length)
    {
      var jsChilds = ajaxAsyncGetRequest(getNodeURL, false)
      try
      {
        var data = xj.parseJSON(jsChilds);
      }
      catch (e)
      {
      }
      if (!data || !data.length)
      {
        return;
      }
      tab.append(portalNav.generateContainer(data));
    }
    tab.attr("class", newClass);

    menuItemContainer = tab.find("." + portalNav.containerStyleClass).first();
    if (menuItemContainer.length)
    {
      portalNav.cancelHideMenuContainer(menuItemContainer.attr("id"));
      portalNav.showMenu(tab, menuItemContainer);
    }
    return false;
  },

  /**
   * Method triggered as mouse cursor leaves a navigation node showed on navigation tab
   *
   * @param tab
   * @param oldClass
   */
  mouseLeaveTab : function(tab, oldClass)
  {
    var portalNav = eXo.portal.UIPortalNavigation;

    tab.attr("class", oldClass);
    var conts = tab.find("." + portalNav.containerStyleClass);
    if (conts.length)
    {
      portalNav.hideMenuTimeoutIds.put(conts[0].id, window.setTimeout(function() {portalNav.hideMenu(conts[0].id); }, 300));
    }
    return false;
  },

  /**
* Shows a submenu
   * Sets the width of the submenu (the first time it is shown) to fix a bug in IE
   * Sets the currentOpenedMenu to the menu being opened
   */
  showMenu : function(tab, menuItemContainer) {
    var portalNav = eXo.portal.UIPortalNavigation;
    var browser = eXo.core.Browser;
    portalNav.superClass.pushVisibleContainer(menuItemContainer.attr("id"));
        
    menuItemContainer.css({"display" : "block", "position" : "absolute"});
    var offParent = menuItemContainer.offsetParent();
    var y = tab.height() + browser.findPosYInContainer(tab[0], offParent[0]);
    var x = browser.findPosXInContainer(tab[0], offParent[0]) + 2;
        if(eXo.core.I18n.isRT()) {
     x = browser.findPosXInContainer(tab[0], offParent[0], true);
        }
    portalNav.superClass.setPosition(menuItemContainer[0], x, y, eXo.core.I18n.isRT());
    portalNav.superClass.show(menuItemContainer[0]);
        
    menuItemContainer.css("width", menuItemContainer.width() + "px");

var posXinBrowser = menuItemContainer.offset().left;
  			if(eXo.core.I18n.isLT()) {
if(posXinBrowser + menuItemContainer.width() >= $(window).width()) {
x += (tab.width() - menuItemContainer.width()) ;
menuItemContainer.css("left", x + "px");
  				}
  			} else {
if(posXinBrowser + tab.width() < menuItemContainer.width()) {
x += (tab.width() - menuItemContainer.width()) ;
menuItemContainer.css("right", x + "px");
  				}
  			}
  },

  cancelHideMenuContainer : function(containerId) {
var timeout = eXo.portal.UIPortalNavigation.hideMenuTimeoutIds.remove(containerId);
    if (timeout) {
      window.clearTimeout(timeout) ;
    }
  },
  
  /**
   * Changes the style of the parent button when a submenu has to be hidden
   */
  hideMenu : function(containerId) {
    var portalNav = eXo.portal.UIPortalNavigation;
    portalNav.hideMenuTimeoutIds.remove(containerId);

    var menuItemContainer = $("#" + containerId);
    if (menuItemContainer.length) {
      var id = menuItemContainer.attr("id");
      portalNav.superClass.pushHiddenContainer(id);
      portalNav.superClass.popVisibleContainer(id);
      portalNav.superClass.setCloseTimeout();
      portalNav.superClass.hide(menuItemContainer[0]);
    }
  },
  
  /**
   * When the mouse goes over a menu item (in the main nav menu)
   * Check if this menu item has a sub menu, if yes, opens it
   * Changes the style of the button
   */
  onMenuItemOver : function() {
    var menuItem = $(this);
    var portalNav = eXo.portal.UIPortalNavigation;
    
    var getNodeURL = menuItem.attr("exo:getNodeURL");
    var subContainer = menuItem.find("." + portalNav.containerStyleClass).first();
    if (getNodeURL && !subContainer.length) {
   var jsChilds = ajaxAsyncGetRequest(getNodeURL, false);
  	  try {
   var data = jQuery.parseJSON(jsChilds);
  	  } catch (e) {
  	  }	
  	  if (!data || !data.length) {
   menuItem.removeClass("ArrowIcon");
   menuItem.removeAttr("exo:getNodeURL");
  		  return;
  	  }
   menuItem.append(portalNav.generateContainer(data));
    }
      
    subContainer = menuItem.find("." + portalNav.containerStyleClass).first();
    if (subContainer.length) {
      portalNav.superClass.pushVisibleContainer(subContainer.attr("id"));
      portalNav.showMenuItemContainer(menuItem, subContainer) ;
      if (!subContainer.data("firstTime")) {
          subContainer.css("width", subContainer.width() + 2 + "px");
          subContainer.data("firstTime", true);
      }
    }
  },
  
  /**
   * Shows a sub menu, uses the methods from superClass (eXo.webui.UIPopupMenu)
   */
  showMenuItemContainer : function(menuItem, menuItemContainer) {
    var x = menuItem.width();
    var y = menuItem.position().top;
    this.superClass.show(menuItemContainer[0]);
    var posRight = $(window).width() - eXo.core.Browser.findPosX(menuItem[0], true) ;
    var rootX = (eXo.core.I18n.isLT() ? eXo.core.Browser.findPosX(menuItem[0]) : posRight) ;
   if (x + menuItemContainer.width() + rootX > $(window).width()) {
     x -= (menuItemContainer.width() + menuItem.width()) ;
    }
    this.superClass.setPosition(menuItemContainer[0], x, y, eXo.core.I18n.isRT());
  },
  
  /**
   * When the mouse goes out a menu item from the main nav menu
   * Checks if this item has a sub menu, if yes calls methods from superClass to hide it
   */
  onMenuItemOut : function() {
    var menuItem = $(this);
    var portalNav = eXo.portal.UIPortalNavigation;

    var subContainer = menuItem.find("." + portalNav.containerStyleClass).first();
    if (subContainer.length) {
      var id = subContainer.attr("id");
      portalNav.superClass.pushHiddenContainer(id);
      portalNav.superClass.currentVisibleContainers.remove(id);
      portalNav.superClass.setCloseTimeout(200);
    }
  },
  

  /***** Scroll Management *****/
  /**
   * Function called to load the scroll manager that will manage the tabs in the main nav menu
   *  . Creates the scroll manager
   *  . Adds the tabs to the scroll manager
   *  . Configures the arrows
   *  . Calls the initScroll function
   */
  loadScroll : function(portalNavId) {
    var uiNav = eXo.portal.UIPortalNavigation;
    var portalNav = $("#" + portalNavId);
    if (portalNav.length) {
      // Creates new ScrollManager and initializes it
      uiNav.scrollMgr = eXo.portal.UIPortalControl.newScrollManager(portalNavId);
      uiNav.scrollMgr.initFunction = uiNav.initScroll;
      // Adds the tab elements to the manager
      uiNav.scrollMgr.mainContainer = portalNav[0];
      var arrowsContainer = portalNav.find(".ScrollButtons").first();
      uiNav.scrollMgr.arrowsContainer = arrowsContainer[0];
      uiNav.scrollMgr.loadElements("UITab");
      // Configures the arrow buttons
      var arrowButtons = arrowsContainer.find("a");
      if (arrowButtons.length == 2) {
        uiNav.scrollMgr.initArrowButton(arrowButtons[0], "left", "ScrollLeftButton", "HighlightScrollLeftButton", "DisableScrollLeftButton");
        uiNav.scrollMgr.initArrowButton(arrowButtons[1], "right", "ScrollRightButton", "HighlightScrollRightButton", "DisableScrollRightButton");
      }
      // Finish initialization
      uiNav.scrollMgr.callback = uiNav.scrollCallback;
      uiNav.scrollManagerLoaded = true;
      uiNav.initScroll();
    }
  },
  
  /**
   * Init function for the scroll manager
   *  . Calls the init function of the scroll manager
   *  . Calculates the available space to render the tabs
   *  . Renders the tabs
   */
  initScroll : function() {
var portalNav = eXo.portal.UIPortalNavigation;
    if (!portalNav.scrollManagerLoaded) portalNav.loadScroll();
    var scrollMgr = portalNav.scrollMgr;
    scrollMgr.init();
    // Gets the maximum width available for the tabs
    scrollMgr.checkAvailableSpace();
    scrollMgr.renderElements();
  },

  /**
   * A callback function to call after a scroll event occurs (and the elements are rendered)
   * Is empty so far.
   */
  scrollCallback : function() {
  },

  generateContainer : function(data) {
   var htmlFrags = "<ul class='" + this.containerStyleClass + "' style='display: none;' id='";
   htmlFrags += eXo.core.DOMUtil.generateId("PortalNavigationContainer") + "' resized='false'>";

   for (var i = 0; i < data.length; i++) {
   var node = data[i];
   var actionLink = node.actionLink ? node.actionLink : "javascript:void(0);";

   htmlFrags += ("<li class='MenuItem " + (node.hasChild ? "ArrowIcon " : "") + (node.isSelected ? "SelectedItem'" : "NormalItem'"));
   htmlFrags += (node.hasChild ? (" exo:getNodeURL='" + node.getNodeURL + "' ") : "" );
   htmlFrags += ("' title='" + node.label + "'>");
   htmlFrags += ("<a class='ItemIcon " + (node.icon ? node.icon : "DefaultPageIcon") + "'" +
   "href='" + actionLink + "'>" + (node.label.length > 40 ? node.label.substring(0,37) + "..." : node.label) + "</a>");
   if (node.childs.length) {
   htmlFrags += eXo.portal.UIPortalNavigation.generateContainer(node.childs);
  }
   htmlFrags += "</li>";
   }
   htmlFrags += "</ul>";
   return htmlFrags;
  }
};