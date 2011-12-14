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

eXo.webui.UIPopupSelectCategory = {

  /**
   * Hide all hidden elements, it is used while showing UIPopupCategory
   */
  hide : function() {
    var ln = eXo.core.DOMUtil.hideElementList.length;
    if (ln > 0) {
      for ( var i = 0; i < ln; i++) {
        eXo.core.DOMUtil.hideElementList[i].style.display = "none";
      }
    }
  },

  /**
   * Show UIPopupCategory object
   * 
   * @param {Object}
   *          obj document object contains UIPopupCategory
   * @param {Event}
   *          evt
   */
  show : function(obj, evt) {
    if (!evt)
      evt = window.event;
    evt.cancelBubble = true;
    var DOMUtil = eXo.core.DOMUtil;
    var uiPopupCategory = DOMUtil.findFirstDescendantByClass(obj, 'div',
        'UIPopupCategory');
    if (!uiPopupCategory)
      return;
    if (uiPopupCategory.style.display == "none") {
      eXo.webui.UIPopupSelectCategory.hide();
      uiPopupCategory.style.display = "block";
      eXo.core.DOMUtil.listHideElements(uiPopupCategory);
    } else
      uiPopupCategory.style.display = "none";
  }
}
