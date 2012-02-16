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
eXo.webui.UIItemSelector = {

  onOver : function(selectedElement, mouseOver) {
    if (selectedElement.className == "Item") {
      eXo.webui.UIItemSelector.beforeActionHappen(selectedElement);
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
    var allItems = xj(itemListContainer).find("div.Item").get();
    eXo.webui.UIItemSelector.beforeActionHappen(clickedElement);
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
    eXo.webui.UIItemSelector.onClick(clickedElement);
    if (eXo.webui.UIItemSelector.SelectedItem == null) {
      eXo.webui.UIItemSelector.SelectedItem = new Object();
    }
    eXo.webui.UIItemSelector.SelectedItem.component = component;
    eXo.webui.UIItemSelector.SelectedItem.option = option;
  },

  /* Pham Thanh Tung added */
  onClickOption : function(clickedElement, form, component, option) {
    var selectedItems = xj(clickedElement).closest(".ItemDetailList").find("div.SelectedItem").get();
    for ( var i = 0; i < selectedItems.length; i++) {
      selectedItems[i].className = "NormalItem";
    }
    clickedElement.className = "SelectedItem";
    if (eXo.webui.UIItemSelector.SelectedItem == null) {
      eXo.webui.UIItemSelector.SelectedItem = new Object();
    }
    eXo.webui.UIItemSelector.SelectedItem.component = component;
    eXo.webui.UIItemSelector.SelectedItem.option = option;
  },

  /* TODO: Review This Function (Ha's comment) */
  beforeActionHappen : function(selectedItem) {
    var jqObj = xj(selectedItem);
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

    this.itemDetails = xj(this.itemDetailList).find("div.ItemDetail").get();
    this.allItems = xj(this.itemList).find("div.Item").eq(0).parent().children("div.Item").get();
  },

  showPopupCategory : function(selectedNode) {
    var itemListCont = xj(selectedNode).closest(".ItemListContainer");
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
    var jqObj = xj(selectedNode);
    var itemListCont = jqObj.closest(".OverflowContainer");
    var selectedNodeIndex = eXo.webui.UIItemSelector.findIndex(selectedNode);

    var itemList = itemListCont.find("div.ItemList");
    var itemDetailList = itemListCont.find("div.ItemDetailList");

    itemList.each(function(index)
    {
      if (index == selectedNodeIndex)
      {
        xj(this).css("display", "block");
        itemDetailList.get(index).style.display = "block";
      }
      else
      {
        xj(this).css("display", "none");
        itemDetailList.get(index).style.display = "none";
      }
    });

    jqObj.closest(".UIPopupCategory").css("display", "none");
  },

  findIndex : function(object) {
    var siblings = xj(object).parent().children("div." + object.className).get();
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
    var dropDownControl = xj("#" + id);
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
        var selectedItem = xj(itemList[i]).find("div.SelectedItem").eq(0);
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
}
