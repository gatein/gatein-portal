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
 * Mouse over event, Set highlight to OverItem
 * 
 * @param {Object}
 *          selectedElement focused element
 * @param {boolean}
 *          mouseOver
 */
var uiItemSelector = {

  init : function(selector, data, clickOnly) {
	  var items = gj(selector);
	  if (!clickOnly) {
		  items.on("mouseover", function() {
			  _module.UIItemSelector.onOver(this, true);
		  });
		  items.on("mouseout", function() {
			  _module.UIItemSelector.onOver(this, false);
		  });		  
	  }
	  items.each(function(index) {
		  var itm = gj(this);
		  itm.on("click", function() {
			  _module.UIItemSelector.onClick(this);
			  itm.find(".ExtraActions").each(function() {
				  var act = gj(this).html();
				  eval(act);
			  });		  
			  if (data) {
				 _module.UIItemSelector.onClickCategory(this, null, data[index].componentName, data[index].categoryName);
			  }
		  });
	  });
  },
  
  onOver : function(selectedElement, mouseOver) {
    if (selectedElement.className == "Item") {
      _module.UIItemSelector.beforeActionHappen(selectedElement);
    }
    if (mouseOver) {
      this.backupClass = selectedElement.className;
      selectedElement.className = "OverItem Item";
      // minh.js.exo
      // this.onChangeItemDetail(selectedElement, true);
    } else {
      selectedElement.className = this.backupClass;
      // this.onChangeItemDetail(selectedElement, false);
    }
  },
  /**
   * Mouse click event, highlight selected item and non-highlight other items
   * There are 3 types of item: Item, OverItem, SeletedItem
   * 
   * @param {Object}
   *          clickedElement
   */
  onClick : function(clickedElement) {
    var itemListContainer = clickedElement.parentNode;
    var allItems = gj(itemListContainer).find("div.Item").get();
    _module.UIItemSelector.beforeActionHappen(clickedElement);
    if (this.allItems.length <= 0)
      return;
    for ( var i = 0; i < allItems.length; i++) {
      if (allItems[i] != clickedElement) {
        allItems[i].className = "Item";
        this.onChangeItemDetail(clickedElement, true);
      } else {
        allItems[i].className = "SelectedItem Item";
        this.backupClass = "SelectedItem Item";
        this.onChangeItemDetail(clickedElement, false);
      }
    }
  },
  /**
   * Change UI of new selected item, selected item will be displayed and others
   * will be hidden
   * 
   * @param {Object}
   *          itemSelected selected item
   * @param {boolean}
   *          mouseOver
   */
  onChangeItemDetail : function(itemSelected, mouseOver) {
    if (!this.allItems || this.allItems.length <= 0)
      return;
    if (mouseOver) {
      for ( var i = 0; i < this.allItems.length; i++) {
        if (this.allItems[i] == itemSelected) {
          this.itemDetails[i].style.display = "block";
        } else {
          this.itemDetails[i].style.display = "none";
        }
      }
    } else {
      for ( var i = 0; i < this.allItems.length; i++) {
        if (this.allItems[i].className == "SelectedItem Item") {
          this.itemDetails[i].style.display = "block";
        } else {
          this.itemDetails[i].style.display = "none";
        }
      }
    }
  },

  /* Pham Thanh Tung added */
  onClickCategory : function(clickedElement, form, component, option) {
    _module.UIItemSelector.onClick(clickedElement);
    if (_module.UIItemSelector.SelectedItem == null) {
      _module.UIItemSelector.SelectedItem = new Object();
    }
    _module.UIItemSelector.SelectedItem.component = component;
    _module.UIItemSelector.SelectedItem.option = option;
  },

  /* Pham Thanh Tung added */
  onClickOption : function(clickedElement, form, component, option) {
    var selectedItems = gj(clickedElement).closest(".ItemDetailList").find("div.SelectedItem").get();
    for ( var i = 0; i < selectedItems.length; i++) {
      selectedItems[i].className = "NormalItem";
    }
    clickedElement.className = "SelectedItem";
    if (_module.UIItemSelector.SelectedItem == null) {
      _module.UIItemSelector.SelectedItem = new Object();
    }
    _module.UIItemSelector.SelectedItem.component = component;
    _module.UIItemSelector.SelectedItem.option = option;
  },

  /* TODO: Review This Function (Ha's comment) */
  beforeActionHappen : function(selectedItem) {
    var jqObj = gj(selectedItem);
    this.uiItemSelector = jqObj.closest(".UIItemSelector")[0];
    this.itemList = jqObj.closest(".ItemList")[0];
    var listCont = jqObj.closest(".ItemListContainer");
    this.itemListContainer = listCont[0];
    this.itemListAray = listCont.parent().find("div.ItemList").get();

    if (this.itemListAray.length > 1) {
      this.itemDetailLists = listCont.parent().find("div.ItemDetailList").get();
      this.itemDetailList = null;
      for ( var i = 0; i < this.itemListAray.length; i++) {
        if (this.itemListAray[i].style.display == "none") {
          this.itemDetailLists[i].style.display = "none";
        } else {
          this.itemDetailList = this.itemDetailLists[i];
          this.itemDetailList.style.display = "block";
        }
      }
    } else {
      this.itemDetailList = listCont.parent().find("div.ItemDetailList")[0];
    }

    this.itemDetails = gj(this.itemDetailList).find("div.ItemDetail").get();
    this.allItems = gj(this.itemList).find("div.Item").eq(0).parent().children("div.Item").get();
  },

  showPopupCategory : function(selectedNode) {
    var itemListCont = gj(selectedNode).closest(".ItemListContainer");
    var popupCategory = itemListCont.find("div.UIPopupCategory").eq(0);

    itemListCont.css("position", "relative");

    if(popupCategory.css("display") == "none")
    {
      popupCategory.css({"position" : "absolute", "top" : "23px", "left" : "0px", "display" : "block", "width" : "100%"});
    }
    else
    {
      popupCategory.css("display", "none");
    }
  },

  selectCategory : function(selectedNode) {
    var jqObj = gj(selectedNode);
    var itemListCont = jqObj.closest(".OverflowContainer");
    var selectedNodeIndex = _module.UIItemSelector.findIndex(selectedNode);

    var itemList = itemListCont.find("div.ItemList");
    var itemDetailList = itemListCont.find("div.ItemDetailList");

    itemList.each(function(index)
    {
      if (index == selectedNodeIndex)
      {
        gj(this).css("display", "block");
        itemDetailList.get(index).style.display = "block";
      }
      else
      {
        gj(this).css("display", "none");
        itemDetailList.get(index).style.display = "none";
      }
    });

    jqObj.closest(".UIPopupCategory").css("display", "none");
  },

  findIndex : function(object) {
    var siblings = gj(object).parent().children("div." + object.className).get();
    for ( var i = 0; i < siblings.length; i++) {
      if (siblings[i] == object)
        return i;
    }
  },

  /**
   * @author dang.tung
   * 
   * TODO To change the template layout in page config Called by
   * UIPageTemplateOptions.java Review UIDropDownControl.java: set javascrip
   * action UIDropDownControl.js : set this method to do
   */
  selectPageLayout : function(id, selectedIndex) {
    var dropDownControl = gj("#" + id);
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
        var selectedItem = gj(itemList[i]).find("div.SelectedItem").eq(0);
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

_module.UIItemSelector = uiItemSelector;