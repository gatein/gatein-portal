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
 * The mask layer, that appears when an ajax call waits for its result
 */
eXo.portal.UIMaskWorkspace = {

  show : function(maskId, width, height) {
    this.maskWorkpace = document.getElementById(maskId);
    if (this.maskWorkpace) {
      if (width > -1) {
        this.maskWorkpace.style.width = width + "px";
      }

      if (eXo.portal.UIMaskWorkspace.maskLayer == null) {
        var maskLayer = eXo.core.UIMaskLayer.createMask("UIPortalApplication",
            this.maskWorkpace, 30);
        eXo.portal.UIMaskWorkspace.maskLayer = maskLayer;
      }
      this.maskWorkpace.style.margin = "auto";
      this.maskWorkpace.style.display = "block";

      eXo.core.Browser.addOnResizeCallback('mid_maskWorkspace',
          eXo.portal.UIMaskWorkspace.resetPositionEvt);
    }
  },

  hide : function(maskId) {
    this.maskWorkpace = document.getElementById(maskId);
    if (eXo.portal.UIMaskWorkspace.maskLayer == undefined || !this.maskWorkpace) {
      return;
    }
    eXo.core.UIMaskLayer.removeMask(eXo.portal.UIMaskWorkspace.maskLayer);
    eXo.portal.UIMaskWorkspace.maskLayer = null;
    this.maskWorkpace.style.display = "none";
  },

  /**
   * Resets the position of the mask calls eXo.core.UIMaskLayer.setPosition to
   * perform this operation
   */
  resetPositionEvt : function() {
    var maskWorkpace = eXo.portal.UIMaskWorkspace.maskWorkpace;
    if (maskWorkpace && (maskWorkpace.style.display == "block")) {
      try {
        eXo.core.UIMaskLayer.blockContainer = document
            .getElementById("UIPortalApplication");
        eXo.core.UIMaskLayer.object = maskWorkpace;
        eXo.core.UIMaskLayer.setPosition();
      } catch (e) {
      }
    }
  }
}