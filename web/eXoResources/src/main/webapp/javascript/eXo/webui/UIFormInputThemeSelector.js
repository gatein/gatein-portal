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
eXo.webui.UIFormInputThemeSelector = {

  showThemeSelected : function(obj, param) {
    var DOMUtil = eXo.core.DOMUtil;
    var itemListContainer = DOMUtil.findAncestorByClass(obj,
        "ItemListContainer");
    var itemDetailList = DOMUtil.findNextElementByTagName(itemListContainer,
        'div');
    var detailList = DOMUtil.findFirstDescendantByClass(itemDetailList, 'div',
        'UIThemeSelector');
    var nameTheme = DOMUtil.findNextElementByTagName(detailList, 'div');
    var nameStyle = DOMUtil.findFirstDescendantByClass(obj, 'div', 'NameStyle');
    nameTheme.innerHTML = nameStyle.innerHTML;
    detailList.className = "UIThemeSelector " + param;

    // get hide input
    var itemList = obj.parentNode;
    var hidenInput = DOMUtil.findPreviousElementByTagName(itemList, 'input');
    hidenInput.value = param;
  },

  setDefaultTheme : function(obj, param) {
    var DOMUtil = eXo.core.DOMUtil;
    var itemDetailList = DOMUtil.findAncestorByClass(obj, "ItemDetailList");
    var detailList = DOMUtil.findFirstDescendantByClass(itemDetailList, 'div',
        'UIThemeSelector');
    detailList.className = "UIThemeSelector " + param;

    var nameTheme = DOMUtil.findNextElementByTagName(detailList, 'div');
    nameTheme.innerHTML = eXo.i18n.I18NMessage.getMessage("DefaultTheme");

    // get hide input
    var itemListContainer = DOMUtil.findPreviousElementByTagName(
        itemDetailList, 'div');
    var itemThemeSelector = DOMUtil.findFirstDescendantByClass(
        itemListContainer, 'div', 'ItemList');
    var hidenInput = DOMUtil.findPreviousElementByTagName(itemThemeSelector,
        'input');
    hidenInput.value = param;
  }
}