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

var eXoDashBoard = {
	
	currentCol : null ,

	targetObj : null,
	
	init : function (dragItem, dragObj) {
		
		var BROWSER = base.Browser;

    var UTIL = _module.UIDashboardUtil;

    var jqDragObj = $(dragObj);//JQuery wrapper of dragObj, that facilitates JQuery integration

    var portletFrag = jqDragObj.closest(".PORTLET-FRAGMENT");

    var gadgetContainer = portletFrag.find("div.GadgetContainer").eq(0);

		common.DragDrop.init(dragItem, dragObj);

    dragObj.onDragStart = function(x, y, lastMouseX, lastMouseY, e)
    {

      var workingWS = document.getElementById("UIWorkingWorkspace");

      var ggwidth = dragObj.offsetWidth;

      //find position to put drag object in
      var mx = BROWSER.findMouseRelativeX(workingWS, e);
      var ox = BROWSER.findMouseRelativeX(dragObj, e);
      var x = mx - ox;

      var my = BROWSER.findMouseRelativeY(workingWS, e);
      var oy = BROWSER.findMouseRelativeY(dragObj, e);
      var y = my - oy;

      jqDragObj.parentsUntil(portletFrag).each(function()
      {
        if (this.scrollLeft > 0)
        {
          x -= this.scrollLeft;
        }
        if (this.scrollTop > 0)
        {
          y -= this.scrollTop;
        }
      });

      if (!jqDragObj.hasClass("SelectItem"))
      {
        var targetArea = $("<div>").attr("id", "UITarget").addClass("UITarget").css("height", dragObj.offsetHeight + "px");
        _module.UIDashboard.targetObj = targetArea;
        jqDragObj.after(targetArea);
      }
      else
      {
        var copy = jqDragObj.clone(true).addClass("CopyObj");
        jqDragObj.before(copy);
      }

      //increase speed of mouse when over iframe by create div layer above it
      gadgetContainer.find("div.UIGadget").each(function()
      {
        var gadgetWindow = $(this);
        var mask = gadgetWindow.find("div.UIMask").eq(0);
        if (mask)
        {
          var app = gadgetWindow.find("div.GadgetApplication")[0];
          mask.css({"marginTop" : - app.offsetHeight + "px", "height" : app.offsetHeight + "px", "width" : app.offsetWidth + "px", "display" : "block", "backgroundColor" : "white"});
          mask.fadeTo(0, 0.03);
        }
      });

      if (!jqDragObj.hasClass("Dragging"))
      {
        jqDragObj.addClass("Dragging");
      }

      jqDragObj.css("width", ggwidth + "px").css("position", "absolute");

      //set position of drag object
      BROWSER.setPositionInContainer(workingWS, dragObj, x, y);
    }

    dragObj.onDrag = function(nx, ny, ex, ey, e)
    {

      _module.UIDashboard.scrollOnDrag(dragObj);

      var targetArea = _module.UIDashboard.targetObj;
      if (UTIL.isIn(ex, ey, gadgetContainer[0]))
      {
        if (!targetArea)
        {
          targetArea = $("<div>").attr("id", "UITarget").addClass("UITarget").css("height", dragObj.offsetHeight + "px");
          _module.UIDashboard.targetObj = targetArea;
        }

        if (!_module.UIDashboard.currentCol)
        {
          //We are sure that currentCol is not null as mouse cursor is already inside the gadget container
          _module.UIDashboard.currentCol = UTIL.findContainingColumn(gadgetContainer, ex);
        }

        var column = _module.UIDashboard.currentCol;
        if (UTIL.isInColumn(column, ex, gadgetContainer.scrollLeft()))
        {
          var addToLast = true;
          column.find("div.UIGadget").not("#" + dragObj.id).each(function()
          {
            if (ey <= $(this).offset().top + (this.offsetHeight / 3) - gadgetContainer.scrollTop())
            {
              $(this).before(targetArea);
              addToLast = false;

              return false;
            }
          });

          //That happens as user drags gadget to the bottom of a column or to an empty column
          if (addToLast)
          {
            column.append(targetArea);
          }
        }
        else
        {
          //There is no column containing mouse cursor as mouse is moved out of gadget container. So we reset the cached column to null
          _module.UIDashboard.currentCol = null;
        }
      }
      else if (targetArea != null && jqDragObj.hasClass("SelectItem"))
      {
        //prevent dragging item form selector popup out of gadget container
        targetArea.remove();
        _module.UIDashboard.targetObj = targetArea = null;
      }
    };


    dragObj.onDragEnd = function(x, y, clientX, clientY)
    {
      gadgetContainer.find("div.UIMask").each(function()
      {
    	var jObj = $(this);
        jObj.fadeTo(0, 1);
        jObj.css("display", "none");
      });

      jqDragObj.removeClass("Dragging").css("position", "static");

      var copyObj = portletFrag.find("div.CopyObj");
      if (copyObj)
      {
        copyObj.replaceWith(jqDragObj);
        jqDragObj.css("width", "auto");
      }

      var targetArea = _module.UIDashboard.targetObj;
      if (targetArea && targetArea.parent())
      {
        //if drag object is not gadget module, create an module
        var col = UTIL.findColIndexInDashboard(targetArea[0]);
        var row = UTIL.findRowIndexInDashboard(targetArea[0]);
        var compId = portletFrag.parent().attr("id");

        if (jqDragObj.hasClass("SelectItem"))
        {
          var params = [
            {name: "colIndex", value: col},
            {name: "rowIndex", value: row},
            {name: "objectId", value: jqDragObj.attr("id")}
          ];
          var url = UTIL.createRequest(compId, 'AddNewGadget', params);
          ajaxGet(url);
        }
        else
        {
          //in case: drop to old position
          if (UTIL.findColIndexInDashboard(dragObj) == col
            && UTIL.findRowIndexInDashboard(dragObj) == (row - 1))
          {
            targetArea.remove();
          }
          else
          {
            targetArea.replaceWith(jqDragObj);
            row = UTIL.findRowIndexInDashboard(dragObj);
            var params = [
              {name: "colIndex", value: col},
              {name: "rowIndex", value: row},
              {name: "objectId", value: dragObj.id}
            ];
            var url = UTIL.createRequest(compId, 'MoveGadget', params);
            ajaxGet(url);
          }
        }
      }

      gadgetContainer.find("div.UITarget").each(function()
      {
        $(this).remove();
      });

      _module.UIDashboard.targetObj = _module.UIDashboard.currentCol = null;
    };


    dragObj.onCancel = function(e)
    {
      e = e || window.event;
      if (BROWSER.isIE() && e.clientY < 0)
      {
        common.DragDrop.end(e);
      }
    };
  },
	
	onLoad : function(windowId, canEdit) {
    var portletWindow = $("#" + windowId).eq(0);
    if(!portletWindow)
    {
      return;
    }

    var portletFrag = portletWindow.find(".PORTLET-FRAGMENT").eq(0);
    var uiDashboard = portletFrag.find("div.UIDashboard").eq(0);
    var container = uiDashboard.find("div.UIDashboardContainer").eq(0);
    if (!container)
    {
      return;
    }

    uiDashboard.css("overflow", "hidden");
    portletFrag.css("overflow", "hidden");

    if (canEdit) {
    	container.find(".AddIcon > a").off("click").on("click", function() {
    		_module.UIDashboard.showSelectPopup(this);
    		return false;
    	});
    }
    var selectPopup = container.prev("div");
    selectPopup.find("a.CloseButton").eq(0).attr("onclick", null).click(function()
    {    	
      _module.UIDashboard.hideSelectPopup(selectPopup);
    });

    var gadgetCont = container.children("div.GadgetContainer").eq(0);
    if (base.Browser.isIE6())
    {
      gadgetCont.css("width", "99.5%");
    }
    gadgetCont.children("div.UIColumns").eq(0).css("width", "100%");

		//Todo: nguyenanhkien2a@gmail.com
		//We set and increase waiting time for initDragDrop function to make sure all UI (tag, div, iframe, etc) 
		//was loaded and to avoid some potential bugs (ex: GTNPORTAL-1068)
		setTimeout(function() {_module.UIDashboard.initDragDrop(windowId,canEdit);}, 400) ;
	},
	
	initDragDrop : function(windowId, canEdit) {
    var portletWindow = $("#" + windowId);

    //TODO: Improve this by seperate gadget control appearing in select popup and ones appearing in the dashboard
    portletWindow.find("div.GadgetControl").each(function()
    {
      var gadgetControl = $(this);
      var gadget = gadgetControl.closest(".UIGadget");
      var minimizeButton = gadget.find("span.MinimizeAction").eq(0);//That might be undefined if actual gadget is the item in Select Gadget popup
      if(canEdit)
      {
        _module.UIDashboard.init(gadgetControl[0], gadget[0]);
        if(minimizeButton)
        {
          minimizeButton.css("display", "block");
        }
      }
      else
      {
        if(minimizeButton)
        {
          minimizeButton.css("display", "none");
          minimizeButton.siblings("div.CloseGadget,div.EditGadget").css("display", "none");
        }
      }
    });
	},
	
	initPopup : function(popup) {
		if(typeof(popup) == "string") popup = document.getElementById(popup);
		if(!popup || popup.style.display == "none") return;
        var dashboard = $(popup).closest(".UIDashboard")[0];
		var deltaY = Math.ceil((dashboard.offsetHeight - popup.offsetHeight) / 2);
		if (deltaY < 0) {
			deltaY = 0;
		}
		var jpopup = $(popup);
		jpopup.find(".TopItemContainer, .BottomItemContainer").on({
			"mouseover": function() {
				_module.UIDashboard.enableContainer(this);
			}, 
			"mouseout": function() {
				_module.UIDashboard.disableContainer(this);
			},
			"mousedown": function() {
				webui.VerticalScrollManager.initScroll(this, $(this).hasClass("TopItemContainer"), 10);
			}
		});
		jpopup.find(".GadgetTab").on('click', function() {
			_module.UIDashboard.onTabClick(this, 'NormalTab', 'SelectedTab');
		});
		
		popup.style.top = $(dashboard).offset().top + deltaY + "px";
	},

	 /**
   * Show SelectPopup as user click on 'Add Gadgets' link. The argument link represents <a> element of 'Add Gadgets' link.
   */
  showSelectPopup : function(link)
  {
    var jqLink = $(link).parent();
    jqLink.css("visibility", "hidden");

    var portletID = jqLink.closest(".PORTLET-FRAGMENT").parent().attr("id");
    var url = _module.UIDashboardUtil.createRequest(portletID, "SetShowSelectContainer", [
      {name : "isShow", value : true}
    ]);
    ajaxGet(url);
  },

  hideSelectPopup : function(selectPopup)
  {
    selectPopup.css({"visibility" : "hidden", "display" : "none"});

    var dashboardCont = selectPopup.next("div.UIDashboardContainer");
    dashboardCont.find(".AddIcon").eq(0).css("visibility", "visible");

    var portletID = dashboardCont.closest(".PORTLET-FRAGMENT").parent().attr("id");

    var url = _module.UIDashboardUtil.createRequest(portletID, "SetShowSelectContainer", [
      {name : "isShow", value : false}
    ]);
    ajaxAsyncGetRequest(url, false);
  },

  /**
   * Using when click event happens on a dashboard tab
   * @param {Object} clickElement
   * @param {String} normalStyle a css style
   * @param {String} selectedType a css style
   */
  onTabClick : function(clickElement, normalStyle, selectedType)
  {
    var jqObj = $(clickElement);
    var category = jqObj.closest(".GadgetCategory");
    var itemCont = category.closest(".GadgetItemsContainer");

    var tab = category.children("div.GadgetTab").eq(0);
    if (tab.hasClass(normalStyle))
    {
      itemCont.children("div.GadgetCategory").each(function()
      {
        var c = $(this);
        c.children("div.GadgetTab").attr("class", "GadgetTab " + normalStyle);
        c.children("div.ItemsContainer").css("display", "none");
      });

      tab.attr("class", "GadgetTab " + selectedType);
      category.children("div.ItemsContainer").css("display", "block");
    }
    else
    {
      tab.attr("class", "GadgetTab " + normalStyle);
      category.children("div.ItemsContainer").css("display", "none");
    }

    var h = $(window).height();
    jqObj.closest(".PopupContent").css("height", (h - 100 < itemCont[0].offsetHeight) ? (h - 100 + "px") : "auto");
  },
  /**
	 * Change disabled object to enable state
	 * @param {Object} elemt object to enable
	 */
  enableContainer : function(elemt)
  {
    var jqObj = $(elemt);
    jqObj.removeClass("DisableContainer");
    jqObj.children("div.Arrow").eq(0).removeClass("DisableArrowIcon");
  },
  /**
   * Change object to disable state
   * @param {Object} elemt object to enable
   */
  disableContainer : function(elemt)
  {
    var jqObj = $(elemt);
    jqObj.addClass("DisableContainer");
    jqObj.children("div.Arrow").eq(0).addClass("DisableArrowIcon");
  },

  scrollOnDrag : function(dragObj) {
    var gadgetContainer = $(dragObj).closest(".UIDashboard").find("div.GadgetContainer").eq(0);

    var colCont = gadgetContainer.children("div.UIColumns")[0];

		var visibleWidth = gadgetContainer[0].offsetWidth;
		var visibleHeight = gadgetContainer[0].offsetHeight;
		var trueWidth = colCont.offsetWidth;
		var trueHeight = colCont.offsetHeight;
		
		var browser = base.Browser;
		var objLeft = browser.findPosXInContainer(dragObj, gadgetContainer[0]);
		var objRight = objLeft + dragObj.offsetWidth;
		var objTop = browser.findPosYInContainer(dragObj, gadgetContainer[0]);
		var objBottom = objTop + dragObj.offsetHeight;
		
		//controls horizontal scroll
    var deltaX = gadgetContainer.scrollLeft();
    if ((trueWidth - (visibleWidth + deltaX) > 0) && objRight > visibleWidth)
    {
      gadgetContainer.scrollLeft(deltaX + 5);
    } else if (objLeft < 0 && deltaX > 0)
    {
      gadgetContainer.scrollLeft(deltaX - 5);
    }


    //controls vertical scroll
    var controlBar = gadgetContainer.children("div.ContainerControlBarL")[0];
    var buttonHeight = 0;
    if (controlBar)
    {
      buttonHeight = controlBar.offsetHeight;
    }
    var deltaY = gadgetContainer.scrollTop();
    if ((trueHeight - (visibleHeight - 10 - buttonHeight + deltaY) > 0) && objBottom > visibleHeight)
    {
      gadgetContainer.scrollTop(deltaY + 5);
    } else
    if (objTop < 0 && deltaY > 0)
    {
      gadgetContainer.scrollTop(deltaY - 5);
    }

  }
};

_module.UIDashboard = eXoDashBoard;