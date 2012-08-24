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
var uiLanguageSelector = {
	init : function(selected, selectOptions) {
		var selector = _module.UILanguageSelector;
		var langForm = $(".UIChangeLanguageForm");
		var saveButton = langForm.find(".UIAction a").first(); 
		var href = saveButton.attr("href");
		saveButton.on("click", function() {selector.changeLanguage(href);return false;});		
		
        selector.SelectedItem = {"component": selected.component, "option" : selected.option};
        langForm.find(".NodeLabel").parent().each(function(index) {
        	var opt = selectOptions[index];
        	$(this).on("click", function() {
            	_module.UIItemSelector.onClickOption(this, null, opt.component, opt.option);
            });
        });
	},
	
	changeLanguage : function(url) {
	   var language = "";                
	   if(_module.UIItemSelector.SelectedItem != undefined) {
		   language = _module.UIItemSelector.SelectedItem.option;
	   }
	   if(language == undefined) {
		   language = "";
	   }
	   window.location = url + "&language=" + language;
	}
};

_module.UILanguageSelector = uiLanguageSelector;