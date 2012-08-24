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
 * Created by The eXo Platform SARL
 * 
 * @author : dang.tung tungcnw@gmail.com
 */
var uiFormInputThemeSelector = {

  initForm : function() {
	  $(".UIFormInputThemeSelector").find(".SetDefault").on("click", function() {
		  _module.UIFormInputThemeSelector.setDefaultTheme(this,'DefaultTheme');
	  });	  
  },
  
  initSelector : function() {
	  $(".UIFormInputThemeSelector").find(".UIThemeSelector").parent().on("click", function() {
		  var theme = $(this).children("div").attr("class").replace("UIThemeSelector ", "");
		  _module.UIFormInputThemeSelector.showThemeSelected(this, theme); 
	  });
  },
  
  showThemeSelected : function(obj, param) {
    var jqObj = $(obj);
    var itemListContainer = jqObj.parent().closest(".ItemListContainer");
    var detailList = itemListContainer.next("div").find("div.UIThemeSelector").eq(0);
    detailList.next("div").html(jqObj.find("div.NameStyle").eq(0).html());
    detailList.attr("class", "UIThemeSelector " + param);
    //jqObj.parent().prev("input")[0].value = param;//This does not work as 'prev' in jQuery does not return hidden input
    jqObj.parent().parent().children("input").eq(0).val(param);
  },

  setDefaultTheme : function(obj, param) {
    var itemDetailList = $(obj).parent().closest(".ItemDetailList");
    var detailList = itemDetailList.find("div.UIThemeSelector").eq(0);
    detailList.attr("class", "UIThemeSelector " + param);

    detailList.next("div").html(base.I18NMessage.getMessage("DefaultTheme"));
    itemDetailList.prev("div").find("div.ItemList").eq(0).parent().children("input").eq(0).val(param);
  }  
};

eXo.webui.UIPageTemplateOptions = {

  /**
   * @author dang.tung
   * 
   * TODO To change the template layout in page config Called by
   * UIPageTemplateOptions.java Review UIDropDownControl.java: set javascrip
   * action UIDropDownControl.js : set this method to do
   */
  selectPageLayout : function(id, selectedIndex) {
    var dropDownControl = $("#" + id);
    var itemSelectorAncest = dropDownControl.closest(".ItemSelectorAncestor");
    var itemList = itemSelectorAncest.find("div.ItemList");
    var itemSelectorLabel = itemSelectorAncest.find("a.OptionItem");
    var itemSelector = dropDownControl.find("div.UIItemSelector");
    var itemDetailList = itemSelector.find("div.ItemDetailList");
    if (itemList.length == 0)
      return;
    for (i = 0; i < itemSelectorLabel.length; ++i) {
      if (i >= itemList.length)
        continue;
      if (i == selectedIndex) {
        itemList[i].style.display = "block";
        if (itemDetailList.length < 1)
          continue;
        itemDetailList[i].style.display = "block";
        var selectedItem = $(itemList[i]).find("div.SelectedItem").eq(0);
        if (!selectedItem || selectedItem == null)
          continue;
        var setValue = selectedItem.find("#SetValue")[0];
        if (setValue == null)
          continue;
        eval(setValue.innerHTML);
      } else {
        itemList[i].style.display = "none";
        if (itemDetailList.length > 0)
          itemDetailList[i].style.display = "none";
      }
    }
  }
};
_module.UIPageTemplateOptions = eXo.webui.UIPageTemplateOptions;
_module.UIFormInputThemeSelector = uiFormInputThemeSelector;