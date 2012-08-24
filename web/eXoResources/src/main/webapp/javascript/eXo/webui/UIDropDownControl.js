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

eXo.webui.UIDropDownControl = {

  init : function(id, webui)
  {
    var elmt = $('#' + id);
    elmt.find('.UIDropDownTitle').on('click', this.showEvt);
    elmt.find('a.OptionItem').on('click', this.onclickEvt);
    elmt.find(".TopItemContainer").on('mousedown', function() {
    	webui.VerticalScrollManager.initScroll(this, true, 10);
    });   
    elmt.find(".BottomItemContainer").on('mousedown', function() {
    	webui.VerticalScrollManager.initScroll(this, false, 10);
    });
  },

  selectItem : function(method, id, selectedIndex) {
    if (method)
      method(id, selectedIndex);
  },

  /*
   * . minh.js.exo
   */
  /**
   * show or hide drop down control
   * 
   * @param {Object}
   *          obj document object to use as Anchor for drop down
   * @param {Object}
   *          evet event object
   */
  showEvt : function() {
    var dropDownAnchor = $(this).next("div");
    if(!dropDownAnchor)
    {
      return;
    }

    if(dropDownAnchor.css("display") == "none")
    {
      dropDownAnchor.css({"display" : "block" , "visibility" : "visible"});

      var middleCont = dropDownAnchor.find("div.MiddleItemContainer");
      var topCont = middleCont.prev("div");
      var bottomCont = middleCont.next("div");

      topCont.css("display", "block");
      bottomCont.css("display", "block");

      var visibleHeight = $(window).height() - $(middleCont[0]).offset().top - 40;
      if(middleCont[0].scrollHeight > visibleHeight)
      {
        middleCont.css("height", visibleHeight - topCont[0].offsetHeight - bottomCont[0].offsetHeight + "px");
        topCont.click(function() { return false;});
        bottomCont.click(function() { return false;});
      }
      else
      {
        topCont.css("display", "none");
        bottomCont.css("display", "none");
        middleCont.scrollTop(0);
        middleCont.css("height", "auto");
      }

      $(document).one("click", function()
      {
    	  $(document).one("click", function()
    	  {
	        dropDownAnchor.css("display", "none");
    	  });
      });
    }
    else
    {
      dropDownAnchor.css({"display" : "none" , "visibility" : "hidden"});
    }
  },
  /**
   * Hide an object
   * 
   * @param {Object,
   *          String} obj object to hide
   */
  hide : function(obj) {
    if (typeof (obj) == "string")
      obj = document.getElementById(obj);
    obj.style.display = "none";
  },
  /**
   * Use as event when user selects a item in drop down list Display content of
   * selected item and hide drop down control
   * 
   * @param {Object}
   *          obj selected object
   */
  onclickEvt : function() {
    var dropDownAnchor = $(this).parents("div.UIDropDownAnchor");
    var dropDownMiddleTitle = dropDownAnchor.prev("div.UIDropDownTitle").find("div.DropDownSelectLabel");
    dropDownMiddleTitle.html($(this).html());

    dropDownAnchor.css("display", "none");
  }
};

_module.UIDropDownControl = eXo.webui.UIDropDownControl;