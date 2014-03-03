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

define("eXo.gadget.ExoBasedUserPrefStore", ["SHARED/jquery", "eXo.gadget.Gadgets"], function($, gadgets) {	
gadgets.ExoBasedUserPrefStore = function() {
  gadgets.UserPrefStore.call(this);
};

gadgets.eXoUtil.inherits(gadgets.ExoBasedUserPrefStore, gadgets.UserPrefStore);

gadgets.ExoBasedUserPrefStore.prototype.getPrefs = function(gadget) {
  return gadget.userPrefs_;
};

gadgets.ExoBasedUserPrefStore.prototype.savePrefs = function(gadget, newPrefs)
{
  var prefs = gadgets.json.stringify(newPrefs || gadget.userPrefs_);
  var encodedPrefs = encodeURIComponent(prefs);
  var ggWindow = $("#gadget_" + gadget.id);
  if (ggWindow.length > 0)
  {
    var compID = ggWindow.parent().attr("id").replace(/^content-/, "");
    var gadgetPortlet = ggWindow.closest(".UIGadgetPortlet");
    if (gadgetPortlet.length > 0)
    {
      compID = gadgetPortlet.attr("id");
    }
    var portletFrag = ggWindow.closest(".PORTLET-FRAGMENT");
    if (portletFrag.length > 0)
    {
      var ampPlain = "&";
      var ampXhtml = "&amp;";
      var amp = eXo.env.portal.urlEncoded ? ampXhtml : ampPlain;

      var portletID = portletFrag.parent().attr("id");
      var href = eXo.env.server.portalBaseURL + "?portal:componentId=" + portletID;
      href += amp + "portal:type=action" + amp + "uicomponent=" + compID;
      href += amp + "op=SaveUserPref";
      href += amp + "ajaxRequest=true";
      href += amp + "userPref=" + encodedPrefs;
      ajaxGet(href, true);
    }
    else
    {
      var params = [
        {name : "userPref", value : prefs}
      ];
      ajaxGet(eXo.env.server.createPortalURL(compID, "SaveUserPref", true, params), true);
    }
  }
};

gadgets.Container.prototype.userPrefStore = new gadgets.ExoBasedUserPrefStore();
});
