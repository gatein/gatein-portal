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

      if (_module.UIMaskWorkspace.maskLayer == null) {
        var maskLayer = _module.UIMaskLayer.createMask("UIPortalApplication",
            this.maskWorkpace, 30);
        _module.UIMaskWorkspace.maskLayer = maskLayer;
      }
      this.maskWorkpace.style.margin = "auto";
      this.maskWorkpace.style.display = "block";

      var browser = _module.Browser; 
      browser.addOnResizeCallback('mid_maskWorkspace',
          _module.UIMaskWorkspace.resetPositionEvt);
      browser.addOnScrollCallback("setPosition_maskWorkspace", _module.UIMaskWorkspace.resetPositionEvt);
    }
  },

  hide : function(maskId) {
    this.maskWorkpace = document.getElementById(maskId);
    if (_module.UIMaskWorkspace.maskLayer == undefined || !this.maskWorkpace) {
      return;
    }
    _module.UIMaskLayer.removeMask(_module.UIMaskWorkspace.maskLayer);
    _module.UIMaskWorkspace.maskLayer = null;
    this.maskWorkpace.style.display = "none";
  },

  /**
   * Resets the position of the mask calls eXo.core.UIMaskLayer.setPosition to
   * perform this operation
   */
  resetPositionEvt : function() {
    var maskWorkpace = _module.UIMaskWorkspace.maskWorkpace;
    if (maskWorkpace && (maskWorkpace.style.display == "block")) {
      try {
        _module.UIMaskLayer.blockContainer = document
            .getElementById("UIPortalApplication");
        _module.UIMaskLayer.object = maskWorkpace;
        _module.UIMaskLayer.setPosition();
      } catch (e) {
      }
    }
  }
};

_module.UIMaskWorkspace = eXo.portal.UIMaskWorkspace;