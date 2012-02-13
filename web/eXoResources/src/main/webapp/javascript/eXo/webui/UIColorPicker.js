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

eXo.webui.UIColorPicker = {

  show : function(obj) {
    document.onmousedown = new Function("eXo.webui.UIColorPicker.hide()");
    var jObj = xj(obj);
    this.tableColor = jObj.next("div")[0];
    this.title = jObj.find(".DisplayValue").first()[0];
    this.input = jObj.parent().find(".UIColorPickerValue").first()[0];
    this.showHide();
    this.getSelectedValue();
  },
  
  setColor : function(color) {
    if (xj(this.title).hasClass(color)) {
      this.hide();
      return;
    }
    var className = "DisplayValue " + color;
    this.title.className = className;
    this.input.value = color;
    this.hide();
  },

  clearSelectedValue : function() {
    var colorCell = xj(this.tableColor).find("a");        
    colorCell.each(function() {
    	var jObj = xj(this);
    	if (jObj.hasClass("SelectedColorCell")) {
    		jObj.removeClass("SelectedColorCell");    		
    		return false;
    	}
    });
  },

  getSelectedValue : function() {
    var selectedValue = this.input.value;
    
    this.clearSelectedValue();
    var colorCell = xj(this.tableColor).find("a");
    colorCell.each(function() {
    	var jObj = xj(this);
    	if (jObj.hasClass(selectedValue)) {
    		jObj.addClass("SelectedColorCell");
    		return false;
    	}
    });
  },

  hide : function() {
    if (eXo.webui.UIColorPicker.tableColor) {
      eXo.webui.UIColorPicker.tableColor.style.display = "none";
      eXo.webui.UIColorPicker.tableColor = null;
      eXo.webui.UIColorPicker.title = null;
      eXo.webui.UIColorPicker.input = null;
      document.onmousedown = null;
    }
  },

  showHide : function() {
    var obj = this.tableColor;
    if (obj.style.display != "block") {
      obj.style.display = "block";
    } else {
      obj.style.display = "none";
    }
  }
}