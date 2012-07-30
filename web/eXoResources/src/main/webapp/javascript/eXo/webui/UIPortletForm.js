/**
 * Copyright (C) 2012 eXo Platform SAS.
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
var uiPortletForm = {
	init : function(id, webui) {
		var tabs = $("#" + id + " .UIHorizontalTabs .MiddleTab");
		tabs.each(function() {
			var tab = $(this);
			tab.on("click", function() {
				if (tab.attr("id") === "EditMode") {
					_module.UIPortletForm.hideSaveButton(this);
				} else {
					_module.UIPortletForm.showSaveButton(this);
				}						
				webui.UIHorizontalTabs.changeTabForUIFormTabpane(this, id.replace("tab-", ""), tab.attr("id"));
				var actionLink = tab.find("~ .ExtraActions");
				eval(actionLink.html());
			});
		});
	},
	
	hideSaveButton : function(comp) {
		$(comp).closest(".WorkingArea").find("div.HorizontalLayout > div.UIAction > a.ActionButton").each(function()
		{
			var button = $(this);
			if(button.attr("id").indexOf("Save") >= 0)
			{
				button.css("display", "none");
			}
			else if(button.attr("id").indexOf("Close") >= 0)
			{
				button.html(button.attr("closeLabel"));
			}
		});
	},

	showSaveButton : function(comp) {
		$(comp).closest(".WorkingArea").find("div.HorizontalLayout > div.UIAction > a.ActionButton").each(function()
		{
			var button = $(this);
			if(button.attr("id").indexOf("Save") >= 0)
			{
				button.css("display", "inline-block");
			}
			else if(button.attr("id").indexOf("Close") >= 0)
			{
				button.html(button.attr("cancelLabel"));
			}
		});
	}
};

_module.UIPortletForm = uiPortletForm;